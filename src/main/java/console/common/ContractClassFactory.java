package console.common;

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
      throws ClassNotFoundException, InstantiationException, IllegalAccessException, MalformedURLException {

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
        File[] classFiles = path.listFiles(new FileFilter() {
          public boolean accept(File pathname) {
            return pathname.isDirectory() || pathname.getName().endsWith(".class");
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

  @SuppressWarnings("rawtypes")
  public static Class[] getParameterType(Class clazz, String methodName, int paramsLen)
      throws ClassNotFoundException {
    Method[] methods = clazz.getDeclaredMethods();
    Class[] type = null;
    for (Method method : methods) {
      if (methodName.equals(method.getName()) && (method.getParameters()).length == paramsLen) {
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
  public static Object[] getPrametersObject(String funcName, Class[] type, String[] params) {
    Object[] obj = new Object[params.length - 4];
    for (int i = 0; i < obj.length; i++) {
      if (type[i] == String.class) {
        if (params[i + 4].startsWith("\"") && params[i + 4].endsWith("\"")) {
          try {
            obj[i] = params[i + 4].substring(1, params[i + 4].length() - 1);
          } catch (Exception e) {
            System.out.println(
                "Please provide double quote for String that cannot contain any blank spaces.");
            System.out.println();
            return null;
          }
        } else {
          System.out.println(
              "Please provide double quote for String that cannot contain any blank spaces.");
          System.out.println();
          return null;
        }
      } else if (type[i] == Boolean.class) {
        try {
          obj[i] = Boolean.parseBoolean(params[i + 4]);
        } catch (Exception e) {
          System.out.println(
              "The " + (i + 1) + "th parameter of " + funcName + " needs boolean value.");
          System.out.println();
          return null;
        }
      } else if (type[i] == BigInteger.class) {
        try {
          obj[i] = new BigInteger(params[i + 4]);
        } catch (Exception e) {
          System.out.println(
              "The " + (i + 1) + "th parameter of " + funcName + " needs integer value.");
          System.out.println();
          return null;
        }
      } else if (type[i] == byte[].class) {
        if (params[i + 4].startsWith("\"") && params[i + 4].endsWith("\"")) {
          byte[] bytes1 = new byte[Integer.MAX_VALUE];
          byte[] bytes2 = params[i + 4].substring(1, params[i + 4].length() - 1).getBytes();
          for (int j = 0; j < bytes2.length; j++) {
            bytes1[j] = bytes2[j];
          }
          obj[i] = bytes1;
        } else {
          System.out.println("Please provide double quote for byte String.");
          System.out.println();
          return null;
        }
      }
    }
    return obj;
  }

  @SuppressWarnings("rawtypes")
  public static String getReturnObject(
      Class clazz, String methodName, Class[] parameterType, Object result)
      throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException,
          InvocationTargetException, NoSuchMethodException, SecurityException {
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
          return resultTx.getTransactionHash();
        } else {
          return result.toString();
        }
      }
    }
    return null;
  }
}
