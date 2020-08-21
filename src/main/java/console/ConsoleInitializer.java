package console;

import console.client.ConsoleClientFace;
import console.client.ConsoleClientImpl;
import console.common.Common;
import console.common.HelpInfo;
import console.contract.ConsoleContractFace;
import console.contract.ConsoleContractImpl;
import console.precompiled.PrecompiledFace;
import console.precompiled.PrecompiledImpl;
import console.precompiled.permission.PermissionFace;
import console.precompiled.permission.PermissionImpl;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.config.exceptions.ConfigException;
import org.fisco.bcos.sdk.crypto.CryptoInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleInitializer {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleInitializer.class);

    public static final String ACCOUNT_DIR1 = "accounts/";
    public static final String ACCOUNT_DIR2 = "./accounts/";

    private BcosSDK bcosSDK;
    private Client client;
    private ConsoleClientFace consoleClientFace;
    private PrecompiledFace precompiledFace;
    private PermissionFace permissionFace;
    private ConsoleContractFace consoleContractFace;

    public void init(String[] args) throws ConfigException {
        String configFile =
                ConsoleInitializer.class
                        .getClassLoader()
                        .getResource("config-example.yaml")
                        .getPath();

        bcosSDK = new BcosSDK(configFile);
        // default group id is 1
        Integer groupId = Integer.valueOf(1);
        if (args.length > 0) {
            groupId = Integer.valueOf(args[0]);
        }
        this.client = bcosSDK.getClient(groupId);
        try {
            this.consoleClientFace = new ConsoleClientImpl(client);
            this.precompiledFace = new PrecompiledImpl(client);
            this.permissionFace = new PermissionImpl(client);
            this.consoleContractFace = new ConsoleContractImpl(client);
        } catch (Exception e) {
            System.out.println(
                    "Failed to connect to the node. Please check the node status and the console configuration.");
            logger.error(" message: {}, e: {}", e.getMessage(), e);
        }
    }

    public void switchGroupID(String[] params) {
        if (params.length < 2) {
            HelpInfo.promptHelp("switch");
            return;
        }
        if (params.length > 2) {
            HelpInfo.promptHelp("switch");
            return;
        }
        String groupIDStr = params[1];
        if ("-h".equals(groupIDStr) || "--help".equals(groupIDStr)) {
            HelpInfo.switchGroupIDHelp();
            return;
        }
        int toGroupID = 1;
        try {
            toGroupID = Integer.parseInt(groupIDStr);
            if (toGroupID <= 0 || toGroupID > Common.MaxGroupID) {
                System.out.println(
                        "Please provide group ID by positive integer mode, "
                                + Common.GroupIDRange
                                + ".");
                System.out.println();
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println(
                    "Please provide group ID by positive integer mode, "
                            + Common.GroupIDRange
                            + ".");
            System.out.println();
            return;
        }
        try {
            this.client = bcosSDK.getClient(toGroupID);
            this.consoleClientFace = new ConsoleClientImpl(client);
            this.precompiledFace = new PrecompiledImpl(client);
            this.permissionFace = new PermissionImpl(client);
            this.consoleContractFace = new ConsoleContractImpl(client);
            System.out.println("Switched to group " + toGroupID + ".");
            System.out.println();
        } catch (Exception e) {
            System.out.println(
                    "Switch to group "
                            + toGroupID
                            + " failed! "
                            + e.getMessage()
                            + " Current groupList is: "
                            + client.getGroupList().getGroupList().toString()
                            + ", please check the existence of group "
                            + toGroupID);
        }
    }

    public void stop() {
        this.bcosSDK.stopAll();
    }

    public Client getClient() {
        return client;
    }

    public BcosSDK getBcosSDK() {
        return bcosSDK;
    }

    public CryptoInterface getCredentials() {
        return client.getCryptoInterface();
    }

    public int getGroupID() {
        return this.client.getGroupId();
    }

    public ConsoleClientFace getConsoleClientFace() {
        return consoleClientFace;
    }

    public PrecompiledFace getPrecompiledFace() {
        return precompiledFace;
    }

    public PermissionFace getPermissionFace() {
        return permissionFace;
    }

    public ConsoleContractFace getConsoleContractFace() {
        return consoleContractFace;
    }
}
