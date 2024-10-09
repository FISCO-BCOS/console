#!/bin/bash

set -e

rare_str_range_names=("CJKUnifiedIdeographs" "CJKCompatibilityIdeographs" "CJKCompatibilityIdeographsSupplement" "KangxiRadicals" "CJKRadicalsSupplement" "IdeographicDescriptionCharacters" "Bopomofo" "BopomofoExtended" "CJKStrokes" "CJKSymbolsandPunctuation" "CJKCompatibilityForms" "CJKCompatibility" "EnclosedCJKLettersandMonths" "CJKUnifiedIdeographsExtensionA" "CJKUnifiedIdeographsExtensionB" "CJKUnifiedIdeographsExtensionC" "CJKUnifiedIdeographsExtensionD" "CJKUnifiedIdeographsExtensionE" "CJKUnifiedIdeographsExtensionF")
rare_str_range_values=("19968,40959" "63744,64255" "194560,195103" "12032,12255" "11904,12031" "12272,12287" "12544,12591" "12704,12735" "12736,12783" "12288,12351" "65072,65103" "13056,13311" "12800,13055" "13312,19903" "131072,173791" "173824,177977" "177984,178205" "178208,183969" "183984,191456")

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

download_rare_string_jar() {
  LOG_INFO "----- Downloading get-rare-string-with-unicode.jar -------"
  curl -LO "https://github.com/FISCO-BCOS/LargeFiles/raw/master/binaries/jar/get-rare-string-with-unicode.jar"
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

getRangeValues() {
  local rangeValue=$1
  IFS=',' read -r startValue endValue <<<"$rangeValue"

  echo "$startValue $endValue"
}

getConcatenatedRareStringWithRange() {
  local startUnicode=${1}
  local endUnicode=${2}

  # concatenate strings with begin middle end
  local concatenatedString=$(java -cp './get-rare-string-with-unicode.jar' org.example.Main ${startUnicode})
  local midUnicode=$((($startUnicode + $endUnicode) / 2))
  for ((i = midUnicode; i <= midUnicode + 5; i++)); do
    local currentRareString=$(java -cp './get-rare-string-with-unicode.jar' org.example.Main ${i})
    concatenatedString+="$currentRareString"
  done
  local endRareString=$(java -cp './get-rare-string-with-unicode.jar' org.example.Main ${endUnicode})
  concatenatedString+="$endRareString"
  echo "$concatenatedString"
}

check_rare_string() {
  download_rare_string_jar
  bash gradlew assemble
  cp ./src/integration-test/resources/config.toml ./dist/conf/config.toml
  cp -r ./nodes/127.0.0.1/sdk/* ./dist/conf/
  export LC_ALL=en_US.UTF-8
  export LANG=en_US.UTF-8
  export LANGUAGE=en_US.UTF-8

  finalConcatenatedInputString=""
  for ((i = 0; i < ${#rare_str_range_names[@]}; i++)); do
    rangeName="${rare_str_range_names[$i]}"
    rangeValue="${rare_str_range_values[$i]}"

    read -r startValue endValue <<<$(getRangeValues "$rangeValue")
    concatenatedString=$(getConcatenatedRareStringWithRange $startValue $endValue)
    finalConcatenatedInputString+="$concatenatedString"
  done

  bash -x .ci/check_rare_string.sh ${finalConcatenatedInputString}
}

check_standard_node()
{
  build_node ${@:2}
  prepare_environment
  ## run integration test
  bash gradlew test --info
  bash gradlew integrationTest --info
  LOG_INFO "------ standard_node check_rare_string---------"
  check_rare_string
  clean_node "${1}"
}

check_sm_node()
{
  build_node ${@:2} -s
  prepare_environment sm
  ## run integration test
  bash gradlew test --info
  bash gradlew integrationTest --info
  LOG_INFO "------ standard_node check_rare_string---------"
  check_rare_string
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

LOG_INFO "------ download_binary: v3.11.0---------"
download_binary "v3.11.0"
download_build_chain "v3.11.0"
LOG_INFO "------ check_standard_node---------"
check_standard_node false
LOG_INFO "------ check_sm_node---------"
check_sm_node true
LOG_INFO "------ check_basic---------"
check_basic

LOG_INFO "------ download_binary: v3.7.3---------"
download_binary "v3.7.3"
download_build_chain "v3.7.3"
LOG_INFO "------ check_standard_node---------"
check_standard_node
rm -rf ./bin

LOG_INFO "------ download_binary: v3.2.6---------"
download_binary "v3.2.6"
download_build_chain "v3.2.6"
LOG_INFO "------ check_standard_node---------"
check_standard_node -s
rm -rf ./bin