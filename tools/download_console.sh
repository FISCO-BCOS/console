#!/bin/bash
package_name="console.tar.gz"
default_version="1.1.1"
solc_suffix=""
supported_solc_versions=(0.4 0.5 0.6)

help() {
    echo "
Usage:
    -v <solc version>      Download latest console with specific solc version, default is 0.4, 0.5 and 0.6 are supported
    -h Help
e.g
    $0 -v 0.6
"

exit 0
}

parse_params(){
while getopts "v:h" option;do
    case $option in
    v) solc_suffix="${OPTARG//[vV]/}"
        if ! echo "${supported_solc_versions[*]}" | grep -i "${solc_suffix}" &>/dev/null; then
            LOG_WARN "${solc_suffix} is not supported. Please set one of ${supported_solc_versions[*]}"
            exit 1;
        fi
        package_name="console-${solc_suffix}.tar.gz"
        if [ "${solc_suffix}" == "0.4" ]; then package_name="console.tar.gz";fi
    ;;
    h) help;;
    *) help;;
    esac
done
}

download_console(){
    version=$(curl -s https://api.github.com/repos/FISCO-BCOS/console/releases | grep "tag_name" | sort -V | tail -n 1 | cut -d \" -f 4 | sed "s/^[vV]//")
    if [ -z "${version}" ];then
        echo "Failed to get latest version number via github api, download default version: ${default_version}"
        version="${default_version}"
    fi
    download_link=https://github.com/FISCO-BCOS/console/releases/download/v${version}/${package_name}
    cos_download_link=https://osp-1257653870.cos.ap-guangzhou.myqcloud.com/FISCO-BCOS/console/releases/v${version}/${package_name}
    echo "Downloading console ${version} from ${download_link}"

    if [ $(curl -IL -o /dev/null -s -w %{http_code} "${cos_download_link}") == 200 ];then
        curl -LO ${download_link} --speed-time 30 --speed-limit 102400 -m 150 || {
            echo -e "\033[32m Download speed is too low, try ${cos_download_link} \033[0m"
            curl -#LO "${cos_download_link}"
        }
    else
        curl -#LO ${download_link}
    fi
    tar -zxf ${package_name} && cd console && chmod +x *.sh
}

parse_params "$@"
download_console
