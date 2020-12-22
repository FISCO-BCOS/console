package console.common;

import static org.fisco.solc.compiler.SolidityCompiler.Options.ABI;
import static org.fisco.solc.compiler.SolidityCompiler.Options.BIN;
import static org.fisco.solc.compiler.SolidityCompiler.Options.INTERFACE;
import static org.fisco.solc.compiler.SolidityCompiler.Options.METADATA;

import console.exception.CompileSolidityException;
import console.exception.ConsoleMessageException;
import io.netty.util.NetUtil;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.commons.io.FileUtils;
import org.fisco.bcos.sdk.channel.model.ChannelPrococolExceiption;
import org.fisco.bcos.sdk.channel.model.EnumNodeVersion;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.codegen.CodeGenMain;
import org.fisco.bcos.sdk.utils.Host;
import org.fisco.bcos.sdk.utils.Numeric;
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
    public static final String SOL_POSTFIX = ".sol";
    public static final String GM_ACCOUNT_POSTFIX = "_gm";
    public static final int ADDRESS_SIZE = 160;
    public static final int ADDRESS_LENGTH_IN_HEX = ADDRESS_SIZE >> 2;

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
                    sb.append("\\");
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
            return true;
        }
    }

    public static long processLong(String name, String number, long minValue, long maxValue) {
        try {
            long value = Long.parseLong(number);
            if (value < minValue || (maxValue > minValue && value > maxValue)) {
                System.out.println(
                        "Please provide \""
                                + name
                                + "\" by integer mode between "
                                + minValue
                                + " and "
                                + maxValue
                                + ".");
                return Common.InvalidLongValue;
            }
            return value;
        } catch (NumberFormatException e) {
            System.out.println("Invalid " + name + ": \"" + number + "\"!");
            System.out.println(
                    "Please provide "
                            + name
                            + " by integer mode, larger than "
                            + Long.MIN_VALUE
                            + " and smaller than "
                            + Long.MAX_VALUE
                            + ".");
            logger.debug("processLong for {} failed, error info: {}", name, e.getMessage());
            return Common.InvalidLongValue;
        }
    }

    public static int proccessNonNegativeNumber(String name, String intStr) {
        return proccessNonNegativeNumber(name, intStr, 0, Integer.MAX_VALUE);
    }

    public static int proccessNonNegativeNumber(
            String name, String intStr, Integer minValue, Integer maxValue) {
        int intParam = 0;
        try {
            intParam = Integer.parseInt(intStr);
            if (intParam < minValue || (maxValue > minValue && intParam > maxValue)) {
                System.out.println(
                        "Please provide \""
                                + name
                                + "\" by non-negative integer mode between "
                                + minValue
                                + " and "
                                + maxValue
                                + ".");
                return Common.InvalidReturnNumber;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid " + name + ": \"" + intStr + "\"!");
            System.out.println(
                    "Please provide "
                            + name
                            + " by non-negative integer mode, "
                            + Common.NonNegativeIntegerRange
                            + ".");
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
                " \t java -cp conf/:lib/*:apps/* console.common.ConsoleUtils [packageName] [solidityFilePath or solidityDirPath] [JavaCodeDirPath].");
        System.out.println(" \t Example: ");
        System.out.println(
                " \t\t java -cp conf/:lib/*:apps/* console.common.ConsoleUtils org.fisco.hello");
        System.out.println(
                " \t\t java -cp conf/:lib/*:apps/* console.common.ConsoleUtils org.fisco.hello ./fisco/HelloWorld.sol");
        System.out.println(
                " \t\t java -cp conf/:lib/*:apps/* console.common.ConsoleUtils org.fisco.hello /data/app/HelloWorld.sol /java");
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
        String solPathOrDir = SOLIDITY_PATH;
        String javaDir = JAVA_PATH;
        if (args.length > 1) {
            solPathOrDir = args[1];
            if (args.length > 2) {
                javaDir = args[2];
            }
        }
        String fullJavaDir = new File(javaDir).getAbsolutePath();

        File sol = new File(solPathOrDir);
        if (!sol.exists()) {
            System.out.println(sol.getAbsoluteFile() + " not exist ");
            System.exit(0);
        }
        File solFileList = new File(SOLIDITY_PATH);
        String tempDirPath = new File(JAVA_PATH).getAbsolutePath();
        try {
            if (sol.isFile()) { // input file
                compileSolToJava(fullJavaDir, pkgName, sol, ABI_PATH, BIN_PATH);
            } else { // input dir
                compileAllSolToJava(fullJavaDir, pkgName, sol, ABI_PATH, BIN_PATH);
            }
        } catch (IOException e) {
            System.out.print(e.getMessage());
            logger.error(" message: {}, e: {}", e.getMessage(), e);
        }
    }

    /**
     * @param javaDir
     * @param packageName
     * @param solFile
     * @param abiDir
     * @param binDir
     * @throws IOException
     */
    public static void compileSolToJava(
            String javaDir, String packageName, File solFile, String abiDir, String binDir)
            throws IOException {

        String contractName = solFile.getName().split("\\.")[0];

        /** ecdsa compile */
        System.out.println("*** Compile solidity " + solFile.getName() + "*** ");
        SolidityCompiler.Result res =
                SolidityCompiler.compile(solFile, false, true, ABI, BIN, INTERFACE, METADATA);
        logger.debug(
                " solidity compiler result, success: {}, output: {}, error: {}",
                !res.isFailed(),
                res.getOutput(),
                res.getErrors());
        if (res.isFailed() || "".equals(res.getOutput())) {
            logger.error(" compile {} failed, e: {}", solFile.getAbsolutePath(), res.getErrors());
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
            logger.error(
                    " compile sm {} failed, e: {}", solFile.getAbsolutePath(), res.getErrors());
            throw new CompileSolidityException(" Compile sm error: " + res.getErrors());
        }
        System.out.println("INFO: Compile for solidity " + solFile.getName() + " success.");

        CompilationResult result = CompilationResult.parse(res.getOutput());
        CompilationResult smResult = CompilationResult.parse(smRes.getOutput());

        CompilationResult.ContractMetadata meta = result.getContract(contractName);
        CompilationResult.ContractMetadata smMeta = smResult.getContract(contractName);

        FileUtils.writeStringToFile(new File(abiDir + contractName + ".abi"), meta.abi);
        FileUtils.writeStringToFile(new File(binDir + contractName + ".bin"), meta.bin);

        FileUtils.writeStringToFile(new File(abiDir + "/sm/" + contractName + ".abi"), smMeta.abi);
        FileUtils.writeStringToFile(new File(binDir + "/sm/" + contractName + ".bin"), smMeta.bin);

        String abiFile = abiDir + contractName + ".abi";
        String binFile = binDir + contractName + ".bin";
        String smBinFile = binDir + "/sm/" + contractName + ".bin";
        CodeGenMain.main(
                Arrays.asList(
                                "-a", abiFile,
                                "-b", binFile,
                                "-s", smBinFile,
                                "-p", packageName,
                                "-o", javaDir)
                        .toArray(new String[0]));
        System.out.println(
                "*** Convert solidity to java  for " + solFile.getName() + " success ***\n");
    }

    public static void compileAllSolToJava(
            String javaDir, String packageName, File solFileList, String abiDir, String binDir)
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

            if (solFile.getName().startsWith("Lib")) {
                continue;
            }
            try {
                compileSolToJava(javaDir, packageName, solFile, abiDir, binDir);
            } catch (Exception e) {
                System.out.println(
                        "ERROR:convert solidity to java for "
                                + solFile.getName()
                                + " failed, error info: "
                                + e.getMessage());
                System.out.println("ERROR stack: ");
                e.printStackTrace();
            }
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

    public static boolean checkEndPoint(String endPoint) {
        int index = endPoint.lastIndexOf(':');
        if (index == -1) {
            System.out.println("Invalid endpoint format, the endpoint format should be IP:Port");
            return false;
        }
        String IP = endPoint.substring(0, index);
        String port = endPoint.substring(index + 1);
        if (!(NetUtil.isValidIpV4Address(IP) || NetUtil.isValidIpV6Address(IP))) {
            System.out.println("Invalid IP " + IP);
            return false;
        }
        if (!Host.validPort(port)) {
            System.out.println("Invalid Port " + port);
            return false;
        }
        return true;
    }

    public static boolean checkVersion(
            String command, Client client, String enumMinSupportVersion, boolean printMessage) {
        try {
            EnumNodeVersion.Version minSupportVersion =
                    EnumNodeVersion.getClassVersion(enumMinSupportVersion);
            EnumNodeVersion.Version supportedVersion =
                    EnumNodeVersion.getClassVersion(
                            client.getClientNodeVersion().getNodeVersion().getSupportedVersion());
            String errorMessage =
                    "The fisco bcos node with supported_version lower than "
                            + minSupportVersion.toVersionString()
                            + " does not support the command "
                            + command
                            + " , current fisco-bcos supported_version: "
                            + supportedVersion.toVersionString();
            if (supportedVersion.getMajor() < minSupportVersion.getMajor()) {
                if (printMessage) {
                    System.out.println(errorMessage);
                    System.out.println();
                }
                return false;
            }
            if (supportedVersion.getMajor() == minSupportVersion.getMajor()
                    && supportedVersion.getMinor() < minSupportVersion.getMinor()) {
                if (printMessage) {
                    System.out.println(errorMessage);
                    System.out.println();
                }
                return false;
            }
            return true;
        } catch (ChannelPrococolExceiption e) {
            return true;
        }
    }

    public static void sortFiles(File[] files) {
        if (files == null || files.length <= 1) {
            return;
        }
        Arrays.sort(
                files,
                new Comparator<File>() {
                    @Override
                    public int compare(File o1, File o2) {
                        long diff = o1.lastModified() - o2.lastModified();
                        if (diff > 0) return -1;
                        else if (diff == 0) return 0;
                        else return 1;
                    }

                    public boolean equals(Object obj) {
                        return true;
                    }
                });
    }

    public static boolean isValidAddress(String address) {
        String addressNoPrefix = Numeric.cleanHexPrefix(address);
        return addressNoPrefix.length() == ADDRESS_LENGTH_IN_HEX;
    }

    public static String getFileCreationTime(File file) {
        if (file == null) {
            return null;
        }
        BasicFileAttributes attr = null;
        try {
            Path path = file.toPath();
            attr = Files.readAttributes(path, BasicFileAttributes.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Instant instant = attr.creationTime().toInstant();
        String format =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        .withZone(ZoneId.systemDefault())
                        .format(instant);
        return format;
    }

    public static String removeSolPostfix(String name) {
        return (name.endsWith(SOL_POSTFIX)
                ? name.substring(0, name.length() - SOL_POSTFIX.length())
                : name);
    }

    /**
     * @param solFileNameOrPath
     * @return
     */
    public static File getSolFile(String solFileNameOrPath, boolean checkExist)
            throws ConsoleMessageException {

        String filePath = solFileNameOrPath;
        File solFile = new File(filePath);
        if (solFile.exists()) {
            return solFile;
        }
        filePath = ConsoleUtils.removeSolPostfix(filePath);
        filePath += SOL_POSTFIX;
        /** Check that the file exists in the default directory first */
        solFile = new File(SOLIDITY_PATH + File.separator + filePath);
        /** file not exist */
        if (!solFile.exists() && checkExist) {
            throw new ConsoleMessageException(solFileNameOrPath + " does not exist ");
        }
        return solFile;
    }

    public static String resolveContractPath(String contractNameOrPath) {
        if (contractNameOrPath.startsWith("~/")) {
            return Paths.get(System.getProperty("user.home"))
                    .resolve(contractNameOrPath.substring(2))
                    .toString();
        }
        return contractNameOrPath;
    }

    public static String getContractName(String contractNameOrPath) throws ConsoleMessageException {
        File contractFile = ConsoleUtils.getSolFile(contractNameOrPath, true);
        return ConsoleUtils.removeSolPostfix(contractFile.getName());
    }

    public static String getContractNameWithoutCheckExists(String contractNameOrPath)
            throws ConsoleMessageException {
        File contractFile = ConsoleUtils.getSolFile(contractNameOrPath, false);
        return ConsoleUtils.removeSolPostfix(contractFile.getName());
    }
}
