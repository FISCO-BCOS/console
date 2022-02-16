package console.contract;

import static org.fisco.solc.compiler.SolidityCompiler.Options.ABI;

import console.ConsoleInitializer;
import console.common.Common;
import console.common.ConsoleUtils;
import console.common.StatusCodeLink;
import console.contract.exceptions.CompileContractException;
import console.contract.model.AbiAndBin;
import console.contract.utils.ContractCompiler;
import console.exception.CompileSolidityException;
import console.exception.ConsoleMessageException;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.client.exceptions.ClientException;
import org.fisco.bcos.sdk.codec.ABICodec;
import org.fisco.bcos.sdk.codec.ABICodecException;
import org.fisco.bcos.sdk.codec.EventEncoder;
import org.fisco.bcos.sdk.codec.wrapper.ABICodecObject;
import org.fisco.bcos.sdk.codec.wrapper.ABIDefinition;
import org.fisco.bcos.sdk.codec.wrapper.ABIDefinitionFactory;
import org.fisco.bcos.sdk.codec.wrapper.ABIObject;
import org.fisco.bcos.sdk.codec.wrapper.ContractABIDefinition;
import org.fisco.bcos.sdk.codegen.CodeGenUtils;
import org.fisco.bcos.sdk.codegen.exceptions.CodeGenException;
import org.fisco.bcos.sdk.contract.precompiled.bfs.BFSPrecompiled;
import org.fisco.bcos.sdk.contract.precompiled.bfs.BFSService;
import org.fisco.bcos.sdk.contract.precompiled.cns.CnsService;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.model.CryptoType;
import org.fisco.bcos.sdk.model.PrecompiledRetCode;
import org.fisco.bcos.sdk.transaction.manager.AssembleTransactionProcessorInterface;
import org.fisco.bcos.sdk.transaction.manager.TransactionProcessorFactory;
import org.fisco.bcos.sdk.transaction.model.dto.CallResponse;
import org.fisco.bcos.sdk.transaction.model.dto.TransactionResponse;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;
import org.fisco.bcos.sdk.transaction.model.exception.TransactionBaseException;
import org.fisco.bcos.sdk.utils.Hex;
import org.fisco.solc.compiler.CompilationResult;
import org.fisco.solc.compiler.SolidityCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleContractImpl implements ConsoleContractFace {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleContractImpl.class);
    public static String DEPLOY_METHOD = "deploy";

    private Client client;
    private AssembleTransactionProcessorInterface assembleTransactionProcessor;
    private CnsService cnsService;
    private BFSService bfsService;
    private ABICodec abiCodec;

    public ConsoleContractImpl(Client client) throws Exception {
        this.client = client;
        CryptoKeyPair cryptoKeyPair = client.getCryptoSuite().getCryptoKeyPair();
        this.assembleTransactionProcessor =
                TransactionProcessorFactory.createAssembleTransactionProcessor(
                        client, cryptoKeyPair);
        this.cnsService = new CnsService(client, cryptoKeyPair);
        this.bfsService = new BFSService(client, cryptoKeyPair);
        this.abiCodec = new ABICodec(client.getCryptoSuite(), client.isWASM());
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
        if (returnABIObject == null
                || returnABIObject == null
                || returnObject.isEmpty()
                || returnABIObject.isEmpty()) {
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
                                + ConsoleUtils.bytesToHex(ABICodecObject.formatBytesN(abiObject));
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
        try {
            boolean sm = client.getCryptoSuite().getCryptoTypeConfig() == CryptoType.SM_TYPE;
            AbiAndBin abiAndBin = ContractCompiler.compileContract(contractNameOrPath, sm);
            String bin = abiAndBin.getBin();
            if (sm) {
                bin = abiAndBin.getSmBin();
            }
            TransactionResponse response =
                    this.assembleTransactionProcessor.deployAndGetResponseWithStringParams(
                            abiAndBin.getAbi(), bin, inputParams, null);
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
        } catch (ClientException | CompileContractException | IOException | ABICodecException e) {
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
        } catch (ClientException | IOException | ABICodecException e) {
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
            throw new Exception("Expected at least 3 arguments but found " + (params.length - 1));
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
        List<BFSPrecompiled.BfsInfo> bfsInfos = new ArrayList<>();
        try {
            bfsInfos = bfsService.list(fixedBfsParam);
        } catch (ContractException e) {
            logger.debug("call contract, path: {}", path);
        }
        if (bfsInfos.size() == 1 && bfsInfos.get(0).getFileType().equals(Common.BFS_TYPE_LNK)) {
            // call link
            BFSPrecompiled.BfsInfo bfsInfo = bfsInfos.get(0);
            List<String> ext = bfsInfo.getExt();
            if (ext.size() < 2) {
                System.out.println(
                        "Resource " + path + " doesnt have abi ext, maybe this is not a link.");
                return;
            }
            AbiAndBin abiAndBin = new AbiAndBin(ext.get(1), "", "");
            String address = ext.get(0);
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
            AbiAndBin abiAndBin,
            String contractName,
            String contractAddress,
            String functionName,
            List<String> callParams)
            throws IOException, CodeGenException, ABICodecException, CompileContractException {
        try {
            boolean sm = client.getCryptoSuite().getCryptoTypeConfig() == CryptoType.SM_TYPE;
            // load bin and abi
            if (abiAndBin == null) {
                String wasmAbiAddress = "";
                if (client.isWASM()) {
                    wasmAbiAddress =
                            Base64.getUrlEncoder()
                                    .withoutPadding()
                                    .encodeToString(
                                            (ContractCompiler.BFS_APPS_PREFIX + contractAddress)
                                                    .getBytes(StandardCharsets.UTF_8));
                }
                abiAndBin =
                        ContractCompiler.loadAbiAndBin(
                                client.getGroup(),
                                contractName,
                                client.isWASM() ? wasmAbiAddress : contractAddress,
                                sm);
            }
            logger.trace(
                    "callContract contractName: {}, contractAddress: {}",
                    contractName,
                    contractAddress);
            // call
            ABIDefinition abiDefinition = getAbiDefinition(abiAndBin, functionName);
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
                sendCall(abiAndBin, contractName, contractAddress, functionName, callParams);
            }
            // send transaction
            else {
                sendTransaction(
                        abiAndBin,
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
            throws ABICodecException, TransactionBaseException {
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
            throws TransactionBaseException, ABICodecException {
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
        String contractName = "";
        if (consoleInitializer.getClient().isWASM()) {
            contractFileName = ConsoleUtils.fixedBfsParam(contractFileName, pwd);
            if (contractFileName.startsWith(ContractCompiler.BFS_APPS_PREFIX)) {
                contractFileName =
                        contractFileName.substring(ContractCompiler.BFS_APPS_PREFIX.length());
            }
            String contractAddress =
                    Base64.getUrlEncoder()
                            .withoutPadding()
                            .encodeToString(
                                    (ContractCompiler.BFS_APPS_PREFIX + contractFileName)
                                            .getBytes(StandardCharsets.UTF_8));
            contractName = FilenameUtils.getBaseName(contractFileName);
            AbiAndBin abiAndBin =
                    ContractCompiler.loadAbiAndBin(
                            consoleInitializer.getGroupID(), contractName, contractAddress, false);
            abiStr = abiAndBin.getAbi();
        } else {
            if (!contractFileName.endsWith(ConsoleUtils.SOL_SUFFIX)) {
                contractFileName =
                        ConsoleUtils.SOLIDITY_PATH
                                + File.separator
                                + contractFileName
                                + ConsoleUtils.SOL_SUFFIX;
            }
            File solFile = new File(contractFileName);
            if (!solFile.exists()) {
                System.out.println("The contract file " + contractFileName + " doesn't exist!");
                return;
            }
            contractName = solFile.getName().split("\\.")[0];

            // compile ecdsa
            SolidityCompiler.Result res =
                    SolidityCompiler.compile(
                            solFile, (client.getCryptoType() == CryptoType.SM_TYPE), true, ABI);

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
            abiStr = contractMetadata.abi;
        }

        // Read Content of the file
        ABIDefinitionFactory abiDefinitionFactory =
                new ABIDefinitionFactory(client.getCryptoSuite());
        ContractABIDefinition contractABIDefinition = abiDefinitionFactory.loadABI(abiStr);
        if (Objects.isNull(contractABIDefinition)) {
            System.out.println(" Unable to load " + contractName + " abi");
            logger.warn(" contract: {}, abi: {}", contractName, abiStr);
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
            System.out.println(contractName + " contains no method.");
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
}
