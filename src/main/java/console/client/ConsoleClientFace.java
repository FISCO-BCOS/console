package console.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import org.fisco.bcos.sdk.client.Client;

public interface ConsoleClientFace {
    void updateClient(Client client);

    void getBlockNumber(String nodeName, String[] params) throws IOException;

    void getPbftView(String nodeName, String[] params) throws IOException;

    void getObserverList(String nodeName, String[] params) throws IOException;

    void getSealerList(String nodeName, String[] params) throws IOException;

    void getSyncStatus(String nodeName, String[] params) throws IOException;

    void getConsensusStatus(String nodeName, String[] params) throws IOException;

    void getPeers(String[] params) throws IOException;

    void getBlockByHash(String nodeName, String[] params) throws IOException;

    void getBlockByNumber(String nodeName, String[] params) throws IOException;

    void getBlockHeaderByHash(String nodeName, String[] params) throws IOException;

    void getBlockHeaderByNumber(String nodeName, String[] params) throws IOException;

    void getTransactionByHash(String nodeName, String[] params);

    void getTransactionReceipt(String nodeName, String[] params) throws Exception;

    void getTransactionByHashWithProof(String nodeName, String[] params) throws Exception;

    void getTransactionReceiptByHashWithProof(String nodeName, String[] params) throws Exception;

    void getPendingTxSize(String nodeName, String[] params) throws IOException;

    void getCode(String nodeName, String[] params) throws IOException;

    void getTotalTransactionCount(String nodeName, String[] params) throws IOException;

    void getSystemConfigByKey(String nodeName, String[] params) throws Exception;

    void newAccount(String[] params);

    void listAccount(String[] params);

    void getGroupPeers(String[] params);

    void getGroupList(String[] params);

    void getGroupInfo(String[] params) throws IOException;

    void getGroupInfoList(String[] params) throws JsonProcessingException;

    void getGroupNodeInfo(String[] params) throws JsonProcessingException;

    void setNodeName(String[] params) throws IOException;

    void clearNodeName();
}
