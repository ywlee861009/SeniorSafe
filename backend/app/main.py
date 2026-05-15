from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.routers.auth import router as auth_router
from app.routers.devices import router as devices_router
from app.routers.fall import router as fall_router
from app.routers.pairing import pairings_router, router as pairing_router

app = FastAPI(title="SeniorSafe API", version="0.1.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/health")
async def health() -> dict[str, str]:
    return {"status": "ok"}


app.include_router(auth_router, prefix="/auth", tags=["auth"])
app.include_router(pairing_router, prefix="/pairing", tags=["pairing"])
app.include_router(pairings_router, prefix="/pairings", tags=["pairing"])
app.include_router(fall_router, prefix="/fall", tags=["fall"])
app.include_router(devices_router, prefix="/devices", tags=["devices"])
