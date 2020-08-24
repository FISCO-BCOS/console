package console.common;

import org.fisco.bcos.web3j.precompile.cns.CnsService;

public class HelpInfo {

    public static void promptHelp(String command) {
        System.out.println("Try '" + command + " -h or --help' for more information.");
        System.out.println();
    }

    public static boolean promptNoParams(String[] params, String funcName) {
        if (params.length == 2) {
            if ("-h".equals(params[1]) || "--help".equals(params[1])) {
                helpNoParams(funcName);
                return true;
            } else {
                promptHelp(funcName);
                return true;
            }
        } else if (params.length > 2) {
            promptHelp(funcName);
            return true;
        } else {
            return false;
        }
    }

    public static void helpNoParams(String func) {
        switch (func) {
            case "help":
            case "h":
                help();
                break;
            case "getBlockNumber":
                getBlockNumberHelp();
                break;
            case "getPbftView":
                getPbftViewHelp();
                break;
            case "getObserverList":
                getObserverListHelp();
                break;
            case "getSealerList":
                getSealerListHelp();
                break;
            case "getConsensusStatus":
                getConsensusStatusHelp();
                break;
            case "getSyncStatus":
                getSyncStatusHelp();
                break;
            case "getNodeVersion":
                getNodeVersionHelp();
                break;
            case "getPeers":
                getPeersHelp();
                break;
            case "getNodeIDList":
                getNodeIDListHelp();
                break;
            case "getGroupPeers":
                getGroupPeersHelp();
                break;
            case "getGroupList":
                getGroupListHelp();
                break;
            case "getPendingTransactions":
                getPendingTransactionsHelp();
                break;
            case "getPendingTxSize":
                getPendingTxSizeHelp();
                break;
            case "getTotalTransactionCount":
                getTotalTransactionCountHelp();
                break;
            case "listDeployAndCreateManager":
                listDeployAndCreateManagerHelp();
                break;
            case "listNodeManager":
                listNodeManagerHelp();
                break;
            case "listCNSManager":
                listCNSManagerHelp();
                break;
            case "listSysConfigManager":
                listSysConfigManagerHelp();
                break;
            case "listContractWritePermission":
                listContractWritePermissionHelp();
                break;
            case "grantContractWritePermission":
                grantContractWritePermissionHelp();
                break;
            case "revokeContractWritePermission":
                revokeContractWritePermissionHelp();
                break;
            case "freezeContract":
                freezeContractHelp();
                break;
            case "unfreezeContract":
                unfreezeContractHelp();
                break;
            case "grantContractStatusManager":
                grantContractStatusManagerHelp();
                break;
            case "listContractStatusManager":
                listContractStatusManagerHelp();
                break;
            case "getContractStatus":
                getContractStatusHelp();
                break;
            case "listCommitteeMembers":
                listCommitteeMembersHelp();
                break;
            case "queryThreshold":
                queryThresholdHelp();
                break;
            case "listOperators":
                listOperatorsHelp();
                break;
            case "listAbi":
                listAbiHelp();
            case "loadAccount":
                loadAccountHelp();
                break;
            case "switchAccount":
                switchAccountHelp();
                break;
            case "newAccount":
                newAccountHelp();
                break;
            case "listAccount":
                listAccountHelp();
                break;
            case "saveAccount":
                saveAccountHelp();
                break;
            case "quit":
            case "q":
                quitHelp();
                break;

            default:
                break;
        }
    }

    public static void help() {
        System.out.println("Provide help information.");
        System.out.println("Usage: help");
        System.out.println();
    }

    public static void freezeContractHelp() {
        System.out.println("Freeze the contract.");
        System.out.println("Usage: freezeContract contractAddress");
        System.out.println("contractAddress -- 20 Bytes - The address of a contract.");
        System.out.println();
    }

    public static void unfreezeContractHelp() {
        System.out.println("Unfreeze the contract.");
        System.out.println("Usage: unfreezeContract contractAddress");
        System.out.println("contractAddress -- 20 Bytes - The address of a contract.");
        System.out.println();
    }

