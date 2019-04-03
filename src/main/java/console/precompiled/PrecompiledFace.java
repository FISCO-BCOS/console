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
  void createTable(String sql);
  
  void insert(String sql);
  
  void update(String sql);
  
  void remove(String sql);
  
  void select(String sql);
  
}
