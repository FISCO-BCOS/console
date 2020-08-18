package console;

import console.client.ConsoleClientFace;
import console.common.ConsoleExceptionUtils;
import console.common.ConsoleUtils;
import console.common.HelpInfo;
import console.common.JlineUtils;
import console.common.WelcomeInfo;
import console.contract.ConsoleContractFace;
import console.precompiled.PrecompiledFace;
import console.precompiled.permission.PermissionFace;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Scanner;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.client.exceptions.ClientException;
import org.fisco.bcos.sdk.utils.exceptions.MessageDecodingException;
import org.jline.keymap.KeyMap;
import org.jline.reader.Binding;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.Reference;
import org.jline.reader.UserInterruptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Console {

    private static final Logger logger = LoggerFactory.getLogger(Console.class);

    private static Client client;
    private static PrecompiledFace precompiledFace;
    private static PermissionFace permissionFace;
    private static ConsoleClientFace consoleClientFace;
    private static ConsoleContractFace consoleContractFace;

    public static int INPUT_FLAG = 0;

    @SuppressWarnings("resource")
    public static void main(String[] args) {

        LineReader lineReader = null;
        Scanner sc = null;
        ConsoleInitializer consoleInitializer = null;
        try {
            consoleInitializer = new ConsoleInitializer();
            consoleInitializer.init(args);
            client = consoleInitializer.getClient();
            precompiledFace = consoleInitializer.getPrecompiledFace();
            permissionFace = consoleInitializer.getPermissionFace();
            consoleClientFace = consoleInitializer.getConsoleClientFace();
            consoleContractFace = consoleInitializer.getConsoleContractFace();
            lineReader = JlineUtils.getLineReader();
            sc = new Scanner(System.in);
            KeyMap<Binding> keymap = lineReader.getKeyMaps().get(LineReader.MAIN);
            keymap.bind(new Reference("beginning-of-line"), "\033[1~");
            keymap.bind(new Reference("end-of-line"), "\033[4~");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            logger.error(" message: {}, e: {}", e.getMessage(), e);
            return;
        }

        WelcomeInfo.welcome();

        while (true) {

            try {
                if (lineReader == null) {
                    System.out.println("Console can not read commands.");
                    break;
                }
                String request = "";
                if (INPUT_FLAG == 0) {
                    request =
                            lineReader.readLine(
                                    "[group:" + consoleInitializer.getGroupID() + "]> ");
                } else {
                    System.out.print("[group:" + consoleInitializer.getGroupID() + "]> ");
                    sc = new Scanner(System.in);
                    request = sc.nextLine();
                }
                String[] params = null;
                params = ConsoleUtils.tokenizeCommand(request);
                if (params.length < 1) {
                    System.out.print("");
                    continue;
                }
                if ("".equals(params[0].trim())) {
                    System.out.print("");
                    continue;
                }
                if ("quit".equals(params[0]) || "q".equals(params[0]) || "exit".equals(params[0])) {
                    if (HelpInfo.promptNoParams(params, "q")) {
                        continue;
                    } else if (params.length > 2) {
                        HelpInfo.promptHelp("q");
                        continue;
                    }
                    consoleInitializer.stop();
                    break;
                }
                switch (params[0]) {
                    case "help":
                    case "h":
                        WelcomeInfo.help(params);
                        break;
                    case "deploy":
                        consoleContractFace.deploy(params);
                        break;
                    case "getDeployLog":
                        consoleContractFace.getDeployLog(params);
                        break;
                    case "call":
                        consoleContractFace.call(params);
                        break;
                    case "deployByCNS":
                        consoleContractFace.deployByCNS(params);
                        break;
                    case "callByCNS":
                        consoleContractFace.callByCNS(params);
                        break;
                        /*
                        case "queryCNS":
                            consoleContractFace.queryCNS(params);
                            break;*/
                    case "switch":
                    case "s":
                        consoleInitializer.switchGroupID(params);
                        break;
                    case "getBlockNumber":
                        consoleClientFace.getBlockNumber(params);
                        break;
                    case "getPbftView":
                        consoleClientFace.getPbftView(params);
                        break;
                    case "getSealerList":
                        consoleClientFace.getSealerList(params);
                        break;
                    case "getObserverList":
                        consoleClientFace.getObserverList(params);
                        break;
                    case "getConsensusStatus":
                        consoleClientFace.getConsensusStatus(params);
                        break;
                    case "getSyncStatus":
                        consoleClientFace.getSyncStatus(params);
                        break;
                    case "getNodeVersion":
                        consoleClientFace.getNodeVersion(params);
                        break;
                    case "getPeers":
                        consoleClientFace.getPeers(params);
                        break;
                    case "getNodeIDList":
                        consoleClientFace.getNodeIDList(params);
                        break;
                    case "getGroupPeers":
                        consoleClientFace.getGroupPeers(params);
                        break;
                    case "getGroupList":
                        consoleClientFace.getGroupList(params);
                        break;
                    case "getBlockByHash":
                        consoleClientFace.getBlockByHash(params);
                        break;
                    case "getBlockByNumber":
                        consoleClientFace.getBlockByNumber(params);
                        break;
                    case "getBlockHeaderByHash":
                        consoleClientFace.getBlockHeaderByHash(params);
                        break;
                    case "getBlockHeaderByNumber":
                        consoleClientFace.getBlockHeaderByNumber(params);
                        break;
                    case "getBlockHashByNumber":
                        consoleClientFace.getBlockHashByNumber(params);
                        break;
                    case "getTransactionByHash":
                        consoleClientFace.getTransactionByHash(params);
                        break;
                        /*
                        case "getTransactionByBlockHashAndIndex":
                            consoleClientFace.getTransactionByBlockHashAndIndex(params);
                            break;*/
                    case "getTransactionByBlockNumberAndIndex":
                        consoleClientFace.getTransactionByBlockNumberAndIndex(params);
                        break;
                    case "getTransactionReceipt":
                        consoleClientFace.getTransactionReceipt(params);
                        break;
                    case "getTransactionByHashWithProof":
                        consoleClientFace.getTransactionByHashWithProof(params);
                        break;
                    case "getTransactionReceiptByHashWithProof":
                        consoleClientFace.getTransactionReceiptByHashWithProof(params);
                        break;
                    case "getPendingTransactions":
                        consoleClientFace.getPendingTransactions(params);
                        break;
                    case "getPendingTxSize":
                        consoleClientFace.getPendingTxSize(params);
                        break;
                    case "getCode":
                        consoleClientFace.getCode(params);
                        break;
                    case "getTotalTransactionCount":
                        consoleClientFace.getTotalTransactionCount(params);
                        break;
                    case "getSystemConfigByKey":
                        consoleClientFace.getSystemConfigByKey(params);
                        break;
                    case "addSealer":
                        precompiledFace.addSealer(params);
                        break;
                    case "addObserver":
                        precompiledFace.addObserver(params);
                        break;
                    case "removeNode":
                        precompiledFace.removeNode(params);
                        break;
                    case "setSystemConfigByKey":
                        precompiledFace.setSystemConfigByKey(params);
                        break;
                    case "create":
                    case "CREATE":
                        precompiledFace.createTable(request);
                        break;
                    case "insert":
                    case "INSERT":
                        precompiledFace.insert(request);
                        break;
                    case "select":
                    case "SELECT":
                        precompiledFace.select(request);
                        break;
                    case "update":
                    case "UPDATE":
                        precompiledFace.update(request);
                        break;
                    case "delete":
                    case "DELETE":
                        precompiledFace.remove(request);
                        break;
                    case "desc":
                    case "DESC":
                        precompiledFace.desc(params);
                        break;
                    case "grantUserTableManager":
                        permissionFace.grantUserTableManager(params);
                        break;
                    case "revokeUserTableManager":
                        permissionFace.revokeUserTableManager(params);
                        break;
                    case "listUserTableManager":
                        permissionFace.listUserTableManager(params);
                        break;
                    case "grantDeployAndCreateManager":
                        permissionFace.grantDeployAndCreateManager(params);
                        break;
                    case "revokeDeployAndCreateManager":
                        permissionFace.revokeDeployAndCreateManager(params);
                        break;
                    case "listDeployAndCreateManager":
                        permissionFace.listDeployAndCreateManager(params);
                        break;
                    case "grantNodeManager":
                        permissionFace.grantNodeManager(params);
                        break;
                    case "revokeNodeManager":
                        permissionFace.revokeNodeManager(params);
                        break;
                    case "listNodeManager":
                        permissionFace.listNodeManager(params);
                        break;
                    case "grantCNSManager":
                        permissionFace.grantCNSManager(params);
                        break;
                    case "revokeCNSManager":
                        permissionFace.revokeCNSManager(params);
                        break;
                    case "listCNSManager":
                        permissionFace.listCNSManager(params);
                        break;
                    case "grantSysConfigManager":
                        permissionFace.grantSysConfigManager(params);
                        break;
                    case "revokeSysConfigManager":
                        permissionFace.revokeSysConfigManager(params);
                        break;
                    case "listSysConfigManager":
                        permissionFace.listSysConfigManager(params);
                        break;
                    case "listContractWritePermission":
                        permissionFace.listContractWritePermission(params);
                        break;
                    case "grantContractWritePermission":
                        permissionFace.grantContractWritePermission(params);
                        break;
                    case "revokeContractWritePermission":
                        permissionFace.revokeContractWritePermission(params);
                        break;
                    case "freezeContract":
                        precompiledFace.freezeContract(params);
                        break;
                    case "unfreezeContract":
                        precompiledFace.unfreezeContract(params);
                        break;
                    case "grantContractStatusManager":
                        precompiledFace.grantContractStatusManager(params);
                        break;
                    case "getContractStatus":
                        precompiledFace.getContractStatus(params);
                        break;
                    case "listContractStatusManager":
                        precompiledFace.listContractStatusManager(params);
                        break;
                    case "grantCommitteeMember":
                        permissionFace.grantCommitteeMember(params);
                        break;
                    case "revokeCommitteeMember":
                        permissionFace.revokeCommitteeMember(params);
                        break;
                    case "listCommitteeMembers":
                        permissionFace.listCommitteeMembers(params);
                        break;
                    case "grantOperator":
                        permissionFace.grantOperator(params);
                        break;
                    case "listOperators":
                        permissionFace.listOperators(params);
                        break;
                    case "revokeOperator":
                        permissionFace.revokeOperator(params);
                        break;
                    case "updateThreshold":
                        permissionFace.updateThreshold(params);
                        break;
                    case "updateCommitteeMemberWeight":
                        permissionFace.updateCommitteeMemberWeight(params);
                        break;
                    case "queryThreshold":
                        permissionFace.queryThreshold(params);
                        break;
                    case "queryCommitteeMemberWeight":
                        permissionFace.queryCommitteeMemberWeight(params);
                        break;
                    case "freezeAccount":
                        permissionFace.freezeAccount(params);
                        break;
                    case "unfreezeAccount":
                        permissionFace.unfreezeAccount(params);
                        break;
                    case "getAccountStatus":
                        permissionFace.getAccountStatus(params);
                        break;
                    default:
                        System.out.println(
                                "Undefined command: \"" + params[0] + "\". Try \"help\".\n");
                        break;
                }
            } catch (ClientException e) {
                ConsoleUtils.printJson(
                        "{\"code\":"
                                + e.getErrorCode()
                                + ", \"msg\":"
                                + "\""
                                + e.getErrorMessage()
                                + "\"}");
                System.out.println();
                logger.error(" message: {}, e: {}", e.getMessage(), e);
            } catch (ClassNotFoundException e) {
                System.out.println(e.getMessage() + " does not exist.");
                System.out.println();
            } catch (MessageDecodingException e) {
                if (e.getMessage().contains("\"status\":\"0x1a\"")) {
                    System.out.println("The contract address is incorrect.");
                    System.out.println();
                } else {
                    ConsoleExceptionUtils.pringMessageDecodeingException(e);
                }
            } catch (IOException e) {
                if (e.getMessage().startsWith("activeConnections")) {
                    System.out.println(
                            "Lost the connection to the node. "
                                    + "Please check the connection between the console and the node.");
                } else if (e.getMessage().startsWith("No value")) {
                    System.out.println(
                            "The groupID is not configured in dist/conf/applicationContext.xml file.");
                } else {
                    System.out.println(e.getMessage());
                    logger.error(" message: {}, e: {}", e.getMessage(), e);
                }
                System.out.println();
            } catch (InvocationTargetException e) {
                Throwable targetException = e.getTargetException();
                System.out.println(targetException.getMessage());
                System.out.println();
            } catch (UserInterruptException e) {
                consoleInitializer.stop();
            } catch (EndOfFileException e) {
                consoleInitializer.stop();
            } catch (RuntimeException e) {
                if (e.getCause() instanceof MessageDecodingException) {
                    ConsoleExceptionUtils.pringMessageDecodeingException(
                            new MessageDecodingException(e.getMessage()));
                } else {
                    System.out.println(e.getMessage());
                    System.out.println();
                    logger.error(" message: {}, e: {}", e.getMessage(), e);
                }

            } catch (Exception e) {
                System.out.println(e.getMessage());
                System.out.println();
                logger.error(" message: {}, e: {}", e.getMessage(), e);
            }
        }
    }

    public static void setClient(Client client) {
        Console.client = client;
    }

    public static void setPrecompiledFace(PrecompiledFace precompiledFace) {
        Console.precompiledFace = precompiledFace;
    }

    public static void setConsoleContractFace(ConsoleContractFace consoleContractFace) {
        Console.consoleContractFace = consoleContractFace;
    }
}
