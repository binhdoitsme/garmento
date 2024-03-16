import json
from PIL import Image
import numpy as np
import torch
from torchvision import transforms  # type: ignore

from model_api.services.try_on import TensorImage
from ..pose_and_segment_agnostic import PoseAndSegmentAgnosticParser


def test__agnostic_parser_should_work__given_correct_input():
    segmented = Image.open("fixtures/00057_00_segments.jpg")
    ref_img = Image.open("fixtures/00057_00.jpg")
    pose_data = np.array(json.load(open("fixtures/00057_00.json")))
    parser = PoseAndSegmentAgnosticParser()
    size = (768, 1024)
    agnostic_mask, agnostic_img = parser.parse(
        (
            torch.tensor(pose_data),
            TensorImage(torch.tensor(np.array(segmented)), size),
            TensorImage(torch.tensor(np.array(ref_img)), size),
        )
    )
    Image.fromarray(agnostic_img.data.numpy()).save("result/agnostic_step_2.jpg")
    print(agnostic_mask, agnostic_img)
    assert agnostic_mask.shape == (13, 1024, 768)
    assert agnostic_img.data.shape == (1024, 768, 3)
