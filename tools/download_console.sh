#!/bin/bash
version=$(curl -s https://api.github.com/repos/FISCO-BCOS/console/releases | grep "tag_name" | sort -u | tail -n 1 | cut -d \" -f 4 | sed "s/^[vV]//")
package_name="console.tar.gz"
echo "Downloading console ${version}"
download_link=https://github.com/FISCO-BCOS/console/releases/download/v${version}/${package_name}

if [ $(curl -IL -o /dev/null -s -w %{http_code}  https://www.fisco.com.cn/cdn/console/releases/download/v${version}/${package_name}) == 200 ];then
    curl -LO ${download_link} --speed-time 30 --speed-limit 1024 -m 90 || {
        echo -e "\033[32m Download speed is too low, try https://www.fisco.com.cn/cdn/console/releases/download/v${version}/${package_name} \033[0m"
        curl -LO https://www.fisco.com.cn/cdn/console/releases/download/v${version}/${package_name}
    }
else
    curl -LO ${download_link}
fi
tar -zxf ${package_name} && cd console && chmod +x *.sh
