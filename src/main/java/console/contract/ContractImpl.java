package console.contract;

import static console.common.ContractClassFactory.getContractClass;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.precompile.cns.CnsInfo;
import org.fisco.bcos.web3j.precompile.cns.CnsService;
import org.fisco.bcos.web3j.precompile.common.PrecompiledCommon;
import org.fisco.bcos.web3j.precompile.permission.PermissionInfo;
import org.fisco.bcos.web3j.precompile.permission.PermissionService;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.core.RemoteCall;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.tx.Contract;
import org.fisco.bcos.web3j.tx.gas.ContractGasProvider;
import org.fisco.bcos.web3j.tx.gas.StaticGasProvider;

import console.common.Address;
import console.common.Common;
import console.common.ConsoleUtils;
import console.common.ContractClassFactory;
import console.common.HelpInfo;
import console.exception.CompileSolidityException;
import console.exception.ConsoleMessageException;
import io.bretty.console.table.Alignment;
import io.bretty.console.table.ColumnFormatter;
import io.bretty.console.table.Table;

public class ContractImpl implements ContractFace {
	
		  private int groupID;
		  private Credentials credentials;
		  private StaticGasProvider gasProvider;
		  private Web3j web3j;
		  
	    private String contractAddress;
	    private String contractName;
	    private String contractVersion;
	    private Class<?> contractClass;
	    private RemoteCall<?> remoteCall;
	
			@Override
			public void setGroupID(int groupID) {
				this.groupID = groupID;
			}
			@Override
			public void setWeb3j(Web3j web3j) {
				this.web3j = web3j;
			}
			@Override
		  public void setGasProvider(StaticGasProvider gasProvider) {
				this.gasProvider = gasProvider;
			}
		  @Override
		  public void setCredentials(Credentials credentials) {
				this.credentials = credentials;
			}
	  
		 @Override
	    public void deploy(String[] params) throws Exception {
	        if (params.length < 2) {
	            HelpInfo.promptHelp("deploy");
	            return;
	        }
	        if ("-h".equals(params[1]) || "--help".equals(params[1])) {
	            HelpInfo.deployHelp();
	            return;
	        }
	        String name = params[1];
	        if (name.endsWith(".sol")) {
	            name = name.substring(0, name.length() - 4);
	        }
	        try {
	        	ConsoleUtils.dynamicCompileSolFilesToJava(name);
	        }catch (CompileSolidityException e) {
	        	System.out.println(e.getMessage());
	        	return;
	        }catch (IOException e) {
	        	System.out.println(e.getMessage());
	        	System.out.println();
	        	return;
	        }
	        ConsoleUtils.dynamicCompileJavaToClass(name);
	        contractName = ConsoleUtils.PACKAGENAME + "." + name;
	        try {
	            contractClass = getContractClass(contractName);
	        } catch (Exception e) {
	            System.out.println(
	                    "There is no " + name + ".class" + " in the directory of solidity/java/classes/org/fisco/bcos/temp/");
	            System.out.println();
	            return;
	        }
	        try {
						handleDeployParameters(params, 2);
					} catch (ConsoleMessageException e) {
						System.out.println(e.getMessage());
						System.out.println();
						return;
					}
	        try {
	        	Contract contract = (Contract) remoteCall.send();
	      	  contractAddress = contract.getContractAddress();
	          System.out.println(contractAddress);
	          System.out.println();
	          contractAddress = contract.getContractAddress();
	          writeLog();
	        } catch (Exception e) {
	            if (e.getMessage().contains("0x19")) {
	                ConsoleUtils.printJson(PrecompiledCommon.transferToJson(PrecompiledCommon.PermissionDenied));
	            } else {
	                throw e;
	            }
	        }

	    }

