from dataclasses import dataclass

import numpy as np
import numpy.typing as npt
import torch

from ...services.try_on import PoseParser, TensorImage
from .torch_openpose import OpenPose
from .util import draw_pose


@dataclass
class OpenPoseParser(PoseParser):
    model: OpenPose = OpenPose("body_25")

    def parse(self, inputs: TensorImage) -> tuple[torch.Tensor, TensorImage]:
        img: npt.NDArray[np.uint8] = inputs.data.numpy()
        pose_data = self.model(img)
        pose_map = draw_pose(img, pose_data, "body_25")
        h, w, c = pose_map.shape
        poses = torch.tensor(pose_data).squeeze()
        out_img = TensorImage(torch.as_tensor(pose_map), (h, w), c)
        print("poses:", poses.shape, "out_img:", out_img.data.shape)
        return poses, out_img
