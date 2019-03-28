package console.web3j;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.core.DefaultBlockParameter;
import org.fisco.bcos.web3j.protocol.core.DefaultBlockParameterName;
import org.fisco.bcos.web3j.protocol.core.methods.response.BcosBlock;
import org.fisco.bcos.web3j.utils.Numeric;

import com.alibaba.fastjson.JSONObject;

import console.common.Address;
import console.common.Common;
import console.common.ConsoleUtils;
import console.common.HelpInfo;

public class Web3jImpl implements Web3jFace {
	
	  private Web3j web3j;
	  
    @Override
    public void setWeb3j(Web3j web3j) {
			this.web3j = web3j;
		}

		@Override
    public void getNodeVersion(String[] params) throws IOException {
        if (HelpInfo.promptNoParams(params, "getNodeVersion")) {
            return;
        }
        
        String nodeVersion = web3j.getNodeVersion().sendForReturnString();
        ConsoleUtils.printJson(nodeVersion);
        System.out.println();
    }

    @Override
    public void getBlockNumber(String[] params) throws IOException {
        if (HelpInfo.promptNoParams(params, "getBlockNumber")) {
            return;
        }
        String blockNumber = web3j.getBlockNumber().sendForReturnString();
        System.out.println(Numeric.decodeQuantity(blockNumber));
        System.out.println();
    }

    @Override
    public void getPbftView(String[] params) throws IOException {
        if (HelpInfo.promptNoParams(params, "getPbftView")) {
            return;
        }
        String pbftView = web3j.getPbftView().sendForReturnString();
        System.out.println(Numeric.decodeQuantity(pbftView));
        System.out.println();
    }

    @Override
    public void getObserverList(String[] params) throws IOException {
        if (HelpInfo.promptNoParams(params, "getObserverList")) {
            return;
        }
        List<String> observerList = web3j.getObserverList().send().getResult();
        String observers = observerList.toString();
        if ("[]".equals(observers)) {
            System.out.println("[]");
        } else {
            ConsoleUtils.printJson(observers);
        }
        System.out.println();
    }

    @Override
    public void getSealerList(String[] params) throws IOException {
        if (HelpInfo.promptNoParams(params, "getSealerList")) {
            return;
        }
        List<String> sealerList = web3j.getSealerList().send().getResult();
        String sealers = sealerList.toString();
        if ("[]".equals(sealers)) {
            System.out.println("[]");
        } else {
            ConsoleUtils.printJson(sealers);
        }
        System.out.println();
    }

    @Override
    public void getConsensusStatus(String[] params) throws IOException {
        if (HelpInfo.promptNoParams(params, "getConsensusStatus")) {
            return;
        }
        String consensusStatus = web3j.getConsensusStatus().sendForReturnString();
        ConsoleUtils.printJson(consensusStatus);
        System.out.println();
    }

    @Override
    public void getSyncStatus(String[] params) throws IOException {
        if (HelpInfo.promptNoParams(params, "getSyncStatus")) {
            return;
        }
        String syncStatus = web3j.getSyncStatus().sendForReturnString();
        ConsoleUtils.printJson(syncStatus);
        System.out.println();
    }

    @Override
    public void getPeers(String[] params) throws IOException {
        if (HelpInfo.promptNoParams(params, "getPeers")) {
            return;
        }
        String peers = web3j.getPeers().sendForReturnString();
        ConsoleUtils.printJson(peers);
        System.out.println();
    }

    @Override
    public void getNodeIDList(String[] params) throws IOException {
        if (HelpInfo.promptNoParams(params, "getNodeIDList")) {
            return;
        }
        List<String> nodeIds = web3j.getNodeIDList().send().getResult();
        ConsoleUtils.printJson(nodeIds.toString());
        System.out.println();
    }

    @Override
    public void getGroupPeers(String[] params) throws IOException {
        if (HelpInfo.promptNoParams(params, "getGroupPeers")) {
            return;
        }
        List<String> groupPeers = web3j.getGroupPeers().send().getResult();
        ConsoleUtils.printJson(groupPeers.toString());
        System.out.println();
    }

