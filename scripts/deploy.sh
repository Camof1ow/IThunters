# deploy.sh

#!/bin/bash

DEPLOY_LOG_PATH="/home/ubuntu/itmonster/deploy.log"
DEPLOY_ERR_LOG_PATH="/home/ubuntu/itmonster/deploy_err.log"
APPLICATION_LOG_PATH="/home/ubuntu/itmonster/application.log"


echo "===== 배포 시작 : $(date +%c) =====" >> $DEPLOY_LOG_PATH


echo "> 현재 동작중인 어플리케이션 pid 체크" >> $DEPLOY_LOG_PATH



echo "> 현재 구동중인 Set 확인" >> $DEPLOY_LOG_PATH
CURRENT_PROFILE=$(curl -s http://localhost/profile)
echo  ">>>>>> Set : $CURRENT_PROFILE" >> $DEPLOY_LOG_PATH

if [ ${CURRENT_PROFILE} == 1 ]
then
  kill -9 $(lsof -t -i:8082)
  PROFILE_SET="set2"
  IDLE_PORT=8082
elif [ ${CURRENT_PROFILE}  == 2 ]
then
  kill -9 $(lsof -t -i:8081)
  PROFILE_SET="set1"
  IDLE_PORT=8081
else
  echo "> 일치하는 Profile이 없습니다. Profile: $CURRENT_PROFILE" >> $DEPLOY_LOG_PATH
  echo "> 8081을 할당합니다." >> $DEPLOY_LOG_PATH
  IDLE_PORT=8081
fi

echo ""

echo "> 전환할 Port: $IDLE_PORT" >> $DEPLOY_LOG_PATH
echo "> Port 전환" >> $DEPLOY_LOG_PATH
echo "set \$service_url http://127.0.0.1:${IDLE_PORT};" |sudo tee /etc/nginx/conf.d/service-url.inc >> $DEPLOY_LOG_PATH

PROXY_PORT=$(curl -s http://localhost/profile)
echo "> Nginx Current Proxy Port: $PROXY_PORT" >> $DEPLOY_LOG_PATH

echo "> Nginx Reload" >> $DEPLOY_LOG_PATH
sudo service nginx reload # reload는 설정만 재적용하기 때문에 바로 적용이 가능합니다.

echo "> DEPLOY_JAR 배포" >> $DEPLOY_LOG_PATH
cd /home/ubuntu/itmonster/deploy/
chmod +x ITsquad-0.0.1-SNAPSHOT.jar
# shellcheck disable=SC2046
nohup java -jar -Duser.timezone=Asia/Seoul /home/ubuntu/itmonster/deploy/ITsquad-0.0.1-SNAPSHOT.jar --spring.config.location=./${PROFILE_SET}.properties --server.port=${IDLE_PORT} >> $APPLICATION_LOG_PATH 2> $DEPLOY_ERR_LOG_PATH &

sleep 3

echo "> 배포 종료 : $(date +%c)" >> $DEPLOY_LOG_PATH