    public static void grantContractStatusManagerHelp() {
        System.out.println("Grant contract authorization to the user.");
        System.out.println("Usage: grantContractStatusManager contractAddress userAddress");
        System.out.println("contractAddress -- 20 Bytes - The address of a contract.");
        System.out.println("userAddress -- 20 Bytes - The address of a tx.origin.");
        System.out.println();
    }

    public static void getContractStatusHelp() {
        System.out.println("Get the status of the contract.");
        System.out.println("Usage: getContractStatus contractAddress");
        System.out.println("contractAddress -- 20 Bytes - The address of a contract.");
        System.out.println();
    }

    public static void listContractStatusManagerHelp() {
        System.out.println("List the authorization of the contract.");
        System.out.println("Usage: listContractStatusManager contractAddress");
        System.out.println("contractAddress -- 20 Bytes - The address of a contract.");
        System.out.println();
    }

    public static void getDeployLogHelp() {
        System.out.println("Query the log of deployed contract.");
        System.out.println("Usage: getDeployLog [recordNumber]");
        System.out.println(
                "recordNumber -- (optional) The number of deployed contract records, "
                        + Common.DeployLogntegerRange
                        + "(default 20).");
        System.out.println();
    }

    public static void listDeployContractAddressHelp() {
        System.out.println("List the deployed addresses list of the specified contract.");
        System.out.println("Usage: listDeployContractAddress [contractName] [offset] [count]");
        System.out.println("contractName -- the contract name");
        System.out.println("offset -- (optional) the start index of results (default 0).");
        System.out.println("count -- (optional) the number of results (default 10).");
        System.out.println();
    }

    public static void listAbiHelp() {
        System.out.println("List functions and events info of the contract.");
        System.out.println("Usage: listAbi [contractPath] [contractName]");
        System.out.println(
                "contractPath -- The name or the path of a contract, if a name is specified, the contract should in the default directory: contracts/solidity ");
        System.out.println(
                "contractName -- (optional) the contract name, default the same with the contract file");
        System.out.println();
    }

    public static void getBlockNumberHelp() {
        System.out.println("Query the number of most recent block.");
        System.out.println("Usage: getBlockNumber");
        System.out.println();
    }

    public static void switchGroupIDHelp() {
        System.out.println("Switch to a specific group by group ID.");
        System.out.println("Usage: switch groupId ");
        System.out.println("groupId -- The ID of a group, " + Common.PositiveIntegerRange + ".");
        System.out.println();
    }

    public static void getPbftViewHelp() {
        System.out.println("Query the pbft view of node.");
        System.out.println("Usage: getPbftView");
        System.out.println();
    }

    public static void getObserverListHelp() {
        System.out.println("Query nodeId list for observer nodes.");
        System.out.println("Usage: getObserverList");
        System.out.println();
    }

    public static void getSealerListHelp() {
        System.out.println("Query nodeId list for sealer nodes.");
        System.out.println("Usage: getSealerList");
        System.out.println();
    }

    public static void getConsensusStatusHelp() {
        System.out.println("Query consensus status.");
        System.out.println("Usage: getConsensusStatus");
        System.out.println();
    }

    public static void getSyncStatusHelp() {
        System.out.println("Query sync status.");
        System.out.println("Usage: getSyncStatus");
        System.out.println();
    }

    public static void getNodeVersionHelp() {
        System.out.println("Query the current node version.");
        System.out.println("Usage: getNodeVersion");
        System.out.println();
    }

    public static void getPeersHelp() {
        System.out.println("Query peers currently connected to the client.");
        System.out.println("Usage: getPeers");
        System.out.println();
    }

    public static void getNodeIDListHelp() {
        System.out.println("Query nodeId list for all connected nodes.");
        System.out.println("Usage: getNodeIDList");
        System.out.println();
    }

    public static void getGroupPeersHelp() {
        System.out.println("Query nodeId list for sealer and observer nodes.");
        System.out.println("Usage: getGroupPeers");
        System.out.println();
    }

