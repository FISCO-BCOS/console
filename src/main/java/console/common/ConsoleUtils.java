package console.common;

import static org.fisco.bcos.web3j.solidity.compiler.SolidityCompiler.Options.ABI;
import static org.fisco.bcos.web3j.solidity.compiler.SolidityCompiler.Options.BIN;
import static org.fisco.bcos.web3j.solidity.compiler.SolidityCompiler.Options.INTERFACE;
import static org.fisco.bcos.web3j.solidity.compiler.SolidityCompiler.Options.METADATA;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.apache.commons.io.FileUtils;
import org.fisco.bcos.web3j.codegen.SolidityFunctionWrapperGenerator;
import org.fisco.bcos.web3j.solidity.compiler.CompilationResult;
import org.fisco.bcos.web3j.solidity.compiler.SolidityCompiler;

public class ConsoleUtils {

  public static final String JAVAPATH = "solidity/java/org/fisco/bcos/temp";
  public static final String CLASSPATH = "solidity/java/classes/org/fisco/bcos/temp";
  public static final String TARGETCLASSPATH = "solidity/java/classes";
  public static final String PACKAGENAME = "org.fisco.bcos.temp";

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
    if (hash.startsWith("0x") && hash.length() == 66) {
      return false;
    } else {
      System.out.println("Please provide a valid hash.");
      System.out.println();
      return true;
    }
  }

  public static boolean isInvalidNumber(String number, int flag) {
    String numberStr = number.trim();
    if (!numberStr.matches("^[0-9]*$") || "".equals(numberStr)) {
      if (flag == 0)
        System.out.println("Please provide block number as a decimal non-negative integer.");
      else
        System.out.println("Please provide transaction index as a decimal non-negative integer.");
      System.out.println();
      return true;
    } else {
      return false;
    }
  }
  
  public static int proccessIndex(String indexStr) {
		int index = 0;
		try {
			index = Integer.parseInt(indexStr);
		  if(index < 0)
		  {
		    System.out.println("Please provide index by non-negative integer mode(0~2147483647).");
		    System.out.println();
		    return Common.InvalidReturnNumber;
		  }
		} catch (NumberFormatException e) {
		    System.out.println("Please provide index by non-negative integer mode(0~2147483647).");
		    System.out.println();
		    return Common.InvalidReturnNumber;
		}
		return index;
	}
  
  public static Address convertAddress(String addressStr)
  {	
  	Address address = new Address();
  	if(addressStr.length() > Address.ValidLen)
  	{
  		address.setValid(false);
  		address.setAddress(addressStr);
  	}
  	else
  	{
  		address.setValid(true);
  		if(addressStr.startsWith("0x"))
  		{	
  			if(!addressStr.substring(2, addressStr.length()).matches("^[a-f0-9]+$"))
  			{
  				address.setValid(false);
  				address.setAddress(addressStr);
  			}
  			else 
  			{
    			if(addressStr.length() == Address.ValidLen)
    			{
    				address.setAddress(addressStr);
    			}
    			else 
    			{
    				getAddress(address, addressStr, Address.ValidLen);
    			}
  			}

  		}else {
  			if(!addressStr.matches("^[a-f0-9]+$"))
  			{
  				address.setValid(false);
  				address.setAddress(addressStr);
  			}
  			else 
  			{
    			if(addressStr.length() > Address.ValidLen - 2 )
    			{
    				address.setValid(false);
    				address.setAddress(addressStr);
    			}
    			else
    			{
    				getAddress(address, addressStr, Address.ValidLen - 2);
    			}
  			}

  		}
  	}
  	if(!address.isValid())
  	{
  		System.out.println("Please provide a valid address.");
  		System.out.println();
  	}
  	return address;
  }

	private static void getAddress(Address address, String addressStr, int length) {
		int len = length - addressStr.length();
		StringBuilder builderAddress = new StringBuilder();
		for(int i = 0; i < len; i++)
		{
			builderAddress.append("0");
		}
		String newAddessStr;
		if(length == Address.ValidLen)
		{
			newAddessStr = "0x" + builderAddress.toString() + addressStr.substring(2, addressStr.length());
		}
		else 
		{
			newAddessStr = "0x" + builderAddress.toString() + addressStr;
		}
		address.setAddress(newAddessStr);
	}

  // dynamic compile target java code
  public static void dynamicCompileJavaToClass(String name) throws Exception {

    File sourceDir = new File(JAVAPATH);
    if (!sourceDir.exists()) {
      sourceDir.mkdirs();
    }

    File distDir = new File(TARGETCLASSPATH);
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
          javac.run(null, null, null, "-d", distDir.getAbsolutePath(), javaFile.getAbsolutePath());
      if (compileResult != 0) {
        System.err.println("compile failed!!");
        System.out.println();
        return;
      }
    }
  }

  public static void dynamicCompileSolFilesToJava(String name) throws IOException {
    File solFileList = new File("solidity/contracts/");
    if(!solFileList.exists()){
      throw new IOException("Please checkout solidity/contracts/ is exist");
    }
    String tempDirPath = new File("solidity/java").getAbsolutePath();
    compileSolToJava(tempDirPath, PACKAGENAME, solFileList, "solidity/abi/", "solidity/bin/");
  }
  
  public static void main(String[] args) throws Exception {
    if (args.length < 1) {
      System.out.println("Please provide a package name.");
      return;
    }

    File solFileList = new File("contracts");
    String tempDirPath = new File("java").getAbsolutePath();
    compileSolToJava(tempDirPath, args[0], solFileList, "abi/", "bin/");
    System.out.println("\nCompile solidity contract files to java contract files successfully!");
  }

	private static void compileSolToJava(String tempDirPath, String packageName, File solFileList, 
			String abiDir, String binDir) throws IOException {
		File[] solFiles = solFileList.listFiles();
    if(solFiles.length == 0)
    {
    	System.out.println("The contracts directory is empty.");
    	return;
    }
    for (File solFile : solFiles) {
      if(!solFile.getName().endsWith(".sol") || solFile.getName().contains("Lib"))
			{
				continue;
			}
      SolidityCompiler.Result res =
          SolidityCompiler.compile(solFile, true, ABI, BIN, INTERFACE, METADATA);
      if("".equals(res.output))
      {
      	System.out.println("Compile error: " + res.errors);
      	return;
      }
      CompilationResult result = CompilationResult.parse(res.output);
      String contractname = solFile.getName().split("\\.")[0];
      CompilationResult.ContractMetadata a = result.getContract(solFile.getName().split("\\.")[0]);
      FileUtils.writeStringToFile(new File(abiDir + contractname + ".abi"), a.abi);
      FileUtils.writeStringToFile(new File(binDir + contractname + ".bin"), a.bin);
      String binFile;
      String abiFile;
      String filename = contractname;
      abiFile = abiDir + filename + ".abi";
      binFile = binDir + filename + ".bin";
      SolidityFunctionWrapperGenerator.main(
          Arrays.asList(
                  "-a", abiFile,
                  "-b", binFile,
                  "-p", packageName,
                  "-o", tempDirPath)
              .toArray(new String[0]));
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
      List<String> tokens = new ArrayList<>();

      StreamTokenizer tokenizer = new CommandTokenizer(new StringReader(command));

      int token = tokenizer.nextToken();
      while (token != StreamTokenizer.TT_EOF) {
          switch (token){
              case StreamTokenizer.TT_EOL:
                  // Ignore \n character.
                  break;
              case StreamTokenizer.TT_WORD:
                  tokens.add(tokenizer.sval);
                  break;
              case '\'':  
              		// If the tailing ' is missing, it will add a tailing ' to it.
              	  // E.g. 'abc -> 'abc'
                  tokens.add(String.format("'%s'", tokenizer.sval));
                  break;
              case '"':
              	  // If the tailing " is missing, it will add a tailing ' to it.
                  // E.g. "abc -> "abc"
                  tokens.add(String.format("\"%s\"", tokenizer.sval));
                  break;
              default:
                  // Ignore all other unknown characters.
              	throw new RuntimeException("unexpected input tokens " + token);
          }
          token = tokenizer.nextToken();
      }
      return tokens.toArray(new String[tokens.size()]);
  }
  
  public static void singleLine() {
    System.out.println(
        "-------------------------------------------------------------------------------------");
  }

  public static void singleLineForTable() {
    System.out.println(
        "---------------------------------------------------------------------------------------------");
  }

  public static void doubleLine() {
    System.out.println(
        "=====================================================================================");
  }
}
