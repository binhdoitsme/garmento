from __future__ import annotations

import dataclasses
import enum
from dataclasses import dataclass, field
from typing import NamedTuple, Protocol
from uuid import UUID, uuid4


class InferenceJobStatus(enum.Enum):
    PENDING = "PENDING"
    IN_PROGRESS = "IN_PROGRESS"
    SUCCEEDED = "SUCCEEDED"
    FAILED = "FAILED"


@dataclass(frozen=True)
class InferenceJobId:
    value: str = field(default_factory=lambda: str(uuid4()))

    @staticmethod
    def from_(value: str):
        return InferenceJobId(str(UUID(value)))


class ImageId(NamedTuple):
    value: str


@dataclass(frozen=True)
class InferenceJob:
    id: InferenceJobId = field(default_factory=InferenceJobId)
    status: InferenceJobStatus = InferenceJobStatus.PENDING
    result_image_id: ImageId | None = None

    def processing(self):
        return dataclasses.replace(self, status=InferenceJobStatus.IN_PROGRESS)

    def succeed_with(self, result_image_id: ImageId):
        return dataclasses.replace(
            self, status=InferenceJobStatus.SUCCEEDED, result_image_id=result_image_id
        )

    def failed(self):
        return dataclasses.replace(self, status=InferenceJobStatus.FAILED)


class InferenceJobRepository(Protocol):
    def save(self, job: InferenceJob): ...

    def find_by_id(self, id: InferenceJobId) -> InferenceJob | None: ...