    public static void getGroupListHelp() {
        System.out.println("Query group list.");
        System.out.println("Usage: getGroupList");
        System.out.println();
    }

    public static void quitHelp() {
        System.out.println("Quit console.");
        System.out.println("Usage: quit or exit");
        System.out.println();
    }

    public static void getBlockByHashHelp() {
        System.out.println("Query information about a block by hash.");
        System.out.println("Usage: getBlockByHash blockHash [boolean]");
        System.out.println("blockHash -- 32 Bytes - The hash of a block.");
        System.out.println(
                "boolean -- (optional) If true it returns the full transaction objects, if false only the hashes of the transactions.");
        System.out.println();
    }

    public static void getBlockByNumberHelp() {
        System.out.println("Query information about a block by block number.");
        System.out.println("Usage: getBlockByNumber blockNumber [boolean]");
        System.out.println(
                "blockNumber -- Integer of a block number, "
                        + Common.NonNegativeIntegerRange
                        + ".");
        System.out.println(
                "boolean -- (optional) If true it returns the full transaction objects, if false only the hashes of the transactions.");
        System.out.println();
    }

    public static void getBlockHeaderByHashHelp() {
        System.out.println("Query information about a block header by hash.");
        System.out.println("Usage: getBlockHeaderByHash blockHash [boolean]");
        System.out.println("blockHash -- 32 Bytes - The hash of a block.");
        System.out.println(
                "boolean -- (optional) If true the signature list will also be returned.");
        System.out.println();
    }

    public static void getBlockHeaderByNumberHelp() {
        System.out.println("Query information about a block header by block number.");
        System.out.println("Usage: getBlockHeaderByNumber blockNumber [boolean]");
        System.out.println(
                "blockNumber -- Integer of a block number, "
                        + Common.NonNegativeIntegerRange
                        + ".");
        System.out.println("blockHash -- 32 Bytes - The hash of a block.");

        System.out.println(
                "boolean -- (optional) If true the signature list will also be returned.");
        System.out.println();
    }

    public static void getBlockHashByNumberHelp() {
        System.out.println("Query block hash by block number.");
        System.out.println("Usage: getBlockHashByNumber blockNumber");
        System.out.println(
                "blockNumber -- Integer of a block number, "
                        + Common.NonNegativeIntegerRange
                        + ".");
        System.out.println();
    }

    public static void getTransactionByHashHelp() {
        System.out.println("Query information about a transaction requested by transaction hash.");
        System.out.println("Usage: getTransactionByHash transactionHash [contractName]");
        System.out.println("transactionHash -- 32 Bytes - The hash of a transaction.");
        System.out.println(
                "[contractName] -- (optional) The name of a contract, which can be used to decode input when provided.");
        System.out.println();
    }

    public static void getTransactionByBlockHashAndIndexHelp() {
        System.out.println(
                "Query information about a transaction by block hash and transaction index position.");
        System.out.println(
                "Usage: getTransactionByBlockHashAndIndex blockHash index [contractName]");
        System.out.println("blockHash -- 32 Bytes - The hash of a block.");
        System.out.println(
                "index -- Integer of a transaction index, " + Common.NonNegativeIntegerRange + ".");
        System.out.println(
                "[contractName] -- (optional) The name of a contract, which can be used to decode input when provided.");
        System.out.println();
    }

    public static void getTransactionByHashWithProofHelp() {
        System.out.println(
                "Query information about the transaction and proof by transaction hash.");
        System.out.println("Usage: getTransactionByHashWithProof transactionHash");
        System.out.println("transactionHash -- 32 Bytes - The hash of a transaction.");
        System.out.println();
    }

    public static void getTransactionReceiptByHashWithProofHelp() {
        System.out.println(
                "Query information about the transaction receipt and proof by transaction hash.");
        System.out.println("Usage: getTransactionReceiptByHashWithProof transactionHash");
        System.out.println("transactionHash -- 32 Bytes - The hash of a transaction.");
        System.out.println();
    }

