from pathlib import Path

import torch
import torch.nn.functional as F
from huggingface_hub import snapshot_download
from transformers import BertTokenizer

import config
from ai.base_analyzer import BaseAnalyzer
from ai.model_factory import ModelFactory
from ai.models.htc_model import HTCModel


@ModelFactory.register("htc")
class HtcSentimentAnalyzer(BaseAnalyzer):
    def __init__(self, device="cpu"):
        super().__init__(device=device)
        self.id2label = {
            0: "negative",
            1: "neutral",
            2: "positive"
        }
        
        self._download_model()
        
        self.tokenizer = BertTokenizer.from_pretrained("bert-base-uncased")
        self.model = HTCModel(num_labels=3).to(self.device)
        self.model.load_state_dict(torch.load(config.MODEL_HTC_PATH, map_location=torch.device(self.device)))
        self.model.eval()
        print("HTC analyzer model khởi tạo thànhcông .")

    def _download_model(self):
        model_dir = Path(config.MODEL_DIR)
        snapshot_download(
            repo_id=config.MODEL_REPO_ID,
            local_dir=str(model_dir)
        )

    def predict(self, payload: dict):
        sentence = payload.get('sentence', '')
        subj = payload.get('subject', '')
        pred = payload.get('predicate', '')
        obj = payload.get('object', '')
        
        with torch.no_grad():
            batch = {
                "subj": self.tokenizer([subj], return_tensors="pt", padding=True, truncation=True),
                "pred": self.tokenizer([pred], return_tensors="pt", padding=True, truncation=True),
                "obj": self.tokenizer([obj], return_tensors="pt", padding=True, truncation=True),
                "sent": self.tokenizer([sentence], return_tensors="pt", padding=True, truncation=True),
            }

            for k in batch:
                batch[k] = {x: batch[k][x].to(self.device) for x in batch[k]}

            logits, _, _, _ = self.model(batch)
            probs = F.softmax(logits, dim=-1)
            confidence = torch.max(probs, dim=-1)[0].cpu().item()
            preds_idx = torch.argmax(logits, dim=-1).cpu().tolist()

            return self.id2label[preds_idx[0]], confidence
