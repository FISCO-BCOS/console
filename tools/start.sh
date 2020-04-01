#!/usr/bin/env bash

dirpath="$(cd "$(dirname "$0")" && pwd)"
cd $dirpath

LANG=zh_CN.UTF-8
##############################################################################
##
##  Console start up script for UN*X
##
##############################################################################

# @function: output log with red color (error log)
# @param: content: error message

function LOG_ERROR()
{
    local content=${1}
    echo -e "\033[31m"${content}"\033[0m"
}

# @function: output information log
# @param: content: information message
function LOG_INFO()
{
    local content=${1}
    echo -e "\033[32m"${content}"\033[0m"
}

function Usage() {
    LOG_INFO "Usage:start the console"
    LOG_INFO "./start.sh"
    LOG_INFO "./start.sh groupID"
    LOG_INFO "./start.sh groupID -pem pemName"
    LOG_INFO "./start.sh groupID -p12 p12Name"
    LOG_INFO "print console version:"
    LOG_INFO "./start.sh --version or -v"
}

function check_java(){
   version=$(java -version 2>&1 |grep version |awk '{print $3}')
   len=${#version}-2
   version=${version:1:len}

   IFS='.' arr=($version)
   IFS=' '
   if [ -z ${arr[0]} ];then
      LOG_ERROR "At least Java8 is required."
      exit 1
   fi
   if [ ${arr[0]} -eq 1 ];then
      if [ ${arr[1]} -lt 8 ];then
           LOG_ERROR "At least Java8 is required."
           exit 1
      fi
   elif [ ${arr[0]} -gt 8 ];then
          :
   else
       LOG_ERROR "At least Java8 is required."
       exit 1
   fi
}
if [ "${1}" == "-h" ] || [ "${1}" == "--help" ] || [ "${1}" == "help" ];then
    Usage
elif [ "${1}" == "-v" ] || [ "${1}" == "--version" ];then
    java -cp "apps/*:conf/:lib/*:classes/" console.common.ConsoleVersion
else
   check_java
   java -Djdk.tls.namedGroups="secp256k1" -cp "apps/*:conf/:lib/*:classes/:accounts/" console.ConsoleClient $@
fi
