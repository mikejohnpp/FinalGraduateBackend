import sys
from confluent_kafka import Consumer, KafkaException, KafkaError
import config

class KafkaConsumer:
    def __init__(self, message_handler, producer=None):
        conf = {
            'bootstrap.servers': config.KAFKA_BOOTSTRAP_SERVERS,
            'group.id': config.KAFKA_GROUP_ID,
            'enable.auto.commit': 'false',
            'auto.offset.reset': 'earliest'
        }
        self.consumer = Consumer(conf)
        self.message_handler = message_handler
        self.producer = producer
        self.running = True

    def start(self, topics):
        try:
            self.consumer.subscribe(topics)
            msg_count = 0
            print(f"Consumer started, subscribing to topics: {topics}")
            
            while self.running:
                msg = self.consumer.poll(timeout=1.0)
                
                # Cập nhật các delivery report của producer
                if self.producer:
                    self.producer.poll(0)
                    
                if msg is None: continue

                if msg.error():
                    if msg.error().code() == KafkaError._PARTITION_EOF:
                        # End of partition event
                        sys.stderr.write('%% %s [%d] reached end at offset %d\n' %
                                         (msg.topic(), msg.partition(), msg.offset()))
                    elif msg.error():
                        raise KafkaException(msg.error())
                else:
                    self.message_handler.handle(msg)
                    msg_count += 1
                    if msg_count % config.MIN_COMMIT_COUNT == 0:
                        self.consumer.commit(asynchronous=False)
        finally:
            self.consumer.close()
            if self.producer:
                self.producer.flush()

    def stop(self):
        self.running = False
