#!/bin/bash
set -e

# Export environment variables passed from GitHub Actions
# Spring Boot will read these at startup
export DB_URL
export DB_USERNAME
export DB_PASSWORD
export JWT_SECRET_KEY
export AWS_ACCESS_KEY_ID
export AWS_SECRET_ACCESS_KEY
export AWS_REGION
export AWS_S3_BUCKET
export SPRING_JPA_HIBERNATE_DDL_AUTO

# Navigate to app directory
cd /home/${EC2_USER}/app || { echo "Error: Cannot cd to /home/${EC2_USER}/app"; exit 1; }

echo "Stopping tweetarchive service..."
sudo systemctl stop tweetarchive || true

# Optional: Backup previous JAR
if [ -f tweetarchive.jar ]; then
  TIMESTAMP=$(date +%Y%m%d-%H%M%S)
  mv tweetarchive.jar "tweetarchive-previous-${TIMESTAMP}.jar"
  echo "Backed up previous JAR → tweetarchive-previous-${TIMESTAMP}.jar"
fi

# Find the uploaded JAR (should be only one)
shopt -s nullglob
new_jars=( *.jar )

if [ ${#new_jars[@]} -eq 0 ]; then
  echo "Error: No .jar file found after upload"
  exit 1
fi

if [ ${#new_jars[@]} -gt 1 ]; then
  echo "Warning: Multiple JARs found → using first: ${new_jars[0]}"
fi

# Rename to fixed name your service expects
mv "${new_jars[0]}" tweetarchive.jar
echo "Renamed → tweetarchive.jar"

# Start service (inherits exported env vars)
echo "Starting tweetarchive service..."
sudo systemctl start tweetarchive

# Wait briefly
sleep 10

# Show status (visible in GitHub Actions logs)
echo "Service status:"
sudo systemctl status tweetarchive --no-pager --lines=20

# Optional: Wait for health check (if Actuator enabled)
# until curl -f -s http://localhost:8080/actuator/health >/dev/null 2>&1; do
#   echo "Waiting for healthy..."
#   sleep 3
# done
# echo "App is healthy"