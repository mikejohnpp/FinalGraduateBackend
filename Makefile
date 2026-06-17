# Makefile for FinalGraduateBackend
#
# Java services run with the Maven wrapper (./mvnw spring-boot:run).
# Python services run with uv (ai-service, preprocessor-service) and conda (srl-service).
#
# Common usage:
#   make help              # list all targets
#   make build             # build all Java modules (skip tests)
#   make install-common    # install the shared `common` jar into the local repo
#   make auth              # run auth-service (port 9093)
#   make gateway           # run api-gateway (port 8080)
#   make logs-dir          # create the logs/ directory
#   make run-java          # start all Java services in the background (logs in logs/)
#   make stop              # stop all background services started by this Makefile
#   make ps                # show running services started by this Makefile

SHELL := /bin/bash
MVNW  := ./mvnw

# Spring profile used by `*-bg` / `run-java` targets. Override with: make run-java PROFILE=prod
PROFILE ?= dev

# Directories for background process logs and pid files.
LOG_DIR := logs
PID_DIR := .pids

JAVA_SERVICES   := api-gateway auth-service user-service chat-service notification-service
PYTHON_SERVICES := ai-service preprocessor-service srl-service

.DEFAULT_GOAL := help

# ----------------------------------------------------------------------------
# Help
# ----------------------------------------------------------------------------
.PHONY: help
help:
	@echo "FinalGraduateBackend - available targets:"
	@echo ""
	@echo "  Build:"
	@echo "    make build            Build all Java modules (skip tests)"
	@echo "    make install-common   Install the shared 'common' jar locally"
	@echo "    make clean            Maven clean all modules"
	@echo ""
	@echo "  Run Java services (foreground):"
	@echo "    make gateway          api-gateway        (port 8080)"
	@echo "    make auth             auth-service       (port 9093)"
	@echo "    make user             user-service       (port 9090)"
	@echo "    make chat             chat-service       (port 9091)"
	@echo "    make notification     notification-svc   (port 9092)"
	@echo ""
	@echo "  Run Python services (foreground):"
	@echo "    make ai               ai-service         (uv)"
	@echo "    make preprocessor     preprocessor-svc   (uv)"
	@echo "    make srl              srl-service        (conda, FastAPI :8000)"
	@echo ""
	@echo "  Orchestration (background):"
	@echo "    make run-java         Start all Java services in background (logs/)"
	@echo "    make stop             Stop all background services"
	@echo "    make ps               Show running background services"
	@echo "    make tail SVC=auth    Tail the log of a background service"
	@echo ""
	@echo "  Override the Spring profile (default: dev):"
	@echo "    make run-java PROFILE=prod"

# ----------------------------------------------------------------------------
# Build
# ----------------------------------------------------------------------------
.PHONY: install-common
install-common:
	$(MVNW) -pl common install -DskipTests

.PHONY: build
build:
	$(MVNW) package -DskipTests

.PHONY: clean
clean:
	$(MVNW) clean

# ----------------------------------------------------------------------------
# Run Java services in the foreground
# ----------------------------------------------------------------------------
.PHONY: gateway auth user chat notification

gateway:
	$(MVNW) -pl api-gateway spring-boot:run -Dspring-boot.run.profiles=$(PROFILE)

auth:
	$(MVNW) -pl auth-service spring-boot:run -Dspring-boot.run.profiles=$(PROFILE)

user:
	$(MVNW) -pl user-service spring-boot:run -Dspring-boot.run.profiles=$(PROFILE)

chat:
	$(MVNW) -pl chat-service spring-boot:run -Dspring-boot.run.profiles=$(PROFILE)

notification:
	$(MVNW) -pl notification-service spring-boot:run -Dspring-boot.run.profiles=$(PROFILE)

# ----------------------------------------------------------------------------
# Run Python services in the foreground
# ----------------------------------------------------------------------------
.PHONY: ai preprocessor srl

ai:
	cd ai-service && uv run python src/main.py

preprocessor:
	cd preprocessor-service && uv run python src/main.py

srl:
	cd srl-service && uv run uvicorn src.main:app --host 0.0.0.0 --port 8000

# ----------------------------------------------------------------------------
# Background orchestration for Java services
# ----------------------------------------------------------------------------
.PHONY: logs-dir
logs-dir:
	@mkdir -p $(LOG_DIR) $(PID_DIR)

# Start one Java service in the background. Usage: make <svc>-bg
define JAVA_BG_TEMPLATE
.PHONY: $(1)-bg
$(1)-bg: logs-dir
	@echo "Starting $(1) (profile=$(PROFILE))..."
	@nohup $(MVNW) -pl $(1) spring-boot:run -Dspring-boot.run.profiles=$(PROFILE) \
		> $(LOG_DIR)/$(1).log 2>&1 & echo $$! > $(PID_DIR)/$(1).pid
	@echo "  -> logs/$(1).log (pid $$$$(cat $(PID_DIR)/$(1).pid))"
endef
$(foreach svc,$(JAVA_SERVICES),$(eval $(call JAVA_BG_TEMPLATE,$(svc))))

# Start all Java services in dependency-friendly order (auth first for token validation).
.PHONY: run-java
run-java: build auth-service-bg user-service-bg chat-service-bg notification-service-bg api-gateway-bg
	@echo ""
	@echo "All Java services started in background. Tail logs with: make tail SVC=<name>"

# Stop everything started via *-bg targets.
.PHONY: stop
stop:
	@if [ -d $(PID_DIR) ]; then \
		for pidfile in $(PID_DIR)/*.pid; do \
			[ -e "$$pidfile" ] || continue; \
			pid=$$(cat $$pidfile); \
			name=$$(basename $$pidfile .pid); \
			if kill -0 $$pid 2>/dev/null; then \
				echo "Stopping $$name (pid $$pid)"; \
				kill $$pid 2>/dev/null || true; \
			fi; \
			rm -f $$pidfile; \
		done; \
	else \
		echo "No background services tracked."; \
	fi

# Show status of background services.
.PHONY: ps
ps:
	@if [ -d $(PID_DIR) ] && ls $(PID_DIR)/*.pid >/dev/null 2>&1; then \
		for pidfile in $(PID_DIR)/*.pid; do \
			pid=$$(cat $$pidfile); \
			name=$$(basename $$pidfile .pid); \
			if kill -0 $$pid 2>/dev/null; then \
				echo "  RUNNING  $$name (pid $$pid)"; \
			else \
				echo "  STOPPED  $$name (stale pid $$pid)"; \
			fi; \
		done; \
	else \
		echo "No background services tracked."; \
	fi

# Tail a background service log. Usage: make tail SVC=auth-service
.PHONY: tail
tail:
	@test -n "$(SVC)" || (echo "Usage: make tail SVC=<service-name>"; exit 1)
	@tail -f $(LOG_DIR)/$(SVC).log
