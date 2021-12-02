package console.command.model;

import console.command.SupportedCommand;
import console.common.Common;
import console.common.ConsoleUtils;
import org.fisco.bcos.sdk.model.PrecompiledConstant;

public class HelpInfo {
    public static void promptHelp(String command) {
        System.out.println("Try '" + command + " -h or --help' for more information.");
        System.out.println();
    }

    public static void printHelp(String func, boolean isWasm, boolean isAuthOpen) {
        CommandInfo commandInfo = SupportedCommand.getCommandInfo(func, isWasm, isAuthOpen);
        if (commandInfo != null) {
            commandInfo.printUsageInfo();
        }
    }

    public static void help() {
        System.out.println("Provide help information.");
        System.out.println("Usage: help");
    }

    public static void freezeContractHelp() {
        System.out.println("Freeze the contract.");
        System.out.println("Usage: \nfreezeContract contractAddress");
        System.out.println("* contractAddress: 20 Bytes - The address of a contract.");
    }

    public static void unfreezeContractHelp() {
        System.out.println("Unfreeze the contract.");
        System.out.println("Usage: \nunfreezeContract contractAddress");
        System.out.println("* contractAddress: 20 Bytes - The address of a contract.");
    }

    public static void grantContractStatusManagerHelp() {
        System.out.println("Grant contract authorization to the user.");
        System.out.println("Usage: \ngrantContractStatusManager contractAddress userAddress");
        System.out.println("* contractAddress: 20 Bytes - The address of a contract.");
        System.out.println("* userAddress: 20 Bytes - The address of a tx.origin.");
    }

    public static void revokeContractStatusManagerHelp() {
        System.out.println("revoke contract authorization to the user.");
        System.out.println("Usage: \nrevokeContractStatusManager contractAddress userAddress");
        System.out.println("* contractAddress: 20 Bytes - The address of a contract.");
        System.out.println("* userAddress: 20 Bytes - The address of a tx.origin.");
    }

    public static void getContractStatusHelp() {
        System.out.println("Get the status of the contract.");
        System.out.println("Usage: \ngetContractStatus contractAddress");
        System.out.println("* contractAddress -- 20 Bytes - The address of a contract.");
    }

    public static void listContractStatusManagerHelp() {
        System.out.println("List the authorization of the contract.");
        System.out.println("Usage: \nlistContractStatusManager contractAddress");
        System.out.println("* contractAddress -- 20 Bytes - The address of a contract.");
    }

    public static void getDeployLogHelp() {
        System.out.println("Query the log of deployed contract.");
        System.out.println("Usage: \ngetDeployLog [recordNumber]");
        System.out.println(
                "* recordNumber -- (optional) The number of deployed contract records, "
                        + Common.DeployLogIntegerRange
                        + "(default 20).");
    }

    public static void getBlockNumberHelp() {
        System.out.println("Query the number of most recent block.");
        System.out.println("Usage: \ngetBlockNumber");
    }

    public static void switchEndPointHelp() {
        System.out.println("Switch to a specific peer by endpoint.");
        System.out.println("Usage: \nswitch groupId");
        System.out.println("* endPoint -- The endPoint of the new peer.");
    }

    public static void getPbftViewHelp() {
        System.out.println("Query the pbft view of node.");
        System.out.println("Usage: \ngetPbftView");
    }

    public static void getObserverListHelp() {
        System.out.println("Query nodeId list for observer nodes.");
        System.out.println("Usage: \ngetObserverList");
    }

    public static void getSealerListHelp() {
        System.out.println("Query nodeId list for sealer nodes.");
        System.out.println("Usage: \ngetSealerList");
    }

    public static void getConsensusStatusHelp() {
        System.out.println("Query consensus status.");
        System.out.println("Usage: \ngetConsensusStatus");
    }

    public static void getSyncStatusHelp() {
        System.out.println("Query sync status.");
        System.out.println("Usage: \ngetSyncStatus");
    }

    public static void getNodeVersionHelp() {
        System.out.println("Query the current node version.");
        System.out.println("Usage: \ngetNodeVersion");
    }

