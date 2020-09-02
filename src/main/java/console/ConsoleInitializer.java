package console;

import console.client.ConsoleClientFace;
import console.client.ConsoleClientImpl;
import console.common.Common;
import console.contract.ConsoleContractFace;
import console.contract.ConsoleContractImpl;
import console.precompiled.PrecompiledFace;
import console.precompiled.PrecompiledImpl;
import console.precompiled.permission.PermissionFace;
import console.precompiled.permission.PermissionImpl;
import java.io.Console;
import java.net.URL;
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
        Integer groupId = Integer.valueOf(1);
        try {
            String configFileName = "config.toml";
            URL configUrl = ConsoleInitializer.class.getClassLoader().getResource(configFileName);
            if (configUrl == null) {
                throw new ConfigException(
                        "The configuration file "
                                + configFileName
                                + " doesn't exist! Please copy config-example.yaml to "
                                + configFileName
                                + ".");
            }
            String configFile = configUrl.getPath();
            bcosSDK = new BcosSDK(configFile);
            if (args.length > 0) {
                groupId = Integer.valueOf(args[0]);
            }
        } catch (NumberFormatException e) {
            System.out.println("Init BcosSDK failed for invalid groupId \"" + args[0] + "\"");
            System.out.println();
            System.exit(0);
        }
        try {
            this.client = bcosSDK.getClient(groupId);
            loadAccount(client, args);
            this.consoleClientFace = new ConsoleClientImpl(client);
            this.precompiledFace = new PrecompiledImpl(client);
            this.permissionFace = new PermissionImpl(client);
            this.consoleContractFace = new ConsoleContractImpl(client);
        } catch (Exception e) {
            System.out.println(
                    "Failed to create BcosSDK failed! Please check the node status and the console configuration, error info: "
                            + e.getMessage());
            System.exit(0);
            logger.error(" message: {}, e: {}", e.getMessage(), e);
        }
    }

    private void loadAccount(Client client, String[] params) {
        if (params.length <= 1) {
            return;
        }
        if (params[1].compareToIgnoreCase("-pem") != 0
                && params[1].compareToIgnoreCase("-p12") != 0) {
            System.out.println("Invalid param " + params[1] + ", must be -pem or -p12");
            System.exit(0);
        }
        if (params[1].compareToIgnoreCase("-pem") == 0 && params.length != 3) {
            System.out.println(
                    "Load account from the pem file failed! Please specified the pem file path");
            System.exit(0);
        }
        if (params[1].compareToIgnoreCase("-p12") == 0 && params.length != 3) {
            System.out.println(
                    "Load account from the p12 file failed! Please specified the p12 file path");
            System.exit(0);
        }
        String password = null;
        if (params[1].compareToIgnoreCase("-p12") == 0) {
            System.out.print("Enter p12 Password:");
            Console cons = System.console();
            char[] passwd = cons.readPassword();
            password = new String(passwd);
        }
        String accountFileFormat = params[1].substring(1);
        String accountFile = params[2];
        client.getCryptoInterface().loadAccount(accountFileFormat, accountFile, password);
    }

    public void switchGroupID(String[] params) {
        String groupIDStr = params[1];
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
