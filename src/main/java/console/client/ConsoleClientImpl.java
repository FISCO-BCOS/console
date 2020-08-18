package console.client;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import console.common.Address;
import console.common.Common;
import console.common.ConsoleUtils;
import console.common.HelpInfo;
import console.common.TotalTransactionCountResult;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Objects;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.client.exceptions.ClientException;
import org.fisco.bcos.sdk.client.protocol.model.JsonTransactionResponse;
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
        if (HelpInfo.promptNoParams(params, "getNodeVersion")) {
            return;
        }
        ConsoleUtils.printJson(client.getNodeVersion().getNodeVersion().toString());
        System.out.println();
    }

    @Override
    public void getBlockNumber(String[] params) throws IOException {
        if (HelpInfo.promptNoParams(params, "getBlockNumber")) {
            return;
        }
        System.out.println(client.getBlockNumber().getBlockNumber());
        System.out.println();
    }

    @Override
    public void getPbftView(String[] params) throws IOException {
        if (HelpInfo.promptNoParams(params, "getPbftView")) {
            return;
        }
        System.out.println(client.getPbftView().getPbftView());
        System.out.println();
    }

    @Override
    public void getObserverList(String[] params) throws IOException {
        if (HelpInfo.promptNoParams(params, "getObserverList")) {
            return;
        }
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
        if (HelpInfo.promptNoParams(params, "getSealerList")) {
            return;
        }
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
        if (HelpInfo.promptNoParams(params, "getConsensusStatus")) {
            return;
        }
        ConsoleUtils.printJson(client.getConsensusStatus().getConsensusStatus().toString());
        System.out.println();
    }

    @Override
    public void getSyncStatus(String[] params) throws IOException {
        if (HelpInfo.promptNoParams(params, "getSyncStatus")) {
            return;
        }
        ConsoleUtils.printJson(client.getSyncStatus().getSyncStatus().toString());
        System.out.println();
    }

    @Override
    public void getPeers(String[] params) throws IOException {
        if (HelpInfo.promptNoParams(params, "getPeers")) {
            return;
        }
        ConsoleUtils.printJson(client.getPeers().getPeers().toString());
        System.out.println();
    }

    @Override
    public void getNodeIDList(String[] params) throws IOException {
        if (HelpInfo.promptNoParams(params, "getNodeIDList")) {
            return;
        }
        ConsoleUtils.printJson(client.getNodeIDList().getNodeIDList().toString());
        System.out.println();
    }

    @Override
    public void getGroupPeers(String[] params) throws IOException {
        if (HelpInfo.promptNoParams(params, "getGroupPeers")) {
            return;
        }
        ConsoleUtils.printJson(client.getGroupPeers().getGroupPeers().toString());
        System.out.println();
    }

    @Override
    public void getGroupList(String[] params) throws IOException {
        if (HelpInfo.promptNoParams(params, "getGroupList")) {
            return;
        }
        System.out.println(client.getGroupList().getGroupList().toString());
        System.out.println();
    }

    @Override
    public void getBlockByHash(String[] params) throws IOException {
        if (params.length < 2) {
            HelpInfo.promptHelp("getBlockByHash");
            return;
        }
        if (params.length > 3) {
            HelpInfo.promptHelp("getBlockByHash");
            return;
        }
        String blockHash = params[1];
        if ("-h".equals(blockHash) || "--help".equals(blockHash)) {
            HelpInfo.getBlockByHashHelp();
            return;
        }
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
        if (params.length < 2) {
            HelpInfo.promptHelp("getBlockByNumber");
            return;
        }
        if (params.length > 3) {
            HelpInfo.promptHelp("getBlockByNumber");
            return;
        }
        String blockNumberStr = params[1];
        if ("-h".equals(blockNumberStr) || "--help".equals(blockNumberStr)) {
            HelpInfo.getBlockByNumberHelp();
            return;
        }
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
        if (params.length < 2) {
            HelpInfo.promptHelp("getBlockHeaderByHash");
            return;
        }
        if (params.length > 3) {
            HelpInfo.promptHelp("getBlockHeaderByHash");
            return;
        }
        String blockHash = params[1];
        if ("-h".equals(blockHash) || "--help".equals(blockHash)) {
            HelpInfo.getBlockHeaderByHashHelp();
            return;
        }
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
        if (params.length < 2) {
            HelpInfo.promptHelp("getBlockHeaderByNumber");
            return;
        }
        if (params.length > 3) {
            HelpInfo.promptHelp("getBlockHeaderByNumber");
            return;
        }
        String blockNumberStr = params[1];
        if ("-h".equals(blockNumberStr) || "--help".equals(blockNumberStr)) {
            HelpInfo.getBlockHeaderByNumberHelp();
            return;
        }
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
        if (params.length < 2) {
            HelpInfo.promptHelp("getBlockHashByNumber");
            return;
        }
        if (params.length > 2) {
            HelpInfo.promptHelp("getBlockHashByNumber");
            return;
        }
        String blockNumberStr = params[1];
        if ("-h".equals(blockNumberStr) || "--help".equals(blockNumberStr)) {
            HelpInfo.getBlockHashByNumberHelp();
            return;
        }
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
        if (params.length < 2) {
            HelpInfo.promptHelp("getTransactionByHash");
            return;
        }
        if (params.length > 3) {
            HelpInfo.promptHelp("getTransactionByHash");
            return;
        }
        String transactionHash = params[1];
        if ("-h".equals(transactionHash) || "--help".equals(transactionHash)) {
            HelpInfo.getTransactionByHashHelp();
            return;
        }
        if (ConsoleUtils.isInvalidHash(transactionHash)) return;
        JsonTransactionResponse transaction =
                client.getTransactionByHash(transactionHash).getTransaction().get();
        if (transaction == null) {
            System.out.println("This transaction hash doesn't exist.");
            return;
        }
        ConsoleUtils.printJson(transaction.toString());
        // TODO: decode the transaction
        /*
        if (params.length == 3) {
            TxDecodeUtil.decdeInputForTransaction(params[2], transactionJson);
        }*/
        System.out.println();
    }

    /*
    @Override
    public void getTransactionByBlockHashAndIndex(String[] params){
        if (params.length < 2) {
            HelpInfo.promptHelp("getTransactionByBlockHashAndIndex");
            return;
        }
        if (params.length > 4) {
            HelpInfo.promptHelp("getTransactionByBlockHashAndIndex");
            return;
        }
        String blockHash = params[1];
        if ("-h".equals(blockHash) || "--help".equals(blockHash)) {
            HelpInfo.getTransactionByBlockHashAndIndexHelp();
            return;
        }
        if (params.length < 3) {
            HelpInfo.promptHelp("getTransactionByBlockHashAndIndex");
            return;
        }
        if (ConsoleUtils.isInvalidHash(blockHash)) {
            return;
        }
        String indexStr = params[2];
        int index = ConsoleUtils.proccessNonNegativeNumber("index", indexStr);
        if (index == Common.InvalidReturnNumber) {
            return;
        }
        BcosBlock.Block bcosBlock = client.getBlockByHash(blockHash, false).getBlock();
        int maxIndex = bcosBlock.getTransactions().size() - 1;
        if (index > maxIndex) {
            System.out.println("The index is out of range.");
            System.out.println();
            return;
        }
        String transactionJson =
                client.getTransactionByBlockNumberAndIndex(blockHash, BigInteger.valueOf(index))
        ConsoleUtils.printJson(transactionJson);
        if (params.length == 4) {
            TxDecodeUtil.decdeInputForTransaction(params[3], transactionJson);
        }
        System.out.println();
    }*/

    @Override
    public void getTransactionByBlockNumberAndIndex(String[] params) {
        try {
            if (params.length < 2) {
                HelpInfo.promptHelp("getTransactionByBlockNumberAndIndex");
                return;
            }
            if (params.length > 4) {
                HelpInfo.promptHelp("getTransactionByBlockNumberAndIndex");
                return;
            }
            String blockNumberStr = params[1];
            if ("-h".equals(blockNumberStr) || "--help".equals(blockNumberStr)) {
                HelpInfo.getTransactionByBlockNumberAndIndexHelp();
                return;
            }
            if (params.length < 3) {
                HelpInfo.promptHelp("getTransactionByBlockNumberAndIndex");
                return;
            }
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
            /*
            if (params.length == 4) {
                TxDecodeUtil.decdeInputForTransaction(params[3], transactionJson);
            }*/
        } catch (ClientException e) {
            ConsoleUtils.printJson(e.getMessage());
        }
        System.out.println();
    }

    @Override
    public void getTransactionReceipt(String[] params) throws Exception {
        if (params.length < 2) {
            HelpInfo.promptHelp("getTransactionReceipt");
            return;
        }
        if (params.length > 3) {
            HelpInfo.promptHelp("getTransactionReceipt");
            return;
        }
        String transactionHash = params[1];
        if ("-h".equals(transactionHash) || "--help".equals(transactionHash)) {
            HelpInfo.getTransactionReceiptHelp();
            return;
        }
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
        if (params.length < 2) {
            HelpInfo.promptHelp("getTransactionByHashWithProof");
            return;
        }
        if (params.length > 3) {
            HelpInfo.promptHelp("getTransactionByHashWithProof");
            return;
        }
        String transactionHash = params[1];
        if ("-h".equals(transactionHash) || "--help".equals(transactionHash)) {
            HelpInfo.getTransactionByHashWithProofHelp();
            return;
        }
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
        if (params.length < 2) {
            HelpInfo.promptHelp("getTransactionReceiptByHashWithProof");
            return;
        }
        if (params.length > 3) {
            HelpInfo.promptHelp("getTransactionReceiptByHashWithProof");
            return;
        }
        String transactionHash = params[1];
        if ("-h".equals(transactionHash) || "--help".equals(transactionHash)) {
            HelpInfo.getTransactionReceiptByHashWithProofHelp();
            return;
        }
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
        if (HelpInfo.promptNoParams(params, "getPendingTxSize")) {
            return;
        }
        String size = client.getPendingTxSize().getResult();
        System.out.println(Numeric.decodeQuantity(size));
        System.out.println();
    }

    @Override
    public void getPendingTransactions(String[] params) throws IOException {
        if (HelpInfo.promptNoParams(params, "getPendingTransactions")) {
            return;
        }
        String pendingTransactions = client.getPendingTransaction().getResult().toString();
        if ("[]".equals(pendingTransactions)) System.out.println(pendingTransactions);
        else ConsoleUtils.printJson(pendingTransactions);
        System.out.println();
    }

    @Override
    public void getCode(String[] params) throws IOException {
        if (params.length < 2) {
            HelpInfo.promptHelp("getCode");
            return;
        }
        if (params.length > 2) {
            HelpInfo.promptHelp("getCode");
            return;
        }
        String address = params[1];
        if ("-h".equals(address) || "--help".equals(address)) {
            HelpInfo.getCodeHelp();
            return;
        }
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
    public void getTotalTransactionCount(String[] params)
            throws JsonParseException, JsonMappingException, IOException {
        if (HelpInfo.promptNoParams(params, "getTotalTransactionCount")) {
            return;
        }

        String transactionCount =
                client.getTotalTransactionCount().getTotalTransactionCount().toString();
        TotalTransactionCountResult totalTransactionCountResult =
                ObjectMapperFactory.getObjectMapper()
                        .readValue(transactionCount, TotalTransactionCountResult.class);

        TotalTransactionCountResult.InnerTotalTransactionCountResult
                innerTotalTransactionCountResult =
                        totalTransactionCountResult.new InnerTotalTransactionCountResult();
        innerTotalTransactionCountResult.setBlockNumber(
                Numeric.decodeQuantity(totalTransactionCountResult.getBlockNumber()));
        innerTotalTransactionCountResult.setTxSum(
                Numeric.decodeQuantity(totalTransactionCountResult.getTxSum()));

        if (totalTransactionCountResult.getFailedTxSum() != null) {
            innerTotalTransactionCountResult.setFailedTxSum(
                    Numeric.decodeQuantity(totalTransactionCountResult.getFailedTxSum()));
        }

        ConsoleUtils.printJson(
                ObjectMapperFactory.getObjectMapper()
                        .writeValueAsString(innerTotalTransactionCountResult));
        System.out.println();
    }

    @Override
    public void getSystemConfigByKey(String[] params) throws Exception {
        if (params.length < 2) {
            HelpInfo.promptHelp("getSystemConfigByKey");
            return;
        }
        if (params.length > 2) {
            HelpInfo.promptHelp("getSystemConfigByKey");
            return;
        }
        String key = params[1];
        if ("-h".equals(key) || "--help".equals(key)) {
            HelpInfo.getSystemConfigByKeyHelp();
            return;
        }
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
}
