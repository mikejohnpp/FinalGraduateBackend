import json
import config

class MessageHandler:
    def __init__(self, analyzer=None, producer=None):
        self.analyzer = analyzer
        self.producer = producer

    def handle(self, msg):
        value_bytes = msg.value()
        if not value_bytes:
            return
        try:
            raw_json = value_bytes.decode('utf-8')
            envelope = json.loads(raw_json)
            
            event_type = envelope.get('eventType')
            payload = envelope.get('payload', {})
            
            if event_type == "PingEvent":
                print(f"[{event_type}] from: {payload.get('from')} - msg: {payload.get('message')} (sentAt: {payload.get('sentAt')})")
                
                # Gửi trả PongEvent nếu producer được khởi tạo
                if self.producer:
                    pong_payload = {
                        "from": "ai-service",
                        "replyTo": payload.get('from', 'unknown'),
                        "message": f"Pong reply to: {payload.get('message')}",
                        "sentAt": envelope.get('occurredAt')
                    }
                    print(f"Sending PongEvent to {config.KAFKA_PONG_TOPIC}")
                    self.producer.send_event(
                        topic=config.KAFKA_PONG_TOPIC, 
                        event_type="PongEvent", 
                        payload=pong_payload
                    )
            elif event_type == "AnalyzeSentimentEvent":
                sentence = payload.get('sentence', '')
                subj = payload.get('subject', '')
                pred = payload.get('predicate', '')
                obj = payload.get('object', '')
                
                print(f"[{event_type}] Received request for: '{sentence}'")
                
                if self.analyzer:
                    result = self.analyzer.predict(sentence, subj, pred, obj)
                    print(f"==> AI Result: {result}")
                else:
                    print("Analyzer not initialized.")
            else:
                print(f"[{event_type}] {payload}")
        except json.JSONDecodeError as e:
            print(f"JSON Decode Error: {e}")
        except Exception as e:
            print(f"Error processing message: {e}")
