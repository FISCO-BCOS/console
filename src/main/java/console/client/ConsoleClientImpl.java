package console.client;

import console.common.Address;
import console.common.Common;
import console.common.ConsoleUtils;
import console.common.TotalTransactionCountResult;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Objects;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.client.exceptions.ClientException;
import org.fisco.bcos.sdk.client.protocol.model.JsonTransactionResponse;
import org.fisco.bcos.sdk.client.protocol.response.TotalTransactionCount;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.model.TransactionReceiptStatus;
import org.fisco.bcos.sdk.utils.Numeric;
import org.fisco.bcos.sdk.utils.ObjectMapperFactory;

public class ConsoleClientImpl implements ConsoleClientFace {

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

    public void startGroup(String[] params) {
        Integer groupId = checkAndGetGroupId(params);
        if (groupId == null) {
            return;
        }
        ConsoleUtils.printJson(client.startGroup(groupId, params[1]).getGroupStatus().toString());
    }

    public void stopGroup(String[] params) {
        Integer groupId = checkAndGetGroupId(params);
        if (groupId == null) {
            return;
        }
        ConsoleUtils.printJson(client.stopGroup(groupId, params[1]).getGroupStatus().toString());
    }

    public void removeGroup(String[] params) {
        Integer groupId = checkAndGetGroupId(params);
        if (groupId == null) {
            return;
        }
        ConsoleUtils.printJson(client.removeGroup(groupId, params[1]).getGroupStatus().toString());
    }

    public void recoverGroup(String[] params) {
        Integer groupId = checkAndGetGroupId(params);
        if (groupId == null) {
            return;
        }
        ConsoleUtils.printJson(client.recoverGroup(groupId, params[1]).getGroupStatus().toString());
    }

    public void queryGroupStatus(String[] params) {
        Integer groupId = checkAndGetGroupId(params);
        if (groupId == null) {
            return;
        }
        ConsoleUtils.printJson(
                client.queryGroupStatus(groupId, params[1]).getGroupStatus().toString());
    }
}
