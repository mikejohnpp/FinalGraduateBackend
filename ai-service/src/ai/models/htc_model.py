import torch
import torch.nn as nn
from transformers import BertModel

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
