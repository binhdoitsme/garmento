import numpy as np
import torch
from PIL import Image
from torch.nn import Upsample
from torch.nn import functional as F
from torchvision.transforms import GaussianBlur  # type: ignore

from .inputs import VITONHDInputs
from .networks import GMM, ALIASGenerator, SegGenerator
from .utils import VITONHDOptions, gen_noise, load_checkpoint  # type: ignore


class VITONHDModel:
    def __init__(self, opt=VITONHDOptions()):
        self.options = opt
        self.seg = SegGenerator(
            opt, input_nc=opt.semantic_nc + 8, output_nc=opt.semantic_nc
        )
        self.gmm = GMM(opt, inputA_nc=7, inputB_nc=3)
        self.alias = ALIASGenerator(opt._replace(semantic_nc=7), input_nc=9)
        load_checkpoint(self.seg, opt.checkpoints["seg"])
        load_checkpoint(self.gmm, opt.checkpoints["gmm"])
        load_checkpoint(self.alias, opt.checkpoints["alias"])

        self.device = (
            opt.device
            if opt.device == "cpu"
            else "cuda" if torch.cuda.is_available() else "mps"
        )
        self.seg.to(self.device).eval()
        self.gmm.to(self.device).eval()
        self.alias.to(self.device).eval()

        upsample_size = (opt.load_height, opt.load_width)
        self.up = Upsample(size=upsample_size, mode="bilinear").to(self.device)
        self.gauss = GaussianBlur(kernel_size=(15, 15), sigma=(3, 3)).to(self.device)

    def infer(self, inputs: VITONHDInputs) -> Image.Image:
        with torch.no_grad():
            img_agnostic = inputs["img_agnostic"].to(self.device)
            parse_agnostic = inputs["parse_agnostic"].to(self.device)
            pose = inputs["pose"].to(self.device)
            c = inputs["cloth"].to(self.device)
            cm = inputs["cloth_mask"].to(self.device)
            opt = self.options

            # Part 1. Segmentation generation
            parse_agnostic_down = F.interpolate(
                parse_agnostic, size=(256, 192), mode="bilinear"
            )
            pose_down = F.interpolate(pose, size=(256, 192), mode="bilinear")
            c_masked_down = F.interpolate(c * cm, size=(256, 192), mode="bilinear")
            cm_down = F.interpolate(cm, size=(256, 192), mode="bilinear")
            seg_input = torch.cat(
                (
                    cm_down,
                    c_masked_down,
                    parse_agnostic_down,
                    pose_down,
                    gen_noise(cm_down.size()).to(self.device),
                ),
                dim=1,
            )

            parse_pred_down = self.seg(seg_input)
            parse_pred = self.gauss(self.up(parse_pred_down))
            parse_pred = parse_pred.argmax(dim=1)[:, None]

            parse_old = torch.zeros(
                parse_pred.size(0), 13, opt.load_height, opt.load_width, dtype=torch.float
            ).to(self.device)
            parse_old.scatter_(1, parse_pred, 1.0)

            labels = {
                0: ("background", [0]),
                1: ("paste", [2, 4, 7, 8, 9, 10, 11]),
                2: ("upper", [3]),
                3: ("hair", [1]),
                4: ("left_arm", [5]),
                5: ("right_arm", [6]),
                6: ("noise", [12]),
            }
            parse = torch.zeros(
                parse_pred.size(0), 7, opt.load_height, opt.load_width, dtype=torch.float
            ).to(self.device)
            for j in range(len(labels)):
                for label in labels[j][1]:
                    parse[:, j] += parse_old[:, label]

            # Part 2. Clothes Deformation
            agnostic_gmm = F.interpolate(img_agnostic, size=(256, 192), mode="nearest")
            parse_cloth_gmm = F.interpolate(parse[:, 2:3], size=(256, 192), mode="nearest")
            pose_gmm = F.interpolate(pose, size=(256, 192), mode="nearest")
            c_gmm = F.interpolate(c, size=(256, 192), mode="nearest")
            gmm_input = torch.cat((parse_cloth_gmm, pose_gmm, agnostic_gmm), dim=1)

            _, warped_grid = self.gmm(gmm_input, c_gmm)
            warped_c = F.grid_sample(c, warped_grid, padding_mode="border")
            warped_cm = F.grid_sample(cm, warped_grid, padding_mode="border")

            # Part 3. Try-on synthesis
            misalign_mask = parse[:, 2:3] - warped_cm
            misalign_mask[misalign_mask < 0.0] = 0.0
            parse_div = torch.cat((parse, misalign_mask), dim=1)
            parse_div[:, 2:3] -= misalign_mask

            output = self.alias(
                torch.cat((img_agnostic, pose, warped_c), dim=1),
                parse,
                parse_div,
                misalign_mask,
            )
            result = output[0].clone().detach()

            tensor = (result.clone() + 1) * 0.5 * 255
            tensor = tensor.cpu().clamp(0, 255)

            try:
                array: np.ndarray = tensor.numpy().astype("uint8")
            except:
                array = tensor.detach().numpy().astype("uint8")

            if array.shape[0] == 1:
                array = array.squeeze(0)
            elif array.shape[0] == 3:
                array = array.swapaxes(0, 1).swapaxes(1, 2)

            return Image.fromarray(array)
