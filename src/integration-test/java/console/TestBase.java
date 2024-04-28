package console;

import console.auth.AuthFace;
import console.client.ConsoleClientFace;
import console.contract.ConsoleContractFace;
import console.precompiled.PrecompiledFace;
import org.fisco.bcos.sdk.v3.model.EnumNodeVersion;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class TestBase {

    protected static ConsoleClientFace consoleClientFace;
    protected static PrecompiledFace precompiledFace;
    protected static ConsoleContractFace consoleContractFace;
    protected static ConsoleInitializer consoleInitializer;
    protected static boolean isWasm;
    protected static boolean isAuthCheck;
    protected static EnumNodeVersion chainVersion;
    protected static AuthFace authFace;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        consoleInitializer = new ConsoleInitializer();
        consoleInitializer.init(new String[0]);
        isWasm = consoleInitializer.getClient().isWASM();
        isAuthCheck = consoleInitializer.getClient().isEnableCommittee();
        authFace = consoleInitializer.getAuthFace();
        consoleClientFace = consoleInitializer.getConsoleClientFace();
        precompiledFace = consoleInitializer.getPrecompiledFace();
        consoleContractFace = consoleInitializer.getConsoleContractFace();
        long compatibilityVersion = consoleInitializer.getClient().getGroupInfo().getResult().getNodeList().get(0).getProtocol().getCompatibilityVersion();
        chainVersion = EnumNodeVersion.valueOf((int) compatibilityVersion);
    }

    @AfterClass
    public static void setUpAfterClass() throws Exception {}
}
