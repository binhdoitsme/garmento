import logging
from uuid import UUID

from asset_manager.storage import FileStorageEngine
from fastapi import FastAPI, File, HTTPException, UploadFile
from fastapi.responses import StreamingResponse

logger = logging.getLogger("uvicorn")


app = FastAPI()
storage_engine = FileStorageEngine()


@app.get("/health")
async def health_check():
    return {"status": "UP"}


@app.post("/assets")
async def upload_image(file: UploadFile = File(...)):
    image_id = storage_engine.save_file(file)
    return {"id": image_id, "url": f"/assets/{image_id}"}


@app.get("/assets/{asset_id}")
async def get_asset(asset_id: UUID):
    asset = storage_engine.get_file(asset_id)
    if asset is None:
        raise HTTPException(status_code=404, detail="Asset not found")
    return StreamingResponse(asset)
