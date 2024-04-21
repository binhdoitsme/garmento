from enum import Enum
from uuid import UUID, uuid4

from pydantic.dataclasses import dataclass


class JobStatus(Enum):
    PENDING = "PENDING"
    IN_PROGRESS = "IN_PROGRESS"
    SUCCESS = "SUCCESS"
    FAILED = "FAILED"
    ABORTED = "ABORTED"


class NotProcessing(Exception): ...


class AlreadyProcessing(Exception): ...

class AlreadyAborted(Exception): ...


@dataclass
class PreprocessingJob:
    ref_image: str  # image
    garment_image: str  # cloth

    id: UUID = uuid4()
    status: JobStatus = JobStatus.PENDING
    # store filenames
    masked_garment_image: str | None = None  # cloth_mask
    densepose_image: str | None = None  # image-densepose
    segmented_image: str | None = None  # image-parse-v3
    pose_keypoints: str | None = None  # openpose_json

    def processing(self):
        if self.status != JobStatus.PENDING:
            raise AlreadyProcessing()
        self.status = JobStatus.IN_PROGRESS

    def success_with(
        self,
        masked_garment_image: str,
        densepose_image: str,
        segmented_image: str,
        pose_keypoints: str,
    ):
        if self.status not in (JobStatus.IN_PROGRESS,):
            raise NotProcessing()
        self.masked_garment_image = masked_garment_image
        self.densepose_image = densepose_image
        self.segmented_image = segmented_image
        self.pose_keypoints = pose_keypoints
        self.status = JobStatus.SUCCESS

    def failed(self):
        if self.status not in (JobStatus.IN_PROGRESS,):
            raise NotProcessing()
        self.status = JobStatus.FAILED

    def aborted(self):
        if self.status not in (JobStatus.IN_PROGRESS,):
            raise NotProcessing()
        self.status = JobStatus.ABORTED
