package console.web3j;

import console.account.AccountManager;
import java.io.IOException;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.exceptions.TransactionException;
import org.fisco.bcos.web3j.tx.gas.StaticGasProvider;
import org.fisco.bcos.web3j.tx.txdecode.BaseException;

public interface Web3jFace {

    void setWeb3j(Web3j web3j);

    void setAccountManager(AccountManager accountManager);

    void getBlockNumber(String[] params) throws IOException;

    void getPbftView(String[] params) throws IOException;

    void getObserverList(String[] params) throws IOException;

    void getSealerList(String[] params) throws IOException;

    void getConsensusStatus(String[] params) throws IOException;

    void getSyncStatus(String[] params) throws IOException;

    void getNodeVersion(String[] params) throws IOException;

    void getPeers(String[] params) throws IOException;

    void getNodeIDList(String[] params) throws IOException;

    void getGroupPeers(String[] params) throws IOException;

    void getGroupList(String[] params) throws IOException;

    void getBlockByHash(String[] params) throws IOException;

    void getBlockByNumber(String[] params) throws IOException;

    void getBlockHeaderByHash(String[] params) throws IOException;

    void getBlockHeaderByNumber(String[] params) throws IOException;

    void getBlockHashByNumber(String[] params) throws IOException;

    void getTransactionByHash(String[] params)
            throws IOException, BaseException, TransactionException;

    void getTransactionByBlockHashAndIndex(String[] params)
            throws IOException, BaseException, TransactionException;

    void getTransactionByBlockNumberAndIndex(String[] params)
            throws IOException, BaseException, TransactionException;

    void getTransactionReceipt(String[] params) throws Exception;

    void getTransactionByHashWithProof(String[] params) throws Exception;

    void getTransactionReceiptByHashWithProof(String[] params) throws Exception;

    void getPendingTxSize(String[] params) throws IOException;

    void getPendingTransactions(String[] params) throws IOException;

    void getCode(String[] params) throws IOException;

    void getTotalTransactionCount(String[] params) throws IOException;

    void getSystemConfigByKey(String[] params) throws Exception;

    void setGasProvider(StaticGasProvider gasProvider);
}
