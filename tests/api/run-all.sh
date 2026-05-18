#!/usr/bin/env bash

# Default to dev.env if no argument is provided
ENV_FILE=${1:-vars/dev.env}

if [ ! -f "$ENV_FILE" ]; then
    echo "Error: Environment file '$ENV_FILE' not found."
    exit 1
fi

echo "Running Hurl tests using environment: $ENV_FILE"

# Run all .hurl files in the directory
hurl --variables-file "$ENV_FILE" --test *.hurl
