/**
 * Copyright 2014-2020 [fisco-dev]
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package console.command;

import console.ConsoleInitializer;
import console.command.model.CommandInfo;
import console.command.model.HelpInfo;
import console.common.Common;
import console.common.ConsoleUtils;
import console.contract.utils.ContractCompiler;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SupportedCommand {
    protected static Map<String, CommandInfo> commandToCommandInfo = new HashMap<>();
    public static boolean isWasm = false;
    public static boolean isAuthOpen = false;

    public static void setIsAuthOpen(boolean authOpen) {
        isAuthOpen = authOpen;
    }

    public static void setIsWasm(boolean wasm) {
        isWasm = wasm;
        if (wasm) {
            SupportedCommand.getCommandInfo("deploy", true, isAuthOpen).setMinParamLength(3);
            SupportedCommand.getCommandInfo("call", true, isAuthOpen).setMinParamLength(2);
        } else {
            SupportedCommand.getCommandInfo("deploy", false, isAuthOpen).setMinParamLength(1);
            SupportedCommand.getCommandInfo("call", false, isAuthOpen).setMinParamLength(1);
        }
    }

    public static final CommandInfo HELP =
            new CommandInfo(
                    "help",
                    "Provide help information",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            printUsageInfo();
                        }
                    },
                    new ArrayList<>(
                            Arrays.asList("-h", "-help", "--h", "--H", "--help", "-H", "h")),
                    (consoleInitializer, params, pwd) -> printDescInfo(isWasm, isAuthOpen));

    public static final CommandInfo GET_DEPLOY_LOG =
            new CommandInfo(
                    "getDeployLog",
                    "Query the log of deployed contracts",
                    HelpInfo::getDeployLogHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleContractFace().getDeployLog(params),
                    -1,
                    -1,
                    true,
                    false);
    public static final CommandInfo SWITCH =
            new CommandInfo(
                    "switch",
                    "Switch to a specific group by name",
                    new ArrayList<String>(Arrays.asList("s")),
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.switchEndPointHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(
                                ConsoleInitializer consoleInitializer, String[] params, String pwd)
                                throws Exception {
                            consoleInitializer.switchGroup(params);
                        }
                    },
                    1,
                    1,
                    false);
    public static final CommandInfo SET_SYSTEM_CONFIG_BY_KEY =
            new CommandInfo(
                    "setSystemConfigByKey",
                    "Set a system config value by key",
                    HelpInfo::setSystemConfigByKeyHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getPrecompiledFace().setSystemConfigByKey(params),
                    2,
                    2);

    public static final CommandInfo DEPLOY =
            new CommandInfo(
                    "deploy",
                    "Deploy a contract on blockchain",
                    () -> HelpInfo.deployHelp(isWasm),
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleContractFace().deploy(params, pwd),
                    1,
                    -1);
    public static final CommandInfo CALL =
            new CommandInfo(
                    "call",
                    "Call a contract by a function and parameters",
                    () -> HelpInfo.callHelp(isWasm),
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleContractFace().call(params, pwd),
                    3,
                    -1);
    public static final CommandInfo DEPLOY_BY_CNS =
            new CommandInfo(
                    "deployByCNS",
                    "Deploy a contract on blockchain by CNS",
                    HelpInfo::deployByCNSHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleContractFace().deployByCNS(params),
                    2,
                    -1,
                    true,
                    true);
    public static final CommandInfo CALL_BY_CNS =
            new CommandInfo(
                    "callByCNS",
                    "Call a contract by a function and parameters by CNS",
                    () -> HelpInfo.callByCNSHelp(),
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleContractFace().callByCNS(params),
                    2,
                    -1,
                    true,
                    true);
    public static final CommandInfo QUERY_CNS =
            new CommandInfo(
                    "queryCNS",
                    "Query CNS information by contract name and contract version",
                    HelpInfo::queryCNSHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getPrecompiledFace().queryCNS(params),
                    1,
                    2,
                    true,
                    true);
    public static final CommandInfo ADD_OBSERVER =
            new CommandInfo(
                    "addObserver",
                    "Add an observer node",
                    HelpInfo::addObserverHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getPrecompiledFace().addObserver(params),
                    1,
                    1);
    public static final CommandInfo ADD_SEALER =
            new CommandInfo(
                    "addSealer",
                    "Add a sealer node",
                    HelpInfo::addSealerHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getPrecompiledFace().addSealer(params),
                    2,
                    2);
    public static final CommandInfo REMOVE_NODE =
            new CommandInfo(
                    "removeNode",
                    "Remove a node",
                    HelpInfo::removeNodeHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getPrecompiledFace().removeNode(params),
                    1,
                    1);

    public static final CommandInfo SET_CONSENSUS_WEIGHT =
            new CommandInfo(
                    "setConsensusWeight",
                    "Set consensus weight for the specified node",
                    HelpInfo::setConsensusWeightHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getPrecompiledFace().setConsensusNodeWeight(params),
                    2,
                    2);
    public static final CommandInfo QUITE =
            new CommandInfo(
                    "quit",
                    "Quit console",
                    new ArrayList<>(Arrays.asList("quit", "q", "exit")),
                    (consoleInitializer, params, pwd) -> System.exit(0),
                    false);
    // TODO: Table CRUD service is not supported in FISCO BCOS 3.0.0 rc1
    /*

    public static final CommandInfo DESC =
            new CommandInfo(
                    "desc",
                    "Description table information",
                    HelpInfo::showDescHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getPrecompiledFace().desc(params),
                    1,
                    1);
    public static final CommandInfo CREATE =
            new CommandInfo(
                    "create",
                    "Create table by sql",
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getPrecompiledFace().createTable(params[0], isWasm));

        public static final CommandInfo SELECT =
                new CommandInfo(
                        "select",
                        "Select records by sql",
                        (consoleInitializer, params, pwd) ->
                                consoleInitializer.getPrecompiledFace().select(params[0]));
        public static final CommandInfo INSERT =
                new CommandInfo(
                        "insert",
                        "Insert records by sql",
                        (consoleInitializer, params, pwd) ->
                                consoleInitializer.getPrecompiledFace().insert(params[0]));
        public static final CommandInfo UPDATE =
                new CommandInfo(
                        "update",
                        "Update records by sql",
                        (consoleInitializer, params, pwd) ->
                                consoleInitializer.getPrecompiledFace().update(params[0]));
        public static final CommandInfo DELETE =
                new CommandInfo(
                        "delete",
                        "Remove records by sql",
                        (consoleInitializer, params, pwd) ->
                                consoleInitializer.getPrecompiledFace().remove(params[0]));
     */

    // TODO: Liquid collaboration service is not supported in FISCO BCOS 3.0.0 rc1
    /*
    public static final CommandInfo INITIALIZE =
            new CommandInfo(
                    "initialize",
                    "Initialize a collaboration",
                    HelpInfo::initializeHelp,
                    (consoleInitializer, params, pwd) -> {
                        consoleInitializer.getCollaborationFace().initialize(params);
                    });

    public static final CommandInfo SIGN =
            new CommandInfo(
                    "sign",
                    "Sign a contract",
                    HelpInfo::signHelp,
                    (consoleInitializer, params, pwd) -> {
                        consoleInitializer.getCollaborationFace().sign(params);
                    });

    public static final CommandInfo EXERCISE =
            new CommandInfo(
                    "execute",
                    "Exercise an right of a contract",
                    HelpInfo::exerciseHelp,
                    (consoleInitializer, params, pwd) -> {
                        consoleInitializer.getCollaborationFace().exercise(params);
                    });

    public static final CommandInfo FETCH =
            new CommandInfo(
                    "fetch",
                    "Fetch a contract",
                    HelpInfo::fetchHelp,
                    (consoleInitializer, params, pwd) -> {
                        consoleInitializer.getCollaborationFace().fetch(params);
                    });
     */

    public static final CommandInfo GET_CURRENT_ACCOUNT =
            new CommandInfo(
                    "getCurrentAccount",
                    "Get the current account info",
                    (consoleInitializer, params, pwd) ->
                            System.out.println(
                                    consoleInitializer
                                            .getClient()
                                            .getCryptoSuite()
                                            .getCryptoKeyPair()
                                            .getAddress()));

    public static final CommandInfo GET_BLOCK_NUMBER =
            new CommandInfo(
                    "getBlockNumber",
                    "Query the number of most recent block",
                    HelpInfo::getBlockNumberHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleClientFace().getBlockNumber(params));
    public static final CommandInfo GET_PBFT_VIEW =
            new CommandInfo(
                    "getPbftView",
                    "Query the pbft view of node",
                    HelpInfo::getPbftViewHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleClientFace().getPbftView(params));
    public static final CommandInfo GET_SEALER_LIST =
            new CommandInfo(
                    "getSealerList",
                    "Query nodeId list for sealer nodes",
                    HelpInfo::getSealerListHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleClientFace().getSealerList(params));
    public static final CommandInfo GET_OBSERVER_LIST =
            new CommandInfo(
                    "getObserverList",
                    "Query nodeId list for observer nodes.",
                    HelpInfo::getObserverListHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleClientFace().getObserverList(params));
    public static final CommandInfo GET_SYNC_STATUS =
            new CommandInfo(
                    "getSyncStatus",
                    "Query sync status",
                    HelpInfo::getSyncStatusHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleClientFace().getSyncStatus(params));
    public static final CommandInfo GET_CONSENSUS_STATUS =
            new CommandInfo(
                    "getConsensusStatus",
                    "Query consensus status",
                    HelpInfo::getConsensusStatusHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleClientFace().getConsensusStatus(params));

    public static final CommandInfo GET_PEERS =
            new CommandInfo(
                    "getPeers",
                    "Query peers currently connected to the client",
                    HelpInfo::getPeersHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleClientFace().getPeers(params));

    public static final CommandInfo GET_BLOCK_BY_HASH =
            new CommandInfo(
                    "getBlockByHash",
                    "Query information about a block by hash",
                    HelpInfo::getBlockByHashHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleClientFace().getBlockByHash(params),
                    1,
                    2);

    public static final CommandInfo GET_BLOCK_BY_NUMBER =
            new CommandInfo(
                    "getBlockByNumber",
                    "Query information about a block by number",
                    HelpInfo::getBlockByNumberHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleClientFace().getBlockByNumber(params),
                    1,
                    2);

    public static final CommandInfo GET_BLOCKHEADER_BY_HASH =
            new CommandInfo(
                    "getBlockHeaderByHash",
                    "Query information about a block header by hash",
                    HelpInfo::getBlockHeaderByHashHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleClientFace().getBlockHeaderByHash(params),
                    1,
                    2);
    public static final CommandInfo GET_BLOCKHEADER_BY_NUMBER =
            new CommandInfo(
                    "getBlockHeaderByNumber",
                    "Query information about a block header by block number",
                    HelpInfo::getBlockHeaderByNumberHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer
                                    .getConsoleClientFace()
                                    .getBlockHeaderByNumber(params),
                    1,
                    2);

    public static final CommandInfo GET_TRANSACTION_BY_HASH =
            new CommandInfo(
                    "getTransactionByHash",
                    "Query information about a transaction requested by transaction hash",
                    HelpInfo::getTransactionByHashHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleClientFace().getTransactionByHash(params),
                    1,
                    2);

    public static final CommandInfo GET_TRANSACTION_BY_HASH_WITH_PROOF =
            new CommandInfo(
                    "getTransactionByHashWithProof",
                    "Query the transaction and transaction proof by transaction hash",
                    HelpInfo::getTransactionByHashWithProofHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer
                                    .getConsoleClientFace()
                                    .getTransactionByHashWithProof(params),
                    1,
                    2);
    public static final CommandInfo GET_TRANSACTION_RECEIPT_BY_HASH_WITH_PROOF =
            new CommandInfo(
                    "getTransactionReceiptByHashWithProof",
                    "Query the receipt and transaction receipt proof by transaction hash",
                    HelpInfo::getTransactionReceiptByHashWithProofHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer
                                    .getConsoleClientFace()
                                    .getTransactionReceiptByHashWithProof(params),
                    1,
                    2);

    public static final CommandInfo GET_PENDING_TX_SIZE =
            new CommandInfo(
                    "getPendingTxSize",
                    "Query pending transactions size",
                    HelpInfo::getPendingTxSizeHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleClientFace().getPendingTxSize(params));
    public static final CommandInfo GET_CODE =
            new CommandInfo(
                    "getCode",
                    "Query code at a given address",
                    HelpInfo::getCodeHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleClientFace().getCode(params),
                    1,
                    1);
    public static final CommandInfo GET_TOTAL_TRANSACTION_COUNT =
            new CommandInfo(
                    "getTotalTransactionCount",
                    "Query total transaction count",
                    HelpInfo::getTotalTransactionCountHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer
                                    .getConsoleClientFace()
                                    .getTotalTransactionCount(params));
    public static final CommandInfo GET_TRANSACTION_RECEIPT =
            new CommandInfo(
                    "getTransactionReceipt",
                    "Query the receipt of a transaction by transaction hash",
                    HelpInfo::getTransactionReceiptHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleClientFace().getTransactionReceipt(params),
                    1,
                    2);
    public static final CommandInfo GET_SYSTEM_CONFIG_BY_KEY =
            new CommandInfo(
                    "getSystemConfigByKey",
                    "Query a system config value by key",
                    HelpInfo::getSystemConfigByKeyHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleClientFace().getSystemConfigByKey(params),
                    1,
                    1);

    public static final CommandInfo LIST_DEPLOY_CONTRACT_ADDRESS =
            new CommandInfo(
                    "listDeployContractAddress",
                    "List the contractAddress for the specified contract",
                    HelpInfo::listDeployContractAddressHelp,
                    (consoleInitializer, params, pwd) -> {
                        String contractNameOrPath = ConsoleUtils.resolvePath(params[1]);
                        String contractName = ConsoleUtils.getContractName(contractNameOrPath);
                        File contractFile =
                                new File(
                                        ContractCompiler.COMPILED_PATH
                                                + File.separator
                                                + consoleInitializer.getClient().getGroup()
                                                + File.separator
                                                + contractName);
                        int recordNum = 20;
                        if (params.length == 3) {
                            recordNum =
                                    ConsoleUtils.processNonNegativeNumber(
                                            "recordNum", params[2], 1, Integer.MAX_VALUE);
                            if (recordNum == Common.InvalidReturnNumber) {
                                return;
                            }
                        }
                        if (!contractFile.exists()) {
                            System.out.println(
                                    "Contract \"" + contractName + "\" doesn't exist!\n");
                            return;
                        }
                        int i = 0;
                        File[] contractFileList = contractFile.listFiles();
                        if (contractFileList == null || contractFileList.length == 0) {
                            return;
                        }
                        ConsoleUtils.sortFiles(contractFileList);
                        for (File contractAddressFile : contractFileList) {
                            if (!ConsoleUtils.isValidAddress(contractAddressFile.getName())) {
                                continue;
                            }
                            System.out.printf(
                                    "%s  %s\n",
                                    contractAddressFile.getName(),
                                    ConsoleUtils.getFileCreationTime(contractAddressFile));
                            i++;
                            if (i == recordNum) {
                                break;
                            }
                        }
                    },
                    1,
                    2,
                    true,
                    false);

    public static final CommandInfo REGISTER_CNS =
            new CommandInfo(
                    "registerCNS",
                    "RegisterCNS information for the given contract",
                    HelpInfo::registerCNSHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getPrecompiledFace().registerCNS(params),
                    3,
                    3,
                    true,
                    true);

    public static final CommandInfo NEW_ACCOUNT =
            new CommandInfo(
                    "newAccount",
                    "Create account",
                    HelpInfo::newAccountHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleClientFace().newAccount(params),
                    0,
                    2);

    public static final CommandInfo LOAD_ACCOUNT =
            new CommandInfo(
                    "loadAccount",
                    "Load account for the transaction signature",
                    HelpInfo::loadAccountHelp,
                    (consoleInitializer, params, pwd) -> consoleInitializer.loadAccount(params),
                    1,
                    2,
                    false);

    public static final CommandInfo LIST_ACCOUNT =
            new CommandInfo(
                    "listAccount",
                    "List the current saved account list",
                    () -> {
                        System.out.println("list all the accounts");
                        System.out.println("Usage: \nlistAccount");
                    },
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleClientFace().listAccount(params),
                    0,
                    0);

    public static final CommandInfo GET_GROUP_LIST =
            new CommandInfo(
                    "getGroupList",
                    "List all group list",
                    () -> {
                        System.out.println("list all group list");
                        System.out.println("Usage: \nlistGroupList");
                    },
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleClientFace().getGroupList(params),
                    0,
                    0);

    public static final CommandInfo GET_GROUP_PEERS =
            new CommandInfo(
                    "getGroupPeers",
                    "List all group peers",
                    () -> {
                        System.out.println("list all group peers");
                        System.out.println("Usage: \ngetGroupPeers");
                    },
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleClientFace().getGroupPeers(params),
                    0,
                    0);

    public static final CommandInfo GET_GROUP_INFO =
            new CommandInfo(
                    "getGroupInfo",
                    "Query the current group information.",
                    () -> {
                        System.out.println("get the group info");
                        System.out.println("Usage: \ngetGroupInfo");
                    },
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleClientFace().getGroupInfo(params),
                    0,
                    0);

    public static final CommandInfo GET_GROUP_INFO_LIST =
            new CommandInfo(
                    "getGroupInfoList",
                    "Get all groups info",
                    () -> {
                        System.out.println("get all group info");
                        System.out.println("Usage: \ngetGroupInfoList");
                    },
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleClientFace().getGroupInfoList(params),
                    0,
                    0);
    public static final CommandInfo GET_GROUP_NODE_INFO =
            new CommandInfo(
                    "getGroupNodeInfo",
                    "Get group node info",
                    () -> {
                        System.out.println("get group node info");
                        System.out.println("Usage: \ngetGroupNodeInfo [nodeName]");
                    },
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleClientFace().getGroupNodeInfo(params),
                    1,
                    1);

    public static final CommandInfo LIST_ABI =
            new CommandInfo(
                    "listAbi",
                    "List functions and events info of the contract.",
                    HelpInfo::listAbiHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleContractFace().listAbi(params),
                    1,
                    1,
                    true,
                    false);

    public static final CommandInfo CHANGE_DIR =
            new CommandInfo(
                    "cd",
                    "Change dir to given path.",
                    HelpInfo::changeDirHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getPrecompiledFace().changeDir(params, pwd),
                    0,
                    1);
    public static final CommandInfo MAKE_DIR =
            new CommandInfo(
                    "mkdir",
                    "Create dir in given path.",
                    HelpInfo::makeDirHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getPrecompiledFace().makeDir(params, pwd),
                    1,
                    1);
    public static final CommandInfo LIST_DIR =
            new CommandInfo(
                    "ls",
                    "List resources in given path.",
                    HelpInfo::listDirHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getPrecompiledFace().listDir(params, pwd),
                    0,
                    1);

    public static final CommandInfo PWD =
            new CommandInfo(
                    "pwd",
                    "Show absolute path of working directory name",
                    HelpInfo::pwdHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getPrecompiledFace().pwd(pwd),
                    0,
                    0);

    public static final CommandInfo SET_NODE_NAME =
            new CommandInfo(
                    "setNodeName",
                    "Set default node name to send request.",
                    HelpInfo::setNodeNameHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleClientFace().setNodeName(params),
                    1,
                    1);

    public static final CommandInfo CLEAR_NODE_NAME =
            new CommandInfo(
                    "clearNodeName",
                    "Clear default node name to empty.",
                    HelpInfo::clearNodeNameHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleClientFace().clearNodeName(),
                    0,
                    0);

    public static final CommandInfo GET_NODE_NAME =
            new CommandInfo(
                    "getNodeName",
                    "Get default node name in this client.",
                    HelpInfo::getNodeNameHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleClientFace().getNodeName(),
                    0,
                    0);

    public static final CommandInfo UPDATE_PROPOSAL =
            new CommandInfo(
                    "updateGovernorProposal",
                    "Create a proposal to committee, which attempt to update a governor.",
                    HelpInfo::updateGovernorProposalHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getAuthFace().createUpdateGovernorProposal(params),
                    2,
                    2,
                    false,
                    false,
                    true);

    public static final CommandInfo SET_RATE_PROPOSAL =
            new CommandInfo(
                    "setRateProposal",
                    "Create a proposal to committee, which attempt to update committee vote rate.",
                    HelpInfo::setRateProposalHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getAuthFace().createSetRateProposal(params),
                    2,
                    2,
                    false,
                    false,
                    true);

    public static final CommandInfo SET_DEPLOY_AUTH_TYPE_PROPOSAL =
            new CommandInfo(
                    "setDeployAuthTypeProposal",
                    "Create a proposal to committee, which attempt to set deploy ACL type globally.",
                    HelpInfo::setDeployAuthTypeProposalHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer
                                    .getAuthFace()
                                    .createSetDeployAuthTypeProposal(params),
                    1,
                    1,
                    false,
                    false,
                    true);

    public static final CommandInfo OPEN_DEPLOY_ACL_PROPOSAL =
            new CommandInfo(
                    "openDeployAuthProposal",
                    "Create a proposal to committee, which attempt to open deploy ACL for specific account.",
                    HelpInfo::openDeployAuthProposalHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getAuthFace().createOpenDeployAuthProposal(params),
                    1,
                    1,
                    false,
                    false,
                    true);

    public static final CommandInfo CLOSE_DEPLOY_ACL_PROPOSAL =
            new CommandInfo(
                    "closeDeployAuthProposal",
                    "Create a proposal to committee, which attempt to close deploy ACL for specific account.",
                    HelpInfo::closeDeployAuthProposalHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getAuthFace().createCloseDeployAuthProposal(params),
                    1,
                    1,
                    false,
                    false,
                    true);

    public static final CommandInfo RESET_ADMIN_PROPOSAL =
            new CommandInfo(
                    "resetAdminProposal",
                    "Create a proposal to committee, which attempt to reset a specific contract's admin.",
                    HelpInfo::resetAdminProposalHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getAuthFace().createResetAdminProposal(params),
                    2,
                    2,
                    false,
                    false,
                    true);

    public static final CommandInfo REVOKE_PROPOSAL =
            new CommandInfo(
                    "revokeProposal",
                    "Revoke a specific proposal from committee.",
                    HelpInfo::revokeProposalHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getAuthFace().revokeProposal(params),
                    1,
                    1,
                    false,
                    false,
                    true);

    public static final CommandInfo VOTE_PROPOSAL =
            new CommandInfo(
                    "voteProposal",
                    "Vote a specific proposal to committee.",
                    HelpInfo::voteProposalHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getAuthFace().voteProposal(params),
                    1,
                    2,
                    false,
                    false,
                    true);

    public static final CommandInfo GET_PROPOSAL_INFO =
            new CommandInfo(
                    "getProposalInfo",
                    "Get a specific proposal info from committee.",
                    HelpInfo::getProposalInfoHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getAuthFace().getProposalInfo(params),
                    1,
                    1,
                    false,
                    false,
                    true);

    public static final CommandInfo GET_COMMITTEE_INFO =
            new CommandInfo(
                    "getCommitteeInfo",
                    "Get committee info.",
                    HelpInfo::getCommitteeInfoHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getAuthFace().getCommitteeInfo(params),
                    0,
                    0,
                    false,
                    false,
                    true);

    public static final CommandInfo GET_CONTRACT_ADMIN =
            new CommandInfo(
                    "getContractAdmin",
                    "Get admin address from specific contract.",
                    HelpInfo::getContractAdminHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getAuthFace().getContractAdmin(params),
                    1,
                    1,
                    false,
                    false,
                    true);

    public static final CommandInfo GET_DEPLOY_AUTH =
            new CommandInfo(
                    "getDeployAuth",
                    "Get deploy ACL strategy globally.",
                    HelpInfo::getDeployAuthHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getAuthFace().getDeployStrategy(params),
                    0,
                    0,
                    false,
                    false,
                    true);

    public static final CommandInfo CHECK_DEPLOY_AUTH =
            new CommandInfo(
                    "checkDeployAuth",
                    "Check whether account has deploy access.",
                    HelpInfo::checkDeployAuthHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer
                                    .getAuthFace()
                                    .checkDeployAuth(consoleInitializer, params),
                    0,
                    1,
                    false,
                    false,
                    true);

    public static final CommandInfo SET_METHOD_AUTH_TYPE =
            new CommandInfo(
                    "setMethodAuth",
                    "Set a method ACL type in specific contract.",
                    HelpInfo::setMethodAuthHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer
                                    .getAuthFace()
                                    .setMethodAuthType(consoleInitializer, params),
                    3,
                    3,
                    false,
                    false,
                    true);

    public static final CommandInfo OPEN_METHOD_AUTH =
            new CommandInfo(
                    "openMethodAuth",
                    "Open method ACL for account in specific contract.",
                    HelpInfo::openMethodAuthHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer
                                    .getAuthFace()
                                    .openMethodAuth(consoleInitializer, params),
                    3,
                    3,
                    false,
                    false,
                    true);

    public static final CommandInfo CLOSE_METHOD_AUTH =
            new CommandInfo(
                    "closeMethodAuth",
                    "Close method ACL for account in specific contract.",
                    HelpInfo::closeMethodAuthHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer
                                    .getAuthFace()
                                    .closeMethodAuth(consoleInitializer, params),
                    3,
                    3,
                    false,
                    false,
                    true);

    public static final CommandInfo CHECK_METHOD_AUTH =
            new CommandInfo(
                    "checkMethodAuth",
                    "Check method ACL for account in specific contract.",
                    HelpInfo::checkMethodAuthHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer
                                    .getAuthFace()
                                    .checkMethodAuth(consoleInitializer, params),
                    2,
                    3,
                    false,
                    false,
                    true);

    public static List<String> BFS_COMMANDS =
            new ArrayList<>(
                    Arrays.asList(
                            LIST_DIR.getCommand(),
                            CHANGE_DIR.getCommand(),
                            MAKE_DIR.getCommand(),
                            PWD.getCommand()));

    // TODO: Table CRUD service is not supported in FISCO BCOS 3.0.0 rc1
    public static List<String> CRUD_COMMANDS =
            new ArrayList<>(
                    Arrays.asList(
                            // CREATE.getCommand(),
                            // INSERT.getCommand(),
                            // SELECT.getCommand(),
                            // UPDATE.getCommand(),
                            // DELETE.getCommand()
                            ));

    // TODO: Liquid collaboration service is not supported in FISCO BCOS 3.0.0 rc1
    public static List<String> COLLABORATION_COMMANDS =
            new ArrayList<>(
                    Arrays.asList(
                            // INITIALIZE.getCommand(),
                            // SIGN.getCommand(),
                            // EXERCISE.getCommand(),
                            // FETCH.getCommand()
                            ));

    static {
        Field[] fields = SupportedCommand.class.getDeclaredFields();
        for (Field field : fields) {
            if (field.getType().equals(CommandInfo.class)) {
                try {
                    CommandInfo constantCommandInfo = (CommandInfo) field.get(null);
                    commandToCommandInfo.put(constantCommandInfo.getCommand(), constantCommandInfo);
                    if (constantCommandInfo.getOptionCommand() != null) {
                        List<String> subCommandList = constantCommandInfo.getOptionCommand();
                        for (String s : subCommandList) {
                            commandToCommandInfo.put(s, constantCommandInfo);
                        }
                    }
                } catch (IllegalAccessException ignored) {
                }
            }
        }
    }

    public static CommandInfo getCommandInfo(String command, boolean isWasm, boolean isAuthOpen) {
        if (commandToCommandInfo.containsKey(command)) {
            CommandInfo commandInfo = commandToCommandInfo.get(command);
            if (isWasm && !commandInfo.isWasmSupport()
                    || (!isAuthOpen && commandInfo.isNeedAuthOpen())) {
                return null;
            }
            return commandInfo;
        }
        return null;
    }

    public static List<String> getAllCommand(boolean isWasm, boolean isAuthOpen) {
        if (isWasm) {
            return commandToCommandInfo
                    .keySet()
                    .stream()
                    .filter((key) -> commandToCommandInfo.get(key).isWasmSupport())
                    .collect(Collectors.toList());
        } else {
            if (!isAuthOpen) {
                return commandToCommandInfo
                        .keySet()
                        .stream()
                        .filter((key) -> !commandToCommandInfo.get(key).isNeedAuthOpen())
                        .collect(Collectors.toList());
            }
            return new ArrayList<>(commandToCommandInfo.keySet());
        }
    }

    public static void printDescInfo(boolean isWasm, boolean isAuthOpen) {
        Set<String> keys = commandToCommandInfo.keySet();
        List<String> commandList = new ArrayList<>(keys);
        Collections.sort(commandList);
        List<String> outputtedCommand = new ArrayList<>();
        for (String s : commandList) {
            CommandInfo commandInfo = commandToCommandInfo.get(s);
            if (outputtedCommand.contains(commandInfo.getCommand())
                    || (isWasm && !commandInfo.isWasmSupport())
                    || (!isAuthOpen && commandInfo.isNeedAuthOpen())) {
                continue;
            }
            commandInfo.printDescInfo();
            outputtedCommand.add(commandInfo.getCommand());
        }
        ConsoleUtils.singleLine();
    }

    public static void printNonInteractiveDescInfo() {
        System.out.println("# bash console.sh [groupId] [Subcommand]");
        System.out.println(
                "# groupId(Optional): The groupId that  that received the request, default is 1");
        System.out.println(
                "# Subcommand[Required]: The command sent to the node, Please refer to the following for the list of subCommand");
        System.out.println(
                "use command \"bash console.sh [subCommand] -h\" to get the help of the subcommand.\n");
        System.out.println("# Subcommand list:");
        Set<String> keys = commandToCommandInfo.keySet();
        List<String> commandList = new ArrayList<String>(keys);
        Collections.sort(commandList);
        List<String> outputtedCommand = new ArrayList<>();
        for (String s : commandList) {
            CommandInfo commandInfo = commandToCommandInfo.get(s);
            if (outputtedCommand.contains(commandInfo.getCommand())) {
                continue;
            }
            if (commandInfo.isSupportNonInteractive()) {
                commandInfo.printDescInfo();
                outputtedCommand.add(commandInfo.getCommand());
            }
        }
        ConsoleUtils.singleLine();
    }
}
