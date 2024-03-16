from __future__ import annotations

import cv2
import numpy as np
import torch
from model_api.services.try_on import HumanSegmentator
from onnxruntime import InferenceSession  # type: ignore
from torchvision import transforms  # type: ignore
from torchvision.transforms import functional as F  # type: ignore
from PIL import Image  # type: ignore

from ...services.try_on import TensorImage
from .transforms import get_meta, transform_logits


class SelfCorrectedHumanSegmentator(HumanSegmentator):
    model_input_size = (473, 473)
    model_output_size = (768, 1024)

    def __init__(self, model_path="model/self_corrected_human_parser.onnx"):
        self.model = InferenceSession(model_path)
        self.transform = transforms.Compose(
            [
                transforms.ToTensor(),
                transforms.Normalize(
                    mean=[0.406, 0.456, 0.485], std=[0.225, 0.224, 0.229]
                ),
            ]
        )

    def parse(self, inputs: TensorImage) -> TensorImage:
        tensor_data = inputs.data
        as_numpy = tensor_data.numpy()
        # resized = cv2.resize(as_numpy, self.model_input_size)
        cv2.imwrite("result/human_segments_step_1.jpg", as_numpy)
        # (h, w, c) -> (c, h, w)
        print("asnumpy", as_numpy.shape)
        im, meta = get_meta(
            as_numpy, output_size=self.model_input_size, transform=self.transform
        )
        c, s, w, h = meta["center"], meta["scale"], meta["width"], meta["height"]
        print(c, s, w, h)
        cv2.imwrite("result/human_segments_step_2.jpg", np.array(transforms.ToPILImage()(im)))
        model_inputs = np.expand_dims(
            F.gaussian_blur(im, [15, 15], [4.0]).numpy(), 0
        ).astype(np.float32)
        output = torch.tensor(self.model.run(None, {"image": model_inputs})[0])
        print(output.shape)

        upsample = torch.nn.Upsample(
            size=self.model_input_size, mode="bicubic", align_corners=True
        )
        upsample_output: torch.Tensor = upsample(output[0].unsqueeze(0))
        upsample_output = upsample_output.squeeze()
        upsample_output = upsample_output.permute(1, 2, 0)  # CHW -> HWC

        logits_result = transform_logits(
            upsample_output.data.cpu().numpy(),
            c,
            s,
            w,
            h,
            input_size=self.model_input_size,
        )
        parsing_result = np.argmax(logits_result, axis=2)
        output_arr = np.asarray(parsing_result, dtype=np.uint8)

        parsed_im = Image.fromarray(output_arr).resize(
            self.model_output_size, resample=Image.BILINEAR
        )
        parsed_im.save("result/human_segments_step_3.jpg")

        return TensorImage(
            torch.tensor(np.array(parsed_im)),
            size=(output.shape[-2], output.shape[-1]),
            channels=1,
        )
