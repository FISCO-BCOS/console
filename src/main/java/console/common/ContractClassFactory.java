package console.common;

import console.exception.ConsoleMessageException;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import org.apache.commons.collections4.map.HashedMap;
import org.fisco.bcos.web3j.abi.EventEncoder;
import org.fisco.bcos.web3j.abi.datatypes.Bytes;
import org.fisco.bcos.web3j.abi.datatypes.Event;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.precompile.cns.CnsService;
import org.fisco.bcos.web3j.precompile.common.PrecompiledCommon;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.core.RemoteCall;
import org.fisco.bcos.web3j.protocol.core.methods.response.Log;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.tuples.Tuple;
import org.fisco.bcos.web3j.tx.gas.StaticGasProvider;

public class ContractClassFactory {

    public static final String SOLIDITY_PATH = "contracts/solidity/";
    public static final String JAVA_PATH = "contracts/console/java/";
    public static final String ABI_PATH = "contracts/console/abi/";
    public static final String BIN_PATH = "contracts/console/bin/";
    public static final String PACKAGE_NAME = "temp";
    public static final String TAR_GET_CLASSPATH = "contracts/console/java/classes/";
    public static final String SOL_POSTFIX = ".sol";
    private static URLClassLoader classLoader;

    public static void initClassLoad() throws MalformedURLException {
        File clazzPath = new File(TAR_GET_CLASSPATH);
        if (!clazzPath.exists()) {
            clazzPath.mkdirs();
        }

        URL[] urls = new URL[1];
        urls[0] = clazzPath.toURI().toURL();
        classLoader = new URLClassLoader(urls);
    }

