package console.common;

import java.math.BigInteger;

public class TotalTransactionCountResult {
    private String blockNumber;
    private String txSum;
    private String failedTxSum;

    public String getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(String blockNumber) {
        this.blockNumber = blockNumber;
    }

    public String getTxSum() {
        return txSum;
    }

    public void setTxSum(String txSum) {
        this.txSum = txSum;
    }

    public String getFailedTxSum() {
        return failedTxSum;
    }

    public void setFailedTxSum(String failedTxSum) {
        this.failedTxSum = failedTxSum;
    }

    @Override
    public String toString() {
        return "TotalTransactionCountResult [blockNumber="
                + blockNumber
                + ", txSum="
                + txSum
                + ", failedTxSum="
                + failedTxSum
                + "]";
    }

    public class InnerTotalTransactionCountResult {

        private BigInteger blockNumber;
        private BigInteger txSum;
        private BigInteger failedTxSum = BigInteger.ZERO;

        public InnerTotalTransactionCountResult() {}

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
}
