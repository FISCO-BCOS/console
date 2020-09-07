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