    public static Class<?> compileContract(String name) throws Exception {
        try {
            name = removeSolPostfix(name);
            dynamicCompileSolFilesToJava(name);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        try {
            dynamicCompileJavaToClass(name);
        } catch (Exception e1) {
            throw new Exception("Compile " + name + ".java failed.");
        }
        String contractName = PACKAGE_NAME + "." + name;
        try {
            return getContractClass(contractName);
        } catch (Exception e) {
            throw new Exception(
                    "There is no " + name + ".class" + " in the directory of java/classes/temp");
        }
    }

    public static String removeSolPostfix(String name) {
        String tempName = "";
        if (name.endsWith(SOL_POSTFIX)) {
            tempName = name.substring(0, name.length() - SOL_POSTFIX.length());
        } else {
            tempName = name;
        }
        return tempName;
    }

    // dynamic compile target java code
    public static void dynamicCompileJavaToClass(String name) throws Exception {

        File sourceDir = new File(JAVA_PATH + "temp/");
        if (!sourceDir.exists()) {
            sourceDir.mkdirs();
        }

        File distDir = new File(TAR_GET_CLASSPATH);
        if (!distDir.exists()) {
            distDir.mkdirs();
        }
        File[] javaFiles = sourceDir.listFiles();
        for (File javaFile : javaFiles) {
            if (!javaFile.getName().equals(name + ".java")) {
                continue;
            }
            JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
            int compileResult =
                    javac.run(
                            null,
                            null,
                            null,
                            "-d",
                            distDir.getAbsolutePath(),
                            javaFile.getAbsolutePath());
            if (compileResult != 0) {
                System.err.println("compile failed!!");
                System.out.println();
                throw new Exception("compile failed, solidity file: " + name);
            }
        }
    }

    public static void dynamicCompileSolFilesToJava(String name) throws IOException {
        if (!name.endsWith(SOL_POSTFIX)) {
            name = name + SOL_POSTFIX;
        }
        File solFileList = new File(SOLIDITY_PATH);
        if (!solFileList.exists()) {
            throw new IOException("Please checkout the directory " + SOLIDITY_PATH + " is exist.");
        }
        File solFile = new File(SOLIDITY_PATH + "/" + name);
        if (!solFile.exists()) {
            throw new IOException("There is no " + name + " in the directory of " + SOLIDITY_PATH);
        }
        String tempDirPath = new File(JAVA_PATH).getAbsolutePath();
        ConsoleUtils.compileSolToJava(
                name, tempDirPath, PACKAGE_NAME, solFileList, ABI_PATH, BIN_PATH);
    }

    public static Class<?> getContractClass(String contractName)
            throws ClassNotFoundException, MalformedURLException {

        File clazzPath = new File(TAR_GET_CLASSPATH);

        if (clazzPath.exists() && clazzPath.isDirectory()) {

            int clazzPathLen = clazzPath.getAbsolutePath().length() + 1;

            Stack<File> stack = new Stack<>();
            stack.push(clazzPath);

            while (!stack.isEmpty()) {
                File path = stack.pop();
                File[] classFiles =
                        path.listFiles(
                                new FileFilter() {
                                    public boolean accept(File pathname) {
                                        return pathname.isDirectory()
                                                || pathname.getName().endsWith(".class");
                                    }
                                });
                for (File subFile : classFiles) {
                    if (subFile.isDirectory()) {
                        stack.push(subFile);
                    } else {
                        String className = subFile.getAbsolutePath();
                        if (className.contains("$")) {
                            continue;
                        }

                        className = className.substring(clazzPathLen, className.length() - 6);
                        className = className.replace(File.separatorChar, '.');

                        if (contractName.equals(className)) {
                            return Class.forName(className, true, classLoader);
                        }
                    }
                }
            }
        }

        return Class.forName(contractName);
    }

    public static RemoteCall<?> handleDeployParameters(
            Web3j web3j,
            Credentials credentials,
            StaticGasProvider gasProvider,
            Class<?> contractClass,
            String[] params,
            int num)
            throws IllegalAccessException, InvocationTargetException, ConsoleMessageException {
        Method method = ContractClassFactory.getDeployFunction(contractClass);
        if (method == null) {
            throw new ConsoleMessageException(
                    "The method constructor with "
                            + contractClass.getName()
                            + " is undefined of the contract.");
        }

        Type[] classType = method.getParameterTypes();
        if (classType.length - 3 != params.length - num) {
            throw new ConsoleMessageException(
                    "The method constructor with "
                            + (params.length - num)
                            + " parameter"
                            + " is undefined of the contract.");
        }
        String[] generic = new String[method.getParameterCount()];
        for (int i = 0; i < classType.length; i++) {
            generic[i] = method.getGenericParameterTypes()[i].getTypeName();
        }
        Class[] classList = new Class[classType.length];
        for (int i = 0; i < classType.length; i++) {
            Class clazz = (Class) classType[i];
            classList[i] = clazz;
        }

        String[] newParams = new String[params.length - num];
        System.arraycopy(params, num, newParams, 0, params.length - num);
        Object[] obj =
                getDeployPrametersObject(
                        web3j, credentials, gasProvider, "deploy", classList, newParams, generic);
        return (RemoteCall<?>) method.invoke(null, obj);
    }

    public static Object[] getDeployPrametersObject(
            Web3j web3j,
            Credentials credentials,
            StaticGasProvider gasProvider,
            String funcName,
            Class[] type,
            String[] params,
            String[] generic)
            throws ConsoleMessageException {
        Object[] obj = new Object[params.length + 3];
        obj[0] = web3j;
        obj[1] = credentials;
        obj[2] = gasProvider;

        for (int i = 0; i < params.length; i++) {
            if (type[i + 3] == String.class) {
                if (params[i].startsWith("\"") && params[i].endsWith("\"")) {
                    obj[i + 3] = params[i].substring(1, params[i].length() - 1);
                } else {
                    throw new ConsoleMessageException(
                            "The "
                                    + (i + 1)
                                    + "th parameter of "
                                    + funcName
                                    + " needs string value.");
                }
            } else if (type[i + 3] == Boolean.class) {
                try {
                    obj[i + 3] = Boolean.parseBoolean(params[i]);
                } catch (Exception e) {
                    throw new ConsoleMessageException(
                            "The "
                                    + (i + 1)
                                    + "th parameter of "
                                    + funcName
                                    + " needs boolean value.");
                }
            } else if (type[i + 3] == BigInteger.class) {
                try {
                    BigInteger param = new BigInteger(params[i]);
                    if (param.compareTo(new BigInteger(Integer.MAX_VALUE + "")) > 0
                            || param.compareTo(new BigInteger(Integer.MIN_VALUE + "")) < 0) {
                        throw new ConsoleMessageException(
                                "The "
                                        + (i + 1)
                                        + "th parameter of "
                                        + funcName
                                        + " needs integer("
                                        + Integer.MIN_VALUE
                                        + " ~ "
                                        + Integer.MAX_VALUE
                                        + ") value in the console.");
                    } else {
                        obj[i + 3] = param;
                    }
                } catch (Exception e) {
                    throw new ConsoleMessageException(
                            "The "
                                    + (i + 1)
                                    + "th parameter of "
                                    + funcName
                                    + " needs integer("
                                    + Integer.MIN_VALUE
                                    + " ~ "
                                    + Integer.MAX_VALUE
                                    + ") value in the console.");
                }
            } else if (type[i + 3] == byte[].class) {
                if (params[i].startsWith("\"") && params[i].endsWith("\"")) {
                    byte[] bytes2 = params[i].substring(1, params[i].length() - 1).getBytes();
                    byte[] bytes1 = new byte[32];
                    for (int j = 0; j < bytes2.length; j++) {
                        bytes1[j] = bytes2[j];
                    }
                    obj[i + 3] = bytes1;
                } else {
                    throw new ConsoleMessageException(
                            "The "
                                    + (i + 1)
                                    + "th parameter of "
                                    + funcName
                                    + " needs byte string value.");
                }
            } else if (type[i + 3] == List.class) {

                if (params[i].startsWith("[") && params[i].endsWith("]")) {
                    String listParams = params[i].substring(1, params[i].length() - 1);
                    String[] ilist = listParams.split(",");
                    String[] jlist = new String[ilist.length];
                    for (int k = 0; k < jlist.length; k++) {
                        jlist[k] = ilist[k].trim();
                    }
                    List paramsList = new ArrayList();
                    if (generic[i + 3].contains("String")) {
                        paramsList = new ArrayList<String>();
                        for (int j = 0; j < jlist.length; j++) {
                            paramsList.add(jlist[j].substring(1, jlist[j].length() - 1));
                        }

                    } else if (generic[i + 3].contains("BigInteger")) {
                        paramsList = new ArrayList<BigInteger>();
                        for (int j = 0; j < jlist.length; j++) {
                            paramsList.add(new BigInteger(jlist[j]));
                        }

                    } else if (generic[i + 3].contains("byte[]")) {
                        paramsList = new ArrayList<byte[]>();
                        for (int j = 0; j < jlist.length; j++) {
                            if (jlist[j].startsWith("\"") && jlist[j].endsWith("\"")) {
                                byte[] bytes =
                                        jlist[j].substring(1, jlist[j].length() - 1).getBytes();
                                byte[] bytes1 = new byte[32];
                                byte[] bytes2 = bytes;
                                for (int k = 0; k < bytes2.length; k++) {
                                    bytes1[k] = bytes2[k];
                                }
                                paramsList.add(bytes1);
                            }
                        }
                    }
                    obj[i + 3] = paramsList;
                } else {
                    throw new ConsoleMessageException(
                            "The "
                                    + (i + 1)
                                    + "th parameter of "
                                    + funcName
                                    + " needs array value.");
                }
            }
        }
        return obj;
    }

    public static Method getDeployFunction(Class<?> contractClass) {
        Method[] methods = contractClass.getDeclaredMethods();
        for (Method method : methods) {
            if ("deploy".equals(method.getName())) {

                Class[] paramsType = method.getParameterTypes();
                List<String> ilist = new ArrayList<>();
                for (Class param : paramsType) {
                    ilist.add(param.getCanonicalName());
                }

                if (ilist.contains("org.fisco.bcos.web3j.protocol.Web3j")
                        && ilist.contains("org.fisco.bcos.web3j.crypto.Credentials")
                        && ilist.contains("org.fisco.bcos.web3j.tx.gas.ContractGasProvider")) {
                    return method;
                }
            }
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    public static Object[] getPrametersObject(
            String funcName, Class[] type, String[] params, String[] generics)
            throws ConsoleMessageException {
        if (type.length != params.length) {
            throw new ConsoleMessageException(
                    "The method "
                            + funcName
                            + " with "
                            + params.length
                            + " parameter"
                            + " is undefined of the contract.");
        }
        Object[] obj = new Object[params.length];
        for (int i = 0; i < obj.length; i++) {

            if (type[i] == String.class) {
                if (params[i].startsWith("\"") && params[i].endsWith("\"")) {
                    try {
                        obj[i] = params[i].substring(1, params[i].length() - 1);
                    } catch (Exception e) {
                        System.out.println(
                                "Please provide double quote for String type parameters.");
                        System.out.println();
                        return null;
                    }
                } else {
                    System.out.println("Please provide double quote for String type parameters.");
                    System.out.println();
                    return null;
                }
            } else if (type[i] == Boolean.class) {
                try {
                    obj[i] = Boolean.parseBoolean(params[i]);
                } catch (Exception e) {
                    System.out.println(
                            "The "
                                    + (i + 1)
                                    + "th parameter of "
                                    + funcName
                                    + " needs boolean value.");
                    System.out.println();
                    return null;
                }
            } else if (type[i] == BigInteger.class) {
                try {
                    BigInteger param = new BigInteger(params[i]);
                    if (param.compareTo(new BigInteger(Integer.MAX_VALUE + "")) > 0
                            || param.compareTo(new BigInteger(Integer.MIN_VALUE + "")) < 0) {
                        throw new ConsoleMessageException(
                                "The "
                                        + (i + 1)
                                        + "th parameter of "
                                        + funcName
                                        + " needs integer("
                                        + Integer.MIN_VALUE
                                        + " ~ "
                                        + Integer.MAX_VALUE
                                        + ") value in the console.");
                    } else {
                        obj[i] = param;
                    }
                } catch (Exception e) {
                    throw new ConsoleMessageException(
                            "The "
                                    + (i + 1)
                                    + "th parameter of "
                                    + funcName
                                    + " needs integer("
                                    + Integer.MIN_VALUE
                                    + " ~ "
                                    + Integer.MAX_VALUE
                                    + ") value in the console.");
                }
            } else if (type[i] == byte[].class) {
                if (params[i].startsWith("\"") && params[i].endsWith("\"")) {
                    byte[] bytes = params[i].substring(1, params[i].length() - 1).getBytes();
                    obj[i] = bytes;
                } else {
                    System.out.println("Please provide double quote for byte String.");
                    System.out.println();
                    return null;
                }
            } else if (type[i] == List.class) {

                if (params[i].startsWith("[") && params[i].endsWith("]")) {
                    try {
                        String listParams = params[i].substring(1, params[i].length() - 1);
                        String[] ilist = listParams.split(",");
                        List paramsList = new ArrayList();
                        if (generics[i].contains("String")) {
                            paramsList = new ArrayList<String>();
                            for (int j = 0; j < ilist.length; j++) {
                                paramsList.add(ilist[j].substring(1, ilist[j].length() - 1));
                            }

                        } else if (generics[i].contains("BigInteger")) {
                            paramsList = new ArrayList<BigInteger>();
                            for (int j = 0; j < ilist.length; j++) {
                                paramsList.add(new BigInteger(ilist[j]));
                            }

                        } else if (generics[i].contains("byte[]")) {
                            paramsList = new ArrayList<byte[]>();
                            for (int j = 0; j < ilist.length; j++) {
                                if (ilist[j].startsWith("\"") && ilist[j].endsWith("\"")) {
                                    byte[] bytes =
                                            ilist[j].substring(1, ilist[j].length() - 1).getBytes();
                                    paramsList.add(bytes);
                                }
                            }
                        }
                        obj[i] = paramsList;
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        }
        return obj;
    }

    @SuppressWarnings("rawtypes")
    public static String getReturnObject(
            Class clazz, String methodName, Class[] parameterType, Object result)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException,
                    NoSuchMethodException, SecurityException {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (methodName.equals(method.getName())
                    && (method.getParameterTypes()).length == parameterType.length) {
                java.lang.reflect.Type genericReturnType = method.getGenericReturnType();
                String typeName = genericReturnType.getTypeName();
                int n = 0;
                if (typeName.contains("org.fisco.bcos.web3j.tuples.generated.Tuple")) {

                    String temp = typeName.split("org.fisco.bcos.web3j.tuples.generated.Tuple")[1];
                    if (typeName.contains("org.fisco.bcos.web3j.tuples.generated.Tuple")) {
                        n = temp.charAt(0) - '0';
                    }
                    int len = temp.length();
                    String detailTypeList = temp.substring(2, len - 1);
                    String[] ilist = detailTypeList.split(",");

                    Tuple resultObj = (Tuple) result;
                    Class<? extends Tuple> classResult = resultObj.getClass();
                    List<Object> finalList = new ArrayList<>();

                    for (int i = 0; i < n; i++) {
                        Method get = classResult.getMethod("getValue" + (i + 1));
                        if (ilist[i].contains("List")) {
                            if (ilist[i].contains("byte")) {
                                List<byte[]> list1 = (List<byte[]>) get.invoke(resultObj);
                                List<Object> resultList = new ArrayList<>();
                                for (byte[] list : list1) {
                                    resultList.add(new String(list).trim());
                                }
                                finalList.add(resultList);
                            } else {
                                finalList.add(get.invoke(resultObj));
                            }
                        } else {
                            if (ilist[i].contains("byte")) {
                                byte[] byte1 = (byte[]) get.invoke(resultObj);
                                finalList.add(new String(byte1).trim());
                            } else {
                                finalList.add(get.invoke(resultObj));
                            }
                        }
                    }

                    return finalList.toString();

                } else if (typeName.contains("TransactionReceipt")) {
                    TransactionReceipt resultTx = (TransactionReceipt) result;
                    return "transaction hash: " + resultTx.getTransactionHash();
                } else if ("org.fisco.bcos.web3j.protocol.core.RemoteCall<byte[]>"
                        .equals(typeName)) {
                    byte[] bresult = (byte[]) result;
                    return new String(bresult);
                } else {
                    return result.toString();
                }
            }
        }
        return null;
    }

    public static Method getMethodByName(Method[] methods, String funcName, String[] params) {
        Method method = null;
        for (Method method1 : methods) {
            if (funcName.equals(method1.getName())) {
                Class[] paramsType = method1.getParameterTypes();
                if (paramsType.length != params.length) {
                    continue;
                }
                List<String> ilist = new ArrayList<>();
                for (Class param : paramsType) {
                    ilist.add(param.getCanonicalName());
                }

                if (!ilist.contains("org.fisco.bcos.channel.client.TransactionSucCallback")) {
                    method = method1;
                    break;
                }
            }
        }
        return method;
    }

    public static Method getEventByName(String funcName, Method[] methods) {
        Method method = null;
        for (Method method1 : methods) {
            if (funcName.equals(method1.getName())) {
                Class[] paramsType = method1.getParameterTypes();
                List<String> ilist = new ArrayList<>();
                for (Class param : paramsType) {
                    ilist.add(param.getCanonicalName());
                }

                if (!ilist.contains("org.fisco.bcos.channel.client.TransactionSucCallback")) {
                    method = method1;
                    break;
                }
            }
        }
        return method;
    }

    public static Set<Event> parseEvent(Object contractObject, TransactionReceipt receipt)
            throws IllegalAccessException {
        Set<Event> eventSet = new LinkedHashSet<>();
        // query topic
        List<Log> logs = receipt.getLogs();
        List<String> topicEventSigns = new ArrayList<>();
        for (Log log : logs) {
            if (log.getTopics() != null && log.getTopics().size() > 0) {
                String topicEventSign = log.getTopics().get(0);
                topicEventSigns.add(topicEventSign);
            }
        }
        // query contract event object
        Field[] fields = contractObject.getClass().getFields();
        Map<String, Event> mapEvent = new HashedMap<>();
        for (Field field : fields) {
            String str = field.getName();
            if (str.endsWith("_EVENT")) {
                Event event = (Event) field.get(contractObject);
                String eventSign = EventEncoder.encode(event);
                mapEvent.put(eventSign, event);
            }
        }
        // match event
        Set<String> keyEventSigns = mapEvent.keySet();
        for (String topicEventSign : topicEventSigns) {
            for (String keyEventSign : keyEventSigns) {
                if (topicEventSign.equals(keyEventSign)) {
                    eventSet.add(mapEvent.get(keyEventSign));
                }
            }
        }
        return eventSet;
    }

    public static void printEventLogByNameAndIndex(int index, String eventName, List<Object> result)
            throws IllegalAccessException {
        Object obj = result.get(index);
        Field[] fields = obj.getClass().getFields();
        System.out.println(eventName + " index: " + index);
        for (Field field : fields) {
            String varName = field.getName();
            if (!"log".equals(varName)) {
                Object varObj = field.get(obj);
                if (varObj instanceof ArrayList<?>) {
                    ArrayList<Object> listObjs = (ArrayList<Object>) varObj;
                    ArrayList<Object> resultList = new ArrayList<>();
                    for (Object listObj : listObjs) {

                        String simpleName = listObj.getClass().getSimpleName();
                        if (simpleName.contains("Bytes")) {
                            Bytes b = (Bytes) listObj;
                            resultList.add(new String(b.getValue()).trim());
                        } else {
                            resultList.add(listObj);
                        }
                    }
                    System.out.println(varName + " = " + resultList);
                } else if (varObj.getClass() == byte[].class) {
                    byte[] b = (byte[]) varObj;
                    System.out.println(varName + " = " + new String(b).trim());
                } else {
                    System.out.println(varName + " = " + varObj);
                }
            }
        }
    }

    public static void printEventLogs(
            Object contractObject, Method[] methods, TransactionReceipt receipt)
            throws IllegalAccessException, ConsoleMessageException, InvocationTargetException {
        Set<Event> events = ContractClassFactory.parseEvent(contractObject, receipt);
        if (events.size() != 0) {
            ConsoleUtils.singleLine();
            System.out.println("Event logs");
            ConsoleUtils.singleLine();
            for (Event event : events) {
                String eventName = event.getName();
                String funcEventName =
                        "get"
                                + eventName.substring(0, 1).toUpperCase()
                                + eventName.substring(1)
                                + "Events";
                Method eventMethod = ContractClassFactory.getEventByName(funcEventName, methods);
                if (eventMethod == null) {
                    throw new ConsoleMessageException(
                            "Cannot find the event "
                                    + eventName
                                    + ", please checkout the event name.");
                }
                List<Object> eventsResults =
                        (ArrayList<Object>) eventMethod.invoke(contractObject, receipt);
                if (eventsResults.size() == 0) {
                    throw new ConsoleMessageException("The " + eventName + " event is empty.");
                }
                for (int i = 0; i < eventsResults.size(); i++) {
                    ContractClassFactory.printEventLogByNameAndIndex(i, eventName, eventsResults);
                }
            }
            ConsoleUtils.singleLine();
        }
    }

    public static boolean checkVersion(String version) throws IOException {
        if (version.length() > CnsService.MAX_VERSION_LENGTH) {
            ConsoleUtils.printJson(
                    PrecompiledCommon.transferToJson(PrecompiledCommon.VersionExceeds));
            System.out.println();
            return false;
        }
        if (!version.matches("^[A-Za-z0-9.]+$")) {
            System.out.println(
                    "Contract version should only contains 'A-Z' or 'a-z' or '0-9' or dot mark.");
            System.out.println();
            return false;
        }
        return true;
    }
}
