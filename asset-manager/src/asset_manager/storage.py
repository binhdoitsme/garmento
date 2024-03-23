import os
from typing import Any, Generator, Optional, Protocol, Union
from uuid import UUID, uuid4

from fastapi import UploadFile
from PIL import Image  # type: ignore


class BaseStorageEngine(Protocol):
    def save_file(self, file: UploadFile) -> str: ...

    def get_file(
        self, asset_id: UUID
    ) -> Optional[Generator[Union[bytes, str], Any, None]]: ...


class FileStorageEngine(BaseStorageEngine):
    def save_file(self, file: UploadFile) -> str:
        asset_id = uuid4()
        if not os.path.exists("assets"):
            os.mkdir("assets")
        image = Image.open(file.file)
        image.thumbnail((2048, 2048))
        image.save(f"assets/{asset_id}.jpg")
        return str(asset_id)

    def get_file(self, asset_id: UUID):
        try:
            with open(f"assets/{asset_id}.jpg", "rb") as f:
                yield from f
        except FileNotFoundError:
            return None
