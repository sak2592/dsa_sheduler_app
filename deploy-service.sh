#!/bin/bash
# deploy-service.sh - Service management script for DSA Scheduler

APP_JAR="dsa_scheduler_app-1.0.0.jar"
DEPLOY_DIR="/home/ec2-user/app"
LOG_DIR="/var/log/dsa-scheduler"

start_service() {
    echo "Starting DSA Scheduler..."
    cd $DEPLOY_DIR

    export DB_HOST="localhost"
    export DB_USERNAME="root"
    export DB_PASSWORD="${DB_PASSWORD}"
    export SPRING_PROFILES_ACTIVE="prod"

    nohup java -jar $APP_JAR \
        --spring.config.location=classpath:/,file:./application-prod.properties \
        --logging.file.name=$LOG_DIR/app.log \
        > $LOG_DIR/console.log 2>&1 &

    echo $! > $DEPLOY_DIR/app.pid
    echo "Service started with PID: $(cat $DEPLOY_DIR/app.pid)"
}

stop_service() {
    if [ -f $DEPLOY_DIR/app.pid ]; then
        PID=$(cat $DEPLOY_DIR/app.pid)
        echo "Stopping service with PID: $PID"
        kill $PID
        rm $DEPLOY_DIR/app.pid
        echo "Service stopped"
    else
        echo "No PID file found. Searching for process..."
        PID=$(ps aux | grep $APP_JAR | grep -v grep | awk '{print $2}')
        if [ ! -z "$PID" ]; then
            echo "Stopping process $PID"
            kill $PID
            echo "Service stopped"
        else
            echo "Service is not running"
        fi
    fi
}

restart_service() {
    stop_service
    sleep 5
    start_service
}

case "$1" in
    start)
        start_service
        ;;
    stop)
        stop_service
        ;;
    restart)
        restart_service
        ;;
    status)
        if [ -f $DEPLOY_DIR/app.pid ]; then
            PID=$(cat $DEPLOY_DIR/app.pid)
            if ps -p $PID > /dev/null; then
                echo "Service is running (PID: $PID)"
            else
                echo "PID file exists but process is not running"
            fi
        else
            echo "Service is not running"
        fi
        ;;
    *)
        echo "Usage: $0 {start|stop|restart|status}"
        exit 1
        ;;
esac