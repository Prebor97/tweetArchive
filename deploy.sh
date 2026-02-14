#!/usr/bin/env bash
set -euo pipefail

# ────────────────────────────────────────────────
# 1. Export all environment variables passed from GitHub Actions
#    Spring Boot reads these via System.getenv() or @Value
# ────────────────────────────────────────────────
export DB_URL
export DB_USERNAME
export DB_PASSWORD
export JWT_SECRET_KEY
export AWS_ACCESS_KEY_ID
export AWS_SECRET_ACCESS_KEY
export AWS_REGION
export AWS_S3_BUCKET
export MAILUSERNAME
export MAILPASSWORD
export GROKAPIKEY
export SPRING_JPA_HIBERNATE_DDL_AUTO
export SPRING_PROFILES_ACTIVE   # optional - set in workflow if needed

# ────────────────────────────────────────────────
# 2. Variables & constants
# ────────────────────────────────────────────────
APP_DIR="/home/${EC2_USER}/app"   # safer than variable expansion in cd
JAR_NAME="tweetarchive.jar"
SERVICE_NAME="tweetarchive"

# ────────────────────────────────────────────────
# 3. Change directory with better error message
# ────────────────────────────────────────────────
if ! cd "$APP_DIR"; then
  echo "Error: Cannot change to directory $APP_DIR (does not exist or permission denied)"
  exit 1
fi

echo "Working directory: $(pwd)"

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
  echo "No previous $JAR_NAME found (first deployment?)"
fi

# ────────────────────────────────────────────────
# 6. Find and rename the newly uploaded JAR
# ────────────────────────────────────────────────
shopt -s nullglob
new_jars=( *.jar )

if [ ${#new_jars[@]} -eq 0 ]; then
  echo "ERROR: No .jar file found in $APP_DIR after upload"
  echo "Directory listing:"
  ls -la
  exit 1
fi

if [ ${#new_jars[@]} -gt 1 ]; then
  echo "Warning: Multiple JARs found (${new_jars[*]}) → using the first one: ${new_jars[0]}"
fi

mv "${new_jars[0]}" "$JAR_NAME"
echo "Renamed uploaded JAR → $JAR_NAME"
ls -lh "$JAR_NAME"   # show size & permissions for debugging

# ────────────────────────────────────────────────
# 7. Optional: Clean very old backups (keep last 5)
#    Uncomment if disk space becomes an issue
# ────────────────────────────────────────────────
# find . -name "${JAR_NAME%.jar}-previous-*.jar" -type f | sort | head -n -5 | xargs -r rm -v

# ────────────────────────────────────────────────
# 8. Start the service
# ────────────────────────────────────────────────
echo "Starting $SERVICE_NAME service..."
sudo systemctl start "$SERVICE_NAME"

# Give it time to start (adjust if your app is slow)
sleep 8

# ────────────────────────────────────────────────
# 9. Show detailed status + recent logs
# ────────────────────────────────────────────────
echo "Service status:"
sudo systemctl status "$SERVICE_NAME" --no-pager --lines=30 || true

echo -e "\nRecent journal logs (last 40 lines):"
sudo journalctl -u "$SERVICE_NAME" --no-pager --lines=40 || echo "No journal logs available"

# ────────────────────────────────────────────────
# 10. Optional: Simple health check (uncomment if you have /actuator/health)
# ────────────────────────────────────────────────
# echo "Waiting for application to become healthy..."
# for i in {1..12}; do
#   if curl -f -s http://localhost:8080/actuator/health >/dev/null 2>&1; then
#     echo "Application is healthy ✓"
#     exit 0
#   fi
#   echo "Attempt $i/12 - still starting..."
#   sleep 5
# done
# echo "Warning: Health check timed out after 60s"
# exit 1

echo "Deployment finished. Check logs if needed:"
echo "  sudo journalctl -u $SERVICE_NAME -f"