    @Override
    public void getGroupList(String[] params) throws IOException {
        if (HelpInfo.promptNoParams(params, "getGroupList")) {
            return;
        }
        List<String> groupList = web3j.getGroupList().send().getResult();
        System.out.println(groupList);
        System.out.println();
    }

    @Override
    public void getBlockByHash(String[] params) throws IOException {
        if (params.length < 2) {
            HelpInfo.promptHelp("getBlockByHash");
            return;
        }
        if (params.length > 3) {
            HelpInfo.promptHelp("getBlockByHash");
            return;
        }
        String blockHash = params[1];
        if ("-h".equals(blockHash) || "--help".equals(blockHash)) {
            HelpInfo.getBlockByHashHelp();
            return;
        }
        if (ConsoleUtils.isInvalidHash(blockHash)) return;
        boolean flag = false;
        if (params.length == 3) {
            if ("true".equals(params[2])) {
                flag = true;
            } else if ("false".equals(params[2])) {
                flag = false;
            } else {
                System.out.println("Please provide true or false for the second parameter.");
                System.out.println();
                return;
            }
        }
        String block = web3j.getBlockByHash(blockHash, flag).sendForReturnString();
        ConsoleUtils.printJson(block);
        System.out.println();
    }

    @Override
    public void getBlockByNumber(String[] params) throws IOException {
        if (params.length < 2) {
            HelpInfo.promptHelp("getBlockByNumber");
            return;
        }
        if (params.length > 3) {
            HelpInfo.promptHelp("getBlockByNumber");
            return;
        }
        String blockNumberStr1 = params[1];
        if ("-h".equals(blockNumberStr1) || "--help".equals(blockNumberStr1)) {
            HelpInfo.getBlockByNumberHelp();
            return;
        }
        int blockNumber = ConsoleUtils.proccessNonNegativeNumber("blockNumber", blockNumberStr1);
        if(blockNumber == Common.InvalidReturnNumber){
        	return;
        }
        BigInteger blockNumber1 = new BigInteger(blockNumberStr1);
        String blockNumberStr2 = web3j.getBlockNumber().sendForReturnString();
        BigInteger blockNumber2 = Numeric.decodeQuantity(blockNumberStr2);
        if (blockNumber1.compareTo(blockNumber2) > 0) {
            System.out.println("BlockNumber does not exist.");
            System.out.println();
            return;
        }
        boolean flag = false;
        if (params.length == 3) {
            if ("true".equals(params[2])) {
                flag = true;
            } else if ("false".equals(params[2])) {
                flag = false;
            } else {
                System.out.println("Please provide true or false for the second parameter.");
                System.out.println();
                return;
            }
        }
        String block =
                web3j
                        .getBlockByNumber(DefaultBlockParameter.valueOf(blockNumber1), flag)
                        .sendForReturnString();
        ConsoleUtils.printJson(block);
        System.out.println();
    }

    @Override
    public void getBlockHashByNumber(String[] params) throws IOException {
        if (params.length < 2) {
            HelpInfo.promptHelp("getBlockHashByNumber");
            return;
        }
        if (params.length > 2) {
            HelpInfo.promptHelp("getBlockHashByNumber");
            return;
        }
        String blockNumberStr = params[1];
        if ("-h".equals(blockNumberStr) || "--help".equals(blockNumberStr)) {
            HelpInfo.getBlockHashByNumberHelp();
            return;
        }
        int blockNumberi = ConsoleUtils.proccessNonNegativeNumber("blockNumber", blockNumberStr);
        if(blockNumberi == Common.InvalidReturnNumber)
        {
        	return;
        }
        BigInteger blockNumber = BigInteger.valueOf(blockNumberi);
        BigInteger getBlockNumber =
                Numeric.decodeQuantity(web3j.getBlockNumber().sendForReturnString());
        if (blockNumber.compareTo(getBlockNumber) > 0) {
            System.out.println("The block number doesn't exsit.");
            System.out.println();
            return;
        }
        String blockHash =
                web3j
                        .getBlockHashByNumber(DefaultBlockParameter.valueOf(blockNumber))
                        .sendForReturnString();
        ConsoleUtils.printJson(blockHash);
        System.out.println();
    }

