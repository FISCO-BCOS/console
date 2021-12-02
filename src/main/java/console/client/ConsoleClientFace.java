package console.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import org.fisco.bcos.sdk.client.Client;

public interface ConsoleClientFace {
    void updateClient(Client client);

    void getBlockNumber(String[] params) throws IOException;

    void getPbftView(String[] params) throws IOException;

    void getObserverList(String[] params) throws IOException;

    void getSealerList(String[] params) throws IOException;

    void getSyncStatus(String[] params) throws IOException;

    void getConsensusStatus(String[] params) throws IOException;

    void getPeers(String[] params) throws IOException;

    void getBlockByHash(String[] params) throws IOException;

    void getBlockByNumber(String[] params) throws IOException;

    void getBlockHeaderByHash(String[] params) throws IOException;

    void getBlockHeaderByNumber(String[] params) throws IOException;

    void getTransactionByHash(String[] params);

    void getTransactionReceipt(String[] params) throws Exception;

    void getTransactionByHashWithProof(String[] params) throws Exception;

    void getTransactionReceiptByHashWithProof(String[] params) throws Exception;

    void getPendingTxSize(String[] params) throws IOException;

    void getCode(String[] params) throws IOException;

    void getTotalTransactionCount(String[] params) throws IOException;

    void getSystemConfigByKey(String[] params) throws Exception;

    void newAccount(String[] params);

    void listAccount(String[] params);

    void getGroupPeers(String[] params);

    void getGroupList(String[] params);

    void getGroupInfo(String[] params) throws IOException;

    void getGroupInfoList(String[] params) throws JsonProcessingException;

    void getGroupNodeInfo(String[] params) throws JsonProcessingException;

    void setNodeName(String[] params) throws IOException;

    void clearNodeName();

    void getNodeName();
}
