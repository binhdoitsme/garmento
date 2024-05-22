from functools import cache
import os
from typing import Literal, NamedTuple

import cv2
import numpy as np
import torch


class VITONHDOptions(NamedTuple):
    init_type: Literal[
        "normal", "xavier", "xavier_uniform", "kaiming", "orthogonal", "none"
    ] = "xavier"
    init_variance: float = 0.02
    load_width: int = 768
    load_height: int = 1024
    grid_size: int = 5
    norm_G: str = "spectralaliasinstance"
    semantic_nc: int = 13
    num_upsampling_layers: Literal["normal", "more", "most"] = "most"
    ngf: int = 64
    device: Literal["cpu", "gpu"] = "cpu"
    version: str = "latest"

    @property
    @cache
    def checkpoints(self) -> dict[str, str]:
        if self.version != "latest":
            return {}

        return {
            "seg": "checkpoints/seg.pth",
            "gmm": "checkpoints/gmm.pth",
            "alias": "checkpoints/alias.pth",
        }


def load_checkpoint(model: torch.nn.Module, checkpoint_path: str):
    if not os.path.exists(checkpoint_path):
        raise ValueError("'{}' is not a valid checkpoint path".format(checkpoint_path))
    model.load_state_dict(torch.load(checkpoint_path))


def gen_noise(shape: tuple[int, ...]):
    noise = np.zeros(shape, dtype=np.uint8)
    ### noise
    noise = cv2.randn(noise, 0, 255)  # type: ignore
    noise = np.asarray(noise / 255, dtype=np.uint8)
    return torch.tensor(noise, dtype=torch.float32)
