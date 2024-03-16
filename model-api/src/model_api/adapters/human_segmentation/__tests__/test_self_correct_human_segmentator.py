import cv2
import torch
from model_api.services.try_on import TensorImage

from ..self_correct_human_segmentator import SelfCorrectedHumanSegmentator


def test__self_corrected_human_segmentator_parses_correctly():
    im_file = "fixtures/00057_00.jpg"
    parser = SelfCorrectedHumanSegmentator()
    im = cv2.imread(im_file)
    im_data = torch.tensor(im)
    result = parser.parse(TensorImage(im_data, (768, 1024)))
    cv2.imwrite("result/human_segments.jpg", result.data.numpy())
    assert result.data.shape == (1024, 768)
