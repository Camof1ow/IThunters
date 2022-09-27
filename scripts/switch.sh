#!/usr/bin/env bash

SWITCH_LOG_PATH="/home/ubuntu/itmonster/switch.log"

echo "> 현재 구동중인  Port 확인" >> $SWITCH_LOG_PATH
CURRENT_PROFILE=$(curl -s http://localhost/profile)
echo "> ${CURRENT_PROFILE}" >> $SWITCH_LOG_PATH
if [ "$CURRENT_PROFILE" == 1 ]
then
  IDLE_PORT=8082
elif [ "$CURRENT_PROFILE" == 2 ]
then
  IDLE_PORT=8081
else
  echo "> 일치하는 Profile이 없습니다. Profile: $CURRENT_PROFILE" >> $SWITCH_LOG_PATH
  echo "> 8081을 할당합니다." >> $SWITCH_LOG_PATH
  IDLE_PORT=8081
fi

echo "> 전환할 Port: $IDLE_PORT" >> $SWITCH_LOG_PATH
echo "> Port 전환" >> $SWITCH_LOG_PATH
echo "set \$service_url http://127.0.0.1:${IDLE_PORT};" |sudo tee /etc/nginx/conf.d/service-url.inc >> $SWITCH_LOG_PATH

PROXY_PORT=$(curl -s http://localhost/profile)
echo "> Nginx Current Proxy Port: $PROXY_PORT" >> $SWITCH_LOG_PATH

echo "> Nginx Reload" >> $SWITCH_LOG_PATH
sudo service nginx reload # reload는 설정만 재적용하기 때문에 바로 적용이 가능합니다.
