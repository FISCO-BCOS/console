package console.precompiled;

import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.precompile.common.PrecompiledCommon;
import org.fisco.bcos.web3j.precompile.config.SystemConfigSerivce;
import org.fisco.bcos.web3j.precompile.consensus.ConsensusService;
import org.fisco.bcos.web3j.protocol.Web3j;

import console.common.Common;
import console.common.ConsoleUtils;
import console.common.HelpInfo;

public class PrecompiledImpl implements PrecompiledFace {
		
	  private Web3j web3j;
	  private Credentials credentials;
	  
		@Override
		public void setWeb3j(Web3j web3j)
		{
			this.web3j = web3j;
		}
		@Override
		public void setCredentials(Credentials credentials)
		{
			this.credentials = credentials;
		}
		
    @Override
    public void addSealer(String[] params) throws Exception {
        if (params.length < 2) {
            HelpInfo.promptHelp("addSealer");
            return;
        }
        if (params.length > 2) {
            HelpInfo.promptHelp("addSealer");
            return;
        }
        String nodeId = params[1];
        if ("-h".equals(nodeId) || "--help".equals(nodeId)) {
            HelpInfo.addSealerHelp();
            return;
        }
        if (nodeId.length() != 128) {
            ConsoleUtils.printJson(PrecompiledCommon.transferToJson(PrecompiledCommon.InvalidNodeId));
        } else {
            ConsensusService consensusService = new ConsensusService(web3j, credentials);
            String result;
            result = consensusService.addSealer(nodeId);
            ConsoleUtils.printJson(result);
        }
        System.out.println();
    }

    @Override
    public void addObserver(String[] params) throws Exception {

        if (params.length < 2) {
            HelpInfo.promptHelp("addObserver");
            return;
        }
        if (params.length > 2) {
            HelpInfo.promptHelp("addObserver");
            return;
        }
        String nodeId = params[1];
        if ("-h".equals(nodeId) || "--help".equals(nodeId)) {
            HelpInfo.addObserverHelp();
            return;
        }
        if (nodeId.length() != 128) {
            ConsoleUtils.printJson(PrecompiledCommon.transferToJson(PrecompiledCommon.InvalidNodeId));
        } else {
						ConsensusService consensusService = new ConsensusService(web3j, credentials);
						String result = consensusService.addObserver(nodeId);
						ConsoleUtils.printJson(result);
        }
        System.out.println();
    }

    @Override
    public void removeNode(String[] params) throws Exception {
        if (params.length < 2) {
            HelpInfo.promptHelp("removeNode");
            return;
        }
        if (params.length > 2) {
            HelpInfo.promptHelp("removeNode");
            return;
        }
        String nodeId = params[1];
        if ("-h".equals(nodeId) || "--help".equals(nodeId)) {
            HelpInfo.removeNodeHelp();
            return;
        }
        if (nodeId.length() != 128) {
            ConsoleUtils.printJson(PrecompiledCommon.transferToJson(PrecompiledCommon.InvalidNodeId));
        } else {
            ConsensusService consensusService = new ConsensusService(web3j, credentials);
            String result = null;
            result = consensusService.removeNode(nodeId);
            ConsoleUtils.printJson(result);
        }
        System.out.println();
    }
    
    @Override
    public void setSystemConfigByKey(String[] params) throws Exception {
        if (params.length < 2) {
            HelpInfo.promptHelp("setSystemConfigByKey");
            return;
        }
        if (params.length > 3) {
            HelpInfo.promptHelp("setSystemConfigByKey");
            return;
        }
        String key = params[1];
        if ("-h".equals(key) || "--help".equals(key)) {
            HelpInfo.setSystemConfigByKeyHelp();
            return;
        }
        if (params.length < 3) {
            HelpInfo.promptHelp("setSystemConfigByKey");
            return;
        }
      	if (Common.TxCountLimit.equals(key) || Common.TxGasLimit.equals(key)) {
          String valueStr = params[2];
          int value = 1;
          try {
          		value = Integer.parseInt(valueStr);
          		if (Common.TxCountLimit.equals(key) ) {
          			if(value <= 0)
          			{
          				System.out.println("Please provide value by positive integer mode, " + Common.PositiveIntegerRange +".");
              		System.out.println();
          				return;
          			}
							}
          		else 
          		{
          			if(value < Common.TxGasLimitMin)
          			{
          				System.out.println("Please provide value by positive integer mode, " + Common.TxGasLimitRange +".");
              		System.out.println();
          				return;
          			}
          		}
              SystemConfigSerivce systemConfigSerivce = new SystemConfigSerivce(web3j, credentials);
              String result = systemConfigSerivce.setValueByKey(key, value+"");
              ConsoleUtils.printJson(result);
          } catch (NumberFormatException e) {
        		if (Common.TxCountLimit.equals(key) ) {
              System.out.println("Please provide value by positive integer mode, " + Common.PositiveIntegerRange +".");
        		}
        		else 
        		{
        			System.out.println("Please provide value by positive integer mode, " + Common.TxGasLimitRange +".");
        		}
        		System.out.println();
        		return;
          }
				}
      	else
      	{
          System.out.println("Please provide a valid key, for example: " + Common.TxCountLimit +" or " + Common.TxGasLimit +".");
      	}
      	System.out.println();
    }
}
