import logging
import os
from contextlib import asynccontextmanager
from functools import lru_cache

from fastapi import FastAPI
from injector import Injector, Module, provider, singleton  # type: ignore
from py_eureka_client.eureka_client import EurekaClient  # type: ignore

from model_api.api.model_router import ModelRouter

from .adapters.file_storage import FileResultStorageService
from .services.inference_service import InferenceService
from .services.result_storage_service import ResultStorageService


class ProductionModule(Module):
    @provider
    def provide_storage_service(self) -> ResultStorageService:
        return FileResultStorageService()

    @provider
    def provide_inference_service(
        self, storage_service: ResultStorageService
    ) -> InferenceService:
        return InferenceService(storage_service=storage_service)

    @singleton
    @provider
    def provide_eureka_client(self) -> EurekaClient:
        return EurekaClient(
            eureka_server=os.getenv("EUREKA_CLIENT_SERVICE_URL", ""),
            app_name=os.getenv("SERVICE_NAME", ""),
        )

    @provider
    def provide_model_router(self, inference_service: InferenceService) -> ModelRouter:
        return ModelRouter(inference_service=inference_service)

    @provider
    def provide_fastapi_app(
        self, service_registry: EurekaClient, model_router: ModelRouter
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

        app = FastAPI()
        app.include_router(model_router.router)
        return app


@lru_cache
def provide_injector():
    return Injector([ProductionModule])
