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
                    print(f"Sending PongEvent to {config.KAFKA_OUTPUT_TOPIC}")
                    self.producer.send_event(
                        topic=config.KAFKA_OUTPUT_TOPIC, 
                        event_type="PongEvent", 
                        payload=pong_payload
                    )
            elif event_type == "postAnalyzeSentiment":
                sentence = payload.get('sentence', '')
                subj = payload.get('subject', '')
                pred = payload.get('predicate', '')
                obj = payload.get('object', '')
                post_id = payload.get('postId', '')


                print(f"[{event_type}] Received request for: '{payload}'")

                if self.analyzer:
                    result = self.analyzer.predict(sentence, subj, pred, obj)
                    print(f"==> AI Result: {result}")
                    if self.producer:
                        response_payload = {
                            "postId": post_id,
                            "sentiment": result
                        }
                        print(f"Sending analysis result for Post ID {post_id} to topic: {config.KAFKA_OUTPUT_TOPIC}")
                        self.producer.send_event(
                            topic=config.KAFKA_OUTPUT_TOPIC, 
                            event_type="postAnalyzeResult", 
                            payload=response_payload
                        )
                else:
                    print("Analyzer not initialized.")
            else:
                print(f"[{event_type}] {payload}")
        except json.JSONDecodeError as e:
            print(f"JSON Decode Error: {e}")
        except Exception as e:
            print(f"Error processing message: {e}")
