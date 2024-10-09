/*
 * Copyright 2014-2020  [fisco-dev]
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package console.contract.utils;

import static org.fisco.solc.compiler.SolidityCompiler.Options.ABI;
import static org.fisco.solc.compiler.SolidityCompiler.Options.BIN;
import static org.fisco.solc.compiler.SolidityCompiler.Options.DEVDOC;
import static org.fisco.solc.compiler.SolidityCompiler.Options.METADATA;
import static org.fisco.solc.compiler.SolidityCompiler.Options.USERDOC;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ArrayNode;
import console.common.ConsoleUtils;
import console.contract.exceptions.CompileContractException;
import console.contract.model.AbiAndBin;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.fisco.bcos.codegen.v3.exceptions.CodeGenException;
import org.fisco.bcos.codegen.v3.utils.CodeGenUtils;
import org.fisco.bcos.sdk.v3.utils.ObjectMapperFactory;
import org.fisco.bcos.sdk.v3.utils.StringUtils;
import org.fisco.evm.analysis.EvmAnalyser;
import org.fisco.solc.compiler.CompilationResult;
import org.fisco.solc.compiler.SolidityCompiler;
import org.fisco.solc.compiler.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContractCompiler {

    private static final Logger logger = LoggerFactory.getLogger(ContractCompiler.class);

    public static final int OnlySM = 1;
    public static final int OnlyNonSM = 2;
    public static final int All = 3;

    public static final String SOLIDITY_PATH = "contracts/solidity/";
    public static final String LIQUID_PATH = "contracts/liquid/";
    public static final String COMPILED_PATH = "contracts/.compiled/";
    public static final String BFS_APPS_PREFIX = "/apps";
    public static final String BFS_SYS_PREFIX = "/sys";
    public static final String BFS_APPS_FULL_PREFIX = "/apps/";
    public static final String SOL_SUFFIX = ".sol";
    public static final String SM_SUFFIX = ".sm";
    public static final String BIN_SUFFIX = ".bin";
    public static final String ABI_SUFFIX = ".abi";
    public static final String WASM_SUFFIX = ".wasm";

    public static AbiAndBin compileContract(
            String contractNameOrPath,
            String specifyContractName,
            boolean sm,
            boolean isContractParallelAnalysis,
            Version version)
            throws CompileContractException {
        // if absolute path
        File contractFile = new File(contractNameOrPath);

        if (!contractFile.exists() || contractFile.isDirectory()) {
            // if absolute path without sol
            // try again with .sol suffix
            contractFile = new File(contractNameOrPath + SOL_SUFFIX);
            if (!contractFile.exists() || contractFile.isDirectory()) {
                // if relative path in contracts/
                // the contractName
                String contractFileName =
                        ConsoleUtils.removeSolSuffix(contractNameOrPath) + SOL_SUFFIX;
                contractFile = new File(SOLIDITY_PATH + File.separator + contractFileName);
                if (!contractFile.exists()) {
                    throw new CompileContractException(
                            "There is no "
                                    + contractFileName
                                    + " in the directory of "
                                    + SOLIDITY_PATH);
                }
            }
        }
        return dynamicCompileSolFilesToJava(
                contractFile, specifyContractName, sm, isContractParallelAnalysis, version);
    }

    public static AbiAndBin dynamicCompileSolFilesToJava(
            File contractFile,
            String specifyContractName,
            boolean sm,
            boolean isContractParallelAnalysis,
            Version version)
            throws CompileContractException {
        try {
            return compileSolToBinAndAbi(
                    contractFile,
                    COMPILED_PATH,
                    COMPILED_PATH,
                    sm ? OnlySM : OnlyNonSM,
                    null,
                    specifyContractName,
                    isContractParallelAnalysis,
                    version);
        } catch (IOException e) {
            throw new CompileContractException(
                    "compile " + contractFile.getName() + " failed, error info: " + e.getMessage(),
                    e);
        }
    }

    // compile with libraries option
    public static AbiAndBin compileSolToBinAndAbi(
            File contractFile,
            String abiDir,
            String binDir,
            int compileType,
            String librariesOption,
            String specifyContractName,
            boolean isContractParallelAnalysis,
            Version version)
            throws IOException, CompileContractException {
        if (compileType != All) {
            return compileSolToBinAndAbi(
                    contractFile,
                    abiDir,
                    binDir,
                    compileType == OnlySM,
                    librariesOption,
                    specifyContractName,
                    isContractParallelAnalysis,
                    version);
        }
        AbiAndBin abiAndBin =
                compileSolToBinAndAbi(
                        contractFile,
                        abiDir,
                        binDir,
                        false,
                        librariesOption,
                        specifyContractName,
                        isContractParallelAnalysis,
                        version);
        AbiAndBin abiAndBinSM =
                compileSolToBinAndAbi(
                        contractFile,
                        abiDir,
                        binDir,
                        true,
                        librariesOption,
                        specifyContractName,
                        isContractParallelAnalysis,
                        version);
        return new AbiAndBin(
                abiAndBin.getAbi(),
                abiAndBin.getBin(),
                abiAndBinSM.getSmBin(),
                abiAndBin.getDevdoc());
    }

    // compile with libraries option
    public static AbiAndBin compileSolToBinAndAbi(
            File contractFile,
            String abiDir,
            String binDir,
            boolean sm,
            String librariesOption,
            String specifyContractName,
            boolean isContractParallelAnalysis,
            Version version)
            throws IOException, CompileContractException {
        SolidityCompiler.Option libraryOption = null;
        if (librariesOption != null && !librariesOption.isEmpty()) {
            libraryOption = new SolidityCompiler.CustomOption("libraries", librariesOption);
        }

        String contractName = contractFile.getName().split("\\.")[0];
        if (!StringUtils.isEmpty(specifyContractName)) {
            contractName = specifyContractName;
        }
        List<SolidityCompiler.Option> defaultOptions =
                Arrays.asList(ABI, BIN, METADATA, USERDOC, DEVDOC);
        List<SolidityCompiler.Option> options = new ArrayList<>(defaultOptions);

        if (libraryOption != null) {
            options.add(libraryOption);
        }

        if (version.toString().compareToIgnoreCase(ConsoleUtils.COMPILE_WITH_BASE_PATH) >= 0) {
            logger.debug(
                    "compileSolToBinAndAbi, solc version:{} ,basePath: {}",
                    version,
                    contractFile.getParentFile().getCanonicalPath());
            SolidityCompiler.Option basePath =
                    new SolidityCompiler.CustomOption(
                            "base-path", contractFile.getParentFile().getCanonicalPath());
            options.add(basePath);
        } else {
            logger.debug("compileSolToBinAndAbi, solc version:{}", version);
        }

        String fileName = contractFile.getName();
        String dir = contractFile.getParentFile().getCanonicalPath() + File.separator;

        String mergedSource = mergeSource(dir, fileName, new HashSet<>());

        SolidityCompiler.Result res =
                SolidityCompiler.compile(
                        mergedSource.getBytes(StandardCharsets.UTF_8),
                        sm,
                        true,
                        version,
                        options.toArray(new SolidityCompiler.Option[0]));

        logger.debug(
                " solidity compiler result, sm: {}, success: {}, output: {}, error: {}",
                sm,
                !res.isFailed(),
                res.getOutput(),
                res.getErrors());
        if (res.isFailed() || res.getOutput().isEmpty()) {
            throw new CompileContractException(" Compile error: " + res.getErrors());
        }

        CompilationResult result = CompilationResult.parse(res.getOutput());
        CompilationResult.ContractMetadata meta = result.getContract(contractName);

        String bin = sm ? "" : meta.bin;
        String smBin = sm ? meta.bin : "";
        String abi = mergeAbi(contractName, result);
        AbiAndBin abiAndBin = new AbiAndBin(abi, bin, smBin, meta.devdoc);

        // evm static analysis
        File abiFile = new File(abiDir + contractName + ".abi");
        File binFile = new File(binDir + contractName + ".bin");
        String abiFilePath = abiFile.getAbsolutePath();
        String binFilePath = binFile.getAbsolutePath();
        FileUtils.writeStringToFile(abiFile, abiAndBin.getAbi());
        if (sm) {
            FileUtils.writeStringToFile(binFile, abiAndBin.getSmBin());
        } else {
            FileUtils.writeStringToFile(binFile, abiAndBin.getBin());
        }

        if (isContractParallelAnalysis) {
            EvmAnalyser.Result ret = EvmAnalyser.process(abiFilePath, binFilePath, sm);
            if (ret.isFailed()) {
                String error =
                        "*** Analysis evm bytecode "
                                + contractFile.getName()
                                + " failed *** \n error: "
                                + ret.getErrors();
                logger.debug(error);
                throw new CompileContractException(error);
            }
        }

        // write abi and re-read abi for add evm analysis result
        if (isContractParallelAnalysis) {
            abi = FileUtils.readFileToString(abiFile, StandardCharsets.UTF_8);
            abiAndBin.setAbi(abi);
        }
        checkBinaryCode(contractName, meta.bin);
        return abiAndBin;
    }

    public static String mergeSource(String currentDir, String sourceFile, Set<String> dependencies)
            throws IOException {
        StringBuilder sourceBuffer = new StringBuilder();

        String fullPath = currentDir + sourceFile;
        String dir = fullPath.substring(0, fullPath.lastIndexOf(File.separator)) + File.separator;

        File sourceResource = new File(fullPath);

        if (!sourceResource.exists()) {
            throw new IOException("Source file:" + fullPath + " not found");
        }

        Pattern simpleImport = Pattern.compile("^\\s*import\\s+[\"'](.+)[\"']\\s*;\\s*$");
        Pattern asImport = Pattern.compile("^\\s*import\\s+[\"'](.+)[\"']\\s*as\\s*(.+);\\s*$");
        Pattern fromImport =
                Pattern.compile("^\\s*import\\s+[\\w{}]*\\s+from\\s+['\"](.+)['\"];\\s*$");
        try (Scanner scanner = new Scanner(sourceResource, "UTF-8")) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.contains("pragma experimental ABIEncoderV2;")) {
                    if (!dependencies.contains("pragma experimental ABIEncoderV2;")) {
                        dependencies.add("pragma experimental ABIEncoderV2;");
                        sourceBuffer.append(line);
                        sourceBuffer.append(System.lineSeparator());
                    }
                    continue;
                }

                // skip SPDX-License-Identifier
                if (line.contains("SPDX-License-Identifier")) {
                    continue;
                }

                Matcher simpleMatcher = simpleImport.matcher(line);
                Matcher fromMatcher = fromImport.matcher(line);
                Matcher asMatcher = asImport.matcher(line);
                boolean simpleFlag = simpleMatcher.find();
                boolean fromFlag = fromMatcher.find();
                boolean asFlag = asMatcher.find();
                if (simpleFlag || fromFlag || asFlag) {
                    String depSourcePath;
                    if (simpleFlag) {
                        depSourcePath = simpleMatcher.group(1);
                    } else if (fromFlag) {
                        depSourcePath = fromMatcher.group(1);
                    } else {
                        depSourcePath = asMatcher.group(1);
                    }
                    String nextPath = dir + depSourcePath;
                    if (nextPath.contains("./")) {
                        nextPath = new File(nextPath).getCanonicalPath();
                    }
                    if (!dependencies.contains(nextPath)) {
                        dependencies.add(nextPath);
                        sourceBuffer.append(mergeSource(dir, depSourcePath, dependencies));
                    }
                } else {
                    sourceBuffer.append(line);
                    sourceBuffer.append(System.lineSeparator());
                }
            }
        }

        return sourceBuffer.toString();
    }

    public static String mergeAbi(String mainContract, CompilationResult result)
            throws JsonProcessingException {

        List<String> contractNames = result.getContractKeys();
        if (contractNames.isEmpty()) {
            return null;
        }
        ObjectReader objectReader = ObjectMapperFactory.getObjectReader();
        ArrayNode mainNode = (ArrayNode) objectReader.createArrayNode();
        CompilationResult.ContractMetadata main = result.getContract(mainContract);
        mainNode.addAll((ArrayNode) objectReader.readTree(main.abi));

        for (String contractName : contractNames) {
            String key = contractName.substring(contractName.lastIndexOf(':') + 1);
            if (key.equals(mainContract)) {
                continue;
            }

            CompilationResult.ContractMetadata contract = result.getContract(key);
            JsonNode jsonNode = objectReader.readTree(contract.abi);
            if (jsonNode.isArray() && !jsonNode.isEmpty()) {
                ArrayNode arrayNode = (ArrayNode) jsonNode;
                for (JsonNode node : arrayNode) {
                    if (node.has("type") && node.get("type").asText().equals("constructor")) {
                    } else {
                        mainNode.add(node);
                    }
                }
            }
        }
        return mainNode.toString();
    }

    public static void checkBinaryCode(String contractName, String binary)
            throws CompileContractException {
        String externalLibSplitter = "_";
        if (binary.contains(externalLibSplitter)) {
            String errorMessage =
                    "Compile binary for "
                            + contractName
                            + " failed, The address of the library must be manually set.\n";
            errorMessage +=
                    "If you use the sol2java.sh script, please deploy the library to the blockchain first, and use the -l option to set the contract address of the dependent library\n";
            throw new CompileContractException(errorMessage);
        }
    }

    public static void saveAbiAndBin(
            String groupId, AbiAndBin abiAndBin, String contractName, String contractAddress)
            throws IOException {
        File saveDir =
                new File(
                        COMPILED_PATH
                                + File.separator
                                + groupId
                                + File.separator
                                + contractName
                                + File.separator
                                + contractAddress);
        if (saveDir.exists()) {
            FileUtils.deleteQuietly(saveDir);
        }
        File abiPath =
                new File(saveDir.getAbsolutePath() + File.separator + contractName + ABI_SUFFIX);
        File binPath =
                new File(saveDir.getAbsolutePath() + File.separator + contractName + BIN_SUFFIX);
        File smBinPath =
                new File(
                        saveDir.getAbsolutePath()
                                + File.separator
                                + contractName
                                + SM_SUFFIX
                                + BIN_SUFFIX);

        if (Objects.nonNull(abiAndBin.getAbi()) && !abiAndBin.getAbi().isEmpty()) {
            FileUtils.writeStringToFile(abiPath, abiAndBin.getAbi());
        }

        if (Objects.nonNull(abiAndBin.getBin()) && !abiAndBin.getBin().isEmpty()) {
            FileUtils.writeStringToFile(binPath, abiAndBin.getBin());
        }

        if (Objects.nonNull(abiAndBin.getSmBin()) && !abiAndBin.getSmBin().isEmpty()) {
            FileUtils.writeStringToFile(smBinPath, abiAndBin.getSmBin());
        }
    }

    public static AbiAndBin loadAbi(
            String groupId, String contractNameOrPath, String contractAddress, boolean isPath)
            throws IOException, CodeGenException {
        String contractName =
                isPath ? ConsoleUtils.getContractName(contractNameOrPath) : contractNameOrPath;
        return loadAbi(groupId, contractName, contractAddress);
    }

    public static AbiAndBin loadAbi(String groupId, String contractName, String contractAddress)
            throws IOException, CodeGenException {
        File abiPath =
                new File(
                        COMPILED_PATH
                                + File.separator
                                + groupId
                                + File.separator
                                + contractName
                                + File.separator
                                + contractAddress
                                + File.separator
                                + contractName
                                + ABI_SUFFIX);

        if (!abiPath.exists()) {
            return new AbiAndBin();
        }

        String abiContent = new String(CodeGenUtils.readBytes(abiPath));
        return new AbiAndBin(abiContent, "", "", null);
    }

    public static AbiAndBin loadAbiAndBin(
            String groupId, String contractName, String contractAddress, boolean sm)
            throws CodeGenException, IOException {
        File abiPath =
                new File(
                        COMPILED_PATH
                                + File.separator
                                + groupId
                                + File.separator
                                + contractName
                                + File.separator
                                + contractAddress
                                + File.separator
                                + contractName
                                + ABI_SUFFIX);
        File binPath =
                new File(
                        COMPILED_PATH
                                + File.separator
                                + groupId
                                + File.separator
                                + contractName
                                + File.separator
                                + contractAddress
                                + File.separator
                                + contractName
                                + (sm ? SM_SUFFIX : "")
                                + BIN_SUFFIX);

        if (!abiPath.exists() || !binPath.exists()) {
            return new AbiAndBin();
        }
        String abiContent = new String(CodeGenUtils.readBytes(abiPath));
        String binContent = new String(CodeGenUtils.readBytes(binPath));
        if (logger.isTraceEnabled()) {
            logger.trace(
                    "loadAbiAndBin load abi and bin, contract: {}, abiPath: {}, binPath: {}",
                    contractAddress,
                    abiPath,
                    binPath);
        }

        return new AbiAndBin(abiContent, sm ? "" : binContent, sm ? binContent : "", null);
    }
}
