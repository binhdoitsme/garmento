from PIL import Image
from typing import Protocol


class ResultStorageService(Protocol):
    def init_store(self, id: str) -> str: ...
    def store(self, id: str, result: Image.Image | str): ...
    def find_by_id(self, id: str) -> Image.Image | str | None: ...