    public static void getGroupInfoHelp() {
        System.out.println("Query the current group information.");
        System.out.println("Usage: \ngetGroupInfo");
    }

    public static void getPeersHelp() {
        System.out.println("Query peers currently connected to the client.");
        System.out.println("Usage: \ngetPeers [NodeEndPoint]");
        System.out.println(
                "* NodeEndPoint[Optional] -- The requested node information, the format is IP:Port, "
                        + "the list of all connected nodes can be obtained through getAvailableConnections");
    }

    public static void getNodeIDListHelp() {
        System.out.println("Query nodeId list for all connected nodes.");
        System.out.println("Usage: \ngetNodeIDList");
    }

    public static void getGroupPeersHelp() {
        System.out.println("Query nodeId list for sealer and observer nodes.");
        System.out.println("Usage: \ngetGroupPeers [NodeEndPoint]");
        System.out.println(
                "* NodeEndPoint[Optional] -- The requested node information, the format is IP:Port, "
                        + "the list of all connected nodes can be obtained through getAvailableConnections");
    }

    public static void getGroupListHelp() {
        System.out.println("Query group list.");
        System.out.println("Usage: \ngetGroupList [NodeEndPoint]");
        System.out.println(
                "* NodeEndPoint[Optional] -- The requested node information, the format is IP:Port, "
                        + "the list of all connected nodes can be obtained through getAvailableConnections");
    }

    public static void quitHelp() {
        System.out.println("Quit console.");
        System.out.println("Usage: \nquit or exit");
    }

    public static void getBlockByHashHelp() {
        System.out.println("Query information about a block by hash.");
        System.out.println("Usage: \ngetBlockByHash blockHash [boolean]");
        System.out.println("* blockHash -- 32 Bytes - The hash of a block.");
        System.out.println(
                "* boolean -- (optional) If true it returns the full transaction objects, if false only the hashes of the transactions.");
    }

    public static void getBlockByNumberHelp() {
        System.out.println("Query information about a block by block number.");
        System.out.println("Usage: \ngetBlockByNumber blockNumber [boolean]");
        System.out.println(
                "* blockNumber -- Integer of a block number, "
                        + Common.NonNegativeIntegerRange
                        + ".");
        System.out.println(
                "* boolean -- (optional) If true it returns the full transaction objects, if false only the hashes of the transactions.");
    }

    public static void getBlockHeaderByHashHelp() {
        System.out.println("Query information about a block header by hash.");
        System.out.println("Usage: \ngetBlockHeaderByHash blockHash [boolean]");
        System.out.println("* blockHash -- 32 Bytes - The hash of a block.");
        System.out.println(
                "* boolean -- (optional) If true the signature list will also be returned.");
    }

    public static void getBlockHeaderByNumberHelp() {
        System.out.println("Query information about a block header by block number.");
        System.out.println("Usage: \ngetBlockHeaderByNumber blockNumber [boolean]");
        System.out.println(
                "* blockNumber -- Integer of a block number, "
                        + Common.NonNegativeIntegerRange
                        + ".");

        System.out.println(
                "* boolean -- (optional) If true the signature list will also be returned.");
    }

    public static void getBlockHashByNumberHelp() {
        System.out.println("Query block hash by block number.");
        System.out.println("Usage: \ngetBlockHashByNumber blockNumber");
        System.out.println(
                "* blockNumber -- Integer of a block number, "
                        + Common.NonNegativeIntegerRange
                        + ".");
    }

    public static void getTransactionByHashHelp() {
        System.out.println("Query information about a transaction requested by transaction hash.");
        System.out.println("Usage: \ngetTransactionByHash transactionHash");
        System.out.println("* transactionHash -- 32 Bytes - The hash of a transaction.");
    }

