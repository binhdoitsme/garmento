import io
from typing import Annotated, Literal

from fastapi.responses import FileResponse, StreamingResponse
import pydantic
from fastapi import BackgroundTasks, File, HTTPException, Path, UploadFile, status
from fastapi.routing import APIRouter
from PIL.Image import Image

from ..services.inference_service import InferenceService


class JobResult(pydantic.BaseModel):
    id: str
    resultURL: str | None = None
    status: Literal["IN_PROGRESS", "SUCCESS", "FAILED"] = "IN_PROGRESS"


class ModelRouter:
    def __init__(self, inference_service: InferenceService):
        self.inference_service = inference_service
        self.router = APIRouter()
        self.router.add_api_route(
            "/try-on/{id}", self.create_try_on_job, methods=["POST"]
        )
        self.router.add_api_route(
            "/try-on/{id}/result", self.get_try_on_job_result, methods=["GET"]
        )
        self.router.add_api_route("/try-on/{id}", self.get_try_on_job, methods=["GET"])
        self.router.add_api_route("/health", self.health_check, methods=["GET"])

    def health_check(self):
        return {"status": "up"}

    async def create_try_on_job(
        self,
        id: Annotated[str, Path()],
        ref_image: Annotated[UploadFile, File()],
        garment_image: Annotated[UploadFile, File()],
        densepose_image: Annotated[UploadFile, File()],
        masked_garment_image: Annotated[UploadFile, File()],
        segmented_image: Annotated[UploadFile, File()],
        pose_keypoints: Annotated[UploadFile, File()],
        background_tasks: BackgroundTasks,
    ):
        background_tasks.add_task(
            self.inference_service.do_infer,
            id=id,
            ref_image=io.BytesIO(await ref_image.read()),
            garment_image=io.BytesIO(await garment_image.read()),
            densepose_image=io.BytesIO(await densepose_image.read()),
            masked_garment_image=io.BytesIO(await masked_garment_image.read()),
            segmented_image=io.BytesIO(await segmented_image.read()),
            pose_keypoints=io.BytesIO(await pose_keypoints.read()),
        )
        return JobResult(id=id)

    def get_try_on_job(self, id: Annotated[str, Path()]):
        try:
            maybe_result = self.inference_service.get_result(id)
            if not maybe_result:
                return JobResult(id=id)
            if isinstance(maybe_result, str):
                return JobResult(id=id, status="FAILED")
            return JobResult(id=id, resultURL=f"/try-on/{id}/result", status="SUCCESS")
        except FileNotFoundError:
            raise HTTPException(status.HTTP_404_NOT_FOUND)

    def get_try_on_job_result(self, id: Annotated[str, Path()]):
        try:
            maybe_result = self.inference_service.get_result(id)
            if not isinstance(maybe_result, Image):
                raise HTTPException(status.HTTP_404_NOT_FOUND)

            # Save the image to a byte stream
            byte_stream = io.BytesIO()
            maybe_result.save(byte_stream, format="PNG")
            byte_stream.seek(0)

            # Return the image as a StreamingResponse
            return StreamingResponse(byte_stream, media_type="image/png")
        except FileNotFoundError:
            raise HTTPException(status.HTTP_404_NOT_FOUND)
