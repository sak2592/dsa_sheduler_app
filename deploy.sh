#!/bin/bash

# =============================================
# DEPLOYMENT SCRIPT FOR DSA SCHEDULER APP
# =============================================

set -e  # Exit on any error

# Configuration
APP_NAME="dsa_schedular_app"
EC2_USER="ubuntu"
EC2_HOST="ec2-34-229-153-181.compute-1.amazonaws.com"
SSH_KEY="$HOME/.ssh/dsa_shedular.pem"  # Fixed path to use home directory
DEPLOY_DIR="/home/${EC2_USER}/app"
BACKUP_DIR="/home/${EC2_USER}/backups"
LOG_DIR="/var/log/dsa-schedular"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Variables that will be set dynamically
JAR_NAME=""
JAR_FILE=""

# Logging function
log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')] $1${NC}"
}

warn() {
    echo -e "${YELLOW}[$(date +'%Y-%m-%d %H:%M:%S')] WARNING: $1${NC}"
}

error() {
    echo -e "${RED}[$(date +'%Y-%m-%d %H:%M:%S')] ERROR: $1${NC}"
    exit 1
}

# Check if EC2_HOST is set
if [ -z "$EC2_HOST" ]; then
    error "Please set EC2_HOST variable in the script"
fi

# Function to find JAR file
find_jar() {
    log "Looking for JAR file in target directory..."
    JAR_FILE=$(find target -name "*.jar" -type f | head -1)

    if [ -z "$JAR_FILE" ]; then
        error "No JAR file found in target directory. Please build the project first."
    fi

    JAR_NAME=$(basename "$JAR_FILE")
    log "Found JAR file: $JAR_FILE"
    log "Using JAR name: $JAR_NAME"
}

# Function to check if JAR file exists
check_jar() {
    find_jar

    if [ ! -f "$JAR_FILE" ]; then
        error "JAR file $JAR_FILE not found."
    fi
}

# Function to build the project
build_app() {
    log "Building the application..."
    ./mvnw clean package -DskipTests || error "Build failed"

    # Find the JAR file that was created
    find_jar

    log "Build completed successfully"
}

# SSH connection function
ssh_connect() {
    if [ -z "$SSH_KEY" ] || [ ! -f "$SSH_KEY" ]; then
        ssh -o StrictHostKeyChecking=no -o ConnectTimeout=10 ${EC2_USER}@${EC2_HOST} "$@"
    else
        ssh -i "$SSH_KEY" -o StrictHostKeyChecking=no -o ConnectTimeout=10 ${EC2_USER}@${EC2_HOST} "$@"
    fi
}

# SCP connection function
scp_connect() {
    local source="$1"
    local destination="$2"
    if [ -z "$SSH_KEY" ] || [ ! -f "$SSH_KEY" ]; then
        scp -o StrictHostKeyChecking=no -o ConnectTimeout=10 "$source" "$destination"
    else
        scp -i "$SSH_KEY" -o StrictHostKeyChecking=no -o ConnectTimeout=10 "$source" "$destination"
    fi
}

# Test SSH connection
test_ssh() {
    log "Testing SSH connection to ${EC2_USER}@${EC2_HOST} with key: $SSH_KEY"

    # Check if SSH key exists
    if [ -n "$SSH_KEY" ] && [ ! -f "$SSH_KEY" ]; then
        warn "SSH key not found at: $SSH_KEY"
        warn "Trying password authentication..."
    fi

    if ssh_connect "echo 'SSH connection successful'"; then
        log "SSH connection test passed"
        return 0
    else
        error "SSH connection failed. Please check:\n1. EC2_HOST is correct: $EC2_HOST\n2. SSH key exists: $SSH_KEY\n3. Key permissions (should be 400)\n4. Security group allows SSH (port 22)\n5. Instance is running"
        return 1
    fi
}

# Function to create necessary directories on EC2
setup_remote_dirs() {
    log "Setting up directories on EC2 instance..."
    ssh_connect "
        # Create log directory with proper permissions
        sudo mkdir -p ${LOG_DIR}
        sudo chown ${EC2_USER}:${EC2_USER} ${LOG_DIR}
        sudo chmod 755 ${LOG_DIR}

        # Create deployment directories
        mkdir -p ${DEPLOY_DIR}
        mkdir -p ${BACKUP_DIR}

        # Create environment file for production
        cat > ${DEPLOY_DIR}/app.env << 'ENV'
DB_HOST=localhost
DB_USERNAME=root
DB_PASSWORD=Selim@123!
SPRING_PROFILES_ACTIVE=prod
ENV

        echo 'Directories created successfully'
    " || error "Failed to setup remote directories"
}

