from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

app = FastAPI(
    title="PhishGuard – Phishing Website Detection API",
    description="Backend API for feature extraction, ML classification, and telemetry logging.",
    version="1.0.0"
)

# CORS configuration for React Vite frontend
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:5173", "http://127.0.0.1:5173", "*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.get("/")
def read_root():
    return {
        "status": "online",
        "system": "PhishGuard Threat Detection Engine",
        "version": "1.0.0",
        "message": "Backend initialized successfully. Ready for ML and Auth modules."
    }

@app.get("/api/health")
def health_check():
    return {
        "status": "healthy",
        "components": {
            "api": "operational",
            "database": "ready_for_migration",
            "ml_engine": "ready_for_model"
        }
    }
