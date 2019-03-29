package console;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import console.contract.ContractFace;
import console.precompiled.PrecompiledFace;
import console.precompiled.permission.PermissionFace;
import console.web3j.Web3jFace;

public class TestBase {
	
	protected static Web3jFace web3jFace;
	protected static PrecompiledFace precompiledFace;
	protected static PermissionFace permissionFace;
	protected static ContractFace contractFace;
	protected static ConsoleInitializer consoleInitializer;
	
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
		 consoleInitializer = new ConsoleInitializer();
		 consoleInitializer.init(new String[0]);
		 web3jFace = consoleInitializer.getWeb3jFace();
		 precompiledFace = consoleInitializer.getPrecompiledFace();
		 permissionFace = consoleInitializer.getPermissionFace();
		 contractFace = consoleInitializer.getContractFace();
  }

  @AfterClass
  public static void setUpAfterClass() throws Exception {
  	
  }
}
