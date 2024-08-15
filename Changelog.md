## v3.8.0

(2024-08-20)

请阅读控制台 v3.x+文档：

- [中文用户手册](https://fisco-bcos-doc.readthedocs.io/zh-cn/latest/docs/operation_and_maintenance/console/index.html)

### 新增

- 在编译合约时支持解析所有依赖合约，并合并成同一份合约文件。详情见：[#853](https://github.com/FISCO-BCOS/console/pull/853)
- 新增在部署合约时指定编译器版本的功能，支持 `deploy` 命令后使用 `-v` 或者 `--sol-version` 参数指定编译器版本编译合约。详情见：[#846](https://github.com/FISCO-BCOS/console/pull/846)
- 新增 `listSystemConfigs` 命令，可以查看链上所有系统配置项。详情见：[#848](https://github.com/FISCO-BCOS/console/pull/848)
- 新增 `getLatestBlock` 命令，可以查看链上最新的区块信息。详情见：[#848](https://github.com/FISCO-BCOS/console/pull/848)
- 新增Solidity `0.8.26` 版本的支持，提供示例合约。详情见：[#851](https://github.com/FISCO-BCOS/console/pull/851)

### 更新

- 更新 `code-generator` 到 `1.6.0` 版本，以支持根据Solidity合约中devdoc和userdoc生成Java接口注释，解决合约接口返回struct类型时生成Java接口文件的问题。

## v3.7.0

(2024-03-25)

请阅读控制台 v3.x+文档：

- [中文用户手册](https://fisco-bcos-doc.readthedocs.io/zh-cn/latest/docs/operation_and_maintenance/console/index.html)

### 新增

- 新增`v2`版本的交易支持，控制台启动时增加v2选项，可让控制台只发新版本的交易。详情见：https://github.com/FISCO-BCOS/console/pull/835
- 新增用户使用控制台的脚本`contract2java.sh`从合约生成Java文件时，增加 `-t` 参数选项 `2`, 可以生成带有 `FunctionWrapper`返回的接口，`FunctionWrapper`使用可见：https://github.com/FISCO-BCOS/java-sdk/releases/tag/v3.7.0 , https://github.com/FISCO-BCOS/java-sdk/pull/899

### 更新

- `org.fisco-bcos.java-sdk:fisco-bcos-java-sdk` 更新到3.7.0版本
- `org.fisco-bcos.code-generator:bcos-code-generator` 更新到1.5.0版本

### 修复

- 修复feature list、bugfix list没有展示全的问题。

### 兼容性说明

- 支持[FISCO BCOS 3.0.0版本](https://github.com/FISCO-BCOS/FISCO-BCOS/releases/tag/v3.0.0) 以上的区块链节点。
- 控制台连接FISCO BCOS 3.1.0版本后，才支持BFS list分页查询、link新接口，连接3.1.0版本前的节点将会用旧版本接口。
- 控制台连接FISCO BCOS 3.2.0版本后，才支持CRUD的LIKE语句和数字序建表，连接3.2.0版本前的节点将会用旧版本接口。
- 控制台连接FISCO BCOS 3.3.0版本后，才支持使用ShardService的接口。
- 控制台连接FISCO BCOS 3.4.0版本后，才支持使用call with sign接口。
- 控制台连接FISCO BCOS 3.6.0版本后，才支持使用BalanceService的接口。
- 控制台连接FISCO BCOS 3.6.0版本后，才支持使用v1版本的交易接口。
- 控制台连接FISCO BCOS 3.7.0版本后，才支持使用v2版本的交易接口。

---

## v3.6.0
(2024-02-08)

请阅读控制台 v3.x+文档：

- [中文用户手册](https://fisco-bcos-doc.readthedocs.io/zh-cn/latest/docs/operation_and_maintenance/console/index.html)

### 新增

- 新增`v1`版本的交易支持，控制台启动时增加v1选项，可让控制台只发新版本的交易。详情见：https://github.com/FISCO-BCOS/console/pull/809
- 新增用户使用控制台的脚本`contract2java.sh`从合约生成Java文件时，增加 `-t` 参数，可以指定生成交易版本号。详情见：https://github.com/FISCO-BCOS/console/pull/817
- 新增用户使用控制台的脚本`contract2java.sh`从合约生成Java文件时，支持合约带有 `payable` 的函数接口带上value参数。详情见：https://github.com/FISCO-BCOS/console/pull/812
- 新增 `transfer` 命令，用户可以发起原生balance转账。详情见：https://github.com/FISCO-BCOS/console/pull/805
- 新增Balance操作命令，管理员可以通过命令操作某个用户的balance。详情见：https://github.com/FISCO-BCOS/console/pull/808

### 更新

- `org.fisco-bcos.java-sdk:fisco-bcos-java-sdk` 更新到3.6.0版本
- `org.fisco-bcos.code-generator:bcos-code-generator` 更新到1.3.0版本

### 修复

- 修复在加载账户时当账户文件不存在时，控制台不退出的问题。详情见：https://github.com/FISCO-BCOS/console/pull/813

### 兼容性说明

- 支持[FISCO BCOS 3.0.0版本](https://github.com/FISCO-BCOS/FISCO-BCOS/releases/tag/v3.0.0) 以上的区块链节点。
- 控制台连接FISCO BCOS 3.1.0版本后，才支持BFS list分页查询、link新接口，连接3.1.0版本前的节点将会用旧版本接口。
- 控制台连接FISCO BCOS 3.2.0版本后，才支持CRUD的LIKE语句和数字序建表，连接3.2.0版本前的节点将会用旧版本接口。
- 控制台连接FISCO BCOS 3.3.0版本后，才支持使用ShardService的接口。
- 控制台连接FISCO BCOS 3.4.0版本后，才支持使用call with sign接口。
- 控制台连接FISCO BCOS 3.6.0版本后，才支持使用BalanceService的接口。
- 控制台连接FISCO BCOS 3.6.0版本后，才支持使用v1版本的交易接口。

---

## v3.5.0
(2023-10-16)

请阅读控制台 v3.x+文档：

- [中文用户手册](https://fisco-bcos-doc.readthedocs.io/zh-cn/latest/docs/operation_and_maintenance/console/index.html)

### 新增

- 增加系统Feature在 `SystemConfig`相关的命令中联想补全的功能。
- 增加 `getCandidateList` 命令，用于查询rPBFT当前节点的候选人列表。

### 更新

- 脚本 `get_account.sh` 生成账户密钥对时增加`.public.pem` 的公钥格式，与2.0版本保持一致。
- `setNodeName` 命令使用SDK的 `setNodeToSendRequest` 接口，支持Client全局接口指定节点发送。
- `org.fisco-bcos.java-sdk:fisco-bcos-java-sdk` 更新到3.5.0版本
- `org.fisco-bcos.code-generator:bcos-code-generator` 更新到1.2.0版本

### 修复

- 修复在调用 `link` 资源时没有正确返回ABI导致调用失败的问题。
- 修复权限命令的联想补全使用了错误Client标志位的问题。
- 修复JLine在刷新控制台时出现的联想补全错误。

### 兼容性说明

- 支持[FISCO BCOS 3.0.0版本](https://github.com/FISCO-BCOS/FISCO-BCOS/releases/tag/v3.0.0) 以上的区块链节点。
- 控制台连接FISCO BCOS 3.1.0版本后，才支持BFS list分页查询、link新接口，连接3.1.0版本前的节点将会用旧版本接口。
- 控制台连接FISCO BCOS 3.2.0版本后，才支持CRUD的LIKE语句和数字序建表，连接3.2.0版本前的节点将会用旧版本接口。
- 控制台连接FISCO BCOS 3.3.0版本后，才支持使用ShardService的接口。
- 控制台连接FISCO BCOS 3.4.0版本后，才支持使用call with sign接口。

## v3.4.0
(2023-06-13)

请阅读控制台 v3.x+文档：

- [中文用户手册](https://fisco-bcos-doc.readthedocs.io/zh-cn/latest/docs/operation_and_maintenance/console/index.html)

### 更新

- 将所有的static call请求都使用call with sign接口，支持在发起static call请求时使用私钥对请求体(to+data)进行签名，在节点侧将会对应会付出签名对应的用户地址，合约中可以取到call请求时的tx.origin和msg.sender，达到用户身份认证的目的。
- 优化liquid合约部署的体验，支持在部署时指定.wasm和.abi所在的文件夹进行部署。
- 适配Java sdk v3.4.0

### 兼容性说明

- 支持[FISCO BCOS 3.0.0版本](https://github.com/FISCO-BCOS/FISCO-BCOS/releases/tag/v3.0.0) 以上的区块链节点。
- 控制台连接FISCO BCOS 3.1.0版本后，才支持BFS list分页查询、link新接口，连接3.1.0版本前的节点将会用旧版本接口。
- 控制台连接FISCO BCOS 3.2.0版本后，才支持CRUD的LIKE语句和数字序建表，连接3.2.0版本前的节点将会用旧版本接口。
- 控制台连接FISCO BCOS 3.3.0版本后，才支持使用ShardService的接口。

---

## v3.3.0
(2023-04-17)

请阅读控制台 v3.x+文档：

- [中文用户手册](https://fisco-bcos-doc.readthedocs.io/zh-cn/latest/docs/operation_and_maintenance/console/index.html)

### 新增

- 新增对硬件密码机的支持。
- 新增适配链上动态权限配置项`auth_check_status`，新增初始化权限治理的命令`initAuth`。
- 新增链上分区服务命令：`makeShard`、`linkShard`、`getContractShard`。
- 新增命令 `fixBFS` 以恢复3.2.0版本节点写入的错误数据，相关Issue：https://github.com/FISCO-BCOS/FISCO-BCOS/issues/3600

### 更新

- 在升级链版本号后自动`switch group` 刷新控制台状态。
- get_account/get_gm_account脚本支持返回生成密钥的公私钥十六进制字符串。
- TableTest示例合约改名未TableTestV320，与TableTest合约区分。
- 更新 `fisco-bcos-java-sdk` 到3.3.0。
- 更新 `bcos-code-generator` 到1.1.0。

### 修复

- 修复struct类型的打印输出。
- 修复动态数组的返回bug。
- 修复get_account/get_gm_account脚本不支持OpenSSL 3.x版本的问题。

### 兼容性说明

- 支持[FISCO BCOS 3.0.0版本](https://github.com/FISCO-BCOS/FISCO-BCOS/releases/tag/v3.0.0) 以上的区块链节点。
- 控制台连接FISCO BCOS 3.1.0版本后，才支持BFS list分页查询、link新接口，连接3.1.0版本前的节点将会用旧版本接口。
- 控制台连接FISCO BCOS 3.2.0版本后，才支持CRUD的LIKE语句和数字序建表，连接3.2.0版本前的节点将会用旧版本接口。

---

## v3.2.0
(2023-01-17)

请阅读控制台 v3.x+文档：

- [中文用户手册](https://fisco-bcos-doc.readthedocs.io/zh-cn/latest/docs/operation_and_maintenance/console/index.html)

### 新增

- 新增合约权限管理命令： `abolishContract`，合约管理员可以通过这个命令废止某个合约，不再使用。
- 支持CRUD范围查询、修改、删除新接口，在CRUD SQL语句中支持LIKE语句，支持前缀、后缀、包含查询。
- 支持CRUD建表新接口，支持在建表时指定主键为整数类型，在插入时为数字序排序。
- 新增EntryWrapper、Cast的合约示例，优化CRUD的使用体验，提供在Solidity快速类型转换的工具。

### 更新

- 更新 `fisco-bcos-java-sdk` 到3.2.0
- 控制台提供的Solidity`TableTest`合约和Liquid`table_test`均已经支持全新的CRUD接口。

### 修复

- 修复一些错误发生时的信息展示。

### 兼容性说明

- 支持[FISCO BCOS 3.0.0版本](https://github.com/FISCO-BCOS/FISCO-BCOS/releases/tag/v3.0.0) 以上的区块链节点。
- 控制台连接FISCO BCOS 3.1.0版本后，才支持BFS list分页查询、link新接口，连接3.1.0版本前的节点将会用旧版本接口。
- 控制台连接FISCO BCOS 3.2.0版本后，才支持CRUD的LIKE语句和数字序建表，连接3.2.0版本前的节点将会用旧版本接口。

## v3.1.1
(2023-01-04)

请阅读控制台 v3.x+文档：

- [中文用户手册](https://fisco-bcos-doc.readthedocs.io/zh-cn/latest/docs/operation_and_maintenance/console/index.html)

### 新增

- 交易、交易回执数据结构添加`extraData`字段

### 兼容性说明

- 支持[FISCO BCOS 3.0.0版本](https://github.com/FISCO-BCOS/FISCO-BCOS/releases/tag/v3.0.0) 以上的区块链节点。
- 控制台连接FISCO BCOS 3.1.0版本后，才支持BFS list分页查询、link新接口，连接3.1.0版本前的节点将会用旧版本接口。

## v3.1.0
(2022-11-21)

请阅读控制台 v3.x+文档：

- [中文用户手册](https://fisco-bcos-doc.readthedocs.io/zh-cn/latest/docs/operation_and_maintenance/console/index.html)

### 新增

- 新增账户权限管理命令： `freezeAccount`、 `unfreezeAccount`、 `abolishAccount`
- 新增支持FISCO BCOS 3.1.0 新的BFS list分页接口、link接口，连接3.1.0以下版本时将会使用旧接口。
- 新增支持部署Solidity合约文件内指定的合约类，使用姿势为 'deploy A:B'
- 新增支持 contract2java.sh 脚本编译Solidity合约文件内指定的合约类
- 新增支持 contract2java.sh --no-analysis选项，可选不启用Solidity并行静态分析

### 更新

- 更新更新 `fisco-bcos-java-sdk` 到3.1.0
- 更新 `jackson-databind` 到2.14.0

### 修复

- 修复CRUD因为更新包导致的解析SQL使用错误

### 兼容性说明

- 支持[FISCO BCOS 3.0.0版本](https://github.com/FISCO-BCOS/FISCO-BCOS/releases/tag/v3.0.0) 以上的区块链节点。
- 控制台连接FISCO BCOS 3.1.0版本后，才支持BFS list分页查询、link新接口，连接3.1.0版本前的节点将会用旧版本接口。

## v3.0.1
(2022-9-26)

请阅读控制台 v3.x+文档：

- [中文用户手册](https://fisco-bcos-doc.readthedocs.io/zh-cn/latest/docs/operation_and_maintenance/console/index.html)

### 新增

* 新增 `deploy` 命令的 '-l' 选项，支持部署成功后在指定文件下建立软链接
* listAbi支持通过合约地址参数列出ABI

### 更新

* 升级`fisco-bcos-java-sdk`到`3.0.1`版本，请阅读Java SDK v3.x+文档：[Java SDK v3.x+文档](https://fisco-bcos-doc.readthedocs.io/zh_CN/latest/docs/develop/sdk/java_sdk/index.html)
* 部署Liquid合约只需要输入合约所在文件夹即可

### 修复

* 修复控制台部署时合约路径补全问题
* 修复控制台部署Liquid时文件路径找不到的问题

### 兼容性说明

- 支持[FISCO BCOS 3.0.1版本](https://github.com/FISCO-BCOS/FISCO-BCOS/releases/tag/v3.0.1)
- 不兼容 FISCO BCOS 2.0+ 版本
- 不兼容 FISCO BCOS 3.0-rc+ 版本

## v3.0.0
(2022-8-23)

请阅读控制台 v3.x+文档：

- [中文用户手册](https://fisco-bcos-doc.readthedocs.io/zh-cn/latest/docs/operation_and_maintenance/console/index.html)

### 新增

* 使用重构后的ABI Code Generator工具 `bcos-code-generator` ：https://github.com/FISCO-BCOS/code-generator
* 新增 disableSSL 配置选项，支持与节点无SSL通信

### 更新

* 升级`fisco-bcos-java-sdk`到`3.0.0`版本，请阅读Java SDK v3.x+文档：[Java SDK v3.x+文档](https://fisco-bcos-doc.readthedocs.io/zh_CN/latest/docs/develop/sdk/java_sdk/index.html)
* 升级`evm-static-analysis`到`1.0.0-rc3`
* 升级`Log4j`等外部依赖到较新版本

### 修复

* 修复控制台合约路径补全问题
* 修复控制台不更新本地ABI缓存的问题
* 在Solidity 0.8.11以上支持`base-path`功能

### 兼容性说明

- 支持[FISCO BCOS 3.0.0版本](https://github.com/FISCO-BCOS/FISCO-BCOS/releases/tag/v3.0.0)版本
- 不兼容 FISCO BCOS 2.0+ 版本
- 不兼容 FISCO BCOS 3.0-rc+ 版本

## v3.0.0-rc4
(2022-7-1)

请阅读Java SDK v3.x+文档：

- [中文用户手册](https://fisco-bcos-doc.readthedocs.io/zh_CN/latest/docs/develop/sdk/java_sdk/index.html)

### 新增

* 新增Table CRUD的命令：`select`,`update`,`insert`,`remove`,`alter`
* 新增共识节点变更提案、系统配置变更提案、升级计算逻辑提案等的命令
* 新增冻结、解冻合约功能命令
* 获取提案命令支持批量获取

### 更新

* 升级`fisco-bcos-java-sdk`到`3.0.0-rc4`
* 升级`evm-static-analysis`到`1.0.0-rc2`

### 修复

* 修复非交互式控制台的调用问题
* 重构命令代码，将所有命令抽象成`BasicCategoryCommand`，实际分类可参考`./src/main/java/console/command/category`

### 兼容性说明

- 不兼容 FISCO BCOS 2.0+ 版本
- 兼容java-sdk v3.x的历史版本
- 支持[3.0.0-rc4](https://github.com/FISCO-BCOS/FISCO-BCOS/releases/tag/v3.0.0-rc4)版本

### v3.0.0-rc3
(2022-03-31)

* 新增
1. 默认使用Solidity 0.8
2. 支持编译合约进行Solidity并行字段冲突分析
3. 添加`listABI`命令
4. BFS支持软链操作
5. 支持 call link操作，控制台支持调用所有链上部署的合约


* 更新
1. 适配[FISCO BCOS v3.0.0-rc3](https://github.com/FISCO-BCOS/FISCO-BCOS/releases/tag/v3.0.0-rc3)
2. 适配[FISCO BCOS Java SDK v3.0.0-rc3](https://github.com/FISCO-BCOS/java-sdk/releases/tag/v3.0.0-rc3)
3. 使用BFS软链取代CNS


* 修复
1. 修复BFS `cd`、`pwd`命令的bug

* 兼容性说明
- 沿用2.0+版本的大部分功能, 不兼容 FISCO BCOS 2.0+ 版本。FISCO BCOS 2.0+ 版本请使用对应版本的Console
- 不兼容[3.0.0-rc1](https://github.com/FISCO-BCOS/FISCO-BCOS/releases/tag/v3.0.0-rc1)版本的FISCO BCOS区块链
- 支持[3.0.0-rc3](https://github.com/FISCO-BCOS/FISCO-BCOS/releases/tag/v3.0.0-rc3)版本和[3.0.0-rc2](https://github.com/FISCO-BCOS/FISCO-BCOS/releases/tag/v3.0.0-rc2)版本的FISCO BCOS区块链

### v3.0.0-rc2
(2022-02-23)

* 更新
1. 适配[FISCO BCOS v3.0.0-rc2](https://github.com/FISCO-BCOS/FISCO-BCOS/releases/tag/v3.0.0-rc2)
2. 适配[FISCO BCOS Java SDK v3.0.0-rc2](https://github.com/FISCO-BCOS/java-sdk/releases/tag/v3.0.0-rc2)
3. 更新log4j2依赖到2.17.1版本


* 修复
1. 修复权限初始化失败的问题
2. 修复BFS和KVTable相关的部分命令补全的问题

* 兼容性说明
- 沿用2.0+版本的大部分功能, 不兼容 FISCO BCOS 2.0+ 版本。FISCO BCOS 2.0+ 版本请使用对应版本的Console
- 不兼容[3.0.0-rc1](https://github.com/FISCO-BCOS/FISCO-BCOS/releases/tag/v3.0.0-rc1)版本的FISCO BCOS区块链
- 仅支持[3.0.0-rc2](https://github.com/FISCO-BCOS/FISCO-BCOS/releases/tag/v3.0.0-rc2)版本的FISCO BCOS区块链

### v3.0.0-rc1
(2021-12-10)

* 更新
1. 适配[FISCO BCOS v3.0.0-rc1](https://github.com/FISCO-BCOS/FISCO-BCOS/releases/tag/v3.0.0-rc1)
2. 适配[FISCO BCOS Java SDK v3.0.0-rc1](https://github.com/FISCO-BCOS/java-sdk/releases/tag/v3.0.0-rc1)


* 新增
1. 新增BFS、权限管理相关命令
2. 支持WBC-Liquid合约，包含部署和调用WBC-Liquid合约
3. 新增WBC-Liquid编译后的WASM和ABI文件转换成Java合约文件的脚本


* 兼容性说明
- 沿用2.0+版本的大部分功能, 不兼容 FISCO BCOS 2.0+ 版本。FISCO BCOS 2.0+ 版本请使用对应版本的Console
- 仅支持[3.0.0-rc1](https://github.com/FISCO-BCOS/FISCO-BCOS/releases/tag/v3.0.0-rc1)版本的FISCO BCOS区块链


### v2.8.0
(2021-07-28)

* 更新
1. 支持硬件密码模块，使用硬件保护您的密钥安全，提升密码运算效率。
    - 支持使用密码卡/密码机内部SM2密钥，用硬件保护您的私钥安全。
    - 支持接入符合国密《GMT0018-2012密码设备应用接口规范》标准的密码机/密码卡，使用硬件安全模块进行SM2、SM3、SM4等算法运算。
    - 支持使用密码卡/密码机进行共识签名、交易验签、建立TLS连接。
    - 新增支持硬件安全模块的OpenSSL 1.1.1i


### v2.7.2
(2021-3-24)

* 更新
1. 升级Java SDK版本为`2.7.2`

* 修复
1. 修复`getCurrentAccount`输出的账户地址与发送交易的账户地址不一致的问题

### v2.7.1
(2020-12-24)

* 更新
1. 升级Java SDK版本为`2.7.1`
2. 切换群组时不清除历史命令，支持基于历史命令的补全

* 修复
1. 修复合约返回值为`bytes`和`bytesN`类型时，控制台输出乱码的问题
2. 修复控制台无法正常输入`bytes`类型参数的问题

### v2.7.0
(2020-11-20)

* 新增
1. 添加`getBatchReceiptsByBlockHashAndRange`和`getBatchReceiptsByBlockNumberAndRange`命令，支持批量拉取交易回执
2. 添加`getNodeInfo`命令，支持拉取节点订阅的topics信息
3. 添加`revokeContractStatusManager`命令，支持撤销合约生命周期管理权限
4. 添加`queryVotesOfMember`和`queryVotesOfThreshold`命令，支持委员权限管理投票情况查询

* 更新
1. `callByCNS`从节点拉取`ABI`描述信息
2. 升级Java SDK版本为`2.7.0`

### v2.6.1
(2020-10-30)


* 新增
1. `call`命令支持使用`latest`关键字调用最新合约

* 更新
1. 依赖`v2.6.1`版本的`Java SDK`

### v2.6.0
(2020-10-16)

* 新增
1. 添加`getCurrentAccount`命令，查看当前账户信息
2. 添加`getCryptoType`命令，查看当前区块链账本类型(目前支持国密和非国密)、控制台与节点之间的SSL连接类型(目前支持OpenSSL和国密SSL)
3. 添加`generateGroup`、`generateGroupFromFile`、`startGroup`、`stopGroup`、`removeGroup`、`recoverGroup`命令支持动态群组管理

* 更新
1. 将控制台对`Web3SDK`的依赖替换为依赖`Java SDK`
2. `deploy`、`deployByCNS`、`call`、`callByCNS`、`listAbi`、`listDeployContractAddress`、`queryCNS`、`registerCNS`命令中的`contractNameOrPath`参数同时支持合约名与合约路径，且支持合约名、合约路径的补全
3. 修改`get_gm_account.sh`脚本，将生成的国密账户私钥信息放到`accounts_gm`目录下

### v1.2.0

(2020-09-30)

* 新增
1. 添加简单私钥管理工具命令：`newAccount` `switchAccount` `listAccount` `loadAccount`
2. 添加`listAbi`命令查看合约接口以及Event列表
3. 添加`registerCNS`命令，将合约信息注册CNS
4. 支持非交互式命令行

* 更新
1. `call`命令支持合约地址，接口补全
1. 升级web3sdk版本为2.6.2, 支持JDK14
2. `sol2java.sh`支持绝对路径与相对路径，支持合约文件路径以及合约目录路径，支持设置生成`Java`文件目录
3. 修改`get_gm_account.sh`，生成的国密私钥放入`accounts`目录，并且添加`_gm`标记
4. `call` `deploy` `deployByCNS`支持相对路径与绝对路径的方式

### v1.1.1

(2020-09-07)

* 修复
1. `solcJ`编译器在`aarch64`平台动态链接失败导致无法使用的问题

### v1.1.0

(2020-08-12)

* 新增
1. 添加`getBlockHeaderByHash` `getBlockHeaderByNumber`命令

* 更新
1. 升级web3sdk版本为2.6.0, 参考web3sdk的ChangeLog更新内容
2. 更新`get_acount.sh` `get_gm_account.sh`支持`aarch64`架构
3. 更新`solcJ`版本，支持同时编译合约的国密/非国密版本，支持`aarch64`架构
4. 更新`sol2java.sh`，能够生成国密/非国密环境同时能够使用的Java合约代码

### v1.0.10

(2020-06-19)

* 新增
1. 新增角色权限的命令列表

* 更新
1. 升级web3sdk版本为2.5.0, 参考web3sdk的ChangeLog更新内容

### v1.0.9

(2020-03-27)

* 新增
1. 新增冻结合约的命令列表
2. 新增合约状态管理的命令列表
3. 添加KVTableTest.sol示例合约

* 更新
1. 升级web3sdk版本为2.3.0, 参考web3sdk的更新内容
2. 提供更友好的错误信息展示

* 修复
1. SQL语句设置的字段值无法包含逗号的bug
2. 修复deploylog.txt文件格式不正确，控制台无法启动的问题

### v1.0.8

(2020-01-17)

* 更新
1. 升级web3sdk版本为2.2.1, 参考web3sdk的更新内容

### v1.0.7

(2019-12-24)

* 更新
1. 升级web3sdk版本为2.2.0
2. 适配fisco-bcos 2.2.0限制CRUD表名的最大长度为48
3. 添加get_gm_account.sh生成国密版账户

### v1.0.6

(2019-11-26)

* 更新
1. web3sdk版本更新为2.1.2
2. start.sh 添加 -Djdk.tls.namedGroups="secp256k1" 参数

### v1.0.5

(2019-09-17)

* 更新
1. `web3sdk`版本更新为`2.1.0`
2. `deployByCNS`将合约的`ABI`保存入`CNS`表
3. `start.sh -p12/-pem`参数执行的文件支持相对路径，绝对路径

* 修复
1. `getTotalTransactionCount`显示结果为`null`
2. 控制台调用合约，无法区分合约`bytes` `bytesN`类型导致输入被截断
3. 多处读取文件没有关闭输入流
4. 其他修复

### v1.0.4

(2019-07-05)

* 增加

1. 添加交易解析功能：`call`、`callCNS`支持解析`output`、`event log`；
    `getTransactionByHash`、`getTransactionByBlockHashAndIndex`、`getTransactionByBlockHashAndIndex` 支持解析`input`；
    `getTransactionReceipt`支持解析`input`、`output`、`event log`
2. 去除无用的jar包依赖


### v1.0.3

(2019-05-28)

* 增加

1. 增加操作用户表的sql命令，包括`create`, `insert` , `update`, `delete`, `select`, `desc`命令。
2. 增加账户生成脚本`get_accounts.sh`，支持加载账户文件登录控制台。
3. 支持`call`, `callByCNS`，`getTransactionReceipt`对event log进行解析输出。
4. 发送交易后，交易哈希值前面加`transaction hash:`。


### v1.0.2

(2019-04-19)

* 更新

1. `console/conf`目录下移除`applicationContext.xml`配置文件和`ca.crt`， `node.crt`，`node.key`文件。新增`applicationContext-sample.xml`配置文件。
2. 一个控制台部署合约，另一个控制台可以直接调用合约。
3. `callByCNS`支持在不传合约版本号时调用最新版本合约。传入版本号时，其合约名与版本号使用英文冒号分隔，例如`HelloWorld:1.0`
4. 重复创建用户表会提示表已存在的错误信息，无权限的账号对用户表进行增删改操作会提示无权限信息。（适配fisco bcos 2.0.0-rc2）
5. 调用合约时，传入错误合约地址会获取错误提示信息。（适配fisco bcos 2.0.0-rc2）
6. 部署合约后，显示的合约地址前面加字符串`contract 增加ress:`。
7. 合约地址只能省略前缀0，不能省略`0x`。例如`0x000ac78`可以简写成`0xac78`。
8. `help`命令显示的命令列表进行了字典排序。


### v1.0.1

(2019-04-08)

* 增加 

1. 提供`getDeployLog`（查询部署的合约地址），`exit`（退出控制台）命令。
2. 提供下载控制台脚本`download_console.sh`。
3. `start.sh`脚本中增加对Java版本检测。

* 更新

1. 支持合约引入Solidity library库。
2. 支持合约地址可以省略前缀0。例如，`0x000ac78`可以简写成`0xac78`。
3. 优化命令的帮助信息。

### v1.0.0

(2019-03-18)

* 增加 

1. 提供区块链状态查询命令。
2. 提供管理区块链节点的命令。
3. 提供简单易用的部署和调用合约命令。
4. 提供合约编译工具将Solidity合约文件编译为Java合约文件。
