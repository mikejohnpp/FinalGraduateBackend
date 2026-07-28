#!/bin/bash
# ==============================================================================
# VieFace JMeter Stress Test Runner (Step-up Load Test)
# Target: https://api.vieface.io.vn
# ==============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

if ! command -v jmeter &> /dev/null; then
    echo "[ERROR] Apache JMeter is not installed or not in PATH."
    exit 1
fi

HOST="api.vieface.io.vn"
PORT=443
PROTOCOL="https"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
RESULTS_BASE="results/stress_$TIMESTAMP"
mkdir -p "$RESULTS_BASE"

echo "=============================================================================="
# Step-up testing: 25 -> 50 -> 75 -> 100 -> 150 threads
# ==============================================================================
STEPS=(25 50 75 100 150)
LOOPS=15

for THREADS in "${STEPS[@]}"; do
    RAMPUP=$(( THREADS / 2 ))
    STEP_DIR="$RESULTS_BASE/step_${THREADS}_users"
    mkdir -p "$STEP_DIR"
    JTL_FILE="$STEP_DIR/results.jtl"
    REPORT_DIR="$STEP_DIR/html_report"
    
    echo ""
    echo ">>> Starting Stress Step: $THREADS Concurrent Users (Ramp-up: ${RAMPUP}s, Loops: $LOOPS)"
    
    jmeter -n \
        -t vieface-load-test.jmx \
        -Jhost="$HOST" \
        -Jport="$PORT" \
        -Jprotocol="$PROTOCOL" \
        -Jthreads="$THREADS" \
        -Jrampup="$RAMPUP" \
        -Jloops="$LOOPS" \
        -l "$JTL_FILE" \
        -e -o "$REPORT_DIR" || {
            echo "[WARNING] Step $THREADS encountered errors or failed assertion!"
        }
        
    echo ">>> Completed Step $THREADS. Report: $REPORT_DIR/index.html"
    echo "Waiting 10 seconds before next step..."
    sleep 10
done

echo "=============================================================================="
echo "[SUCCESS] Stress test completed across all steps! Results at: $RESULTS_BASE"
