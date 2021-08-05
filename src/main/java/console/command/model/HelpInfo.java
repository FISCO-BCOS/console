package console.command.model;

import console.command.SupportedCommand;
import console.common.Common;
import org.fisco.bcos.sdk.model.PrecompiledConstant;

public class HelpInfo {
    public static void promptHelp(String command) {
        System.out.println("Try '" + command + " -h or --help' for more information.");
        System.out.println();
    }

    public static void printHelp(String func) {
        CommandInfo commandInfo = SupportedCommand.getCommandInfo(func);
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

    public static void switchGroupIDHelp() {
        System.out.println("Switch to a specific group by group ID.");
        System.out.println("Usage: \nswitch groupId");
        System.out.println("* groupId -- The ID of a group, " + Common.PositiveIntegerRange + ".");
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

    public static void getNodeInfoHelp() {
        System.out.println("Query the specified node information.");
        System.out.println("Usage: \ngetNodeInfo [NodeEndPoint]");
        System.out.println(
                "* NodeEndPoint -- The requested node information, the format is IP:Port");
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

    public static void deployHelp() {
        System.out.println("Deploy a contract on blockchain.");
        System.out.println("Usage: \ndeploy contractNameOrPath");
        System.out.println(
                "* contractNameOrPath -- The name of a contract or the path of a contract (Default load contract from the \"contracts/solidity\" path when using contractName).");
    }

    public static void callHelp() {
        System.out.println("Call a contract by a function and paramters.");
        System.out.println("Usage: \ncall contractNameOrPath contractAddress function parameters");
        System.out.println(
                "* contractNameOrPath -- The name of a contract or the path of a contract, when set to \"latest\", the contract address is the latest contract address (Default load contract from the \"contracts/solidity\" path when using contractName).");
        System.out.println("* contractAddress -- 20 Bytes - The address of a contract.");
        System.out.println("* function -- The function of a contract.");
        System.out.println("* parameters -- The parameters(splited by a space) of a function.");
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
                "* contractNameOrPath:contractVersion -- The name(or path) and version of a contract. If contract version is not provided, then the latest version of contract will be called.");
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
        System.out.println("Usage: \naddSealer nodeId");
        System.out.println("* nodeId -- The nodeId of a node.");
    }

    public static void removeNodeHelp() {
        System.out.println("Remove a node.");
        System.out.println("Usage: \nremoveNode nodeId");
        System.out.println("* nodeId -- The nodeId of a node.");
    }

    public static void grantUserTableManagerHelp() {
        System.out.println("Grant permission for user table by table name and address.");
        System.out.println("Usage: \ngrantUserTableManager tableName address");
        System.out.println("* tableName -- The name of a table.");
        System.out.println("* address -- 20 Bytes - The address of a tx.origin.");
    }

    public static void revokeUserTableManagerHelp() {
        System.out.println("Revoke permission for user table by table name and address.");
        System.out.println("Usage: \nrevokeUserTableManager tableName address");
        System.out.println("* tableName -- The name of a table.");
        System.out.println("* address -- 20 Bytes - The The address of a tx.origin.");
    }

    public static void listUserTableManagerHelp() {
        System.out.println("Query permission for user table information.");
        System.out.println("Usage: \nlistUserTableManager tableName");
        System.out.println("* tableName -- The name of a table.");
    }

    public static void grantDeployAndCreateManagerHelp() {
        System.out.println(
                "Grant permission for deploy contract and create user table by address.");
        System.out.println("Usage: \ngrantDeployAndCreateManager address");
        System.out.println("* address -- 20 Bytes - The address of a tx.origin.");
    }

    public static void revokeDeployAndCreateManagerHelp() {
        System.out.println(
                "Revoke permission for deploy contract and create user table by address.");
        System.out.println("Usage: \nrevokeDeployAndCreateManager address");
        System.out.println("* address -- 20 Bytes - The address of a tx.origin.");
    }

    public static void listDeployAndCreateManagerHelp() {
        System.out.println(
                "Query permission information for deploy contract and create user table.");
        System.out.println("Usage: \nlistDeployAndCreateManager");
    }

    public static void grantNodeManagerHelp() {
        System.out.println("Grant permission for node configuration by address.");
        System.out.println("Usage: \ngrantNodeManager address");
        System.out.println("* address -- 20 Bytes - The address of a tx.origin.");
    }

    public static void revokeNodeManagerHelp() {
        System.out.println("Revoke permission for node configuration by address.");
        System.out.println("Usage: \nrevokeNodeManager address");
        System.out.println("* address -- 20 Bytes - The address of a tx.origin.");
    }

    public static void listNodeManagerHelp() {
        System.out.println("Query permission information for node configuration.");
        System.out.println("Usage: \nlistNodeManager");
    }

    public static void grantCNSManagerHelp() {
        System.out.println("Grant permission for CNS by address.");
        System.out.println("Usage: \ngrantCNSManager address");
        System.out.println("* address -- 20 Bytes - The address of a tx.origin.");
    }

    public static void revokeCNSManagerHelp() {
        System.out.println("Revoke permission for CNS by address.");
        System.out.println("Usage: \nrevokeCNSManager address");
        System.out.println("* address -- 20 Bytes - The address of a tx.origin.");
    }

    public static void listCNSManagerHelp() {
        System.out.println("Query permission information for CNS.");
        System.out.println("Usage: \nlistCNSManager");
    }

    public static void grantSysConfigManagerHelp() {
        System.out.println("Grant permission for system configuration by address.");
        System.out.println("Usage: \ngrantSysConfigManager address");
        System.out.println("* address -- 20 Bytes - The address of a tx.origin.");
    }

    public static void revokeSysConfigManagerHelp() {
        System.out.println("Revoke permission for system configuration by address.");
        System.out.println("Usage: \nrevokeSysConfigManager address");
        System.out.println("* address -- 20 Bytes - The address of a tx.origin.");
    }

    public static void listSysConfigManagerHelp() {
        System.out.println("Query permission information for system configuration.");
        System.out.println("Usage: \nlistSysConfigManager");
    }

    public static void listContractWritePermissionHelp() {
        System.out.println("Query write permission information by address.");
        System.out.println("Usage: \nlistContractWritePermission");
        System.out.println("* address -- 20 Bytes - The address of a tx.origin.");
    }

    public static void grantContractWritePermissionHelp() {
        System.out.println("Grant write permission information by address.");
        System.out.println("Usage: \ngrantContractWritePermission");
        System.out.println("* contractAddress -- 20 Bytes - The address of a contract.");
        System.out.println("* userAddress -- 20 Bytes - The address of a tx.origin.");
    }

    public static void revokeContractWritePermissionHelp() {
        System.out.println("Revoke write permission information by address.");
        System.out.println("Usage: \nrevokeWritePermission");
        System.out.println("* contractAddress -- 20 Bytes - The address of a contract.");
        System.out.println("* userAddress -- 20 Bytes - The address of a tx.origin.");
    }

    public static void listCommitteeMembersHelp() {
        System.out.println("List committee members.");
        System.out.println("Usage: \nlistCommitteeMembers");
    }

    public static void grantCommitteeMemberHelp() {
        System.out.println("Grant committee member.");
        System.out.println("Usage: \ngrantCommitteeMember account");
        System.out.println("* account -- 20 Bytes - The address of a account.");
    }

    public static void revokeCommitteeMemberHelp() {
        System.out.println("Revoke committee member.");
        System.out.println("Usage: \nrevokeCommitteeMember account");
        System.out.println("* account -- 20 Bytes - The address of a account.");
    }

    public static void queryCommitteeMemberWeightHelp() {
        System.out.println("Query committee member weight.");
        System.out.println("Usage: \nqueryCommitteeMemberWeight account");
        System.out.println("* account -- 20 Bytes - The address of a account.");
    }

    public static void updateCommitteeMemberWeightHelp() {
        System.out.println("Update committee member weight.");
        System.out.println("Usage: \nupdateCommitteeMemberWeight account weight");
        System.out.println("* account -- 20 Bytes - The address of a account.");
        System.out.println("* weight -- int - The weight of the account.");
    }

    public static void queryThresholdHelp() {
        System.out.println("Query committee threshold.");
        System.out.println("Usage: \nqueryThreshold");
    }

    public static void updateThresholdHelp() {
        System.out.println("Update committee threshold.");
        System.out.println("Usage: \nupdateThreshold threshold");
        System.out.println("* threshold -- int - The threshold of the committee.");
    }

    public static void queryVotesOfMemberHelp() {
        System.out.println("Query votes of a committee member.");
        System.out.println("Usage: \nqueryVotesOfMember account");
        System.out.println("* account -- The account address of the committee member.");
    }

    public static void queryVotesOfThresholdHelp() {
        System.out.println("Query votes of the updateThreshold operation.");
        System.out.println("Usage: \nqueryVotesOfThreshold");
    }

    public static void listOperatorsHelp() {
        System.out.println("List operator members.");
        System.out.println("Usage: \nlistOperators");
    }

    public static void grantOperatorHelp() {
        System.out.println("Grant operator.");
        System.out.println("Usage: \ngrantOperator account");
        System.out.println("* account -- 20 Bytes - The address of a account.");
    }

    public static void revokeOperatorHelp() {
        System.out.println("Revoke operator.");
        System.out.println("Usage: \nrevokeOperator account");
        System.out.println("* account -- 20 Bytes - The address of a account.");
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
                "* key   -- The name of system config(tx_count_limit/tx_gas_limit/rpbft_epoch_block_num/rpbft_epoch_sealer_num/consensus_timeout supported currently).");
        System.out.println("* value -- The value of system config to be set.");
        System.out.println(
                "      -- The value of tx_count_limit "
                        + Common.PositiveIntegerRange
                        + "(default 1000).");
        System.out.println(
                "      -- the value of tx_gas_limit "
                        + Common.TxGasLimitRange
                        + "(default 300000000).");
        System.out.println(
                "      -- the value of  rpbft_epoch_block_num from 1 to 2147483647"
                        + "(default 1000).");
        System.out.println(
                "      -- the value of  rpbft_epoch_sealer_num " + Common.PositiveIntegerRange);
        System.out.println(
                "      -- the value of  consensus_timeout (seconds)"
                        + Common.ConsensusTimeoutRange
                        + "(default 3s).");
    }

    public static void getSystemConfigByKeyHelp() {
        System.out.println("Query a system config value by key.");
        System.out.println("Usage: \ngetSystemConfigByKey key");
        System.out.println(
                "* key -- The name of system config(tx_count_limit/tx_gas_limit/rpbft_epoch_block_num/rpbft_epoch_sealer_num/consensus_timeout supported currently).");
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
}