    public static void getTransactionByBlockHashAndIndexHelp() {
        System.out.println(
                "Query information about a transaction by block hash and transaction index position.");
        System.out.println("Usage: \ngetTransactionByBlockHashAndIndex blockHash index");
        System.out.println("* blockHash -- 32 Bytes - The hash of a block.");
        System.out.println(
                "* index -- Integer of a transaction index, "
                        + Common.NonNegativeIntegerRange
                        + ".");
    }

    public static void getTransactionByHashWithProofHelp() {
        System.out.println(
                "Query information about the transaction and proof by transaction hash.");
        System.out.println("Usage: \ngetTransactionByHashWithProof transactionHash");
        System.out.println("* transactionHash -- 32 Bytes - The hash of a transaction.");
    }

    public static void getTransactionReceiptByHashWithProofHelp() {
        System.out.println(
                "Query information about the transaction receipt and proof by transaction hash.");
        System.out.println("Usage: \ngetTransactionReceiptByHashWithProof transactionHash");
        System.out.println("* transactionHash -- 32 Bytes - The hash of a transaction.");
    }

    public static void getTransactionByBlockNumberAndIndexHelp() {
        System.out.println(
                "Query information about a transaction by block number and transaction index position.");
        System.out.println("Usage: \ngetTransactionByBlockNumberAndIndex blockNumber index");
        System.out.println(
                "* blockNumber -- Integer of a block number, "
                        + Common.NonNegativeIntegerRange
                        + ".");
        System.out.println(
                "* index -- Integer of a transaction index, "
                        + Common.NonNegativeIntegerRange
                        + ".");
    }

    public static void getTransactionReceiptHelp() {
        System.out.println("Query the receipt of a transaction by transaction hash.");
        System.out.println("Usage: \ngetTransactionReceipt transactionHash");
        System.out.println("* transactionHash -- 32 Bytes - The hash of a transaction.");
    }

    public static void getPendingTransactionsHelp() {
        System.out.println("Query pending transactions.");
        System.out.println("Usage: \ngetPendingTransactions");
    }

    public static void getPendingTxSizeHelp() {
        System.out.println("Query pending transactions size.");
        System.out.println("Usage: \ngetPendingTxSize");
    }

    public static void getCodeHelp() {
        System.out.println("Query code at a given address.");
        System.out.println("Usage: \ngetCode address");
        System.out.println("* address -- 20 Bytes - The address of a contract.");
    }

    public static void getTotalTransactionCountHelp() {
        System.out.println("Query total transaction count.");
        System.out.println("Usage: \ngetTotalTransactionCount");
    }

    public static void deployHelp(boolean isWasm) {
        System.out.println("Deploy a contract on blockchain.");
        if (!isWasm) {
            System.out.println("Usage: \ndeploy solidity contractNameOrPath parameters...");
            System.out.println(
                    "* contractNameOrPath -- The name of a contract or the path of a contract (Default load contract from the \"contracts/solidity\" path when using contractName).");
            System.out.println(
                    "* parameters -- Parameters will be passed to constructor when deploying the contract.");
        } else {
            System.out.println("Usage: \ndeploy liquid bin abi path parameters...");
            System.out.println(
                    "* bin -- The path of binary file after contract being compiled via cargo-liquid.");
            System.out.println(
                    "* abi -- The path of ABI file after contract being compiled via cargo-liquid.");
            System.out.println("* path -- The path where the contract will be located at.");
            System.out.println(
                    "* parameters -- Parameters will be passed to constructor when deploying the contract.");
        }
    }

    public static void callHelp(boolean isWasm) {
        System.out.println("Call a contract by a function and parameters.");
        if (!isWasm) {
            System.out.println(
                    "Usage: \ncall contractNameOrPath contractAddress function parameters");
            System.out.println(
                    "* contractNameOrPath -- The name of a contract or the path of a contract, when set to \"latest\", the contract address is the latest contract address (Default load contract from the \"contracts/solidity\" path when using contractName).");
            System.out.println("* contractAddress -- 20 Bytes - The address of a contract.");
            System.out.println("* function -- The function of a contract.");
            System.out.println("* parameters -- The parameters(splited by a space) of a function.");
        } else {
            System.out.println("Usage: \ncall path function parameters");
            System.out.println(
                    "* path -- The path where the contract located at, when set to \"latest\", the path of latest contract deployment will be used.");
            System.out.println("* function -- The function of a contract.");
            System.out.println("* parameters -- The parameters(splited by a space) of a function.");
        }
    }

