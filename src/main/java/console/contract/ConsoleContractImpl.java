package console.contract;

import console.common.Address;
import console.common.Common;
import console.common.ConsoleUtils;
import console.contract.exceptions.CompileContractException;
import console.contract.model.AbiAndBin;
import console.contract.utils.ContractCompiler;
import console.exception.ConsoleMessageException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.fisco.bcos.sdk.abi.ABICodec;
import org.fisco.bcos.sdk.abi.ABICodecException;
import org.fisco.bcos.sdk.abi.wrapper.ABIDefinition;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.client.exceptions.ClientException;
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
        this.abiCodec = new ABICodec(client.getCryptoSuite());
    }

    @Override
    public void deploy(String[] params) throws ConsoleMessageException {
        String contractName = params[1];
        List<String> inputParams = Arrays.asList(params).subList(1, params.length - 1);
        deployContract(contractName, inputParams);
    }

    public TransactionResponse deployContract(String contractName, List<String> inputParams)
            throws ConsoleMessageException {
        try {
            AbiAndBin abiAndBin = ContractCompiler.compileContract(contractName);
            String bin = abiAndBin.getBin();
            if (client.getCryptoSuite().getCryptoTypeConfig() == CryptoType.SM_TYPE) {
                bin = abiAndBin.getSmBin();
            }
            TransactionResponse response =
                    this.assembleTransactionProcessor.deployAndGetResponseWithStringParams(
                            abiAndBin.getAbi(), bin, inputParams);
            if (response.getReturnCode() != PrecompiledRetCode.CODE_SUCCESS.getCode()) {
                System.out.println("deploy contract for " + contractName + " failed!");
                System.out.println("return message: " + response.getReturnMessage());
                System.out.println("return code:" + response.getReturnCode());
                return response;
            }
            String contractAddress = response.getTransactionReceipt().getContractAddress();
            System.out.println("Hash: " + response.getTransactionReceipt().getTransactionHash());
            System.out.println("contract address: " + contractAddress);
            writeLog(contractName, contractAddress);
            // save the bin and abi
            ContractCompiler.saveAbiAndBin(
                    client.getGroupId(), abiAndBin, contractName, contractAddress);
            // save the keyPair
            client.getCryptoSuite().getCryptoKeyPair().storeKeyPairWithPemFormat();
            return response;
        } catch (ClientException | CompileContractException | IOException | ABICodecException e) {
            throw new ConsoleMessageException("deploy contract failed for " + e.getMessage(), e);
        }
    }

    private synchronized void writeLog(String contractName, String contractAddress) {
        contractName = ContractCompiler.removeSolPostfix(contractName);
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

    @Override
    public void call(String[] params) throws Exception {
        String contractName = params[1];
        String contractAddressStr = params[2];
        // check contract address
        Address contractAddress = ConsoleUtils.convertAddress(contractAddressStr);
        if (!contractAddress.isValid()) {
            System.out.println("Invalid contract address: " + contractAddressStr);
            return;
        }
        String functionName = params[3];
        // get callParams
        List<String> callParams = Arrays.asList(params).subList(4, params.length);
        callContract(contractName, contractAddress.getAddress(), functionName, callParams);
    }

    protected void callContract(
            String contractName,
            String contractAddress,
            String functionName,
            List<String> callParams)
            throws IOException, CodeGenException, ABICodecException, CompileContractException {
        try {
            // load bin and abi
            AbiAndBin abiAndBin =
                    ContractCompiler.loadAbiAndBin(
                            client.getGroupId(), contractName, contractAddress);
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
            if (abiDefinition != null && abiDefinition.isConstant()) {
                logger.debug(
                        "sendCall request, params: {}, contractAddress: {}, contractName: {}, functionName:{}, paramSize: {}",
                        callParams.toString(),
                        contractAddress,
                        contractName,
                        functionName,
                        callParams.size());
                CryptoKeyPair cryptoKeyPair = client.getCryptoSuite().getCryptoKeyPair();
                CallResponse response =
                        assembleTransactionProcessor.sendCallWithStringParams(
                                cryptoKeyPair.getAddress(),
                                contractAddress,
                                abiAndBin.getAbi(),
                                functionName,
                                callParams);
                if (response.getReturnCode() == PrecompiledRetCode.CODE_SUCCESS.getCode()) {
                    System.out.println("Return values: ");
                    ConsoleUtils.printJson(response.getValues());
                }
                System.out.println("Return message: " + response.getReturnMessage());
                System.out.println("Return code: " + response.getReturnCode());
            }
            // send transaction
            else {
                logger.trace(
                        "sendTransactionAndGetResponse request, params: {}, contractAddress: {}, contractName: {}, functionName: {}, paramSize:{},  abiDefinition: {}",
                        callParams.toString(),
                        contractAddress,
                        contractName,
                        functionName,
                        callParams.size(),
                        abiDefinition.toString());
                TransactionResponse response =
                        assembleTransactionProcessor.sendTransactionWithStringParamsAndGetResponse(
                                contractAddress, abiAndBin.getAbi(), functionName, callParams);
                System.out.println(
                        "hash: " + response.getTransactionReceipt().getTransactionHash());
                System.out.println("Receipt message: " + response.getReceiptMessages());
                System.out.println("Return message: " + response.getReturnMessage());
                if (response.getEvents() != null && !response.getEvents().equals("")) {
                    System.out.println("Event: " + response.getEvents());
                }
            }

        } catch (TransactionBaseException e) {
            System.out.println(
                    "call for " + contractName + " failed, contractAddress is " + contractAddress);
            if (e.getRetCode() != null) {
                ConsoleUtils.printJson(e.getRetCode().toString());
            } else {
                System.out.println(e.getMessage());
            }
        }
    }

    @Override
    public void deployByCNS(String[] params) throws ConsoleMessageException {
        try {
            String contractName = params[1];
            String contractVersion = params[2];
            contractName = ContractCompiler.removeSolPostfix(contractName);
            // query the the contractName and version has been registered or not
            List<CnsInfo> cnsInfos =
                    cnsService.selectByNameAndVersion(contractName, contractVersion);
            if (cnsInfos.size() > 0) {
                System.out.println(
                        "The version \""
                                + contractVersion
                                + "\" of contract \""
                                + contractName
                                + "\" already exists!");
                return;
            }
            List<String> inputParams = Arrays.asList(params).subList(3, params.length);
            TransactionResponse response = deployContract(contractName, inputParams);
            if (response.getReturnCode() != PrecompiledRetCode.CODE_SUCCESS.getCode()) {
                return;
            }
            String contractAddress = response.getContractAddress();
            AbiAndBin abiAndBin =
                    ContractCompiler.loadAbiAndBin(
                            client.getGroupId(), contractName, contractAddress);
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
        String contractName = contractNameAndVersion;
        String contractVersion = null;
        if (contractNameAndVersion.contains(":")) {
            String[] nameAndVersion = contractNameAndVersion.split(":");
            if (nameAndVersion.length == 2) {
                contractName = nameAndVersion[0].trim();
                contractVersion = nameAndVersion[1].trim();
            } else {
                System.out.println(
                        "Contract name and version has incorrect format. For example, contractName:contractVersion");
                return;
            }
        }
        logger.debug(
                "callByCNS, contractName: {}, contractVersion: {}, contractNameAndVersion: {}, cnsService: {}",
                contractName,
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
                List<CnsInfo> cnsInfos =
                        cnsService.selectByNameAndVersion(contractName, contractVersion);
                if (cnsInfos == null || cnsInfos.isEmpty()) {
                    System.out.println(
                            "Can't find \""
                                    + contractName
                                    + ":"
                                    + contractVersion
                                    + "\" information from the cns list! Please deploy it by cns firstly!\n");
                    return;
                }
                contractAddress = cnsInfos.get(0).getAddress();
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
            }
        } catch (ContractException | ClientException e) {
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
        }
        String functionName = params[2];
        List<String> inputParams = Arrays.asList(params).subList(3, params.length);
        callContract(contractName, contractAddress, functionName, inputParams);
    }
}