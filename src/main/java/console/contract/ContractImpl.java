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
import console.common.TxDecodeUtil;
import console.exception.CompileSolidityException;
import console.exception.ConsoleMessageException;
import io.bretty.console.table.Alignment;
import io.bretty.console.table.ColumnFormatter;
import io.bretty.console.table.Table;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigInteger;
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
import org.fisco.bcos.web3j.precompile.permission.PermissionInfo;
import org.fisco.bcos.web3j.precompile.permission.PermissionService;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.channel.StatusCode;
import org.fisco.bcos.web3j.protocol.core.RemoteCall;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.tuples.generated.Tuple2;
import org.fisco.bcos.web3j.tx.Contract;
import org.fisco.bcos.web3j.tx.RevertResolver;
import org.fisco.bcos.web3j.tx.exceptions.ContractCallException;
import org.fisco.bcos.web3j.tx.gas.ContractGasProvider;
import org.fisco.bcos.web3j.tx.gas.StaticGasProvider;
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
            TransactionReceipt transactionReceipt = contract.getTransactionReceipt().get();
            String contractAddress = contract.getContractAddress();
            System.out.println("contract address: " + contractAddress);
            System.out.println();
            contractAddress = contract.getContractAddress();
            deployContractManager.addNewDeployContract(
                    String.valueOf(groupID), name, contractAddress);
        } catch (Exception e) {
            if (e.getMessage().contains("0x19")) {
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
            if (logger.isWarnEnabled()) {
                logger.warn(" contract: {}, abi: {}", contractName, contractMetadata.abi);
            }
            return;
        }

        Map<String, ABIDefinition> methodIDToFunctions =
                contractABIDefinition.getMethodIDToFunctions();

        if (!methodIDToFunctions.isEmpty()) {
            System.out.println(" \tMethod list: ");
            System.out.println(
                    " \t -------------------------------------------------------------- ");
            System.out.printf(
                    " \t%-5s \t%-5s \t%-10s \t%-10s\n",
                    "name", "constant", "methodId", "signature");
            for (Map.Entry<String, ABIDefinition> entry : methodIDToFunctions.entrySet()) {
                System.out.printf(
                        "  \t%-5s| \t%-5b| \t%-10s| \t%-10s\n",
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
            System.out.println(" \tEvent list: ");
            System.out.println(
                    " \t -------------------------------------------------------------- ");
            System.out.printf("\t  %-10s   %-66s   %-10s\n", "name", "topic", "signature");
            for (Map.Entry<String, ABIDefinition> entry : methodIDToFunctions.entrySet()) {

                System.out.printf(
                        "\t %-10s|   %66s|   %-10s\n",
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

        String contractNameOrPath = params[1];
        File solFile = PathUtils.getSolFile(contractNameOrPath);
        String name = solFile.getName().split("\\.")[0];

        Class<?> contractClass = ContractClassFactory.compileContract(solFile);
        Method load =
                contractClass.getMethod(
                        "load",
                        String.class,
                        Web3j.class,
                        Credentials.class,
                        ContractGasProvider.class);
        String contractAddress = params[2];
        Address convertAddr = ConsoleUtils.convertAddress(contractAddress);
        if (!convertAddr.isValid()) {
            return;
        }
        contractAddress = convertAddr.getAddress();
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
                            + " is undefined of the contract.");
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
                ContractClassFactory.getPrametersObject(
                        funcName, parameterType, newParams, generic);
        if (argobj == null) {
            return;
        }
        RemoteCall<?> remoteCall = (RemoteCall<?>) func.invoke(contractObject, argobj);
        Object result = remoteCall.send();
        if (result instanceof TransactionReceipt) {
            TransactionReceipt receipt = (TransactionReceipt) result;

            Tuple2<Boolean, String> booleanStringTuple2 =
                    RevertResolver.tryResolveRevertMessage(receipt);

            if (StatusCode.RevertInstruction.equals(receipt.getStatus())) {
                throw new ContractCallException(
                        "The execution of the contract rolled back"
                                + (booleanStringTuple2.getValue1()
                                        ? ", " + booleanStringTuple2.getValue2()
                                        : "")
                                + ".");
            }
            if (StatusCode.CallAddressError.equals(receipt.getStatus())) {
                System.out.println("The contract address is incorrect.");
                System.out.println();
                return;
            }
            if (!StatusCode.Success.equals(receipt.getStatus())) {
                System.out.println(
                        StatusCode.getStatusMessage(receipt.getStatus(), receipt.getMessage())
                                + (booleanStringTuple2.getValue1()
                                        ? ", " + booleanStringTuple2.getValue2()
                                        : "")
                                + ".");
                System.out.println();
                return;
            }
            String output = receipt.getOutput();
            if (!"0x".equals(output)) {
                int code = new BigInteger(output.substring(2, output.length()), 16).intValue();
                if (code == Common.TableExist) {
                    ConsoleUtils.printJson(PrecompiledCommon.transferToJson(Common.TableExist));
                    System.out.println();
                    return;
                }
                if (code == Common.PermissionCode) {
                    ConsoleUtils.printJson(PrecompiledCommon.transferToJson(Common.PermissionCode));
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
        if (result instanceof TransactionReceipt) {
            AbiAndBin abiAndBin = TxDecodeUtil.readAbiAndBin(name);
            String abi = abiAndBin.getAbi();
            TransactionReceipt receipt = (TransactionReceipt) result;
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
        }

        String contractNameOrPath = params[1];
        File solFile = PathUtils.getSolFile(contractNameOrPath);
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

        String contractNameOrPath = params[1];
        File solFile = PathUtils.getSolFile(contractNameOrPath);
        String name = solFile.getName().split("\\.")[0];

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
                if (!ContractClassFactory.checkVersion(contractVersion)) {
                    return;
                }
                contractNameAndVersion = name + ":" + contractVersion;
            } else {
                contractNameAndVersion = name;
            }
        }
        // get address from cns
        String contractAddress = "";
        CnsService cnsResolver =
                new CnsService(web3j, accountManager.getCurrentAccountCredentials());
        try {
            contractAddress =
                    cnsResolver.getAddressByContractNameAndVersion(contractNameAndVersion);
        } catch (Exception e) {
            int i = e.getMessage().indexOf("ContractCallException");
            if (i < 0) {
                System.out.println(
                        "Error when getting cns information (maybe the contract version does not exist), error info: ");
                System.out.println(e.getMessage().toString());
                System.out.println();
            } else {
                System.out.println(
                        e.getMessage().substring(i + "ContractCallException".length() + 2));
                System.out.println();
            }
            return;
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
                            + " is undefined of the contract.");
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
                ContractClassFactory.getPrametersObject(
                        funcName, parameterType, newParams, generic);
        if (argobj == null) {
            return;
        }
        RemoteCall<?> remoteCall = (RemoteCall<?>) func.invoke(contractObject, argobj);
        Object result = remoteCall.send();
        if (result instanceof TransactionReceipt) {
            TransactionReceipt receipt = (TransactionReceipt) result;

            Tuple2<Boolean, String> booleanStringTuple2 =
                    RevertResolver.tryResolveRevertMessage(receipt);
            if (booleanStringTuple2.getValue1()) {
                throw new ContractCallException(booleanStringTuple2.getValue2());
            }

            if (StatusCode.RevertInstruction.equals(receipt.getStatus())) {
                throw new ContractCallException(
                        "The execution of the contract rolled back"
                                + (booleanStringTuple2.getValue1()
                                        ? ", " + booleanStringTuple2.getValue2()
                                        : "")
                                + ".");
            }
            if (StatusCode.CallAddressError.equals(receipt.getStatus())) {
                System.out.println("The contract address is incorrect.");
                System.out.println();
                return;
            }
            if (!StatusCode.Success.equals(receipt.getStatus())) {
                System.out.println(
                        StatusCode.getStatusMessage(receipt.getStatus(), receipt.getMessage()));
                System.out.println();
                return;
            }
            String output = receipt.getOutput();
            if (!"0x".equals(output)) {
                int code = new BigInteger(output.substring(2, output.length()), 16).intValue();
                if (code == Common.TableExist) {
                    ConsoleUtils.printJson(PrecompiledCommon.transferToJson(Common.TableExist));
                    System.out.println();
                    return;
                }
                if (code == Common.PermissionCode) {
                    ConsoleUtils.printJson(PrecompiledCommon.transferToJson(Common.PermissionCode));
                    System.out.println();
                    return;
                }
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
        if (result instanceof TransactionReceipt) {
            AbiAndBin abiAndBin = TxDecodeUtil.readAbiAndBin(name);
            String abi = abiAndBin.getAbi();
            TransactionReceipt receipt = (TransactionReceipt) result;
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
}
