package console.common;

import console.contract.exceptions.CompileContractException;
import console.contract.model.AbiAndBin;
import console.contract.utils.ContractCompiler;
import console.exception.ConsoleMessageException;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.fisco.bcos.codegen.CodeGenMain;
import org.fisco.bcos.sdk.v3.codec.datatypes.Array;
import org.fisco.bcos.sdk.v3.codec.datatypes.Bytes;
import org.fisco.bcos.sdk.v3.codec.datatypes.DynamicBytes;
import org.fisco.bcos.sdk.v3.codec.datatypes.StructType;
import org.fisco.bcos.sdk.v3.codec.datatypes.Type;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.v3.codec.wrapper.ABIObject;
import org.fisco.bcos.sdk.v3.codec.wrapper.ContractCodecTools;
import org.fisco.bcos.sdk.v3.utils.StringUtils;
import org.fisco.solc.compiler.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleUtils {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleUtils.class);

    public static final String SOLIDITY_PATH = "contracts/solidity/";
    public static final String LIQUID_PATH = "contracts/liquid/";
    public static final String JAVA_PATH = "contracts/sdk/java/";
    public static final String ABI_PATH = "contracts/sdk/abi/";
    public static final String BIN_PATH = "contracts/sdk/bin/";
    public static final String DOC_PATH = "contracts/sdk/doc/";
    public static final String SOL_SUFFIX = ".sol";
    public static final String WASM_SUFFIX = ".wasm";
    public static final String GM_ACCOUNT_SUFFIX = "_gm";
    public static final int ADDRESS_SIZE = 160;
    public static final int ADDRESS_LENGTH_IN_HEX = ADDRESS_SIZE >> 2;
    public static final String EMPTY_ADDRESS = "0x0000000000000000000000000000000000000000";
    public static final String COMPILE_WITH_BASE_PATH = "0.8";

    public static void printJson(String jsonStr) {
        System.out.println(formatJson(jsonStr));
    }

    public static String formatJson(String jsonStr) {
        if (null == jsonStr || jsonStr.isEmpty()) return "";
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

    public static boolean isValidNumber(String number) {
        return number.matches("^-?\\d+$") || number.matches("^0[xX][0-9a-fA-F]+$");
    }

    public static String[] fixedBfsParams(String[] params, String pwd) throws Exception {
        String[] fixedParams = new String[params.length];
        fixedParams[0] = params[0];
        for (int i = 1; i < params.length; i++) {
            fixedParams[i] = fixedBfsParam(params[i], pwd);
        }
        return fixedParams;
    }

    public static String fixedBfsParam(String param, String pwd) throws Exception {
        String fixedParam;
        String pathToFix;
        if (param.startsWith("/")) {
            // absolute path
            pathToFix = param;
        } else {
            // relative path
            if (param.startsWith("~")) {
                pathToFix = "/apps/" + param.substring(1);
            } else {
                pathToFix = pwd + ((pwd.equals("/")) ? "" : "/") + param;
            }
        }
        fixedParam = "/" + String.join("/", path2Level(pathToFix));
        return fixedParam;
    }

    public static List<String> path2Level(String absolutePath) throws Exception {
        Stack<String> pathStack = new Stack<>();
        for (String s : absolutePath.split("/")) {
            if (s.isEmpty() || s.equals(".")) {
                continue;
            }
            if (s.equals("..")) {
                if (!pathStack.isEmpty()) {
                    pathStack.pop();
                }
                continue;
            }
            if (!s.matches("^[0-9a-zA-Z][^\\>\\<\\*\\?\\/\\=\\+\\(\\)\\$\\\"\\']*$")) {
                throw new Exception("path is invalid: " + absolutePath);
            }
            pathStack.push(s);
        }
        return new ArrayList<>(pathStack);
    }

    public static Tuple2<String, String> getParentPathAndBaseName(String path) throws Exception {
        if (path.equals("/")) return new Tuple2<>("/", "/");
        List<String> path2Level = path2Level(path);
        if (path2Level.isEmpty()) {
            throw new Exception("path is invalid: " + path);
        }
        String baseName = path2Level.get(path2Level.size() - 1);
        String parentPath = '/' + String.join("/", path2Level.subList(0, path2Level.size() - 1));
        return new Tuple2<>(parentPath, baseName);
    }

    public static String prettyPwd(String pwd) {
        // pwd is formatted
        try {
            List<String> path2Level = path2Level(pwd);
            if (path2Level.size() > 3) {
                return String.join(
                        "/", path2Level.subList(path2Level.size() - 3, path2Level.size()));
            }
        } catch (Exception e) {
            return pwd;
        }
        return pwd;
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
                return Common.INVALID_LONG_VALUE;
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
            return Common.INVALID_LONG_VALUE;
        }
    }

    public static int processNonNegativeNumber(String name, String intStr) {
        return processNonNegativeNumber(name, intStr, 0, Integer.MAX_VALUE);
    }

    public static int processNonNegativeNumber(
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
                return Common.INVALID_RETURN_NUMBER;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid " + name + ": \"" + intStr + "\"!");
            System.out.println(
                    "Please provide \""
                            + name
                            + "\" by non-negative integer mode between "
                            + minValue
                            + " and "
                            + maxValue
                            + ".");
            return Common.INVALID_RETURN_NUMBER;
        }
        return intParam;
    }

    public static BigInteger processNonNegativeBigNumber(
            String name, String intStr, BigInteger minValue, BigInteger maxValue)
            throws ConsoleMessageException {
        BigInteger intParam;
        try {
            intParam = new BigInteger(intStr);
            if (intParam.compareTo(minValue) < 0 || intParam.compareTo(maxValue) > 0) {
                throw new ConsoleMessageException(
                        "Please provide \""
                                + name
                                + "\" by non-negative big int mode between "
                                + minValue
                                + " and "
                                + maxValue
                                + ".");
            }
        } catch (NumberFormatException e) {
            throw new ConsoleMessageException("Invalid " + name + ": \"" + intStr + "\"!");
        }
        return intParam;
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
            String javaDir,
            String packageName,
            File solFile,
            String abiDir,
            String binDir,
            String docDir,
            String librariesOption,
            String specifyContract,
            boolean isContractParallelAnalysis,
            boolean enableAsyncCall,
            String transactionVersion,
            Version version)
            throws IOException, CompileContractException {

        String contractName = solFile.getName().split("\\.")[0];

        /** ecdsa compile */
        System.out.println("*** Compile solidity " + solFile.getName() + "*** ");
        AbiAndBin abiAndBin =
                ContractCompiler.compileSolToBinAndAbi(
                        solFile,
                        abiDir,
                        binDir,
                        ContractCompiler.All,
                        librariesOption,
                        specifyContract,
                        isContractParallelAnalysis,
                        version);
        System.out.println("INFO: Compile for solidity " + solFile.getName() + " success.");
        File abiFile = new File(abiDir + contractName + ".abi");
        File binFile = new File(binDir + contractName + ".bin");
        String abiFilePath = abiFile.getAbsolutePath();
        String binFilePath = binFile.getAbsolutePath();
        FileUtils.writeStringToFile(abiFile, abiAndBin.getAbi());
        FileUtils.writeStringToFile(binFile, abiAndBin.getBin());

        File smBinFile = new File(binDir + "/sm/" + contractName + ".bin");
        File smAbiFile = new File(abiDir + "/sm/" + contractName + ".abi");
        String smBinFilePath = smBinFile.getAbsolutePath();
        FileUtils.writeStringToFile(smAbiFile, abiAndBin.getAbi());
        FileUtils.writeStringToFile(smBinFile, abiAndBin.getSmBin());

        File devdocFile = new File(docDir + contractName + ".devdoc");
        if (!StringUtils.isEmpty(abiAndBin.getDevdoc())) {
            FileUtils.writeStringToFile(devdocFile, abiAndBin.getDevdoc());
        }

        List<String> args =
                new ArrayList<>(
                        Arrays.asList(
                                "-v", "V3",
                                "-a", abiFilePath,
                                "-b", binFilePath,
                                "-s", smBinFilePath,
                                "-d", devdocFile.getAbsolutePath(),
                                "-p", packageName,
                                "-o", javaDir));
        if (enableAsyncCall) {
            args.add("-e");
        }
        if (!transactionVersion.equals("V0")) {
            args.add("-t");
            args.add(transactionVersion);
        }
        CodeGenMain.main(args.toArray(new String[0]));
        System.out.println(
                "*** Convert solidity to java  for " + solFile.getName() + " success ***\n");
    }

    public static void compileAllSolToJava(
            String javaDir,
            String packageName,
            File solFileList,
            String abiDir,
            String binDir,
            String docDir,
            boolean isContractParallelAnalysis,
            boolean enableAsyncCall,
            String transactionVersion,
            Version version)
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
                compileSolToJava(
                        javaDir,
                        packageName,
                        solFile,
                        abiDir,
                        binDir,
                        docDir,
                        null,
                        null,
                        isContractParallelAnalysis,
                        enableAsyncCall,
                        transactionVersion,
                        version);
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

        @Override
        public void parseNumbers() {}
    }

    public static String[] tokenizeCommand(String command) throws Exception {
        // example: call HelloWorld.sol set "Hello" parse [call, HelloWorld.sol,
        // set"Hello"]
        List<String> commandWords1 = new ArrayList<>();
        StringTokenizer stringTokenizer = new StringTokenizer(command, " ");
        while (stringTokenizer.hasMoreTokens()) {
            commandWords1.add(stringTokenizer.nextToken());
        }
        // example: call HelloWorld.sol set "Hello" parse [call, HelloWorld.sol, set,
        // "Hello"]
        List<String> commandWords2 = new ArrayList<>();
        StreamTokenizer tokenizer = new CommandTokenizer(new StringReader(command));
        int token = tokenizer.nextToken();
        while (token != StreamTokenizer.TT_EOF) {
            switch (token) {
                case StreamTokenizer.TT_EOL:
                    // Ignore \n character.
                    break;
                case StreamTokenizer.TT_WORD:
                    commandWords2.add(tokenizer.sval);
                    break;
                case '\'':
                    // If the tailing ' is missing, it will add a tailing ' to it.
                    // E.g. 'abc -> 'abc'
                    commandWords2.add(String.format("'%s'", tokenizer.sval));
                    break;
                case '"':
                    // If the tailing " is missing, it will add a tailing ' to it.
                    // E.g. "abc -> "abc"
                    commandWords2.add(String.format("\"%s\"", tokenizer.sval));
                    break;
                default:
                    // Ignore all other unknown characters.
                    throw new RuntimeException("unexpected input tokens " + token);
            }
            token = tokenizer.nextToken();
        }
        return commandWords1.size() <= commandWords2.size()
                ? commandWords1.toArray(new String[0])
                : commandWords2.toArray(new String[0]);
    }

    public static void singleLine() {
        System.out.println(
                "---------------------------------------------------------------------------------------------");
    }

    public static void doubleLine() {
        System.out.println(
                "=============================================================================================");
    }

    public static void sortFiles(File[] files) {
        if (files == null || files.length <= 1) {
            return;
        }
        Arrays.sort(
                files,
                (f1, f2) -> {
                    long diff = f1.lastModified() - f2.lastModified();
                    if (diff > 0) return -1;
                    else if (diff == 0) return 0;
                    else return 1;
                });
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
            return null;
        }
        Instant instant = attr.creationTime().toInstant();
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault())
                .format(instant);
    }

    public static String removeSolSuffix(String name) {
        return (name.endsWith(SOL_SUFFIX)
                ? name.substring(0, name.length() - SOL_SUFFIX.length())
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
        filePath = ConsoleUtils.removeSolSuffix(filePath);
        filePath += SOL_SUFFIX;
        // check again path file exist: contracts/solidity/Asset + .sol
        solFile = new File(filePath);
        if (solFile.exists()) {
            return solFile;
        }

        // Check that the file exists in the default directory first
        solFile = new File(SOLIDITY_PATH + File.separator + filePath);
        // file not exist
        if (!solFile.exists() && checkExist) {
            throw new ConsoleMessageException(solFileNameOrPath + " does not exist ");
        }
        return solFile;
    }

    public static String getLiquidFilePath(String liquidFileNameOrPath)
            throws ConsoleMessageException {

        File liquidFile = new File(liquidFileNameOrPath);
        if (liquidFile.exists()) {
            return liquidFile.getAbsolutePath();
        }
        // Check that the file exists in the default directory first
        liquidFile = new File(LIQUID_PATH + File.separator + liquidFileNameOrPath);
        // file not exist
        if (!liquidFile.exists()) {
            throw new ConsoleMessageException(liquidFileNameOrPath + " does not exist ");
        }
        return liquidFile.getAbsolutePath();
    }

    // scan file in path matches suffix
    public static String scanPathWithSuffix(String path, String suffix)
            throws ConsoleMessageException {
        class FileSuffixFilter implements FileFilter {
            final String suffix;

            FileSuffixFilter(String suffix) {
                this.suffix = suffix.toLowerCase();
            }

            @Override
            public boolean accept(File pathname) {
                if (pathname.getName().toLowerCase().endsWith(this.suffix)) {
                    return !pathname.getName().toLowerCase().endsWith("_gm" + this.suffix);
                }
                return false;
            }
        }
        File scanFile = new File(path);
        // not exist or not a directory, use contract path
        if (!scanFile.exists() || !scanFile.isDirectory()) {
            scanFile =
                    new File(
                            suffix.equals(SOL_SUFFIX)
                                    ? SOLIDITY_PATH
                                    : LIQUID_PATH + File.separator + path);
        }
        // still not exist
        if (!scanFile.exists() || !scanFile.isDirectory()) {
            throw new ConsoleMessageException("There is no any file end with " + suffix);
        }
        File[] files = scanFile.listFiles(new FileSuffixFilter(suffix));
        if (files == null || files.length == 0) {
            throw new ConsoleMessageException("There is no any file end with " + suffix);
        } else if (files.length > 1) {
            throw new ConsoleMessageException(
                    "There are more than one file end with "
                            + suffix
                            + ": "
                            + Arrays.toString(files));
        }
        return files[0].getAbsolutePath();
    }

    public static String resolvePath(String path) {
        if (path.startsWith("~/")) {
            return Paths.get(System.getProperty("user.home")).resolve(path.substring(2)).toString();
        }
        return path;
    }

    public static String getContractName(String contractNameOrPath) throws ConsoleMessageException {
        File contractFile = ConsoleUtils.getSolFile(contractNameOrPath, true);
        return ConsoleUtils.removeSolSuffix(contractFile.getName());
    }

    public static String getContractNameWithoutCheckExists(String contractNameOrPath)
            throws ConsoleMessageException {
        File contractFile = ConsoleUtils.getSolFile(contractNameOrPath, false);
        return ConsoleUtils.removeSolSuffix(contractFile.getName());
    }

    public static String bytesToHex(byte[] bytes) {
        String strHex = "";
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            strHex = Integer.toHexString(aByte & 0xFF);
            sb.append((strHex.length() == 1) ? "0" + strHex : strHex);
        }
        return sb.toString().trim();
    }

    // for compatibility, if AbiObject not exist, use this method print results
    @Deprecated
    public static void getReturnResults(
            StringBuilder resultType, StringBuilder resultData, Type result) {
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
            resultType.append("[");
            resultData.append("[");
            List<Type> values = ((StructType) result).getComponentTypes();
            for (int i = 0; i < values.size(); ++i) {
                getReturnResults(resultType, resultData, values.get(i));
                if (i != values.size() - 1) {
                    resultType.append(", ");
                    resultData.append(", ");
                }
            }
            resultData.append("]");
            resultType.append("]");
            throw new UnsupportedOperationException();
        } else if (result instanceof Bytes) {
            String data = "hex://0x" + bytesToHex(((Bytes) result).getValue());
            resultType.append(result.getTypeAsString());
            resultData.append(data);
        } else if (result instanceof DynamicBytes) {
            String data = "hex://0x" + bytesToHex(((DynamicBytes) result).getValue());
            resultType.append(result.getTypeAsString());
            resultData.append(data);
        } else {
            resultType.append(result.getTypeAsString());
            resultData.append(result.getValue());
        }
    }

    @Deprecated
    public static void printReturnResults(List<Type> results) {
        if (results == null) {
            return;
        }
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

    public static void printResults(
            List<ABIObject> returnABIObject, List<Object> returnObject, List<Type> results) {
        if (returnABIObject == null
                || returnObject == null
                || returnObject.isEmpty()
                || returnABIObject.isEmpty()) {
            // if AbiObject not exist, use this method print results
            printReturnResults(results);
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

    public static void getReturnObjectOutputData(
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
                String data = "hex://0x" + bytesToHex(ContractCodecTools.formatBytesN(abiObject));
                resultData.append(data).append(", ");
            } else if (abiObject.getValueType().equals(ABIObject.ValueType.DBYTES)) {
                String data = "hex://0x" + bytesToHex(abiObject.getDynamicBytesValue().getValue());
                resultData.append(data).append(", ");
            } else if (returnObject.size() > i) {
                resultData.append(returnObject.get(i).toString()).append(", ");
            }
            i += 1;
        }
    }

    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println("Mode should be specified firstly(as `solidity` or `liquid`).");
            System.out.println(" eg:");
            System.out.println(" contract2java.sh solidity ...");
            System.out.println(" contract2java.sh liquid ...");
            System.exit(-1);
        }

        String cmdSyntax = "contract2java.sh <solidity|liquid> [OPTIONS...]";
        String mode = args[0];

        if (mode.equals("-h") || mode.equals("--help")) {
            System.out.println("usage: " + cmdSyntax);
            System.out.println(
                    "use \"contract2java.sh solidity -h\" or \"contract2java.sh liquid -h\" to get more information.");
            System.exit(0);
        }

        Options options = new Options();
        // package name
        String DEFAULT_PACKAGE = "com";
        String PACKAGE_OPTION = "package";
        Option packageOption =
                new Option(
                        "p",
                        PACKAGE_OPTION,
                        true,
                        "[Optional] The package name of the generated java code, default is "
                                + DEFAULT_PACKAGE);
        packageOption.setRequired(false);
        options.addOption(packageOption);

        // the generated java code path
        String OUTPUT_OPTION = "output";
        String DEFAULT_OUTPUT = JAVA_PATH;
        Option outputPathOption =
                new Option(
                        "o",
                        OUTPUT_OPTION,
                        true,
                        "[Optional] The file path of the generated java code, default is "
                                + DEFAULT_OUTPUT);
        outputPathOption.setRequired(false);
        options.addOption(outputPathOption);

        String HELP_OPTION = "help";
        Option helpOption = new Option("h", HELP_OPTION, false, "");
        helpOption.setRequired(false);
        options.addOption(helpOption);

        // solidityFilePath or solidityDirPath
        String SOL_OPTION = "sol";
        String DEFAULT_SOL = SOLIDITY_PATH;
        String LIBS_OPTION = "libraries";

        String SOL_VERSION_OPTION = "sol-version";
        Version DEFAULT_SOL_VERSION = Version.V0_8_11;

        String BIN_OPTION = "bin";
        String SM_BIN_OPTION = "sm-bin";
        String ABI_OPTION = "abi";

        String NO_ANALYSIS_OPTION = "no-analysis";
        String ENABLE_ASYNC_CALL_OPTION = "enable-async-call";
        String TRANSACTION_VERSION = "transaction-version";

        Option transactionVersion =
                new Option(
                        "t",
                        TRANSACTION_VERSION,
                        true,
                        "[Optional] Specify transaction version interface, default is 0; Supporting {0,1,2}; If you want to use the latest transaction interface, please specify 2.");
        transactionVersion.setRequired(false);
        options.addOption(transactionVersion);
        if (mode.equals("solidity")) {
            Option solidityFilePathOption =
                    new Option(
                            "s",
                            SOL_OPTION,
                            true,
                            "[Optional] The solidity file path or the solidity directory path, default is "
                                    + DEFAULT_SOL);
            solidityFilePathOption.setRequired(false);
            options.addOption(solidityFilePathOption);

            Option solidityVersionPathOption =
                    new Option(
                            "v",
                            SOL_VERSION_OPTION,
                            true,
                            "[Optional] The solidity compiler version, default is "
                                    + DEFAULT_SOL_VERSION);
            solidityFilePathOption.setRequired(false);
            options.addOption(solidityVersionPathOption);

            // libraries
            Option libraryOption =
                    new Option(
                            "l",
                            LIBS_OPTION,
                            true,
                            "[Optional] Set library address information built into the solidity contract\n eg:\n --libraries lib1:lib1_address lib2:lib2_address\n");
            libraryOption.setRequired(false);
            options.addOption(libraryOption);

            // no evm static analysis
            Option noAnalysisOption =
                    new Option(
                            "n",
                            NO_ANALYSIS_OPTION,
                            false,
                            "[Optional] NOT use evm static parallel-able analysis. It will not active DAG analysis, but will speedup compile speed.");
            options.addOption(noAnalysisOption);

            Option enableAsyncCall =
                    new Option(
                            "e",
                            ENABLE_ASYNC_CALL_OPTION,
                            false,
                            "[Optional] Enable generate async interfaces for constant call, java file only compilable when java-sdk >= 3.3.0.");
            options.addOption(enableAsyncCall);
        } else if (mode.equals("liquid")) {
            Option liquidBinPathOption =
                    new Option(
                            "b",
                            BIN_OPTION,
                            true,
                            "[Required] The binary file path of Liquid contract.");
            liquidBinPathOption.setRequired(true);
            options.addOption(liquidBinPathOption);

            Option liquidAbiPathOption =
                    new Option(
                            "a",
                            ABI_OPTION,
                            true,
                            "[Required] The ABI file path of Liquid contract.");
            liquidAbiPathOption.setRequired(true);
            options.addOption(liquidAbiPathOption);

            Option liquidSmBinPathOption =
                    new Option(
                            "s",
                            SM_BIN_OPTION,
                            true,
                            "[Required] The SM binary file path of Liquid contract.");
            liquidSmBinPathOption.setRequired(true);
            options.addOption(liquidSmBinPathOption);
        } else {
            System.out.println(
                    " Unknown mode: " + mode + ", only `solidity` or `liquid` are available.");
            System.exit(-1);
        }

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, Arrays.copyOfRange(args, 1, args.length));
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp(cmdSyntax, options);
            System.exit(1);
        }
        if (cmd == null) {
            formatter.printHelp(cmdSyntax, options);
            System.exit(1);
        }
        if (cmd.hasOption(HELP_OPTION)) {
            formatter.printHelp(cmdSyntax, options);
            System.exit(0);
        }

        String pkgName = cmd.getOptionValue(PACKAGE_OPTION, DEFAULT_PACKAGE);
        String javaDir = cmd.getOptionValue(OUTPUT_OPTION, DEFAULT_OUTPUT);
        String transactionVersionStr = "V" + cmd.getOptionValue(TRANSACTION_VERSION, "0");
        if (mode.equals("solidity")) {
            String solPathOrDir = cmd.getOptionValue(SOL_OPTION, DEFAULT_SOL);
            Version solVersion =
                    convertStringToVersion(
                            cmd.getOptionValue(SOL_VERSION_OPTION, DEFAULT_SOL_VERSION.toString()));
            String librariesOption = cmd.getOptionValue(LIBS_OPTION, "");
            boolean useDagAnalysis = !cmd.hasOption(NO_ANALYSIS_OPTION);
            boolean enableAsyncCall = cmd.hasOption(ENABLE_ASYNC_CALL_OPTION);
            String fullJavaDir = new File(javaDir).getAbsolutePath();
            String specifyContract = null;
            if (solPathOrDir.contains(":")
                    && solPathOrDir.indexOf(':') == solPathOrDir.lastIndexOf(':')) {
                String[] strings = solPathOrDir.split(":");
                solPathOrDir = strings[0];
                specifyContract = strings[1];
            }
            File sol = new File(solPathOrDir);
            if (!sol.exists()) {
                System.out.println(sol.getAbsoluteFile() + " not exist ");
                System.exit(0);
            }
            try {
                if (sol.isFile()) { // input file
                    compileSolToJava(
                            fullJavaDir,
                            pkgName,
                            sol,
                            ABI_PATH,
                            BIN_PATH,
                            DOC_PATH,
                            librariesOption,
                            specifyContract,
                            useDagAnalysis,
                            enableAsyncCall,
                            transactionVersionStr,
                            solVersion);
                } else { // input dir
                    compileAllSolToJava(
                            fullJavaDir,
                            pkgName,
                            sol,
                            ABI_PATH,
                            BIN_PATH,
                            DOC_PATH,
                            useDagAnalysis,
                            enableAsyncCall,
                            transactionVersionStr,
                            solVersion);
                }
            } catch (IOException | CompileContractException e) {
                System.out.print(e.getMessage());
                logger.error(" message: {}, e: {}", e.getMessage(), e);
            }
            return;
        }

        if (mode.equals("liquid")) {
            String abiFile = cmd.getOptionValue(ABI_OPTION);
            String binFile = cmd.getOptionValue(BIN_OPTION);
            String smBinFile = cmd.getOptionValue(SM_BIN_OPTION);
            List<String> params =
                    new ArrayList<>(
                            Arrays.asList(
                                    "-v", "V3",
                                    "-a", abiFile,
                                    "-b", binFile,
                                    "-s", smBinFile,
                                    "-p", pkgName,
                                    "-o", javaDir));
            if (!transactionVersionStr.equals("0")) {
                params.add("-t");
                params.add(transactionVersionStr);
            }
            CodeGenMain.main(params.toArray(new String[0]));
        }
    }

    public static Version convertStringToVersion(String version) {
        try {
            return Version.valueOf("V" + version.replace('.', '_'));
        } catch (Exception e) {
            System.out.println(
                    "Invalid solidity version: "
                            + version
                            + ", only support: "
                            + Arrays.toString(Version.values()));
            throw e;
        }
    }
}