	    synchronized private void writeLog() {
	    	
	    	BufferedReader reader = null;
	    	try {
	  			File logFile = new File("deploylog.txt");
	  			if (!logFile.exists()) {
	  				logFile.createNewFile();
	  			}
					reader = new BufferedReader(new FileReader("deploylog.txt"));
					String line;
					List<String> textList = new ArrayList<String>();
					while ((line = reader.readLine()) != null) {
							textList.add(line);
					}
					int i = 0;
					if (textList.size() >= Common.LogMaxCount) {
						i = textList.size() - Common.LogMaxCount + 1;
	          if(logFile.exists()){
	              logFile.delete();
	              logFile.createNewFile();
	          }
	          PrintWriter pw = new PrintWriter(new FileWriter("deploylog.txt",true));
	          for(; i < textList.size(); i++)
	          {
	          	pw.println(textList.get(i));
	          }
	          pw.flush();
	          pw.close();
					}
				}
				catch(IOException e)
				{
					System.out.println("Read deploylog.txt failed.");
					return;
				}
				finally 
				{
					try {
						reader.close();
					} catch (IOException e) {
						System.out.println("Close deploylog.txt failed.");;
					}
				}
	       DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	       String name =  contractName.substring(20);
	       while(name.length() < 20){
	           name = name + " ";
	       }
	        String log = LocalDateTime.now().format(formatter) + "  [group:"+ groupID +  "]  " + name + "  " + contractAddress;
	        try {
	            File logFile =  new File("deploylog.txt");
	            if(!logFile.exists()){
	                logFile.createNewFile();
	            }
	            PrintWriter pw = new PrintWriter(new FileWriter("deploylog.txt",true));
	            pw.println(log);
	            pw.flush();
	            pw.close();
	        } catch (IOException e) {
	            System.out.println(e.getMessage());
	            System.out.println();
	            return;
	        }
	    }

			public void getDeployLog(String[] params) throws Exception {
				
				if (params.length > 2) {
					HelpInfo.promptHelp("getDeployLog");
					return;
				}
				String queryRecordNumber = "";
				int recordNumber = Common.QueryLogCount;
				if (params.length == 2) {
					queryRecordNumber = params[1];
					if ("-h".equals(queryRecordNumber) || "--help".equals(queryRecordNumber)) {
						HelpInfo.getDeployLogHelp();
						return;
					}
					try {
						recordNumber = Integer.parseInt(queryRecordNumber);
						if (recordNumber <= 0 || recordNumber > 100) {
							System.out.println("Please provide record number by integer mode, " + Common.DeployLogntegerRange +".");
							System.out.println();
							return;
						}
					} catch (NumberFormatException e) {
						System.out.println("Please provide record number by integer mode, " + Common.DeployLogntegerRange +".");
						System.out.println();
						return;
					}
				}
				File logFile = new File("deploylog.txt");
				if (!logFile.exists()) {
					logFile.createNewFile();
				}
				BufferedReader reader = new BufferedReader(new FileReader("deploylog.txt"));
				String line;
				String ls = System.getProperty("line.separator");
				List<String> textList = new ArrayList<String>();
				try {
					while ((line = reader.readLine()) != null) {
						String[] contractInfos = ConsoleUtils.tokenizeCommand(line);
						if (("[group:" + groupID + "]").equals(contractInfos[2])) {
							textList.add(line);
						}
					}
					StringBuilder stringBuilder = new StringBuilder();
					int i = 0;
					int len = textList.size();
					if(recordNumber >= len)
					{
						recordNumber = len;
					}
					else
					{
						i = len - recordNumber;
					}
					for (; i < len; i++) {
						stringBuilder.append(textList.get(i));
						stringBuilder.append(ls);
					}
					if ("".equals(stringBuilder.toString())) {
						System.out.println("Empty set.");
						System.out.println();
					} else {
						System.out.println();
						System.out.println(stringBuilder.toString());
					}
				} finally {
					reader.close();
				}
			}

