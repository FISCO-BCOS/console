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

import console.common.AbiAndBin;
import console.contract.exceptions.CompileContractException;
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
    public static final String ABI_PATH = "contracts/console/abi/";
    public static final String BIN_PATH = "contracts/console/bin/";
    public static final String SOL_POSTFIX = ".sol";

    public static String removeSolPostfix(String name) {
        return (name.endsWith(SOL_POSTFIX)
                ? name.substring(0, name.length() - SOL_POSTFIX.length())
                : name);
    }

    public static AbiAndBin compileContract(String name) throws CompileContractException {
        return dynamicCompileSolFilesToJava(removeSolPostfix(name));
    }

    public static AbiAndBin dynamicCompileSolFilesToJava(String contractFileName)
            throws CompileContractException {
        try {
            contractFileName =
                    (contractFileName.endsWith(SOL_POSTFIX)
                            ? contractFileName
                            : (contractFileName + SOL_POSTFIX));
            File contractFile = new File(SOLIDITY_PATH + "/" + contractFileName);
            if (!contractFile.exists()) {
                throw new CompileContractException(
                        "There is no "
                                + contractFileName
                                + " in the directory of "
                                + SOLIDITY_PATH);
            }
            return compileSolToBinAndAbi(contractFile, ABI_PATH, BIN_PATH);
        } catch (IOException e) {
            throw new CompileContractException(
                    "compile " + contractFileName + " failed, error info: " + e.getMessage(), e);
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
            AbiAndBin abiAndBin, String contractName, String contractAddress) throws IOException {
        String baseAbiPath = ABI_PATH + contractAddress + File.separator + contractName;
        String baseBinPath = BIN_PATH + contractAddress + File.separator + contractName;
        String baseSMBinPath =
                BIN_PATH + contractAddress + File.separator + "sm" + File.separator + contractName;
        FileUtils.writeStringToFile(new File(baseAbiPath + ".abi"), abiAndBin.getAbi());
        FileUtils.writeStringToFile(new File(baseBinPath + ".bin"), abiAndBin.getBin());
        FileUtils.writeStringToFile(new File(baseSMBinPath + ".bin"), abiAndBin.getSmBin());
    }

    public static AbiAndBin loadAbiAndBin(String contractName, String contractAddress)
            throws IOException, CodeGenException {
        String baseAbiPath = ABI_PATH + contractAddress + File.separator + contractName;
        String baseBinPath = BIN_PATH + contractAddress + File.separator + contractName;
        String baseSMBinPath =
                BIN_PATH + contractAddress + File.separator + "sm" + File.separator + contractName;
        String abiContent = new String(CodeGenUtils.readBytes(new File(baseAbiPath + ".abi")));
        String binContent = new String(CodeGenUtils.readBytes(new File(baseBinPath + ".bin")));
        String smBinContent = new String(CodeGenUtils.readBytes(new File(baseSMBinPath + ".bin")));
        return new AbiAndBin(abiContent, binContent, smBinContent);
    }
}