    public static void deployByCNSHelp() {
        System.out.println("Deploy a contract on blockchain by CNS.");
        System.out.println("Usage: \ndeployByCNS contractNameOrPath contractVersion");
        System.out.println(
                "* contractNameOrPath -- The name of a contract or the path of a contract (Default load contract from the \"contracts/solidity\" path when using contractName).");
        System.out.println(
                "* contractVersion -- The version of a contract. The maximum length of the version hex string is "
                        + PrecompiledConstant.CNS_MAX_VERSION_LENGTH
                        + ".");
    }

    public static void callByCNSHelp() {
        System.out.println("Call a contract by a function and paramters by CNS.");
        System.out.println(
                "Usage: \ncallByCNS contractNameOrPath:contractVersion function parameters");
        System.out.println(
                "* contractNameOrPath:contractVersion -- The name(or path) and version of a contract.");
        System.out.println("* function -- The function of a contract.");
        System.out.println("* parameters -- The parameters(splited by a space) of a function.");
    }

    public static void queryCNSHelp() {
        System.out.println("Query CNS information by contract name and contract version.");
        System.out.println("Usage: \nqueryCNS contractNameOrPath [contractVersion]");
        System.out.println(
                "* contractNameOrPath -- The name of a contract or the path of a contract (Default load contract from the \"contracts/solidity\" path when using contractName).");
        System.out.println("* contractVersion -- (optional) The version of a contract. ");
    }

    public static void registerCNSHelp() {
        System.out.println("Register CNS information by contract name and contract version.");
        System.out.println(
                "Usage: \nregisterCNS contractNameOrPath contractAddress contractVersion");
        System.out.println(
                "* contractNameOrPath -- The name of a contract or the path of a contract (Default load contract from the \"contracts/solidity\" path when using contractName).");
        System.out.println("* contractAddress[Required] -- The address of a contract.");
        System.out.println("* contractVersion[Required] -- The version of a contract.");
    }

    public static void addObserverHelp() {
        System.out.println("Add an observer node.");
        System.out.println("Usage: \naddObserver nodeId");
        System.out.println("* nodeId -- The nodeId of a node.");
    }

    public static void addSealerHelp() {
        System.out.println("Add a sealer node.");
        System.out.println("Usage: \naddSealer nodeId weight");
        System.out.println("* nodeId -- The nodeId of a node.");
        System.out.println("* weight -- The weight of the consensus node.");
    }

    public static void removeNodeHelp() {
        System.out.println("Remove a node.");
        System.out.println("Usage: \nremoveNode nodeId");
        System.out.println("* nodeId -- The nodeId of a node.");
    }

    public static void setConsensusWeightHelp() {
        System.out.println("Set consensus weight for the specified node");
        System.out.println("Usage: \nsetConsensusWeight nodeId weight");
        System.out.println("* nodeId -- The nodeId of a node.");
        System.out.println("* weight -- The weight for the consensus node.");
    }

    public static void freezeAccountHelp() {
        System.out.println("Freeze account.");
        System.out.println("Usage: \nfreezeAccount account");
        System.out.println("* account -- 20 Bytes - The address of a account.");
    }

    public static void unfreezeAccountHelp() {
        System.out.println("Unfreeze account.");
        System.out.println("Usage: \nunfreezeAccount account");
        System.out.println("* account -- 20 Bytes - The address of a account.");
    }

    public static void getAccountStatusHelp() {
        System.out.println("Account status.");
        System.out.println("Usage: \ngetAccountStatus account");
        System.out.println("* account -- 20 Bytes - The address of a account.");
    }

