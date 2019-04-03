package console.precompiled;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.precompile.common.PrecompiledCommon;
import org.fisco.bcos.web3j.precompile.config.SystemConfigSerivce;
import org.fisco.bcos.web3j.precompile.consensus.ConsensusService;
import org.fisco.bcos.web3j.precompile.crud.CRUDSerivce;
import org.fisco.bcos.web3j.precompile.crud.Condition;
import org.fisco.bcos.web3j.precompile.crud.Entry;
import org.fisco.bcos.web3j.precompile.crud.Table;
import org.fisco.bcos.web3j.protocol.Web3j;

import console.common.CRUDParseUtils;
import console.common.Common;
import console.common.ConsoleUtils;
import console.common.HelpInfo;
import console.exception.ConsoleMessageException;
import net.sf.jsqlparser.JSQLParserException;

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
    
    @Override
    public void createTable(String sql) {
			Table table = new Table();
			try {
				CRUDParseUtils.parseCreateTable(sql, table);
			} catch (JSQLParserException e) {
				System.out.println(e.getMessage());
				System.out.println();
				return;
			}
			try {
					CRUDSerivce crudSerivce = new CRUDSerivce(web3j, credentials);
					int result = crudSerivce.createTable(table);
					if(result == 0)
					{
						System.out.println("Create table " + table.getTableName() + " successfully.");
					}
					else 
					{
						System.out.println("Create table " + table.getTableName() + " failure.");
					}
					System.out.println();
			} catch (Exception e) {
				System.out.println("Could not parse SQL statement.");
				System.out.println();
				return;
			}
    }
    
    @Override
    public void insert(String sql) {
    	CRUDSerivce crudSerivce = new CRUDSerivce(web3j, credentials);
    	Table table = new Table();
    	Entry entry = table.getEntry();
    	try {
				CRUDParseUtils.parseInsert(sql, table, entry);
			} catch (JSQLParserException e) {
				System.out.println("Could not parse SQL statement.");
				System.out.println();
				return;
			}
    	try {
    		String key = queryKey(table.getTableName());
    		table.setKey(key);
				crudSerivce.insert(table, entry);
				System.out.println("Insert successfully.");
				System.out.println();
    	} catch (Exception e) {
    		System.out.println(e.getMessage());
				System.out.println();
				return;
    	}
    }
    
    @Override
    public void update(String sql) {
    	CRUDSerivce crudSerivce = new CRUDSerivce(web3j, credentials);
    	Table table = new Table();
    	Entry entry = table.getEntry();
    	Condition condition = table.getCondition();
    	try {
    		CRUDParseUtils.parseUpdate(sql, table, entry, condition);
    	} catch (JSQLParserException e) {
    		System.out.println("Could not parse SQL statement.");
    		System.out.println();
    		return;
    	}
    	try {
    		String key = queryKey(table.getTableName());
    		table.setKey(key);
    		crudSerivce.update(table, entry, condition);
    		System.out.println("Update successfully.");
    		System.out.println();
    	} catch (Exception e) {
    		System.out.println(e.getMessage());
    		System.out.println();
    		return;
    	}
    }
    
    @Override
    public void remove(String sql) {
    	CRUDSerivce crudSerivce = new CRUDSerivce(web3j, credentials);
    	Table table = new Table();
    	Condition condition = table.getCondition();
    	try {
    		CRUDParseUtils.parseRemove(sql, table, condition);
    	} catch (JSQLParserException e) {
    		System.out.println("Could not parse SQL statement.");
    		System.out.println();
    		return;
    	}
    	try {
    		String key = queryKey(table.getTableName());
    		table.setKey(key);
    		crudSerivce.remove(table, condition);
    		System.out.println("Remove successfully.");
    		System.out.println();
    	} catch (Exception e) {
    		System.out.println(e.getMessage());
    		System.out.println();
    		return;
    	}
    }
    
    @Override
    public void select(String sql) {
    	Table table = new Table();
    	Condition condition = table.getCondition();
      List<String> selectColumns = new ArrayList<>();
			try {
				CRUDParseUtils.parseSelect(sql, table, condition, selectColumns);
			} catch (JSQLParserException e) {
				System.out.println("Could not parse SQL statement.");
				System.out.println();
				return;
			}
			CRUDSerivce crudSerivce = new CRUDSerivce(web3j, credentials);
    	try {
    		String key = queryKey(table.getTableName());
    		table.setKey(key);
    		List<Map<String, String>> result = crudSerivce.select(table, condition);
    		if (result.size() == 0) {
					System.out.println("Empty set.");
					System.out.println();
					return;
				}
    		
    		if("*".equals(selectColumns.get(0)))
    		{
    			result.stream().forEach(System.out::println);
    		}
    		else
    		{	
    			int size = result.size();
					List<Map<String, String>> selectedResult = new ArrayList<>(size);
					Map<String, String> selectedRecords = new HashMap<>();
    			for (Map<String, String> records : result) {
  					for (String column : selectColumns) {
  						Set<String> recordKeys = records.keySet();
  						for (String recordKey : recordKeys) {
								if (recordKey.equals(column)) {
									selectedRecords.put(recordKey, records.get(recordKey));
								}
							}
    				}
    				selectedResult.add(selectedRecords);
    			}
    			selectedResult.stream().forEach(System.out::println);
    		}
    	} catch (Exception e) {
    		System.out.println(e.getMessage());
    		System.out.println();
    		return;
    	}
    	System.out.println();
    }
    
  	private String queryKey(String tableName) throws Exception
  	{
  		CRUDSerivce crudSerivce = new CRUDSerivce(web3j, credentials);
    	Table table = new Table();
    	table.setTableName("_sys_tables_");
    	table.setKey("_user_" +tableName);
    	Condition condition = table.getCondition();
  		List<Map<String, String>> userTable = crudSerivce.select(table, condition);
  		if(userTable.size() != 0)
  		{
  			return userTable.get(0).get("key_field");
  		}
  		else 
  		{
  			throw new ConsoleMessageException("The table " + tableName + " does not exist.");
  		}
  	}
}
