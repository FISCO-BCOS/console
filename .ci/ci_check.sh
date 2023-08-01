#!/bin/bash

set -e
default_tag=v3.1.1

rare_str_range_names=("CJKUnifiedIdeographs" "CJKCompatibilityIdeographs" "CJKCompatibilityIdeographsSupplement" "KangxiRadicals" "CJKRadicalsSupplement" "IdeographicDescriptionCharacters" "Bopomofo" "BopomofoExtended" "CJKStrokes" "CJKSymbolsandPunctuation" "CJKCompatibilityForms" "CJKCompatibility" "EnclosedCJKLettersandMonths" "CJKUnifiedIdeographsExtensionA" "CJKUnifiedIdeographsExtensionB" "CJKUnifiedIdeographsExtensionC" "CJKUnifiedIdeographsExtensionD" "CJKUnifiedIdeographsExtensionE" "CJKUnifiedIdeographsExtensionF")
rare_str_range_values=("19968,40959" "63744,64255" "194560,195103" "12032,12255" "11904,12031" "12272,12287" "12544,12591" "12704,12735" "12736,12783" "12288,12351" "65072,65103" "13056,13311" "12800,13055" "13312,19903" "131072,173791" "173824,177977" "177984,178205" "178208,183969" "183984,191456")

LOG_INFO() {
  local content=${1}
  echo -e "\033[32m ${content}\033[0m"
}

get_sed_cmd() {
  local sed_cmd="sed -i"
  if [ "$(uname)" == "Darwin" ]; then
    sed_cmd="sed -i .bkp"
  fi
  echo "$sed_cmd"
}

download_rare_string_jar() {
  LOG_INFO "----- Downloading get-rare-string-with-unicode.jar -------"
  curl -LO "https://github.com/FISCO-BCOS/LargeFiles/raw/master/binaries/jar/get-rare-string-with-unicode.jar"
}

download_build_chain() {
  tag=$(curl -sS "https://gitee.com/api/v5/repos/FISCO-BCOS/FISCO-BCOS/tags" | grep -oe "\"name\":\"v[2-9]*\.[0-9]*\.[0-9]*\"" | cut -d \" -f 4 | sort -V | tail -n 1)
  LOG_INFO "--- current tag: $tag"
  if [[ -z ${tag} ]]; then
    LOG_INFO "tag is empty, use default tag: ${default_tag}"
    tag="${default_tag}"
  fi
  curl -#LO "https://github.com/FISCO-BCOS/FISCO-BCOS/releases/download/${tag}/build_chain.sh" && chmod u+x build_chain.sh
}

prepare_environment() {
  ## prepare resources for integration test
  pwd
  ls -a
  local node_type="${1}"
  mkdir -p "./src/integration-test/resources/conf"
  cp -r nodes/127.0.0.1/sdk/* ./src/integration-test/resources/conf
  cp ./src/integration-test/resources/config-example.toml ./src/integration-test/resources/config.toml
  cp -r ./src/main/resources/contract ./contracts
  if [ "${node_type}" == "sm" ]; then
    sed_cmd=$(get_sed_cmd)
    $sed_cmd 's/useSMCrypto = "false"/useSMCrypto = "true"/g' ./src/integration-test/resources/config.toml
  fi
}

build_node() {
  local node_type="${1}"
  if [ "${node_type}" == "sm" ]; then
    bash -x build_chain.sh -l 127.0.0.1:4 -s
    sed_cmd=$(get_sed_cmd)
    $sed_cmd 's/sm_crypto_channel=false/sm_crypto_channel=true/g' nodes/127.0.0.1/node*/config.ini
  else
    bash -x build_chain.sh -l 127.0.0.1:4
  fi
  ./nodes/127.0.0.1/fisco-bcos -v
  ./nodes/127.0.0.1/start_all.sh
}

check_standard_node() {
  build_node
  prepare_environment
  # run integration test
  bash gradlew test --info
  bash gradlew integrationTest --info

  LOG_INFO "------ standard_node check_rare_string---------"
  check_rare_string
}

check_sm_node() {
  build_node sm
  prepare_environment sm
  # run integration test
  bash gradlew test --info
  bash gradlew integrationTest --info

  LOG_INFO "------ sm_node check_rare_string---------"
  check_rare_string
}

check_basic() {
  # check code format
  bash gradlew verifyGoogleJavaFormat
  # build
  bash gradlew build
  # test
  bash gradlew test
  bash gradlew integrationTest --info
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

#cp src/integration-test/resources/config-example.toml src/integration-test/resources/config.toml
#LOG_INFO "------ download_build_chain---------"
download_build_chain
#LOG_INFO "------ check_standard_node---------"
#check_standard_node
LOG_INFO "------ check_sm_node---------"
check_sm_node
LOG_INFO "------ check_basic---------"
check_basic
