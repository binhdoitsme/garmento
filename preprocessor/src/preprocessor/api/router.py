import base64
from io import BytesIO
from uuid import UUID

from fastapi import HTTPException
from fastapi.routing import APIRouter
from injector import inject
from PIL import Image

from ..services.job_repository import NotFound
from ..services.preprocessing_service import PreprocessingService
from .requests import CreateJobRequest
from .responses import JobResponse


@inject
class PreprocessingRouter:
    def __init__(self, service: PreprocessingService) -> None:
        self.service = service
        self.router = APIRouter()
        self.router.post("/jobs")(self.create_job)
        self.router.get("/jobs/{job_id}")(self.get_job_status)
        self.router.delete("/jobs/{job_id}")(self.abort_job)

    def create_job(self, request: CreateJobRequest) -> JobResponse:
        ref_image = Image.open(BytesIO(base64.b64decode(request.ref_image)))
        garment_image = Image.open(BytesIO(base64.b64decode(request.garment_image)))
        job_id = self.service.create_job(
            ref_image=ref_image, garment_image=garment_image
        )
        return JobResponse(id=job_id)

    def get_job_status(self, job_id: UUID) -> JobResponse:
        try:
            job = self.service.get_job(str(job_id))
            return JobResponse(
                id=str(job.id),
                ref_image=job.ref_image,
                garment_image=job.garment_image,
                masked_garment_image=job.masked_garment_image,
                densepose_image=job.densepose_image,
                segmented_image=job.segmented_image,
                pose_keypoints=job.pose_keypoints,
            )
        except NotFound as e:
            raise HTTPException(404, ": ".join(str(arg) for arg in e.args))

    def abort_job(self, job_id: str):
        self.service.abort_job(job_id)
        return {}
