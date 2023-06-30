#!/bin/bash
git fetch --force origin
git diff ..origin/main --exit-code
ret=$?
if [ $ret -eq 0 ]
then
    echo "no changes"
    exit 0
fi

echo "changed"
git checkout -f origin/main

echo "building"
./build.sh
ret=$?
if [ $ret -ne 0 ]
then
    echo "build failed"
    exit -1
fi
echo "successfully built"

echo "deploying"
./cd.sh
ret=$?
if [ $ret -eq 0 ]
then
    echo "deploy failed"
    exit -1
fi

echo "successfully deployed"