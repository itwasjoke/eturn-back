#!/usr/bin/env bash
mvn clean package -Dmaven.test.skip=true
echo 'Copy files...'
scp /Users/new/Downloads/eturn/out/artifacts/eturn_jar2/eturn.jar \
    root@185.154.194.3:/home/admin/web/185.154.194.3/public_shtml
echo 'Restart server...'
ssh -t root@185.154.194.3 << EOF
pgrep java | xargs kill -9
nohup java -jar /home/admin/web/185.154.194.3/public_shtml/eturn.jar > /home/admin/web/185.154.194.3/log.txt &
EOF
echo 'Bye'