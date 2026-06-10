#!/bin/bash
package_name="console.tar.gz"
solcj_name=""
solcj_default_version="solcJ-0.8.11.1.jar"
only_solc_flag=""
default_version="3.8.0"
download_version="${default_version}"
solc_download_version="3.0.0"
specify_console=0
solc_suffix=""
supported_solc_versions=(0.4 0.5 0.6 0.8)
only_solc_versions=(0.4 0.5 0.6)

LOG_WARN()
{
    local content=${1}
    echo -e "\033[31m[WARN] ${content}\033[0m"
}

LOG_INFO()
{
    local content=${1}
    echo -e "\033[32m[INFO] ${content}\033[0m"
}

help() {
    echo "
Usage:
    -c <console version>   Specify the downloaded console version, download the latest version of the console by default 
    -v <solc version>      Download the console with specific solc version, default is 0.8, 0.4, 0.5 and 0.6 are supported
    -s <only solc>         Only download solcJ jar with specific solc version, 0.6, 0.4 and 0.5 are supported, 0.6 is recommended
    -h Help
e.g
    $0 -v 0.6
"

exit 0
}

parse_params(){
while getopts "v:c:hs:" option;do
    case $option in
    v) solc_suffix="${OPTARG//[vV]/}"
        if ! echo "${supported_solc_versions[*]}" | grep -i "${solc_suffix}" &>/dev/null; then
            LOG_WARN "${solc_suffix} is not supported. Please set one of ${supported_solc_versions[*]}"
            exit 1;
        fi
        solcj_name="solcJ-${solc_suffix}.tar.gz"
        if [ "${solc_suffix}" == "0.8" ]; then solcj_name="";fi
    ;;
    c) specify_console=1
        download_version="${OPTARG//[vV]/}"
        ;;
    s)  solc_suffix="${OPTARG//[vV]/}"
        if ! echo "${only_solc_versions[*]}" | grep -i "${solc_suffix}" &>/dev/null; then
            LOG_WARN "Download solcJ ${solc_suffix} is not supported. Please set one of ${only_solc_versions[*]}"
            exit 1;
        fi
        only_solc_flag="true"
        solcj_name="solcJ-${solc_suffix}.tar.gz"
        if [ "${solc_suffix}" == "0.8" ]; then solcj_name="";fi
    ;;
    h) help;;
    *) help;;
    esac
done
}

# check params
check_params()
{
    local version=${download_version}
    local major_version=$(echo ${version} | awk -F'.' '{print $1}')
    local middle_version=$(echo ${version} | awk -F'.' '{print $2}')
    local minor_version=$(echo ${version} | awk -F'.' '{print $3}')
    if [ -z "${major_version}" ] || [ -z "${middle_version}" ] || [ -z "${minor_version}" ];then
        LOG_WARN "Illegal version \"${version}\", please specify a legal version number, latest version is ${default_version}"
        exit 1;
    fi
}

download_console(){
    check_params
    git_download_link=https://github.com/FISCO-BCOS/console/releases/download/v${download_version}/${package_name}
    download_link=https://github.com/FISCO-BCOS/console/releases/v${download_version}/${package_name}

    if [ $(curl -IL -o /dev/null -s -w %{http_code} "${download_link}") == 200 ];then
        LOG_INFO "Downloading console ${download_version} from ${download_link}"
        curl -#LO "${download_link}"
    else
        LOG_INFO "Downloading console ${download_version} from ${git_download_link}"
        curl -#LO ${git_download_link}
    fi
    if [ $? -eq 0 ];then
        LOG_INFO "Download console successfully"
    else
        LOG_WARN "Download console failed, please switch to better network and try again!"
    fi
    tar -zxf ${package_name} && chmod +x console*/*.sh
    if [ $? -eq 0 ];then
        LOG_INFO "unzip console successfully"
    else
        LOG_WARN "unzip console failed, please try again!"
    fi 
}

download_solcJ(){
    check_params
    git_download_link=https://github.com/FISCO-BCOS/console/releases/download/v${solc_download_version}/${solcj_name}
    download_link=https://github.com/FISCO-BCOS/console/releases/v${solc_download_version}/${solcj_name}

    if [ $(curl -IL -o /dev/null -s -w %{http_code} "${download_link}") == 200 ];then
        LOG_INFO "Downloading solcJ ${solcj_name} from ${download_link}"
        curl -#LO "${download_link}"
    else
        LOG_INFO "Downloading solcJ ${solcj_name} from ${git_download_link}"
        curl -#LO ${git_download_link}
    fi
    if [ $? -eq 0 ];then
        LOG_INFO "Download solcJ successfully"
    else
        LOG_WARN "Download solcJ failed, please switch to better network and try again!"
    fi
    if [ -z ${only_solc_flag} ];then
        LOG_INFO "Switching SolcJ from ${solcj_default_version} to solcJ-${solc_suffix}.jar"
        tar -zxf ${solcj_name} && rm ./console/lib/${solcj_default_version} && mv solcJ-*.jar ./console/lib/
        if [ $? -eq 0 ];then
            LOG_INFO "Switching solcJ successfully"
        else
            LOG_WARN "Switching solcJ failed, please try again!"
        fi
    else
        tar -zxf ${solcj_name}
        if [ $? -eq 0 ];then
            LOG_INFO "Unzip solcJ successfully"
        else
            LOG_WARN "Unzip solcJ failed, please try again!"
        fi
    fi
}

parse_params "$@"
if [ -z "${only_solc_flag}" ];then
  download_console
fi
if [ -n "${solcj_name}" ];then
  download_solcJ
fi
