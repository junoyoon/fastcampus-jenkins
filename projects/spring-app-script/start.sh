#!/bin/sh
cd /root/
echo "detaching server from lb"
touch about-to-shutdown
sleep 10

echo "kill previous server run"
ps -eaf | grep -v grep | grep -v defunct | grep java |  awk '{print "kill -TERM "$2}' | sh -x

echo "starting server"
nohup java -jar demo-0.0.1-SNAPSHOT.jar "$@" &

echo "waiting to run"
sleep 10

echo "attaching server to lb"
rm about-to-shutdown
sleep 5