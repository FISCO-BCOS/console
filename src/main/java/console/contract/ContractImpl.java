package console.contract;

import static org.fisco.solc.compiler.SolidityCompiler.Options.ABI;

import console.account.AccountManager;
import console.common.AbiAndBin;
import console.common.Address;
import console.common.Common;
import console.common.ConsoleUtils;
import console.common.ContractClassFactory;
import console.common.DeployContractManager;
import console.common.HelpInfo;
import console.common.PathUtils;
import console.common.PrecompiledUtility;
import console.common.StatusCodeLink;
import console.common.TxDecodeUtil;
import console.exception.CompileSolidityException;
import console.exception.ConsoleMessageException;
import io.bretty.console.table.Alignment;
import io.bretty.console.table.ColumnFormatter;
import io.bretty.console.table.Table;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.fisco.bcos.web3j.abi.EventEncoder;
import org.fisco.bcos.web3j.abi.wrapper.ABIDefinition;
import org.fisco.bcos.web3j.abi.wrapper.ABIDefinitionFactory;
import org.fisco.bcos.web3j.abi.wrapper.ContractABIDefinition;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.EncryptType;
import org.fisco.bcos.web3j.precompile.cns.CnsInfo;
import org.fisco.bcos.web3j.precompile.cns.CnsService;
import org.fisco.bcos.web3j.precompile.common.PrecompiledCommon;
import org.fisco.bcos.web3j.precompile.exception.PrecompileMessageException;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.channel.StatusCode;
import org.fisco.bcos.web3j.protocol.core.RemoteCall;
import org.fisco.bcos.web3j.protocol.core.methods.response.Code;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.protocol.exceptions.TransactionException;
import org.fisco.bcos.web3j.tx.Contract;
import org.fisco.bcos.web3j.tx.gas.ContractGasProvider;
import org.fisco.bcos.web3j.tx.gas.StaticGasProvider;
import org.fisco.bcos.web3j.tx.txdecode.BaseException;
import org.fisco.bcos.web3j.utils.Numeric;
import org.fisco.solc.compiler.CompilationResult;
import org.fisco.solc.compiler.SolidityCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContractImpl implements ContractFace {

    private static final Logger logger = LoggerFactory.getLogger(ContractImpl.class);

    private int groupID;
    private AccountManager accountManager;
    private DeployContractManager deployContractManager;
    private StaticGasProvider gasProvider;
    private Web3j web3j;

    public Web3j getWeb3j() {
        return this.web3j;
    }

    @Override
    public void setGroupID(int groupID) {
        this.groupID = groupID;
    }

    @Override
    public void setWeb3j(Web3j web3j) {
        this.web3j = web3j;
    }

    @Override
    public void setAccountManager(AccountManager accountManager) {
        this.accountManager = accountManager;
    }

    @Override
    public void setGasProvider(StaticGasProvider gasProvider) {
        this.gasProvider = gasProvider;
    }

    @Override
    public void setDeployContractManager(DeployContractManager deployContractManager) {
        this.deployContractManager = deployContractManager;
    }

    @Override
    public AccountManager getAccountManager() {
        return accountManager;
    }

    @Override
    public DeployContractManager getDeployContractManager() {
        return this.deployContractManager;
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

        String contractNameOrPath = params[1];
        File solFile = PathUtils.getSolFile(contractNameOrPath);
        String name = solFile.getName().split("\\.")[0];

        try {
            Class<?> contractClass = ContractClassFactory.compileContract(solFile);
            RemoteCall<?> remoteCall =
                    ContractClassFactory.handleDeployParameters(
                            web3j,
                            accountManager.getCurrentAccountCredentials(),
                            gasProvider,
                            contractClass,
                            params,
                            2);
            Contract contract = (Contract) remoteCall.send();
            // TransactionReceipt transactionReceipt = contract.getTransactionReceipt().get();
            String contractAddress = contract.getContractAddress();
            System.out.println("contract address: " + contractAddress);
            System.out.println();
            contractAddress = contract.getContractAddress();
            deployContractManager.addNewDeployContract(
                    String.valueOf(groupID), name, contractAddress);
        } catch (Exception e) {
            logger.error("e: ", e);
            if ((e.getMessage() != null) && e.getMessage().contains("0x19")) {
                ConsoleUtils.printJson(PrecompiledCommon.transferToJson(Common.PermissionCode));
                System.out.println();
            } else {
                throw e;
            }
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
                                    + Common.DeployLongIntegerRange
                                    + ".");
                    System.out.println();
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println(
                        "Please provide record number by integer mode, "
                                + Common.DeployLongIntegerRange
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
                if ((contractInfos.length > 2)
                        && ("[group:" + groupID + "]").equals(contractInfos[2])) {
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
        } catch (Exception e) {
            logger.error(" load {} failed, e: {}", Common.ContractLogFileName, e);
        } finally {
            reader.close();
        }
    }

    @Override
    public void listDeployContractAddress(String[] params) throws Exception {
        // listDeployContractAddress [contractName] [offset] [count]
        if (params.length < 2) {
            HelpInfo.promptHelp("listDeployContractAddress");
            return;
        }

        if ("-h".equals(params[1]) || "--help".equals(params[1])) {
            HelpInfo.listDeployContractAddressHelp();
            return;
        }

        String contractName = params[1];
        int offset = 0;
        int count = 20;

        if (params.length > 2) {
            offset = ConsoleUtils.processNonNegativeNumber("offset", params[2]);
            if (offset == Common.InvalidReturnNumber) {
                return;
            }
            if (params.length > 3) {
                count = ConsoleUtils.processPositiveIntegerNumber("count", params[3]);
                if (count == Common.InvalidReturnNumber) {
                    return;
                }
            }
        }

        logger.debug("contractName: {}, offset: {}, count: {}", contractName, offset, count);

        List<DeployContractManager.DeployedContract> deployContractList =
                deployContractManager.getDeployContractList(String.valueOf(groupID), contractName);
        System.out.println(
                "contract: "
                        + contractName
                        + " has been deployed "
                        + deployContractList.size()
                        + " times.");

        if (offset <= deployContractList.size()) {
            if (offset + count >= deployContractList.size()) {
                count = deployContractList.size() - offset;
            }

            for (int i = offset; i < offset + count; i++) {
                System.out.printf(
                        "\t%3d. %s  %s\n",
                        i,
                        deployContractList.get(i).getContractAddress(),
                        deployContractList.get(i).getTimestamp());
            }
        }
        System.out.println();
    }

    @Override
    public void listAbi(String[] params) throws Exception {
        if (params.length < 2) {
            HelpInfo.promptHelp("listAbi");
            return;
        }
        if (params.length > 3) {
            HelpInfo.promptHelp("listAbi");
            return;
        }

        if ("-h".equals(params[1]) || "--help".equals(params[1])) {
            HelpInfo.listAbiHelp();
            return;
        }

        String contractFileName = params[1];
        File solFile = PathUtils.getSolFile(contractFileName);

        String contractName = null;
        if (params.length > 2) {
            contractName = params[2];
        } else {
            contractName = solFile.getName().split("\\.")[0];
        }

        SolidityCompiler.Result res =
                SolidityCompiler.compile(
                        solFile, EncryptType.encryptType == EncryptType.SM2_TYPE, true, ABI);

        if (logger.isDebugEnabled()) {
            logger.debug(
                    " solidity compiler, contract: {}, result: {}, output: {}, error: {}",
                    solFile,
                    !res.isFailed(),
                    res.getOutput(),
                    res.getErrors());
        }

        if (res.isFailed()) {
            throw new CompileSolidityException(
                    " Compile " + solFile.getName() + " error: " + res.getErrors());
        }

        CompilationResult result = CompilationResult.parse(res.getOutput());
        CompilationResult.ContractMetadata contractMetadata = result.getContract(contractName);

        // Read Content of the file
        ContractABIDefinition contractABIDefinition =
                ABIDefinitionFactory.loadABI(contractMetadata.abi);
        if (Objects.isNull(contractABIDefinition)) {
            System.out.println(" Unable to load " + contractName + " abi");
            logger.warn(" contract: {}, abi: {}", contractName, contractMetadata.abi);
            return;
        }

        Map<String, ABIDefinition> methodIDToFunctions =
                contractABIDefinition.getMethodIDToFunctions();

        if (!methodIDToFunctions.isEmpty()) {
            System.out.println("Method list: ");
            System.out.printf(
                    " %-20s|    %-10s|    %-10s  |    %-10s\n",
                    "name", "constant", "methodId", "signature");
            System.out.println("  -------------------------------------------------------------- ");
            for (Map.Entry<String, ABIDefinition> entry : methodIDToFunctions.entrySet()) {
                System.out.printf(
                        " %-20s|    %-10s|    %-10s  |    %-10s\n",
                        entry.getValue().getName(),
                        entry.getValue().isConstant(),
                        entry.getValue().getMethodId(),
                        entry.getValue().getMethodSignatureAsString());
            }
        } else {
            System.out.println(contractName + " contains no method.");
        }

        Map<String, List<ABIDefinition>> events = contractABIDefinition.getEvents();
        if (!events.isEmpty()) {
            System.out.println();
            System.out.println("Event list: ");
            // System.out.println("  --------------------------------------------------------------
            // ");
            System.out.printf(" %-20s|   %-66s     %10s\n", "name", "topic", "signature");
            System.out.println("  -------------------------------------------------------------- ");
            for (Map.Entry<String, ABIDefinition> entry : methodIDToFunctions.entrySet()) {

                System.out.printf(
                        " %-20s|   %-66s  |   %10s\n",
                        entry.getValue().getName(),
                        EventEncoder.buildEventSignature(
                                entry.getValue().getMethodSignatureAsString()),
                        entry.getValue().getMethodSignatureAsString());
            }
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

        String contractPath = params[1];
        File solFile = PathUtils.getSolFile(contractPath);
        String name = solFile.getName().split("\\.")[0];

        String contractAddress = params[2];
        if (contractAddress.toLowerCase().equals("latest")) { // latest
            DeployContractManager.DeployedContract latestDeployContract =
                    deployContractManager.getLatestDeployContract(String.valueOf(groupID), name);
            if (latestDeployContract == null) {
                System.out.println("contract " + name + " has not been deployed.");
                System.out.println();
                return;
            }
            contractAddress = latestDeployContract.getContractAddress();
            logger.debug(
                    " last deployed contract name: {}, contract: {}", name, latestDeployContract);
        } else if (ConsoleUtils.isNumeric(contractAddress)) {
            int index = Integer.valueOf(contractAddress);
            DeployContractManager.DeployedContract deployContractByIndex =
                    deployContractManager.getDeployContractByIndex(
                            String.valueOf(groupID), name, index);
            if (deployContractByIndex == null) {
                System.out.println(
                        "contract: "
                                + name
                                + " ,index: "
                                + index
                                + " not exist, please check if index is out of range.");
                System.out.println();
                return;
            }
            contractAddress = deployContractByIndex.getContractAddress();
            logger.debug(
                    " index deployed contract name: {}, index: {}, contract: {}",
                    name,
                    index,
                    deployContractByIndex);
        } else {
            Address convertAddr = ConsoleUtils.convertAddress(contractAddress);
            if (!convertAddr.isValid()) {
                return;
            }
            contractAddress = convertAddr.getAddress();
        }

        Class<?> contractClass = ContractClassFactory.compileContract(solFile);
        Method load =
                contractClass.getMethod(
                        "load",
                        String.class,
                        Web3j.class,
                        Credentials.class,
                        ContractGasProvider.class);

        Object contractObject =
                load.invoke(
                        null,
                        contractAddress,
                        web3j,
                        accountManager.getCurrentAccountCredentials(),
                        gasProvider);
        String funcName = params[3];
        Method[] methods = contractClass.getDeclaredMethods();
        String[] newParams = new String[params.length - 4];
        System.arraycopy(params, 4, newParams, 0, params.length - 4);
        Method method = ContractClassFactory.getMethodByName(methods, funcName, newParams);
        if (method == null) {
            throw new ConsoleMessageException(
                    "The method "
                            + funcName
                            + " with "
                            + newParams.length
                            + " parameter"
                            + " is undefined in the contract.");
        }
        String[] generic = new String[method.getParameterCount()];
        Type[] classType = method.getParameterTypes();
        for (int i = 0; i < classType.length; i++) {
            generic[i] = method.getGenericParameterTypes()[i].getTypeName();
        }
        Class[] parameterType = new Class[classType.length];
        for (int i = 0; i < classType.length; i++) {
            Class clazz = (Class) classType[i];
            parameterType[i] = clazz;
        }
        Method func = contractClass.getMethod(funcName, parameterType);
        Object[] argobj =
                ContractClassFactory.getParametersObject(
                        funcName, parameterType, newParams, generic);
        if (argobj == null) {
            return;
        }

        RemoteCall<?> remoteCall = (RemoteCall<?>) func.invoke(contractObject, argobj);

        Object result = remoteCall.send();
        if (result instanceof TransactionReceipt) {
            AbiAndBin abiAndBin = TxDecodeUtil.readAbiAndBin(name);
            handleTransactionReceipt(abiAndBin.getAbi(), (TransactionReceipt) result);
        } else {
            String returnObject =
                    ContractClassFactory.getReturnObject(
                            contractClass, funcName, parameterType, result);
            if (returnObject == null) {
                HelpInfo.promptNoFunc(params[1], funcName, params.length - 4);
                return;
            }
            System.out.println(returnObject);
        }
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

        /*
        The node performs permission detection, remove it from console
         */
        /*
        PermissionService permissionTableService =
                new PermissionService(web3j, accountManager.getCurrentAccountCredentials());
        List<PermissionInfo> permissions = permissionTableService.listCNSManager();
        boolean flag = false;
        if (permissions.size() == 0) {
            flag = true;
        } else {
            for (PermissionInfo permission : permissions) {
                if ((accountManager.getCurrentAccountCredentials().getAddress())
                        .equals(permission.getAddress())) {
                    flag = true;
                    break;
                }
            }
        }
        if (!flag) {
            ConsoleUtils.printJson(PrecompiledCommon.transferToJson(Common.PermissionCode));
            System.out.println();
            return;
        }*/

        String contractPath = params[1];
        File solFile = PathUtils.getSolFile(contractPath);
        String name = solFile.getName().split("\\.")[0];

        CnsService cnsService =
                new CnsService(web3j, accountManager.getCurrentAccountCredentials());
        List<CnsInfo> qcns = cnsService.queryCnsByNameAndVersion(name, params[2]);
        if (qcns.size() != 0) {
            ConsoleUtils.printJson(
                    PrecompiledCommon.transferToJson(
                            PrecompiledCommon.ContractNameAndVersionExist));
            System.out.println();
            return;
        }
        try {
            Class<?> contractClass = ContractClassFactory.compileContract(solFile);
            RemoteCall<?> remoteCall =
                    ContractClassFactory.handleDeployParameters(
                            web3j,
                            accountManager.getCurrentAccountCredentials(),
                            gasProvider,
                            contractClass,
                            params,
                            3);
            String contractVersion = params[2];
            if (!ContractClassFactory.checkVersion(contractVersion)) {
                return;
            }
            Contract contract = (Contract) remoteCall.send();
            String contractAddress = contract.getContractAddress();
            // register cns
            cnsService.registerCns(
                    name,
                    contractVersion,
                    contractAddress,
                    TxDecodeUtil.readAbiAndBin(name).getAbi());
            System.out.println("contract address: " + contractAddress);
            String contractName = name + ":" + contractVersion;
            deployContractManager.addNewDeployContract(
                    String.valueOf(groupID), name, contractAddress);
            System.out.println();
        } catch (Exception e) {
            if (e.getMessage().contains("0x19")) {
                ConsoleUtils.printJson(PrecompiledCommon.transferToJson(Common.PermissionCode));
                System.out.println();
            } else {
                throw e;
            }
        }
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

        String contractName = contractNameAndVersion;
        String contractVersion = null;

        if (contractNameAndVersion.contains(":")) {
            String[] nameAndVersion = contractNameAndVersion.split(":");
            if (nameAndVersion.length == 2) {
                contractName = nameAndVersion[0].trim();
                contractVersion = nameAndVersion[1].trim();
                if (!ContractClassFactory.checkVersion(contractVersion)) {
                    return;
                }
            } else {
                System.out.println(
                        "Contract name and version has incorrect format. For example, contractName:contractVersion");
                System.out.println();
                return;
            }
        }

        CnsService cnsResolver =
                new CnsService(web3j, accountManager.getCurrentAccountCredentials());

        List<CnsInfo> cnsInfos = null;
        if (contractVersion != null && !contractVersion.isEmpty()) {
            cnsInfos = cnsResolver.queryCnsByNameAndVersion(contractName, contractVersion);
        } else {
            cnsInfos = cnsResolver.queryCnsByName(contractName);
        }

        if (cnsInfos == null || cnsInfos.isEmpty()) {
            throw new PrecompileMessageException("The contract version does not exist.");
        }

        String contractAddress = cnsInfos.get(cnsInfos.size() - 1).getAddress();
        String abi = cnsInfos.get(cnsInfos.size() - 1).getAbi();
        String version = cnsInfos.get(cnsInfos.size() - 1).getVersion();

        logger.debug("contractAddress: {}, version: {}, abi: {}", contractAddress, version, abi);

        Class<?> contractClass = ContractClassFactory.compileContract(contractName, abi);
        Method load =
                contractClass.getMethod(
                        "load",
                        String.class,
                        Web3j.class,
                        Credentials.class,
                        ContractGasProvider.class);
        Object contractObject =
                load.invoke(
                        null,
                        contractAddress,
                        web3j,
                        accountManager.getCurrentAccountCredentials(),
                        gasProvider);
        String funcName = params[2];
        Method[] methods = contractClass.getMethods();
        String[] newParams = new String[params.length - 3];
        System.arraycopy(params, 3, newParams, 0, params.length - 3);
        Method method = ContractClassFactory.getMethodByName(methods, funcName, newParams);
        if (method == null) {
            throw new ConsoleMessageException(
                    "The method "
                            + funcName
                            + " with "
                            + newParams.length
                            + " parameter"
                            + " is undefined in the contract.");
        }
        String[] generic = new String[method.getParameterCount()];
        Type[] classType = method.getParameterTypes();
        for (int i = 0; i < classType.length; i++) {
            generic[i] = method.getGenericParameterTypes()[i].getTypeName();
        }

        Class[] parameterType = new Class[classType.length];
        for (int i = 0; i < classType.length; i++) {
            Class clazz = (Class) classType[i];
            parameterType[i] = clazz;
        }
        Method func = contractClass.getMethod(funcName, parameterType);
        Object[] argobj =
                ContractClassFactory.getParametersObject(
                        funcName, parameterType, newParams, generic);
        if (argobj == null) {
            return;
        }
        RemoteCall<?> remoteCall = (RemoteCall<?>) func.invoke(contractObject, argobj);
        Object result = remoteCall.send();
        // logger.info(" ====>>> " + result.getClass().getName());
        if (result instanceof TransactionReceipt) {
            handleTransactionReceipt(abi, (TransactionReceipt) result);
        } else {
            String returnObject =
                    ContractClassFactory.getReturnObject(
                            contractClass, funcName, parameterType, result);
            if (returnObject == null) {
                HelpInfo.promptNoFunc(params[1], funcName, params.length - 3);
                return;
            }
            System.out.println(returnObject);
        }
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

        CnsService cnsService =
                new CnsService(web3j, accountManager.getCurrentAccountCredentials());
        List<CnsInfo> cnsInfos = new ArrayList<>();
        String contractName = params[1];
        if (contractName.endsWith(".sol")) {
            contractName = contractName.substring(0, contractName.length() - 4);
        }
        if (params.length == 3) {
            String contractVersion = params[2];
            if (!ContractClassFactory.checkVersion(contractVersion)) {
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
        ConsoleUtils.singleLine();
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
        ConsoleUtils.singleLine();
        System.out.println();
    }

    @Override
    public void registerCNS(String[] params) throws Exception {
        if (params.length < 2) {
            HelpInfo.promptHelp("registerCNS");
            return;
        }
        if ("-h".equals(params[1]) || "--help".equals(params[1])) {
            HelpInfo.registerCNSHelp();
            return;
        }
        if (params.length < 4) {
            HelpInfo.promptHelp("registerCNS");
            return;
        }

        // registerCNS contractPath contractAddress contractVersion
        String contractPath = params[1];
        String contractAddress = params[2];
        String contractVersion = params[3];

        Address convertAddr = ConsoleUtils.convertAddress(contractAddress);
        if (!convertAddr.isValid()) {
            return;
        }
        contractAddress = convertAddr.getAddress();

        File solFile = PathUtils.getSolFile(contractPath);
        String name = solFile.getName().split("\\.")[0];

        /** check if contractAddress exist */
        Code code = getWeb3j().getCode(contractAddress).send();
        if (code.getCode() == null || Numeric.cleanHexPrefix(code.getCode()).isEmpty()) {
            ConsoleUtils.printJson(
                    PrecompiledCommon.transferToJson(PrecompiledCommon.InvalidTableNotExist));
            System.out.println();
            return;
        }

        String abi = "";
        if (solFile.getName().endsWith(PathUtils.SOL_POSTFIX)) {
            // solidity source file
            abi = ConsoleUtils.compileSolForABI(name, solFile);
        } else { // solidity abi file
            byte[] bytes = Files.readAllBytes(solFile.toPath());
            abi = new String(bytes);
        }

        CnsService cnsService =
                new CnsService(web3j, accountManager.getCurrentAccountCredentials());
        List<CnsInfo> qcns = cnsService.queryCnsByNameAndVersion(name, contractVersion);
        if (qcns.size() != 0) {
            ConsoleUtils.printJson(
                    PrecompiledCommon.transferToJson(
                            PrecompiledCommon.ContractNameAndVersionExist));
            System.out.println();
            return;
        }

        try {

            if (logger.isDebugEnabled()) {
                logger.debug(
                        " contractAddress: {}, contractName: {}, contractVersion: {}, file: {}",
                        contractAddress,
                        name,
                        contractVersion,
                        solFile.getName());
            }

            if (!ContractClassFactory.checkVersion(contractVersion)) {
                return;
            }

            // register cns
            TransactionReceipt receipt =
                    cnsService.registerCnsAndRetReceipt(
                            name, contractVersion, contractAddress, abi);
            if (receipt.isStatusOK()) { // deal with precompiled return
                String result = PrecompiledCommon.handleTransactionReceipt(receipt, web3j);
                ConsoleUtils.printJson(result);
                if (result.contains("success")) {
                    deployContractManager.addNewDeployContract(
                            String.valueOf(groupID), name, contractAddress);
                }
            } else { // deal with transaction result
                PrecompiledUtility.handleTransactionReceipt(receipt);
            }

            System.out.println();
        } catch (Exception e) {
            if (e.getMessage().contains("0x19")) {
                ConsoleUtils.printJson(PrecompiledCommon.transferToJson(Common.PermissionCode));
                System.out.println();
            } else {
                throw e;
            }
        }
    }

    public void handleTransactionReceipt(String abi, TransactionReceipt receipt)
            throws IOException, BaseException, TransactionException {

        System.out.println("transaction hash: " + receipt.getTransactionHash());

        /*
        printf the whole transaction receipt
         */
        // ConsoleUtils.singleLine();
        // System.out.println("transaction receipt:");
        // ConsoleUtils.printJson(ObjectMapperFactory.getObjectMapper().writeValueAsString(receipt));

        ConsoleUtils.singleLine();
        System.out.println("transaction status: " + receipt.getStatus());
        if (StatusCode.Success.equals(receipt.getStatus())) {
            System.out.println("description: " + "transaction executed successfully");
        } else {
            String errorMessage = StatusCode.getStatusMessage(receipt.getStatus());
            /*
            decode revert message in format of Error(string)
             */
            //            Tuple2<Boolean, String> hasRevertMsg =
            // RevertResolver.tryResolveRevertMessage(receipt);
            //            if (hasRevertMsg.getValue1()) {
            //                errorMessage = (errorMessage + ", " + hasRevertMsg.getValue2());
            //            }
            System.out.println(
                    "description: "
                            + errorMessage
                            + ", please refer to "
                            + StatusCodeLink.txReceiptStatusLink);
            return;
        }

        String version = PrecompiledCommon.BCOS_VERSION;
        if (version == null
                || PrecompiledCommon.BCOS_RC1.equals(version)
                || PrecompiledCommon.BCOS_RC2.equals(version)
                || PrecompiledCommon.BCOS_RC3.equals(version)) {
            TxDecodeUtil.setInputForReceipt(web3j, receipt);
        }
        if (!Common.EMPTY_OUTPUT.equals(receipt.getOutput())) {
            TxDecodeUtil.decodeOutput(abi, receipt);
        }
        if (receipt.getLogs() != null && receipt.getLogs().size() != 0) {
            TxDecodeUtil.decodeEventLog(abi, receipt);
        }
    }
}
