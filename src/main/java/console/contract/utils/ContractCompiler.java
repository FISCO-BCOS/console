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
import static org.fisco.solc.compiler.SolidityCompiler.Options.METADATA;

import console.common.ConsoleUtils;
import console.contract.exceptions.CompileContractException;
import console.contract.model.AbiAndBin;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import org.apache.commons.io.FileUtils;
import org.fisco.bcos.sdk.v3.codegen.CodeGenUtils;
import org.fisco.bcos.sdk.v3.codegen.exceptions.CodeGenException;
import org.fisco.evm.analysis.EvmAnalyser;
import org.fisco.solc.compiler.CompilationResult;
import org.fisco.solc.compiler.SolidityCompiler;
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
    public static final String BFS_APPS_FULL_PREFIX = "/apps/";
    public static final String SOL_SUFFIX = ".sol";
    public static final String SM_SUFFIX = ".sm";
    public static final String BIN_SUFFIX = ".bin";
    public static final String ABI_SUFFIX = ".abi";
    public static final String WASM_SUFFIX = ".wasm";

    public static AbiAndBin compileContract(
            String contractNameOrPath, boolean sm, boolean isContractParallelAnalysis)
            throws CompileContractException {
        File contractFile = new File(contractNameOrPath);
        // the contractPath
        if (contractFile.exists() && !contractFile.isDirectory()) {
            return dynamicCompileSolFilesToJava(contractFile, sm, isContractParallelAnalysis);
        }
        // the contractName
        String contractFileName = ConsoleUtils.removeSolSuffix(contractNameOrPath) + SOL_SUFFIX;
        contractFile = new File(SOLIDITY_PATH + "/" + contractFileName);
        if (!contractFile.exists()) {
            throw new CompileContractException(
                    "There is no " + contractFileName + " in the directory of " + SOLIDITY_PATH);
        }
        return dynamicCompileSolFilesToJava(contractFile, sm, isContractParallelAnalysis);
    }

    public static AbiAndBin dynamicCompileSolFilesToJava(
            File contractFile, boolean sm, boolean isContractParallelAnalysis)
            throws CompileContractException {
        try {
            return compileSolToBinAndAbi(
                    contractFile,
                    COMPILED_PATH,
                    COMPILED_PATH,
                    sm ? OnlySM : OnlyNonSM,
                    null,
                    isContractParallelAnalysis);
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
            boolean isContractParallelAnalysis)
            throws IOException, CompileContractException {
        if (compileType == OnlyNonSM) {
            return compileSolToBinAndAbi(
                    contractFile,
                    abiDir,
                    binDir,
                    false,
                    librariesOption,
                    isContractParallelAnalysis);
        } else if (compileType == OnlySM) {
            return compileSolToBinAndAbi(
                    contractFile,
                    abiDir,
                    binDir,
                    true,
                    librariesOption,
                    isContractParallelAnalysis);
        } else {
            AbiAndBin abiAndBin =
                    compileSolToBinAndAbi(
                            contractFile,
                            abiDir,
                            binDir,
                            false,
                            librariesOption,
                            isContractParallelAnalysis);
            AbiAndBin abiAndBinSM =
                    compileSolToBinAndAbi(
                            contractFile,
                            abiDir,
                            binDir,
                            true,
                            librariesOption,
                            isContractParallelAnalysis);
            return new AbiAndBin(abiAndBin.getAbi(), abiAndBin.getBin(), abiAndBinSM.getSmBin());
        }
    }

    // compile with libraries option
    public static AbiAndBin compileSolToBinAndAbi(
            File contractFile,
            String abiDir,
            String binDir,
            boolean sm,
            String librariesOption,
            boolean isContractParallelAnalysis)
            throws IOException, CompileContractException {
        SolidityCompiler.CustomOption libraryOption = null;
        if (librariesOption != null && !librariesOption.equals("")) {
            libraryOption = new SolidityCompiler.CustomOption("libraries", librariesOption);
        }

        String contractName = contractFile.getName().split("\\.")[0];
        SolidityCompiler.Result res = null;
        if (libraryOption == null) {
            res = SolidityCompiler.compile(contractFile, sm, true, ABI, BIN, METADATA);
        } else {
            res =
                    SolidityCompiler.compile(
                            contractFile, sm, true, ABI, BIN, METADATA, libraryOption);
        }

        logger.debug(
                " solidity compiler result, sm: {}, success: {}, output: {}, error: {}",
                sm,
                !res.isFailed(),
                res.getOutput(),
                res.getErrors());
        if (res.isFailed() || "".equals(res.getOutput())) {
            throw new CompileContractException(" Compile error: " + res.getErrors());
        }

        CompilationResult result = CompilationResult.parse(res.getOutput());
        CompilationResult.ContractMetadata meta = result.getContract(contractName);

        String bin = sm ? "" : meta.bin;
        String smBin = sm ? meta.bin : "";
        AbiAndBin abiAndBin = new AbiAndBin(meta.abi, bin, smBin);

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

        String abi = FileUtils.readFileToString(abiFile, StandardCharsets.UTF_8);
        abiAndBin.setAbi(abi);
        checkBinaryCode(contractName, meta.bin);
        return abiAndBin;
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
                                + BIN_SUFFIX);
        File smBinPath =
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
        return new AbiAndBin(abiContent, "", "");
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

        return new AbiAndBin(abiContent, sm ? "" : binContent, sm ? binContent : "");
    }
}
