package console.client;

import java.io.IOException;
import org.fisco.bcos.sdk.client.Client;

public interface ConsoleClientFace {
    void updateClient(Client client);

    void getBlockNumber(String[] params) throws IOException;

    void getPbftView(String[] params) throws IOException;

    void getObserverList(String[] params) throws IOException;

    void getSealerList(String[] params) throws IOException;

    void getConsensusStatus(String[] params) throws IOException;

    void getSyncStatus(String[] params) throws IOException;

    void getNodeVersion(String[] params) throws IOException;

    void getNodeInfo(String[] params) throws IOException;

    void getPeers(String[] params) throws IOException;

    void getNodeIDList(String[] params) throws IOException;

    void getGroupPeers(String[] params) throws IOException;

    void getGroupList(String[] params) throws IOException;

    void getBlockByHash(String[] params) throws IOException;

    void getBlockByNumber(String[] params) throws IOException;

    void getBlockHeaderByHash(String[] params) throws IOException;

    void getBlockHeaderByNumber(String[] params) throws IOException;

    void getBlockHashByNumber(String[] params) throws IOException;

    void getTransactionByHash(String[] params);

    void getTransactionByBlockHashAndIndex(String[] params);

    void getTransactionByBlockNumberAndIndex(String[] params);

    void getTransactionReceipt(String[] params) throws Exception;

    void getTransactionByHashWithProof(String[] params) throws Exception;

    void getTransactionReceiptByHashWithProof(String[] params) throws Exception;

    void getPendingTxSize(String[] params) throws IOException;

    void getPendingTransactions(String[] params) throws IOException;

    void getCode(String[] params) throws IOException;

    void getTotalTransactionCount(String[] params) throws IOException;

    void getSystemConfigByKey(String[] params) throws Exception;

    void startGroup(String[] params);

    void stopGroup(String[] params);

    void removeGroup(String[] params);

    void recoverGroup(String[] params);

    void queryGroupStatus(String[] params);

    void generateGroup(String[] params);

    void generateGroupFromFile(String[] params);

    void newAccount(String[] params);

    void listAccount(String[] params);

    void getBatchReceiptsByBlockHashAndRange(String[] params);

    void getBatchReceiptsByBlockNumberAndRange(String[] params);
}
