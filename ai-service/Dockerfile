## ------------------------------- Builder Stage ------------------------------ ##
FROM python:3.12.12-bookworm AS builder

COPY --from=ghcr.io/astral-sh/uv:latest /uv /uvx /bin/

RUN apt-get update && apt-get install --no-install-recommends -y \
    build-essential && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY pyproject.toml uv.lock ./

RUN uv sync --frozen --no-dev
RUN /app/.venv/bin/python -m ensurepip --upgrade && \
       /app/.venv/bin/python -m pip --version

COPY src ./src

## ------------------------------- Runtime Stage ------------------------------ ##
FROM python:3.12.12-slim-bookworm AS production

COPY --from=eclipse-temurin:21-jre /opt/java/openjdk /opt/java/openjdk

WORKDIR /app
COPY --from=builder /app/.venv .venv
COPY --from=builder /app/src ./src

ENV JAVA_HOME=/opt/java/openjdk \
    PATH="/opt/java/openjdk/bin:/app/.venv/bin:$PATH" \
    PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1


# CMD ["bash"]

CMD ["python", "-u", "src/main.py"]

