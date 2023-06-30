#!/bin/sh
while [ true ]
do
  echo "1-cicd 실행!"
  ./1-cicd.sh
  echo "30초 대기"
  sleep 30
done