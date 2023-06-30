#!/bin/bash

cat > deploy/start.sh << EOF
echo "-----------------"
echo "LB 에서 제외"
echo "-----------------"
touch about-to-shutdown
sleep 5

echo "-----------------"
echo "기존 프로세스를 죽임"
echo "-----------------"
ps -eaf | grep -v grep | grep -v defunct | grep java |  awk '{print "kill -TERM "\$2}' | sh -x

echo "-----------------"
echo "신규 버전 실행"
echo "-----------------"
nohup java -jar demo-0.0.1-SNAPSHOT.jar --application.branch=\`hostname\` &
sleep 10
ps -eaf | grep -v grep | grep -v defunct | grep java |  awk '{print "new pid is "\$2}'

echo "-----------------"
echo "LB 에 다시 추가"
echo "-----------------"
rm about-to-shutdown
sleep 5

EOF

chmod 755 deploy/start.sh

for server in server_1 server_2
do
  echo "-----------------"
  echo "$server 배포 시작 "
  echo "-----------------"
  scp -o StrictHostKeychecking=no -i /key/private.key -P 2222 deploy/* \
      user@$server:~

  echo "-----------------"
  echo "$server 재시작 "
  echo "-----------------"
  ssh -o StrictHostKeychecking=no -tt -i /key/private.key -p 2222 \
      user@$server "./start.sh"
  echo "-----------------"
  echo "$server 배포 완료 "
  echo "-----------------"
done