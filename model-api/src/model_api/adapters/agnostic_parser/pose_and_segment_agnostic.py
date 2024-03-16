from typing import Sequence

import numpy as np
import torch
from model_api.services.try_on import AgnosticParser
from PIL import Image

from ...services.try_on import TensorImage
from .masks import get_parse_agnostic, get_img_agnostic


class PoseAndSegmentAgnosticParser(AgnosticParser):
    semantic_num_classes = 13
    load_size = (1024, 768)  # h, w
    human_segment_labels: dict[int, tuple[str, Sequence[int]]] = {
        0: ("background", [0, 10]),
        1: ("hair", [1, 2]),
        2: ("face", [4, 13]),
        3: ("upper", [5, 6, 7]),
        4: ("bottom", [9, 12]),
        5: ("left_arm", [14]),
        6: ("right_arm", [15]),
        7: ("left_leg", [16]),
        8: ("right_leg", [17]),
        9: ("left_shoe", [18]),
        10: ("right_shoe", [19]),
        11: ("socks", [8]),
        12: ("noise", [3, 11]),
    }

    def parse(
        self, inputs: tuple[torch.Tensor, TensorImage, TensorImage]
    ) -> tuple[torch.Tensor, TensorImage]:
        _pose_data, segmented, ref_image = inputs
        print("segmented", segmented.data.max())
        assert _pose_data.shape == (25, 3)
        pose_data = np.array(_pose_data[:, :2])
        segmented_im = Image.fromarray(segmented.data.numpy())
        mask = get_parse_agnostic(segmented_im, pose_data)
        parse_agnostic = torch.from_numpy(np.array(mask)[None]).long()
        print("parse_agnostic",parse_agnostic.shape)
        print("parse_agnostic__", parse_agnostic.max())
        load_height, load_width = self.load_size

        parse_agnostic_map = torch.zeros(21, load_height, load_width, dtype=torch.float)
        print("parse_agnostic_map",parse_agnostic_map.shape)
        parse_agnostic_map.scatter_(0, parse_agnostic, 1.0)
        new_parse_agnostic_map = torch.zeros(
            self.semantic_num_classes, load_height, load_width, dtype=torch.float
        )
        print("new_parse_agnostic_map",new_parse_agnostic_map.shape)
        for i in range(len(self.human_segment_labels)):
            for label in self.human_segment_labels[i][1]:
                new_parse_agnostic_map[i] += parse_agnostic_map[label]

        img_agnostic = get_img_agnostic(
            Image.fromarray(ref_image.data.numpy()),
            Image.fromarray(segmented.data.numpy()),
            pose_data,
        )

        return (
            new_parse_agnostic_map,
            TensorImage(torch.tensor(np.array(img_agnostic)), self.load_size),
        )
