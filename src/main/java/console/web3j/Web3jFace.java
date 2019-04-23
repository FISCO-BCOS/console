package console.web3j;

import java.io.IOException;

import org.fisco.bcos.web3j.protocol.Web3j;

public interface Web3jFace {

  void setWeb3j(Web3j web3j); 
  
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

  void getBlockHashByNumber(String[] params) throws IOException;

  void getTransactionByHash(String[] params) throws IOException;

  void getTransactionByBlockHashAndIndex(String[] params) throws IOException;

  void getTransactionByBlockNumberAndIndex(String[] params) throws IOException;

  void getTransactionReceipt(String[] params) throws IOException;

  void getPendingTxSize(String[] params) throws IOException;

  void getPendingTransactions(String[] params) throws IOException;

  void getCode(String[] params) throws IOException;

  void getTotalTransactionCount(String[] params) throws IOException;
  
  void getSystemConfigByKey(String[] params) throws Exception;

}
