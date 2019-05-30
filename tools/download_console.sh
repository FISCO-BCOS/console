#!/bin/bash

version=$(curl -s https://raw.githubusercontent.com/FISCO-BCOS/console/master/release_note.txt | sed "s/^[vV]//")
package_name="console.tar.gz"
download_link=https://github.com/FISCO-BCOS/console/releases/download/v${version}/${package_name}
curl -LO ${download_link}

tar -zxf ${package_name} && cd console && chmod +x start.sh replace_solc_jar.sh
