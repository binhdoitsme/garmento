from typing import Annotated, Literal

import pydantic
from fastapi import Path, Query, Response, status
from fastapi.routing import APIRouter
from model_api.services.try_on_repository import InferenceJobId, InferenceJobRepository
from pydantic.alias_generators import to_camel

TryOnJobCategory = Literal["reference-preset", "reference-custom"]
TryOnJobStatus = Literal["PENDING", "IN_PROGRESS", "SUCCEEDED", "FAILED"]


class APIModel(pydantic.BaseModel):
    model_config = pydantic.ConfigDict(alias_generator=to_camel, populate_by_name=True)

    def model_dump(self, *args, **kwargs):
        if kwargs and kwargs.get("exclude_unset") is not None:
            kwargs["exclude_unset"] = True
        return pydantic.BaseModel.model_dump(self, *args, **kwargs)


class TryOnJobInputModel(APIModel):
    reference_image_id: str
    garment_image_id: str


class TryOnJobStatusModel(APIModel):
    job_id: str
    status: TryOnJobStatus = "PENDING"
    result_image_id: str | None = None


class TryOnModelEndpoints:
    def __init__(self, repository: InferenceJobRepository):
        self.repository = repository
        self.router = APIRouter()
        self.router.add_api_route("/try-on", self.create_try_on_job, methods=["POST"])
        self.router.add_api_route("/try-on/{id}", self.get_try_on_job, methods=["GET"])
        self.router.add_api_route("/health", self.health_check, methods=["GET"])

    def health_check(self):
        return {"status": "up"}

    def create_try_on_job(
        self,
        body: TryOnJobInputModel,
        category: TryOnJobCategory = Query(default="reference-custom"),
    ):
        print("category =", category)
        return TryOnJobStatusModel(job_id="12345", status="PENDING")

    def get_try_on_job(self, id: Annotated[str, Path(title="id")], response: Response):
        try:
            job_id = InferenceJobId.from_(id)
        except Exception as e:
            response.status_code = status.HTTP_400_BAD_REQUEST
            return {}
        existing = self.repository.find_by_id(job_id)
        if not existing:
            response.status_code = status.HTTP_404_NOT_FOUND
            return {}
        print("id:", id)
        return TryOnJobStatusModel(
            job_id=existing.id.value,
            status=existing.status.value,
            result_image_id=(
                existing.result_image_id.value if existing.result_image_id else None
            ),
        )
