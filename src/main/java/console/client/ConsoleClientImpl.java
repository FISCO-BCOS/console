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
import org.fisco.bcos.sdk.crypto.CryptoInterface;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
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
        System.out.println();
    }

    @Override
    public void getBlockNumber(String[] params) throws IOException {
        System.out.println(client.getBlockNumber().getBlockNumber());
        System.out.println();
    }

    @Override
    public void getPbftView(String[] params) throws IOException {
        System.out.println(client.getPbftView().getPbftView());
        System.out.println();
    }

    @Override
    public void getObserverList(String[] params) throws IOException {
        String observers = client.getObserverList().getObserverList().toString();
        if ("[]".equals(observers)) {
            System.out.println("[]");
        } else {
            ConsoleUtils.printJson(observers);
        }
        System.out.println();
    }

    @Override
    public void getSealerList(String[] params) throws IOException {
        String sealers = client.getSealerList().getSealerList().toString();
        if ("[]".equals(sealers)) {
            System.out.println("[]");
        } else {
            ConsoleUtils.printJson(sealers);
        }
        System.out.println();
    }

    @Override
    public void getConsensusStatus(String[] params) throws IOException {
        ConsoleUtils.printJson(client.getConsensusStatus().getConsensusStatus().toString());
        System.out.println();
    }

    @Override
    public void getSyncStatus(String[] params) throws IOException {
        ConsoleUtils.printJson(client.getSyncStatus().getSyncStatus().toString());
        System.out.println();
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
        System.out.println();
    }

    @Override
    public void getNodeIDList(String[] params) throws IOException {
        ConsoleUtils.printJson(client.getNodeIDList().getNodeIDList().toString());
        System.out.println();
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
        System.out.println();
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
        System.out.println();
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
                System.out.println();
                return;
            }
        }
        ConsoleUtils.printJson(client.getBlockByHash(blockHash, flag).getBlock().toString());
        System.out.println();
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
                System.out.println();
                return;
            }
        }
        ConsoleUtils.printJson(
                client.getBlockByNumber(BigInteger.valueOf(blockNumber), flag)
                        .getBlock()
                        .toString());
        System.out.println();
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
                System.out.println();
                return;
            }
        }
        ConsoleUtils.printJson(
                client.getBlockHeaderByHash(blockHash, flag).getBlockHeader().toString());
        System.out.println();
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
                System.out.println();
                return;
            }
        }
        ConsoleUtils.printJson(
                client.getBlockHeaderByNumber(BigInteger.valueOf(blockNumber), flag)
                        .getBlockHeader()
                        .toString());
        System.out.println();
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
        System.out.println();
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
        System.out.println();
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
        System.out.println();
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
            ConsoleUtils.printJson(e.getMessage());
        }
        System.out.println();
    }

    @Override
    public void getTransactionReceipt(String[] params) throws Exception {
        String transactionHash = params[1];
        if (ConsoleUtils.isInvalidHash(transactionHash)) return;

        TransactionReceipt receipt =
                client.getTransactionReceipt(transactionHash).getTransactionReceipt().get();
        if (Objects.isNull(receipt) || Objects.isNull(receipt.getTransactionHash())) {
            System.out.println("This transaction hash doesn't exist.");
            System.out.println();
            return;
        }

        if (!receipt.isStatusOK()) {
            receipt.setMessage(
                    TransactionReceiptStatus.getStatusMessage(
                                    receipt.getStatus(), receipt.getMessage())
                            .getMessage());
        }

        ConsoleUtils.printJson(ObjectMapperFactory.getObjectMapper().writeValueAsString(receipt));
        System.out.println();
    }

    @Override
    public void getTransactionByHashWithProof(String[] params) throws Exception {
        String transactionHash = params[1];
        if (ConsoleUtils.isInvalidHash(transactionHash)) return;
        String transactionWithProof =
                client.getTransactionByHashWithProof(transactionHash).getResult().toString();

        if (Objects.isNull(transactionWithProof) || "".equals(transactionWithProof)) {
            System.out.println("This transaction hash doesn't exist.");
            System.out.println();
            return;
        }
        ConsoleUtils.printJson(transactionWithProof);
        System.out.println();
    }

    @Override
    public void getTransactionReceiptByHashWithProof(String[] params) throws Exception {
        String transactionHash = params[1];
        if (ConsoleUtils.isInvalidHash(transactionHash)) return;

        String transactionReceiptWithProof =
                client.getTransactionReceiptByHashWithProof(transactionHash).getResult().toString();

        if (Objects.isNull(transactionReceiptWithProof) || "".equals(transactionReceiptWithProof)) {
            System.out.println("This transaction hash doesn't exist.");
            System.out.println();
            return;
        }

        ConsoleUtils.printJson(transactionReceiptWithProof);

        System.out.println();
    }

    @Override
    public void getPendingTxSize(String[] params) throws IOException {
        String size = client.getPendingTxSize().getResult();
        System.out.println(Numeric.decodeQuantity(size));
        System.out.println();
    }

    @Override
    public void getPendingTransactions(String[] params) throws IOException {
        String pendingTransactions = client.getPendingTransaction().getResult().toString();
        if ("[]".equals(pendingTransactions)) System.out.println(pendingTransactions);
        else ConsoleUtils.printJson(pendingTransactions);
        System.out.println();
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
            System.out.println();
            return;
        }
        ConsoleUtils.printJson(code);
        System.out.println();
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
        System.out.println();
    }

    @Override
    public void getSystemConfigByKey(String[] params) throws Exception {
        String key = params[1];
        if (Common.TxCountLimit.equals(key)
                || Common.TxGasLimit.equals(key)
                || Common.RPBFTEpochSealerNum.equals(key)
                || Common.RPBFTEpochBlockNum.equals(key)
                || Common.ConsensusTimeout.equals(key)) {
            String value = client.getSystemConfigByKey(key).getSystemConfig();
            if (Common.RPBFTEpochSealerNum.equals(key) || Common.RPBFTEpochBlockNum.equals(key)) {
                System.out.println("Note: " + key + " only takes effect when RPBFT is used!");
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
        System.out.println();
    }

    private Integer checkAndGetGroupId(String[] params) {
        Integer groupId = client.getGroupId();
        if (!ConsoleUtils.checkEndPoint(params[1])) {
            return null;
        }
        if (params.length == 3) {
            groupId = ConsoleUtils.proccessNonNegativeNumber("groupId", params[2]);
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
        String targetNode = params[1];
        if (!ConsoleUtils.checkEndPoint(targetNode)) {
            return;
        }
        int groupId = Integer.valueOf(params[2]);
        long timestamp = Long.valueOf(params[3]);
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
        Integer groupId = Integer.valueOf(params[1]);
        File groupConfigFile = new File(params[2]);
        if (!groupConfigFile.exists()) {
            System.out.println(
                    "Please make sure the group configuration file " + params[2] + " exists!");
            return;
        }
        GenerateGroupParam generateGroupParam =
                new Toml().read(groupConfigFile).to(GenerateGroupParam.class);
        // check generateGroupParam
        if (!generateGroupParam.checkGenerateGroupParam()) {
            return;
        }
        logger.debug(
                "generateGroupFromFile, peers: {}, timestamp: {}, sealerList: {}",
                generateGroupParam.getGroupPeerInfo().toString(),
                generateGroupParam.getTimestamp(),
                generateGroupParam.getSealerListInfo());
        for (String peer : generateGroupParam.getGroupPeerInfo()) {
            ConsoleUtils.printJson(
                    client.generateGroup(
                                    groupId,
                                    generateGroupParam.getTimestamp(),
                                    false,
                                    generateGroupParam.getSealerListInfo(),
                                    peer)
                            .getGroupStatus()
                            .toString());
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
        CryptoInterface cryptoInterface = client.getCryptoInterface();
        CryptoKeyPair cryptoKeyPair = cryptoInterface.createKeyPair();
        cryptoInterface.setConfig(cryptoInterface.getConfig());
        if (accountFormat.equals("pem")) {
            // save the account
            cryptoKeyPair.storeKeyPairWithPemFormat();
            System.out.println("AccountPath: " + cryptoKeyPair.getPemKeyStoreFilePath());
        } else {
            cryptoKeyPair.storeKeyPairWithP12Format(password);
            System.out.println("AccountPath: " + cryptoKeyPair.getP12KeyStoreFilePath());
        }
        System.out.println("newAccount: " + cryptoKeyPair.getAddress());
        System.out.println(
                "AccountType: "
                        + (cryptoInterface.getCryptoTypeConfig() == CryptoInterface.ECDSA_TYPE
                                ? "ecdsa"
                                : "sm"));
    }

    @Override
    public void loadAccount(String[] params) {
        String accountPath = params[1];
        if (!new File(accountPath).exists()) {
            // try to load the account from the given address
            String pemAccountPath =
                    client.getCryptoInterface()
                            .getCryptoKeyPair()
                            .getPemKeyStoreFilePath(accountPath);
            logger.debug("pemAccountPath: {}", pemAccountPath);
            if (!new File(pemAccountPath).exists()) {
                String p12AccountPath =
                        client.getCryptoInterface()
                                .getCryptoKeyPair()
                                .getP12KeyStoreFilePath(accountPath);
                logger.debug("p12AccountPath: {}", p12AccountPath);
                if (!new File(p12AccountPath).exists()) {
                    System.out.println("The account file " + accountPath + " doesn't exist!");
                    return;
                } else {
                    accountPath = p12AccountPath;
                }
            } else {
                accountPath = pemAccountPath;
            }
        }
        String accountFormat = params[2];
        if (accountFormat.equals("pem") && accountFormat.equals("p12")) {
            System.out.println(
                    "Load account failed! Only support \"pem\" and \"p12\" account now!");
            return;
        }
        String accountPassword = null;
        if (params.length == 4) {
            accountPassword = params[3];
        }
        CryptoInterface cryptoInterface = client.getCryptoInterface();
        cryptoInterface.loadAccount(accountFormat, accountPath, accountPassword);
        System.out.println("Load account " + params[1] + " success!");
        System.out.println();
    }

    @Override
    public void listAccount(String[] params) {
        List<String> accountList = listAccount(this.client);
        if (accountList.size() == 0) {
            System.out.println("Empty set");
            return;
        }
        for (int i = 0; i < accountList.size(); i++) {
            System.out.println(accountList.get(i));
        }
        System.out.println();
    }

    public static String getAccountDir(Client client) {
        ConfigOption configOption = client.getCryptoInterface().getConfig();
        String accountFilePath = configOption.getAccountConfig().getAccountFilePath();
        String subDir = CryptoKeyPair.ECDSA_ACCOUNT_SUBDIR;
        if (client.getCryptoInterface().getCryptoTypeConfig() == CryptoInterface.SM_TYPE) {
            subDir = CryptoKeyPair.GM_ACCOUNT_SUBDIR;
        }
        // load account from the given path
        if (accountFilePath == null || accountFilePath.equals("")) {
            accountFilePath =
                    configOption.getAccountConfig().getKeyStoreDir() + File.separator + subDir;
        }
        return accountFilePath;
    }

    public static List<String> listAccount(Client client) {
        List<String> accountList = new ArrayList<>();
        File accountFile = new File(getAccountDir(client));
        if (!accountFile.exists()) {
            return accountList;
        }
        for (String account : accountFile.list()) {
            logger.debug("account is: {}", account);
            String[] accountAddressList = account.split("\\.");
            if (accountAddressList.length == 0) {
                continue;
            }
            String accountAddress = accountAddressList[0];
            if (!accountList.contains(accountAddress)) {
                accountList.add(accountAddress);
            }
        }
        return accountList;
    }
}
