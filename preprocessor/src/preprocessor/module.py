import logging
import os
from contextlib import asynccontextmanager
from functools import lru_cache

from fastapi import FastAPI
from injector import Binder, Injector, Module, provider, singleton
from py_eureka_client.eureka_client import EurekaClient  # type: ignore
from sqlalchemy import create_engine
from sqlalchemy.orm import Session

from .api.health_router import HealthCheckRouter
from .api.router import PreprocessingRouter
from .api.static_images_router import ImagesRouter
from .db.job_repository_sqla import JobRepositoryOnSQLA
from .services.job_repository import JobRepository
from .services.preprocessing_service import PreprocessingService


def wire(binder: Binder):
    binder.bind(JobRepository, JobRepositoryOnSQLA)  # type: ignore


class ProductionModule(Module):
    @provider
    def provide_sqla_session(self) -> Session:
        engine = create_engine(os.getenv("DB_CONNECTION_STR", ""))
        return Session(bind=engine)

    @provider
    def provide_preprocessing_service(
        self, repository: JobRepository
    ) -> PreprocessingService:
        service = PreprocessingService(repository)
        return service
    
    @singleton
    @provider
    def provide_eureka_client(self) -> EurekaClient:
        return EurekaClient(
            eureka_server=os.getenv("EUREKA_CLIENT_SERVICE_URL", ""),
            app_name=os.getenv("SERVICE_NAME", ""),
        )

    @singleton
    @provider
    def provide_fastapi_app(
        self,
        router: PreprocessingRouter,
        images_router: ImagesRouter,
        health_check_router: HealthCheckRouter,
        service_registry: EurekaClient,
    ) -> FastAPI:
        logger = logging.getLogger("uvicorn")

        @asynccontextmanager
        async def lifespan(app: FastAPI):
            if service_registry:
                await service_registry.start()
                logger.info("Started Service Registry over Eureka.")
                yield
                await service_registry.stop()
            else:
                yield
        
        app = FastAPI(lifespan=lifespan)
        app.include_router(router.router)
        app.include_router(images_router.router)
        app.include_router(health_check_router.router)
        return app


@lru_cache
def provide_injector():
    return Injector([wire, ProductionModule])
