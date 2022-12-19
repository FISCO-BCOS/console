#!/bin/bash

set -e
default_tag=v3.1.1
LOG_INFO() {
    local content=${1}
    echo -e "\033[32m ${content}\033[0m"
}

get_sed_cmd()
{
  local sed_cmd="sed -i"
  if [ "$(uname)" == "Darwin" ];then
        sed_cmd="sed -i .bkp"
  fi
  echo "$sed_cmd"
}

download_build_chain()
{
  tag=$(curl -sS "https://gitee.com/api/v5/repos/FISCO-BCOS/FISCO-BCOS/tags" | grep -oe "\"name\":\"v[2-9]*\.[0-9]*\.[0-9]*\"" | cut -d \" -f 4 | sort -V | tail -n 1)
  LOG_INFO "--- current tag: $tag"
  if [[ -z ${tag} ]]; then
    LOG_INFO "tag is empty, use default tag: ${default_tag}"
    tag="${default_tag}"
  fi
  curl -#LO "https://osp-1257653870.cos.ap-guangzhou.myqcloud.com/FISCO-BCOS/FISCO-BCOS/releases/${tag}/build_chain.sh" && chmod u+x build_chain.sh
}

prepare_environment()
{
  ## prepare resources for integration test
  pwd
  ls -a
  local node_type="${1}"
  mkdir -p "./src/integration-test/resources/conf"
  cp -r nodes/127.0.0.1/sdk/* ./src/integration-test/resources/conf
  cp ./src/integration-test/resources/config-example.toml ./src/integration-test/resources/config.toml
  cp -r ./src/main/resources/contract ./contracts
  if [ "${node_type}" == "sm" ];then
     sed_cmd=$(get_sed_cmd)
     $sed_cmd 's/useSMCrypto = "false"/useSMCrypto = "true"/g' ./src/integration-test/resources/config.toml
  fi
}

build_node()
{
  local node_type="${1}"
  if [ "${node_type}" == "sm" ];then
      bash -x build_chain.sh -l 127.0.0.1:4 -s -A
      sed_cmd=$(get_sed_cmd)
      $sed_cmd 's/sm_crypto_channel=false/sm_crypto_channel=true/g' nodes/127.0.0.1/node*/config.ini
  else
      bash -x build_chain.sh -l 127.0.0.1:4 -A
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

check_sm_node()
{
  build_node sm
  prepare_environment sm
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

#cp src/integration-test/resources/config-example.toml src/integration-test/resources/config.toml
#LOG_INFO "------ download_build_chain---------"
download_build_chain
#LOG_INFO "------ check_standard_node---------"
#check_standard_node
LOG_INFO "------ check_sm_node---------"
check_sm_node
LOG_INFO "------ check_basic---------"
check_basic
