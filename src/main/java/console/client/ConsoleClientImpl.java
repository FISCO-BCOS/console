package console.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import console.ConsoleInitializer;
import console.client.model.TotalTransactionCountResult;
import console.common.Common;
import console.common.ConsoleUtils;
import console.contract.ConsoleContractImpl;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.client.exceptions.ClientException;
import org.fisco.bcos.sdk.v3.client.protocol.model.JsonTransactionResponse;
import org.fisco.bcos.sdk.v3.client.protocol.response.BcosBlock;
import org.fisco.bcos.sdk.v3.client.protocol.response.BcosGroupNodeInfo;
import org.fisco.bcos.sdk.v3.client.protocol.response.SystemConfig;
import org.fisco.bcos.sdk.v3.client.protocol.response.TotalTransactionCount;
import org.fisco.bcos.sdk.v3.config.ConfigOption;
import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.v3.model.CryptoType;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.utils.AddressUtils;
import org.fisco.bcos.sdk.v3.utils.Numeric;
import org.fisco.bcos.sdk.v3.utils.ObjectMapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleClientImpl implements ConsoleClientFace {
    private static final Logger logger = LoggerFactory.getLogger(ConsoleContractImpl.class);
    private Client client;
    private static String EMPTY_HASH_HEX =
            "0x0000000000000000000000000000000000000000000000000000000000000000";

    public ConsoleClientImpl(Client client) {
        this.client = client;
    }

    @Override
    public void updateClient(Client client) {
        this.client = client;
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
    public void getCandidateSealerList(String[] params) throws IOException {
        String sealers =
                client.getNodeListByType("consensus_candidate_sealer").getSealerList().toString();
        if ("[]".equals(sealers)) {
            System.out.println("[]");
        } else {
            ConsoleUtils.printJson(sealers);
        }
    }

    @Override
    public void getSyncStatus(String[] params) throws IOException {
        ConsoleUtils.printJson(client.getSyncStatus().getSyncStatus().toString());
    }

    @Override
    public void getConsensusStatus(String[] params) throws IOException {
        ConsoleUtils.printJson(client.getConsensusStatus().getConsensusStatus().toString());
    }

    @Override
    public void getPeers(String[] params) throws IOException {
        ConsoleUtils.printJson(client.getPeers().getPeers().toString());
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
        BcosBlock.Block block = client.getBlockByHash(blockHash, false, flag).getBlock();
        if (block == null) {
            System.out.println("Block can not ne found, please check hash: " + blockHash);
            return;
        }
        ConsoleUtils.printJson(block.toString());
    }

    @Override
    public void getBlockByNumber(String[] params) throws IOException {
        String blockNumberStr = params[1];
        int blockNumber = ConsoleUtils.processNonNegativeNumber("blockNumber", blockNumberStr);
        if (blockNumber == Common.INVALID_RETURN_NUMBER) {
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
        BcosBlock blockByNumber =
                client.getBlockByNumber(BigInteger.valueOf(blockNumber), false, flag);
        if (blockByNumber.getBlock() == null) {
            System.out.println("Block not found, please check number: " + blockNumber);
        } else {
            ConsoleUtils.printJson(blockByNumber.getBlock().toString());
        }
    }

    @Override
    public void getLatestBlock(String[] params) throws IOException {
        boolean flag = false;
        if (params.length == 2) {
            if ("true".equals(params[1])) {
                flag = true;
            } else if ("false".equals(params[1])) {
                flag = false;
            } else {
                System.out.println("Please provide true or false for the second parameter.");
                return;
            }
        }
        BigInteger blockNumber = client.getBlockNumber().getBlockNumber();
        BcosBlock block = client.getBlockByNumber(blockNumber, false, flag);
        if (block.getBlock() == null) {
            System.out.println("Block not found, please check number: " + blockNumber);
        } else {
            ConsoleUtils.printJson(block.getBlock().toString());
        }
    }

    @Override
    public void getBlockHeaderByHash(String[] params) throws IOException {
        String blockHash = params[1];
        if (ConsoleUtils.isInvalidHash(blockHash)) return;
        ConsoleUtils.printJson(client.getBlockByHash(blockHash, true, false).getBlock().toString());
    }

    @Override
    public void getBlockHashByNumber(String[] params) throws IOException {
        String blockNumberStr = params[1];
        int blockNumber = ConsoleUtils.processNonNegativeNumber("blockNumber", blockNumberStr);
        if (blockNumber == Common.INVALID_RETURN_NUMBER) {
            return;
        }
        String blockHashByNumber =
                client.getBlockHashByNumber(BigInteger.valueOf(blockNumber)).getBlockHashByNumber();
        if (blockHashByNumber.equals(EMPTY_HASH_HEX)) {
            System.out.println("BlockHash is empty, please check block number exists.");
            return;
        }
        System.out.println(
                client.getBlockHashByNumber(BigInteger.valueOf(blockNumber))
                        .getBlockHashByNumber());
    }

    @Override
    public void getBlockHeaderByNumber(String[] params) throws IOException {
        String blockNumberStr = params[1];
        int blockNumber = ConsoleUtils.processNonNegativeNumber("blockNumber", blockNumberStr);
        if (blockNumber == Common.INVALID_RETURN_NUMBER) {
            return;
        }
        ConsoleUtils.printJson(
                client.getBlockByNumber(BigInteger.valueOf(blockNumber), true, false)
                        .getBlock()
                        .toString());
    }

    @Override
    public void getTransactionByHash(String[] params) {
        String transactionHash = params[1];
        if (ConsoleUtils.isInvalidHash(transactionHash)) return;
        JsonTransactionResponse transaction =
                client.getTransaction(transactionHash, false).getTransaction().get();
        if (transaction == null) {
            System.out.println("This transaction hash doesn't exist.");
            return;
        }
        ConsoleUtils.printJson(transaction.toString());
    }

    @Override
    public void getTransactionReceipt(String[] params) throws Exception {
        String transactionHash = params[1];
        if (ConsoleUtils.isInvalidHash(transactionHash)) return;
        TransactionReceipt receipt = null;
        try {
            receipt = client.getTransactionReceipt(transactionHash, false).getTransactionReceipt();
        } catch (ClientException e) {
            System.out.println(
                    "This transaction hash doesn't exist, errorMsg:" + e.getErrorMessage());
            return;
        }

        if (Objects.isNull(receipt) || Objects.isNull(receipt.getTransactionHash())) {
            System.out.println("This transaction hash doesn't exist.");
            return;
        }
        ConsoleUtils.printJson(ObjectMapperFactory.getObjectMapper().writeValueAsString(receipt));
    }

    @Override
    public void getTransactionByHashWithProof(String[] params) throws Exception {
        String transactionHash = params[1];
        if (ConsoleUtils.isInvalidHash(transactionHash)) return;
        String transactionWithProof =
                client.getTransaction(transactionHash, true).getResult().toString();

        if (Objects.isNull(transactionWithProof) || transactionWithProof.isEmpty()) {
            System.out.println("This transaction hash doesn't exist.");
            return;
        }
        ConsoleUtils.printJson(transactionWithProof);
    }

    @Override
    public void getTransactionReceiptByHashWithProof(String[] params) throws Exception {
        String transactionHash = params[1];
        if (ConsoleUtils.isInvalidHash(transactionHash)) return;
        String transactionReceiptWithProof;
        try {
            transactionReceiptWithProof =
                    client.getTransactionReceipt(transactionHash, true).getResult().toString();
        } catch (ClientException e) {
            System.out.println(
                    "This transaction hash doesn't exist, errorMsg:" + e.getErrorMessage());
            return;
        }

        if (Objects.isNull(transactionReceiptWithProof) || transactionReceiptWithProof.isEmpty()) {
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
    public void getCode(String[] params, boolean isWasm, String pwd) throws IOException {
        String address = params[1];
        if (!isWasm) {
            if (!AddressUtils.isValidAddress(address)) {
                System.out.println("This address is invalid.");
                return;
            }
        } else {
            try {
                address = ConsoleUtils.fixedBfsParam(address, pwd).substring("/apps".length());
            } catch (Exception e) {
                System.out.println("Path is error: " + address);
                return;
            }
        }
        String code = client.getCode(address).getCode();
        if ("0x".equals(code) || code.isEmpty()) {
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
                Numeric.decodeQuantity(transactionCount.getTransactionCount()));

        if (transactionCount.getFailedTransactionCount() != null) {
            innerTotalTransactionCountResult.setFailedTxSum(
                    Numeric.decodeQuantity(transactionCount.getFailedTransactionCount()));
        }

        ConsoleUtils.printJson(
                ObjectMapperFactory.getObjectMapper()
                        .writeValueAsString(innerTotalTransactionCountResult));
    }

    @Override
    public void getSystemConfigByKey(String[] params) throws Exception {
        String key = params[1];
        SystemConfig systemConfigByKey = client.getSystemConfigByKey(key);
        if (systemConfigByKey.getSystemConfig() == null) {
            System.out.println("System config not found, please check key: " + key);
        } else {
            String value = client.getSystemConfigByKey(key).getSystemConfig().getValue();
            System.out.println(value);
        }
    }

    @Override
    public void listConfigs(String[] params) throws Exception {
        Map<String, Optional<SystemConfig>> systemConfigList = client.getSystemConfigList();
        int longestKeySize = 0;
        for (String key : systemConfigList.keySet()) {
            if (key.length() > longestKeySize) {
                longestKeySize = key.length();
            }
        }
        String leftAlignFormat = "| %-" + longestKeySize + "s | %-14s | %-12s |%n";
        String separatorLine =
                Stream.generate(() -> "-").limit(longestKeySize + 2).collect(Collectors.joining());
        String adaptableTitle =
                "| Config"
                        + Stream.generate(() -> " ")
                                .limit(longestKeySize - "Config".length())
                                .collect(Collectors.joining());
        System.out.println("+" + separatorLine + "+----------------+--------------+");
        System.out.println(adaptableTitle + " | Value          | Enable Block |");
        System.out.println("+" + separatorLine + "+----------------+--------------+");
        systemConfigList.forEach(
                (key, value) -> {
                    String configValue = "null";
                    long blockNumber = 0;
                    if (value.isPresent()) {
                        configValue = value.get().getSystemConfig().getValue();
                        blockNumber = value.get().getSystemConfig().getBlockNumber();
                    }
                    System.out.format(leftAlignFormat, key, configValue, blockNumber);
                });
        System.out.println("+" + separatorLine + "+----------------+--------------+");
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
        if (accountList.isEmpty()) {
            System.out.println("Empty set");
            return;
        }
        String currentAccount = client.getCryptoSuite().getCryptoKeyPair().getAddress();
        String currentAccountSuffix =
                client.getCryptoSuite().getCryptoTypeConfig() == CryptoType.HSM_TYPE
                        ? "(current HSM account) <="
                        : "(current account) <=";
        System.out.println(currentAccount + currentAccountSuffix);
        for (String s : accountList) {
            if (!s.equals(currentAccount)) {
                System.out.println(s);
            }
        }
    }

    public static String getAccountDir(Client client) {
        ConfigOption configOption = client.getCryptoSuite().getConfig();
        String subDir = CryptoKeyPair.ECDSA_ACCOUNT_SUBDIR;
        if (client.getCryptoSuite().getCryptoTypeConfig() == CryptoType.SM_TYPE
                || client.getCryptoSuite().getCryptoTypeConfig() == CryptoType.HSM_TYPE) {
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
                    accountAddressList[0].split(ConsoleUtils.GM_ACCOUNT_SUFFIX);
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
    public void getGroupList(String[] params) {
        ConsoleUtils.printJson(client.getGroupList().getResult().getGroupList().toString());
    }

    @Override
    public void getGroupPeers(String[] params) {
        int i = 0;
        for (String groupPeer : client.getGroupPeers().getGroupPeers()) {
            System.out.println("peer" + (i++) + ": " + groupPeer);
        }
    }

    @Override
    public void getGroupInfo(String[] params) throws IOException {
        ConsoleUtils.printJson(
                ObjectMapperFactory.getObjectMapper()
                        .writeValueAsString(client.getGroupInfo().getResult()));
    }

    @Override
    public void getGroupInfoList(String[] params) throws JsonProcessingException {
        ConsoleUtils.printJson(
                ObjectMapperFactory.getObjectMapper()
                        .writeValueAsString(client.getGroupInfoList().getResult()));
    }

    @Override
    public void getGroupNodeInfo(String[] params) throws JsonProcessingException {
        String node = params[1];
        ConsoleUtils.printJson(
                ObjectMapperFactory.getObjectMapper()
                        .writeValueAsString(client.getGroupNodeInfo(node).getResult()));
    }

    @Override
    public void setNodeName(ConsoleInitializer consoleInitializer, String[] params) {
        String newNodeName = params[1];
        List<BcosGroupNodeInfo.GroupNodeInfo> nodeInfos =
                client.getGroupInfo().getResult().getNodeList();
        AtomicBoolean findFlag = new AtomicBoolean(false);
        nodeInfos.forEach(
                groupNodeInfo -> {
                    if (groupNodeInfo.getName().equals(newNodeName)) {
                        findFlag.set(true);
                    }
                });
        if (!findFlag.get()) {
            System.out.println(
                    "Invalid nodeName: "
                            + newNodeName
                            + ", node not contains in node list, check command 'getGroupInfo'.");
            return;
        }
        consoleInitializer.getClient().setNodeToSendRequest(newNodeName);
        getNodeName(consoleInitializer);
    }

    @Override
    public void clearNodeName(ConsoleInitializer consoleInitializer) {
        System.out.println("Clear nodeName to empty.");
        consoleInitializer.getClient().setNodeToSendRequest("");
    }

    @Override
    public void getNodeName(ConsoleInitializer consoleInitializer) {
        String nodeToSendRequest = consoleInitializer.getClient().getNodeToSendRequest();
        if (nodeToSendRequest.isEmpty()) {
            System.out.println(
                    "Current default node name is empty, RPC will send request to node randomly.");
            return;
        }
        System.out.println("Current default node name: " + nodeToSendRequest);
    }
}
