import io
import pytest

from src.model_api.services.viton_hd.data_loader import to_model_inputs


@pytest.fixture
def correct_inputs() -> dict[str, io.IOBase]:
    return {
        "ref_image": open("fixtures/00057_00.jpg", mode="rb"),
        "garment_image": open("fixtures/00055_00.jpg", mode="rb"),
        "masked_garment_image": open("fixtures/00055_00_mask.jpg", mode="rb"),
        "densepose_image": open("fixtures/00057_00_rendered.png", mode="rb"),
        "segmented_image": open("fixtures/00057_00_segments.png", mode="rb"),
        "pose_keypoints": open("fixtures/00057_00.json", mode="r"),
    }


def test__should_give_correct_output__given_correct_input(correct_inputs):
    converted = to_model_inputs(
        ref_image=correct_inputs["ref_image"],
        garment_image=correct_inputs["garment_image"],
        densepose_image=correct_inputs["densepose_image"],
        masked_garment_image=correct_inputs["masked_garment_image"],
        segmented_image=correct_inputs["segmented_image"],
        pose_keypoints=correct_inputs["pose_keypoints"],
    )
    assert "img_agnostic" in converted
    assert "parse_agnostic" in converted
    assert "pose" in converted
    assert "cloth" in converted
    assert "cloth_mask" in converted
