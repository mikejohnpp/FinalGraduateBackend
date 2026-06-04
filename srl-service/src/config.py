import os

ALLENNLP_CACHE_ROOT = os.environ.get("ALLENNLP_CACHE_ROOT", "./model_cache")

MODEL_URL = os.environ.get(
    "MODEL_URL", 
    "https://storage.googleapis.com/allennlp-public-models/structured-prediction-srl-bert.2020.12.15.tar.gz"
)

CUDA_DEVICE = int(os.environ.get("CUDA_DEVICE", "-1"))
HOST = os.environ.get("HOST", "0.0.0.0")
PORT = int(os.environ.get("PORT", "8080"))
