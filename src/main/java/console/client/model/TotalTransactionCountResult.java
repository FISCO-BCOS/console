package console.client.model;

import java.math.BigInteger;

public class TotalTransactionCountResult {

    private BigInteger blockNumber;
    private BigInteger txSum;
    private BigInteger failedTxSum = BigInteger.ZERO;

    public TotalTransactionCountResult() {}

    public BigInteger getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(BigInteger blockNumber) {
        this.blockNumber = blockNumber;
    }

    public BigInteger getTxSum() {
        return txSum;
    }

    public void setTxSum(BigInteger txSum) {
        this.txSum = txSum;
    }

    public BigInteger getFailedTxSum() {
        return failedTxSum;
    }

    public void setFailedTxSum(BigInteger failedTxSum) {
        this.failedTxSum = failedTxSum;
    }

    @Override
    public String toString() {
        return "InnerTotalTransactionCountResult [blockNumber="
                + blockNumber
                + ", txSum="
                + txSum
                + ", failedTxSum="
                + failedTxSum
                + "]";
    }
}