    @Override
    public void getTransactionByHash(String[] params) throws IOException {
        if (params.length < 2) {
            HelpInfo.promptHelp("getTransactionByHash");
            return;
        }
        if (params.length > 2) {
            HelpInfo.promptHelp("getTransactionByHash");
            return;
        }
        String transactionHash = params[1];
        if ("-h".equals(transactionHash) || "--help".equals(transactionHash)) {
            HelpInfo.getTransactionByHashHelp();
            return;
        }
        if (ConsoleUtils.isInvalidHash(transactionHash)) return;
        String transaction = web3j.getTransactionByHash(transactionHash).sendForReturnString();
        if ("null".equals(transaction)) {
            System.out.println("This transaction hash doesn't exist.");
            return;
        }
        ConsoleUtils.printJson(transaction);
        System.out.println();
    }

    @Override
    public void getTransactionByBlockHashAndIndex(String[] params) throws IOException {
        if (params.length < 2) {
            HelpInfo.promptHelp("getTransactionByBlockHashAndIndex");
            return;
        }
        if (params.length > 3) {
            HelpInfo.promptHelp("getTransactionByBlockHashAndIndex");
            return;
        }
        String blockHash = params[1];
        if ("-h".equals(blockHash) || "--help".equals(blockHash)) {
            HelpInfo.getTransactionByBlockHashAndIndexHelp();
            return;
        }
        if (params.length < 3) {
            HelpInfo.promptHelp("getTransactionByBlockHashAndIndex");
            return;
        }
        if (ConsoleUtils.isInvalidHash(blockHash)) 
        {
        	return;
        }
        String indexStr = params[2];
        int index = ConsoleUtils.proccessNonNegativeNumber("index", indexStr);
        if (index == Common.InvalidReturnNumber) {
					return;
				}
        BcosBlock bcosBlock = web3j.getBlockByHash(blockHash, false).send();
				int maxIndex = bcosBlock.getResult().getTransactions().size() - 1;
				if(index > maxIndex)
				{
	        System.out.println("The index is out of range.");
	        System.out.println();
	        return;
				}
        String transaction =
                web3j.getTransactionByBlockHashAndIndex(blockHash, BigInteger.valueOf(index)).sendForReturnString();
        ConsoleUtils.printJson(transaction);
        System.out.println();
    }

    @Override
    public void getTransactionByBlockNumberAndIndex(String[] params) throws IOException {
        if (params.length < 2) {
            HelpInfo.promptHelp("getTransactionByBlockNumberAndIndex");
            return;
        }
        if (params.length > 3) {
            HelpInfo.promptHelp("getTransactionByBlockNumberAndIndex");
            return;
        }
        String blockNumberStr = params[1];
        if ("-h".equals(blockNumberStr) || "--help".equals(blockNumberStr)) {
            HelpInfo.getTransactionByBlockNumberAndIndexHelp();
            return;
        }
        if (params.length < 3) {
            HelpInfo.promptHelp("getTransactionByBlockNumberAndIndex");
            return;
        }
        int blockNumber = ConsoleUtils.proccessNonNegativeNumber("blockNumber", blockNumberStr);
        if(blockNumber == Common.InvalidReturnNumber){
        	return;
        }
        BigInteger getBlockNumber =
            Numeric.decodeQuantity(web3j.getBlockNumber().sendForReturnString());
		    if (BigInteger.valueOf(blockNumber).compareTo(getBlockNumber) > 0) {
		        System.out.println("The block number doesn't exsit.");
		        System.out.println();
		        return;
		    }
        String indexStr = params[2];
        int index = ConsoleUtils.proccessNonNegativeNumber("index", indexStr);
        if (index == Common.InvalidReturnNumber) {
					return;
				}
        BcosBlock bcosBlock = web3j.getBlockByNumber(DefaultBlockParameter.valueOf(BigInteger.valueOf(blockNumber)), false).send();
				int maxIndex = bcosBlock.getResult().getTransactions().size() - 1;
				if(index > maxIndex)
				{
	        System.out.println("The index is out of range.");
	        System.out.println();
	        return;
				}
        String transaction = web3j
						.getTransactionByBlockNumberAndIndex(DefaultBlockParameter.valueOf(BigInteger.valueOf(blockNumber)), BigInteger.valueOf(index))
						.sendForReturnString();
				ConsoleUtils.printJson(transaction);
				System.out.println();
    }

