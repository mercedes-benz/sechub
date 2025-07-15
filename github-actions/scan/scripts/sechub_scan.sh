#!/bin/bash

# Start the node process in the background
node "$GITHUB_ACTION_PATH"/dist/index.js &

# Capture the PID of the node process
NODE_PID=$!

# Function to forward signals
forward_signal() {
  kill -s "$1" "$NODE_PID"
}

# Trap SIGTERM and SIGINT and forward them
trap 'forward_signal SIGTERM' SIGTERM
trap 'forward_signal SIGINT' SIGINT

# Wait for the node process to finish
wait "$NODE_PID"