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
package console.common;

import console.ConsoleInitializer;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.crypto.CryptoInterface;

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
                    new ArrayList<String>(Arrays.asList("h", "-h", "-help", "--h", "--help", "-H")),
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            printDescInfo();
                        }
                    });

    public static final CommandInfo GET_NODE_VERSION =
            new CommandInfo(
                    "getNodeVersion",
                    "Query the current node version",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.getNodeVersionHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getConsoleClientFace().getNodeVersion(params);
                        }
                    });

    public static final CommandInfo LIST_DEPLOY_AND_CREATE_MANAGER =
            new CommandInfo(
                    "listDeployAndCreateManager",
                    "Query permission information for deploy contract and create user table",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.listDeployAndCreateManagerHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer
                                    .getPermissionFace()
                                    .listDeployAndCreateManager(params);
                        }
                    });

    public static final CommandInfo LIST_NODE_MANAGER =
            new CommandInfo(
                    "listNodeManager",
                    "Query permission information for node configuration",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.listNodeManagerHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getPermissionFace().listNodeManager(params);
                        }
                    });
    public static final CommandInfo LIST_CNS_MANAGER =
            new CommandInfo(
                    "listCNSManager",
                    "Query permission information for CNS",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.listCNSManagerHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getPermissionFace().listCNSManager(params);
                        }
                    });

    public static final CommandInfo LIST_SYSCONFIG_MANAGER =
            new CommandInfo(
                    "listSysConfigManager",
                    "Query permission information for system configuration",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.listSysConfigManagerHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getPermissionFace().listSysConfigManager(params);
                        }
                    });
    public static final CommandInfo LIST_CONTRACT_WRITE =
            new CommandInfo(
                    "listContractWritePermission",
                    "Query the account list which have write permission of the contract.",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.listContractWritePermissionHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer
                                    .getPermissionFace()
                                    .listContractWritePermission(params);
                        }
                    },
                    1,
                    1);

    public static final CommandInfo GRANT_CONTRACT_WRITE =
            new CommandInfo(
                    "grantContractWritePermission",
                    "Grant the account the contract write permission.",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.grantContractWritePermissionHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer
                                    .getPermissionFace()
                                    .grantContractWritePermission(params);
                        }
                    },
                    2,
                    2);
    public static final CommandInfo REVOKE_CONTRACT_WRITE =
            new CommandInfo(
                    "revokeContractWritePermission",
                    "Revoke the account the contract write permission",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.revokeContractWritePermissionHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer
                                    .getPermissionFace()
                                    .revokeContractWritePermission(params);
                        }
                    },
                    2,
                    2);
    public static final CommandInfo FREEZE_CONTRACT =
            new CommandInfo(
                    "freezeContract",
                    "Freeze the contract",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.freezeContractHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getPrecompiledFace().freezeContract(params);
                        }
                    },
                    1,
                    1);
    public static final CommandInfo UNFREEZE_CONTRACT =
            new CommandInfo(
                    "unfreezeContract",
                    "Unfreeze the contract",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.unfreezeContractHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getPrecompiledFace().unfreezeContract(params);
                        }
                    },
                    1,
                    1);
    public static final CommandInfo GRANT_CONTRACT_STATUS =
            new CommandInfo(
                    "grantContractStatusManager",
                    "Grant contract authorization to the user",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.grantContractStatusManagerHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer
                                    .getPrecompiledFace()
                                    .grantContractStatusManager(params);
                        }
                    },
                    2,
                    2);
    public static final CommandInfo LIST_CONTRACT_STATUS =
            new CommandInfo(
                    "listContractStatusManager",
                    "List the authorization of the contract",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.listContractStatusManagerHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer
                                    .getPrecompiledFace()
                                    .listContractStatusManager(params);
                        }
                    },
                    1,
                    1);

    public static final CommandInfo GET_CONTRACT_STATUS =
            new CommandInfo(
                    "getContractStatus",
                    "Get the status of the contract",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.getContractStatusHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getPrecompiledFace().getContractStatus(params);
                        }
                    },
                    1,
                    1);
    public static final CommandInfo LIST_COMMITTEE_MEMBERS =
            new CommandInfo(
                    "listCommitteeMembers",
                    "List all committee members",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.listCommitteeMembersHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getPermissionFace().listCommitteeMembers(params);
                        }
                    });

    public static final CommandInfo QUERY_THRESHOLD =
            new CommandInfo(
                    "queryThreshold",
                    "Query the threshold",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.queryThresholdHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getPermissionFace().queryThreshold(params);
                        }
                    });
    public static final CommandInfo LIST_OPERATORS =
            new CommandInfo(
                    "listOperators",
                    "List all operators",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.listOperatorsHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getPermissionFace().listOperators(params);
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
                    },
                    1,
                    1);
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
                    1);
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
    public static final CommandInfo GET_ACCOUNT_STATUS =
            new CommandInfo(
                    "getAccountStatus",
                    "GetAccountStatus of the account",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.getAccountStatusHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getPermissionFace().getAccountStatus(params);
                        }
                    },
                    1,
                    1);
    public static final CommandInfo UNFREEZE_ACCOUNT =
            new CommandInfo(
                    "unfreezeAccount",
                    "Unfreeze the account",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.unfreezeAccountHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getPermissionFace().unfreezeAccount(params);
                        }
                    },
                    1,
                    1);
    public static final CommandInfo FREEZE_ACCOUNT =
            new CommandInfo(
                    "freezeAccount",
                    "Freeze the account",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.freezeAccountHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getPermissionFace().freezeAccount(params);
                        }
                    },
                    1,
                    1);
    public static final CommandInfo REVOKE_OPERATOR =
            new CommandInfo(
                    "revokeOperator",
                    "Revoke the operator",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.revokeOperatorHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getPermissionFace().revokeOperator(params);
                        }
                    },
                    1,
                    1);
    public static final CommandInfo GRANT_OPERATOR =
            new CommandInfo(
                    "grantOperator",
                    "Grant the account operator",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.grantOperatorHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getPermissionFace().grantOperator(params);
                        }
                    },
                    1,
                    1);
    public static final CommandInfo UPDATE_THRESHOLD =
            new CommandInfo(
                    "updateThreshold",
                    "Update the threshold",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.updateThresholdHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getPermissionFace().updateThreshold(params);
                        }
                    },
                    1,
                    1);
    public static final CommandInfo UPDATE_COMMITTEE_MEMBER_WEIGHT =
            new CommandInfo(
                    "updateCommitteeMemberWeight",
                    "Update the committee member weight",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.updateCommitteeMemberWeightHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer
                                    .getPermissionFace()
                                    .updateCommitteeMemberWeight(params);
                        }
                    },
                    2,
                    2);
    public static final CommandInfo QUERY_COMMMITTEE_MEMBER_WEIGHT =
            new CommandInfo(
                    "queryCommitteeMemberWeight",
                    "Query the committee member weight",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.queryCommitteeMemberWeightHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer
                                    .getPermissionFace()
                                    .queryCommitteeMemberWeight(params);
                        }
                    },
                    1,
                    1);
    public static final CommandInfo REVOKE_COMMMITTEE_MEMBER =
            new CommandInfo(
                    "revokeCommitteeMember",
                    "Revoke the account from committee member",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.revokeCommitteeMemberHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getPermissionFace().revokeCommitteeMember(params);
                        }
                    },
                    1,
                    1);
    public static final CommandInfo GRANT_COMMMITTEE_MEMBER =
            new CommandInfo(
                    "grantCommitteeMember",
                    "Grant the account committee member",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.grantCommitteeMemberHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getPermissionFace().grantCommitteeMember(params);
                        }
                    },
                    1,
                    1);
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
                    2,
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
                            // TODO: queryCNS
                        }
                    });
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
    public static final CommandInfo GRANT_USER_TABLE_MANAGER =
            new CommandInfo(
                    "grantUserTableManager",
                    "Grant permission for user table by table name and address",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.grantUserTableManagerHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getPermissionFace().grantUserTableManager(params);
                        }
                    },
                    2,
                    2);
    public static final CommandInfo REVOKE_USER_TABLE_MANAGER =
            new CommandInfo(
                    "revokeUserTableManager",
                    "Revoke permission for user table by table name and address",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.revokeUserTableManagerHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getPermissionFace().revokeUserTableManager(params);
                        }
                    },
                    2,
                    2);
    public static final CommandInfo LIST_USER_TABLE_MANAGER =
            new CommandInfo(
                    "listUserTableManager",
                    "Query permission for user table information",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.listUserTableManagerHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getPermissionFace().listUserTableManager(params);
                        }
                    },
                    1,
                    1);
    public static final CommandInfo GRANT_DEPLOY_AND_CREATE_MANAGER =
            new CommandInfo(
                    "grantDeployAndCreateManager",
                    "Grant permission for deploy contract and create user table by address",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.grantDeployAndCreateManagerHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer
                                    .getPermissionFace()
                                    .grantDeployAndCreateManager(params);
                        }
                    },
                    1,
                    1);
    public static final CommandInfo REVOKE_DEPLOY_AND_CREATE_MANAGER =
            new CommandInfo(
                    "revokeDeployAndCreateManager",
                    "Revoke permission for deploy contract and create user table by address",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.revokeDeployAndCreateManagerHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer
                                    .getPermissionFace()
                                    .revokeDeployAndCreateManager(params);
                        }
                    },
                    1,
                    1);
    public static final CommandInfo GRANT_NODE_MANAGER =
            new CommandInfo(
                    "grantNodeManager",
                    "Grant permission for node configuration by address",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.grantNodeManagerHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getPermissionFace().grantNodeManager(params);
                        }
                    },
                    1,
                    1);
    public static final CommandInfo REVOKE_NODE_MANAGER =
            new CommandInfo(
                    "revokeNodeManager",
                    "Revoke permission for node configuration by address",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.revokeNodeManagerHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getPermissionFace().revokeNodeManager(params);
                        }
                    },
                    1,
                    1);
    public static final CommandInfo GRANT_CNS_MANAGER =
            new CommandInfo(
                    "grantCNSManager",
                    "Grant permission for CNS by address",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.grantCNSManagerHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getPermissionFace().grantCNSManager(params);
                        }
                    },
                    1,
                    1);
    public static final CommandInfo REVOKE_CNS_MANAGER =
            new CommandInfo(
                    "revokeCNSManager",
                    "Revoke permission for CNS by address",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.revokeCNSManagerHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getPermissionFace().revokeCNSManager(params);
                        }
                    },
                    1,
                    1);
    public static final CommandInfo GRANT_SYSCONFIG_MANAGER =
            new CommandInfo(
                    "grantSysConfigManager",
                    "Grant permission for system configuration by address",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.grantSysConfigManagerHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getPermissionFace().grantSysConfigManager(params);
                        }
                    },
                    1,
                    1);
    public static final CommandInfo REVOKE_SYSCONFIG_MANAGER =
            new CommandInfo(
                    "revokeSysConfigManager",
                    "Revoke permission for system configuration by address",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.revokeSysConfigManagerHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getPermissionFace().revokeSysConfigManager(params);
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
                            consoleInitializer.stop();
                        }
                    });

    public static final CommandInfo DESC =
            new CommandInfo(
                    "desc",
                    " Description table information",
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
                                            .getCryptoInterface()
                                            .getCryptoKeyPair()
                                            .getAddress());
                            System.out.println();
                        }
                    });
    public static final CommandInfo GET_CRYPTO_TYPE =
            new CommandInfo(
                    "getCryptoType",
                    "Get the current crypto type",
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            Client client = consoleInitializer.getClient();
                            // get ledger cryptoType
                            int ledgerCryptoType =
                                    client.getCryptoInterface().getCryptoTypeConfig();
                            System.out.println(
                                    "ledger crypto type: "
                                            + (ledgerCryptoType == CryptoInterface.ECDSA_TYPE
                                                    ? "ECDSA"
                                                    : "SM"));

                            // get ssl cryptoType
                            int sslCryptoType = consoleInitializer.getBcosSDK().getSSLCryptoType();
                            System.out.println(
                                    "ssl crypto type: "
                                            + (sslCryptoType == CryptoInterface.ECDSA_TYPE
                                                    ? "ECDSA"
                                                    : "SM"));
                            System.out.println();
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
    public static final CommandInfo GET_CONSENSUS_STATUS =
            new CommandInfo(
                    "getConsensusStatus",
                    "Query consensus status",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.getConsensusStatusHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getConsoleClientFace().getConsensusStatus(params);
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
    public static final CommandInfo GET_NODEIDLIST =
            new CommandInfo(
                    "getNodeIDList",
                    "Query nodeId list for all connected nodes",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.getNodeIDListHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getConsoleClientFace().getNodeIDList(params);
                        }
                    });
    public static final CommandInfo GET_GROUP_PEERS =
            new CommandInfo(
                    "getGroupPeers",
                    "Query nodeId list for sealer and observer nodes",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.getGroupPeersHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getConsoleClientFace().getGroupPeers(params);
                        }
                    });
    public static final CommandInfo GET_GROUP_LIST =
            new CommandInfo(
                    "getGroupList",
                    "Query group list",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.getGroupListHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getConsoleClientFace().getGroupList(params);
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

    public static final CommandInfo GET_BLOCK_BY_Number =
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
    public static final CommandInfo GET_BLOCKHASH_BY_NUMBER =
            new CommandInfo(
                    "getBlockHashByNumber",
                    "Query block hash by block number",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.getBlockHashByNumberHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer.getConsoleClientFace().getBlockHashByNumber(params);
                        }
                    },
                    1,
                    1);
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
    public static final CommandInfo GET_TRANSACTION_BY_BLOCKHASH_AND_INDEX =
            new CommandInfo(
                    "getTransactionByBlockHashAndIndex",
                    "Query information about a transaction by block hash and transaction index position",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.getTransactionByBlockHashAndIndexHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer
                                    .getConsoleClientFace()
                                    .getTransactionByBlockHashAndIndex(params);
                        }
                    },
                    2,
                    3);
    public static final CommandInfo GET_TRANSACTION_BY_BLOCKNUMBER_AND_INDEX =
            new CommandInfo(
                    "getTransactionByBlockNumberAndIndex",
                    "Query information about a transaction by block number and transaction index position",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.getTransactionByBlockNumberAndIndexHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer
                                    .getConsoleClientFace()
                                    .getTransactionByBlockNumberAndIndex(params);
                        }
                    },
                    2,
                    3);
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
    public static final CommandInfo GET_PENDING_TRANSACTIONS =
            new CommandInfo(
                    "getPendingTransactions",
                    "Query pending transactions",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            HelpInfo.getPendingTransactionsHelp();
                        }
                    },
                    new CommandInfo.CommandImplement() {
                        @Override
                        public void call(ConsoleInitializer consoleInitializer, String[] params)
                                throws Exception {
                            consoleInitializer
                                    .getConsoleClientFace()
                                    .getPendingTransactions(params);
                        }
                    });
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
    public static final CommandInfo GET_TRANSACTIONRECEIPT =
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
        for (int i = 0; i < commandList.size(); i++) {
            commandToCommandInfo.get(commandList.get(i)).printDescInfo();
        }
    }
}
