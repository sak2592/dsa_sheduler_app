#!/bin/bash
JAR_FILE="target/dsa-scheduler.jar"
EC2_USER="ubuntu"
EC2_HOST="ec2-54-87-141-191.compute-1.amazonaws.com"
KEY_FILE="dsa_scheduler.pem"

echo "Building project..."
mvn clean package

echo "Deploying to EC2..."
scp -i "$KEY_FILE" "$JAR_FILE" $EC2_USER@$EC2_HOST:/home/ubuntu/

echo "Restarting application on EC2..."
ssh -i "$KEY_FILE" $EC2_USER@$EC2_HOST "pkill -f 'java -jar' || true"
ssh -i "$KEY_FILE" $EC2_USER@$EC2_HOST "java -jar /home/ubuntu/dsa-scheduler.jar > /home/ubuntu/app.log 2>&1 &"

echo "Deployment complete!"



