package console.contract;

import static console.contract.utils.ContractCompiler.mergeSource;
import static org.fisco.solc.compiler.SolidityCompiler.Options.ABI;
import static org.fisco.solc.compiler.SolidityCompiler.Options.BIN;
import static org.fisco.solc.compiler.SolidityCompiler.Options.METADATA;

import console.ConsoleInitializer;
import console.command.category.ContractOpCommand;
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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.fisco.bcos.codegen.v3.exceptions.CodeGenException;
import org.fisco.bcos.codegen.v3.utils.CodeGenUtils;
import org.fisco.bcos.sdk.jni.common.JniException;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.client.exceptions.ClientException;
import org.fisco.bcos.sdk.v3.client.protocol.response.Abi;
import org.fisco.bcos.sdk.v3.codec.ContractCodecException;
import org.fisco.bcos.sdk.v3.codec.EventEncoder;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.v3.codec.wrapper.ABIDefinition;
import org.fisco.bcos.sdk.v3.codec.wrapper.ABIDefinitionFactory;
import org.fisco.bcos.sdk.v3.codec.wrapper.ContractABIDefinition;
import org.fisco.bcos.sdk.v3.contract.precompiled.bfs.BFSPrecompiled;
import org.fisco.bcos.sdk.v3.contract.precompiled.bfs.BFSService;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.v3.model.CryptoType;
import org.fisco.bcos.sdk.v3.model.EnumNodeVersion;
import org.fisco.bcos.sdk.v3.model.PrecompiledRetCode;
import org.fisco.bcos.sdk.v3.model.RetCode;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.transaction.codec.decode.RevertMessageParser;
import org.fisco.bcos.sdk.v3.transaction.manager.AssembleTransactionProcessorInterface;
import org.fisco.bcos.sdk.v3.transaction.manager.TransactionProcessorFactory;
import org.fisco.bcos.sdk.v3.transaction.manager.transactionv1.AssembleTransactionService;
import org.fisco.bcos.sdk.v3.transaction.manager.transactionv1.ProxySignTransactionManager;
import org.fisco.bcos.sdk.v3.transaction.manager.transactionv1.TransferTransactionService;
import org.fisco.bcos.sdk.v3.transaction.manager.transactionv1.dto.DeployTransactionRequestWithStringParams;
import org.fisco.bcos.sdk.v3.transaction.manager.transactionv1.dto.TransactionRequestWithStringParams;
import org.fisco.bcos.sdk.v3.transaction.manager.transactionv1.utils.TransactionRequestBuilder;
import org.fisco.bcos.sdk.v3.transaction.model.dto.CallResponse;
import org.fisco.bcos.sdk.v3.transaction.model.dto.TransactionResponse;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;
import org.fisco.bcos.sdk.v3.transaction.model.exception.TransactionBaseException;
import org.fisco.bcos.sdk.v3.transaction.tools.Convert;
import org.fisco.bcos.sdk.v3.utils.AddressUtils;
import org.fisco.bcos.sdk.v3.utils.Hex;
import org.fisco.bcos.sdk.v3.utils.Numeric;
import org.fisco.bcos.sdk.v3.utils.StringUtils;
import org.fisco.solc.compiler.CompilationResult;
import org.fisco.solc.compiler.SolidityCompiler;
import org.fisco.solc.compiler.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleContractImpl implements ConsoleContractFace {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleContractImpl.class);

    private final Client client;
    private final AssembleTransactionProcessorInterface assembleTransactionProcessor;
    // new version tx v2
    private AssembleTransactionService assembleTransactionService = null;
    private final TransferTransactionService transferTransactionService;
    private final BFSService bfsService;
    private byte[] extension = null;
    private boolean useTransactionV1 = false;

    public ConsoleContractImpl(Client client) {
        this.client = client;
        CryptoKeyPair cryptoKeyPair = client.getCryptoSuite().getCryptoKeyPair();
        this.assembleTransactionProcessor =
                TransactionProcessorFactory.createAssembleTransactionProcessor(
                        client, cryptoKeyPair);
        this.bfsService = new BFSService(client, cryptoKeyPair);
        ProxySignTransactionManager proxySignTransactionManager =
                new ProxySignTransactionManager(client);
        transferTransactionService = new TransferTransactionService(proxySignTransactionManager);
    }

    public ConsoleContractImpl(Client client, boolean useTransactionV1) {
        this(client);
        this.useTransactionV1 = useTransactionV1;
        if (useTransactionV1) {
            int negotiatedProtocol = client.getNegotiatedProtocol();
            // if protocol version < 2, it means client connect to old version node, not support
            if ((negotiatedProtocol >> 16) < 2) {
                throw new UnsupportedOperationException(
                        "The node version is too low, incompatible with v1 params, please upgrade the node to 3.6.0 or higher");
            }
            assembleTransactionService = new AssembleTransactionService(client);
        }
    }

    public void setExtension(byte[] extension) {
        this.extension = extension;
    }

    @Override
    public void deploy(String[] params, String pwd) throws Exception {
        List<String> paramsList = new ArrayList<>(Arrays.asList(params));
        String linkPath = null;
        String address = null;
        String abiString = null;
        if (paramsList.contains("-l")) {
            int index = paramsList.indexOf("-l");
            // unique -l
            if (paramsList.lastIndexOf("-l") != index) {
                throw new Exception("option '-l' should be unique in one deploy!");
            }
            // -l follows param
            if (index == paramsList.size() - 1) {
                throw new Exception("option '-l' should follows path param");
            }
            if (paramsList.size() <= ContractOpCommand.DEPLOY.getMinParamLength() + 2) {
                throw new Exception(
                        "Expected at least "
                                + ContractOpCommand.DEPLOY.getMinParamLength()
                                + " without link arguments, but found "
                                + (paramsList.size() - 3));
            }
            // get path
            linkPath = ConsoleUtils.fixedBfsParam(paramsList.get(index + 1), pwd);
            List<String> levels = ConsoleUtils.path2Level(linkPath);
            if (levels.size() != 3 || !levels.get(0).equals("apps")) {
                System.out.println(
                        "Link must locate in /apps, and only support 3-level directory.");
                System.out.println("Example: ln /apps/Name/Version 0x1234567890");
                return;
            }
            paramsList.remove(index);
            paramsList.remove(index);
            if (logger.isTraceEnabled()) {
                logger.trace(
                        "deploy with link, link path:{}, params:{}",
                        linkPath,
                        StringUtils.joinAll(",", params));
            }
            System.out.println("deploy contract with link, link path: " + linkPath);
        }
        if (!client.isWASM()) {
            // solidity
            String contractNameOrPath = ConsoleUtils.resolvePath(paramsList.get(1));
            // have and only have one
            String contractName;
            if (contractNameOrPath.contains(":")
                    && contractNameOrPath.indexOf(':') == contractNameOrPath.lastIndexOf(':')) {
                String[] strings = contractNameOrPath.split(":");
                contractNameOrPath = strings[0];
                contractName = strings[1];
            } else {
                contractName = ConsoleUtils.getContractName(contractNameOrPath);
            }
            List<String> inputParams = paramsList.subList(2, paramsList.size());
            TransactionResponse transactionResponse =
                    deploySolidity(contractName, contractNameOrPath, inputParams);
            address = transactionResponse.getContractAddress();
            abiString = ContractCompiler.loadAbi(client.getGroup(), contractName, address).getAbi();
        } else {
            // liquid
            String wasmSuffix =
                    client.getCryptoSuite().getCryptoTypeConfig() == CryptoType.SM_TYPE
                            ? "_gm" + ContractCompiler.WASM_SUFFIX
                            : ContractCompiler.WASM_SUFFIX;
            String liquidDir = paramsList.get(1);
            String binPath = ConsoleUtils.scanPathWithSuffix(liquidDir, wasmSuffix);
            String abiPath =
                    ConsoleUtils.scanPathWithSuffix(liquidDir, ContractCompiler.ABI_SUFFIX);
            String path = paramsList.get(2);
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
            List<String> inputParams = paramsList.subList(3, paramsList.size());
            TransactionResponse transactionResponse =
                    deployWasm(binPath, abiPath, path, inputParams);
            address = transactionResponse.getContractAddress();
            abiString = FileUtils.readFileToString(new File(abiPath));
        }
        if (linkPath != null && !StringUtils.isEmpty(address) && abiString != null) {
            deployLink(linkPath, address, abiString);
        }
    }

    private void deployLink(String linkPath, String address, String abiString) throws Exception {
        EnumNodeVersion.Version supportedVersion = bfsService.getCurrentVersion();
        final RetCode retCode;
        if (supportedVersion.compareTo(EnumNodeVersion.BCOS_3_1_0.toVersionObj()) >= 0) {
            retCode =
                    bfsService.link(
                            linkPath.substring(ContractCompiler.BFS_APPS_PREFIX.length()),
                            address,
                            abiString);
        } else {
            List<String> levels = ConsoleUtils.path2Level(linkPath);
            if (levels.size() != 3 || !levels.get(0).equals("apps")) {
                retCode = PrecompiledRetCode.CODE_FILE_INVALID_PATH;
            } else {
                String name = levels.get(1);
                String version = levels.get(2);
                retCode = bfsService.link(name, version, address, abiString);
            }
        }
        if (retCode.getCode() != PrecompiledRetCode.CODE_SUCCESS.getCode()) {
            System.out.println("link contract " + address + " to path " + linkPath + " failed!");
            System.out.println("return message: " + retCode.getMessage());
            System.out.println("return code:" + retCode.getCode());
            return;
        }
        System.out.println("link path: " + linkPath);
    }

    public TransactionResponse deploySolidity(
            String contractName, String contractNameOrPath, List<String> inputParams)
            throws ConsoleMessageException {
        try {
            boolean isContractParallelAnalysis = false;
            Version version = Version.V0_8_11;
            if (!inputParams.isEmpty()) {
                int lastIndexOf = inputParams.lastIndexOf("-p");
                if (lastIndexOf != -1) {
                    isContractParallelAnalysis = true;
                    inputParams.remove(lastIndexOf);
                    logger.info("deploy contract {} with '-p'", contractName);
                }

                lastIndexOf = inputParams.lastIndexOf("--parallel-analysis");
                if (lastIndexOf != -1) {
                    isContractParallelAnalysis = true;
                    inputParams.remove(lastIndexOf);
                    logger.info("deploy contract {} with '--parallel-analysis'", contractName);
                }

                lastIndexOf = inputParams.lastIndexOf("-v");
                if (lastIndexOf != -1 && lastIndexOf != inputParams.size() - 1) {
                    version = ConsoleUtils.convertStringToVersion(inputParams.get(lastIndexOf + 1));
                    inputParams.remove(lastIndexOf);
                    inputParams.remove(lastIndexOf);
                    logger.info("deploy contract {} with '-v'", contractName);
                }

                lastIndexOf = inputParams.lastIndexOf("--sol-version");
                if (lastIndexOf != -1 && lastIndexOf != inputParams.size() - 1) {
                    version = ConsoleUtils.convertStringToVersion(inputParams.get(lastIndexOf + 1));
                    inputParams.remove(lastIndexOf);
                    inputParams.remove(lastIndexOf);
                    logger.info("deploy contract {} with '--sol-version'", contractName);
                }
            }

            boolean sm =
                    (client.getCryptoSuite().getCryptoTypeConfig() == CryptoType.SM_TYPE)
                            || (client.getCryptoSuite().getCryptoTypeConfig()
                                    == CryptoType.HSM_TYPE);
            AbiAndBin abiAndBin =
                    ContractCompiler.compileContract(
                            contractNameOrPath,
                            contractName,
                            sm,
                            isContractParallelAnalysis,
                            version);
            String bin = abiAndBin.getBin();
            if (sm) {
                bin = abiAndBin.getSmBin();
            }
            TransactionResponse response;
            if (useTransactionV1) {
                DeployTransactionRequestWithStringParams request =
                        new TransactionRequestBuilder(abiAndBin.getAbi(), bin)
                                .setExtension(extension)
                                .buildDeployStringParamsRequest(inputParams);
                response = assembleTransactionService.deployContract(request);
            } else {
                response =
                        this.assembleTransactionProcessor.deployAndGetResponseWithStringParams(
                                abiAndBin.getAbi(), bin, inputParams, null);
            }
            if (response.getReturnCode() != PrecompiledRetCode.CODE_SUCCESS.getCode()) {
                System.out.println("deploy contract for " + contractName + " failed!");
                System.out.println("return message: " + response.getReturnMessage());
                System.out.println("return code:" + response.getReturnCode());
                ConsoleUtils.printResults(
                        response.getReturnABIObject(),
                        response.getReturnObject(),
                        response.getResults());
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
                | ContractException
                | JniException
                | ContractCodecException e) {
            throw new ConsoleMessageException("deploy contract failed for " + e.getMessage(), e);
        }
    }

    private byte[] readBytes(File file) throws IOException {
        byte[] bytes = new byte[(int) file.length()];
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            if (fileInputStream.read(bytes) != bytes.length) {
                throw new IOException("incomplete reading of file: " + file.toString());
            }
        }
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
            TransactionResponse response;
            if (useTransactionV1) {
                DeployTransactionRequestWithStringParams request =
                        new TransactionRequestBuilder(abi, binStr)
                                .setTo(path)
                                .setExtension(extension)
                                .buildDeployStringParamsRequest(inputParams);
                response = assembleTransactionService.deployContract(request);
            } else {
                response =
                        this.assembleTransactionProcessor.deployAndGetResponseWithStringParams(
                                abi, binStr, inputParams, path);
            }
            if (response.getReturnCode() != PrecompiledRetCode.CODE_SUCCESS.getCode()) {
                System.out.println("deploy contract for " + path + " failed!");
                System.out.println("return message: " + response.getReturnMessage());
                System.out.println("return code:" + response.getReturnCode());
                ConsoleUtils.printResults(
                        response.getReturnABIObject(),
                        response.getReturnObject(),
                        response.getResults());
                return response;
            }

            System.out.println(
                    "transaction hash: " + response.getTransactionReceipt().getTransactionHash());
            System.out.println("contract address: " + ContractCompiler.BFS_APPS_PREFIX + path);
            System.out.println(
                    "currentAccount: " + client.getCryptoSuite().getCryptoKeyPair().getAddress());
            String contractName = FilenameUtils.getBaseName(path);
            writeLog(contractName, ContractCompiler.BFS_APPS_PREFIX + path);
            // save the bin and abi
            AbiAndBin abiAndBin =
                    client.getCryptoSuite().getCryptoTypeConfig() == CryptoType.SM_TYPE
                            ? new AbiAndBin(abi, null, binStr, null)
                            : new AbiAndBin(abi, binStr, null, null);

            String contractAddress =
                    Base64.getUrlEncoder()
                            .withoutPadding()
                            .encodeToString(
                                    (ContractCompiler.BFS_APPS_PREFIX + path)
                                            .getBytes(StandardCharsets.UTF_8));
            ContractCompiler.saveAbiAndBin(
                    client.getGroup(), abiAndBin, contractName, contractAddress);
            return response;
        } catch (ClientException
                | IOException
                | JniException
                | ContractException
                | ContractCodecException e) {
            throw new ConsoleMessageException("deploy contract failed due to:" + e.getMessage(), e);
        }
    }

    private synchronized void writeLog(String contractName, String contractAddress) {
        contractName = ConsoleUtils.removeSolSuffix(contractName);

        File logFile = new File(Common.CONTRACT_LOG_FILE_NAME);
        try {
            if (!logFile.exists() && !logFile.createNewFile()) {
                System.out.println("Failed to create log file: " + Common.CONTRACT_LOG_FILE_NAME);
                return;
            }
        } catch (IOException e) {
            System.out.println("Failed to create log file: " + Common.CONTRACT_LOG_FILE_NAME);
            logger.error("create file exception", e);
            return;
        }
        try (BufferedReader reader =
                        new BufferedReader(new FileReader(Common.CONTRACT_LOG_FILE_NAME));
                PrintWriter pw =
                        new PrintWriter(new FileWriter(Common.CONTRACT_LOG_FILE_NAME, true)); ) {
            String line;
            List<String> textList = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                textList.add(line);
            }
            int i = 0;
            if (textList.size() >= Common.LOG_MAX_COUNT) {
                i = textList.size() - Common.LOG_MAX_COUNT + 1;
                if (logFile.exists()) {

                    if (!logFile.delete()) {
                        System.out.println(
                                "Failed to delete log file: " + Common.CONTRACT_LOG_FILE_NAME);
                        return;
                    }

                    if (!logFile.createNewFile()) {
                        System.out.println(
                                "Failed to create log file: " + Common.CONTRACT_LOG_FILE_NAME);
                        return;
                    }
                }
                for (; i < textList.size(); i++) {
                    pw.println(textList.get(i));
                }
                pw.flush();
            }
        } catch (IOException e) {
            System.out.println("Read deploylog.txt failed.");
            logger.error("Read deploylog.txt failed.", e);
            return;
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
        try (PrintWriter pw =
                new PrintWriter(new FileWriter(Common.CONTRACT_LOG_FILE_NAME, true))) {
            if (!logFile.exists() && !logFile.createNewFile()) {
                System.out.println("Failed to create file " + Common.CONTRACT_LOG_FILE_NAME);
            }
            pw.println(log);
            pw.flush();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            logger.error(" message: {}, e: {}", e.getMessage(), e);
        }
    }

    @Override
    public void getDeployLog(String[] params) throws Exception {
        String queryRecordNumber = "";
        int recordNumber = Common.QUERY_LOG_COUNT;
        if (params.length == 2) {
            queryRecordNumber = params[1];
            try {
                recordNumber = Integer.parseInt(queryRecordNumber);
                if (recordNumber <= 0 || recordNumber > 100) {
                    System.out.println(
                            "Please provide record number by integer mode, "
                                    + Common.DEPLOY_LOG_INTEGER_RANGE
                                    + ".");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println(
                        "Please provide record number by integer mode, "
                                + Common.DEPLOY_LOG_INTEGER_RANGE
                                + ".");
                return;
            }
        }
        File logFile = new File(Common.CONTRACT_LOG_FILE_NAME);
        if (!logFile.exists() && !logFile.createNewFile()) {
            System.out.println("Failed to create file " + Common.CONTRACT_LOG_FILE_NAME);
            return;
        }
        BufferedReader reader = new BufferedReader(new FileReader(Common.CONTRACT_LOG_FILE_NAME));
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
            if (stringBuilder.toString().isEmpty()) {
                System.out.println("Empty set.");
            } else {
                System.out.println(stringBuilder);
            }
        } catch (Exception e) {
            logger.error(" load {} failed, e: {}", Common.CONTRACT_LOG_FILE_NAME, e);
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
                        && AddressUtils.isValidAddress(contractAddressFile.getName())) {
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
        if (!AddressUtils.isValidAddress(contractAddressStr)) {
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
                Tuple2<BigInteger, List<BFSPrecompiled.BfsInfo>> list =
                        bfsService.list(fixedBfsParam, BigInteger.ZERO, BigInteger.TEN);
                if (list.getValue2().isEmpty()
                        || list.getValue2().get(0).getExt().size() < 2
                        || list.getValue2().get(0).getExt().get(1).isEmpty()) {
                    System.out.println(
                            "Resource " + path + " doesnt have abi, maybe this is not a link.");
                    return;
                }
                abi = list.getValue2().get(0).getExt().get(1);
            }
            AbiAndBin abiAndBin = new AbiAndBin(abi, "", "", null);
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
        } catch (JniException | ContractException e) {
            System.out.println(
                    "call for " + contractName + " failed, contractAddress: " + contractAddress);
            System.out.println(e.getMessage());
        }
    }

    private void sendTransaction(
            AbiAndBin abiAndBin,
            String contractName,
            String contractAddress,
            String functionName,
            List<String> callParams,
            ABIDefinition abiDefinition)
            throws ContractCodecException, TransactionBaseException, ContractException,
                    JniException {
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
        TransactionResponse response;
        if (useTransactionV1) {
            TransactionRequestWithStringParams request =
                    new TransactionRequestBuilder(abiAndBin.getAbi(), functionName, contractAddress)
                            .setExtension(extension)
                            .buildStringParamsRequest(callParams);
            response = assembleTransactionService.sendTransaction(request);
        } else {
            response =
                    assembleTransactionProcessor.sendTransactionWithStringParamsAndGetResponse(
                            contractAddress, abiAndBin.getAbi(), functionName, callParams);
        }
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
        ConsoleUtils.printResults(
                response.getReturnABIObject(), response.getReturnObject(), response.getResults());
        ConsoleUtils.singleLine();
        if (response.getEvents() != null && !response.getEvents().isEmpty()) {
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
            throws TransactionBaseException, ContractCodecException, JniException,
                    ContractException {
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
        CallResponse response;
        if (useTransactionV1) {
            TransactionRequestWithStringParams request =
                    new TransactionRequestBuilder(abiAndBin.getAbi(), functionName, contractAddress)
                            .buildStringParamsRequest(callParams);
            response = assembleTransactionService.sendCall(request);
        } else {
            response =
                    assembleTransactionProcessor.sendCallWithSignWithStringParams(
                            cryptoKeyPair.getAddress(),
                            contractAddress,
                            abiAndBin.getAbi(),
                            functionName,
                            callParams);
        }
        ConsoleUtils.singleLine();
        System.out.println("Return code: " + response.getReturnCode());
        if (response.getReturnCode() == PrecompiledRetCode.CODE_SUCCESS.getCode()) {
            System.out.println("description: " + "transaction executed successfully");
            System.out.println("Return message: " + response.getReturnMessage());
            ConsoleUtils.singleLine();
            ConsoleUtils.printResults(
                    response.getReturnABIObject(),
                    response.getReturnObject(),
                    response.getResults());
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
        String abiStr = client.getABI(contractFileName).getABI();
        if (abiStr.isEmpty()) {
            if (consoleInitializer.getClient().isWASM()) {
                abiStr = getWasmAbi(consoleInitializer.getGroupID(), pwd, contractFileName);
            } else {
                abiStr = getSolidityAbi(contractFileName);
            }
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
        ABIDefinition constructorABIDefinition = contractABIDefinition.getConstructor();
        if (constructorABIDefinition != null) {
            constructorABIDefinition.setName("constructor");
            methodIDToFunctions.put(
                    ByteBuffer.wrap("constructor".getBytes()), constructorABIDefinition);
        }

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
    public void listDeployContractAddress(ConsoleInitializer consoleInitializer, String[] params)
            throws Exception {
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
            if (recordNum == Common.INVALID_RETURN_NUMBER) {
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
            if (!isWasm && !AddressUtils.isValidAddress(contractAddressFile.getName())) {
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

    @Override
    public void transfer(ConsoleInitializer consoleInitializer, String[] params) throws Exception {

        int negotiatedProtocol = consoleInitializer.getClient().getNegotiatedProtocol();
        if ((negotiatedProtocol >> 16) < 2) {
            throw new UnsupportedOperationException(
                    "The node version is too low, please upgrade the node to 3.6.0 or higher");
        }
        String address = params[1];
        String amount = params[2];

        Convert.Unit unit = Convert.Unit.WEI;
        if (params.length == 4) {
            String unitStr = params[3];
            unit = Convert.Unit.fromString(unitStr);
        }
        if (!AddressUtils.isValidAddress(address)) {
            System.out.println("Invalid contract address: " + address);
            return;
        }
        if (!ConsoleUtils.isValidNumber(amount)) {
            System.out.println("Invalid amount: " + amount);
            return;
        }
        BigDecimal value = new BigDecimal(amount);
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            System.out.println("Invalid amount: " + amount);
            return;
        }
        if (value.compareTo(BigDecimal.ZERO) == 0) {
            System.out.println("Amount is zero, no need to transfer.");
            return;
        }
        TransactionReceipt transactionReceipt =
                transferTransactionService.sendFunds(address, value, unit);
        System.out.println("transaction hash: " + transactionReceipt.getTransactionHash());
        ConsoleUtils.singleLine();
        System.out.println("transaction status: " + transactionReceipt.getStatus());

        ConsoleUtils.singleLine();
        if (transactionReceipt.getStatus() == 0) {
            System.out.println("description: transaction executed successfully");
            System.out.println(
                    transactionReceipt.getFrom() + " => " + address + ", " + amount + " " + unit);
        } else {
            Tuple2<Boolean, String> revertMessage =
                    RevertMessageParser.tryResolveRevertMessage(transactionReceipt);
            System.out.println(
                    "description: transfer transaction failed."
                            + (revertMessage.getValue1()
                                    ? " Reason: " + revertMessage.getValue2()
                                    : ""));
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

        List<SolidityCompiler.Option> defaultOptions = Arrays.asList(ABI, BIN, METADATA);
        List<SolidityCompiler.Option> options = new ArrayList<>(defaultOptions);

        logger.debug(
                "compileSolToBinAndAbi, solc version:{} ,basePath: {}",
                Version.V0_8_26,
                solFile.getParentFile().getCanonicalPath());
        SolidityCompiler.Option basePath =
                new SolidityCompiler.CustomOption(
                        "base-path", solFile.getParentFile().getCanonicalPath());
        options.add(basePath);
        String fileName = solFile.getName();
        String dir = solFile.getParentFile().getCanonicalPath() + File.separator;

        String mergedSource = mergeSource(dir, fileName, new HashSet<>());

        // compile ecdsa
        SolidityCompiler.Result res =
                SolidityCompiler.compile(
                        mergedSource.getBytes(StandardCharsets.UTF_8),
                        (client.getCryptoType() == CryptoType.SM_TYPE),
                        true,
                        Version.V0_8_26,
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
        String contractName = solFile.getName().split("\\.")[0];
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
