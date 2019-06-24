package console.common;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.protocol.exceptions.TransactionException;
import org.fisco.bcos.web3j.tx.txdecode.BaseException;
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

    public static void decodeInput(AbiAndBin abiAndBin, TransactionReceipt receipt)
            throws BaseException, IOException, TransactionException {
        TransactionDecoder transactionDecoder =
                TransactionDecoderFactory.buildTransactionDecoder(
                        abiAndBin.getAbi(), abiAndBin.getBin());
        List<ResultEntity> input = transactionDecoder.decodeInputReturnObject(receipt.getInput());
        ConsoleUtils.singleLine();
        System.out.println("Input ");
        StringBuilder resultType = new StringBuilder();
        StringBuilder resultData = new StringBuilder();
        resultType.append("(");
        resultData.append("(");
        for (ResultEntity resultEntity : input) {
            resultType.append(resultEntity.getType()).append(", ");
            resultData.append(resultEntity.getData()).append(", ");
        }
        resultType.delete(resultType.length() - 2, resultType.length());
        resultData.delete(resultData.length() - 2, resultData.length());
        resultType.append(")");
        resultData.append(")");
        System.out.println("type: " + resultType);
        System.out.println("data: " + resultData);
        ConsoleUtils.singleLine();
    }

    public static void decodeOutput(String abi, TransactionReceipt receipt)
            throws BaseException, IOException, TransactionException {
        TransactionDecoder transactionDecoder =
                TransactionDecoderFactory.buildTransactionDecoder(abi, "");
        List<ResultEntity> output =
                transactionDecoder.decodeOutputReturnObject(
                        receipt.getInput(), receipt.getOutput());
        System.out.println("Output ");
        StringBuilder resultType = new StringBuilder();
        StringBuilder resultData = new StringBuilder();
        resultType.append("(");
        resultData.append("(");
        for (ResultEntity resultEntity : output) {
            resultType.append(resultEntity.getType()).append(", ");
            resultData.append(resultEntity.getData()).append(", ");
        }
        resultType.delete(resultType.length() - 2, resultType.length());
        resultData.delete(resultData.length() - 2, resultData.length());
        resultType.append(")");
        resultData.append(")");
        System.out.println("type: " + resultType);
        System.out.println("data: " + resultData);
        ConsoleUtils.singleLine();
    }

    public static void decodeEventLog(String abi, TransactionReceipt receipt)
            throws BaseException, IOException {
        TransactionDecoder transactionDecoder =
                TransactionDecoderFactory.buildTransactionDecoder(abi, "");
        Map<String, List<List<ResultEntity>>> eventlog =
                transactionDecoder.decodeEventReturnObject(receipt.getLogs());
        System.out.println("Event logs");
        Set<String> keySet = eventlog.keySet();
        for (String eventName : keySet) {
            List<List<ResultEntity>> loglists = eventlog.get(eventName);
            for (int i = 0; i < loglists.size(); i++) {
                System.out.println("event name: " + eventName + " index: " + i);
                StringBuilder result = new StringBuilder();
                result.append("(");
                List<ResultEntity> log = loglists.get(i);
                for (int j = 0; j < log.size(); j++) {
                    result.append(log.get(j).getData()).append(", ");
                }
                result.delete(result.length() - 2, result.length());
                result.append(")");
                System.out.println("data: " + result);
            }
        }
        ConsoleUtils.singleLine();
    }
}
