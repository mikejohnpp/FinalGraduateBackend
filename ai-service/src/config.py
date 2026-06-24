import os

KAFKA_BOOTSTRAP_SERVERS = os.getenv("KAFKA_BOOTSTRAP_SERVERS", "100.120.5.8:31835")
KAFKA_GROUP_ID = os.getenv("KAFKA_GROUP_ID", "ai-service-group")
KAFKA_TOPICS = os.getenv("KAFKA_TOPICS", "dev.post.analyze.sentiment").split(",")
KAFKA_OUTPUT_TOPIC = os.getenv("KAFKA_OUTPUT_TOPIC", "dev.post.analyze.result")

MODEL_REPO_ID = os.getenv("MODEL_REPO_ID", "MikeJohnP/HTC_ImplicitSentiment")
MODEL_DIR = os.getenv("MODEL_DIR", "my_model")
MODEL_HTC_PATH = os.getenv("MODEL_HTC_PATH", f"{MODEL_DIR}/htc/model.pt")
MODEL_BERTNTN_SENTENCE_PATH = os.getenv("MODEL_BERTNTN_SENTENCE_PATH", f"{MODEL_DIR}/bertntn_sentence/model.pt")

MIN_COMMIT_COUNT = int(os.getenv("MIN_COMMIT_COUNT", "1"))

MODEL_TYPE = os.getenv("MODEL_TYPE", "htc")
DEVICE = os.getenv("DEVICE", "cpu")

MAX_LEN = int(os.getenv("MAX_LEN", 40))
