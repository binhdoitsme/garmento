import numpy as np
import numpy.typing as npt
from PIL import Image, ImageDraw
import torch


def get_parse_agnostic(
    im_parse: Image.Image, pose_data: npt.NDArray[np.float32], w=768, h=1024
):
    parse_array = np.array(im_parse, dtype=np.uint8)
    upper = np.array((parse_array == 5) + (parse_array == 6) + (parse_array == 7))
    upper = upper.astype(np.float32)
    neck = (parse_array == 10).astype(np.float32)

    r = 10
    agnostic = im_parse.copy()

    # mask arms
    for parse_id, pose_ids in ((14, [2, 5, 6, 7]), (15, [5, 2, 3, 4])):
        mask_arm = Image.new("L", (w, h), "black")
        mask_arm_draw = ImageDraw.Draw(mask_arm)
        i_prev = pose_ids[0]
        for i in pose_ids[1:]:
            is_already_masked_out = (
                pose_data[i_prev, 0] == 0.0 and pose_data[i_prev, 1] == 0.0
            ) or (pose_data[i, 0] == 0.0 and pose_data[i, 1] == 0.0)
            if is_already_masked_out:
                continue
            mask_arm_draw.line(
                [(pose_data[j][0], pose_data[j][1]) for j in [i_prev, i]],
                "white",
                width=r * 10,
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
    agnostic.paste(0, None, Image.fromarray(np.uint8(upper * 255), "L"))
    agnostic.paste(0, None, Image.fromarray(np.uint8(neck * 255), "L"))
    return agnostic


def to_tuple2(l: npt.NDArray[np.float32]):
    return float(l[0]), float(l[1])


def get_img_agnostic(img: Image.Image, parse: Image.Image, pose_data: npt.NDArray[np.float32]):
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
    agnostic_draw.line([to_tuple2(pose_data[i]) for i in [2, 5]], "black", width=r * 10)
    for i in [2, 5]:
        pointx, pointy = pose_data[i]
        agnostic_draw.ellipse(
            (pointx - r * 5, pointy - r * 5, pointx + r * 5, pointy + r * 5),
            "black",
            "black",
        )
    for i in [3, 4, 6, 7]:
        if (pose_data[i - 1, 0] == 0.0 and pose_data[i - 1, 1] == 0.0) or (
            pose_data[i, 0] == 0.0 and pose_data[i, 1] == 0.0
        ):
            continue
        agnostic_draw.line(
            [to_tuple2(pose_data[j]) for j in [i - 1, i]], "black", width=r * 10
        )
        pointx, pointy = pose_data[i]
        agnostic_draw.ellipse(
            (pointx - r * 5, pointy - r * 5, pointx + r * 5, pointy + r * 5),
            "black",
            "black",
        )

    # mask torso
    for i in [9, 12]:
        pointx, pointy = pose_data[i]
        agnostic_draw.ellipse(
            (pointx - r * 3, pointy - r * 6, pointx + r * 3, pointy + r * 6),
            "black",
            "black",
        )
    agnostic_draw.line([to_tuple2(pose_data[i]) for i in [2, 9]], "black", width=r * 6)
    agnostic_draw.line([to_tuple2(pose_data[i]) for i in [5, 12]], "black", width=r * 6)
    agnostic_draw.line([to_tuple2(pose_data[i]) for i in [9, 12]], "black", width=r * 12)
    agnostic_draw.polygon([to_tuple2(pose_data[i]) for i in [2, 5, 12, 9]], "black", "black")

    # mask neck
    pointx, pointy = pose_data[1]
    agnostic_draw.rectangle(
        (pointx - r * 7, pointy - r * 7, pointx + r * 7, pointy + r * 7), "black", "black"
    )
    agnostic.paste(img, None, Image.fromarray(np.uint8(parse_head * 255), "L"))
    agnostic.paste(img, None, Image.fromarray(np.uint8(parse_lower * 255), "L"))

    return agnostic