	    @Override
	    public void call(String[] params) throws Exception {
	        if (params.length < 2) {
	            HelpInfo.promptHelp("call");
	            return;
	        }
	        if ("-h".equals(params[1]) || "--help".equals(params[1])) {
	            HelpInfo.callHelp();
	            return;
	        }
	        if (params.length < 4) {
	            HelpInfo.promptHelp("call");
	            return;
	        }
	        String name = params[1];
	        if (name.endsWith(".sol")) {
	            name = name.substring(0, name.length() - 4);
	        }
	        contractName = ConsoleUtils.PACKAGENAME + "." + name;
	        try {
	            contractClass = getContractClass(contractName);
	        } catch (Exception e) {
	            System.out.println(
	                    "There is no "
	                            + name
	                            + ".class"
	                            + " in the directory of java/classes/org/fisco/bcos/temp/");
	            System.out.println();
	            return;
	        }
	        Method load =
	                contractClass.getMethod(
	                        "load",
	                        String.class,
	                        Web3j.class,
	                        Credentials.class,
	                        ContractGasProvider.class);
	        Object contractObject;

	        contractAddress = params[2];
	        Address convertAddr = ConsoleUtils.convertAddress(contractAddress);
	        if (!convertAddr.isValid()) {
	            return;
	        }
	        contractAddress = convertAddr.getAddress();
	        contractObject = load.invoke(null, contractAddress, web3j, credentials, gasProvider);
	        String funcName = params[3];
	        Method[] methods = contractClass.getDeclaredMethods();
	        Method method = ContractClassFactory.getMethodByName(funcName, methods);
	        if(method == null) {
	        	System.out.println("Cannot find the method. Please checkout the method name.");
	        	System.out.println();
	        	return;
	        }
	        String[] generic = new String[method.getParameterCount()];
	        Type[] classType = method.getParameterTypes();
	        for (int i = 0; i < classType.length; i++) {
	            generic[i] = method.getGenericParameterTypes()[i].getTypeName();
	        }
	        Class[] classList = new Class[classType.length];
	        for (int i = 0; i < classType.length; i++) {
	            Class clazz = (Class) classType[i];
	            classList[i] = clazz;
	        }
	        Class[] parameterType =
	                ContractClassFactory.getParameterType(contractClass, funcName, params.length - 4);
	        if (parameterType == null) {
	            HelpInfo.promptNoFunc(params[1], funcName, params.length - 4);
	            return;
	        }
	        Method func = contractClass.getMethod(funcName, parameterType);
	        String[] newParams = new String[params.length - 4];
	        System.arraycopy(params, 4, newParams, 0, params.length - 4);
	        Object[] argobj = ContractClassFactory.getPrametersObject(funcName, parameterType, newParams, generic);
	        if (argobj == null) {
	            return;
	        }
	        remoteCall = (RemoteCall<?>) func.invoke(contractObject, argobj);
	        Object result;
					result = remoteCall.send();
					if(result instanceof TransactionReceipt)
					{
						TransactionReceipt receipt = (TransactionReceipt)result;
						if(!"0x0".equals(receipt.getStatus()))
						{
							System.out.println("Call failed.");
							System.out.println();
							return;
						}
					}
	        String returnObject =
	                ContractClassFactory.getReturnObject(contractClass, funcName, parameterType, result);
	        if (returnObject == null) {
	            HelpInfo.promptNoFunc(params[1], funcName, params.length - 4);
	            return;
	        }
	        System.out.println(returnObject);
	        System.out.println();

	    }

