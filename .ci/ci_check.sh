#!/bin/bash

set -e
LOG_INFO() {
    local content=${1}
    echo -e "\033[32m ${content}\033[0m"
}

download_tassl()
{
local OPENSSL_CMD=${HOME}/.fisco/tassl-1.1.1b
if [ -f "${OPENSSL_CMD}" ];then
    return
fi
local package_name="tassl-1.1.1b-linux-x86_64"
if [ "$(uname)" == "Darwin" ];then
    package_name="tassl-1.1.1b-macOS-x86_64"
fi
curl -LO "https://github.com/FISCO-BCOS/LargeFiles/raw/master/tools/${package_name}.tar.gz" && tar -zxvf "${package_name}.tar.gz" && mv "${package_name}" tassl-1.1.1b && mkdir -p ~/.fisco && mv tassl-1.1.1b ~/.fisco/
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
  local tag="${1}"
  if [ -z "${tag}" ]; then
    tag=$(curl -sS "https://gitee.com/api/v5/repos/FISCO-BCOS/FISCO-BCOS/tags" | grep -oe "\"name\":\"v[2-9]*\.[0-9]*\.[0-9]*\"" | cut -d \" -f 4 | sort -V | tail -n 1)
  fi
  LOG_INFO "--- current tag: $tag"
  curl -LO "https://github.com/FISCO-BCOS/FISCO-BCOS/releases/download/${tag}/build_chain.sh" && chmod u+x build_chain.sh
}

download_binary()
{
  local tag="${1}"
  LOG_INFO "--- current tag: $tag"
  local package_name="fisco-bcos-linux-x86_64.tar.gz"
  if [ "$(uname)" == "Darwin" ];then
      package_name="fisco-bcos-macOS-x86_64.tar.gz"
  fi
  curl -LO "https://github.com/FISCO-BCOS/FISCO-BCOS/releases/download/${tag}/${package_name}" && tar -zxvf "${package_name}"
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
  curl -LO https://github.com/FISCO-BCOS/console/releases/download/v3.0.0/get_account.sh
  curl -LO https://github.com/FISCO-BCOS/console/releases/download/v3.0.0/get_gm_account.sh
  bash build_chain.sh -l 127.0.0.1:4 ${@} -e ./fisco-bcos
  ./nodes/127.0.0.1/fisco-bcos -v
  ./nodes/127.0.0.1/start_all.sh
}

clean_node()
{
  bash nodes/127.0.0.1/stop_all.sh
  rm -rf nodes
  if [ "${1}" == "true" ]; then
    rm -rf ./fisco-bcos*
  fi
}

check_standard_node()
{
  build_node ${@:2}
  prepare_environment
  ## run integration test
  bash gradlew test --info
  bash gradlew integrationTest --info
  clean_node "${1}"
}

check_sm_node()
{
  build_node ${@:2} -s
  prepare_environment sm
  ## run integration test
  bash gradlew test --info
  bash gradlew integrationTest --info
  clean_node "${1}"
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

LOG_INFO "------ check java version --------"
java -version
#cp src/integration-test/resources/config-example.toml src/integration-test/resources/config.toml
download_tassl
LOG_INFO "------ download_binary: v3.3.0---------"
download_binary "v3.3.0"
download_build_chain "v3.3.0"
LOG_INFO "------ check_standard_node---------"
check_standard_node false
LOG_INFO "------ check_sm_node---------"
check_sm_node true
LOG_INFO "------ check_basic---------"
check_basic

LOG_INFO "------ download_binary: v3.2.0---------"
download_binary "v3.2.0"
download_build_chain "v3.2.0"
LOG_INFO "------ check_standard_node---------"
check_standard_node -s
rm -rf ./bin

LOG_INFO "------ download_binary: v3.1.0---------"
download_binary "v3.1.0"
download_build_chain "v3.1.0"
LOG_INFO "------ check_standard_node---------"
check_standard_node -s
rm -rf ./bin

LOG_INFO "------ download_binary: v3.0.0---------"
download_binary "v3.0.0"
download_build_chain "v3.0.0"
LOG_INFO "------ check_standard_node---------"
check_standard_node -s
rm -rf ./bin