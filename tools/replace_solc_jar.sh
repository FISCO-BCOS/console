#!/bin/bash
if [ $# == 0 ];then
  echo "Please provide the name of a solcJ jar"
  exit 0
fi

SHELL_FOLDER=$(cd $(dirname $0);pwd)

rm -rf ${SHELL_FOLDER}/lib/solcJ*.jar
cp ${1} ${SHELL_FOLDER}/lib/
