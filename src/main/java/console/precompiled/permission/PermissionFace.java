package console.precompiled.permission;

import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.protocol.Web3j;

public interface PermissionFace {
		
  void setWeb3j(Web3j web3j);
  void setCredentials(Credentials credentials);
  
  void grantUserTableManager(String[] params) throws Exception;
  void revokeUserTableManager(String[] params) throws Exception;
  void listUserTableManager(String[] params) throws Exception;

  void grantDeployAndCreateManager(String[] params) throws Exception;
  void revokeDeployAndCreateManager(String[] params) throws Exception;
  void listDeployAndCreateManager(String[] params) throws Exception;

  void grantPermissionManager(String[] params) throws Exception;
  void revokePermissionManager(String[] params) throws Exception;
  void listPermissionManager(String[] params) throws Exception;

  void grantNodeManager(String[] params) throws Exception;
  void revokeNodeManager(String[] params) throws Exception;
  void listNodeManager(String[] params) throws Exception;

  void grantCNSManager(String[] params) throws Exception;
  void revokeCNSManager(String[] params) throws Exception;
  void listCNSManager(String[] params) throws Exception;

  void grantSysConfigManager(String[] params) throws Exception;
  void revokeSysConfigManager(String[] params) throws Exception;
  void listSysConfigManager(String[] params) throws Exception;
  
}
