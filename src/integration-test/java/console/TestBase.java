package console;

import console.client.ConsoleClientFace;
import console.contract.ConsoleContractFace;
import console.precompiled.PrecompiledFace;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class TestBase {

    protected static ConsoleClientFace consoleClientFace;
    protected static PrecompiledFace precompiledFace;
    protected static ConsoleContractFace consoleContractFace;
    protected static ConsoleInitializer consoleInitializer;
    protected static boolean isWasm;
    protected static boolean isAuthCheck;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        consoleInitializer = new ConsoleInitializer();
        consoleInitializer.init(new String[0]);
        isWasm = consoleInitializer.getClient().isWASM();
        isAuthCheck = consoleInitializer.getClient().isAuthCheck();
        consoleClientFace = consoleInitializer.getConsoleClientFace();
        precompiledFace = consoleInitializer.getPrecompiledFace();
        consoleContractFace = consoleInitializer.getConsoleContractFace();
    }

    @AfterClass
    public static void setUpAfterClass() throws Exception {}
}
