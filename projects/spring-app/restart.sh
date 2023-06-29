#!/bin/sh
cd /root/
docker exec server1 "ps -eaf | grep -v grep | grep java |  awk '{print \"kill -TERM \"$1}' | sh -x"
sleep 10
java -jar demo-0.0.1-SNAPSHOT.jar