    @Override
    public void getTransactionReceipt(String[] params) throws IOException {
        if (params.length < 2) {
            HelpInfo.promptHelp("getTransactionReceipt");
            return;
        }
        if (params.length > 2) {
            HelpInfo.promptHelp("getTransactionReceipt");
            return;
        }
        String transactionHash = params[1];
        if ("-h".equals(transactionHash) || "--help".equals(transactionHash)) {
            HelpInfo.getTransactionReceiptHelp();
            return;
        }
        if (ConsoleUtils.isInvalidHash(transactionHash)) return;
        String transactionReceipt = web3j.getTransactionReceipt(transactionHash).sendForReturnString();
        if ("null".equals(transactionReceipt)) {
            System.out.println("This transaction hash doesn't exist.");
            System.out.println();
            return;
        }
        ConsoleUtils.printJson(transactionReceipt);
        System.out.println();
    }

    @Override
    public void getPendingTxSize(String[] params) throws IOException {
        if (HelpInfo.promptNoParams(params, "getPendingTxSize")) {
            return;
        }
        String size = web3j.getPendingTxSize().sendForReturnString();
        System.out.println(Numeric.decodeQuantity(size));
        System.out.println();
    }

    @Override
    public void getPendingTransactions(String[] params) throws IOException {
        if (HelpInfo.promptNoParams(params, "getPendingTransactions")) {
            return;
        }
        String pendingTransactions = web3j.getPendingTransaction().sendForReturnString();
        if ("[]".equals(pendingTransactions)) System.out.println(pendingTransactions);
        else ConsoleUtils.printJson(pendingTransactions);
        System.out.println();
    }

    @Override
    public void getCode(String[] params) throws IOException {
        if (params.length < 2) {
            HelpInfo.promptHelp("getCode");
            return;
        }
        if (params.length > 2) {
            HelpInfo.promptHelp("getCode");
            return;
        }
        String address = params[1];
        if ("-h".equals(address) || "--help".equals(address)) {
            HelpInfo.getCodeHelp();
            return;
        }
        Address convertAddr = ConsoleUtils.convertAddress(address);
        if (!convertAddr.isValid()) {
            return;
        }
        address = convertAddr.getAddress();
        String code = web3j.getCode(address, DefaultBlockParameterName.LATEST).sendForReturnString();
        if ("0x".equals(code)) {
            System.out.println("This address doesn't exist.");
            System.out.println();
            return;
        }
        ConsoleUtils.printJson(code);
        System.out.println();
    }

    @Override
    public void getTotalTransactionCount(String[] params) throws IOException {
        if (HelpInfo.promptNoParams(params, "getTotalTransactionCount")) {
            return;
        }
        String transactionCount = web3j.getTotalTransactionCount().sendForReturnString();
        JSONObject jo = JSONObject.parseObject(transactionCount);
        jo.put("txSum", Numeric.decodeQuantity(jo.get("txSum").toString()));
        jo.put("blockNumber", Numeric.decodeQuantity(jo.get("blockNumber").toString()));
        ConsoleUtils.printJson(jo.toJSONString());
        System.out.println();
    }
    
    @Override
    public void getSystemConfigByKey(String[] params) throws Exception {
        if (params.length < 2) {
            HelpInfo.promptHelp("getSystemConfigByKey");
            return;
        }
        if (params.length > 2) {
            HelpInfo.promptHelp("getSystemConfigByKey");
            return;
        }
        String key = params[1];
        if ("-h".equals(key) || "--help".equals(key)) {
            HelpInfo.getSystemConfigByKeyHelp();
            return;
        }
      	if (Common.TxCountLimit.equals(key) || Common.TxGasLimit.equals(key)) {
      		String value = web3j.getSystemConfigByKey(key).sendForReturnString();
      		System.out.println(value);
      	}
      	else 
      	{
      		System.out.println("Please provide a valid key, for example: " + Common.TxCountLimit +" or " + Common.TxGasLimit +".");
      	}
      	System.out.println();
    }
}
