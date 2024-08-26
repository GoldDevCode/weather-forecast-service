#!/bin/bash

# Run tests
docker compose up -d --build test
test_status=$?

# Check if tests passed
if [ $test_status -eq 0 ]; then
  echo "Tests passed. Starting the application..."
  docker compose up -d --build app redis
else
  echo "Tests failed. Application will not start."
  exit 1
fi