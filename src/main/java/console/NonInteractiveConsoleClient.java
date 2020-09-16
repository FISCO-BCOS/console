package console;

import console.account.AccountInterface;
import console.common.ConsoleExceptionUtils;
import console.common.ConsoleUtils;
import console.common.WelcomeInfo;
import console.contract.ContractFace;
import console.precompiled.PrecompiledFace;
import console.precompiled.permission.PermissionFace;
import console.web3j.Web3jFace;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import org.fisco.bcos.web3j.protocol.channel.ResponseExcepiton;
import org.fisco.bcos.web3j.protocol.exceptions.MessageDecodingException;
import org.jline.reader.EndOfFileException;
import org.jline.reader.UserInterruptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NonInteractiveConsoleClient {

    private static final Logger logger = LoggerFactory.getLogger(NonInteractiveConsoleClient.class);

    private static Web3jFace web3jFace;
    private static PrecompiledFace precompiledFace;
    private static PermissionFace permissionFace;
    private static ContractFace contractFace;
    private static AccountInterface accountInterface;

    @SuppressWarnings("resource")
    public static void main(String[] args) {

        if (args.length == 0 || args[0].equals("-h") || args[0].equals("--help")) {
            WelcomeInfo.help(args);
            System.exit(0);
        }

        ConsoleInitializer consoleInitializer = null;
        try {
            consoleInitializer = new ConsoleInitializer();
            consoleInitializer.init(new String[] {});
            web3jFace = consoleInitializer.getWeb3jFace();
            precompiledFace = consoleInitializer.getPrecompiledFace();
            permissionFace = consoleInitializer.getPermissionFace();
            contractFace = consoleInitializer.getContractFace();
            accountInterface = consoleInitializer.getAccountInterface();

        } catch (Exception e) {
            System.out.println(e.getMessage());
            logger.error(" message: {}, e: {}", e.getMessage(), e);
            return;
        }

        String params[] = args;

        try {
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
                case "listDeployContractAddress":
                    contractFace.listDeployContractAddress(params);
                    break;
                case "listAbi":
                    contractFace.listAbi(params);
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
                case "registerCNS":
                    contractFace.registerCNS(params);
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
                case "getBlockHeaderByHash":
                    web3jFace.getBlockHeaderByHash(params);
                    break;
                case "getBlockHeaderByNumber":
                    web3jFace.getBlockHeaderByNumber(params);
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
                case "getTransactionByHashWithProof":
                    web3jFace.getTransactionByHashWithProof(params);
                    break;
                case "getTransactionReceiptByHashWithProof":
                    web3jFace.getTransactionReceiptByHashWithProof(params);
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
                case "newAccount":
                    accountInterface.newAccount(params);
                    break;
                case "loadAccount":
                    accountInterface.loadAccount(params);
                    break;
                case "listAccount":
                    accountInterface.listAccount(params);
                    break;
                case "switchAccount":
                    accountInterface.switchAccount(params);
                    break;
                default:
                    System.out.println("Undefined command: \"" + params[0] + "\". Try \"help\".\n");
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
                ConsoleExceptionUtils.printMessageDecodingException(e);
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
                ConsoleExceptionUtils.printMessageDecodingException(
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
        } finally {
            System.exit(0);
        }
    }
}
