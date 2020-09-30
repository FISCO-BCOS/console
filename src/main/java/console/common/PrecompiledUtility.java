package console.common;

import java.io.IOException;
import org.fisco.bcos.web3j.precompile.common.PrecompiledCommon;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.channel.StatusCode;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.protocol.exceptions.TransactionException;

public class PrecompiledUtility {

    public static void handleTransactionReceipt(TransactionReceipt receipt, Web3j web3j)
            throws IOException, TransactionException {

        if (receipt.isStatusOK()) {
            String result = PrecompiledCommon.handleTransactionReceipt(receipt, web3j);
            ConsoleUtils.printJson(result);
            if (!result.contains("success")) {
                System.out.println(
                        "please refer to https://fisco-bcos-documentation.readthedocs.io/zh_CN/latest/docs/api.html#precompiled-service-api");
            }
        } else {
            final String transactionHash = receipt.getTransactionHash();
            if (transactionHash != null && !transactionHash.isEmpty()) {
                System.out.println("transaction hash: " + receipt.getTransactionHash());
            }

            ConsoleUtils.singleLine();
            System.out.println("transaction status: " + receipt.getStatus());
            if (StatusCode.Success.equals(receipt.getStatus())) {
                System.out.println("description: " + "transaction executed successfully");
            } else {
                String errorMessage = StatusCode.getStatusMessage(receipt.getStatus());
                System.out.println(
                        "description: "
                                + errorMessage
                                + ", please refer to "
                                + StatusCodeLink.txReceiptStatusLink);
            }
        }
    }
}
