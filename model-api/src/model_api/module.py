from functools import lru_cache

from fastapi import FastAPI
from injector import Injector, Module, provider  # type: ignore

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

    @provider
    def provide_model_router(self, inference_service: InferenceService) -> ModelRouter:
        return ModelRouter(inference_service=inference_service)

    @provider
    def provide_fastapi_app(self, model_router: ModelRouter) -> FastAPI:
        app = FastAPI()
        app.include_router(model_router.router)
        return app


@lru_cache
def provide_injector():
    return Injector([ProductionModule])
