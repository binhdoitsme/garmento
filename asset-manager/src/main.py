import logging
import os
from fastapi import FastAPI, File, UploadFile, HTTPException
from uuid import UUID

from contextlib import asynccontextmanager

from fastapi.responses import StreamingResponse
from asset_manager.storage import FileStorageEngine
from py_eureka_client.eureka_client import EurekaClient  # type: ignore


logger = logging.getLogger("uvicorn")


@asynccontextmanager
async def lifespan(app: FastAPI):
    await service_registry.start()
    logger.info("Started Service Registry over Eureka.")
    yield
    await service_registry.stop()


app = FastAPI(lifespan=lifespan)
storage_engine = FileStorageEngine()
service_registry = EurekaClient(
    eureka_server=os.getenv("EUREKA_CLIENT_SERVICE_URL") or "",
    app_name="assets-manager",
    instance_port=8002,
)


@app.get("/health")
async def health_check():
    return {"status": "UP"}


@app.post("/assets/")
async def upload_image(file: UploadFile = File(...)):
    image_id = storage_engine.save_file(file)
    return {"id": image_id, "url": f"/assets/{image_id}"}


@app.get("/assets/{asset_id}/")
async def get_asset(asset_id: UUID):
    asset = storage_engine.get_file(asset_id)
    if asset is None:
        raise HTTPException(status_code=404, detail="Asset not found")
    return StreamingResponse(asset)
