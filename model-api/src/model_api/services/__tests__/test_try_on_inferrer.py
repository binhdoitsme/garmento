import pytest
import torch
from model_api.services.try_on import (
    IMG_SIZE,
    AgnosticParser,
    HumanSegmentator,
    PoseParser,
    TensorImage,
    TryOnInferrer,
    TryOnInputs,
    TryOnResult,
    TryOnSynthesizer,
)
from pytest_mock import MockerFixture


def get_full_name(cls: type):
    return f"{cls.__module__}.{cls.__name__}"


def test__should_output_correct_dimensions__given_invalid_input_dimensions(
    mocker: MockerFixture,
):
    human_segmentator = mocker.patch(get_full_name(HumanSegmentator))
    pose_parser = mocker.patch(get_full_name(PoseParser))
    agnostic_mask_parser = mocker.patch(get_full_name(AgnosticParser))
    synthesizer = mocker.patch(get_full_name(TryOnSynthesizer))
    try_on_inferrer = TryOnInferrer(
        pose_parser=pose_parser,
        human_segmentator=human_segmentator,
        agnostic_mask_parser=agnostic_mask_parser,
        synthesizer=synthesizer,
    )
    
    fake_input_tensor = torch.zeros([3, *IMG_SIZE])
    pose_parser.parse.return_value = (1, 2)
    agnostic_mask_parser.parse.return_value = (1, 2)
    synthesizer.synthesize.return_value = TryOnResult(
        TensorImage(fake_input_tensor, IMG_SIZE)
    )

    fake_inputs = TryOnInputs(
        reference_image=TensorImage(fake_input_tensor, IMG_SIZE),
        garment_image=TensorImage(fake_input_tensor, IMG_SIZE),
        size=IMG_SIZE,
    )
    outputs = try_on_inferrer.execute(fake_inputs)
    assert outputs.result_image.size == IMG_SIZE
    assert outputs.result_image.data.shape == fake_input_tensor.shape
