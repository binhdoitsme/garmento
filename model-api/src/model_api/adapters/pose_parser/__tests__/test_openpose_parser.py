import json
import cv2
import torch
from model_api.services.try_on import TensorImage

from ..openpose_parser import OpenPoseParser


def test__openpose_body25_parses_correctly():
    im_file = "fixtures/00057_00.jpg"
    pose_parser = OpenPoseParser()
    im = cv2.imread(im_file)
    im = cv2.cvtColor(im, cv2.COLOR_BGR2RGB)
    im_data = torch.tensor(im)
    poses, pose_map = pose_parser.parse(TensorImage(im_data, (768, 1024)))

    cv2.imwrite("result/pose.jpg", pose_map.data.numpy())
    with open("result/pose.json", "w") as w:
        w.write(json.dumps(poses.tolist()))

    assert pose_map.data.shape == (1024, 768, 3)
    assert poses.shape == (25, 3)
