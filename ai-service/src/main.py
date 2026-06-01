import torch
import torch.nn as nn
from transformers import BertModel
from huggingface_hub import snapshot_download
from transformers import BertTokenizer
from pathlib import Path

class HTCModel(nn.Module):
    def __init__(self, num_labels, K=100, d=768, dropout=0.1):
        super().__init__()
        self.bert = BertModel.from_pretrained("bert-base-uncased")
        self.K = K

        self.T_sp = nn.Parameter(torch.randn(K, d, d))
        self.T_po = nn.Parameter(torch.randn(K, d, d))
        self.T_e  = nn.Parameter(torch.randn(K, d, d))
        self.T_f  = nn.Parameter(torch.randn(K, d, d))

        self.W_sp = nn.Linear(2 * d, d)
        self.W_po = nn.Linear(2 * d, d)
        self.W_e  = nn.Linear(2 * d, d)
        self.W_f  = nn.Linear(2 * d, d)

        self.sigma_mlp = nn.Sequential(
            nn.Linear(d, d // 2),
            nn.Tanh(),
            nn.Linear(d // 2, 1)
        )

        self.dropout    = nn.Dropout(dropout)
        self.classifier = nn.Linear(d, num_labels)

    def encode(self, x):
        out = self.bert(
            input_ids=x["input_ids"].squeeze(1),
            attention_mask=x["attention_mask"].squeeze(1)
        )
        return out.last_hidden_state[:, 0, :]

    def tensor_composition(self, h1, h2, T, W):
        bilinear = torch.einsum("bd,kde,be->bd", h1, T, h2) / self.K
        linear   = W(torch.cat([h1, h2], dim=-1))
        return self.dropout(torch.tanh(bilinear + linear))

    def forward(self, batch):
        h_subj = self.encode(batch["subj"])
        h_pred = self.encode(batch["pred"])
        h_obj  = self.encode(batch["obj"])
        h_sen  = self.encode(batch["sent"])

        v_sp = self.tensor_composition(h_subj, h_pred, self.T_sp, self.W_sp)
        v_po = self.tensor_composition(h_pred, h_obj,  self.T_po, self.W_po)

        e = self.tensor_composition(v_sp, v_po, self.T_e, self.W_e)

        r = self.tensor_composition(e, h_sen, self.T_f, self.W_f)

        log_var = self.sigma_mlp(r).squeeze(-1)  # (B,)

        logits = self.classifier(r)

        return logits, e, h_sen, log_var


model_dir = Path("my_model")
if not model_dir.exists():
    snapshot_download(
        repo_id="MikeJohnP/HTC_ImplicitSentiment",
        local_dir=str(model_dir)
    )

def main():
    tokenizer = BertTokenizer.from_pretrained("bert-base-uncased")
    # Xác định thiết bị là CPU
    device = "cpu"
    model_path = "my_model/best_model.pt"

    # Khởi tạo model và chuyển sang CPU
    model = HTCModel(num_labels=3).to(device)

    # Load weights với map_location để ép dữ liệu về CPU
    model.load_state_dict(torch.load(model_path, map_location=torch.device('cpu')))
    sentences = [
        "Fans want the team to win the championship",
        "Customers want the company to stop ignoring their complaints",
        "The manager beat his employee for making a mistake",
        "The boy beat the drum"
    ]

    subjs = [
        "Fans",
        "Customers",
        "The manager",
        "The boy"
    ]

    preds = [
        "want",
        "want",
        "beat",
        "beat"
    ]

    objs = [
        "the team to win the championship",
        "the company to stop ignoring their complaints",
        "his employee",
        "the drum"
    ]

    id2label = {
        0: "negative",
        1: "neutral",
        2: "positive"
    }

    model.eval()

    with torch.no_grad():

        batch = {
            "subj": tokenizer(subjs, return_tensors="pt", padding=True, truncation=True),
            "pred": tokenizer(preds, return_tensors="pt", padding=True, truncation=True),
            "obj": tokenizer(objs, return_tensors="pt", padding=True, truncation=True),
            "sent": tokenizer(sentences, return_tensors="pt", padding=True, truncation=True),
        }

        # đưa lên GPU
        for k in batch:
            batch[k] = {x: batch[k][x].to(device) for x in batch[k]}

        logits, _, _, _ = model(batch)

        preds = torch.argmax(logits, dim=-1).cpu().tolist()

        for sent, p in zip(sentences, preds):
            print(f"{sent} → {id2label[p]}")

if __name__ == "__main__":
    main()

