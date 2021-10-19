package console.contract;

import static org.fisco.solc.compiler.SolidityCompiler.Options.ABI;

import console.common.Address;
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
import org.fisco.bcos.sdk.codec.datatypes.Array;
import org.fisco.bcos.sdk.codec.datatypes.Bytes;
import org.fisco.bcos.sdk.codec.datatypes.StructType;
import org.fisco.bcos.sdk.codec.datatypes.Type;
import org.fisco.bcos.sdk.codec.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.codec.wrapper.ABICodecObject;
import org.fisco.bcos.sdk.codec.wrapper.ABIDefinition;
import org.fisco.bcos.sdk.codec.wrapper.ABIDefinitionFactory;
import org.fisco.bcos.sdk.codec.wrapper.ABIObject;
import org.fisco.bcos.sdk.codec.wrapper.ContractABIDefinition;
import org.fisco.bcos.sdk.codegen.CodeGenUtils;
import org.fisco.bcos.sdk.codegen.exceptions.CodeGenException;
import org.fisco.bcos.sdk.contract.precompiled.cns.CnsInfo;
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
    private ABICodec abiCodec;

    public ConsoleContractImpl(Client client) throws Exception {
        this.client = client;
        CryptoKeyPair cryptoKeyPair = client.getCryptoSuite().getCryptoKeyPair();
        this.assembleTransactionProcessor =
                TransactionProcessorFactory.createAssembleTransactionProcessor(
                        client, cryptoKeyPair);
        this.cnsService = new CnsService(client, cryptoKeyPair);
        this.abiCodec = new ABICodec(client.getCryptoSuite(), client.isWASM());
    }

    @Override
    public void deploy(String[] params, String pwd) throws ConsoleMessageException {
        if (!client.isWASM()) {
            String contractNameOrPath = ConsoleUtils.resolvePath(params[1]);
            String contractName = ConsoleUtils.getContractName(contractNameOrPath);
            List<String> inputParams = Arrays.asList(params).subList(2, params.length);
            deploySolidity(contractName, contractNameOrPath, inputParams);
        } else {
            String binPath = ConsoleUtils.resolvePath(params[1]);
            String abiPath = ConsoleUtils.resolvePath(params[2]);
            String path = params[3];
            if (!path.startsWith("/")) {
                path = pwd + "/" + path;
            }
            List<String> inputParams = Arrays.asList(params).subList(4, params.length);
            deployWasm(binPath, abiPath, path, inputParams);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        String strHex = "";
        StringBuilder sb = new StringBuilder("");
        for (int n = 0; n < bytes.length; n++) {
            strHex = Integer.toHexString(bytes[n] & 0xFF);
            sb.append((strHex.length() == 1) ? "0" + strHex : strHex);
        }
        return sb.toString().trim();
    }

    public void printReturnResults(List<Type> results) {
        StringBuilder resultType = new StringBuilder();
        StringBuilder resultData = new StringBuilder();
        resultType.append("(");
        resultData.append("(");
        for (int i = 0; i < results.size(); ++i) {
            getReturnResults(resultType, resultData, results.get(i));
            if (i != results.size() - 1) {
                resultType.append(", ");
                resultData.append(", ");
            }
        }
        resultType.append(")");
        resultData.append(")");
        System.out.println("Return value size:" + results.size());
        System.out.println("Return types: " + resultType);
        System.out.println("Return values:" + resultData);
    }

    public void getReturnResults(StringBuilder resultType, StringBuilder resultData, Type result) {
        if (result instanceof Array) {
            resultType.append("[");
            resultData.append("[");
            List<Type> values = ((Array) result).getValue();
            for (int i = 0; i < values.size(); ++i) {
                getReturnResults(resultType, resultData, values.get(i));
                if (i != values.size() - 1) {
                    resultType.append(", ");
                    resultData.append(", ");
                }
            }
            resultData.append("]");
            resultType.append("]");
        } else if (result instanceof StructType) {
            throw new UnsupportedOperationException();
        } else if (result instanceof Bytes) {
            String data = "hex://0x" + bytesToHex(((Bytes) result).getValue());
            resultType.append(result.getTypeAsString());
            resultData.append(data);
        } else {
            resultType.append(result.getTypeAsString());
            resultData.append(result.getValue());
        }
    }

    public void printReturnObject(
            List<Object> returnObject, List<ABIObject> returnABIObject, String returnValue) {
        if (returnABIObject == null
                || returnABIObject == null
                || returnObject.size() == 0
                || returnABIObject.size() == 0) {
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
                String data = "hex://0x" + bytesToHex(ABICodecObject.formatBytesN(abiObject));
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
            AbiAndBin abiAndBin = ContractCompiler.compileContract(contractNameOrPath);
            String bin = abiAndBin.getBin();
            if (client.getCryptoSuite().getCryptoTypeConfig() == CryptoType.SM_TYPE) {
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
                    client.getGroupId(), abiAndBin, contractName, contractAddress);
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
            writeLog(contractName, path);
            // save the bin and abi
            AbiAndBin abiAndBin =
                    client.getCryptoSuite().getCryptoTypeConfig() == CryptoType.SM_TYPE
                            ? new AbiAndBin(abi, null, binStr)
                            : new AbiAndBin(abi, binStr, null);

            String contractAddress =
                    Base64.getUrlEncoder()
                            .withoutPadding()
                            .encodeToString(path.getBytes(StandardCharsets.UTF_8));
            ContractCompiler.saveAbiAndBin(
                    client.getGroupId(), abiAndBin, contractName, contractAddress);
            return response;
        } catch (ClientException | IOException | ABICodecException e) {
            throw new ConsoleMessageException("deploy contract failed due to:" + e.getMessage(), e);
        }
    }

    private synchronized void writeLog(String contractName, String contractAddress) {
        contractName = ConsoleUtils.removeSolPostfix(contractName);
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
                        + client.getGroupId()
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
                        && ("[group:" + client.getGroupId() + "]").equals(contractInfos[2])) {
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
        if (!path.startsWith("/")) {
            path = pwd + "/" + path;
        }
        String contractName = FilenameUtils.getBaseName(path);
        String contractAddress =
                Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(path.getBytes(StandardCharsets.UTF_8));
        List<String> callParams = Arrays.asList(params).subList(3, params.length);
        callContract(null, contractName, path, contractAddress, functionName, callParams);
    }

    private void callSolidity(String[] params) throws Exception {
        String contractNameOrPath = ConsoleUtils.resolvePath(params[1]);
        String contractAddressStr = params[2];
        String contractName = ConsoleUtils.getContractName(contractNameOrPath);

        if (contractAddressStr.equals("latest")) {
            File contractDir =
                    new File(
                            ContractCompiler.COMPILED_PATH
                                    + File.separator
                                    + client.getGroupId()
                                    + File.separator
                                    + contractName);
            if (!contractDir.exists()) {
                System.out.println(
                        "Can not find the latest address. Please make sure group "
                                + client.getGroupId()
                                + " has deployed contract \""
                                + contractName
                                + "\"!");
                return;
            }
            File[] contractAddressFiles = contractDir.listFiles();
            if (contractAddressFiles == null || contractAddressFiles.length == 0) {
                System.out.println(
                        "Can not find the latest address. Please make sure group "
                                + client.getGroupId()
                                + " has deployed contract \""
                                + contractName
                                + "\"!");
                return;
            }
            ConsoleUtils.sortFiles(contractAddressFiles);
            for (File contractAddressFile : contractAddressFiles) {
                if (!contractAddressFile.isDirectory()) {
                    continue;
                }

                if (!ConsoleUtils.isValidAddress(contractAddressFile.getName())) {
                    continue;
                }
                contractAddressStr = contractAddressFile.getName();
                break;
            }

            System.out.println(
                    "latest contract address for \""
                            + contractName
                            + "\" is "
                            + contractAddressStr);
        }

        // check contract address
        Address contractAddress = ConsoleUtils.convertAddress(contractAddressStr);
        if (!contractAddress.isValid()) {
            System.out.println("Invalid contract address: " + contractAddressStr);
            return;
        }
        contractAddressStr = contractAddress.getAddress();

        String functionName = params[3];
        // get callParams
        List<String> callParams = Arrays.asList(params).subList(4, params.length);
        callContract(
                null,
                contractName,
                contractNameOrPath,
                contractAddressStr,
                functionName,
                callParams);
    }

    @Override
    public void call(String[] params, String pwd) throws Exception {
        if (this.client.isWASM()) {
            callWasm(params, pwd);
        } else {
            callSolidity(params);
        }
    }

    protected void callContract(
            AbiAndBin abiAndBin,
            String contractName,
            String contractNameOrPath,
            String contractAddress,
            String functionName,
            List<String> callParams)
            throws IOException, CodeGenException, ABICodecException, CompileContractException {
        try {
            // load bin and abi
            for (String p : callParams) {
                System.out.println("P: " + p);
            }
            if (abiAndBin == null) {
                abiAndBin =
                        ContractCompiler.loadAbiAndBin(
                                client.getGroupId(),
                                contractName,
                                contractNameOrPath,
                                contractAddress,
                                !this.client.isWASM());
            }
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
                logger.debug(
                        "sendCall request, params: {}, contractAddress: {}, contractName: {}, functionName:{}, paramSize: {}",
                        callParams.toString(),
                        this.client.isWASM() ? contractNameOrPath : contractAddress,
                        contractName,
                        functionName,
                        callParams.size());
                CryptoKeyPair cryptoKeyPair = client.getCryptoSuite().getCryptoKeyPair();
                CallResponse response =
                        assembleTransactionProcessor.sendCallWithStringParams(
                                cryptoKeyPair.getAddress(),
                                this.client.isWASM() ? contractNameOrPath : contractAddress,
                                abiAndBin.getAbi(),
                                functionName,
                                callParams);

                ConsoleUtils.singleLine();
                System.out.println("Return code: " + response.getReturnCode());
                if (response.getReturnCode() == PrecompiledRetCode.CODE_SUCCESS.getCode()) {
                    System.out.println("description: " + "transaction executed successfully");
                    System.out.println("Return message: " + response.getReturnMessage());
                    ConsoleUtils.singleLine();
                    printReturnResults(response.getResults());
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
            // send transaction
            else {
                logger.trace(
                        "sendTransactionAndGetResponse request, params: {}, contractAddress: {}, contractName: {}, functionName: {}, paramSize:{},  abiDefinition: {}",
                        callParams.toString(),
                        this.client.isWASM() ? contractNameOrPath : contractAddress,
                        contractName,
                        functionName,
                        callParams.size(),
                        abiDefinition);
                TransactionResponse response =
                        assembleTransactionProcessor.sendTransactionWithStringParamsAndGetResponse(
                                this.client.isWASM() ? contractNameOrPath : contractAddress,
                                abiAndBin.getAbi(),
                                functionName,
                                callParams);
                System.out.println(
                        "transaction hash: "
                                + response.getTransactionReceipt().getTransactionHash());
                ConsoleUtils.singleLine();
                System.out.println(
                        "transaction status: " + response.getTransactionReceipt().getStatus());

                if (response.getTransactionReceipt().getStatus() == 0) {
                    System.out.println("description: " + "transaction executed successfully");
                }
                ConsoleUtils.singleLine();
                System.out.println("Receipt message: " + response.getReceiptMessages());
                System.out.println("Return message: " + response.getReturnMessage());
                printReturnResults(response.getResults());
                ConsoleUtils.singleLine();
                if (response.getEvents() != null && !response.getEvents().equals("")) {
                    System.out.println("Event logs");
                    System.out.println("Event: " + response.getEvents());
                }
            }

        } catch (TransactionBaseException e) {
            System.out.println(
                    "call for "
                            + contractName
                            + " failed, contractAddress: "
                            + (this.client.isWASM() ? contractNameOrPath : contractAddress));
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

    @Override
    public void deployByCNS(String[] params) throws ConsoleMessageException {
        try {
            String contractNameOrPath = ConsoleUtils.resolvePath(params[1]);
            String contractVersion = params[2];
            String contractName = ConsoleUtils.getContractName(contractNameOrPath);
            // query the the contractName and version has been registered or not
            Tuple2<String, String> cnsTuple =
                    cnsService.selectByNameAndVersion(contractName, contractVersion);
            if (cnsTuple.getValue1() != null
                    && cnsTuple.getValue2() != null
                    && !cnsTuple.getValue2().equals("")) {
                System.out.println(
                        "The version \""
                                + contractVersion
                                + "\" of contract \""
                                + contractName
                                + "\" already exists!");
                return;
            }
            List<String> inputParams = Arrays.asList(params).subList(3, params.length);
            TransactionResponse response =
                    deploySolidity(contractName, contractNameOrPath, inputParams);
            if (response.getReturnCode() != PrecompiledRetCode.CODE_SUCCESS.getCode()) {
                return;
            }
            String contractAddress = response.getContractAddress();
            AbiAndBin abiAndBin =
                    ContractCompiler.loadAbiAndBin(
                            client.getGroupId(), contractNameOrPath, contractName, contractAddress);
            // register cns
            ConsoleUtils.printJson(
                    cnsService
                            .registerCNS(
                                    contractName,
                                    contractVersion,
                                    contractAddress,
                                    abiAndBin.getAbi())
                            .toString());
        } catch (ContractException e) {
            throw new ConsoleMessageException(
                    "deployByCNS failed for " + e.getMessage() + ", code: " + e.getErrorCode(), e);
        } catch (IOException | CodeGenException e) {
            throw new ConsoleMessageException("deployByCNS failed for " + e.getMessage(), e);
        } catch (CompileContractException e) {
            throw new ConsoleMessageException("deployByCNS failed for " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void callByCNS(String[] params) throws Exception {
        String contractNameAndVersion = params[1];
        String contractNameOrPath = ConsoleUtils.resolvePath(contractNameAndVersion);
        String contractVersion = null;
        String contractAbi = "";
        if (contractNameAndVersion.contains(":")) {
            String[] nameAndVersion = contractNameAndVersion.split(":");
            if (nameAndVersion.length == 2) {
                contractNameOrPath = nameAndVersion[0].trim();
                contractVersion = nameAndVersion[1].trim();
            } else {
                System.out.println(
                        "Contract name and version has incorrect format. For example, contractName:contractVersion");
                return;
            }
        }
        String contractName = ConsoleUtils.getContractNameWithoutCheckExists(contractNameOrPath);
        logger.debug(
                "callByCNS, contractName: {}, contractVersion: {}, contractNameAndVersion: {}, cnsService: {}",
                contractNameOrPath,
                contractVersion,
                contractNameAndVersion,
                cnsService);
        if (contractName.endsWith(".sol")) {
            contractName = contractName.substring(0, contractName.length() - 4);
        }

        // get address from cnsService
        String contractAddress = "";
        try {
            if (contractVersion != null) {
                Tuple2<String, String> cnsTuple =
                        cnsService.selectByNameAndVersion(contractName, contractVersion);
                if ("".equals(cnsTuple.getValue1())) {
                    System.out.println(
                            "Can't find \""
                                    + contractName
                                    + ":"
                                    + contractVersion
                                    + "\" information from the cns list! Please deploy it by cns firstly!\n");
                    return;
                }
                // get address
                contractAddress = cnsTuple.getValue1();
                // get abi
                contractAbi = cnsTuple.getValue2();
            } else {
                List<CnsInfo> cnsInfos = cnsService.selectByName(contractName);
                if (cnsInfos.size() == 0) {
                    System.out.println(
                            "Can't find \""
                                    + contractName
                                    + "\" information from the cns list! Please deploy it by cns firstly!\n");
                    return;
                }
                CnsInfo latestCNSInfo = cnsInfos.get(cnsInfos.size() - 1);
                contractAddress = latestCNSInfo.getAddress();
                contractAbi = latestCNSInfo.getAbi();
            }
        } catch (ContractException e) {
            System.out.println("Error when getting cns information: ");
            System.out.println("Error message: " + e.getMessage());
            System.out.println(
                    "Please check the existence of the contract name \""
                            + contractName
                            + "\""
                            + " and contractVersion \""
                            + contractVersion
                            + "\"");
            return;
        } catch (ClientException e) {
            System.out.println("Error when getting cns information: ");
            System.out.println("Error message: " + e.getMessage());
        }
        String functionName = params[2];
        List<String> inputParams = Arrays.asList(params).subList(3, params.length);
        AbiAndBin abiAndBin = null;
        if (!contractAbi.equals("") && contractAbi != null) {
            abiAndBin = new AbiAndBin(contractAbi, null, null);
        }
        callContract(
                abiAndBin,
                contractName,
                contractNameOrPath,
                contractAddress,
                functionName,
                inputParams);
    }

    public void listAbi(String[] params) throws Exception {
        String contractFileName = params[1];
        if (!contractFileName.endsWith(ConsoleUtils.SOL_POSTFIX)) {
            contractFileName =
                    ConsoleUtils.SOLIDITY_PATH
                            + File.separator
                            + contractFileName
                            + ConsoleUtils.SOL_POSTFIX;
        }
        File solFile = new File(contractFileName);
        if (!solFile.exists()) {
            System.out.println("The contract file " + contractFileName + " doesn't exist!");
            return;
        }
        String contractName = solFile.getName().split("\\.")[0];

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

        // Read Content of the file
        ABIDefinitionFactory abiDefinitionFactory =
                new ABIDefinitionFactory(client.getCryptoSuite());
        ContractABIDefinition contractABIDefinition =
                abiDefinitionFactory.loadABI(contractMetadata.abi);
        if (Objects.isNull(contractABIDefinition)) {
            System.out.println(" Unable to load " + contractName + " abi");
            logger.warn(" contract: {}, abi: {}", contractName, contractMetadata.abi);
            return;
        }

        Map<ByteBuffer, ABIDefinition> methodIDToFunctions =
                contractABIDefinition.getMethodIDToFunctions();

        if (!methodIDToFunctions.isEmpty()) {
            System.out.println("Method list: ");
            System.out.printf(
                    " %-20s|    %-10s|    %-10s  |    %-10s\n",
                    "name", "constant", "methodId", "signature");
            System.out.println("  -------------------------------------------------------------- ");
            for (Map.Entry<ByteBuffer, ABIDefinition> entry : methodIDToFunctions.entrySet()) {
                System.out.printf(
                        " %-20s|    %-10s|    %-10s  |    %-10s\n",
                        entry.getValue().getName(),
                        entry.getValue().isConstant(),
                        entry.getValue().getMethodId(client.getCryptoSuite()),
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
            for (Map.Entry<String, List<ABIDefinition>> entry : events.entrySet()) {
                EventEncoder eventEncoder = new EventEncoder(client.getCryptoSuite());
                System.out.printf(
                        " %-20s|   %-66s  |   %10s\n",
                        entry.getValue().get(0).getName(),
                        eventEncoder.buildEventSignature(
                                entry.getValue().get(0).getMethodSignatureAsString()),
                        entry.getValue().get(0).getMethodSignatureAsString());
            }
        }
    }
}
