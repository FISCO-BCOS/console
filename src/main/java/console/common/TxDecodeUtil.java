package console.common;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.core.methods.response.AbiDefinition.NamedType.Type;
import org.fisco.bcos.web3j.protocol.core.methods.response.Transaction;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.protocol.exceptions.TransactionException;
import org.fisco.bcos.web3j.tx.txdecode.BaseException;
import org.fisco.bcos.web3j.tx.txdecode.EventResultEntity;
import org.fisco.bcos.web3j.tx.txdecode.InputAndOutputResult;
import org.fisco.bcos.web3j.tx.txdecode.ResultEntity;
import org.fisco.bcos.web3j.tx.txdecode.TransactionDecoder;
import org.fisco.bcos.web3j.tx.txdecode.TransactionDecoderFactory;

public class TxDecodeUtil {

    public static AbiAndBin readAbiAndBin(String contractName) throws IOException {
        String tempName = ContractClassFactory.removeSolPostfix(contractName);
        BufferedReader abiReader =
                new BufferedReader(
                        new FileReader(ContractClassFactory.ABI_PATH + tempName + ".abi"));
        BufferedReader binReader =
                new BufferedReader(
                        new FileReader(ContractClassFactory.BIN_PATH + tempName + ".bin"));
        StringBuilder abiBuilder = new StringBuilder();
        StringBuilder binBuilder = new StringBuilder();
        String abiStr = "";
        while ((abiStr = abiReader.readLine()) != null) {
            abiBuilder.append(abiStr);
        }
        String binStr = "";
        while ((binStr = binReader.readLine()) != null) {
            binBuilder.append(binStr);
        }
        AbiAndBin abiAndBin = new AbiAndBin();
        abiAndBin.setAbi(abiBuilder.toString());
        abiAndBin.setBin(binBuilder.toString());
        return abiAndBin;
    }

    public static void decodeInput(AbiAndBin abiAndBin, String input)
            throws BaseException, IOException, TransactionException {
        TransactionDecoder transactionDecoder =
                TransactionDecoderFactory.buildTransactionDecoder(
                        abiAndBin.getAbi(), abiAndBin.getBin());
        InputAndOutputResult result = transactionDecoder.decodeInputReturnObject(input);
        List<ResultEntity> resultList = result.getResult();
        ConsoleUtils.singleLine();
        System.out.println("Input ");
        StringBuilder resultData = new StringBuilder();
        resultData.append("(");
        for (ResultEntity resultEntity : resultList) {
            resultData.append(resultEntity.getData()).append(", ");
        }
        if (!resultList.isEmpty()) {
            resultData.delete(resultData.length() - 2, resultData.length());
        }
        resultData.append(")");
        System.out.println("function: " + result.getFunction());
        System.out.println("input value: " + resultData);
        ConsoleUtils.singleLine();
    }

    public static void decodeOutput(String abi, TransactionReceipt receipt)
            throws BaseException, IOException, TransactionException {
        TransactionDecoder transactionDecoder =
                TransactionDecoderFactory.buildTransactionDecoder(abi, "");
        InputAndOutputResult result =
                transactionDecoder.decodeOutputReturnObject(
                        receipt.getInput(), receipt.getOutput());
        List<ResultEntity> resultList = result.getResult();
        ConsoleUtils.singleLine();
        System.out.println("Output ");
        StringBuilder resultType = new StringBuilder();
        StringBuilder resultData = new StringBuilder();
        resultType.append("(");
        resultData.append("(");
        for (ResultEntity resultEntity : resultList) {
            resultType.append(resultEntity.getType()).append(", ");
            resultData.append(resultEntity.getData()).append(", ");
        }
        resultType.delete(resultType.length() - 2, resultType.length());
        resultData.delete(resultData.length() - 2, resultData.length());
        resultType.append(")");
        resultData.append(")");
        System.out.println("function: " + result.getFunction());
        System.out.println("return type: " + resultType);
        System.out.println("return value: " + resultData);
        ConsoleUtils.singleLine();
    }

    public static void decodeEventLog(String abi, TransactionReceipt receipt)
            throws BaseException, IOException {
        TransactionDecoder transactionDecoder =
                TransactionDecoderFactory.buildTransactionDecoder(abi, "");
        Map<String, List<List<EventResultEntity>>> eventlog =
                transactionDecoder.decodeEventReturnObject(receipt.getLogs());
        System.out.println("Event logs");
        Set<String> keySet = eventlog.keySet();
        for (String eventName : keySet) {
            List<List<EventResultEntity>> loglists = eventlog.get(eventName);
            for (int i = 0; i < loglists.size(); i++) {
                System.out.println("event signature: " + eventName + " index: " + i);
                StringBuilder result = new StringBuilder();
                result.append("(");
                List<EventResultEntity> log = loglists.get(i);
                for (int j = 0; j < log.size(); j++) {
                    if (log.get(j).isIndexed() && isDynamic(log.get(j).getType())) {
                        byte[] bytes = (byte[]) log.get(j).getTypeObject().getValue();
                        String data = "0x" + byteToHex(bytes);
                        result.append(data).append(", ");
                    } else {
                        result.append(log.get(j).getData()).append(", ");
                    }
                }
                result.delete(result.length() - 2, result.length());
                result.append(")");
                System.out.println("event value: " + result);
            }
        }
        ConsoleUtils.singleLine();
    }

    public static String byteToHex(byte[] bytes) {
        String strHex = "";
        StringBuilder sb = new StringBuilder("");
        for (int n = 0; n < bytes.length; n++) {
            strHex = Integer.toHexString(bytes[n] & 0xFF);
            sb.append((strHex.length() == 1) ? "0" + strHex : strHex); // 每个字节由两个字符表示，位数不够，高位补0
        }
        return sb.toString().trim();
    }

    public static boolean isDynamic(String typeStr) {
        Type type = new Type(typeStr);
        if (type.dynamicArray()) {
            return true;
        }
        if (type.getBaseName().equals("string") || type.getBaseName().equals("bytes")) {
            return true;
        }
        return false;
    }

    public static void decdeInputForTransaction(String contractName, String transactionJson)
            throws IOException, JsonParseException, JsonMappingException, BaseException,
                    TransactionException {
        AbiAndBin abiAndBin =
                TxDecodeUtil.readAbiAndBin(ContractClassFactory.removeSolPostfix(contractName));
        org.fisco.bcos.web3j.protocol.core.methods.response.Transaction transacton =
                ObjectMapperFactory.getObjectMapper()
                        .readValue(
                                transactionJson,
                                org.fisco.bcos.web3j.protocol.core.methods.response.Transaction
                                        .class);
        if (!Common.EMPTY_CONTRACT_ADDRESS.equals(transacton.getTo())) {
            TxDecodeUtil.decodeInput(abiAndBin, transacton.getInput());
        }
    }

    public static void setInputForReceipt(Web3j web3j, TransactionReceipt receipt)
            throws IOException {
        Transaction transaction =
                web3j.getTransactionByHash(receipt.getTransactionHash())
                        .send()
                        .getTransaction()
                        .get();
        receipt.setInput(transaction.getInput());
    }
}
