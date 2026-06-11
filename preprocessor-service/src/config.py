import os

KAFKA_BOOTSTRAP_SERVERS = os.getenv("KAFKA_BOOTSTRAP_SERVERS","100.106.249.45:31835")
KAFKA_GROUP_ID = os.getenv("KAFKA_GROUP_ID", "preprocessor-service-group")
KAFKA_TOPICS = os.getenv("KAFKA_TOPICS", "dev.post.analyze.preprocessor").split(",")
KAFKA_PONG_TOPIC = os.getenv("KAFKA_PONG_TOPIC", "demo.pong")
KAFKA_OUTPUT_TOPIC = os.getenv("KAFKA_OUTPUT_TOPIC", "dev.post.analyze.sentiment")

MIN_COMMIT_COUNT = int(os.getenv("MIN_COMMIT_COUNT", "1"))

CORENLP_DIR = os.getenv("CORENLP_DIR", "corenlp")

SRL_API_URL = os.getenv("SRL_API_URL", "http://localhost:9096/predict")
