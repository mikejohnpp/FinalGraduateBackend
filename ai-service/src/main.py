import ai.bertntn_sentence_sentiment_analyzer
import ai.htc_sentiment_analyzer
import config
from ai.model_factory import ModelFactory
from kafka_layer.consumer import KafkaConsumer
from kafka_layer.handlers import MessageHandler
from kafka_layer.producer import KafkaProducer


def main():
    print("Initializing AI Service...")
    
    # Khởi tạo model từ Factory
    analyzer = ModelFactory.get_analyzer(config.MODEL_TYPE, device=config.DEVICE)
    print("Model initialized successfully.")

    # Khởi tạo Producer
    producer = KafkaProducer()
    print("Kafka Producer initialized.")

    # Khởi tạo Kafka Message Handler
    handler = MessageHandler(analyzer=analyzer, producer=producer)

    # Khởi tạo và chạy Kafka Consumer
    consumer = KafkaConsumer(message_handler=handler, producer=producer)
    
    try:
        print("Starting consumer loop...")
        consumer.start(topics=config.KAFKA_TOPICS)
    except KeyboardInterrupt:
        print("Shutting down...")
        consumer.stop()

if __name__ == "__main__":
    main()