    public static void setSystemConfigByKeyHelp() {
        System.out.println("Set a system config.");
        System.out.println("Usage: \nsetSystemConfigByKey key value");
        System.out.println(
                "* key   -- The name of system config(tx_count_limit/tx_gas_limit/consensus_leader_period supported currently).");
        System.out.println("* value -- The value of system config to be set.");
        System.out.println(
                "      -- The value of tx_count_limit "
                        + Common.TxCountLimitRange
                        + "(default 1000).");
        System.out.println(
                "      -- the value of tx_gas_limit "
                        + Common.TxGasLimitRange
                        + "(default 300000000).");
        System.out.println(
                "      -- the value of  "
                        + Common.ConsensusLeaderPeriod
                        + " "
                        + Common.ConsensusLeaderPeriodRange
                        + "(default 1).");
    }

    public static void getSystemConfigByKeyHelp() {
        System.out.println("Query a system config value by key.");
        System.out.println("Usage: \ngetSystemConfigByKey key");
        System.out.println(
                "* key -- The name of system config(tx_count_limit/tx_gas_limit/consensus_leader_period supported currently).");
    }

    public static void operateGroupHelp(String command, String operator) {
        System.out.println("Usage: \n" + command + " endPoint groupId");
        System.out.println(
                "* endPoint[Required] -- the IP and Port information of the target node , the format is IP:Port");
        System.out.println(
                "* groupId[Optional] -- the group that should be "
                        + operator
                        + ", default is the current group.");
    }

    public static void generateGroupHelp() {
        System.out.println("Usage:\ngenerateGroup endPoint groupId timestamp sealerList");
        System.out.println(
                "* endPoint(required) -- The IP:Port information of the node need to generate a new group");
        System.out.println("* groupId(required) -- The groupId of the generated group");
        System.out.println("* timestamp(required) -- The timestamp of the group genesis block");
        System.out.println(
                "* sealerList(required) -- The initial nodeID of the sealer list for the generated group. Different sealer NodeIDs are separated by spaces\n");
    }

    public static void generateGroupFromFileHelp() {
        System.out.println("Usage: \ngenerateGroupFromFile groupConfigFilePath groupId");
        System.out.println(
                "* groupConfigFilePath(required) -- The configuration file path of the generated group. "
                        + "For specific configuration options, please refer to group-generate-config.toml in the conf directory");
        System.out.println(
                "* groupId(Optional) -- The groupId of the generated group, default is the current group.");
    }

    public static void showDescHelp() {
        System.out.println("Description table information.");
        System.out.println("Usage: \ndesc tableName");
        System.out.println("* tableName -- The name of the table.");
    }

    public static void promptNoFunc(String contractName, String funcName, int lenParams) {
        if (lenParams <= 1) {
            System.out.println(
                    "The method "
                            + funcName
                            + " with "
                            + lenParams
                            + " parameter"
                            + " is undefined of the contract.");
        } else {
            System.out.println(
                    "The method "
                            + funcName
                            + " with "
                            + lenParams
                            + " parameters"
                            + " is undefined of the contract.");
        }
    }

    public static void loadAccountHelp() {
        System.out.println("Load account for the transaction signature");
        System.out.println("Usage: \nloadAccount accountPath accountFormat");
        System.out.println(
                "* accountPath[Required] -- The path of the account key file, support .pem and .p12 format file; or the account address");
        System.out.println(
                "* accountFormat[Optional] -- The account file format, support \\\"pem\\\" and \\\"p12\\\", default is \"pem\"!");
    }

    public static void newAccountHelp() {
        System.out.println("Create a new account");
        System.out.println("Usage: \nnewAccount [accountFormat] [password]");
        System.out.println(
                "* accountFormat[Optional] -- The account file format, support \"pem\" and \"p12\" now, default is \"pem\".");
        System.out.println(
                "* password[Optional] -- The password of the p12 account file. Default is empty string.");
    }

