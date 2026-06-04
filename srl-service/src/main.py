import os
from allennlp.predictors import Predictor
from fastapi import FastAPI
from pydantic import BaseModel
import config


os.environ["ALLENNLP_CACHE_ROOT"] = config.ALLENNLP_CACHE_ROOT

predictor = Predictor.from_path(
    config.MODEL_URL,
    cuda_device=config.CUDA_DEVICE
)

app = FastAPI(title="SRL Preprocessor Service")

class SentenceRequest(BaseModel):
    sentence: str

def extract_core_args(words, tags):
    arg0, verb, arg1 = [], [], []

    for w, t in zip(words, tags):
        if t == "O":
            continue

        role = t.split("-", 1)[-1]   # ARG0, V, ARG1, ARGM-TMP...

        if role == "ARG0":
            arg0.append(w)
        elif role == "V":
            verb.append(w)
        elif role == "ARG1":
            arg1.append(w)

    return {
        "ARG0": " ".join(arg0) if arg0 else None,
        "V": " ".join(verb) if verb else None,
        "ARG1": " ".join(arg1) if arg1 else None
    }

@app.post("/predict")
def predict_srl(request: SentenceRequest):
    output = predictor.predict(request.sentence)
    words = output['words']

    result = []
    for v in output['verbs']:
        core = extract_core_args(words, v['tags'])

        if core["ARG0"] and core["V"] and core["ARG1"]:
            result.append(core)

    return {"result": result}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host=config.HOST, port=config.PORT)
