#!/usr/bin/env bash

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
    LOG_INFO "# Compile Solidity Tool"
    LOG_INFO "./sol2java.sh [packageName] [solidityFilePath] [javaCodeOutputDir]"
    LOG_INFO " \t packageName:"
    LOG_INFO " \t\t the package name of the generated Java class file"
    LOG_INFO " \t solidityFilePath:"
    LOG_INFO " \t\t (optional) the solidity file path or the directory where solidity files located, default: contracts/solidity"
    LOG_INFO " \t javaCodeOutputDir:"
    LOG_INFO " \t\t (optional) the directory where the generated Java files located, default: contracts/sdk/java"
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
if [ $# == 0 ] || [ "${1}" == "-h" ] || [ "${1}" == "--help" ] || [ "${1}" == "help" ];then
    Usage
    exit 0
else
     check_java
     java -cp "apps/*:lib/*:conf/" console.common.ConsoleUtils $@
fi


