from typing import Optional, Protocol, Union
from uuid import UUID, uuid4

from fastapi import UploadFile


class BaseStorageEngine(Protocol):
    def save_file(self, file: UploadFile) -> str:
        ...

    def get_file(self, asset_id: UUID) -> Optional[Union[bytes, str]]:
        ...


class FileStorageEngine(BaseStorageEngine):
    def save_file(self, file: UploadFile) -> str:
        asset_id = uuid4()
        with open(f"assets/{asset_id}.jpg", "wb") as f:
            f.write(file.file.read())
        return str(asset_id)

    def get_file(self, asset_id: UUID) -> Optional[bytes]:
        try:
            with open(f"assets/{asset_id}.jpg", "rb") as f:
                return f.read()
        except FileNotFoundError:
            return None
