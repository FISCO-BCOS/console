![](https://github.com/FISCO-BCOS/FISCO-BCOS/raw/master/docs/images/FISCO_BCOS_Logo.svg?sanitize=true)

中文 / [English](doc/README_EN.md)

# 控制台

[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat-square)](http://makeapullrequest.com)
[![Build Status](https://travis-ci.org/FISCO-BCOS/console.svg?branch=master)](https://travis-ci.org/FISCO-BCOS/console)
[![GitHub All Releases](https://img.shields.io/github/downloads/FISCO-BCOS/console/total.svg)](https://github.com/FISCO-BCOS/console)
---

控制台是FISCO BCOS的重要交互式客户端工具。控制台拥有丰富的命令，包括查询区块链状态、管理区块链节点、部署并调用合约等。

## 版本及兼容性说明

**v3.x版本控制台仅适用于FISCO BCOS v3.x，不兼容FISCO BCOS v2.x**。

### **v2.x控制台**

- [部署文档](https://fisco-bcos-documentation.readthedocs.io/zh_CN/latest/docs/installation.html)

- **代码**: [GitHub](https://github.com/FISCO-BCOS/console/tree/master-2.0), [Gitee](https://gitee.com/FISCO-BCOS/console/tree/master-2.0/)

- **FISCO BCOS v2.x**: [GitHub](https://github.com/FISCO-BCOS/FISCO-BCOS/tree/master-2.0), [Gitee](https://gitee.com/FISCO-BCOS/FISCO-BCOS/tree/master-2.0/)

### **v3.x控制台**

- [部署文档](https://fisco-bcos-doc.readthedocs.io/zh_CN/latest/docs/quick_start/air_installation.html)

- **代码**: [GitHub](https://github.com/FISCO-BCOS/console/tree/master), [Gitee](https://gitee.com/FISCO-BCOS/console/tree/master)

- **FISCO BCOS v3.x**: [GitHub](https://github.com/FISCO-BCOS/FISCO-BCOS/tree/master), [Gitee](https://gitee.com/FISCO-BCOS/FISCO-BCOS/tree/master)


## 关键特性

 - 提供大量的区块链状态查询命令。
 - 提供简单易用的部署和调用合约命令。
 - 提供一些可以管理区块链节点的命令。
 - 提供一个合约编译工具，用户可以方便快捷的将Solidity合约文件编译为Java合约文件。
 - 提供一个合约转换工具，用户可将编译好的Liquid合约的物料包，例如WASM文件和ABI文件，转换成Java合约文件。

## 使用
- 可以直接下载控制台压缩包，然后解压控制台压缩包使用控制台。具体参考 [控制台手册](https://fisco-bcos-doc.readthedocs.io/zh_CN/latest/docs/develop/console/index.html)。

## 源码安装
```
$ git clone https://github.com/FISCO-BCOS/console.git
$ cd console
$ bash gradlew build
```
如果安装成功，将在当前目录生成一个`dist`目录。

## 配置
控制台具体配置参考[这里](https://fisco-bcos-doc.readthedocs.io/zh_CN/latest/docs/develop/console/console_config.html)。

## 贡献代码
欢迎参与FISCO BCOS的社区建设：
- 点亮我们的小星星(点击项目左上方Star按钮)。
- 提交代码(Pull requests)，参考我们的[代码贡献流程](CONTRIBUTING_CN.md)。
- [提问和提交BUG](https://github.com/FISCO-BCOS/console/issues)。

## 加入我们的社区

FISCO BCOS开源社区是国内活跃的开源社区，社区长期为机构和个人开发者提供各类支持与帮助。已有来自各行业的数千名技术爱好者在研究和使用FISCO BCOS。如您对FISCO BCOS开源技术及应用感兴趣，欢迎加入社区获得更多支持与帮助。


![](https://media.githubusercontent.com/media/FISCO-BCOS/LargeFiles/master/images/QR_image.png)


## License

![license](https://img.shields.io/badge/license-Apache%20v2-blue.svg)

FISCO-BCOS/Console的开源协议为[Apache License 2.0](http://www.apache.org/licenses/). 详情参考[LICENSE](LICENSE)。