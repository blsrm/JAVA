#!/bin/bash

# Variable declation
ARGS_MODE=$1

if [ "$ARGS_MODE" = "update" ]; then
#### --------- All your update steps to be updated inside this block

   echo "INSIDE UPDATE BLOCK !!!"
   `touch /tmp/test1`


fi


PASSWD_FILE=/etc/passwd
USERNAME=`grep "109" $PASSWD_FILE | awk -F: '{print $3,$1}' | grep "^109" | awk '{print $2}'`

#### This block code is useful to create  excel report based on KEY=VALUE pairs
echo "VALIDATION START"
echo "[REPORT] IP=localhost"
echo "[REPORT]USERNAME=$USERNAME"
echo "VALIDATION END"


