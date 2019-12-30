package console;

import console.common.ConsoleExceptionUtils;
import console.common.ConsoleUtils;
import console.common.HelpInfo;
import console.common.JlineUtils;
import console.common.WelcomeInfo;
import console.contract.ContractFace;
import console.precompiled.PrecompiledFace;
import console.precompiled.permission.PermissionFace;
import console.web3j.Web3jFace;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Scanner;
import org.fisco.bcos.web3j.protocol.channel.ResponseExcepiton;
import org.fisco.bcos.web3j.protocol.exceptions.MessageDecodingException;
import org.jline.keymap.KeyMap;
import org.jline.reader.Binding;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.Reference;
import org.jline.reader.UserInterruptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleClient {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleClient.class);

    private static Web3jFace web3jFace;
    private static PrecompiledFace precompiledFace;
    private static PermissionFace permissionFace;
    private static ContractFace contractFace;

    public static int INPUT_FLAG = 0;

    @SuppressWarnings("resource")
    public static void main(String[] args) {

        LineReader lineReader = null;
        Scanner sc = null;
        ConsoleInitializer consoleInitializer = null;
        try {
            consoleInitializer = new ConsoleInitializer();
            consoleInitializer.init(args);
            web3jFace = consoleInitializer.getWeb3jFace();
            precompiledFace = consoleInitializer.getPrecompiledFace();
            permissionFace = consoleInitializer.getPermissionFace();
            contractFace = consoleInitializer.getContractFace();
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
                    consoleInitializer.close();
                    break;
                }
                switch (params[0]) {
                    case "help":
                    case "h":
                        WelcomeInfo.help(params);
                        break;
                    case "deploy":
                        contractFace.deploy(params);
                        break;
                    case "getDeployLog":
                        contractFace.getDeployLog(params);
                        break;
                    case "call":
                        contractFace.call(params);
                        break;
                    case "deployByCNS":
                        contractFace.deployByCNS(params);
                        break;
                    case "callByCNS":
                        contractFace.callByCNS(params);
                        break;
                    case "queryCNS":
                        contractFace.queryCNS(params);
                        break;
                    case "switch":
                    case "s":
                        consoleInitializer.switchGroupID(params);
                        break;
                    case "getBlockNumber":
                        web3jFace.getBlockNumber(params);
                        break;
                    case "getPbftView":
                        web3jFace.getPbftView(params);
                        break;
                    case "getSealerList":
                        web3jFace.getSealerList(params);
                        break;
                    case "getObserverList":
                        web3jFace.getObserverList(params);
                        break;
                    case "getConsensusStatus":
                        web3jFace.getConsensusStatus(params);
                        break;
                    case "getSyncStatus":
                        web3jFace.getSyncStatus(params);
                        break;
                    case "getNodeVersion":
                        web3jFace.getNodeVersion(params);
                        break;
                    case "getPeers":
                        web3jFace.getPeers(params);
                        break;
                    case "getNodeIDList":
                        web3jFace.getNodeIDList(params);
                        break;
                    case "getGroupPeers":
                        web3jFace.getGroupPeers(params);
                        break;
                    case "getGroupList":
                        web3jFace.getGroupList(params);
                        break;
                    case "getBlockByHash":
                        web3jFace.getBlockByHash(params);
                        break;
                    case "getBlockByNumber":
                        web3jFace.getBlockByNumber(params);
                        break;
                    case "getBlockHashByNumber":
                        web3jFace.getBlockHashByNumber(params);
                        break;
                    case "getTransactionByHash":
                        web3jFace.getTransactionByHash(params);
                        break;
                    case "getTransactionByBlockHashAndIndex":
                        web3jFace.getTransactionByBlockHashAndIndex(params);
                        break;
                    case "getTransactionByBlockNumberAndIndex":
                        web3jFace.getTransactionByBlockNumberAndIndex(params);
                        break;
                    case "getTransactionReceipt":
                        web3jFace.getTransactionReceipt(params);
                        break;
                    case "getPendingTransactions":
                        web3jFace.getPendingTransactions(params);
                        break;
                    case "getPendingTxSize":
                        web3jFace.getPendingTxSize(params);
                        break;
                    case "getCode":
                        web3jFace.getCode(params);
                        break;
                    case "getTotalTransactionCount":
                        web3jFace.getTotalTransactionCount(params);
                        break;
                    case "getSystemConfigByKey":
                        web3jFace.getSystemConfigByKey(params);
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
                    case "grantPermissionManager":
                        permissionFace.grantPermissionManager(params);
                        break;
                    case "revokePermissionManager":
                        permissionFace.revokePermissionManager(params);
                        break;
                    case "listPermissionManager":
                        permissionFace.listPermissionManager(params);
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
                    default:
                        System.out.println(
                                "Undefined command: \"" + params[0] + "\". Try \"help\".\n");
                        break;
                }
            } catch (ResponseExcepiton e) {
                ConsoleUtils.printJson(
                        "{\"code\":" + e.getCode() + ", \"msg\":" + "\"" + e.getMessage() + "\"}");
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
                consoleInitializer.close();
            } catch (EndOfFileException e) {
                consoleInitializer.close();
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

    public static void setWeb3jFace(Web3jFace web3jFace) {
        ConsoleClient.web3jFace = web3jFace;
    }

    public static void setPrecompiledFace(PrecompiledFace precompiledFace) {
        ConsoleClient.precompiledFace = precompiledFace;
    }

    public static void setContractFace(ContractFace contractFace) {
        ConsoleClient.contractFace = contractFace;
    }
}