    public static void getTransactionByBlockNumberAndIndexHelp() {
        System.out.println(
                "Query information about a transaction by block number and transaction index position.");
        System.out.println(
                "Usage: getTransactionByBlockNumberAndIndex blockNumber index [contractName]");
        System.out.println(
                "blockNumber -- Integer of a block number, "
                        + Common.NonNegativeIntegerRange
                        + ".");
        System.out.println(
                "index -- Integer of a transaction index, " + Common.NonNegativeIntegerRange + ".");
        System.out.println(
                "[contractName] -- (optional) The name of a contract, which can be used to decode input when provided.");
        System.out.println();
    }

    public static void getTransactionReceiptHelp() {
        System.out.println("Query the receipt of a transaction by transaction hash.");
        System.out.println("Usage: getTransactionReceipt transactionHash [contractName]");
        System.out.println("transactionHash -- 32 Bytes - The hash of a transaction.");
        System.out.println(
                "[contractName] -- (optional) The name of a contract, which can be used to decode input, output and event log when provided.");
        System.out.println();
    }

    public static void getPendingTransactionsHelp() {
        System.out.println("Query pending transactions.");
        System.out.println("Usage: getPendingTransactions");
        System.out.println();
    }

    public static void getPendingTxSizeHelp() {
        System.out.println("Query pending transactions size.");
        System.out.println("Usage: getPendingTxSize");
        System.out.println();
    }

    public static void getCodeHelp() {
        System.out.println("Query code at a given address.");
        System.out.println("Usage: getCode address");
        System.out.println("address -- 20 Bytes - The address of a contract.");
        System.out.println();
    }

    public static void getTotalTransactionCountHelp() {
        System.out.println("Query total transaction count.");
        System.out.println("Usage: getTotalTransactionCount");
        System.out.println();
    }

    public static void deployHelp() {
        System.out.println("Deploy a contract on blockchain.");
        System.out.println("Usage: deploy [contractPath]");
        System.out.println(
                "contractPath -- The name or the path of a contract, if a name is specified, the contract should in the default directory: contracts/solidity ");
        System.out.println();
    }

    public static void callHelp() {
        System.out.println("Call a contract by a function and parameters.");
        System.out.println("Usage: call contractPath contractAddress function parameters");
        System.out.println(
                "contractPath -- The name or the path of a contract, if a name is specified, the contract should in the default directory: contracts/solidity ");
        System.out.println("contractAddress -- 20 Bytes - The address of a contract.");
        System.out.println("function -- The function of a contract.");
        System.out.println("parameters -- The parameters(splited by a space) of a function.");
        System.out.println();
    }

    public static void deployByCNSHelp() {
        System.out.println("Deploy a contract on blockchain by CNS.");
        System.out.println("Usage: deployByCNS contractPath contractVersion");
        System.out.println(
                "contractPath -- The name or the path of a contract, if a name is specified, the contract should in the default directory: contracts/solidity ");
        System.out.println(
                "contractVersion -- The version of a contract. The maximum length of the version hex string is "
                        + CnsService.MAX_VERSION_LENGTH
                        + ".");
        System.out.println();
    }

    public static void callByCNSHelp() {
        System.out.println("Call a contract by a function and parameters by CNS.");
        System.out.println("Usage: callByCNS contractName:contractVersion function parameters");
        System.out.println(
                "contractName:contractVersion -- The name and version of a contract. If contract version is not provided, then the latest version of contract will be called.");
        System.out.println("function -- The function of a contract.");
        System.out.println("parameters -- The parameters(splited by a space) of a function.");
        System.out.println();
    }

    public static void queryCNSHelp() {
        System.out.println("Query CNS information by contract name and contract version.");
        System.out.println("Usage: queryCNS contractName contractVersion");
        System.out.println("contractName -- The name of a contract.");
        System.out.println(
                "contractVersion -- The version of a contract. The maximum length of the version hex string is "
                        + CnsService.MAX_VERSION_LENGTH
                        + ".");
        System.out.println();
    }

