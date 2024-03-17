from dataclasses import dataclass

from sqlalchemy import String
from model_api.services.try_on_repository import (
    ImageId,
    InferenceJob,
    InferenceJobId,
    InferenceJobRepository,
    InferenceJobStatus,
)
from sqlalchemy.orm import Mapped, Session, mapped_column

from .base import Base


class InferenceJobModel(Base):
    __tablename__ = "inference_jobs"

    id: Mapped[str] = mapped_column(String(48), nullable=False, primary_key=True)
    status: Mapped[str] = mapped_column(String(48), nullable=False)
    result_image_id: Mapped[str] = mapped_column(String(48), nullable=True)


@dataclass
class InferenceJobRepositoryOnSqlAlchemy(InferenceJobRepository):
    session: Session

    def save(self, job: InferenceJob):
        id = job.id.value
        with self.session.begin_nested():
            existing = self.session.query(InferenceJobModel).filter_by(id=id).first()
            if existing:
                existing.status = job.status.value
            else:
                existing = InferenceJobModel(
                    id=job.id.value,
                    status=job.status.value,
                )
                self.session.add(existing)
            if job.result_image_id:
                existing.result_image_id = job.result_image_id.value

    def find_by_id(self, id: InferenceJobId):
        existing = self.session.query(InferenceJobModel).filter_by(id=id).first()
        if not existing:
            return None
        return InferenceJob(
            id=InferenceJobId(existing.id),
            status=InferenceJobStatus(existing.status),
            result_image_id=ImageId(existing.result_image_id),
        )
