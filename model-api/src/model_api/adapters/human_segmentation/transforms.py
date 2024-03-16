# ------------------------------------------------------------------------------
# Copyright (c) Microsoft
# Licensed under the MIT License.
# Written by Bin Xiao (Bin.Xiao@microsoft.com)
# binhdh: Added types for type safety, merged with other preprocessing code
# ------------------------------------------------------------------------------

from __future__ import absolute_import, division, print_function

from typing import Any, Sequence

import cv2
import numpy as np
import torch
from numpy import typing as npt
from torchvision import transforms  # type: ignore


def transform_logits(
    logits: npt.NDArray[np.generic],
    center: npt.NDArray[np.float32],
    scale: npt.NDArray[np.float32],
    width: int,
    height: int,
    input_size: tuple[int, int],
):
    trans = get_affine_transform(center, scale, 0, input_size, inv=1)
    channel = logits.shape[2]
    target_logits = list[npt.NDArray[np.generic]]()
    for i in range(channel):
        target_logit = cv2.warpAffine(
            logits[:, :, i],
            trans,
            (width, height),
            flags=cv2.INTER_LINEAR,
            borderMode=cv2.BORDER_CONSTANT,
            borderValue=(0,),
        )
        target_logits.append(target_logit)
    return np.stack(target_logits, axis=2)


def get_affine_transform(
    center: npt.NDArray[np.float32],
    scale: npt.NDArray[np.float32] | list[float] | float,
    rot: float,
    output_size: tuple[int, int],
    shift=np.array([0, 0], dtype=np.float32),
    inv=0,
) -> npt.NDArray[np.float32]:
    if not isinstance(scale, np.ndarray) and not isinstance(scale, list):
        print(scale)
        scale = np.array([scale, scale], dtype=np.float32)

    scale_tmp = scale

    src_w = scale_tmp[0]
    dst_w = output_size[1]
    dst_h = output_size[0]

    rot_rad = np.pi * rot / 180
    src_dir = get_dir([0, src_w * -0.5], rot_rad)
    dst_dir = np.array([0, (dst_w - 1) * -0.5], np.float32)

    src = np.zeros((3, 2), dtype=np.float32)
    dst = np.zeros((3, 2), dtype=np.float32)
    src[0, :] = center + scale_tmp * shift
    src[1, :] = center + src_dir + scale_tmp * shift
    dst[0, :] = [(dst_w - 1) * 0.5, (dst_h - 1) * 0.5]
    dst[1, :] = np.array([(dst_w - 1) * 0.5, (dst_h - 1) * 0.5]) + dst_dir

    src[2:, :] = get_3rd_point(src[0, :], src[1, :])
    dst[2:, :] = get_3rd_point(dst[0, :], dst[1, :])

    if inv:
        trans = cv2.getAffineTransform(
            np.array(dst, dtype=np.float32), np.array(src, dtype=np.float32)
        )
    else:
        trans = cv2.getAffineTransform(
            np.array(src, dtype=np.float32), np.array(dst, dtype=np.float32)
        )

    return np.array(trans, dtype=np.float32)


def get_3rd_point(a, b):
    direct = a - b
    return b + np.array([-direct[1], direct[0]], dtype=np.float32)


def get_dir(src_point, rot_rad):
    sn, cs = np.sin(rot_rad), np.cos(rot_rad)

    src_result = [0, 0]
    src_result[0] = src_point[0] * cs - src_point[1] * sn
    src_result[1] = src_point[0] * sn + src_point[1] * cs

    return src_result


# ----------------------------------------


def _xywh2cs(x: int, y: int, w: float, h: float, aspect_ratio: float):
    center = np.zeros((2), dtype=np.float32)
    center[0] = x + w * 0.5
    center[1] = y + h * 0.5
    if w > aspect_ratio * h:
        h = w * 1.0 / aspect_ratio
    elif w < aspect_ratio * h:
        w = h * aspect_ratio
    scale = np.array([w, h], dtype=np.float32)
    return center, scale


def _box2cs(box: Sequence[int], aspect_ratio: float):
    x, y, w, h = box[:4]
    return _xywh2cs(x, y, w, h, aspect_ratio)


def get_meta(
    img: npt.NDArray[np.float32],
    output_size: tuple[int, int],
    transform: transforms.Compose,
) -> tuple[torch.Tensor, dict[str, Any]]:
    h, w, _ = img.shape
    aspect_ratio = output_size[1] * 1.0 / output_size[0]

    # Get person center and scale
    person_center, s = _box2cs([0, 0, w - 1, h - 1], aspect_ratio)
    r = 0
    trans = get_affine_transform(person_center, s, r, output_size)
    warped = cv2.warpAffine(
        img,
        trans,
        (int(output_size[1]), int(output_size[0])),
        flags=cv2.INTER_LINEAR,
        borderMode=cv2.BORDER_CONSTANT,
        borderValue=(0, 0, 0),
    )
    cv2.imwrite("result/human_segments_step_2.1.jpg", warped)

    warped_tensor: torch.Tensor = transform(warped)

    return warped_tensor, {
        "center": person_center,
        "height": h,
        "width": w,
        "scale": s,
        "rotation": r,
    }