    public static void registerCNSHelp() {
        // // registerCNS contractAddress contractPath contractVersion
        System.out.println("Register contract info to CNS.");
        System.out.println("Usage: registerCNS contractPath contractAddress [contractVersion]");
        System.out.println(
                "contractPath -- The name or the path of a contract, if a name is specified, the contract should in the default directory: contracts/solidity ");
        System.out.println("contractAddress -- Address of the contract.");
        System.out.println(
                "contractVersion -- The version of a contract. The maximum length of the version hex string is "
                        + CnsService.MAX_VERSION_LENGTH
                        + ".");
        System.out.println();
    }

    public static void addObserverHelp() {
        System.out.println("Add an observer node.");
        System.out.println("Usage: addObserver nodeId");
        System.out.println(
                "nodeId -- The nodeId of a node. The length of the node hex string is "
                        + Common.NodeIdLength
                        + ".");
        System.out.println();
    }

    public static void addSealerHelp() {
        System.out.println("Add a sealer node.");
        System.out.println("Usage: addSealer nodeId");
        System.out.println(
                "nodeId -- The nodeId of a node. The length of the node hex string is "
                        + Common.NodeIdLength
                        + ".");
        System.out.println();
    }

    public static void removeNodeHelp() {
        System.out.println("Remove a node.");
        System.out.println("Usage: removeNode nodeId");
        System.out.println(
                "nodeId -- The nodeId of a node. The length of the node hex string is "
                        + Common.NodeIdLength
                        + ".");
        System.out.println();
    }

    public static void grantUserTableManagerHelp() {
        System.out.println("Grant permission for user table by table name and address.");
        System.out.println("Usage: grantUserTableManager tableName address");
        System.out.println("tableName -- The name of a table.");
        System.out.println("address -- 20 Bytes - The address of a tx.origin.");
        System.out.println();
    }

    public static void revokeUserTableManagerHelp() {
        System.out.println("Revoke permission for user table by table name and address.");
        System.out.println("Usage: revokeUserTableManager tableName address");
        System.out.println("tableName -- The name of a table.");
        System.out.println("address -- 20 Bytes - The The address of a tx.origin.");
        System.out.println();
    }

    public static void listUserTableManagerHelp() {
        System.out.println("Query permission for user table information.");
        System.out.println("Usage: listUserTableManager tableName");
        System.out.println("tableName -- The name of a table.");
        System.out.println();
    }

    public static void grantDeployAndCreateManagerHelp() {
        System.out.println(
                "Grant permission for deploy contract and create user table by address.");
        System.out.println("Usage: grantDeployAndCreateManager address");
        System.out.println("address -- 20 Bytes - The address of a tx.origin.");
        System.out.println();
    }

    public static void revokeDeployAndCreateManagerHelp() {
        System.out.println(
                "Revoke permission for deploy contract and create user table by address.");
        System.out.println("Usage: revokeDeployAndCreateManager address");
        System.out.println("address -- 20 Bytes - The address of a tx.origin.");
        System.out.println();
    }

    public static void listDeployAndCreateManagerHelp() {
        System.out.println(
                "Query permission information for deploy contract and create user table.");
        System.out.println("Usage: listDeployAndCreateManager");
        System.out.println();
    }

    public static void grantNodeManagerHelp() {
        System.out.println("Grant permission for node configuration by address.");
        System.out.println("Usage: grantNodeManager address");
        System.out.println("address -- 20 Bytes - The address of a tx.origin.");
        System.out.println();
    }

    public static void revokeNodeManagerHelp() {
        System.out.println("Revoke permission for node configuration by address.");
        System.out.println("Usage: revokeNodeManager address");
        System.out.println("address -- 20 Bytes - The address of a tx.origin.");
        System.out.println();
    }

    public static void listNodeManagerHelp() {
        System.out.println("Query permission information for node configuration.");
        System.out.println("Usage: listNodeManager");
        System.out.println();
    }

