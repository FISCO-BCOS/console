package console.common;

import static org.fisco.solc.compiler.SolidityCompiler.Options.ABI;
import static org.fisco.solc.compiler.SolidityCompiler.Options.BIN;
import static org.fisco.solc.compiler.SolidityCompiler.Options.INTERFACE;
import static org.fisco.solc.compiler.SolidityCompiler.Options.METADATA;

import console.exception.CompileSolidityException;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.commons.io.FileUtils;
import org.fisco.bcos.web3j.codegen.SolidityFunctionWrapperGenerator;
import org.fisco.solc.compiler.CompilationResult;
import org.fisco.solc.compiler.SolidityCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleUtils {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleUtils.class);

    public static final String SOLIDITY_PATH = "contracts/solidity/";
    public static final String JAVA_PATH = "contracts/sdk/java/";
    public static final String ABI_PATH = "contracts/sdk/abi/";
    public static final String BIN_PATH = "contracts/sdk/bin/";

    public static void printJson(String jsonStr) {
        System.out.println(formatJson(jsonStr));
    }

    public static String formatJson(String jsonStr) {
        if (null == jsonStr || "".equals(jsonStr)) return "";
        jsonStr = jsonStr.replace("\\n", "");
        StringBuilder sb = new StringBuilder();
        char last = '\0';
        char current = '\0';
        int indent = 0;
        boolean isInQuotationMarks = false;
        for (int i = 0; i < jsonStr.length(); i++) {
            last = current;
            current = jsonStr.charAt(i);
            switch (current) {
                case '"':
                    if (last != '\\') {
                        isInQuotationMarks = !isInQuotationMarks;
                    }
                    sb.append(current);
                    break;
                case '{':
                case '[':
                    sb.append(current);
                    if (!isInQuotationMarks) {
                        sb.append('\n');
                        indent++;
                        addIndentBlank(sb, indent);
                    }
                    break;
                case '}':
                case ']':
                    if (!isInQuotationMarks) {
                        sb.append('\n');
                        indent--;
                        addIndentBlank(sb, indent);
                    }
                    sb.append(current);
                    break;
                case ',':
                    sb.append(current);
                    if (last != '\\' && !isInQuotationMarks) {
                        sb.append('\n');
                        addIndentBlank(sb, indent);
                    }
                    break;
                case ' ':
                    if (',' != jsonStr.charAt(i - 1)) {
                        sb.append(current);
                    }
                    break;
                case '\\':
                    break;
                default:
                    if (!(current == " ".charAt(0))) sb.append(current);
            }
        }

        return sb.toString();
    }

    private static void addIndentBlank(StringBuilder sb, int indent) {
        for (int i = 0; i < indent; i++) {
            sb.append("    ");
        }
    }

    public static boolean isInvalidHash(String hash) {
        if (hash.matches("^0x[0-9a-fA-F]{64}$")) {
            return false;
        } else {
            System.out.println("Please provide a valid hash.");
            System.out.println();
            return true;
        }
    }

    public static int proccessNonNegativeNumber(String name, String intStr) {
        int intParam = 0;
        try {
            intParam = Integer.parseInt(intStr);
            if (intParam < 0) {
                System.out.println(
                        "Please provide "
                                + name
                                + " by non-negative integer mode, "
                                + Common.NonNegativeIntegerRange
                                + ".");
                System.out.println();
                return Common.InvalidReturnNumber;
            }
        } catch (NumberFormatException e) {
            System.out.println(
                    "Please provide "
                            + name
                            + " by non-negative integer mode, "
                            + Common.NonNegativeIntegerRange
                            + ".");
            System.out.println();
            return Common.InvalidReturnNumber;
        }
        return intParam;
    }

    public static Address convertAddress(String addressStr) {
        Address address = new Address();
        if (addressStr.length() > Address.ValidLen) {
            address.setValid(false);
            address.setAddress(addressStr);
        } else {
            address.setValid(true);
            if (addressStr.startsWith("0x")) {
                if (!addressStr.substring(2, addressStr.length()).matches("^[a-fA-F0-9]+$")) {
                    address.setValid(false);
                    address.setAddress(addressStr);
                } else {
                    if (addressStr.length() == Address.ValidLen) {
                        address.setAddress(addressStr);
                    } else {
                        getAddress(address, addressStr, Address.ValidLen);
                    }
                }

            } else {
                address.setValid(false);
                address.setAddress(addressStr);
            }
        }
        if (!address.isValid()) {
            System.out.println("Please provide a valid address.");
            System.out.println();
        }
        return address;
    }

    private static void getAddress(Address address, String addressStr, int length) {
        int len = length - addressStr.length();
        StringBuilder builderAddress = new StringBuilder();
        for (int i = 0; i < len; i++) {
            builderAddress.append("0");
        }
        String newAddessStr;
        if (length == Address.ValidLen) {
            newAddessStr =
                    "0x" + builderAddress.toString() + addressStr.substring(2, addressStr.length());
        } else {
            newAddessStr = "0x" + builderAddress.toString() + addressStr;
        }
        address.setAddress(newAddessStr);
    }

    public static void Usage() {
        System.out.println(" Usage:");
        System.out.println(
                " \t java -cp conf/:lib/*:apps/* console.common.ConsoleUtils packageName [solidityFilePath].");
        System.out.println(" \t Example: ");
        System.out.println(
                " \t\t java -cp conf/:lib/*:apps/* console.common.ConsoleUtils org.fisco.hello");
        System.out.println(
                " \t\t java -cp conf/:lib/*:apps/* console.common.ConsoleUtils org.fisco.hello /data/app/HelloWorld.sol");
        System.out.println(
                " \t\t java -cp conf/:lib/*:apps/* console.common.ConsoleUtils org.fisco.hello ./fisco/HelloWorld.sol");
        System.exit(0);
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please provide a package name.");
            return;
        } else if (args[0].equals("-h") || args[0].equals("--help")) {
            Usage();
        }

        String pkgName = args[0];
        String tempDirPath = new File(JAVA_PATH).getAbsolutePath();
        try {
            if (args.length > 1) {
                File solFile = new File(args[1]);
                compileSolToJava(tempDirPath, pkgName, solFile, ABI_PATH, BIN_PATH);
            } else {
                File solFileDir = new File(SOLIDITY_PATH);
                compileSolToJava("*", tempDirPath, pkgName, solFileDir, ABI_PATH, BIN_PATH);
            }

            System.out.println(
                    "\nCompile solidity contract files to java contract files successfully!");
        } catch (IOException e) {
            System.out.print(e.getMessage());
            logger.error(" message: {}, e: {}", e.getMessage(), e);
        }
    }

    /**
     * Compile the contract file and return the ABI file
     *
     * @param solFile
     * @return
     * @throws IOException
     */
    public static String compileSolForABI(File solFile) throws IOException {

        String contractName = solFile.getName().split("\\.")[0];

        /** ecdsa compile */
        SolidityCompiler.Result res = SolidityCompiler.compile(solFile, false, true, ABI);

        logger.debug(
                " solidity compiler, contract: {}, result: {}, output: {}, error: {}",
                contractName,
                !res.isFailed(),
                res.getOutput(),
                res.getErrors());

        if (res.isFailed() || "".equals(res.getOutput())) {
            throw new CompileSolidityException(" Compile error: " + res.getErrors());
        }

        CompilationResult result = CompilationResult.parse(res.getOutput());
        CompilationResult.ContractMetadata meta = result.getContract(contractName);

        return meta.abi;
    }

    /**
     * @param tempDirPath
     * @param packageName
     * @param solFile
     * @param abiDir
     * @param binDir
     * @throws IOException
     */
    public static void compileSolToJava(
            String tempDirPath, String packageName, File solFile, String abiDir, String binDir)
            throws IOException {

        String contractName = solFile.getName().split("\\.")[0];

        /** compile */
        SolidityCompiler.Result res =
                SolidityCompiler.compile(solFile, false, true, ABI, BIN, INTERFACE, METADATA);
        logger.debug(
                " solidity compiler, contract: {}, result: {}, output: {}, error: {}",
                contractName,
                !res.isFailed(),
                res.getOutput(),
                res.getErrors());

        if (res.isFailed() || "".equals(res.getOutput())) {
            throw new CompileSolidityException(" Compile error: " + res.getErrors());
        }

        /** sm compile */
        SolidityCompiler.Result smRes =
                SolidityCompiler.compile(solFile, true, true, ABI, BIN, INTERFACE, METADATA);
        logger.debug(
                " sm solidity compiler result, success: {}, output: {}, error: {}",
                !smRes.isFailed(),
                smRes.getOutput(),
                smRes.getErrors());
        if (smRes.isFailed() || "".equals(smRes.getOutput())) {
            throw new CompileSolidityException(" Compile SM error: " + res.getErrors());
        }

        CompilationResult result = CompilationResult.parse(res.getOutput());
        CompilationResult smResult = CompilationResult.parse(smRes.getOutput());

        CompilationResult.ContractMetadata meta = result.getContract(contractName);
        CompilationResult.ContractMetadata smMeta = smResult.getContract(contractName);

        FileUtils.writeStringToFile(new File(abiDir + contractName + ".abi"), meta.abi);
        FileUtils.writeStringToFile(new File(binDir + contractName + ".bin"), meta.bin);

        FileUtils.writeStringToFile(new File(abiDir + "/sm/" + contractName + ".abi"), smMeta.abi);
        FileUtils.writeStringToFile(new File(binDir + "/sm/" + contractName + ".bin"), smMeta.bin);

        String binFile;
        String abiFile;
        String smBinFile;
        String filename = contractName;
        abiFile = abiDir + filename + ".abi";
        binFile = binDir + filename + ".bin";
        smBinFile = binDir + "/sm/" + filename + ".bin";
        SolidityFunctionWrapperGenerator.main(
                Arrays.asList(
                                "-a", abiFile,
                                "-b", binFile,
                                "-s", smBinFile,
                                "-p", packageName,
                                "-o", tempDirPath)
                        .toArray(new String[0]));
    }

    public static void compileSolToJava(
            String solName,
            String tempDirPath,
            String packageName,
            File solFileList,
            String abiDir,
            String binDir)
            throws IOException {
        File[] solFiles = solFileList.listFiles();
        if (solFiles.length == 0) {
            System.out.println("The contracts directory is empty.");
            return;
        }
        for (File solFile : solFiles) {
            if (!solFile.getName().endsWith(".sol")) {
                continue;
            }
            if (!"*".equals(solName)) {
                if (!solFile.getName().equals(solName)) {
                    continue;
                }
                if (solFile.getName().startsWith("Lib")) {
                    throw new IOException("Don't deploy the library: " + solFile.getName());
                }
            } else {
                if (solFile.getName().startsWith("Lib")) {
                    continue;
                }
            }

            compileSolToJava(tempDirPath, packageName, solFile, abiDir, binDir);
        }
    }

    private static class CommandTokenizer extends StreamTokenizer {
        public CommandTokenizer(Reader r) {
            super(r);
            resetSyntax();
            // Invisible ASCII characters.
            whitespaceChars(0x00, 0x20);
            // All visible ASCII characters.
            wordChars(0x21, 0x7E);
            // Other UTF8 characters.
            wordChars(0xA0, 0xFF);
            // Uncomment this to allow comments in the command.
            // commentChar('/');
            // Allow both types of quoted strings, e.g. 'abc' and "abc".
            quoteChar('\'');
            quoteChar('"');
        }

        public void parseNumbers() {}
    }

    public static String[] tokenizeCommand(String command) throws Exception {
        // example: callByCNS HelloWorld.sol set"Hello" parse [callByCNS, HelloWorld.sol,
        // set"Hello"]
        List<String> tokens1 = new ArrayList<>();
        StringTokenizer stringTokenizer = new StringTokenizer(command, " ");
        while (stringTokenizer.hasMoreTokens()) {
            tokens1.add(stringTokenizer.nextToken());
        }
        // example: callByCNS HelloWorld.sol set"Hello" parse [callByCNS, HelloWorld.sol, set,
        // "Hello"]
        List<String> tokens2 = new ArrayList<>();
        StreamTokenizer tokenizer = new CommandTokenizer(new StringReader(command));
        int token = tokenizer.nextToken();
        while (token != StreamTokenizer.TT_EOF) {
            switch (token) {
                case StreamTokenizer.TT_EOL:
                    // Ignore \n character.
                    break;
                case StreamTokenizer.TT_WORD:
                    tokens2.add(tokenizer.sval);
                    break;
                case '\'':
                    // If the tailing ' is missing, it will add a tailing ' to it.
                    // E.g. 'abc -> 'abc'
                    tokens2.add(String.format("'%s'", tokenizer.sval));
                    break;
                case '"':
                    // If the tailing " is missing, it will add a tailing ' to it.
                    // E.g. "abc -> "abc"
                    tokens2.add(String.format("\"%s\"", tokenizer.sval));
                    break;
                default:
                    // Ignore all other unknown characters.
                    throw new RuntimeException("unexpected input tokens " + token);
            }
            token = tokenizer.nextToken();
        }
        return tokens1.size() <= tokens2.size()
                ? tokens1.toArray(new String[tokens1.size()])
                : tokens2.toArray(new String[tokens2.size()]);
    }

    public static void singleLine() {
        System.out.println(
                "---------------------------------------------------------------------------------------------");
    }

    public static void doubleLine() {
        System.out.println(
                "=============================================================================================");
    }
}
