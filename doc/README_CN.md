![](https://github.com/FISCO-BCOS/FISCO-BCOS/raw/master/docs/images/FISCO_BCOS_Logo.svg?sanitize=true)

[English](../README.md) / 中文

# 控制台

[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat-square)](http://makeapullrequest.com)
[![Build Status](https://travis-ci.org/FISCO-BCOS/console.svg?branch=master)](https://travis-ci.org/FISCO-BCOS/console)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/a2a6c2eb499e42739d066ff775d1b288)](https://www.codacy.com/app/fisco/console?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=FISCO-BCOS/console&amp;utm_campaign=Badge_Grade)
[![GitHub All Releases](https://img.shields.io/github/downloads/FISCO-BCOS/console/total.svg)](https://github.com/FISCO-BCOS/console)
---

控制台是[FISCO BCOS 2.0](https://fisco-bcos-documentation.readthedocs.io/zh_CN/latest/)的重要交互式客户端工具。控制台拥有丰富的命令，包括查询区块链状态、管理区块链节点、部署并调用合约等。

## 关键特性

 - 提供大量的区块链状态查询命令。
 - 提供简单易用的部署和调用合约命令。
 - 提供一些可以管理区块链节点的命令。
 - 提供一个合约编译工具，用户可以方便快捷的将Solidity合约文件编译为Java合约文件。

## 使用
- 可以直接下载控制台压缩包，然后解压控制台压缩包使用控制台。具体参考 [控制台手册](https://fisco-bcos-documentation.readthedocs.io/zh_CN/latest/docs/manual/console.html)。

## 源码安装
```
$ git clone https://github.com/FISCO-BCOS/console.git
$ cd console
$ ./gradlew build
```
如果安装成功，将在当前目录生成一个`dist`目录。

## 配置
控制台具体配置参考[这里](https://fisco-bcos-documentation.readthedocs.io/zh_CN/latest/docs/manual/console.html#id11)。

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

Web3SDK的开源协议为[Apache License 2.0](http://www.apache.org/licenses/). 详情参考[LICENSE](../LICENSE)。