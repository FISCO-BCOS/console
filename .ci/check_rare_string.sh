#!/bin/bash

concatenatedString=$1

function LOG_ERROR()
{
    local content=${1}
    echo -e "\033[31m"${content}"\033[0m"
}

function LOG_INFO()
{
    local content=${1}
    echo -e "\033[32m"${content}"\033[0m"
}

get_md5sum_cmd() {
  local md5sum_cmd="md5sum"
  if [ "$(uname)" == "Darwin" ]; then
    md5sum_cmd="md5"
  fi
  echo "$md5sum_cmd"
}

function checkConcatenatedRareString() {
      local contract_address=${1}
      md5sum_cmd=$(get_md5sum_cmd)

      md5_concatenatedString=$(echo -n "$concatenatedString" | $md5sum_cmd | awk '{print $1}')

      local set_output=$(./dist/console.sh call HelloWorld "${contract_address}" set "${concatenatedString}")
      eventLogsFromSet=$(echo "set_output" | grep -o 'Event: {}' | sed 's/Event: {\(.*\)}/\1/')
      if [ ! -z "$eventLogsFromSet" ]; then
        echo "eventLogsFromSet=${eventLogsFromSet}"
        md5_eventLogsFromSet=$(echo -n "$eventLogsFromSet" | $md5sum_cmd | awk '{print $1}')
          if [ "$md5_concatenatedString" != "$md5_eventLogsFromSet" ]; then
            LOG_ERROR "error: check failed, the md5 values of rareString and eventLogsFromSet are not equal, fail concatenatedString: ${concatenatedString}"
            exit 1
          fi
      fi

      # compare rare string and stringFromGet
      get_output=$(./dist/console.sh call HelloWorld "${contract_address}" get)
      stringFromGet=$(echo "$get_output" | grep "Return values" | sed 's/Return values:\(.*\)/\1/' | tr -d '()')
      md5_stringFromGet=$(echo -n "$stringFromGet" | $md5sum_cmd | awk '{print $1}')
      if [ "$md5_concatenatedString" != "$md5_stringFromGet" ]; then
        LOG_ERROR "error: check failed, the md5 values of rareString and stringFromGet are not equal, fail concatenatedString: ${concatenatedString}"
        exit 1
      else
        LOG_INFO "check success, concatenatedString: ${concatenatedString}"
      fi
}

main() {
    LOG_INFO "check rare string start, concatenatedString: ${concatenatedString}"

    # deploy HelloWorld contract
    console_output=$(./dist/console.sh deploy HelloWorld)
    contract_address=$(echo "$console_output" | grep -oE 'contract address: 0x[0-9a-fA-F]+' | sed 's/contract address: //')
    if [ -z "$contract_address" ]; then
        LOG_ERROR "deploy HelloWorld contract failed, contract_address: ${contract_address}"
        exit 1
    fi

    checkConcatenatedRareString $contract_address
    LOG_INFO "check rare string finished!"
}

main "$@"