    public static void listDeployContractAddressHelp() {
        System.out.println("List the contractAddress for the specified contract");
        System.out.println("Usage: \nlistDeployContractAddress contractNameOrPath [recordNumber]");
        System.out.println(
                "* contractNameOrPath -- The name of a contract or the path of a contract (Default load contract from the \"contracts/solidity\" path when using contractName).");
        System.out.println(
                "recordNumber -- (optional) The number of deployed contract records, (default 20).");
    }

    public static void startHelp() {
        System.out.println("Please provide one of the following ways to start the console.");
        System.out.println("Usage: ");
        System.out.println("./start.sh");
        System.out.println("./start.sh groupID");
        System.out.println("./start.sh groupID -pem pemName");
        System.out.println("./start.sh groupID -p12 p12Name");
    }

    public static void listAbiHelp() {
        System.out.println("List functions and events info of the contract.");
        System.out.println("Usage: listAbi [contractPath/contractName]");
        System.out.println(
                "contractPath/contractName[Required] -- The name or the path of a contract, if a name is specified, the contract should in the default directory \"contracts/solidity\"");
    }

    public static void changeDirHelp() {
        System.out.println("Change dir to given path.");
        System.out.println("Usage: cd [relativePath/absolutePath]");
        System.out.println(
                "relativePath/absolutePath[Required] -- The name or the path of a directory.");
    }

    public static void makeDirHelp() {
        System.out.println("Creat dir in given path.");
        System.out.println("Usage: cd [relativePath/absolutePath]");
        System.out.println(
                "relativePath/absolutePath[Required] -- The name or the path of a directory.");
    }

    public static void listDirHelp() {
        System.out.println("List resource in given path.");
        System.out.println("Usage: cd [relativePath/absolutePath]");
        System.out.println(
                "relativePath/absolutePath[Required] -- The name or the path of a directory.");
    }

    public static void pwdHelp() {
        System.out.println("Show absolute path of working directory name.");
        System.out.println("Usage: pwd");
    }

    public static void initializeHelp() {
        System.out.println("Usage: \ninitialize binPath abiPath");
        System.out.println("* binPath -- The path of contract template.");
        System.out.println("* abiPath -- The path of corresponding ABI.");
    }

    public static void signHelp() {
        System.out.println("Usage: \nsign contractName parameters...");
        System.out.println("* contractName -- The name of the contract.");
        System.out.println("* parameters -- The parameters(split by space) of the contract.");
    }

    public static void exerciseHelp() {
        System.out.println("Usage: \nexercise contract rightName parameters...");
        System.out.println(
                "* contract -- The name and ID(split by `#`) of the exercised contract.");
        System.out.println("* rightName -- The name of the exercised right.");
        System.out.println("* parameters -- The parameters(split by space) of the contract.");
    }

    public static void fetchHelp() {
        System.out.println("Usage: \nfetch contract");
        System.out.println(
                "* contract -- The name and ID(split by `#`) of the exercised contract.");
    }

    public static void setNodeNameHelp() {
        System.out.println("Set default node name to send request.");
        System.out.println("Usage: setNodeName [nodeName]");
        System.out.println(
                "nodeName[Required] -- The name of node to send request, default is \"\".");
    }

    public static void clearNodeNameHelp() {
        System.out.println("Clear default node name to empty.");
        System.out.println("Usage: clearNodeName");
        System.out.println(
                "[Note]: If you clear node name to empty, RPC will send request to node randomly.");
    }

    public static void getNodeNameHelp() {
        System.out.println("Get default node name in this client.");
        System.out.println("Usage: getNodeName");
    }

    public static void updateGovernorProposalHelp() {
        System.out.println("Create a proposal to committee, which attempt to update a governor.");
        System.out.println(
                "\033[32m"
                        + "[Note]: this command is only available for governors of committee."
                        + "\033[m");
        ConsoleUtils.singleLine();
        System.out.println("Usage: updateGovernorProposal account weight");
        System.out.println(
                "account[Required] -- The address of governor, it's length should be 40 in hex.");
        System.out.println(
                "weight[Required] -- The weight of governor, which is larger equal than 0.");
        System.out.println(
                "        [Note]: if you set a new governor, you can set a new weigh for governor.");
        System.out.println(
                "\033[31m"
                        + "        [WARNING]: if you set governor's weight to 0, it will delete this governor from committee."
                        + "\033[m");
    }

