import json
import uuid
import datetime
from confluent_kafka import Producer
import config

class KafkaProducer:
    def __init__(self):
        conf = {
            'bootstrap.servers': config.KAFKA_BOOTSTRAP_SERVERS,
            'client.id': 'ai-service-producer'
        }
        self.producer = Producer(conf)

    def delivery_report(self, err, msg):
        """ Được gọi khi một message được gửi thành công hoặc thất bại """
        if err is not None:
            print(f"Lỗi gửi message: {err}")
        else:
            print(f"Gửi thành công tới {msg.topic()} [{msg.partition()}]")

    def send_event(self, topic, event_type, payload):
        """ Đóng gói dữ liệu thành chuẩn EventEnvelope và gửi """
        # Thời gian ISO-8601 (Spring Boot Instant có thể nhận định dạng 'Z')
        now_iso = datetime.datetime.now(datetime.timezone.utc).isoformat().replace("+00:00", "Z")
        
        event_id = str(uuid.uuid4())
        
        envelope = {
            "eventId": event_id,
            "eventType": event_type,
            "traceId": None,
            "occurredAt": now_iso,
            "source": "ai-service",
            "version": 1,
            "payload": payload
        }
        
        value_json = json.dumps(envelope).encode('utf-8')
        
        # Thêm kafka headers nếu phía Java cần
        headers = [
            ("X-Event-Id", event_id.encode('utf-8')),
            ("X-Event-Type", event_type.encode('utf-8')),
            ("X-Event-Source", b"ai-service"),
            ("X-Event-Version", b"1"),
            ("X-Occurred-At", now_iso.encode('utf-8'))
        ]
        
        self.producer.produce(
            topic=topic,
            value=value_json,
            headers=headers,
            on_delivery=self.delivery_report
        )
        # Sẽ được trigger bởi producer.poll() trong consumer loop
        
    def poll(self, timeout):
        self.producer.poll(timeout)
        
    def flush(self):
        print("Flushing Kafka producer...")
        self.producer.flush()
