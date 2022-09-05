package console.contract;

import static org.fisco.solc.compiler.SolidityCompiler.Options.ABI;
import static org.fisco.solc.compiler.SolidityCompiler.Options.BIN;
import static org.fisco.solc.compiler.SolidityCompiler.Options.METADATA;

import console.ConsoleInitializer;
import console.common.Common;
import console.common.ConsoleUtils;
import console.common.StatusCodeLink;
import console.contract.exceptions.CompileContractException;
import console.contract.model.AbiAndBin;
import console.contract.utils.ContractCompiler;
import console.exception.CompileSolidityException;
import console.exception.ConsoleMessageException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.fisco.bcos.codegen.v3.exceptions.CodeGenException;
import org.fisco.bcos.codegen.v3.utils.CodeGenUtils;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.client.exceptions.ClientException;
import org.fisco.bcos.sdk.v3.client.protocol.response.Abi;
import org.fisco.bcos.sdk.v3.codec.ContractCodecException;
import org.fisco.bcos.sdk.v3.codec.EventEncoder;
import org.fisco.bcos.sdk.v3.codec.wrapper.ABIDefinition;
import org.fisco.bcos.sdk.v3.codec.wrapper.ABIDefinitionFactory;
import org.fisco.bcos.sdk.v3.codec.wrapper.ABIObject;
import org.fisco.bcos.sdk.v3.codec.wrapper.ContractABIDefinition;
import org.fisco.bcos.sdk.v3.codec.wrapper.ContractCodecTools;
import org.fisco.bcos.sdk.v3.contract.precompiled.bfs.BFSService;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.v3.model.CryptoType;
import org.fisco.bcos.sdk.v3.model.PrecompiledRetCode;
import org.fisco.bcos.sdk.v3.transaction.manager.AssembleTransactionProcessorInterface;
import org.fisco.bcos.sdk.v3.transaction.manager.TransactionProcessorFactory;
import org.fisco.bcos.sdk.v3.transaction.model.dto.CallResponse;
import org.fisco.bcos.sdk.v3.transaction.model.dto.TransactionResponse;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;
import org.fisco.bcos.sdk.v3.transaction.model.exception.TransactionBaseException;
import org.fisco.bcos.sdk.v3.utils.Hex;
import org.fisco.bcos.sdk.v3.utils.Numeric;
import org.fisco.bcos.sdk.v3.utils.StringUtils;
import org.fisco.solc.compiler.CompilationResult;
import org.fisco.solc.compiler.SolidityCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleContractImpl implements ConsoleContractFace {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleContractImpl.class);

    private final Client client;
    private final AssembleTransactionProcessorInterface assembleTransactionProcessor;
    private final BFSService bfsService;

    public ConsoleContractImpl(Client client) {
        this.client = client;
        CryptoKeyPair cryptoKeyPair = client.getCryptoSuite().getCryptoKeyPair();
        this.assembleTransactionProcessor =
                TransactionProcessorFactory.createAssembleTransactionProcessor(
                        client, cryptoKeyPair);
        this.bfsService = new BFSService(client, cryptoKeyPair);
    }

    @Override
    public void deploy(String[] params, String pwd) throws Exception {
        if (!client.isWASM()) {
            String contractNameOrPath = ConsoleUtils.resolvePath(params[1]);
            String contractName = ConsoleUtils.getContractName(contractNameOrPath);
            if (contractName.endsWith(".wasm")) {
                throw new Exception("Error: you should not treat a WASM file as Solidity!");
            }
            List<String> inputParams = Arrays.asList(params).subList(2, params.length);
            deploySolidity(contractName, contractNameOrPath, inputParams);
        } else {
            String binPath = ConsoleUtils.getLiquidFilePath(ConsoleUtils.resolvePath(params[1]));
            if (binPath.endsWith(".sol")) {
                throw new Exception("Error: you should not treat a Solidity file as WASM!");
            }
            String abiPath = ConsoleUtils.getLiquidFilePath(ConsoleUtils.resolvePath(params[2]));
            String path = params[3];
            try {
                path = ConsoleUtils.fixedBfsParam(path, pwd);
                if (path.startsWith(ContractCompiler.BFS_APPS_FULL_PREFIX)) {
                    path = path.substring(ContractCompiler.BFS_APPS_PREFIX.length());
                }
            } catch (Exception e) {
                System.out.println("Path parse error for: " + e.getMessage());
                System.out.println("Please use 'deploy -h' to check deploy arguments.");
                return;
            }
            List<String> inputParams = Arrays.asList(params).subList(4, params.length);
            deployWasm(binPath, abiPath, path, inputParams);
        }
    }

    public void printReturnObject(
            List<Object> returnObject, List<ABIObject> returnABIObject, String returnValue) {
        if (returnABIObject == null || returnObject.isEmpty() || returnABIObject.isEmpty()) {
            System.out.println("Return values:" + returnValue);
            return;
        }
        StringBuilder resultType = new StringBuilder();
        StringBuilder resultData = new StringBuilder();
        resultType.append("(");
        resultData.append("(");
        getReturnObjectOutputData(resultType, resultData, returnObject, returnABIObject);
        if (resultType.toString().endsWith(", ")) {
            resultType.delete(resultType.length() - 2, resultType.length());
        }
        if (resultData.toString().endsWith(", ")) {
            resultData.delete(resultData.length() - 2, resultData.length());
        }
        resultType.append(")");
        resultData.append(")");
        System.out.println("Return value size:" + returnObject.size());
        System.out.println("Return types: " + resultType);
        System.out.println("Return values:" + resultData);
    }

    public void getReturnObjectOutputData(
            StringBuilder resultType,
            StringBuilder resultData,
            List<Object> returnObject,
            List<ABIObject> returnABIObject) {
        int i = 0;
        for (ABIObject abiObject : returnABIObject) {
            if (abiObject.getListValues() != null) {
                resultType.append("[");
                resultData.append("[");
                getReturnObjectOutputData(
                        resultType,
                        resultData,
                        (List<Object>) returnObject.get(i),
                        abiObject.getListValues());
                if (resultType.toString().endsWith(", ")) {
                    resultType.delete(resultType.length() - 2, resultType.length());
                }
                if (resultData.toString().endsWith(", ")) {
                    resultData.delete(resultData.length() - 2, resultData.length());
                }
                resultData.append("] ");
                resultType.append("] ");
                i += 1;
                continue;
            }
            if (abiObject.getValueType() == null && returnObject.size() > i) {
                resultData.append(returnObject.get(i).toString()).append(", ");
                i += 1;
                continue;
            }
            resultType.append(abiObject.getValueType()).append(", ");
            if (abiObject.getValueType().equals(ABIObject.ValueType.BYTES)) {
                String data =
                        "hex://0x"
                                + ConsoleUtils.bytesToHex(
                                        ContractCodecTools.formatBytesN(abiObject));
                resultData.append(data).append(", ");
            } else if (returnObject.size() > i) {
                resultData.append(returnObject.get(i).toString()).append(", ");
            }
            i += 1;
        }
    }

    public TransactionResponse deploySolidity(
            String contractName, String contractNameOrPath, List<String> inputParams)
            throws ConsoleMessageException {
        List<String> tempInputParams = inputParams;
        try {
            boolean isContractParallelAnalysis = false;
            if (!inputParams.isEmpty()) {
                if ("-p".equals(inputParams.get(inputParams.size() - 1))
                        || "--parallel-analysis".equals(inputParams.get(inputParams.size() - 1))) {
                    isContractParallelAnalysis = true;
                    tempInputParams = inputParams.subList(0, inputParams.size() - 1);
                    logger.info(
                            "deploy contract {} with '--parallel-analysis' or '-p'", contractName);
                }
            }

            boolean sm = client.getCryptoSuite().getCryptoTypeConfig() == CryptoType.SM_TYPE;
            AbiAndBin abiAndBin =
                    ContractCompiler.compileContract(
                            contractNameOrPath, sm, isContractParallelAnalysis);
            String bin = abiAndBin.getBin();
            if (sm) {
                bin = abiAndBin.getSmBin();
            }
            TransactionResponse response =
                    this.assembleTransactionProcessor.deployAndGetResponseWithStringParams(
                            abiAndBin.getAbi(), bin, tempInputParams, null);
            if (response.getReturnCode() != PrecompiledRetCode.CODE_SUCCESS.getCode()) {
                System.out.println("deploy contract for " + contractName + " failed!");
                System.out.println("return message: " + response.getReturnMessage());
                System.out.println("return code:" + response.getReturnCode());
                printReturnObject(
                        response.getReturnObject(),
                        response.getReturnABIObject(),
                        response.getValues());
                return response;
            }
            String contractAddress = response.getTransactionReceipt().getContractAddress();
            if (!contractAddress.startsWith("0x")) {
                contractAddress = "0x" + contractAddress;
            }
            System.out.println(
                    "transaction hash: " + response.getTransactionReceipt().getTransactionHash());
            System.out.println("contract address: " + contractAddress);
            System.out.println(
                    "currentAccount: " + client.getCryptoSuite().getCryptoKeyPair().getAddress());
            writeLog(contractName, contractAddress);
            // save the bin and abi
            ContractCompiler.saveAbiAndBin(
                    client.getGroup(), abiAndBin, contractName, contractAddress);
            return response;
        } catch (ClientException
                | CompileContractException
                | IOException
                | ContractCodecException e) {
            throw new ConsoleMessageException("deploy contract failed for " + e.getMessage(), e);
        }
    }

    private byte[] readBytes(File file) throws IOException {
        byte[] bytes = new byte[(int) file.length()];
        FileInputStream fileInputStream = new FileInputStream(file);
        if (fileInputStream.read(bytes) != bytes.length) {
            throw new IOException("incomplete reading of file: " + file.toString());
        }
        fileInputStream.close();
        return bytes;
    }

    public TransactionResponse deployWasm(
            String binPath, String abiPath, String path, List<String> inputParams)
            throws ConsoleMessageException {
        try {
            File binFile = new File(binPath);
            byte[] bin = readBytes(binFile);
            String binStr = Hex.toHexString(bin);
            File abiFile = new File(abiPath);
            String abi = FileUtils.readFileToString(abiFile);
            TransactionResponse response =
                    this.assembleTransactionProcessor.deployAndGetResponseWithStringParams(
                            abi, binStr, inputParams, path);
            if (response.getReturnCode() != PrecompiledRetCode.CODE_SUCCESS.getCode()) {
                System.out.println("deploy contract for " + path + " failed!");
                System.out.println("return message: " + response.getReturnMessage());
                System.out.println("return code:" + response.getReturnCode());
                printReturnObject(
                        response.getReturnObject(),
                        response.getReturnABIObject(),
                        response.getValues());
                return response;
            }

            System.out.println(
                    "transaction hash: " + response.getTransactionReceipt().getTransactionHash());
            System.out.println("contract address: " + path);
            System.out.println(
                    "currentAccount: " + client.getCryptoSuite().getCryptoKeyPair().getAddress());
            String contractName = FilenameUtils.getBaseName(path);
            writeLog(contractName, ContractCompiler.BFS_APPS_PREFIX + path);
            // save the bin and abi
            AbiAndBin abiAndBin =
                    client.getCryptoSuite().getCryptoTypeConfig() == CryptoType.SM_TYPE
                            ? new AbiAndBin(abi, null, binStr)
                            : new AbiAndBin(abi, binStr, null);

            String contractAddress =
                    Base64.getUrlEncoder()
                            .withoutPadding()
                            .encodeToString(
                                    (ContractCompiler.BFS_APPS_PREFIX + path)
                                            .getBytes(StandardCharsets.UTF_8));
            ContractCompiler.saveAbiAndBin(
                    client.getGroup(), abiAndBin, contractName, contractAddress);
            return response;
        } catch (ClientException | IOException | ContractCodecException e) {
            throw new ConsoleMessageException("deploy contract failed due to:" + e.getMessage(), e);
        }
    }

    private synchronized void writeLog(String contractName, String contractAddress) {
        contractName = ConsoleUtils.removeSolSuffix(contractName);
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
            }
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        while (contractName.length() < 20) {
            contractName = contractName + " ";
        }
        String log =
                LocalDateTime.now().format(formatter)
                        + "  [group:"
                        + client.getGroup()
                        + "]  "
                        + contractName
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
            logger.error(" message: {}, e: {}", e.getMessage(), e);
            return;
        }
    }

    public void getDeployLog(String[] params) throws Exception {
        String queryRecordNumber = "";
        int recordNumber = Common.QueryLogCount;
        if (params.length == 2) {
            queryRecordNumber = params[1];
            try {
                recordNumber = Integer.parseInt(queryRecordNumber);
                if (recordNumber <= 0 || recordNumber > 100) {
                    System.out.println(
                            "Please provide record number by integer mode, "
                                    + Common.DeployLogIntegerRange
                                    + ".");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println(
                        "Please provide record number by integer mode, "
                                + Common.DeployLogIntegerRange
                                + ".");
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
                        && ("[group:" + client.getGroup() + "]").equals(contractInfos[2])) {
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
            } else {
                System.out.println(stringBuilder.toString());
            }
        } catch (Exception e) {
            logger.error(" load {} failed, e: {}", Common.ContractLogFileName, e);
        } finally {
            reader.close();
        }
    }

    public ABIDefinition getAbiDefinition(AbiAndBin abiAndBin, String functionName)
            throws IOException {
        if (abiAndBin.getAbi().isEmpty()) {
            throw new IOException("Abi is empty, please check contract abi exists.");
        }
        List<ABIDefinition> abiDefinitions =
                CodeGenUtils.loadContractAbiDefinition(abiAndBin.getAbi());
        for (ABIDefinition definition : abiDefinitions) {
            if (definition.getName() != null && definition.getName().equals(functionName)) {
                return definition;
            }
        }
        return null;
    }

    private void callWasm(String[] params, String pwd) throws Exception {
        String path = params[1];
        String functionName = params[2];
        path = ConsoleUtils.fixedBfsParam(path, pwd);
        String contractName = FilenameUtils.getBaseName(path);
        if (path.startsWith(ContractCompiler.BFS_APPS_FULL_PREFIX)) {
            path = path.substring(ContractCompiler.BFS_APPS_PREFIX.length());
        }
        List<String> callParams = Arrays.asList(params).subList(3, params.length);
        callContract(null, contractName, path, functionName, callParams);
    }

    private void callSolidity(String[] params) throws Exception {
        String contractNameOrPath = ConsoleUtils.resolvePath(params[1]);
        String contractAddressStr = params[2];
        if (params.length < 4) {
            throw new Exception(
                    "Expected at least 3 arguments but found "
                            + (params.length - 1)
                            + ".\nPlease check the contract address or link is valid.");
        }
        String functionName = params[3];
        String contractName = ConsoleUtils.getContractName(contractNameOrPath);

        if (contractAddressStr.equals("latest")) {
            File contractDir =
                    new File(
                            ContractCompiler.COMPILED_PATH
                                    + File.separator
                                    + client.getGroup()
                                    + File.separator
                                    + contractName);
            if (!contractDir.exists()) {
                System.out.println(
                        "Can not find the latest address. Please make sure group "
                                + client.getGroup()
                                + " has deployed contract \""
                                + contractName
                                + "\"!");
                return;
            }
            File[] contractAddressFiles = contractDir.listFiles();
            if (contractAddressFiles == null || contractAddressFiles.length == 0) {
                System.out.println(
                        "Can not find the latest address. Please make sure group "
                                + client.getGroup()
                                + " has deployed contract \""
                                + contractName
                                + "\"!");
                return;
            }
            ConsoleUtils.sortFiles(contractAddressFiles);
            for (File contractAddressFile : contractAddressFiles) {
                if (contractAddressFile.isDirectory()
                        && ConsoleUtils.isValidAddress(contractAddressFile.getName())) {
                    contractAddressStr = contractAddressFile.getName();
                    break;
                }
            }

            System.out.println(
                    "latest contract address for \""
                            + contractName
                            + "\" is "
                            + contractAddressStr);
        }

        // check contract address
        if (!ConsoleUtils.isValidAddress(contractAddressStr)) {
            System.out.println("Invalid contract address: " + contractAddressStr);
            return;
        }
        // get callParams
        List<String> callParams = Arrays.asList(params).subList(4, params.length);
        callContract(null, contractName, contractAddressStr, functionName, callParams);
    }

    @Override
    public void call(String[] params, String pwd) throws Exception {
        String path = params[1];
        String fixedBfsParam = ConsoleUtils.fixedBfsParam(path, pwd);
        String address = "";
        try {
            address = bfsService.readlink(fixedBfsParam);
        } catch (ContractException e) {
            logger.debug("call contract, path: {}", path, e);
        }
        if (!address.isEmpty() && !address.equals(Common.EMPTY_CONTRACT_ADDRESS)) {
            String abi = client.getABI(address).getABI();
            if (abi.isEmpty()) {
                System.out.println(
                        "Resource " + path + " doesnt have abi, maybe this is not a link.");
                return;
            }
            AbiAndBin abiAndBin = new AbiAndBin(abi, "", "");
            String functionName = params[2];
            List<String> inputParams = Arrays.asList(params).subList(3, params.length);
            callContract(abiAndBin, "", address, functionName, inputParams);
            return;
        }
        if (this.client.isWASM()) {
            callWasm(params, pwd);
        } else {
            callSolidity(params);
        }
    }

    protected void callContract(
            AbiAndBin abi,
            String contractName,
            String contractAddress,
            String functionName,
            List<String> callParams)
            throws IOException, CodeGenException, ContractCodecException {
        try {
            // just load abi
            // load local abi first
            if (abi == null) {
                String wasmAbiAddress = "";
                if (client.isWASM()) {
                    wasmAbiAddress =
                            Base64.getUrlEncoder()
                                    .withoutPadding()
                                    .encodeToString(
                                            (ContractCompiler.BFS_APPS_PREFIX + contractAddress)
                                                    .getBytes(StandardCharsets.UTF_8));
                }
                abi =
                        ContractCompiler.loadAbi(
                                client.getGroup(),
                                contractName,
                                client.isWASM()
                                        ? wasmAbiAddress
                                        : Numeric.prependHexPrefix(contractAddress));
                // still empty, get abi on chain
                if (abi.getAbi().isEmpty()) {
                    Abi remoteAbi = client.getABI(contractAddress);
                    abi.setAbi(remoteAbi.getABI());
                    ContractCompiler.saveAbiAndBin(
                            client.getGroup(),
                            abi,
                            contractName,
                            client.isWASM()
                                    ? wasmAbiAddress
                                    : Numeric.prependHexPrefix(contractAddress));
                }
            }
            logger.trace(
                    "callContract contractName: {}, contractAddress: {}",
                    contractName,
                    contractAddress);
            // call
            ABIDefinition abiDefinition = getAbiDefinition(abi, functionName);
            if (abiDefinition == null) {
                System.out.println(
                        "call contract \""
                                + contractName
                                + "\" failed ! Please check the existence of method \""
                                + functionName
                                + "\"");
                return;
            }

            if (abiDefinition.isConstant()) {
                sendCall(abi, contractName, contractAddress, functionName, callParams);
            }
            // send transaction
            else {
                sendTransaction(
                        abi,
                        contractName,
                        contractAddress,
                        functionName,
                        callParams,
                        abiDefinition);
            }

        } catch (TransactionBaseException e) {
            System.out.println(
                    "call for " + contractName + " failed, contractAddress: " + contractAddress);
            if (e.getRetCode() != null) {
                ConsoleUtils.printJson(e.getRetCode().toString());
            } else {
                System.out.println(e.getMessage());
            }
            String errorMessage = e.getMessage();
            if (e.getRetCode() != null) {
                errorMessage = e.getRetCode().getMessage();
            }
            System.out.println(
                    "description: "
                            + errorMessage
                            + ", please refer to "
                            + StatusCodeLink.txReceiptStatusLink);
        }
    }

    private void sendTransaction(
            AbiAndBin abiAndBin,
            String contractName,
            String contractAddress,
            String functionName,
            List<String> callParams,
            ABIDefinition abiDefinition)
            throws ContractCodecException, TransactionBaseException {
        if (logger.isTraceEnabled()) {
            logger.trace(
                    "sendTransactionAndGetResponse request, params: {}, contractAddress: {}, contractName: {}, functionName: {}, paramSize:{},  abiDefinition: {}",
                    callParams,
                    contractAddress,
                    contractName,
                    functionName,
                    callParams.size(),
                    abiDefinition);
        }
        TransactionResponse response =
                assembleTransactionProcessor.sendTransactionWithStringParamsAndGetResponse(
                        contractAddress, abiAndBin.getAbi(), functionName, callParams);
        System.out.println(
                "transaction hash: " + response.getTransactionReceipt().getTransactionHash());
        ConsoleUtils.singleLine();
        System.out.println("transaction status: " + response.getTransactionReceipt().getStatus());

        if (response.getTransactionReceipt().getStatus() == 0) {
            System.out.println("description: " + "transaction executed successfully");
        }
        ConsoleUtils.singleLine();
        System.out.println("Receipt message: " + response.getReceiptMessages());
        System.out.println("Return message: " + response.getReturnMessage());
        ConsoleUtils.printReturnResults(response.getResults());
        ConsoleUtils.singleLine();
        if (response.getEvents() != null && !response.getEvents().equals("")) {
            System.out.println("Event logs");
            System.out.println("Event: " + response.getEvents());
        }
    }

    private void sendCall(
            AbiAndBin abiAndBin,
            String contractName,
            String contractAddress,
            String functionName,
            List<String> callParams)
            throws TransactionBaseException, ContractCodecException {
        if (logger.isDebugEnabled()) {
            logger.debug(
                    "sendCall request, params: {}, contractAddress: {}, contractName: {}, functionName:{}, paramSize: {}",
                    callParams,
                    contractAddress,
                    contractName,
                    functionName,
                    callParams.size());
        }
        CryptoKeyPair cryptoKeyPair = client.getCryptoSuite().getCryptoKeyPair();
        CallResponse response =
                assembleTransactionProcessor.sendCallWithStringParams(
                        cryptoKeyPair.getAddress(),
                        contractAddress,
                        abiAndBin.getAbi(),
                        functionName,
                        callParams);

        ConsoleUtils.singleLine();
        System.out.println("Return code: " + response.getReturnCode());
        if (response.getReturnCode() == PrecompiledRetCode.CODE_SUCCESS.getCode()) {
            System.out.println("description: " + "transaction executed successfully");
            System.out.println("Return message: " + response.getReturnMessage());
            ConsoleUtils.singleLine();
            ConsoleUtils.printReturnResults(response.getResults());
        } else {
            String errorMessage = response.getReturnMessage();
            System.out.println(
                    "description: "
                            + errorMessage
                            + ", please refer to "
                            + StatusCodeLink.txReceiptStatusLink);
        }
        ConsoleUtils.singleLine();
    }

    @Override
    public void listAbi(ConsoleInitializer consoleInitializer, String[] params, String pwd)
            throws Exception {
        String contractFileName = params[1];
        String abiStr = "";
        if (consoleInitializer.getClient().isWASM()) {
            abiStr = getWasmAbi(consoleInitializer.getGroupID(), pwd, contractFileName);
        } else {
            abiStr = getSolidityAbi(contractFileName);
        }

        // Read Content of the file
        ABIDefinitionFactory abiDefinitionFactory =
                new ABIDefinitionFactory(client.getCryptoSuite());
        ContractABIDefinition contractABIDefinition = abiDefinitionFactory.loadABI(abiStr);
        if (Objects.isNull(contractABIDefinition)) {
            System.out.println(" Unable to load " + contractFileName + " abi");
            logger.warn(" contract: {}, abi: {}", contractFileName, abiStr);
            return;
        }

        Map<ByteBuffer, ABIDefinition> methodIDToFunctions =
                contractABIDefinition.getMethodIDToFunctions();

        if (!methodIDToFunctions.isEmpty()) {
            System.out.println("Method list: ");
            System.out.printf(
                    " %-20s|    %-10s|    %-10s  |    %-10s%n",
                    "name", "constant", "methodId", "signature");
            System.out.println("  -------------------------------------------------------------- ");
            for (Map.Entry<ByteBuffer, ABIDefinition> entry : methodIDToFunctions.entrySet()) {
                System.out.printf(
                        " %-20s|    %-10s|    %-10s  |    %-10s%n",
                        entry.getValue().getName(),
                        entry.getValue().isConstant(),
                        Hex.toHexString(entry.getValue().getMethodId(client.getCryptoSuite())),
                        entry.getValue().getMethodSignatureAsString());
            }
        } else {
            System.out.println(contractFileName + " contains no method.");
        }

        Map<String, List<ABIDefinition>> events = contractABIDefinition.getEvents();
        if (!events.isEmpty()) {
            System.out.println();
            System.out.println("Event list: ");
            System.out.printf(" %-20s|   %-66s     %10s%n", "name", "topic", "signature");
            System.out.println("  -------------------------------------------------------------- ");
            for (Map.Entry<String, List<ABIDefinition>> entry : events.entrySet()) {
                EventEncoder eventEncoder = new EventEncoder(client.getCryptoSuite());
                System.out.printf(
                        " %-20s|   %-66s  |   %10s%n",
                        entry.getValue().get(0).getName(),
                        eventEncoder.buildEventSignature(
                                entry.getValue().get(0).getMethodSignatureAsString()),
                        entry.getValue().get(0).getMethodSignatureAsString());
            }
        }
    }

    @Override
    public void listDeployContractAddress(
            ConsoleInitializer consoleInitializer, String[] params, String pwd) throws Exception {
        boolean isWasm = consoleInitializer.getClient().isWASM();
        String contractNameOrPath = ConsoleUtils.resolvePath(params[1]);
        String contractName =
                isWasm
                        ? FilenameUtils.getBaseName(contractNameOrPath)
                        : ConsoleUtils.getContractName(contractNameOrPath);
        File contractFile =
                new File(
                        ContractCompiler.COMPILED_PATH
                                + File.separator
                                + consoleInitializer.getClient().getGroup()
                                + File.separator
                                + contractName);
        int recordNum = 20;
        if (params.length == 3) {
            recordNum =
                    ConsoleUtils.processNonNegativeNumber(
                            "recordNum", params[2], 1, Integer.MAX_VALUE);
            if (recordNum == Common.InvalidReturnNumber) {
                return;
            }
        }
        if (!contractFile.exists()) {
            System.out.println("Contract \"" + contractName + "\" doesn't exist!\n");
            return;
        }
        int i = 0;
        File[] contractFileList = contractFile.listFiles();
        if (contractFileList == null || contractFileList.length == 0) {
            return;
        }
        ConsoleUtils.sortFiles(contractFileList);
        for (File contractAddressFile : contractFileList) {
            if (!isWasm && !ConsoleUtils.isValidAddress(contractAddressFile.getName())) {
                continue;
            }
            String contractAddress =
                    isWasm
                            ? new String(
                                    Base64.getUrlDecoder().decode(contractAddressFile.getName()))
                            : contractAddressFile.getName();
            System.out.printf(
                    "%s  %s%n",
                    contractAddress, ConsoleUtils.getFileCreationTime(contractAddressFile));
            i++;
            if (i == recordNum) {
                break;
            }
        }
    }

    private String getSolidityAbi(String contractFileName) throws Exception {
        String contractFilePath = contractFileName;
        if (!contractFilePath.endsWith(ConsoleUtils.SOL_SUFFIX)) {
            contractFilePath =
                    ConsoleUtils.SOLIDITY_PATH
                            + File.separator
                            + contractFilePath
                            + ConsoleUtils.SOL_SUFFIX;
        }
        File solFile = new File(contractFilePath);
        if (!solFile.exists()) {
            throw new Exception("The contract file " + contractFilePath + " doesn't exist!");
        }
        String contractName = solFile.getName().split("\\.")[0];

        List<SolidityCompiler.Option> defaultOptions = Arrays.asList(ABI, BIN, METADATA);
        List<SolidityCompiler.Option> options = new ArrayList<>(defaultOptions);

        if (ContractCompiler.solcJVersion.compareToIgnoreCase(ConsoleUtils.COMPILE_WITH_BASE_PATH)
                >= 0) {
            logger.debug(
                    "compileSolToBinAndAbi, solc version:{} ,basePath: {}",
                    ContractCompiler.solcJVersion,
                    solFile.getParentFile().getCanonicalPath());
            SolidityCompiler.Option basePath =
                    new SolidityCompiler.CustomOption(
                            "base-path", solFile.getParentFile().getCanonicalPath());
            options.add(basePath);
        } else {
            logger.debug(
                    "compileSolToBinAndAbi, solc version:{}",
                    org.fisco.solc.compiler.Version.version);
        }

        // compile ecdsa
        SolidityCompiler.Result res =
                SolidityCompiler.compile(
                        solFile,
                        (client.getCryptoType() == CryptoType.SM_TYPE),
                        true,
                        options.toArray(new SolidityCompiler.Option[0]));

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
        return contractMetadata.abi;
    }

    private String getWasmAbi(String groupId, String pwd, String contractFileName)
            throws Exception {
        String abiStr;
        if (contractFileName.endsWith(ContractCompiler.ABI_SUFFIX)) {
            // read file
            File abiFile = new File(contractFileName);
            if (!abiFile.exists() || !abiFile.isFile()) {
                throw new Exception(
                        "The contract abi file " + contractFileName + " doesn't exist!");
            }
            abiStr = new String(CodeGenUtils.readBytes(abiFile));
        } else {
            // read contract
            String absoluteFileName = ConsoleUtils.fixedBfsParam(contractFileName, pwd);
            // load local abi file first
            String contractAddress =
                    Base64.getUrlEncoder()
                            .withoutPadding()
                            .encodeToString((absoluteFileName).getBytes(StandardCharsets.UTF_8));
            String contractName = FilenameUtils.getBaseName(absoluteFileName);
            AbiAndBin abiAndBin = ContractCompiler.loadAbi(groupId, contractName, contractAddress);
            abiStr = abiAndBin.getAbi();
            if (StringUtils.isEmpty(abiStr)) {
                // still empty, read remote abi
                Abi remoteAbi = client.getABI(contractFileName);
                abiStr = remoteAbi.getABI();
            }
        }
        return abiStr;
    }
}
