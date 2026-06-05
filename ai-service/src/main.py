import config
from ai.inference import SentimentAnalyzer
from kafka_layer.consumer import KafkaConsumer
from kafka_layer.handlers import MessageHandler
from kafka_layer.producer import KafkaProducer


def main():
    print("Initializing AI Service...")
    
    # Khởi tạo model (commented out prediction code per user request)
    analyzer = SentimentAnalyzer(device="cpu")
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
