import os

KAFKA_BOOTSTRAP_SERVERS = os.getenv("KAFKA_BOOTSTRAP_SERVERS", "100.106.249.45:31835")
KAFKA_GROUP_ID = os.getenv("KAFKA_GROUP_ID", "ai-service-group")
KAFKA_TOPICS = os.getenv("KAFKA_TOPICS", "demo.ping,ai.analyze.request").split(",")
KAFKA_PONG_TOPIC = os.getenv("KAFKA_PONG_TOPIC", "demo.pong")

MODEL_REPO_ID = os.getenv("MODEL_REPO_ID", "MikeJohnP/HTC_ImplicitSentiment")
MODEL_DIR = os.getenv("MODEL_DIR", "my_model")
MODEL_WEIGHTS_PATH = os.getenv("MODEL_WEIGHTS_PATH", "my_model/best_model.pt")

MIN_COMMIT_COUNT = int(os.getenv("MIN_COMMIT_COUNT", "1"))
