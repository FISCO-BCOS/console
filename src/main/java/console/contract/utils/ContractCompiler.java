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
import static org.fisco.solc.compiler.SolidityCompiler.Options.INTERFACE;
import static org.fisco.solc.compiler.SolidityCompiler.Options.METADATA;

import console.common.ConsoleUtils;
import console.contract.exceptions.CompileContractException;
import console.contract.model.AbiAndBin;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import org.apache.commons.io.FileUtils;
import org.fisco.bcos.sdk.codegen.CodeGenUtils;
import org.fisco.bcos.sdk.codegen.exceptions.CodeGenException;
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
    private static final String SM_SUFFIX = ".sm";
    private static final String BIN_SUFFIX = ".bin";
    private static final String ABI_SUFFIX = ".abi";
    private static final String WASM_SUFFIX = ".wasm";

    public static AbiAndBin compileContract(String contractNameOrPath, boolean sm)
            throws CompileContractException {
        File contractFile = new File(contractNameOrPath);
        // the contractPath
        if (contractFile.exists() && !contractFile.isDirectory()) {
            return dynamicCompileSolFilesToJava(contractFile, sm);
        }
        // the contractName
        String contractFileName = ConsoleUtils.removeSolPostfix(contractNameOrPath) + SOL_SUFFIX;
        contractFile = new File(SOLIDITY_PATH + "/" + contractFileName);
        if (!contractFile.exists()) {
            throw new CompileContractException(
                    "There is no " + contractFileName + " in the directory of " + SOLIDITY_PATH);
        }
        return dynamicCompileSolFilesToJava(contractFile, sm);
    }

    public static AbiAndBin dynamicCompileSolFilesToJava(File contractFile, boolean sm)
            throws CompileContractException {
        try {
            return compileSolToBinAndAbi(
                    contractFile, COMPILED_PATH, COMPILED_PATH, sm ? OnlySM : OnlyNonSM, null);
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
            String librariesOption)
            throws IOException, CompileContractException {
        if (compileType == OnlyNonSM) {
            return compileSolToBinAndAbi(contractFile, abiDir, binDir, false, librariesOption);
        } else if (compileType == OnlySM) {
            return compileSolToBinAndAbi(contractFile, abiDir, binDir, true, librariesOption);
        } else {
            AbiAndBin abiAndBin =
                    compileSolToBinAndAbi(contractFile, abiDir, binDir, false, librariesOption);
            AbiAndBin abiAndBinSM =
                    compileSolToBinAndAbi(contractFile, abiDir, binDir, true, librariesOption);
            return new AbiAndBin(abiAndBin.getAbi(), abiAndBin.getBin(), abiAndBinSM.getSmBin());
        }
    }

    // compile with libraries option
    public static AbiAndBin compileSolToBinAndAbi(
            File contractFile, String abiDir, String binDir, boolean sm, String librariesOption)
            throws IOException, CompileContractException {
        SolidityCompiler.CustomOption libraryOption = null;
        if (librariesOption != null && !librariesOption.equals("")) {
            libraryOption = new SolidityCompiler.CustomOption("libraries", librariesOption);
        }

        String contractName = contractFile.getName().split("\\.")[0];
        SolidityCompiler.Result res = null;
        if (libraryOption == null) {
            res = SolidityCompiler.compile(contractFile, sm, true, ABI, BIN, INTERFACE, METADATA);
        } else {
            res =
                    SolidityCompiler.compile(
                            contractFile, sm, true, ABI, BIN, INTERFACE, METADATA, libraryOption);
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
            String groupId, String contractNameOrPath, String contractAddress)
            throws IOException, CodeGenException {
        String contractName = ConsoleUtils.getContractName(contractNameOrPath);
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
            String groupId, String contractNameOrPath, String contractAddress, boolean sm)
            throws CompileContractException, IOException, CodeGenException {
        String contractName = ConsoleUtils.getContractName(contractNameOrPath);
        return loadAbiAndBin(groupId, contractName, contractNameOrPath, contractAddress, sm, true);
    }

    public static AbiAndBin loadAbiAndBin(
            String groupId,
            String contractName,
            String contractNameOrPath,
            String contractAddress,
            boolean sm)
            throws CompileContractException, IOException, CodeGenException {
        return loadAbiAndBin(groupId, contractName, contractNameOrPath, contractAddress, sm, true);
    }

    public static AbiAndBin loadAbiAndBin(
            String groupId,
            String contractName,
            String contractNameOrPath,
            String contractAddress,
            boolean sm,
            boolean needCompile)
            throws CompileContractException, CodeGenException, IOException {
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
            if (needCompile) {
                return ContractCompiler.compileContract(contractNameOrPath, sm);
            } else {
                return new AbiAndBin();
            }
        }

        String abiContent = new String(CodeGenUtils.readBytes(abiPath));
        String binContent = new String(CodeGenUtils.readBytes(binPath));
        logger.trace(
                "loadAbiAndBin load abi and bin, contract: {}, abiPath: {}, binPath: {}",
                contractAddress,
                abiPath,
                binPath);

        return new AbiAndBin(abiContent, sm ? "" : binContent, sm ? binContent : "");
    }
}
