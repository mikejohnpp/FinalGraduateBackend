import os
import stanza
from stanza.server import CoreNLPClient

import config
from kafka_layer.handlers import MessageHandler
from kafka_layer.consumer import KafkaConsumer
from kafka_layer.producer import KafkaProducer

def main():
    print("Initializing Preprocessor Service...")
    
    # 1. Khởi tạo Stanza CoreNLP
    corenlp_dir = config.CORENLP_DIR
    print(f"Checking CoreNLP installation at {corenlp_dir}")
    stanza.install_corenlp(dir=corenlp_dir)
    os.environ["CORENLP_HOME"] = corenlp_dir
    print("CoreNLP initialized successfully.")

    # 2. Khởi tạo Producer
    producer = KafkaProducer()
    print("Kafka Producer initialized.")

    # 3. Khởi tạo Stanza CoreNLP Client
    with CoreNLPClient(
        annotators=["tokenize", "ssplit", "pos", "lemma"],
        timeout=120000,
        memory="6G",
        endpoint="http://localhost:9001",
        be_quiet=True,
    ) as nlp_client:
        print("CoreNLP Client started.")

        # 4. Khởi tạo Kafka Message Handler
        handler = MessageHandler(nlp_client=nlp_client, producer=producer)

        # 5. Khởi tạo và chạy Kafka Consumer
        consumer = KafkaConsumer(message_handler=handler, producer=producer)
        
        try:
            print("Starting consumer loop...")
            consumer.start(topics=config.KAFKA_TOPICS)
        except KeyboardInterrupt:
            print("Shutting down...")
            consumer.stop()

if __name__ == "__main__":
    main()
