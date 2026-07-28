#!/bin/bash
# ==============================================================================
# VieFace JMeter Load Test Runner
# Target: https://api.vieface.io.vn
# ==============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Check if JMeter is installed
if ! command -v jmeter &> /dev/null; then
    echo "[ERROR] Apache JMeter is not installed or not in PATH."
    echo "Please install JMeter (e.g. 'sudo apt install jmeter' or download from apache website)."
    exit 1
fi

PROFILE=${1:-smoke}
HOST="api.vieface.io.vn"
PORT=443
PROTOCOL="https"

THREADS=1
RAMPUP=1
LOOPS=1

case "$PROFILE" in
    smoke)
        echo "=== [MODE: SMOKE TEST] (1 User, 1 Loop, Verify Functional Correctness) ==="
        THREADS=1; RAMPUP=1; LOOPS=1
        ;;
    light)
        echo "=== [MODE: LIGHT LOAD] (10 Users, 5 Loops) ==="
        THREADS=10; RAMPUP=10; LOOPS=5
        ;;
    normal)
        echo "=== [MODE: NORMAL LOAD] (25 Users, 10 Loops) ==="
        THREADS=25; RAMPUP=20; LOOPS=10
        ;;
    heavy)
        echo "=== [MODE: HEAVY LOAD] (50 Users, 20 Loops) ==="
        THREADS=50; RAMPUP=30; LOOPS=20
        ;;
    custom)
        shift
        THREADS=${1:-10}
        RAMPUP=${2:-10}
        LOOPS=${3:-10}
        echo "=== [MODE: CUSTOM LOAD] ($THREADS Users, Rampup: ${RAMPUP}s, Loops: $LOOPS) ==="
        ;;
    *)
        echo "Usage: $0 [smoke|light|normal|heavy|custom <threads> <rampup> <loops>]"
        exit 1
        ;;
esac

TIMESTAMP=$(date +%Y%m%d_%H%M%S)
RESULTS_DIR="results/run_${PROFILE}_${TIMESTAMP}"
mkdir -p "$RESULTS_DIR"
JTL_FILE="$RESULTS_DIR/results.jtl"
REPORT_DIR="$RESULTS_DIR/html_report"

echo "Target Server: $PROTOCOL://$HOST:$PORT"
echo "Results Directory: $RESULTS_DIR"
echo "------------------------------------------------------------------------------"

jmeter -n \
    -t vieface-load-test.jmx \
    -Jhost="$HOST" \
    -Jport="$PORT" \
    -Jprotocol="$PROTOCOL" \
    -Jthreads="$THREADS" \
    -Jrampup="$RAMPUP" \
    -Jloops="$LOOPS" \
    -l "$JTL_FILE" \
    -e -o "$REPORT_DIR"

echo "------------------------------------------------------------------------------"
echo "[SUCCESS] Test finished successfully!"
echo "Summary report generated at: $REPORT_DIR/index.html"
echo "Raw JTL log located at: $JTL_FILE"
