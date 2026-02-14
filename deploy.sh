#!/usr/bin/env bash
set -euo pipefail

# Optional: uncomment for full debug output in GitHub Actions logs
# set -x

# ────────────────────────────────────────────────
# 1. Variables & constants
# ────────────────────────────────────────────────
APP_DIR="/home/ubuntu/app"
JAR_NAME="tweetArchive-0.0.1-SNAPSHOT.jar"
SERVICE_NAME="tweetarchive"
ENV_FILE="/home/ubuntu/app/.env"

# ────────────────────────────────────────────────
# 2. Create environment file for systemd
# ────────────────────────────────────────────────
echo "Creating environment file at $ENV_FILE"
cat > "$ENV_FILE" << EOF
DB_URL=${DB_URL}
DB_USERNAME=${DB_USERNAME}
DB_PASSWORD=${DB_PASSWORD}
JWT_SECRET_KEY=${JWT_SECRET_KEY}
AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID}
AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY}
AWS_REGION=${AWS_REGION}
AWS_S3_BUCKET=${AWS_S3_BUCKET}
MAILUSERNAME=${MAILUSERNAME}
MAILPASSWORD=${MAILPASSWORD}
GROKAPIKEY=${GROKAPIKEY}
SPRING_JPA_HIBERNATE_DDL_AUTO=${SPRING_JPA_HIBERNATE_DDL_AUTO}
SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-prod}
SERVER_PORT=${SERVER_PORT}
SERVER_SSL_KEY_STORE=${SERVER_SSL_KEY_STORE}
SERVER_SSL_KEY_ALIAS=${SERVER_SSL_KEY_ALIAS}
SERVER_SSL_KEY_STORE_PASSWORD=${SERVER_SSL_KEY_STORE_PASSWORD}
SERVER_SSL_KEY_STORE_TYPE=${SERVER_SSL_KEY_STORE_TYPE}
EOF

# Secure the environment file (important!)
chmod 600 "$ENV_FILE"
echo "Environment file created and secured"

# ────────────────────────────────────────────────
# 3. Change directory with better error message
# ────────────────────────────────────────────────
if ! cd "$APP_DIR"; then
  echo "Error: Cannot change to directory $APP_DIR (does not exist or permission denied)"
  ls -la /home/ubuntu || true
  exit 1
fi

echo "Working directory: $(pwd)"
echo "Files currently in directory:"
ls -la

# ────────────────────────────────────────────────
# 4. Stop the service gracefully (ignore if not running)
# ────────────────────────────────────────────────
echo "Stopping $SERVICE_NAME service..."
if sudo systemctl stop "$SERVICE_NAME"; then
  echo "Service stopped successfully"
else
  echo "Service was not running or stop failed (continuing)"
fi

# Give systemd a moment to release the port/file locks
sleep 2

# ────────────────────────────────────────────────
# 5. Backup previous JAR (only if it exists)
# ────────────────────────────────────────────────
if [ -f "$JAR_NAME" ]; then
  TIMESTAMP=$(date +%Y%m%d-%H%M%S)
  BACKUP_NAME="${JAR_NAME%.jar}-previous-${TIMESTAMP}.jar"
  mv "$JAR_NAME" "$BACKUP_NAME"
  echo "Backed up previous JAR → $BACKUP_NAME"
else
  echo "No previous $JAR_NAME found (first deployment or previous rename failed?)"
fi

# ────────────────────────────────────────────────
# 6. Find and rename the newly uploaded JAR
# ────────────────────────────────────────────────
shopt -s nullglob

# Prefer exact Maven naming pattern first, then fallback to any .jar
new_jars=( tweetArchive-*.jar *.jar )

if [ ${#new_jars[@]} -eq 0 ]; then
  echo "ERROR: No .jar file found in $(pwd) after upload"
  echo "Directory listing:"
  ls -la
  exit 1
fi

if [ ${#new_jars[@]} -gt 1 ]; then
  echo "Warning: Multiple JARs found: ${new_jars[*]}"
  echo "→ Using the first one: ${new_jars[0]}"
fi

echo "Found uploaded JAR: ${new_jars[0]}"
echo "Renaming to: $JAR_NAME"

mv "${new_jars[0]}" "$JAR_NAME"

# Verify if rename succeeded changed
if [ ! -f "$JAR_NAME" ]; then
  echo "ERROR: Rename failed! File $JAR_NAME still does not exist"
  ls -la
  exit 1
fi

echo "Successfully renamed → $JAR_NAME"
ls -lh "$JAR_NAME"
echo "Current directory after rename:"
ls -la

# ────────────────────────────────────────────────
# 7. Optional: Clean very old backups (keep last 5)
# ────────────────────────────────────────────────
# find . -name "${JAR_NAME%.jar}-previous-*.jar" -type f | sort | head -n -5 | xargs -r rm -v

# ────────────────────────────────────────────────
# 8. Start the service
# ────────────────────────────────────────────────
echo "Starting $SERVICE_NAME service..."
sudo systemctl start "$SERVICE_NAME"

# Give it time to start (adjust if your app is slow)
sleep 10

# ────────────────────────────────────────────────
# 9. Show detailed status + recent logs
# ────────────────────────────────────────────────
echo "Service status:"
sudo systemctl status "$SERVICE_NAME" --no-pager --lines=30 || true

echo -e "\nRecent journal logs (last 40 lines):"
sudo journalctl -u "$SERVICE_NAME" --no-pager --lines=40 || echo "No journal logs available"

# ────────────────────────────────────────────────
# 10. Optional: Simple health check
# ────────────────────────────────────────────────
# echo "Waiting for application to become healthy..."
# for i in {1..12}; do
#   if curl -f -s http://localhost:8080/actuator/health >/dev/null 2>&1; then
#     echo "Application is healthy ✓"
#     exit 0
#   fi
#   echo "Attempt $i/12 - still starting..."
#   sleep 5
# done deploying app
# echo "Warning: Health check timed out after 60s"

echo "Deployment finished. Check logs if needed:"
echo "  sudo journalctl -u $SERVICE_NAME -f"