    public static void grantCNSManagerHelp() {
        System.out.println("Grant permission for CNS by address.");
        System.out.println("Usage: grantCNSManager address");
        System.out.println("address -- 20 Bytes - The address of a tx.origin.");
        System.out.println();
    }

    public static void revokeCNSManagerHelp() {
        System.out.println("Revoke permission for CNS by address.");
        System.out.println("Usage: revokeCNSManager address");
        System.out.println("address -- 20 Bytes - The address of a tx.origin.");
        System.out.println();
    }

    public static void listCNSManagerHelp() {
        System.out.println("Query permission information for CNS.");
        System.out.println("Usage: listCNSManager");
        System.out.println();
    }

    public static void grantSysConfigManagerHelp() {
        System.out.println("Grant permission for system configuration by address.");
        System.out.println("Usage: grantSysConfigManager address");
        System.out.println("address -- 20 Bytes - The address of a tx.origin.");
        System.out.println();
    }

    public static void revokeSysConfigManagerHelp() {
        System.out.println("Revoke permission for system configuration by address.");
        System.out.println("Usage: revokeSysConfigManager address");
        System.out.println("address -- 20 Bytes - The address of a tx.origin.");
        System.out.println();
    }

    public static void listSysConfigManagerHelp() {
        System.out.println("Query permission information for system configuration.");
        System.out.println("Usage: listSysConfigManager");
        System.out.println();
    }

    public static void listContractWritePermissionHelp() {
        System.out.println("Query write permission information by address.");
        System.out.println("Usage: listContractWritePermission");
        System.out.println("address -- 20 Bytes - The address of a tx.origin.");
        System.out.println();
    }

    public static void grantContractWritePermissionHelp() {
        System.out.println("Grant write permission information by address.");
        System.out.println("Usage: grantContractWritePermission");
        System.out.println("contractAddress -- 20 Bytes - The address of a contract.");
        System.out.println("userAddress -- 20 Bytes - The address of a tx.origin.");
        System.out.println();
    }

    public static void revokeContractWritePermissionHelp() {
        System.out.println("Revoke write permission information by address.");
        System.out.println("Usage: remokeWritePermission");
        System.out.println("contractAddress -- 20 Bytes - The address of a contract.");
        System.out.println("userAddress -- 20 Bytes - The address of a tx.origin.");
        System.out.println();
    }

    public static void listCommitteeMembersHelp() {
        System.out.println("List committee members.");
        System.out.println("Usage: listCommitteeMembers");
        System.out.println();
    }

    public static void grantCommitteeMemberHelp() {
        System.out.println("Grant committee member.");
        System.out.println("Usage: grantCommitteeMember account");
        System.out.println("account -- 20 Bytes - The address of a account.");
        System.out.println();
    }

    public static void revokeCommitteeMemberHelp() {
        System.out.println("Revoke committee member.");
        System.out.println("Usage: revokeCommitteeMember account");
        System.out.println("account -- 20 Bytes - The address of a account.");
        System.out.println();
    }

    public static void queryCommitteeMemberWeightHelp() {
        System.out.println("Query committee member weight.");
        System.out.println("Usage: queryCommitteeMemberWeight account");
        System.out.println("account -- 20 Bytes - The address of a account.");
        System.out.println();
    }

    public static void updateCommitteeMemberWeightHelp() {
        System.out.println("Update committee member weight.");
        System.out.println("Usage: updateCommitteeMemberWeight account weight");
        System.out.println("account -- 20 Bytes - The address of a account.");
        System.out.println("weight -- int - The weight of the account.");
        System.out.println();
    }

    public static void queryThresholdHelp() {
        System.out.println("Query committee threshold.");
        System.out.println("Usage: queryThreshold");
        System.out.println();
    }

    public static void updateThresholdHelp() {
        System.out.println("Update committee threshold.");
        System.out.println("Usage: updateThreshold threshold");
        System.out.println("threshold -- int - The threshold of the committee.");
        System.out.println();
    }