    public static void setRateProposalHelp() {
        System.out.println(
                "Create a proposal to committee, which attempt to update committee vote rate.");
        System.out.println(
                "\033[32m"
                        + "[Note]: this command is only available for governors of committee."
                        + "\033[m");
        ConsoleUtils.singleLine();
        System.out.println("Usage: setRateProposal participatesRate winRate");
        System.out.println(
                "participatesRate[Required] -- The percent rate of participate threshold, it should range in [0,100].");
        System.out.println(
                "winRate[Required] -- The percent rate of proposal win threshold, it should range in [0,100].");
        System.out.println(
                "        [Note]: if you set any rate to 0, proposal will always success.");
    }

    public static void setDeployAuthTypeProposalHelp() {
        System.out.println(
                "Create a proposal to committee, which attempt to set deploy ACL type globally.");
        System.out.println(
                "\033[32m"
                        + "[Note]: this command is only available for governors of committee."
                        + "\033[m");
        ConsoleUtils.singleLine();
        System.out.println("Usage: setDeployAuthTypeProposal authType");
        System.out.println(
                "authType[Required] -- The ACL strategy of deploy, it should be 'white_list' or 'black_list'.");
    }

    public static void openDeployAuthProposalHelp() {
        System.out.println(
                "Create a proposal to committee, which attempt to open deploy ACL for specific account.");
        System.out.println(
                "\033[32m"
                        + "[Note]: this command is only available for governors of committee."
                        + "\033[m");
        ConsoleUtils.singleLine();
        System.out.println("Usage: openDeployAuthProposal account");
        System.out.println(
                "account[Required] -- The address of admin account, it's length should be 40 in hex.");
    }

    public static void closeDeployAuthProposalHelp() {
        System.out.println(
                "Create a proposal to committee, which attempt to close deploy ACL for specific account.");
        System.out.println(
                "\033[32m"
                        + "[Note]: this command is only available for governors of committee."
                        + "\033[m");
        ConsoleUtils.singleLine();
        System.out.println("Usage: closeDeployAuthProposal account");
        System.out.println(
                "account[Required] -- The address of admin account, it's length should be 40 in hex.");
    }

    public static void resetAdminProposalHelp() {
        System.out.println(
                "Create a proposal to committee, which attempt to reset a specific contract's admin.");
        System.out.println(
                "\033[32m"
                        + "[Note]: this command is only available for governors of committee."
                        + "\033[m");
        ConsoleUtils.singleLine();
        System.out.println("Usage: resetAdminProposal newAdmin address");
        System.out.println(
                "newAdmin[Required] -- The address of admin account, it's length should be 40 in hex.");
        System.out.println(
                "address[Required] -- The address of a specific contract, it's length should be 40 in hex.");
    }

    public static void revokeProposalHelp() {
        System.out.println("Revoke a specific proposal from committee.");
        System.out.println(
                "\033[32m"
                        + "[Note]: this command is only available for proposal sender."
                        + "\033[m");
        ConsoleUtils.singleLine();
        System.out.println("Usage: revokeProposal proposalId");
        System.out.println(
                "proposalId[Required] -- The ID of a proposal, it should be larger than 0.");
    }

    public static void voteProposalHelp() {
        System.out.println("Vote a specific proposal from committee.");
        System.out.println(
                "\033[32m"
                        + "[Note]: this command is only available for  governors of committee."
                        + "\033[m");
        ConsoleUtils.singleLine();
        System.out.println("Usage: voteProposal proposalId agree");
        System.out.println(
                "proposalId[Required] -- The ID of a proposal, it should be larger than 0.");
        System.out.println("agree[Optional] -- Agree this proposal or not, default is true.");
    }

    public static void getProposalInfoHelp() {
        System.out.println("Get a specific proposal info from committee.");
        ConsoleUtils.singleLine();
        System.out.println("Usage: getProposalInfo proposalId");
        System.out.println(
                "proposalId[Required] -- The ID of a proposal, it should be larger than 0.");
    }

