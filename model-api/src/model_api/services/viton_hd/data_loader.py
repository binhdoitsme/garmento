import json
from typing import IO, Any, Callable

import numpy as np
import numpy.typing as npt
import torch
from PIL import Image, ImageDraw
from torchvision import transforms  # type: ignore
from torchvision.transforms import InterpolationMode  # type: ignore

from .inputs import VITONHDInputs


def first_two(data: npt.NDArray[np.float64]):
    return data[0], data[1]


def get_parse_agnostic(
    parse: Image.Image,
    pose_data: npt.NDArray[np.float64],
    load_width: int = 768,
    load_height: int = 1024,
):
    parse_array = np.array(parse)
    parse_upper = (
        (parse_array == 5).astype(np.float32)
        + (parse_array == 6).astype(np.float32)
        + (parse_array == 7).astype(np.float32)
    )
    parse_neck = (parse_array == 10).astype(np.float32)

    r = 10
    agnostic = parse.copy()

    # mask arms
    for parse_id, pose_ids in [(14, [2, 5, 6, 7]), (15, [5, 2, 3, 4])]:
        mask_arm = Image.new("L", (load_width, load_height), "black")
        mask_arm_draw = ImageDraw.Draw(mask_arm)
        i_prev = pose_ids[0]
        for i in pose_ids[1:]:
            if (pose_data[i_prev, 0] == 0.0 and pose_data[i_prev, 1] == 0.0) or (
                pose_data[i, 0] == 0.0 and pose_data[i, 1] == 0.0
            ):
                continue
            mask_arm_draw.line(
                [first_two(pose_data[j]) for j in [i_prev, i]], "white", width=r * 10
            )
            pointx, pointy = pose_data[i]
            radius = r * 4 if i == pose_ids[-1] else r * 15
            mask_arm_draw.ellipse(
                (pointx - radius, pointy - radius, pointx + radius, pointy + radius),
                "white",
                "white",
            )
            i_prev = i
        parse_arm = (np.array(mask_arm) / 255) * (parse_array == parse_id).astype(
            np.float32
        )
        agnostic.paste(0, None, Image.fromarray(np.uint8(parse_arm * 255), "L"))

    # mask torso & neck
    agnostic.paste(0, None, Image.fromarray(np.uint8(parse_upper * 255), "L"))
    agnostic.paste(0, None, Image.fromarray(np.uint8(parse_neck * 255), "L"))

    return agnostic


def get_img_agnostic(
    img: Image.Image, parse: Image.Image, pose_data: npt.NDArray[np.float64]
):
    parse_array = np.array(parse)
    parse_head = (parse_array == 4).astype(np.float32) + (parse_array == 13).astype(
        np.float32
    )
    parse_lower = (
        (parse_array == 9).astype(np.float32)
        + (parse_array == 12).astype(np.float32)
        + (parse_array == 16).astype(np.float32)
        + (parse_array == 17).astype(np.float32)
        + (parse_array == 18).astype(np.float32)
        + (parse_array == 19).astype(np.float32)
    )

    r = 20
    agnostic = img.copy()
    agnostic_draw = ImageDraw.Draw(agnostic)

    length_a = np.linalg.norm(pose_data[5] - pose_data[2])
    length_b = np.linalg.norm(pose_data[12] - pose_data[9])
    point = (pose_data[9] + pose_data[12]) / 2
    pose_data[9] = point + (pose_data[9] - point) / length_b * length_a
    pose_data[12] = point + (pose_data[12] - point) / length_b * length_a

    # mask arms
    agnostic_draw.line([first_two(pose_data[i]) for i in [2, 5]], "gray", width=r * 10)
    for i in [2, 5]:
        pointx, pointy = pose_data[i]
        agnostic_draw.ellipse(
            (pointx - r * 5, pointy - r * 5, pointx + r * 5, pointy + r * 5),
            "gray",
            "gray",
        )
    for i in [3, 4, 6, 7]:
        if (pose_data[i - 1, 0] == 0.0 and pose_data[i - 1, 1] == 0.0) or (
            pose_data[i, 0] == 0.0 and pose_data[i, 1] == 0.0
        ):
            continue
        agnostic_draw.line(
            [first_two(pose_data[j]) for j in [i - 1, i]], "gray", width=r * 10
        )
        pointx, pointy = pose_data[i]
        agnostic_draw.ellipse(
            (pointx - r * 5, pointy - r * 5, pointx + r * 5, pointy + r * 5),
            "gray",
            "gray",
        )

    # mask torso
    for i in [9, 12]:
        pointx, pointy = pose_data[i]
        agnostic_draw.ellipse(
            (pointx - r * 3, pointy - r * 6, pointx + r * 3, pointy + r * 6),
            "gray",
            "gray",
        )
    agnostic_draw.line([first_two(pose_data[i]) for i in [2, 9]], "gray", width=r * 6)
    agnostic_draw.line([first_two(pose_data[i]) for i in [5, 12]], "gray", width=r * 6)
    agnostic_draw.line([first_two(pose_data[i]) for i in [9, 12]], "gray", width=r * 12)
    agnostic_draw.polygon(
        [first_two(pose_data[i]) for i in [2, 5, 12, 9]], "gray", "gray"
    )

    # mask neck
    pointx, pointy = pose_data[1]
    agnostic_draw.rectangle(
        (pointx - r * 7, pointy - r * 7, pointx + r * 7, pointy + r * 7), "gray", "gray"
    )
    agnostic.paste(img, None, Image.fromarray(np.uint8(parse_head * 255), "L"))
    agnostic.paste(img, None, Image.fromarray(np.uint8(parse_lower * 255), "L"))

    return agnostic