    public static void listOperatorsHelp() {
        System.out.println("List operator members.");
        System.out.println("Usage: listOperators");
        System.out.println();
    }

    public static void grantOperatorHelp() {
        System.out.println("Grant operator.");
        System.out.println("Usage: grantOperator account");
        System.out.println("account -- 20 Bytes - The address of a account.");
        System.out.println();
    }

    public static void revokeOperatorHelp() {
        System.out.println("Revoke operator.");
        System.out.println("Usage: revokeOperator account");
        System.out.println("account -- 20 Bytes - The address of a account.");
        System.out.println();
    }

    public static void freezeAccountHelp() {
        System.out.println("Freeze account.");
        System.out.println("Usage: freezeAccount account");
        System.out.println("account -- 20 Bytes - The address of a account.");
        System.out.println();
    }

    public static void unfreezeAccountHelp() {
        System.out.println("Unfreeze account.");
        System.out.println("Usage: unfreezeAccount account");
        System.out.println("account -- 20 Bytes - The address of a account.");
        System.out.println();
    }

    public static void getAccountStatusHelp() {
        System.out.println("Account status.");
        System.out.println("Usage: getAccountStatus account");
        System.out.println("account -- 20 Bytes - The address of a account.");
        System.out.println();
    }

    public static void setSystemConfigByKeyHelp() {
        System.out.println("Set a system config.");
        System.out.println("Usage: setSystemConfigByKey key value");
        System.out.println(
                "key   -- The name of system config(tx_count_limit/tx_gas_limit/rpbft_epoch_block_num/rpbft_epoch_sealer_num/consensus_timeout supported currently).");
        System.out.println("value -- The value of system config to be set.");
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
        System.out.println();
    }

    public static void getSystemConfigByKeyHelp() {
        System.out.println("Query a system config value by key.");
        System.out.println("Usage: getSystemConfigByKey key");
        System.out.println(
                "key -- The name of system config(tx_count_limit/tx_gas_limit/rpbft_epoch_block_num/rpbft_epoch_sealer_num/consensus_timeout supported currently).");
        System.out.println();
    }

    public static void showDescHelp() {
        System.out.println("Description table information.");
        System.out.println("Usage: desc tableName");
        System.out.println("tableName -- The name of the table.");
        System.out.println();
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
        System.out.println();
    }

    public static void consoleHelp() {
        System.out.println("Usage: ");
        System.out.println("./console.sh -h");
        System.out.println("./console.sh -v");
    }

    public static void startHelp() {
        System.out.println("Please provide one of the following ways to start the console.");
        System.out.println("Usage: ");
        System.out.println("./start.sh");
        System.out.println("./start.sh groupID");
        System.out.println("./start.sh groupID -pem pemName");
        System.out.println("./start.sh groupID -p12 p12Name");
    }

    public static void newAccountHelp() {
        System.out.println("create a new account for the transaction signature");
        System.out.println("Usage: newAccount");
        System.out.println();
    }

    public static void loadAccountHelp() {
        System.out.println("load account for the transaction signature");
        System.out.println("Usage: loadAccount accountPath");
        System.out.println(
                "accountPath -- The path of the account private key file, support .pem and .p12 format file");
        System.out.println();
    }

    public static void switchAccountHelp() {
        System.out.println("switch account for the transaction signature");
        System.out.println("Usage: switchAccount accountAddress");
        System.out.println("accountAddress -- The address of the account to switch");
        System.out.println();
    }

    public static void saveAccountHelp() {
        System.out.println("save the account to the file");
        System.out.println("Usage: saveAccount accountAddress [saveDir]");
        System.out.println("accountAddress -- The address of the account to save");
        System.out.println(
                "saveDir -- (optional) The directory of the account file to save, default: accounts/");
        System.out.println();
    }

    public static void listAccountHelp() {
        System.out.println("list all the accounts");
        System.out.println("Usage: listAccount");
        System.out.println();
    }
}
