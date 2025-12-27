#!/bin/bash
# check-db.sh - Database connectivity check

EC2_USER="ubuntu"
EC2_HOST="ec2-54-226-107-173.compute-1.amazonaws.com"
SSH_KEY="$HOME/.ssh/dsa-scheduler-app.pem"

ssh -i "$SSH_KEY" $EC2_USER@$EC2_HOST "
    echo '=== Checking MySQL Status ==='
    sudo systemctl status mysql --no-pager

    echo -e '\n=== Checking if MySQL is listening on port 3306 ==='
    sudo netstat -tuln | grep 3306 || echo 'MySQL not listening on 3306'

    echo -e '\n=== Testing MySQL connection ==='
    sudo mysql -u dsa_user -pSelim@123 -e 'SHOW DATABASES;' && echo 'MySQL connection successful' || echo 'MySQL connection failed'

    echo -e '\n=== Checking if database exists ==='
    sudo mysql -u dsa_user -pSelim@123 -e 'SHOW DATABASES LIKE \"dsa_scheduler_prod\";'

    echo -e '\n=== Current disk space ==='
    df -h
"