# PhishGuard – AI-Based Phishing Website Detection and Risk Analysis System

## System Architecture Overview

```text
               +-------------------------------------------+
               |           User / Browser / Client         |
               +-------------------------------------------+
                                     |
                         HTTPS / JSON REST API
                                     v
               +-------------------------------------------+
               |        FastAPI Backend Web Service        |
               |       (Auth, JWT, Rate Limiting, ORM)     |
               +-------------------------------------------+
                        |                          |
       Lexical Feature Extraction       Database Storage
                        |                          |
                        v                          v
     +-------------------------------+   +-------------------+
     | Feature Extractor (18 Metrics)|   |   SQLite (Local)  |
     | - Length, Subdomains, IP, @   |   |   PostgreSQL      |
     | - Keywords, HTTPS, Protocol   |   |   (Users & Scans) |
     +-------------------------------+   +-------------------+
                        |
            Extracted Feature Vector
                        v
     +-------------------------------+
     |  Trained Random Forest Model  |
     |     (phishing_model.pkl)      |
     +-------------------------------+
                        |
            Probability & Prediction
                        v
     +-------------------------------+
     |  Risk Analysis & Explanation  |
     |  (Score, Verdict, Indicators) |
     +-------------------------------+
```

## Directory Structure

```text
phishguard/
│
├── frontend/                     # React + Vite Client
│   ├── src/
│   │   ├── components/
│   │   ├── pages/
│   │   ├── services/
│   │   ├── App.jsx
│   │   └── main.jsx
│   ├── package.json
│   ├── vite.config.js
│   └── index.html
│
├── backend/                      # Python FastAPI Service
│   ├── app/
│   │   ├── __init__.py
│   │   ├── main.py               # API Gateway & Routes
│   │   ├── database.py           # SQLAlchemy Engine & Session
│   │   ├── models/               # ORM Entities (User, Scan)
│   │   ├── schemas/              # Pydantic Request/Response Models
│   │   ├── routes/               # Modular Endpoints (Auth, Scan, Admin)
│   │   └── services/             # Prediction & Evaluation Services
│   ├── requirements.txt
│   └── .env.example
│
├── ml/                           # Machine Learning Pipeline
│   ├── dataset/                  # Phishing & Legitimate URL data
│   ├── feature_extractor.py      # Feature extraction engine
│   ├── train.py                  # Model training script
│   ├── evaluate.py               # Evaluation & confusion matrix
│   └── phishing_model.pkl        # Serialized Random Forest model
│
└── README.md
```