    public static void getCommitteeInfoHelp() {
        System.out.println("Get committee info.");
        System.out.println("Usage: getCommitteeInfo");
    }

    public static void getContractAdminHelp() {
        System.out.println("Get admin address from specific contract.");
        System.out.println("Usage: getContractAdmin address");
        System.out.println(
                "address[Required] -- The address of a specific contract, it's length should be 40 in hex.");
    }

    public static void getDeployAuthHelp() {
        System.out.println("Get deploy ACL strategy globally.");
        System.out.println("Usage: getDeployAuth");
    }

    public static void checkDeployAuthHelp() {
        System.out.println("Check whether account has deploy access.");
        System.out.println("Usage: checkDeployAuth [account]");
        System.out.println(
                "account[Optional] -- The address of a specific account. If you dont specify it, then check current account.");
    }

    public static void setMethodAuthHelp() {
        System.out.println("Set a method ACL type in specific contract.");
        System.out.println(
                "\033[32m"
                        + "[Note]: this command is only available for admin of contract."
                        + "\033[m");
        ConsoleUtils.singleLine();
        System.out.println("Usage: setMethodAuth contract selector authType");
        System.out.println(
                "contract[Required] -- The address of a specific contract, it's length should be 40 in hex.");
        System.out.println(
                "selector[Required] -- The method selector string, an interface of contract");
        System.out.println(
                "authType[Required] -- The ACL strategy of deploy, it should be 'white_list' or 'black_list'.");
        System.out.println(
                "    [example] setMethodAuth 0x1234567890123456789012345678901234567890 \"set(string)\" white_list");
    }

    public static void openMethodAuthHelp() {
        System.out.println("Open method ACL for account in specific contract.");
        System.out.println(
                "\033[32m"
                        + "[Note]: this command is only available for admin of contract."
                        + "\033[m");
        ConsoleUtils.singleLine();
        System.out.println("Usage: openMethodAuth contract selector account");
        System.out.println(
                "contract[Required] -- The address of a specific contract, it's length should be 40 in hex.");
        System.out.println(
                "selector[Required] -- The method selector string, an interface of contract");
        System.out.println(
                "account[Required]  -- The address of a specific account, it's length should be 40 in hex.");
        System.out.println(
                "    [example] openMethodAuth 0x1234567890123456789012345678901234567890 \"set(string)\" 0x1234567890123456789012345678901234567890");
    }

    public static void closeMethodAuthHelp() {
        System.out.println("Close method ACL for account in specific contract.");
        System.out.println(
                "\033[32m"
                        + "[Note]: this command is only available for admin of contract."
                        + "\033[m");
        ConsoleUtils.singleLine();
        System.out.println("Usage: closeMethodAuth contract selector account");
        System.out.println(
                "contract[Required] -- The address of a specific contract, it's length should be 40 in hex.");
        System.out.println(
                "selector[Required] -- The method selector string, an interface of contract");
        System.out.println(
                "account[Required]  -- The address of a specific account, it's length should be 40 in hex.");
        System.out.println(
                "    [example] closeMethodAuth 0x1234567890123456789012345678901234567890 \"set(string)\" 0x1234567890123456789012345678901234567890");
    }

    public static void checkMethodAuthHelp() {
        System.out.println("Check method ACL for account in specific contract.");
        ConsoleUtils.singleLine();
        System.out.println("Usage: closeMethodAuth contract selector account");
        System.out.println(
                "contract[Required] -- The address of a specific contract, it's length should be 40 in hex.");
        System.out.println(
                "selector[Required] -- The method selector string, an interface of contract");
        System.out.println(
                "account[Optional]  -- The address of a specific account. If you dont specify it, then check current account.");
        System.out.println(
                "    [example] checkMethodAuth 0x1234567890123456789012345678901234567890 \"set(string)\" 0x1234567890123456789012345678901234567890");
    }
}
