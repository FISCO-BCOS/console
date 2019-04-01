package console.precompiled;

import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.protocol.Web3j;

public interface PrecompiledFace {
	
	void setWeb3j(Web3j web3j);
	void setCredentials(Credentials credentials);
	
	//ConsensusPrecompiled
  void addSealer(String[] params) throws Exception;

  void addObserver(String[] params) throws Exception;

  void removeNode(String[] params) throws Exception;
  
  //SystemConfigPrecompiled
  void setSystemConfigByKey(String[] params) throws Exception;
  
  //CRUDPrecompiled
  void createTable(String[] params);
  
  void insert(String[] params);
  
  void select(String[] params);
  
}