	    @Override
	    public void deployByCNS(String[] params) throws Exception {
	        if (params.length < 2) {
	            HelpInfo.promptHelp("deployByCNS");
	            return;
	        }
	        if ("-h".equals(params[1]) || "--help".equals(params[1])) {
	            HelpInfo.deployByCNSHelp();
	            return;
	        }
	        if (params.length < 3) {
	            HelpInfo.promptHelp("deployByCNS");
	            return;
	        }
	        PermissionService permissionTableService = new PermissionService(web3j, credentials);
	        List<PermissionInfo> permissions = permissionTableService.listCNSManager();
	        boolean flag = false;
	        if (permissions.size() == 0) {
	            flag = true;
	        } else {
	            for (PermissionInfo permission : permissions) {
	                if ((credentials.getAddress()).equals(permission.getAddress())) {
	                    flag = true;
	                    break;
	                }
	            }
	        }
	        if (!flag) {
	            ConsoleUtils.printJson(PrecompiledCommon.transferToJson(PrecompiledCommon.PermissionDenied));
	            System.out.println();
	            return;
	        }

	        String name = params[1];
	        if (name.endsWith(".sol")) {
	            name = name.substring(0, name.length() - 4);
	        }
	        CnsService cnsService = new CnsService(web3j, credentials);
	        List<CnsInfo> qcns = cnsService.queryCnsByNameAndVersion(name, params[2]);
	        if (qcns.size() != 0) {
	            ConsoleUtils.printJson(PrecompiledCommon.transferToJson(PrecompiledCommon.ContractNameAndVersionExist));
	            System.out.println();
	            return;
	        }
	        try {
	            ConsoleUtils.dynamicCompileSolFilesToJava(name);
	        } catch (CompileSolidityException e) {
	        	System.out.println(e.getMessage());
	        	return;
	        } catch (IOException e) {
	            System.out.println(e.getMessage());
	            System.out.println();
	            return;
	        }
	        contractName = ConsoleUtils.PACKAGENAME + "." + name;
	        ConsoleUtils.dynamicCompileJavaToClass(name);
	        try {
	            contractClass = getContractClass(contractName);
	        } catch (Exception e) {
	        	  System.out.println(
	               "There is no " + name + ".class" + " in the directory of solidity/java/classes/org/fisco/bcos/temp/.");
	            System.out.println();
	            return;
	        }
	        try {
						handleDeployParameters(params, 3);
					} catch (ConsoleMessageException e) {
						System.out.println(e.getMessage());
						System.out.println();
						return;
					}
	        contractVersion = params[2];
	        if (contractVersion.length() > CnsService.MAX_VERSION_LENGTH) {
	            ConsoleUtils.printJson(PrecompiledCommon.transferToJson(PrecompiledCommon.VersionExceeds));
	            System.out.println();
	            return;
	        }
	        try {
	        		Contract contract = (Contract) remoteCall.send();
	            contractAddress = contract.getContractAddress();
	            // register cns
	            String result = cnsService.registerCns(name, contractVersion, contractAddress, "");
	            System.out.println(contractAddress);
	            contractName = contractName+":"+contractVersion;
	            writeLog();
	            System.out.println();
	        } catch (Exception e) {
	            if (e.getMessage().contains("0x19")) {
	                ConsoleUtils.printJson(PrecompiledCommon.transferToJson(PrecompiledCommon.PermissionDenied));
	            } else {
	                throw e;
	            }
	        }

	    }

			private void handleDeployParameters(String[] params, int num) throws IllegalAccessException, InvocationTargetException, ConsoleMessageException {
				Method method = ContractClassFactory.getDeployFunction(contractClass);
				Type[] classType = method.getParameterTypes();
				if(classType.length - 3  != params.length - num) {
					throw new ConsoleMessageException("The number of paramters does not match!");
				}
				String[] generic = new String[method.getParameterCount()];
				for (int i = 0; i < classType.length; i++) {
				    generic[i] = method.getGenericParameterTypes()[i].getTypeName();
				}
				Class[] classList = new Class[classType.length];
				for (int i = 0; i < classType.length; i++) {
				    Class clazz = (Class) classType[i];
				    classList[i] = clazz;
				}

				String[] newParams = new String[params.length - num];
				System.arraycopy(params, num, newParams, 0, params.length - num);
				Object[] obj = getDeployPrametersObject("deploy", classList, newParams, generic);
				remoteCall = (RemoteCall<?>) method.invoke(null, obj);
			}

