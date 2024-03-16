import numpy as np
import onnxruntime as ort  # type: ignore
import torch  # type: ignore
from model_api.services.try_on import TryOnSynthesizer
from PIL import Image
from torch.nn import functional as F  # type: ignore
from torchvision import transforms  # type: ignore

from ...services.try_on import TensorImage, TryOnResult, TryOnSynthesisInputs


class TryOnSynthesizerOnSDAFNet(TryOnSynthesizer):
    model_input_size = (256, 192)

    def __init__(self, model_path="model/sdafnet.onnx"):
        self.model = ort.InferenceSession(model_path)
        self.transform = transforms.Compose(
            [
                transforms.Resize(self.model_input_size),
                transforms.ToTensor(),
                transforms.Normalize(mean=[0.5, 0.5, 0.5], std=[0.5, 0.5, 0.5]),
            ]
        )

    def synthesize(self, inputs: TryOnSynthesisInputs) -> TryOnResult:
        print(inputs.garment_image.data.shape)
        garment_image = self.transform(
            Image.fromarray(inputs.garment_image.data.numpy())
        )
        agnostic_img = self.transform(Image.fromarray(inputs.agnostic_img.data.numpy()))
        pose_map = self.transform(Image.fromarray(inputs.pose_map.data.numpy()))
        ref_input = torch.cat((pose_map.unsqueeze(0), agnostic_img.unsqueeze(0)), dim=1)
        model_inputs = {
            "ref_input": ref_input.cpu().numpy(),
            "garment": garment_image.unsqueeze(0).cpu().numpy(),
            "img_agnostic": agnostic_img.unsqueeze(0).cpu().numpy(),
        }
        model_outputs = self.model.run(None, model_inputs)
        result = torch.tensor(model_outputs[0])[0]
        return TryOnResult(
            TensorImage(
                torch.tensor(
                    np.array(transforms.ToPILImage()(result).resize((768, 1024)))
                ),
                (1024, 768),
            )
        )
