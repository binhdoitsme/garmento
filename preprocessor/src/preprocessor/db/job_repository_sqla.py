from dataclasses import dataclass
from uuid import UUID

from injector import inject
from sqlalchemy.orm import Session

from ..services.job_repository import JobRepository, NotFound
from ..services.jobs import PreprocessingJob
from .records import JobRecord


@inject
@dataclass
class JobRepositoryOnSQLA(JobRepository):
    session: Session

    def save(self, job: PreprocessingJob):
        record = JobRecord.from_domain(job)
        self.session.merge(record)
        self.session.commit()

    def find_by_id(self, id: UUID) -> PreprocessingJob:
        maybe_record = self.session.query(JobRecord).filter_by(id=str(id)).first()
        if not maybe_record:
            raise NotFound(PreprocessingJob, f"id={id}")
        return maybe_record.to_domain()
