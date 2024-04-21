import os
from concurrent.futures import Executor, ProcessPoolExecutor

from fastapi import FastAPI
from injector import Binder, Module, provider, singleton
from sqlalchemy import create_engine
from sqlalchemy.orm import Session

from preprocessor.db.job_repository_sqla import JobRepositoryOnSQLA

from .api.router import PreprocessingRouter
from .scheduler.job_scheduler import BackgroundJobScheduler
from .services.job_repository import JobRepository
from .services.job_scheduler import JobScheduler
from .services.preprocessing_service import PreprocessingService


def wire(binder: Binder):
    binder.bind(JobRepository, JobRepositoryOnSQLA)  # type: ignore
    binder.bind(JobScheduler, BackgroundJobScheduler)  # type: ignore


class ProductionModule(Module):
    @provider
    def provide_sqla_session(self) -> Session:
        engine = create_engine(os.getenv("DB_CONNECTION_STR", ""))
        return Session(bind=engine)

    @singleton
    @provider
    def provide_process_pool_executor(self) -> Executor:
        process_count = int(os.getenv("PROCESS_COUNT", "4"))
        return ProcessPoolExecutor(process_count)

    @provider
    def provide_preprocessing_service(
        self, repository: JobRepository, scheduler: JobScheduler
    ) -> PreprocessingService:
        return PreprocessingService(repository, scheduler)

    @provider
    def provide_preprocessing_router(
        self, service: PreprocessingService
    ) -> PreprocessingRouter:
        return PreprocessingRouter(service)

    @singleton
    @provider
    def provide_fastapi_app(self, router: PreprocessingRouter) -> FastAPI:
        app = FastAPI()
        app.include_router(router.router)
        return app