	    @SuppressWarnings("rawtypes")
	    @Override
	    public void callByCNS(String[] params) throws Exception {
	        if (params.length < 2) {
	            HelpInfo.promptHelp("callByCNS");
	            return;
	        }
	        if ("-h".equals(params[1]) || "--help".equals(params[1])) {
	            HelpInfo.callByCNSHelp();
	            return;
	        }
	        if (params.length < 4) {
	            HelpInfo.promptHelp("callByCNS");
	            return;
	        }
	        String name = params[1];
	        if (name.endsWith(".sol")) {
	            name = name.substring(0, name.length() - 4);
	        }
	        contractName = ConsoleUtils.PACKAGENAME + "." + name;
	        try {
	            contractClass = getContractClass(contractName);
	        } catch (Exception e) {
	            System.out.println(
	                    "There is no "
	                            + name
	                            + ".class"
	                            + " in the directory of java/classes/org/fisco/bcos/temp");
	            System.out.println();
	            return;
	        }
	        Method load =
	                contractClass.getMethod(
	                        "load",
	                        String.class,
	                        Web3j.class,
	                        Credentials.class,
	                        ContractGasProvider.class);
	        Object contractObject;

	        // get address from cns
	        contractName = name;
	        contractVersion = params[2];
	        if (contractVersion.length() > CnsService.MAX_VERSION_LENGTH) {
	          ConsoleUtils.printJson(PrecompiledCommon.transferToJson(PrecompiledCommon.VersionExceeds));
	          System.out.println();
	          return;
	        }
	        CnsService cnsResolver = new CnsService(web3j, credentials);
	        try {
	            contractAddress =
	                    cnsResolver.getAddressByContractNameAndVersion(contractName + ":" + contractVersion);
	        } catch (Exception e) {
	            System.out.println(
	                    "The contract " + contractName + " for version " + contractVersion + " doesn't exsit.");
	            System.out.println();
	            return;
	        }
	        contractObject = load.invoke(null, contractAddress, web3j, credentials, gasProvider);
	        String funcName = params[3];
	        Method[] methods = contractClass.getMethods();
	        Class[] type = null;
	        Method method = ContractClassFactory.getMethodByName(funcName, methods);
	        if(method == null) {
	        	System.out.println("Cannot find the method. Please checkout the method name.");
	        	System.out.println();
	        	return;
	        }
	        String[] generic = new String[method.getParameterCount()];
	        Type[] classType = method.getParameterTypes();
	        for (int i = 0; i < classType.length; i++) {
	            generic[i] = method.getGenericParameterTypes()[i].getTypeName();
	        }

	        Class[] classList = new Class[classType.length];
	        for (int i = 0; i < classType.length; i++) {
	            Class clazz = (Class) classType[i];
	            classList[i] = clazz;
	        }
	        Class[] parameterType =
	                ContractClassFactory.getParameterType(contractClass, funcName, params.length - 4);
	        if (parameterType == null) {
	            HelpInfo.promptNoFunc(params[1], funcName, params.length - 4);
	            return;
	        }
	        Method func = contractClass.getMethod(funcName, parameterType);
	        String[] newParams = new String[params.length - 4];
	        System.arraycopy(params, 4, newParams, 0, params.length - 4);
	        Object[] argobj = ContractClassFactory.getPrametersObject(funcName, parameterType, newParams, generic);
	        if (argobj == null) {
	            return;
	        }
	        remoteCall = (RemoteCall<?>) func.invoke(contractObject, argobj);
	        Object result = remoteCall.send();
					if(result instanceof TransactionReceipt)
					{
						TransactionReceipt receipt = (TransactionReceipt)result;
						if(!"0x0".equals(receipt.getStatus()))
						{
							System.out.println("Call failed.");
							System.out.println();
							return;
						}
					}
	        String returnObject =
	                ContractClassFactory.getReturnObject(contractClass, funcName, parameterType, result);
	        if (returnObject == null) {
	            HelpInfo.promptNoFunc(params[1], funcName, params.length - 4);
	            return;
	        }
	        System.out.println(returnObject);
	        System.out.println();
	    }

	    @SuppressWarnings("rawtypes")
	    @Override
	    public void queryCNS(String[] params) throws Exception {
	        if (params.length < 2) {
	            HelpInfo.promptHelp("queryCNS");
	            return;
	        }
	        if (params.length > 3) {
	            HelpInfo.promptHelp("queryCNS");
	            return;
	        }
	        if ("-h".equals(params[1]) || "--help".equals(params[1])) {
	            HelpInfo.queryCNSHelp();
	            return;
	        }

	        CnsService cnsService = new CnsService(web3j, credentials);
	        List<CnsInfo> cnsInfos = new ArrayList<>();
	        contractName = params[1];
	        if (contractName.endsWith(".sol")) {
	            contractName = contractName.substring(0, contractName.length() - 4);
	        }
	        if (params.length == 3) {
	            contractVersion = params[2];
	            if (contractVersion.length() > CnsService.MAX_VERSION_LENGTH) {
	              ConsoleUtils.printJson(PrecompiledCommon.transferToJson(PrecompiledCommon.VersionExceeds));
	              System.out.println();
	              return;
	            }
	            cnsInfos = cnsService.queryCnsByNameAndVersion(contractName, contractVersion);
	        } else {
	            cnsInfos = cnsService.queryCnsByName(contractName);
	        }

	        if (cnsInfos.isEmpty()) {
	            System.out.println("Empty set.");
	            System.out.println();
	            return;
	        }
	        ConsoleUtils.singleLineForTable();
	        String[] headers = {"version", "address"};
	        int size = cnsInfos.size();
	        String[][] data = new String[size][2];
	        for (int i = 0; i < size; i++) {
	            data[i][0] = cnsInfos.get(i).getVersion();
	            data[i][1] = cnsInfos.get(i).getAddress();
	        }
	        ColumnFormatter<String> cf = ColumnFormatter.text(Alignment.CENTER, 45);
	        Table table = Table.of(headers, data, cf);
	        System.out.println(table);
	        ConsoleUtils.singleLineForTable();
	        System.out.println();
	    }

