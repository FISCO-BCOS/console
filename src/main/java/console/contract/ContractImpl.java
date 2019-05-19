package console.contract;

import static console.common.ContractClassFactory.getContractClass;

import console.common.Address;
import console.common.Common;
import console.common.ConsoleUtils;
import console.common.ContractClassFactory;
import console.common.HelpInfo;
import console.exception.ConsoleMessageException;
import io.bretty.console.table.Alignment;
import io.bretty.console.table.ColumnFormatter;
import io.bretty.console.table.Table;
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
import java.util.NoSuchElementException;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.precompile.cns.CnsInfo;
import org.fisco.bcos.web3j.precompile.cns.CnsService;
import org.fisco.bcos.web3j.precompile.common.PrecompiledCommon;
import org.fisco.bcos.web3j.precompile.permission.PermissionInfo;
import org.fisco.bcos.web3j.precompile.permission.PermissionService;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.channel.StatusCode;
import org.fisco.bcos.web3j.protocol.core.RemoteCall;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.tx.Contract;
import org.fisco.bcos.web3j.tx.exceptions.ContractCallException;
import org.fisco.bcos.web3j.tx.gas.ContractGasProvider;
import org.fisco.bcos.web3j.tx.gas.StaticGasProvider;

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
            compileContract(name);
        } catch (Exception e) {
            System.out.println(e.getMessage());
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
            System.out.println("contract address:" + contractAddress);
            System.out.println();
            contractAddress = contract.getContractAddress();
            writeLog();
        } catch (Exception e) {
            if (e.getMessage().contains("0x19")) {
                ConsoleUtils.printJson(PrecompiledCommon.transferToJson(Common.PermissionCode));
            } else {
                System.out.println(e.getMessage());
                System.out.println();
            }
        }
    }

    private synchronized void writeLog() {

        BufferedReader reader = null;
        try {
            File logFile = new File(Common.ContractLogFileName);
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            reader = new BufferedReader(new FileReader(Common.ContractLogFileName));
            String line;
            List<String> textList = new ArrayList<String>();
            while ((line = reader.readLine()) != null) {
                textList.add(line);
            }
            int i = 0;
            if (textList.size() >= Common.LogMaxCount) {
                i = textList.size() - Common.LogMaxCount + 1;
                if (logFile.exists()) {
                    logFile.delete();
                    logFile.createNewFile();
                }
                PrintWriter pw = new PrintWriter(new FileWriter(Common.ContractLogFileName, true));
                for (; i < textList.size(); i++) {
                    pw.println(textList.get(i));
                }
                pw.flush();
                pw.close();
            }
        } catch (IOException e) {
            System.out.println("Read deploylog.txt failed.");
            return;
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                System.out.println("Close deploylog.txt failed.");
                ;
            }
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        String name = contractName.substring(20);
        while (name.length() < 20) {
            name = name + " ";
        }
        String log =
                LocalDateTime.now().format(formatter)
                        + "  [group:"
                        + groupID
                        + "]  "
                        + name
                        + "  "
                        + contractAddress;
        try {
            File logFile = new File(Common.ContractLogFileName);
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            PrintWriter pw = new PrintWriter(new FileWriter(Common.ContractLogFileName, true));
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
                    System.out.println(
                            "Please provide record number by integer mode, "
                                    + Common.DeployLogntegerRange
                                    + ".");
                    System.out.println();
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println(
                        "Please provide record number by integer mode, "
                                + Common.DeployLogntegerRange
                                + ".");
                System.out.println();
                return;
            }
        }
        File logFile = new File(Common.ContractLogFileName);
        if (!logFile.exists()) {
            logFile.createNewFile();
        }
        BufferedReader reader = new BufferedReader(new FileReader(Common.ContractLogFileName));
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
            if (recordNumber >= len) {
                recordNumber = len;
            } else {
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
        try {
            compileContract(name);
        } catch (Exception e) {
            System.out.println(e.getMessage());
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
        if (method == null) {
            System.out.println(
                    "Cannot find the method " + funcName + ", please checkout the method name.");
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
        Object[] argobj =
                ContractClassFactory.getPrametersObject(
                        funcName, parameterType, newParams, generic);
        if (argobj == null) {
            return;
        }
        remoteCall = (RemoteCall<?>) func.invoke(contractObject, argobj);
        Object result;
        result = remoteCall.send();
        if (result instanceof TransactionReceipt) {
            TransactionReceipt receipt = (TransactionReceipt) result;
            if (StatusCode.RevertInstruction.equals(receipt.getStatus())) {
                throw new ContractCallException("The execution of the contract rolled back.");
            }
            if (StatusCode.CallAddressError.equals(receipt.getStatus())) {
                System.out.println("The contract address is incorrect.");
                System.out.println();
                return;
            }
            if (!StatusCode.Success.equals(receipt.getStatus())) {
                System.out.println("Receipt status:" + receipt.getStatus());
                System.out.println();
                return;
            }
            String output = receipt.getOutput();
            if (!"0x".equals(output)) {
                int code = new BigInteger(output.substring(2, output.length()), 16).intValue();
                if (code == PrecompiledCommon.TableExist) {
                    System.out.println("The table already exist.");
                    System.out.println();
                    return;
                }
                if (code == Common.PermissionCode) {
                    System.out.println("Permission denied.");
                    System.out.println();
                    return;
                }
            }
        }
        String returnObject =
                ContractClassFactory.getReturnObject(
                        contractClass, funcName, parameterType, result);
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
            ConsoleUtils.printJson(PrecompiledCommon.transferToJson(Common.PermissionCode));
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
            ConsoleUtils.printJson(
                    PrecompiledCommon.transferToJson(
                            PrecompiledCommon.ContractNameAndVersionExist));
            System.out.println();
            return;
        }
        try {
            compileContract(name);
        } catch (Exception e) {
            System.out.println(e.getMessage());
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
        if (!checkVersion(contractVersion)) {
            return;
        }
        try {
            Contract contract = (Contract) remoteCall.send();
            contractAddress = contract.getContractAddress();
            // register cns
            String result = cnsService.registerCns(name, contractVersion, contractAddress, "");
            System.out.println("contract address:" + contractAddress);
            contractName = contractName + ":" + contractVersion;
            writeLog();
            System.out.println();
        } catch (Exception e) {
            if (e.getMessage().contains("0x19")) {
                ConsoleUtils.printJson(PrecompiledCommon.transferToJson(Common.PermissionCode));
            } else {
                System.out.println(e.getMessage());
                System.out.println();
            }
        }
    }

    private boolean checkVersion(String version) throws IOException {
        if (version.length() > CnsService.MAX_VERSION_LENGTH) {
            ConsoleUtils.printJson(
                    PrecompiledCommon.transferToJson(PrecompiledCommon.VersionExceeds));
            System.out.println();
            return false;
        }
        if (!version.matches("^[A-Za-z0-9.]+$")) {
            System.out.println(
                    "Contract version should only contains 'A-Z' or 'a-z' or '0-9' or dot mark.");
            System.out.println();
            return false;
        }
        return true;
    }

    private void handleDeployParameters(String[] params, int num)
            throws IllegalAccessException, InvocationTargetException, ConsoleMessageException {
        Method method = ContractClassFactory.getDeployFunction(contractClass);
        Type[] classType = method.getParameterTypes();
        if (classType.length - 3 != params.length - num) {
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
        if (params.length < 3) {
            HelpInfo.promptHelp("callByCNS");
            return;
        }
        String contractNameAndVersion = params[1];
        String name = params[1];
        String contractVersion = null;
        if (contractNameAndVersion.contains(":")) {
            String[] nameAndVersion = contractNameAndVersion.split(":");
            if (nameAndVersion.length == 2) {
                name = nameAndVersion[0].trim();
                contractVersion = nameAndVersion[1].trim();
            } else {
                System.out.println(
                        "Contract name and version has incorrect format. For example, contractName:contractVersion");
                System.out.println();
                return;
            }
        }
        if (name.endsWith(".sol")) {
            name = name.substring(0, name.length() - 4);
            if (contractVersion != null) {
                if (!checkVersion(contractVersion)) {
                    return;
                }
                contractNameAndVersion = name + ":" + contractVersion;
            } else {
                contractNameAndVersion = name;
            }
        }
        // get address from cns
        String contractAddress = "";
        CnsService cnsResolver = new CnsService(web3j, credentials);
        try {
            contractAddress =
                    cnsResolver.getAddressByContractNameAndVersion(contractNameAndVersion);
        } catch (Exception e) {
            System.out.println("The contract version does not exist.");
            System.out.println();
            return;
        }
        try {
            compileContract(name);
        } catch (Exception e) {
            System.out.println(e.getMessage());
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
        Object contractObject = load.invoke(null, contractAddress, web3j, credentials, gasProvider);
        String funcName = params[2];
        Method[] methods = contractClass.getMethods();
        Method method = ContractClassFactory.getMethodByName(funcName, methods);
        if (method == null) {
            System.out.println(
                    "Cannot find the method " + funcName + ", please checkout the method name.");
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
                ContractClassFactory.getParameterType(contractClass, funcName, params.length - 3);
        if (parameterType == null) {
            HelpInfo.promptNoFunc(params[1], funcName, params.length - 3);
            return;
        }
        Method func = contractClass.getMethod(funcName, parameterType);
        String[] newParams = new String[params.length - 3];
        System.arraycopy(params, 3, newParams, 0, params.length - 3);
        Object[] argobj =
                ContractClassFactory.getPrametersObject(
                        funcName, parameterType, newParams, generic);
        if (argobj == null) {
            return;
        }
        remoteCall = (RemoteCall<?>) func.invoke(contractObject, argobj);
        Object result = remoteCall.send();
        if (result instanceof TransactionReceipt) {
            TransactionReceipt receipt = (TransactionReceipt) result;
            if (!"0x0".equals(receipt.getStatus())) {
                System.out.println(receipt.getStatus());
                System.out.println();
                return;
            }
        }
        String returnObject =
                ContractClassFactory.getReturnObject(
                        contractClass, funcName, parameterType, result);
        if (returnObject == null) {
            HelpInfo.promptNoFunc(params[1], funcName, params.length - 3);
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
            if (!checkVersion(contractVersion)) {
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

    public Object[] getDeployPrametersObject(
            String funcName, Class[] type, String[] params, String[] generic)
            throws ConsoleMessageException {
        Object[] obj = new Object[params.length + 3];
        obj[0] = web3j;
        obj[1] = credentials;
        obj[2] = gasProvider;

        for (int i = 0; i < params.length; i++) {
            if (type[i + 3] == String.class) {
                if (params[i].startsWith("\"") && params[i].endsWith("\"")) {
                    obj[i + 3] = params[i].substring(1, params[i].length() - 1);
                } else {
                    throw new ConsoleMessageException(
                            "The "
                                    + (i + 1)
                                    + "th parameter of "
                                    + funcName
                                    + " needs string value.");
                }
            } else if (type[i + 3] == Boolean.class) {
                try {
                    obj[i + 3] = Boolean.parseBoolean(params[i]);
                } catch (Exception e) {
                    throw new ConsoleMessageException(
                            "The "
                                    + (i + 1)
                                    + "th parameter of "
                                    + funcName
                                    + " needs boolean value.");
                }
            } else if (type[i + 3] == BigInteger.class) {
                try {
                    BigInteger param = new BigInteger(params[i]);
                    if (param.compareTo(new BigInteger(Integer.MAX_VALUE + "")) == 1
                            || param.compareTo(new BigInteger(Integer.MIN_VALUE + "")) == -1) {
                        throw new ConsoleMessageException(
                                "The "
                                        + (i + 1)
                                        + "th parameter of "
                                        + funcName
                                        + " needs integer("
                                        + Integer.MIN_VALUE
                                        + " ~ "
                                        + Integer.MAX_VALUE
                                        + ") value in the console.");
                    } else {
                        obj[i + 3] = param;
                    }
                } catch (Exception e) {
                    throw new ConsoleMessageException(
                            "The "
                                    + (i + 1)
                                    + "th parameter of "
                                    + funcName
                                    + " needs integer("
                                    + Integer.MIN_VALUE
                                    + " ~ "
                                    + Integer.MAX_VALUE
                                    + ") value in the console.");
                }
            } else if (type[i + 3] == byte[].class) {
                if (params[i].startsWith("\"") && params[i].endsWith("\"")) {
                    byte[] bytes2 = params[i].substring(1, params[i].length() - 1).getBytes();
                    byte[] bytes1 = new byte[32];
                    for (int j = 0; j < bytes2.length; j++) {
                        bytes1[j] = bytes2[j];
                    }
                    obj[i + 3] = bytes1;
                } else {
                    throw new ConsoleMessageException(
                            "The "
                                    + (i + 1)
                                    + "th parameter of "
                                    + funcName
                                    + " needs byte string value.");
                }
            } else if (type[i + 3] == List.class) {

                if (params[i].startsWith("[") && params[i].endsWith("]")) {
                    String listParams = params[i].substring(1, params[i].length() - 1);
                    String[] ilist = listParams.split(",");
                    String[] jlist = new String[ilist.length];
                    for (int k = 0; k < jlist.length; k++) {
                        jlist[k] = ilist[k].trim();
                    }
                    List paramsList = new ArrayList();
                    if (generic[i + 3].contains("String")) {
                        paramsList = new ArrayList<String>();
                        for (int j = 0; j < jlist.length; j++) {
                            paramsList.add(jlist[j].substring(1, jlist[j].length() - 1));
                        }

                    } else if (generic[i + 3].contains("BigInteger")) {
                        paramsList = new ArrayList<BigInteger>();
                        for (int j = 0; j < jlist.length; j++) {
                            paramsList.add(new BigInteger(jlist[j]));
                        }

                    } else if (generic[i + 3].contains("byte[]")) {
                        paramsList = new ArrayList<byte[]>();
                        for (int j = 0; j < jlist.length; j++) {
                            if (jlist[j].startsWith("\"") && jlist[j].endsWith("\"")) {
                                byte[] bytes =
                                        jlist[j].substring(1, jlist[j].length() - 1).getBytes();
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
                } else {
                    throw new ConsoleMessageException(
                            "The "
                                    + (i + 1)
                                    + "th parameter of "
                                    + funcName
                                    + " needs array value.");
                }
            }
        }
        return obj;
    }

    private void compileContract(String name) throws Exception {
        try {
            ConsoleUtils.dynamicCompileSolFilesToJava(name);
        } catch (IOException e) {
            throw new IOException(e.getMessage());
        }
        try {
            ConsoleUtils.dynamicCompileJavaToClass(name);
        } catch (Exception e1) {
            throw new Exception("Compile " + name + ".java failed.");
        }
        contractName = ConsoleUtils.PACKAGENAME + "." + name;
        try {
            contractClass = getContractClass(contractName);
        } catch (Exception e) {
            throw new Exception(
                    "There is no "
                            + name
                            + ".class"
                            + " in the directory of java/classes/org/fisco/bcos/temp");
        }
    }

    @Override
    public void getTxReceiptEvents(String[] params) throws Exception {
        if (params.length < 2) {
            HelpInfo.promptHelp("getTxReceiptEvents");
            return;
        }
        if (params.length > 6) {
            HelpInfo.promptHelp("getTxReceiptEvents");
            return;
        }
        String contractName = params[1];
        if ("-h".equals(contractName) || "--help".equals(contractName)) {
            HelpInfo.getTxReceiptEventsHelp();
            return;
        }
        if (params.length < 5) {
            HelpInfo.promptHelp("getTransactionByBlockNumberAndIndex");
            return;
        }
        String contractAddress = params[2];
        Address convertAddr = ConsoleUtils.convertAddress(contractAddress);
        if (!convertAddr.isValid()) {
            return;
        }
        String transactionHash = params[3];
        if (ConsoleUtils.isInvalidHash(transactionHash)) return;
        String eventName = params[4];
        int index = -1;
        if (params.length == 6) {
            index = ConsoleUtils.proccessNonNegativeNumber("index", params[5]);
            if (index == Common.InvalidReturnNumber) {
                return;
            }
        }
        // query txReceipt
        TransactionReceipt transactionReceipt;
        try {
            transactionReceipt =
                    web3j.getTransactionReceipt(transactionHash)
                            .send()
                            .getTransactionReceipt()
                            .get();
        } catch (NoSuchElementException e) {
            System.out.println("This transaction hash doesn't exist.");
            System.out.println();
            return;
        }
        // parse txReceipt
        try {
            compileContract(contractName);
        } catch (Exception e) {
            System.out.println(e.getMessage());
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
        contractAddress = convertAddr.getAddress();
        Object contractObject = load.invoke(null, contractAddress, web3j, credentials, gasProvider);
        Method[] methods = contractClass.getDeclaredMethods();
        String funcName =
                "get" + eventName.substring(0, 1).toUpperCase() + eventName.substring(1) + "Events";
        Method method = ContractClassFactory.getMethodByName(funcName, methods);
        if (method == null) {
            System.out.println(
                    "Cannot find the event " + funcName + ", please checkout the event name.");
            System.out.println();
            return;
        }
        List<Object> result = (ArrayList<Object>) method.invoke(contractObject, transactionReceipt);
        if (result.size() == 0) {
            System.out.println("The event is empty.");
            System.out.println();
            return;
        }
        if (index == -1) {
            for (int i = 0; i < result.size(); i++) {
                ConsoleUtils.printEventLog(i, result);
            }
        } else {
            if (index > result.size()) {
                System.out.println("The event index is greater event size.");
                System.out.println();
                return;
            }
            ConsoleUtils.printEventLog(index, result);
        }
        System.out.println();
    }
}
