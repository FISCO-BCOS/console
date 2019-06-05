#!/bin/bash
version=$(curl -s https://api.github.com/repos/FISCO-BCOS/console/releases | grep "tag_name" | sort -u | tail -n 1 | cut -d \" -f 4 | sed "s/^[vV]//")
echo "Downloading console version" ${version}
package_name="console.tar.gz"
download_link=https://github.com/FISCO-BCOS/console/releases/download/v${version}/${package_name}
curl -LO ${download_link}

tar -zxf ${package_name} && cd console && chmod +x *.sh
