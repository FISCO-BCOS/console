### v2.9.1
(2022-06-15)

* 更新
1. 升级Java SDK版本为`2.9.1`

* 修复
1. 修复`sol2java.sh`生成`Java`合约代码，编译失败的问题[#622](https://github.com/FISCO-BCOS/console/issues/622)

### v2.9.0
(2022-05-12)

* 更新
1. 升级Java SDK版本为`2.9.0`，支持`APPLE M1`

### v2.8.1
(2022-04-29)

* 更新
1. 升级Java SDK版本为`2.8.1`，优化启动失败错误提示

### v2.8.0
(2021-07-28)

* 更新
1. 升级Java SDK版本为`2.8.0-GMT0018`


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