labels: dict[int, tuple[str, list[int]]] = {
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

transform: Callable[..., torch.Tensor] = transforms.Compose(
    [transforms.ToTensor(), transforms.Normalize((0.5, 0.5, 0.5), (0.5, 0.5, 0.5))]
)


def to_model_inputs(
    ref_image: IO[bytes],
    garment_image: IO[bytes],
    densepose_image: IO[bytes],
    masked_garment_image: IO[bytes],
    segmented_image: IO[bytes],
    pose_keypoints: IO[bytes],
    load_width=768,
    load_height=1024,
    semantic_nc=13,
) -> VITONHDInputs:
    c = Image.open(garment_image).convert("RGB")
    c = transforms.Resize(load_width, interpolation=InterpolationMode.BILINEAR)(c)
    cm = Image.open(masked_garment_image)
    cm = transforms.Resize(load_width, interpolation=InterpolationMode.NEAREST)(cm)

    cloth = transform(c)  # [-1,1]
    cm_array = np.array(cm)
    cm_array = (cm_array >= 128).astype(np.float32)
    cloth_mask = torch.from_numpy(cm_array).unsqueeze(0)  # [0,1]

    # load pose image
    pose_rgb = Image.open(densepose_image)
    pose_rgb = transforms.Resize(load_width, interpolation=InterpolationMode.BILINEAR)(
        pose_rgb
    )

    pose_label = json.load(pose_keypoints)
    pose_data = pose_label["people"][0]["pose_keypoints_2d"]
    pose_data = np.array(pose_data)
    pose_data = pose_data.reshape((-1, 3))[:, :2]

    # load parsing image
    parse = Image.open(segmented_image)
    parse = transforms.Resize(load_width, interpolation=InterpolationMode.NEAREST)(
        parse
    )
    parse_agnostic = get_parse_agnostic(parse, pose_data)
    parse_agnostic = torch.from_numpy(np.array(parse_agnostic)[None]).long()
    parse_agnostic_map = torch.zeros(20, load_height, load_width, dtype=torch.float)
    parse_agnostic_map.scatter_(0, parse_agnostic, 1.0)
    new_parse_agnostic_map = torch.zeros(
        semantic_nc, load_height, load_width, dtype=torch.float
    )
    for i in range(len(labels)):
        for label in labels[i][1]:
            new_parse_agnostic_map[i] += parse_agnostic_map[label]

    # load person image
    img = Image.open(ref_image)
    img = transforms.Resize(load_width, interpolation=InterpolationMode.BILINEAR)(img)
    img_agnostic: Any = get_img_agnostic(img, parse, pose_data)

    return {
        "img_agnostic": transform(img_agnostic).unsqueeze(0),
        "parse_agnostic": new_parse_agnostic_map.unsqueeze(0),
        "pose": transform(pose_rgb).unsqueeze(0),
        "cloth": cloth.unsqueeze(0),
        "cloth_mask": cloth_mask.unsqueeze(0),
    }
