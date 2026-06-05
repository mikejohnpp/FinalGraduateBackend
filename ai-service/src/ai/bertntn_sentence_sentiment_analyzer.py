from pathlib import Path

import torch
import torch.nn.functional as F
from huggingface_hub import snapshot_download
from transformers import BertTokenizer

import config
from ai.base_analyzer import BaseAnalyzer
from ai.model_factory import ModelFactory
from ai.models.bertntn_sentence_model import BERTNTNSentenceModel


@ModelFactory.register("bertntn-sentence")
class BertntnSentenceSentimentAnalyzer(BaseAnalyzer):
    def __init__(self, device="cpu"):
        super().__init__(device=device)
        self.id2label = {
            0: "negative",
            1: "neutral",
            2: "positive"
        }
        
        self._download_model()
        
        self.tokenizer = BertTokenizer.from_pretrained("bert-base-uncased")
        self.model = BERTNTNSentenceModel(num_labels=3).to(self.device)
        self.model.load_state_dict(torch.load(config.MODEL_BERTNTN_SENTENCE_PATH, map_location=torch.device(self.device)))
        self.model.eval()
        print("BertNTN + sentence analyzer model khởi tạo thành công .")

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
            sen_enc = self.tokenizer(
                str(sentence),
                max_length=config.MAX_LEN,
                padding='max_length',
                truncation=True,
                return_tensors='pt'
            )
            tri_enc = self.tokenizer(
                str(subj),
                text_pair=str(pred) + ' [SEP] ' + str(obj),
                max_length=config.MAX_LEN,
                padding='max_length',
                truncation=True,
                return_tensors='pt'
            )

            sen_input_ids = sen_enc['input_ids'].squeeze(0).unsqueeze(0).to(self.device)
            sen_attention_mask = sen_enc['attention_mask'].squeeze(0).unsqueeze(0).to(self.device)
            tri_input_ids = tri_enc['input_ids'].squeeze(0).unsqueeze(0).to(self.device)
            tri_attention_mask = tri_enc['attention_mask'].squeeze(0).unsqueeze(0).to(self.device)

            outputs = self.model(sen_input_ids, sen_attention_mask, tri_input_ids, tri_attention_mask)
            prediction = outputs.argmax(dim=1).item()

            return self.id2label[prediction], 0
