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

public class SupportedCommand {
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
                    new ArrayList<String>(
                            Arrays.asList("-h", "-help", "--h", "--H", "--help", "-H", "h")),
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            printDescInfo();
                        }
                    });

    public static final CommandInfo GET_NODE_INFO =
            new CommandInfo(
                    "getNodeInfo",
                    "Query the specified node information.",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.getNodeInfoHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getConsoleClientFace().getNodeInfo(params);
                        }
                    });
    public static final CommandInfo GET_DEPLOY_LOG =
            new CommandInfo(
                    "getDeployLog",
                    "Query the log of deployed contracts",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.getDeployLogHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getConsoleContractFace().getDeployLog(params);
                        }
                    });
    public static final CommandInfo SWITCH =
            new CommandInfo(
                    "switch",
                    "Switch to a specific group by group ID",
                    new ArrayList<String>(Arrays.asList("s")),
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.switchGroupIDHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.switchGroupID(params);
                        }
                    },
                    1,
                    1,
                    false);
    public static final CommandInfo SET_SYSTEMCONFIGBYKEY =
            new CommandInfo(
                    "setSystemConfigByKey",
                    "Set a system config value by key",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.setSystemConfigByKeyHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getPrecompiledFace().setSystemConfigByKey(params);
                        }
                    },
                    2,
                    2);

    public static final CommandInfo DEPLOY =
            new CommandInfo(
                    "deploy",
                    "Deploy a contract on blockchain",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.deployHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getConsoleContractFace().deploy(params);
                        }
                    },
                    1,
                    -1);
    public static final CommandInfo CALL =
            new CommandInfo(
                    "call",
                    "Call a contract by a function and parameters",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.callHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getConsoleContractFace().call(params);
                        }
                    },
                    3,
                    -1);
    public static final CommandInfo DEPLOY_BY_CNS =
            new CommandInfo(
                    "deployByCNS",
                    "Deploy a contract on blockchain by CNS",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.deployByCNSHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getConsoleContractFace().deployByCNS(params);
                        }
                    },
                    2,
                    -1);
    public static final CommandInfo CALL_BY_CNS =
            new CommandInfo(
                    "callByCNS",
                    "Call a contract by a function and parameters by CNS",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.callByCNSHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getConsoleContractFace().callByCNS(params);
                        }
                    },
                    2,
                    -1);
    public static final CommandInfo QUERY_CNS =
            new CommandInfo(
                    "queryCNS",
                    "Query CNS information by contract name and contract version",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.queryCNSHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getPrecompiledFace().queryCNS(params);
                        }
                    },
                    1,
                    2);
    public static final CommandInfo ADDOBSERVER =
            new CommandInfo(
                    "addObserver",
                    "Add an observer node",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.addObserverHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getPrecompiledFace().addObserver(params);
                        }
                    },
                    1,
                    1);
    public static final CommandInfo ADDSEALER =
            new CommandInfo(
                    "addSealer",
                    "Add a sealer node",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.addSealerHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getPrecompiledFace().addSealer(params);
                        }
                    },
                    1,
                    1);
    public static final CommandInfo REMOVENODE =
            new CommandInfo(
                    "removeNode",
                    "Remove a node",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.removeNodeHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getPrecompiledFace().removeNode(params);
                        }
                    },
                    1,
                    1);
    public static final CommandInfo QUITE =
            new CommandInfo(
                    "quit",
                    "Quit console",
                    new ArrayList<>(Arrays.asList("quit", "q", "exit")),
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            System.exit(0);
                        }
                    },
                    false);

    public static final CommandInfo DESC =
            new CommandInfo(
                    "desc",
                    "Description table information",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.showDescHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getPrecompiledFace().desc(params);
                        }
                    },
                    1,
                    1);
    public static final CommandInfo CREATE =
            new CommandInfo(
                    "create",
                    "Create table by sql",
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] inputDatas)
                                throws Exception {
                            consoleInitializer.getPrecompiledFace().createTable(inputDatas[0]);
                        }
                    });
    public static final CommandInfo SELECT =
            new CommandInfo(
                    "select",
                    "Select records by sql",
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] inputDatas)
                                throws Exception {
                            consoleInitializer.getPrecompiledFace().select(inputDatas[0]);
                        }
                    });
    public static final CommandInfo INSERT =
            new CommandInfo(
                    "insert",
                    "Insert records by sql",
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] inputDatas)
                                throws Exception {
                            consoleInitializer.getPrecompiledFace().insert(inputDatas[0]);
                        }
                    });
    public static final CommandInfo UPDATE =
            new CommandInfo(
                    "update",
                    "Update records by sql",
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] inputDatas)
                                throws Exception {
                            consoleInitializer.getPrecompiledFace().update(inputDatas[0]);
                        }
                    });
    public static final CommandInfo DELETE =
            new CommandInfo(
                    "delete",
                    "Remove records by sql",
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] inputDatas)
                                throws Exception {
                            consoleInitializer.getPrecompiledFace().remove(inputDatas[0]);
                        }
                    });
    public static final CommandInfo GET_CURRENT_ACCOUNT =
            new CommandInfo(
                    "getCurrentAccount",
                    "Get the current account info",
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            System.out.println(
                                    consoleInitializer
                                            .getClient()
                                            .getCryptoSuite()
                                            .getCryptoKeyPair()
                                            .getAddress());
                        }
                    });

    public static final CommandInfo GET_BLOCK_NUMBER =
            new CommandInfo(
                    "getBlockNumber",
                    "Query the number of most recent block",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.getBlockNumberHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getConsoleClientFace().getBlockNumber(params);
                        }
                    });
    public static final CommandInfo GET_PBFT_VIEW =
            new CommandInfo(
                    "getPbftView",
                    "Query the pbft view of node",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.getPbftViewHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getConsoleClientFace().getPbftView(params);
                        }
                    });
    public static final CommandInfo GET_SEALER_LIST =
            new CommandInfo(
                    "getSealerList",
                    "Query nodeId list for sealer nodes",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.getSealerListHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getConsoleClientFace().getSealerList(params);
                        }
                    });
    public static final CommandInfo GET_OBSERVER_LIST =
            new CommandInfo(
                    "getObserverList",
                    "Query nodeId list for observer nodes.",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.getObserverListHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getConsoleClientFace().getObserverList(params);
                        }
                    });
    public static final CommandInfo GET_SYNC_STATUS =
            new CommandInfo(
                    "getSyncStatus",
                    "Query sync status",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.getSyncStatusHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getConsoleClientFace().getSyncStatus(params);
                        }
                    });
    public static final CommandInfo GET_PEERS =
            new CommandInfo(
                    "getPeers",
                    "Query peers currently connected to the client",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.getPeersHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getConsoleClientFace().getPeers(params);
                        }
                    });

    public static final CommandInfo GET_BLOCK_BY_HASH =
            new CommandInfo(
                    "getBlockByHash",
                    "Query information about a block by hash",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.getBlockByHashHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getConsoleClientFace().getBlockByHash(params);
                        }
                    },
                    1,
                    2);

    public static final CommandInfo GET_BLOCK_BY_NUMBER =
            new CommandInfo(
                    "getBlockByNumber",
                    "Query information about a block by number",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.getBlockByNumberHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getConsoleClientFace().getBlockByNumber(params);
                        }
                    },
                    1,
                    2);

    public static final CommandInfo GET_BLOCKHEADER_BY_HASH =
            new CommandInfo(
                    "getBlockHeaderByHash",
                    "Query information about a block header by hash",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.getBlockHeaderByHashHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getConsoleClientFace().getBlockHeaderByHash(params);
                        }
                    },
                    1,
                    2);
    public static final CommandInfo GET_BLOCKHEADER_BY_NUMBER =
            new CommandInfo(
                    "getBlockHeaderByNumber",
                    "Query information about a block header by block number",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.getBlockHeaderByNumberHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer
                                    .getConsoleClientFace()
                                    .getBlockHeaderByNumber(params);
                        }
                    },
                    1,
                    2);

    public static final CommandInfo GET_TRANSACTION_BY_HASH =
            new CommandInfo(
                    "getTransactionByHash",
                    "Query information about a transaction requested by transaction hash",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.getTransactionByHashHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getConsoleClientFace().getTransactionByHash(params);
                        }
                    },
                    1,
                    2);

    public static final CommandInfo GET_TRANSACTION_BY_HASH_WITH_PROOF =
            new CommandInfo(
                    "getTransactionByHashWithProof",
                    "Query the transaction and transaction proof by transaction hash",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.getTransactionByHashWithProofHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer
                                    .getConsoleClientFace()
                                    .getTransactionByHashWithProof(params);
                        }
                    },
                    1,
                    2);
    public static final CommandInfo GET_TRANSACTION_RECEIPT_BY_HASH_WITH_PROOF =
            new CommandInfo(
                    "getTransactionReceiptByHashWithProof",
                    "Query the receipt and transaction receipt proof by transaction hash",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.getTransactionReceiptByHashWithProofHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer
                                    .getConsoleClientFace()
                                    .getTransactionReceiptByHashWithProof(params);
                        }
                    },
                    1,
                    2);

    public static final CommandInfo GET_PENDING_TX_SIZE =
            new CommandInfo(
                    "getPendingTxSize",
                    "Query pending transactions size",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.getPendingTxSizeHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getConsoleClientFace().getPendingTxSize(params);
                        }
                    });
    public static final CommandInfo GET_CODE =
            new CommandInfo(
                    "getCode",
                    "Query code at a given address",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.getCodeHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getConsoleClientFace().getCode(params);
                        }
                    },
                    1,
                    1);
    public static final CommandInfo GET_TOTAL_TRANSACTION_COUNT =
            new CommandInfo(
                    "getTotalTransactionCount",
                    "Query total transaction count",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.getTotalTransactionCountHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer
                                    .getConsoleClientFace()
                                    .getTotalTransactionCount(params);
                        }
                    });
    public static final CommandInfo GET_TRANSACTION_RECEIPT =
            new CommandInfo(
                    "getTransactionReceipt",
                    "Query the receipt of a transaction by transaction hash",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.getTransactionReceiptHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getConsoleClientFace().getTransactionReceipt(params);
                        }
                    },
                    1,
                    2);
    public static final CommandInfo GET_SYSTEM_CONFIG_BY_KEY =
            new CommandInfo(
                    "getSystemConfigByKey",
                    "Query a system config value by key",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.getSystemConfigByKeyHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getConsoleClientFace().getSystemConfigByKey(params);
                        }
                    },
                    1,
                    1);

    public static final CommandInfo LIST_DEPLOY_CONTRACT_ADDRESS =
            new CommandInfo(
                    "listDeployContractAddress",
                    "List the contractAddress for the specified contract",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.listDeployContractAddressHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            String contractNameOrPath = ConsoleUtils.resolveContractPath(params[1]);
                            String contractName = ConsoleUtils.getContractName(contractNameOrPath);
                            File contractFile =
                                    new File(
                                            ContractCompiler.COMPILED_PATH
                                                    + File.separator
                                                    + consoleInitializer.getClient().getGroupId()
                                                    + File.separator
                                                    + contractName);
                            int recordNum = 20;
                            if (params.length == 3) {
                                recordNum =
                                        ConsoleUtils.proccessNonNegativeNumber(
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
                        }
                    },
                    1,
                    2);

    public static final CommandInfo REGISTER_CNS =
            new CommandInfo(
                    "registerCNS",
                    "RegisterCNS information for the given contract",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.registerCNSHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getPrecompiledFace().registerCNS(params);
                        }
                    },
                    3,
                    3);

    public static final CommandInfo NEW_ACCOUNT =
            new CommandInfo(
                    "newAccount",
                    "Create account",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.newAccountHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getConsoleClientFace().newAccount(params);
                        }
                    },
                    0,
                    2);

    public static final CommandInfo LOAD_ACCOUNT =
            new CommandInfo(
                    "loadAccount",
                    "Load account for the transaction signature",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.loadAccountHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.loadAccount(params);
                        }
                    },
                    1,
                    2,
                    false);

    public static final CommandInfo LIST_ACCOUNT =
            new CommandInfo(
                    "listAccount",
                    "List the current saved account list",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            System.out.println("list all the accounts");
                            System.out.println("Usage: \nlistAccount");
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getConsoleClientFace().listAccount(params);
                        }
                    },
                    0,
                    0);

    public static final CommandInfo LIST_ABI =
            new CommandInfo(
                    "listAbi",
                    "List functions and events info of the contract.",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.listAbiHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getConsoleContractFace().listAbi(params);
                        }
                    },
                    1,
                    1);

    public static List<String> CRUD_COMMANDS =
            new ArrayList<String>(
                    Arrays.asList(
                            CREATE.getCommand(),
                            INSERT.getCommand(),
                            SELECT.getCommand(),
                            UPDATE.getCommand(),
                            DELETE.getCommand()));

    protected static Map<String, CommandInfo> commandToCommandInfo = new HashMap<>();

    static {
        Field[] fields = SupportedCommand.class.getDeclaredFields();
        for (Field field : fields) {
            if (field.getType().equals(CommandInfo.class)) {
                try {
                    CommandInfo constantCommandInfo = (CommandInfo) field.get(null);
                    commandToCommandInfo.put(constantCommandInfo.getCommand(), constantCommandInfo);
                    if (constantCommandInfo.getOptionCommand() != null) {
                        List<String> subCommandList = constantCommandInfo.getOptionCommand();
                        for (int i = 0; i < subCommandList.size(); i++) {
                            commandToCommandInfo.put(subCommandList.get(i), constantCommandInfo);
                        }
                    }
                } catch (IllegalAccessException e) {
                    continue;
                }
            }
        }
    }

    public static CommandInfo getCommandInfo(String command) {
        if (commandToCommandInfo.containsKey(command)) {
            return commandToCommandInfo.get(command);
        }
        return null;
    }

    public static List<String> getAllCommand() {
        List<String> commandList = new ArrayList<>();
        for (String command : commandToCommandInfo.keySet()) {
            commandList.add(command);
        }
        return commandList;
    }

    public static void printDescInfo() {
        Set<String> keys = commandToCommandInfo.keySet();
        List<String> commandList = new ArrayList<String>(keys);
        Collections.sort(commandList);
        List<String> outputtedCommand = new ArrayList<>();
        for (int i = 0; i < commandList.size(); i++) {
            CommandInfo commandInfo = commandToCommandInfo.get(commandList.get(i));
            if (outputtedCommand.contains(commandInfo.getCommand())) {
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
        for (int i = 0; i < commandList.size(); i++) {
            CommandInfo commandInfo = commandToCommandInfo.get(commandList.get(i));
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
