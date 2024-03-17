import os

from fastapi import FastAPI
from model_api.api.model_endpoints import TryOnModelEndpoints
from model_api.db.try_on_repository import InferenceJobRepositoryOnSqlAlchemy
from sqlalchemy import create_engine
from sqlalchemy.orm import Session


class ModelAPIModule:
    def __init__(self) -> None:
        connection_str = os.getenv("DB_CONNECTION_STR", "")
        db_session = Session(create_engine(connection_str))
        inference_repository = InferenceJobRepositoryOnSqlAlchemy(db_session)
        model_endpoints = TryOnModelEndpoints(inference_repository)
        self.app = FastAPI()
        self.app.include_router(model_endpoints.router, prefix="/models")
