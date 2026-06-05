FROM continuumio/miniconda3:latest

RUN apt-get update && apt-get install -y --no-install-recommends \
    patch build-essential patchelf \
    && rm -rf /var/lib/apt/lists/*

RUN conda create -n py38 -y python=3.8
RUN conda run -n py38 python -m pip install -U pip setuptools wheel
RUN conda run -n py38 pip install --no-cache-dir \
    "lmdb==1.4.1" \
    "allennlp==2.10.1" \
    "allennlp-models==2.10.1"

RUN find /opt/conda/envs/py38/lib/python3.8/site-packages/torch/lib \
    -name "*.so*" -exec patchelf --clear-execstack {} \;
