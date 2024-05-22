from functools import cache
import traceback
from typing import IO

from PIL import Image

from .result_storage_service import ResultStorageService
from .viton_hd.data_loader import to_model_inputs
from .viton_hd.model import VITONHDModel
from .viton_hd.utils import VITONHDOptions


class InferenceService:
    def __init__(self, storage_service: ResultStorageService) -> None:
        self.storage_service = storage_service

    @cache
    def create_model(self, version: str):
        options = VITONHDOptions()._replace(version=version)
        return VITONHDModel(options)

    def do_infer(
        self,
        id: str,
        ref_image: IO[bytes],
        garment_image: IO[bytes],
        densepose_image: IO[bytes],
        masked_garment_image: IO[bytes],
        segmented_image: IO[bytes],
        pose_keypoints: IO[bytes],
        version="latest",
    ):
        try:
            model_inputs = to_model_inputs(
                ref_image,
                garment_image,
                densepose_image,
                masked_garment_image,
                segmented_image,
                pose_keypoints,
            )
            model = self.create_model(version=version)
            result = model.infer(model_inputs)
            self.storage_service.store(id, result)
        except Exception as e:
            details_str = f"""{e}: {"|".join(str(arg) for arg in e.args)}"""
            self.storage_service.store(id, details_str)
            traceback.print_exception(e)

    def get_result(self, id: str) -> Image.Image | str | None:
        return self.storage_service.find_by_id(id)
