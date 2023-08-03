#!/bin/bash

set -e
LOG_INFO() {
    local content=${1}
    echo -e "\033[32m ${content}\033[0m"
}

download_build_chain()
{
  tag=$(curl -sS "https://gitee.com/api/v5/repos/FISCO-BCOS/FISCO-BCOS/tags" | grep -oe "\"name\":\"v[2-9]*\.[0-9]*\.[0-9]*\"" | cut -d \" -f 4 | sort -V | tail -n 1)
  LOG_INFO "--- current tag: $tag"
  curl -LO "https://github.com/FISCO-BCOS/FISCO-BCOS/releases/download/v2.7.2/build_chain.sh" && chmod u+x build_chain.sh
}

prepare_environment()
{
  ## prepare resources for integration test
  mkdir -p src/test/resources/
  mkdir -p conf
  cp -r nodes/127.0.0.1/sdk/* conf
}

build_node()
{
  local node_type="${1}"
  if [ "${node_type}" == "sm" ];then
      ./build_chain.sh -l 127.0.0.1:4 -g
      sed_cmd=$(get_sed_cmd)
      $sed_cmd 's/sm_crypto_channel=false/sm_crypto_channel=true/g' nodes/127.0.0.1/node*/config.ini
  else
      ./build_chain.sh -l 127.0.0.1:4
  fi
  ./nodes/127.0.0.1/fisco-bcos -v
  ./nodes/127.0.0.1/start_all.sh
}

check_standard_node()
{
  build_node
  prepare_environment
  ## run integration test
  bash gradlew test --info
  bash gradlew integrationTest --info
}

check_basic()
{
# check code format
bash gradlew verifyGoogleJavaFormat
# build
bash gradlew build
# test
bash gradlew test
bash gradlew integrationTest --info
}

cp src/integration-test/resources/config-example.toml src/integration-test/resources/config.toml
LOG_INFO "------ check java version -------"
java -version
LOG_INFO "------ check openssl version -------"
openssl version
LOG_INFO "------ download_build_chain---------"
download_build_chain
LOG_INFO "------ check_standard_node---------"
check_standard_node
LOG_INFO "------ check_basic---------"
check_basic
