package console.common;

import org.fisco.bcos.web3j.protocol.channel.StatusCode;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;

public class PrecompiledUtility {

    public static void handleTransactionReceipt(TransactionReceipt receipt) {

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
