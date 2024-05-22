from typing import TypedDict

import torch


class VITONHDInputs(TypedDict):
    img_agnostic: torch.Tensor
    parse_agnostic: torch.Tensor
    pose: torch.Tensor
    cloth: torch.Tensor
    cloth_mask: torch.Tensor
