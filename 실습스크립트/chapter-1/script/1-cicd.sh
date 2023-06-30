#!/bin/bash
rm -f ci-flow.log
git fetch --force origin >> ci-flow.log
git diff ..origin/main --exit-code > diff.txt
ret=$?
if [ $ret -eq 0 ]
then
    echo "-------------------------------------------------"
    echo "no changes"
    exit 0
fi

echo ""
echo "-------------------------------------------------"
echo "changed"
git checkout -f origin/main >> ci-flow.log

echo ""
echo "-------------------------------------------------"
echo "building"
./2-build.sh
ret=$?
if [ $ret -ne 0 ]
then
    echo ""
    echo "-------------------------------------------------"
    echo "build failed"
    exit -1
fi
echo ""
echo "-------------------------------------------------"
echo "successfully built"

echo ""
echo "-------------------------------------------------"
echo "deploying"
./3-deploy.sh
ret=$?
if [ $ret -ne 0 ]
then
    echo ""
    echo "-------------------------------------------------"
    echo "deploy failed"
    exit -1
fi
echo ""
echo "-------------------------------------------------"
echo "successfully deployed"
