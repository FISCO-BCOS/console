![](https://github.com/FISCO-BCOS/FISCO-BCOS/raw/master/docs/images/FISCO_BCOS_Logo.svg?sanitize=true)

English / [中文](../README.md)

# console
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat-square)](http://makeapullrequest.com)
[![Build Status](https://travis-ci.org/FISCO-BCOS/console.svg?branch=master)](https://travis-ci.org/FISCO-BCOS/console)
[![GitHub All Releases](https://img.shields.io/github/downloads/FISCO-BCOS/console/total.svg)](https://github.com/FISCO-BCOS/console)
---

**The console is an important interactive client tool for FISCO BCOS. The console has wealth of commands, including querying blockchain status, managing blockchain nodes, deploying and invoking contracts, etc.**

## Version and Compatibility Notes

**v3.x version console only works with FISCO BCOS v3.x, not compatible with FISCO BCOS v2.x**.

### **v2.x**

- [Deployment documentation](https://fisco-bcos-documentation.readthedocs.io/zh_CN/latest/docs/installation.html)

- **Code**: [GitHub](https://github.com/FISCO-BCOS/console/tree/master-2.0), [Gitee](https://gitee.com/FISCO-BCOS/console/tree/master-2.0/)

- **FISCO BCOS v2.x**: [GitHub](https://github.com/FISCO-BCOS/FISCO-BCOS/tree/master-2.0), [Gitee](https://gitee.com/FISCO-BCOS/FISCO-BCOS/tree/master-2.0/)

### **v3.x**

- [Deployment documentation](https://fisco-bcos-doc.readthedocs.io/zh_CN/latest/docs/quick_start/air_installation.html)

- **Code**: [GitHub](https://github.com/FISCO-BCOS/console/tree/master), [Gitee](https://gitee.com/FISCO-BCOS/console/tree/master)

- **FISCO BCOS v2.x**: [GitHub](https://github.com/FISCO-BCOS/FISCO-BCOS/tree/master), [Gitee](https://gitee.com/FISCO-BCOS/FISCO-BCOS/tree/master)


## Features

- Provide a lot of query commands for blockchain.
- Provide the easiest way to deploy and invoke contracts. 
- Provide some commands which can manage blockchain node.
- Provide a contract compilation tool that allows users to easily and quickly compile Solidity contract files into Java contract files.
- Provide an easy way to transfer compiled Liquid contract material, such as WASM and ABI, into Java contract files.

## Usage

- You can download a tar file and decompress it to enjoy console. See [console manual](https://fisco-bcos-doc.readthedocs.io/zh_CN/latest/docs/develop/console/index.html) for more details.

## Source Installation
```
$ git clone https://github.com/FISCO-BCOS/console.git
$ cd console
$ bash gradlew build
```

If you install successfully, it produces the `dist` directory.

## Configuration
Please see the [documentation](https://fisco-bcos-doc.readthedocs.io/zh_CN/latest/docs/develop/console/console_config.html) about configuration for the console. Have fun :)

## Code Contribution
- Star our GitHub.
- Pull requests. See [CONTRIBUTING](CONTRIBUTING.md).
- [Ask questions](https://github.com/FISCO-BCOS/console/issues).


## Join Our Community

The FISCO BCOS community is one of the most active open-source blockchain communities in China. It provides long-term technical support for both institutional and individual developers and users of FISCO BCOS. Thousands of technical enthusiasts from numerous industry sectors have joined this community, studying and using FISCO BCOS platform. If you are also interested, you are most welcome to join us for more support and fun.

![](https://media.githubusercontent.com/media/FISCO-BCOS/LargeFiles/master/images/QR_image_en.png)

## License
![license](https://img.shields.io/badge/license-Apache%20v2-blue.svg)


All contributions are made under the [Apache License 2.0](http://www.apache.org/licenses/). See [LICENSE](../LICENSE).
