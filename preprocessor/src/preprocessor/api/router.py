from typing import Annotated
from uuid import UUID

from fastapi import BackgroundTasks, File, HTTPException, UploadFile
from fastapi.routing import APIRouter
from injector import inject

from ..services.job_repository import NotFound
from ..services.preprocessing_service import PreprocessingService
from .responses import JobResponse


@inject
class PreprocessingRouter:
    def __init__(self, service: PreprocessingService) -> None:
        self.service = service
        self.router = APIRouter()
        self.router.post("/jobs")(self.create_job)
        self.router.get("/jobs/{job_id}")(self.get_job_status)
        self.router.delete("/jobs/{job_id}")(self.abort_job)

    def create_job(
        self,
        ref_image: Annotated[UploadFile, File()],
        garment_image: Annotated[UploadFile, File()],
        background_tasks: BackgroundTasks,
    ) -> JobResponse:
        job_id, background_task = self.service.create_job(
            ref_image=ref_image.file, garment_image=garment_image.file
        )
        background_tasks.add_task(background_task)
        return JobResponse(id=job_id)

    def get_job_status(self, job_id: UUID) -> JobResponse:
        try:
            job = self.service.get_job(str(job_id))
            return JobResponse(
                id=str(job.id),
                refImage=job.ref_image,
                garmentImage=job.garment_image,
                maskedGarmentImage=job.masked_garment_image,
                denseposeImage=job.densepose_image,
                segmentedImage=job.segmented_image,
                poseKeypoints=job.pose_keypoints,
            )
        except NotFound as e:
            raise HTTPException(404, ": ".join(str(arg) for arg in e.args))

    def abort_job(self, job_id: str):
        self.service.abort_job(job_id)
        return {}
