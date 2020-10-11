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
import org.apache.commons.io.FileUtils;
import org.fisco.bcos.sdk.codegen.CodeGenUtils;
import org.fisco.bcos.sdk.codegen.exceptions.CodeGenException;
import org.fisco.solc.compiler.CompilationResult;
import org.fisco.solc.compiler.SolidityCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContractCompiler {
    private static final Logger logger = LoggerFactory.getLogger(ContractCompiler.class);

    public static final String SOLIDITY_PATH = "contracts/solidity/";
    public static final String COMPILED_PATH = "contracts/.compiled/";
    public static final String SOL_POSTFIX = ".sol";
    private static final String SM_POSTFIX = ".sm";
    private static final String BIN_POSTFIX = ".bin";
    private static final String ABI_POSTFIX = ".abi";

    public static AbiAndBin compileContract(String contractNameOrPath)
            throws CompileContractException {
        File contractFile = new File(contractNameOrPath);
        // the contractPath
        if (contractFile.exists()) {
            return dynamicCompileSolFilesToJava(contractFile);
        }
        // the contractName
        String contractFileName =
                (ConsoleUtils.removeSolPostfix(contractNameOrPath).endsWith(SOL_POSTFIX)
                        ? contractNameOrPath
                        : (contractNameOrPath + SOL_POSTFIX));
        contractFile = new File(SOLIDITY_PATH + "/" + contractFileName);
        if (!contractFile.exists()) {
            throw new CompileContractException(
                    "There is no " + contractFileName + " in the directory of " + SOLIDITY_PATH);
        }
        return dynamicCompileSolFilesToJava(contractFile);
    }

    public static AbiAndBin dynamicCompileSolFilesToJava(File contractFile)
            throws CompileContractException {
        try {
            return compileSolToBinAndAbi(contractFile, COMPILED_PATH, COMPILED_PATH);
        } catch (IOException e) {
            throw new CompileContractException(
                    "compile " + contractFile.getName() + " failed, error info: " + e.getMessage(),
                    e);
        }
    }

    public static AbiAndBin compileSolToBinAndAbi(File contractFile, String abiDir, String binDir)
            throws CompileContractException, IOException {
        if (!contractFile.getName().endsWith(".sol")) {
            throw new CompileContractException("invalid contractFile: " + contractFile.getName());
        }
        String contractName = contractFile.getName().split("\\.")[0];

        /** ecdsa compile */
        SolidityCompiler.Result res =
                SolidityCompiler.compile(contractFile, false, true, ABI, BIN, INTERFACE, METADATA);
        logger.debug(
                " solidity compiler result, success: {}, output: {}, error: {}",
                !res.isFailed(),
                res.getOutput(),
                res.getErrors());
        if (res.isFailed() || "".equals(res.getOutput())) {
            throw new CompileContractException(" Compile error: " + res.getErrors());
        }

        /** sm compile */
        SolidityCompiler.Result smRes =
                SolidityCompiler.compile(contractFile, true, true, ABI, BIN, INTERFACE, METADATA);
        logger.debug(
                " sm solidity compiler result, success: {}, output: {}, error: {}",
                !smRes.isFailed(),
                smRes.getOutput(),
                smRes.getErrors());
        if (smRes.isFailed() || "".equals(smRes.getOutput())) {
            throw new CompileContractException(" Compile SM error: " + res.getErrors());
        }

        CompilationResult result = CompilationResult.parse(res.getOutput());
        CompilationResult smResult = CompilationResult.parse(smRes.getOutput());

        CompilationResult.ContractMetadata meta = result.getContract(contractName);
        CompilationResult.ContractMetadata smMeta = smResult.getContract(contractName);
        return new AbiAndBin(meta.abi, meta.bin, smMeta.bin);
    }

    public static void saveAbiAndBin(
            Integer groupId, AbiAndBin abiAndBin, String contractName, String contractAddress)
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
                                + ABI_POSTFIX);
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
                                + BIN_POSTFIX);
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
                                + SM_POSTFIX
                                + BIN_POSTFIX);
        FileUtils.writeStringToFile(abiPath, abiAndBin.getAbi());
        FileUtils.writeStringToFile(binPath, abiAndBin.getBin());
        FileUtils.writeStringToFile(smBinPath, abiAndBin.getSmBin());
    }

    public static AbiAndBin loadAbiAndBin(
            Integer groupId, String contractNameOrPath, String contractAddress)
            throws CompileContractException, IOException, CodeGenException {
        String contractName = ConsoleUtils.getContractName(contractNameOrPath);
        return loadAbiAndBin(groupId, contractName, contractNameOrPath, contractAddress, true);
    }

    public static AbiAndBin loadAbiAndBin(
            Integer groupId, String contractName, String contractNameOrPath, String contractAddress)
            throws CompileContractException, IOException, CodeGenException {
        return loadAbiAndBin(groupId, contractName, contractNameOrPath, contractAddress, true);
    }

    public static AbiAndBin loadAbiAndBin(
            Integer groupId,
            String contractName,
            String contractNameOrPath,
            String contractAddress,
            boolean needCompile)
            throws IOException, CodeGenException, CompileContractException {
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
                                + ABI_POSTFIX);
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
                                + BIN_POSTFIX);
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
                                + SM_POSTFIX
                                + BIN_POSTFIX);
        if (!abiPath.exists() || !binPath.exists() || !smBinPath.exists()) {
            if (needCompile) {
                AbiAndBin abiAndBin = ContractCompiler.compileContract(contractNameOrPath);
                ContractCompiler.saveAbiAndBin(groupId, abiAndBin, contractName, contractAddress);
            } else {
                return new AbiAndBin();
            }
        }
        String abiContent = new String(CodeGenUtils.readBytes(abiPath));
        String binContent = new String(CodeGenUtils.readBytes(binPath));
        String smBinContent = new String(CodeGenUtils.readBytes(smBinPath));
        return new AbiAndBin(abiContent, binContent, smBinContent);
    }
}
