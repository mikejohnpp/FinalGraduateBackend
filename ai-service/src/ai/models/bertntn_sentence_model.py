import torch
import torch.nn as nn
from transformers import BertModel


class TensorComposition(nn.Module):
    def __init__(self, input_dim, k=100):
        super().__init__()
        self.T   = nn.Parameter(torch.randn(input_dim, input_dim, k) * 0.01)
        self.W   = nn.Linear(input_dim * 2, k, bias=True)
        self.act = nn.Tanh()

    def forward(self, h1, h2):
        # Bilinear tensor: (batch, input_dim) x (input_dim, input_dim, k) x (batch, input_dim) → (batch, k)
        tensor_out = torch.einsum('bi,ijk,bj->bk', h1, self.T, h2)
        linear_out = self.W(torch.cat([h1, h2], dim=-1))
        return self.act(tensor_out + linear_out)


class BERTNTNSentenceModel(nn.Module):
    def __init__(self, hidden_dim=768, k=100, num_labels=3, dropout=0.1):
        super().__init__()
        self.bert = BertModel.from_pretrained('bert-base-uncased')

        fused_dim = hidden_dim * 2   # 1536 — sau khi concat h_sen vào mỗi phần

        self.tc1 = TensorComposition(fused_dim, k)   # subj_sen × pred_sen → rs_p
        self.tc2 = TensorComposition(fused_dim, k)   # pred_sen × obj_sen  → rp_o
        # tc3 nhận input_dim = k
        self.tc3 = TensorComposition(k, k)            # rs_p × rp_o → e

        self.dropout = nn.Dropout(dropout)

        # Classifier chỉ nhận e (k chiều)
        self.classifier = nn.Sequential(
            nn.Linear(k, 128),
            nn.ReLU(),
            nn.Dropout(dropout),
            nn.Linear(128, num_labels)
        )

    # ── helpers ────────────────────────────────────────────
    def get_mean_pool(self, input_ids, attention_mask):
        out    = self.bert(input_ids=input_ids, attention_mask=attention_mask)
        hidden = out.last_hidden_state                      # (batch, seq, 768)
        mask   = attention_mask.unsqueeze(-1).float()       # (batch, seq, 1)
        return (hidden * mask).sum(dim=1) / mask.sum(dim=1) # (batch, 768)

    def get_segment_mean(self, last_hidden_state, input_ids, sep_token_id=102):
        batch_size = input_ids.shape[0]
        h_subj_list, h_pred_list, h_obj_list = [], [], []

        for i in range(batch_size):
            sep_pos = (input_ids[i] == sep_token_id).nonzero(as_tuple=True)[0]

            # Đảm bảo có đủ 3 [SEP]
            if len(sep_pos) < 3:
                pad = torch.tensor(
                    [input_ids.shape[1] - 1] * (3 - len(sep_pos)),
                    device=input_ids.device
                )
                sep_pos = torch.cat([sep_pos, pad])

            s1, s2, s3 = sep_pos[0].item(), sep_pos[1].item(), sep_pos[2].item()

            # Lấy mean của từng đoạn (bỏ [CLS] và [SEP])
            seg_subj = last_hidden_state[i, 1:s1, :]
            seg_pred = last_hidden_state[i, s1+1:s2, :]
            seg_obj  = last_hidden_state[i, s2+1:s3, :]

            # Tránh đoạn rỗng (mean của tensor rỗng gây NaN)
            h_subj_list.append(seg_subj.mean(dim=0) if seg_subj.shape[0] > 0
                                else torch.zeros(last_hidden_state.shape[-1], device=input_ids.device))
            h_pred_list.append(seg_pred.mean(dim=0) if seg_pred.shape[0] > 0
                                else torch.zeros(last_hidden_state.shape[-1], device=input_ids.device))
            h_obj_list.append(seg_obj.mean(dim=0)  if seg_obj.shape[0]  > 0
                               else torch.zeros(last_hidden_state.shape[-1], device=input_ids.device))

        return (
            torch.stack(h_subj_list),   # (batch, 768)
            torch.stack(h_pred_list),   # (batch, 768)
            torch.stack(h_obj_list)     # (batch, 768)
        )

    # ── forward ────────────────────────────────────────────
    def forward(self, sen_input_ids, sen_attention_mask,
                      tri_input_ids, tri_attention_mask):

        # Bước 1: Encode sentence → h_sen (768)
        h_sen = self.dropout(
            self.get_mean_pool(sen_input_ids, sen_attention_mask)
        )

        # Bước 2: Encode triplet → h_subj, h_pred, h_obj (mỗi cái 768)
        tri_out = self.bert(
            input_ids=tri_input_ids,
            attention_mask=tri_attention_mask
        ).last_hidden_state

        h_subj, h_pred, h_obj = self.get_segment_mean(tri_out, tri_input_ids)
        h_subj = self.dropout(h_subj)
        h_pred = self.dropout(h_pred)
        h_obj  = self.dropout(h_obj)

        # Bước 3: Concat h_sen vào TỪNG phần của triplet (đây là điểm sửa chính)
        subj_sen = torch.cat([h_subj, h_sen], dim=-1)   # (batch, 1536)
        pred_sen = torch.cat([h_pred, h_sen], dim=-1)   # (batch, 1536)
        obj_sen  = torch.cat([h_obj,  h_sen], dim=-1)   # (batch, 1536)

        # Bước 4-6: Hierarchical NTN
        rs_p = self.tc1(subj_sen, pred_sen)   # (batch, k)
        rp_o = self.tc2(pred_sen, obj_sen)    # (batch, k)
        e    = self.tc3(rs_p, rp_o)           # (batch, k)

        # Bước 7: Classify
        return self.classifier(self.dropout(e))
