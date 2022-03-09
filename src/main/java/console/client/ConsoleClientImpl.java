package console.client;

import com.moandjiezana.toml.Toml;
import console.client.model.GenerateGroupParam;
import console.client.model.TotalTransactionCountResult;
import console.common.Address;
import console.common.Common;
import console.common.ConsoleUtils;
import console.contract.ConsoleContractImpl;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.client.exceptions.ClientException;
import org.fisco.bcos.sdk.client.protocol.model.JsonTransactionResponse;
import org.fisco.bcos.sdk.client.protocol.response.TotalTransactionCount;
import org.fisco.bcos.sdk.config.ConfigOption;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.model.CryptoType;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.model.TransactionReceiptStatus;
import org.fisco.bcos.sdk.utils.Numeric;
import org.fisco.bcos.sdk.utils.ObjectMapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleClientImpl implements ConsoleClientFace {
    private static final Logger logger = LoggerFactory.getLogger(ConsoleContractImpl.class);
    private Client client;

    public ConsoleClientImpl(Client client) {
        this.client = client;
    }

    @Override
    public void updateClient(Client client) {
        this.client = client;
    }

    @Override
    public void getNodeVersion(String[] params) throws IOException {
        ConsoleUtils.printJson(client.getNodeVersion().getNodeVersion().toString());
    }

    @Override
    public void getNodeInfo(String[] params) throws IOException {
        if (ConsoleUtils.checkEndPoint(params[1])) {
            ConsoleUtils.printJson(client.getNodeInfo(params[1]).getNodeInfo().toString());
        }
    }

    @Override
    public void getBlockNumber(String[] params) throws IOException {
        System.out.println(client.getBlockNumber().getBlockNumber());
    }

    @Override
    public void getPbftView(String[] params) throws IOException {
        System.out.println(client.getPbftView().getPbftView());
    }

    @Override
    public void getObserverList(String[] params) throws IOException {
        String observers = client.getObserverList().getObserverList().toString();
        if ("[]".equals(observers)) {
            System.out.println("[]");
        } else {
            ConsoleUtils.printJson(observers);
        }
    }

    @Override
    public void getSealerList(String[] params) throws IOException {
        String sealers = client.getSealerList().getSealerList().toString();
        if ("[]".equals(sealers)) {
            System.out.println("[]");
        } else {
            ConsoleUtils.printJson(sealers);
        }
    }

    @Override
    public void getConsensusStatus(String[] params) throws IOException {
        ConsoleUtils.printJson(client.getConsensusStatus().getConsensusStatus().toString());
    }

    @Override
    public void getSyncStatus(String[] params) throws IOException {
        ConsoleUtils.printJson(client.getSyncStatus().getSyncStatus().toString());
    }

    @Override
    public void getPeers(String[] params) throws IOException {
        if (params.length <= 1) {
            ConsoleUtils.printJson(client.getPeers().getPeers().toString());
        } else {
            if (ConsoleUtils.checkEndPoint(params[1])) {
                ConsoleUtils.printJson(client.getPeers(params[1]).getPeers().toString());
            }
        }
    }

    @Override
    public void getNodeIDList(String[] params) throws IOException {
        ConsoleUtils.printJson(client.getNodeIDList().getNodeIDList().toString());
    }

    @Override
    public void getGroupPeers(String[] params) throws IOException {
        if (params.length <= 1) {
            ConsoleUtils.printJson(client.getGroupPeers().getGroupPeers().toString());
        } else {
            if (ConsoleUtils.checkEndPoint(params[1])) {
                ConsoleUtils.printJson(client.getGroupPeers(params[1]).getGroupPeers().toString());
            }
        }
    }

    @Override
    public void getGroupList(String[] params) throws IOException {
        if (params.length <= 1) {
            System.out.println(client.getGroupList().getGroupList().toString());
        } else {
            if (ConsoleUtils.checkEndPoint(params[1])) {
                System.out.println(client.getGroupList(params[1]).getGroupList().toString());
            }
        }
    }

    @Override
    public void getBlockByHash(String[] params) throws IOException {
        String blockHash = params[1];
        if (ConsoleUtils.isInvalidHash(blockHash)) return;
        boolean flag = false;
        if (params.length == 3) {
            if ("true".equals(params[2])) {
                flag = true;
            } else if ("false".equals(params[2])) {
                flag = false;
            } else {
                System.out.println("Please provide true or false for the second parameter.");
                return;
            }
        }
        ConsoleUtils.printJson(client.getBlockByHash(blockHash, flag).getBlock().toString());
    }

    @Override
    public void getBlockByNumber(String[] params) throws IOException {
        String blockNumberStr = params[1];
        int blockNumber = ConsoleUtils.proccessNonNegativeNumber("blockNumber", blockNumberStr);
        if (blockNumber == Common.InvalidReturnNumber) {
            return;
        }
        boolean flag = false;
        if (params.length == 3) {
            if ("true".equals(params[2])) {
                flag = true;
            } else if ("false".equals(params[2])) {
                flag = false;
            } else {
                System.out.println("Please provide true or false for the second parameter.");
                return;
            }
        }
        ConsoleUtils.printJson(
                client.getBlockByNumber(BigInteger.valueOf(blockNumber), flag)
                        .getBlock()
                        .toString());
    }

    @Override
    public void getBlockHeaderByHash(String[] params) throws IOException {
        String blockHash = params[1];
        if (ConsoleUtils.isInvalidHash(blockHash)) return;

        boolean flag = false;
        if (params.length == 3) {
            if ("true".equals(params[2])) {
                flag = true;
            } else if ("false".equals(params[2])) {
                flag = false;
            } else {
                System.out.println("Please provide true or false for the second parameter.");
                return;
            }
        }
        ConsoleUtils.printJson(
                client.getBlockHeaderByHash(blockHash, flag).getBlockHeader().toString());
    }

    @Override
    public void getBlockHeaderByNumber(String[] params) throws IOException {
        String blockNumberStr = params[1];
        int blockNumber = ConsoleUtils.proccessNonNegativeNumber("blockNumber", blockNumberStr);
        if (blockNumber == Common.InvalidReturnNumber) {
            return;
        }
        boolean flag = false;
        if (params.length == 3) {
            if ("true".equals(params[2])) {
                flag = true;
            } else if ("false".equals(params[2])) {
                flag = false;
            } else {
                System.out.println("Please provide true or false for the second parameter.");
                return;
            }
        }
        ConsoleUtils.printJson(
                client.getBlockHeaderByNumber(BigInteger.valueOf(blockNumber), flag)
                        .getBlockHeader()
                        .toString());
    }

    @Override
    public void getBlockHashByNumber(String[] params) throws IOException {
        String blockNumberStr = params[1];
        int blockNumber = ConsoleUtils.proccessNonNegativeNumber("blockNumber", blockNumberStr);
        if (blockNumber == Common.InvalidReturnNumber) {
            return;
        }
        ConsoleUtils.printJson(
                client.getBlockHashByNumber(BigInteger.valueOf(blockNumber))
                        .getBlockHashByNumber()
                        .toString());
    }

    @Override
    public void getTransactionByHash(String[] params) {
        String transactionHash = params[1];
        if (ConsoleUtils.isInvalidHash(transactionHash)) return;
        JsonTransactionResponse transaction =
                client.getTransactionByHash(transactionHash).getTransaction().get();
        if (transaction == null) {
            System.out.println("This transaction hash doesn't exist.");
            return;
        }
        ConsoleUtils.printJson(transaction.toString());
    }

    @Override
    public void getTransactionByBlockHashAndIndex(String[] params) {
        String blockHash = params[1];
        if (ConsoleUtils.isInvalidHash(blockHash)) {
            return;
        }
        String indexStr = params[2];
        int index = ConsoleUtils.proccessNonNegativeNumber("index", indexStr);
        if (index == Common.InvalidReturnNumber) {
            return;
        }
        ConsoleUtils.printJson(
                client.getTransactionByBlockHashAndIndex(blockHash, BigInteger.valueOf(index))
                        .getTransaction()
                        .toString());
    }

    @Override
    public void getTransactionByBlockNumberAndIndex(String[] params) {
        try {
            String blockNumberStr = params[1];
            int blockNumber = ConsoleUtils.proccessNonNegativeNumber("blockNumber", blockNumberStr);
            if (blockNumber == Common.InvalidReturnNumber) {
                return;
            }
            String indexStr = params[2];
            int index = ConsoleUtils.proccessNonNegativeNumber("index", indexStr);
            if (index == Common.InvalidReturnNumber) {
                return;
            }
            String transactionJson =
                    client.getTransactionByBlockNumberAndIndex(
                                    BigInteger.valueOf(blockNumber), BigInteger.valueOf(index))
                            .getTransaction()
                            .toString();
            ConsoleUtils.printJson(transactionJson);
        } catch (ClientException e) {
            ConsoleUtils.printJson(
                    "{\"code\":"
                            + e.getErrorCode()
                            + ", \"msg\":"
                            + "\""
                            + e.getErrorMessage()
                            + "\"}");
        }
    }

    @Override
    public void getTransactionReceipt(String[] params) throws Exception {
        String transactionHash = params[1];
        if (ConsoleUtils.isInvalidHash(transactionHash)) return;

        TransactionReceipt receipt =
                client.getTransactionReceipt(transactionHash).getTransactionReceipt().get();
        if (Objects.isNull(receipt) || Objects.isNull(receipt.getTransactionHash())) {
            System.out.println("This transaction hash doesn't exist.");
            return;
        }

        if (!receipt.isStatusOK()) {
            receipt.setMessage(
                    TransactionReceiptStatus.getStatusMessage(
                                    receipt.getStatus(), receipt.getMessage())
                            .getMessage());
        }
        ConsoleUtils.printJson(receipt.toString());
    }

    @Override
    public void getTransactionByHashWithProof(String[] params) throws Exception {
        String transactionHash = params[1];
        if (ConsoleUtils.isInvalidHash(transactionHash)) return;
        String transactionWithProof =
                client.getTransactionByHashWithProof(transactionHash).getResult().toString();

        if (Objects.isNull(transactionWithProof) || "".equals(transactionWithProof)) {
            System.out.println("This transaction hash doesn't exist.");
            return;
        }
        ConsoleUtils.printJson(transactionWithProof);
    }

    @Override
    public void getTransactionReceiptByHashWithProof(String[] params) throws Exception {
        String transactionHash = params[1];
        if (ConsoleUtils.isInvalidHash(transactionHash)) return;

        String transactionReceiptWithProof =
                client.getTransactionReceiptByHashWithProof(transactionHash).getResult().toString();

        if (Objects.isNull(transactionReceiptWithProof) || "".equals(transactionReceiptWithProof)) {
            System.out.println("This transaction hash doesn't exist.");
            return;
        }
        ConsoleUtils.printJson(transactionReceiptWithProof);
    }

    @Override
    public void getPendingTxSize(String[] params) throws IOException {
        String size = client.getPendingTxSize().getResult();
        System.out.println(Numeric.decodeQuantity(size));
    }

    @Override
    public void getPendingTransactions(String[] params) throws IOException {
        String pendingTransactions = client.getPendingTransaction().getResult().toString();
        if ("[]".equals(pendingTransactions)) System.out.println(pendingTransactions);
        else ConsoleUtils.printJson(pendingTransactions);
    }

    @Override
    public void getCode(String[] params) throws IOException {
        String address = params[1];
        Address convertAddr = ConsoleUtils.convertAddress(address);
        if (!convertAddr.isValid()) {
            return;
        }
        address = convertAddr.getAddress();
        String code = client.getCode(address).getCode();
        if ("0x".equals(code)) {
            System.out.println("This address doesn't exist.");
            return;
        }
        ConsoleUtils.printJson(code);
    }

    @Override
    public void getTotalTransactionCount(String[] params) throws IOException {

        TotalTransactionCount.TransactionCountInfo transactionCount =
                client.getTotalTransactionCount().getTotalTransactionCount();

        TotalTransactionCountResult innerTotalTransactionCountResult =
                new TotalTransactionCountResult();
        innerTotalTransactionCountResult.setBlockNumber(
                Numeric.decodeQuantity(transactionCount.getBlockNumber()));
        innerTotalTransactionCountResult.setTxSum(
                Numeric.decodeQuantity(transactionCount.getTxSum()));

        if (transactionCount.getFailedTxSum() != null) {
            innerTotalTransactionCountResult.setFailedTxSum(
                    Numeric.decodeQuantity(transactionCount.getFailedTxSum()));
        }

        ConsoleUtils.printJson(
                ObjectMapperFactory.getObjectMapper()
                        .writeValueAsString(innerTotalTransactionCountResult));
    }

    @Override
    public void getSystemConfigByKey(String[] params) throws Exception {
        String key = params[1];
        if (Common.TxCountLimit.equals(key)
                || Common.TxGasLimit.equals(key)
                || Common.RPBFTEpochSealerNum.equals(key)
                || Common.RPBFTEpochBlockNum.equals(key)
                || Common.ConsensusTimeout.equals(key)
                || Common.EnableGasChargeMgr.equals(key)) {
            String value = client.getSystemConfigByKey(key).getSystemConfig();
            if (Common.RPBFTEpochSealerNum.equals(key) || Common.RPBFTEpochBlockNum.equals(key)) {
                System.out.println("Note: " + key + " only takes effect when RPBFT is used!");
            }
            if (Common.EnableGasChargeMgr.equals("")) {
                System.out.println("off");
                return;
            }
            System.out.println(value);
        } else {
            System.out.println(
                    "Please provide a valid key, for example: "
                            + Common.TxCountLimit
                            + " or "
                            + Common.TxGasLimit
                            + ".");
        }
    }

    private Integer checkAndGetGroupId(String[] params) {
        Integer groupId = client.getGroupId();
        if (!ConsoleUtils.checkEndPoint(params[1])) {
            return null;
        }
        if (params.length >= 3) {
            groupId =
                    ConsoleUtils.proccessNonNegativeNumber(
                            "groupId", params[2], 1, Common.MaxGroupID);
            if (groupId == Common.InvalidReturnNumber) {
                groupId = null;
            }
        }
        return groupId;
    }

    @Override
    public void startGroup(String[] params) {
        Integer groupId = checkAndGetGroupId(params);
        if (groupId == null) {
            return;
        }
        ConsoleUtils.printJson(client.startGroup(groupId, params[1]).getGroupStatus().toString());
    }

    @Override
    public void stopGroup(String[] params) {
        Integer groupId = checkAndGetGroupId(params);
        if (groupId == null) {
            return;
        }
        ConsoleUtils.printJson(client.stopGroup(groupId, params[1]).getGroupStatus().toString());
    }

    @Override
    public void removeGroup(String[] params) {
        Integer groupId = checkAndGetGroupId(params);
        if (groupId == null) {
            return;
        }
        ConsoleUtils.printJson(client.removeGroup(groupId, params[1]).getGroupStatus().toString());
    }

    @Override
    public void recoverGroup(String[] params) {
        Integer groupId = checkAndGetGroupId(params);
        if (groupId == null) {
            return;
        }
        ConsoleUtils.printJson(client.recoverGroup(groupId, params[1]).getGroupStatus().toString());
    }

    @Override
    public void queryGroupStatus(String[] params) {
        Integer groupId = checkAndGetGroupId(params);
        if (groupId == null) {
            return;
        }
        ConsoleUtils.printJson(
                client.queryGroupStatus(groupId, params[1]).getGroupStatus().toString());
    }

    @Override
    public void generateGroup(String[] params) {
        Integer groupId = checkAndGetGroupId(params);
        if (groupId == null) {
            return;
        }
        String targetNode = params[1];
        // check timestamp
        long timestamp = ConsoleUtils.processLong("timestamp", params[3], 0, Long.MAX_VALUE - 1);
        if (timestamp == Common.InvalidLongValue) {
            return;
        }
        boolean enableFreeStorage = false;
        int startIndex = 4;
        if (params[4].equals("true")) {
            enableFreeStorage = true;
            startIndex = 5;
        }
        if (params.equals("false")) {
            enableFreeStorage = false;
            startIndex = 5;
        }

        List<String> nodes = new ArrayList<>();
        for (int i = startIndex; i < params.length; i++) {
            nodes.add(params[i]);
        }
        logger.debug(
                "generate group, targetNode:{}, groupId: {}, timestamp: {}, enableFreeStorage: {}, sealers: {}",
                targetNode,
                groupId,
                timestamp,
                enableFreeStorage,
                nodes.toString());
        ConsoleUtils.printJson(
                client.generateGroup(groupId, timestamp, enableFreeStorage, nodes, targetNode)
                        .getGroupStatus()
                        .toString());
    }

    @Override
    public void generateGroupFromFile(String[] params) {
        String groupIdStr = params[2];
        String groupConfigFileStr = params[1];
        int groupId =
                ConsoleUtils.proccessNonNegativeNumber("groupId", groupIdStr, 1, Common.MaxGroupID);
        if (groupId == Common.InvalidReturnNumber) {
            return;
        }

        File groupConfigFile = new File(groupConfigFileStr);
        if (!groupConfigFile.exists()) {
            System.out.println(
                    "Please make sure the group configuration file "
                            + groupConfigFileStr
                            + " exists!");
            return;
        }
        if (groupConfigFile.isDirectory()) {
            System.out.println(
                    "Invalid group config file for \"" + groupConfigFileStr + "\" is a directory!");
            return;
        }
        GenerateGroupParam generateGroupParam =
                new Toml().read(groupConfigFile).to(GenerateGroupParam.class);
        // check generateGroupParam
        if (!generateGroupParam.checkGenerateGroupParam()) {
            return;
        }
        logger.debug(
                "generateGroupFromFile, peers: {}, sealerList: {}",
                generateGroupParam.getGroupPeerInfo().toString(),
                generateGroupParam.getSealerListInfo());
        if (generateGroupParam.getTimestamp() == Common.InvalidLongValue) {
            return;
        }
        for (String peer : generateGroupParam.getGroupPeerInfo()) {
            System.out.println("* Result of " + peer + ":");
            try {
                ConsoleUtils.printJson(
                        client.generateGroup(
                                        groupId,
                                        generateGroupParam.getTimestamp(),
                                        false,
                                        generateGroupParam.getSealerListInfo(),
                                        peer)
                                .getGroupStatus()
                                .toString());
            } catch (ClientException e) {
                ConsoleUtils.printJson(
                        "{\"code\":"
                                + e.getErrorCode()
                                + ", \"msg\":"
                                + "\""
                                + e.getErrorMessage()
                                + "\"}");
            }
        }
    }

    @Override
    public void newAccount(String[] params) {
        String accountFormat = "pem";
        if (params.length >= 2) {
            accountFormat = params[1];
        }
        if (!accountFormat.equals("pem") && !accountFormat.equals("p12")) {
            System.out.println(
                    "Invalid account format \""
                            + accountFormat
                            + "\" only support \"pem\" and \"p12\" now!");
            return;
        }
        String password = "";
        if (accountFormat.equals("p12") && params.length == 3) {
            password = params[2];
        }
        CryptoSuite cryptoSuite = client.getCryptoSuite();
        CryptoKeyPair cryptoKeyPair = cryptoSuite.getKeyPairFactory().generateKeyPair();
        cryptoSuite.setConfig(cryptoSuite.getConfig());
        if (accountFormat.equals("pem")) {
            // save the account
            cryptoKeyPair.storeKeyPairWithPemFormat();
            System.out.println("AccountPath: " + cryptoKeyPair.getPemKeyStoreFilePath());
        } else {
            cryptoKeyPair.storeKeyPairWithP12Format(password);
            System.out.println("AccountPath: " + cryptoKeyPair.getP12KeyStoreFilePath());
        }
        System.out.println(
                "Note: This operation does not create an account in the blockchain, but only creates a local account, and deploying a contract through this account will create an account in the blockchain");
        System.out.println("newAccount: " + cryptoKeyPair.getAddress());
        System.out.println(
                "AccountType: "
                        + (cryptoSuite.getCryptoTypeConfig() == CryptoType.ECDSA_TYPE
                                ? "ecdsa"
                                : "sm"));
    }

    @Override
    public void listAccount(String[] params) {
        List<String> accountList = listAccount(this.client);
        if (accountList.size() == 0) {
            System.out.println("Empty set");
            return;
        }
        String currentAccount = client.getCryptoSuite().getCryptoKeyPair().getAddress();
        System.out.println(currentAccount + "(current account) <=");
        for (int i = 0; i < accountList.size(); i++) {
            if (!accountList.get(i).equals(currentAccount)) {
                System.out.println(accountList.get(i));
            }
        }
    }

    public static String getAccountDir(Client client) {
        ConfigOption configOption = client.getCryptoSuite().getConfig();
        String subDir = CryptoKeyPair.ECDSA_ACCOUNT_SUBDIR;
        if (client.getCryptoSuite().getCryptoTypeConfig() == CryptoType.SM_TYPE) {
            subDir = CryptoKeyPair.GM_ACCOUNT_SUBDIR;
        }
        return configOption.getAccountConfig().getKeyStoreDir() + File.separator + subDir;
    }

    public static List<String> listAccount(Client client) {
        List<String> accountList = new ArrayList<>();
        File accountFile = new File(getAccountDir(client));
        if (!accountFile.exists()) {
            return accountList;
        }
        File[] accountFileList = accountFile.listFiles();
        if (accountFileList == null || accountFileList.length == 0) {
            return accountList;
        }
        ConsoleUtils.sortFiles(accountFileList);
        for (File accountFileItem : accountFileList) {
            logger.debug("account is: {}", accountFileItem.getName());
            // check the file format
            if (!accountFileItem.getName().endsWith(".pem")
                    && !accountFileItem.getName().endsWith(".p12")) {
                continue;
            }
            String[] accountAddressList = accountFileItem.getName().split("\\.");
            if (accountAddressList.length == 0) {
                continue;
            }
            // trim _gm
            String[] accountAddressListArray =
                    accountAddressList[0].split(ConsoleUtils.GM_ACCOUNT_POSTFIX);
            if (accountAddressListArray.length == 0) {
                continue;
            }
            String accountAddress = accountAddressListArray[0];
            if (!accountList.contains(accountAddress)) {
                accountList.add(accountAddress);
            }
        }
        return accountList;
    }

    @Override
    public void getBatchReceiptsByBlockHashAndRange(String[] params) {
        String from = "0";
        String count = "-1";
        // get groupId
        String blockHash = params[1];
        if (params.length > 2) {
            from = params[2];
        }
        if (params.length > 3) {
            count = params[3];
        }
        ConsoleUtils.printJson(
                client.getBatchReceiptsByBlockHashAndRange(blockHash, from, count)
                        .decodeTransactionReceiptsInfo()
                        .toString());
    }

    @Override
    public void getBatchReceiptsByBlockNumberAndRange(String[] params) {
        String from = "0";
        String count = "-1";
        // get groupId
        Integer blockNumber = ConsoleUtils.proccessNonNegativeNumber("blockNumber", params[1]);
        if (blockNumber == Common.InvalidReturnNumber) {
            return;
        }
        if (params.length > 2) {
            from = params[2];
        }
        if (params.length > 3) {
            count = params[3];
        }
        ConsoleUtils.printJson(
                client.getBatchReceiptsByBlockNumberAndRange(
                                BigInteger.valueOf(blockNumber), from, count)
                        .decodeTransactionReceiptsInfo()
                        .toString());
    }
}
