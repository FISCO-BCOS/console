package console.common;

import console.exception.ConsoleMessageException;
import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.tuples.Tuple;

public class ContractClassFactory {
    public static final String TARGETCLASSPATH = "solidity/java/classes";

    public static Class<?> getContractClass(String contractName)
            throws ClassNotFoundException, MalformedURLException {

        File clazzPath = new File(TARGETCLASSPATH);

        if (clazzPath.exists() && clazzPath.isDirectory()) {
            URL[] urls = new URL[1];
            urls[0] = clazzPath.toURI().toURL();
            URLClassLoader classLoader = new URLClassLoader(urls);

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

        return (Class<?>) Class.forName(contractName);
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
    public static Class[] getParameterType(Class clazz, String methodName, int paramsLen)
            throws ClassNotFoundException, IllegalAccessException, MalformedURLException,
                    InstantiationException {
        Method[] methods = clazz.getDeclaredMethods();
        Class[] type = null;
        for (Method method : methods) {
            if (methodName.equals(method.getName())
                    && (method.getParameters()).length == paramsLen) {
                Parameter[] params = method.getParameters();
                type = new Class[params.length];
                for (int i = 0; i < params.length; i++) {
                    String typeName = params[i].getParameterizedType().getTypeName();
                    if ("byte[]".equals(typeName)) type[i] = byte[].class;
                    else type[i] = Class.forName(typeName);
                }
                break;
            }
        }

        return type;
    }

    @SuppressWarnings("rawtypes")
    public static Object[] getPrametersObject(
            String funcName, Class[] type, String[] params, String[] generics)
            throws ConsoleMessageException {
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
                    if (param.compareTo(new BigInteger(Integer.MAX_VALUE + "")) == 1
                            || param.compareTo(new BigInteger(Integer.MIN_VALUE + "")) == -1) {
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
                    byte[] bytes2 = params[i].substring(1, params[i].length() - 1).getBytes();
                    byte[] bytes1 = new byte[32];

                    for (int j = 0; j < bytes2.length; j++) {
                        bytes1[j] = bytes2[j];
                    }
                    obj[i] = bytes1;
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
                                    byte[] bytes1 = new byte[32];
                                    byte[] bytes2 = bytes;
                                    for (int k = 0; k < bytes2.length; k++) {
                                        bytes1[k] = bytes2[k];
                                    }
                                    paramsList.add(bytes1);
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
                    return "transaction hash:" + resultTx.getTransactionHash();
                } else {
                    return result.toString();
                }
            }
        }
        return null;
    }

    public static Method getMethodByName(String funcName, Method[] methods) {
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
}