# Function to backup current deployment
backup_current() {
    log "Backing up current deployment..."
    ssh_connect "
        if ls ${DEPLOY_DIR}/*.jar 1> /dev/null 2>&1; then
            BACKUP_FILE=\"${BACKUP_DIR}/${APP_NAME}-backup-\$(date +%Y%m%d%H%M%S).jar\"
            cp ${DEPLOY_DIR}/*.jar \"\${BACKUP_FILE}\"
            echo \"Backup created: \${BACKUP_FILE}\"
        else
            echo \"No existing JAR to backup\"
        fi
    " || warn "Backup failed or no JAR to backup"
}

# Function to stop current application
stop_app() {
    log "Stopping current application..."
    ssh_connect "
        # Find any running Java application
        APP_PID=\$(ps aux | grep java | grep -v grep | awk '{print \$2}')
        if [ ! -z \"\$APP_PID\" ]; then
            echo \"Stopping Java processes with PIDs: \$APP_PID\"
            kill \$APP_PID 2>/dev/null || true
            sleep 3
            # Force kill if still running
            for pid in \$APP_PID; do
                if ps -p \$pid > /dev/null 2>&1; then
                    echo \"Force stopping PID: \$pid\"
                    kill -9 \$pid 2>/dev/null || true
                fi
            done
            echo \"Application stopped\"
        else
            echo \"No running Java application found\"
        fi
    " || warn "No running application found or failed to stop"
}

# Function to deploy the application
deploy_app() {
    log "Deploying application to EC2..."

    # Copy JAR to EC2
    scp_connect "$JAR_FILE" "${EC2_USER}@${EC2_HOST}:${DEPLOY_DIR}/" || error "Failed to copy JAR file"

    log "Application deployed successfully"
}

# Function to start the application
# In the start_app() function, update the database configuration:
start_app() {
    log "Starting application..."
    ssh_connect "
        cd ${DEPLOY_DIR}

        # Find the JAR file
        JAR_FILE=\$(ls *.jar | head -1)
        if [ -z \"\$JAR_FILE\" ]; then
            echo \"ERROR: No JAR file found in ${DEPLOY_DIR}\"
            exit 1
        fi

        echo \"Starting application: \$JAR_FILE\"

        # Set environment variables with correct database configuration
        export DB_HOST=\"localhost\"
        export DB_USERNAME=\"root\"  # Changed from root to dedicated user
        export DB_PASSWORD=\"Selim@123\"
        export DB_NAME=\"dsa_scheduler_prod\"  # Added database name
        export SPRING_PROFILES_ACTIVE=\"prod\"

        # Start the application with better error handling
        nohup java -jar \$JAR_FILE > ${LOG_DIR}/console.log 2>&1 &

        echo \"Application starting in background...\"
        sleep 10  # Increased sleep time

        # Check if application started successfully
        APP_PID=\$(ps aux | grep java | grep \$JAR_FILE | grep -v grep | awk '{print \$2}')
        if [ ! -z \"\$APP_PID\" ]; then
            echo \"Application started successfully with PID: \$APP_PID\"
            echo \$APP_PID > ${DEPLOY_DIR}/app.pid

            # Wait a bit more and check if it's still running
            sleep 5
            if ps -p \$APP_PID > /dev/null; then
                echo \"Application is stable and running\"
            else
                echo \"Application crashed shortly after starting. Check logs:\"
                tail -50 ${LOG_DIR}/console.log
            fi
        else
            echo \"Application failed to start. Check logs in ${LOG_DIR}/\"
            echo \"=== Last 50 lines of console log ===\"
            tail -50 ${LOG_DIR}/console.log 2>/dev/null || echo \"No console log found\"
        fi
    "
}
# Function to check application health
check_health() {
    log "Checking application health..."
    sleep 15  # Wait for application to start

    ssh_connect "
        HEALTH_CHECK_URL=\"http://localhost:8080/actuator/health\"

        # First check if port is open
        if netstat -tuln | grep ':8080' > /dev/null; then
            echo \"Port 8080 is listening\"
        else
            echo \"Port 8080 is not listening\"
        fi

        if curl -f -s \"\$HEALTH_CHECK_URL\" > /dev/null 2>&1; then
            echo \"Application is healthy\"
            curl -s \"\$HEALTH_CHECK_URL\" | grep -o '\"status\":\"[^\"]*\"'
        else
            echo \"Application health check failed\"
            echo \"=== Checking application status ===\"
            ps aux | grep java | grep -v grep || echo \"No Java process found\"
            echo \"=== Recent logs ===\"
            tail -30 ${LOG_DIR}/console.log 2>/dev/null || echo \"No console log found\"
        fi
    " || warn "Health check failed"
}

# Function to show deployment status
show_status() {
    log "Current deployment status:"
    ssh_connect "
        echo \"=== Running Java Processes ===\"
        ps aux | grep java | grep -v grep || echo \"No Java processes running\"

        echo -e \"\n=== Deployment Files ===\"
        ls -la ${DEPLOY_DIR}/ 2>/dev/null || echo \"Deployment directory not found\"

        echo -e \"\n=== Recent Logs ===\"
        if [ -f \"${LOG_DIR}/console.log\" ]; then
            tail -10 ${LOG_DIR}/console.log
        else
            echo \"No console log found\"
        fi

        echo -e \"\n=== Disk Usage ===\"
        df -h /home
    "
}

# Function to create systemd service
create_systemd_service() {
    log "Creating systemd service..."
    ssh_connect "
        # Find the actual JAR file name
        JAR_FILE=\$(ls ${DEPLOY_DIR}/*.jar | head -1)
        JAR_NAME=\$(basename \$JAR_FILE)

        sudo tee /etc/systemd/system/dsa-scheduler.service > /dev/null << SERVICE
[Unit]
Description=DSA Scheduler Application
After=network.target

[Service]
Type=simple
User=${EC2_USER}
WorkingDirectory=${DEPLOY_DIR}
Environment=DB_HOST=localhost
Environment=DB_USERNAME=root
Environment=DB_PASSWORD=Selim@123
Environment=SPRING_PROFILES_ACTIVE=prod
ExecStart=/usr/bin/java -jar ${DEPLOY_DIR}/\$JAR_NAME
SuccessExitStatus=143
Restart=always
RestartSec=10
StandardOutput=append:${LOG_DIR}/console.log
StandardError=append:${LOG_DIR}/console.log

[Install]
WantedBy=multi-user.target
SERVICE

        sudo systemctl daemon-reload
        sudo systemctl enable dsa-scheduler.service
        echo \"Systemd service created and enabled for JAR: \$JAR_NAME\"
    " || error "Failed to create systemd service"
}

# Main deployment function
full_deploy() {
    log "Starting full deployment process..."

    test_ssh
    check_jar
    stop_app
    backup_current
    setup_remote_dirs
    deploy_app
    start_app
    #check_health
    show_status

    log "Deployment completed successfully!"
}

# Quick deploy (assumes build is already done)
quick_deploy() {
    log "Starting quick deployment..."

    test_ssh
    check_jar
    stop_app
    deploy_app
    start_app
    #check_health

    log "Quick deployment completed!"
}

# Usage information
usage() {
    echo "Usage: $0 [option]"
    echo "Options:"
    echo "  build        - Build the application only"
    echo "  deploy       - Full deployment (build + deploy)"
    echo "  quick        - Quick deploy (assumes build exists)"
    echo "  stop         - Stop the application"
    echo "  start        - Start the application"
    echo "  status       - Show deployment status"
    echo "  health       - Check application health"
    echo "  test-ssh     - Test SSH connection"
    echo "  systemd      - Create systemd service"
    echo "  all          - Full deployment with systemd service"
}

# Main script execution
case "$1" in
    "build")
        build_app
        ;;
    "deploy")
        full_deploy
        ;;
    "quick")
        quick_deploy
        ;;
    "stop")
        test_ssh
        stop_app
        ;;
    "start")
        test_ssh
        start_app
        ;;
    "status")
        test_ssh
        show_status
        ;;
    "health")
        test_ssh
        check_health
        ;;
    "test-ssh")
        test_ssh
        ;;
    "systemd")
        test_ssh
        create_systemd_service
        ;;
    "all")
        build_app
        full_deploy
        create_systemd_service
        ;;
    *)
        usage
        exit 1
        ;;
esac