# deploy.sh

#!/bin/bash

DEPLOY_LOG_PATH="/home/ubuntu/itmonster/deploy.log"
DEPLOY_ERR_LOG_PATH="/home/ubuntu/itmonster/deploy_err.log"
APPLICATION_LOG_PATH="/home/ubuntu/itmonster"


echo "===== 배포 시작 : $(date +%c) =====" >> $DEPLOY_LOG_PATH

echo "> 현재 구동중인 Set 확인" >> $DEPLOY_LOG_PATH
CURRENT_PROFILE=$(curl -s http://localhost/profile)
echo  ">>>>>> Set : $CURRENT_PROFILE" >> $DEPLOY_LOG_PATH

if [ "$CURRENT_PROFILE" == 1 ]
then
  kill -9 $(lsof -t -i:8082)
  PROFILE_SET="set2"
  IDLE_PORT=8082
  echo ">8082 포트 할당 "
elif [ "$CURRENT_PROFILE" == 2 ]
then
  kill -9 $(lsof -t -i:8081)
  PROFILE_SET="set1"
  IDLE_PORT=8081
  echo ">8081 포트 할당 "
else
  echo "> 일치하는 Profile이 없습니다. Profile: $CURRENT_PROFILE" >> $DEPLOY_LOG_PATH
  echo "> 8081을 할당합니다." >> $DEPLOY_LOG_PATH
  IDLE_PORT=8081
fi

echo "> DEPLOY_JAR 배포" >> $DEPLOY_LOG_PATH
cd /home/ubuntu/itmonster/deploy/
chmod +x ITsquad-0.0.1-SNAPSHOT.jar
# shellcheck disable=SC2046
nohup java -jar -Duser.timezone=Asia/Seoul /home/ubuntu/itmonster/deploy/ITsquad-0.0.1-SNAPSHOT.jar --spring.config.location=./${PROFILE_SET}.properties --server.port=${IDLE_PORT} >> $APPLICATION_LOG_PATH/application$IDLE_PORT.log 2>&1 $DEPLOY_ERR_LOG_PATH &
#nohup java -jar -Duser.timezone=Asia/Seoul /home/ubuntu/itmonster/deploy/ITsquad-0.0.1-SNAPSHOT.jar --spring.config.location=./set2.properties --server.port=8082&
#nohup java -jar -Duser.timezone=Asia/Seoul /home/ubuntu/itmonster/deploy/ITsquad-0.0.1-SNAPSHOT.jar --spring.config.location=./set1.properties --server.port=8081&
sleep 60s

echo "> 배포 종료 : $(date +%c)" >> $DEPLOY_LOG_PATH
