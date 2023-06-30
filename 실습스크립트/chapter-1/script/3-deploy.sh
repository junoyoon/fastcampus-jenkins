#!/bin/sh

cat > deploy/start.sh << EOF
echo "기존 프로세스를 죽임"
ps -eaf | grep -v grep | grep -v defunct | grep java |  awk '{print "kill -TERM "\$2}' | sh -x

echo "신규 버전 실행"
nohup java -jar demo-0.0.1-SNAPSHOT.jar "" &
sleep 2
ps -eaf | grep -v grep | grep -v defunct | grep java |  awk '{print "new pid is "\$2}'
EOF

chmod 755 deploy/start.sh

scp -o StrictHostKeychecking=no -i /key/private.key -P 2222 deploy/* \
     user@server_1:~

ssh -o StrictHostKeychecking=no -tt -i /key/private.key -p 2222 user@server_1 \
    "./start.sh"
