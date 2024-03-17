from __future__ import annotations

import enum
from dataclasses import dataclass
from typing import NamedTuple, Protocol
from uuid import UUID, uuid4


class InferenceJobStatus(enum.Enum):
    PENDING = "PENDING"
    IN_PROGRESS = "IN_PROGRESS"
    SUCCEEDED = "SUCCEEDED"
    FAILED = "FAILED"


class InferenceJobId(NamedTuple):
    value: str = str(uuid4())

    @staticmethod
    def from_(value: str):
        return InferenceJobId(str(UUID(value)))


class ImageId(NamedTuple):
    value: str


@dataclass
class InferenceJob:
    id: InferenceJobId
    status: InferenceJobStatus = InferenceJobStatus.PENDING
    result_image_id: ImageId | None = None


class InferenceJobRepository(Protocol):
    def save(self, job: InferenceJob): ...

    def find_by_id(self, id: InferenceJobId) -> InferenceJob | None: ...