	    public Object[] getDeployPrametersObject(String funcName, Class[] type, String[] params, String[] generic) throws ConsoleMessageException {
	        Object[] obj = new Object[params.length + 3];
	        obj[0] = web3j;
	        obj[1] = credentials;
	        obj[2] = gasProvider;

	        for (int i = 0; i < params.length; i++) {
	            if (type[i + 3] == String.class) {
	                if (params[i].startsWith("\"") && params[i].endsWith("\"")) {
	                    obj[i + 3] = params[i].substring(1, params[i].length() - 1);
	                }
	                else 
	                {
	                  throw new ConsoleMessageException("The " + (i + 1) + "th parameter of " + funcName + " needs string value.");
	                }
	            } else if (type[i + 3] == Boolean.class) {
	                try {
	                    obj[i + 3] = Boolean.parseBoolean(params[i]);
	                } catch (Exception e) {
	                	throw new ConsoleMessageException("The " + (i + 1) + "th parameter of " + funcName + " needs boolean value.");
	                }
	            } else if (type[i + 3] == BigInteger.class) {
	                try {
	                    obj[i + 3] = new BigInteger(params[i]);
	                } catch (Exception e) {
	                	throw new ConsoleMessageException("The " + (i + 1) + "th parameter of " + funcName + " needs integer value.");
	                }
	            } else if (type[i + 3] == byte[].class) {
	                if (params[i].startsWith("\"") && params[i].endsWith("\"")) {
	                    byte[] bytes2 = params[i + 3].substring(1, params[i + 3].length() - 1).getBytes();
	                    byte[] bytes1 = new byte[bytes2.length];
	                    for (int j = 0; j < bytes2.length; j++) {
	                        bytes1[j] = bytes2[j];
	                    }
	                    obj[i + 3] = bytes1;
	                } else {
	                	throw new ConsoleMessageException("The " + (i + 1) + "th parameter of " + funcName + " needs byte string value.");
	                }
	            } else if (type[i + 3] == List.class) {

	                if (params[i].startsWith("[") && params[i].endsWith("]")) {
	                    String listParams = params[i].substring(1, params[i].length() - 1);
	                    String[] ilist = listParams.split(",");
	                    String[] jlist = new String[ilist.length];
	                    for(int k = 0; k < jlist.length; k++)
	                    {
	                    	jlist[k] = ilist[k].trim();
	                    }
	                    List paramsList = new ArrayList();
	                    if (generic[i].contains("String")) {
	                        paramsList = new ArrayList<String>();
	                        for (int j = 0; j < jlist.length; j++) {
	                            paramsList.add(jlist[j].substring(1, jlist[j].length() - 1));
	                        }

	                    } else if (generic[i].contains("BigInteger")) {
	                        paramsList = new ArrayList<BigInteger>();
	                        for (int j = 0; j < jlist.length; j++) {
	                            paramsList.add(new BigInteger(jlist[j]));
	                        }

	                    }
	                    else if(generic[i].contains("byte[]")) {
	                        paramsList = new ArrayList<byte[]>();
	                        for (int j = 0; j < jlist.length; j++) {
	                            if (jlist[j].startsWith("\"") && jlist[j].endsWith("\"")) {
	                                byte[] bytes = jlist[j].substring(1, jlist[j].length() - 1).getBytes();
	                                byte[] bytes1 = new byte[32];
	                                byte[] bytes2 = bytes;
	                                for (int k = 0; k < bytes2.length; k++) {
	                                    bytes1[k] = bytes2[k];
	                                }
	                                paramsList.add(bytes1);
	                            }
	                        }
	                    }
	                    obj[i + 3] = paramsList;
	                }
	                else 
	                {
	                	throw new ConsoleMessageException("The " + (i + 1) + "th parameter of " + funcName + " needs array value.");
	                }
	            }
	        }
	        return obj;
	    }
}
