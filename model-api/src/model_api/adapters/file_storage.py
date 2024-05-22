import os
import shutil
from PIL import Image

from ..services.result_storage_service import ResultStorageService


class FileResultStorageService(ResultStorageService):
    def __init__(self, base_path="results") -> None:
        self.base_path = base_path

    def path(self, id: str):
        return f"{self.base_path}/{id}/result.jpg"

    def err_path(self, id: str):
        return self.path(id).replace(".jpg", ".txt")

    def store(self, id: str, result: Image.Image | str):
        os.makedirs(os.path.dirname(file_path := self.path(id)))
        if isinstance(result, str):
            with open(self.err_path(id), mode="w") as writer:
                writer.write(result)
        else:
            result.save(file_path)

    def find_by_id(self, id: str) -> Image.Image | str | None:
        if not os.path.exists(os.path.dirname(self.path(id))):
            raise FileNotFoundError()
        if not os.path.exists(self.path(id)) and not os.path.exists(self.err_path(id)):
            return None
        elif not os.path.exists(self.path(id)):
            with open(self.err_path(id), mode="r") as reader:
                return reader.read()
        return Image.open(self.path(id)).convert("RGB")
