from __future__ import annotations

from dataclasses import dataclass
from typing import Protocol

import cv2
import torch


Height = int
Width = int
ImageSize = tuple[Height, Width]  # (height, width)


# -------- Data structure --------
@dataclass
class TensorImage:
    data: torch.Tensor
    size: ImageSize
    channels: int = 3  # 3 = RGB, 1 = Lightness

    def resize(self, new_size: ImageSize):
        new_data = cv2.resize(self.data.numpy(), new_size)
        return TensorImage(torch.tensor(new_data), new_size, self.channels)

    def from_image(self, im: cv2.typing.MatLike) -> TensorImage:
        height, width, channels = im.shape
        assert channels in (1, 3)
        return TensorImage(torch.Tensor(im), (height, width), channels)


@dataclass
class TryOnInputs:
    reference_image: TensorImage
    garment_image: TensorImage
    size: ImageSize

    def __post_init__(self): ...


@dataclass
class TryOnSynthesisInputs:
    reference_image: TensorImage
    garment_image: TensorImage
    agnostic_mask: torch.Tensor
    agnostic_img: TensorImage
    pose_map: TensorImage


@dataclass
class TryOnResult:
    result_image: TensorImage


# -------- Interfaces --------
class PoseParser(Protocol):
    def parse(self, inputs: TensorImage) -> tuple[torch.Tensor, TensorImage]: ...


class HumanSegmentator(Protocol):
    def parse(self, inputs: TensorImage) -> TensorImage: ...


class AgnosticParser(Protocol):
    def parse(
        self, inputs: tuple[torch.Tensor, TensorImage, TensorImage]
    ) -> tuple[torch.Tensor, TensorImage]:
        """(Pose, segmented, ref_image) -> (agnostic_mask, agnostic_image)"""
        ...


class TryOnSynthesizer(Protocol):
    def synthesize(self, inputs: TryOnSynthesisInputs) -> TryOnResult: ...


# -------- Main service --------
IMG_SIZE = (768, 1024)


@dataclass
class TryOnInferrer:
    pose_parser: PoseParser
    human_segmentator: HumanSegmentator
    agnostic_mask_parser: AgnosticParser
    synthesizer: TryOnSynthesizer
    input_size = IMG_SIZE
    output_size = IMG_SIZE

    def execute(self, inputs: TryOnInputs) -> TryOnResult:
        reference_image = inputs.reference_image
        garment_image = inputs.garment_image
        human_segments = self.human_segmentator.parse(reference_image)
        pose, pose_map = self.pose_parser.parse(human_segments)
        agnostic_mask, agnostic_img = self.agnostic_mask_parser.parse(
            (pose, human_segments, reference_image)
        )
        synthesis_inputs = TryOnSynthesisInputs(
            reference_image=reference_image,
            garment_image=garment_image,
            pose_map=pose_map,
            agnostic_mask=agnostic_mask,
            agnostic_img=agnostic_img,
        )
        return self.synthesizer.synthesize(synthesis_inputs)
