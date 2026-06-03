import json
import config
from processor.pipeline import preprocess_pipeline, lemma_string

class MessageHandler:
    def __init__(self, nlp_client=None, producer=None):
        self.nlp_client = nlp_client
        self.producer = producer

    def _process_text(self, raw_text):
        if not raw_text:
            return ""
        # 1. Pipeline xử lý text
        cleaned = preprocess_pipeline(raw_text)
        if not cleaned:
            return ""
        # 2. Lemmatization bằng Stanza
        lemmatized = lemma_string(self.nlp_client, cleaned)
        return lemmatized

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
                        "from": "preprocessor-service",
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
            elif event_type == "postAnalyze":
                sentence = payload.get('sentence', '')
                post_id = payload.get('postId', 'unknown')
                
                print(f"[{event_type}] Received request for preprocessor: '{sentence}'")
                
                if self.nlp_client:
                    processed_sentence = self._process_text(sentence)
                    
                    print(f"==> Processed sentence: {processed_sentence} (for postId: {post_id})")

                    if self.producer:
                        result_payload = {
                            "sentence": processed_sentence,
                        }
                        
                        print(f"Sending processed result to {config.KAFKA_OUTPUT_TOPIC}")
                        self.producer.send_event(
                            topic=config.KAFKA_OUTPUT_TOPIC,
                            event_type="AnalyzeSentimentEvent",
                            payload=result_payload
                        )
                else:
                    print("NLP Client (Stanza) not initialized.")
            else:
                print(f"[{event_type}] {payload}")
        except json.JSONDecodeError as e:
            print(f"JSON Decode Error: {e}")
        except Exception as e:
            print(f"Error processing message: {e}")
