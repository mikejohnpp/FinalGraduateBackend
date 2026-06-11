## ------------------------------- Builder Stage ------------------------------ ##
FROM python:3.8.20-bookworm AS builder

COPY --from=ghcr.io/astral-sh/uv:latest /uv /uvx /bin/

RUN apt-get update && apt-get install --no-install-recommends -y \
    build-essential gcc g++ patch patchelf && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY pyproject.toml uv.lock ./

RUN uv sync --frozen --no-dev
RUN /app/.venv/bin/python -m ensurepip --upgrade && \
       /app/.venv/bin/python -m pip --version

RUN find /app/.venv/lib/python3.8/site-packages/torch/lib \
    -name "*.so*" -exec patchelf --clear-execstack {} \; || true

COPY src ./src

## ------------------------------- Runtime Stage ------------------------------ ##
FROM python:3.8.20-slim-bookworm AS production

WORKDIR /app
COPY --from=builder /app/.venv .venv
COPY --from=builder /app/src ./src

ENV PATH="/app/.venv/bin:$PATH" \
    PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1

EXPOSE 8080

# CMD ["bash"]

CMD ["python", "-u", "src/main.py"]

