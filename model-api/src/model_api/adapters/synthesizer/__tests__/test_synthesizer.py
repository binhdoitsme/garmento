import numpy as np
import torch
from model_api.services.try_on import TensorImage, TryOnResult, TryOnSynthesisInputs
from PIL import Image

from ..try_on_synthesizer_sdafnet import TryOnSynthesizerOnSDAFNet


def test__synthesizer_should_work():
    ref_image = Image.open("fixtures/000020_0.jpg")
    garment_image = Image.open("fixtures/000048_1.jpg")
    agnostic_image = Image.open("fixtures/000020_0_agnostic.jpg")
    pose_map_image = Image.open("fixtures/000020_0_keypoints.jpg")
    synthesizer = TryOnSynthesizerOnSDAFNet()
    size = (768, 1024)
    inputs = TryOnSynthesisInputs(
        reference_image=TensorImage(torch.tensor(np.array(ref_image)), size),
        garment_image=TensorImage(torch.tensor(np.array(garment_image)), size),
        agnostic_mask=torch.tensor(()),
        agnostic_img=TensorImage(torch.tensor(np.array(agnostic_image)), size),
        pose_map=TensorImage(torch.tensor(np.array(pose_map_image)), size)
    )
    result = synthesizer.synthesize(inputs)
    Image.fromarray(np.array(result.result_image.data)).save("result/tryon.jpg")
