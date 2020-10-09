package console;

import console.client.ConsoleClientFace;
import console.client.ConsoleClientImpl;
import console.common.Common;
import console.common.ConsoleUtils;
import console.contract.ConsoleContractFace;
import console.contract.ConsoleContractImpl;
import console.precompiled.PrecompiledFace;
import console.precompiled.PrecompiledImpl;
import console.precompiled.permission.PermissionFace;
import console.precompiled.permission.PermissionImpl;
import java.io.Console;
import java.io.File;
import java.net.URL;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.config.exceptions.ConfigException;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.model.CryptoType;
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
    public static boolean DisableAutoCompleter = false;

    public void init(String[] args) throws ConfigException {
        Integer groupId = Integer.valueOf(1);
        AccountInfo accountInfo = null;
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
            bcosSDK = BcosSDK.build(configFile);
            // bash start.sh -l
            if (args.length == 1) {
                if ("-l".equals(args[0])) { // input by scanner for log
                    DisableAutoCompleter = true;
                } else {
                    groupId = Integer.valueOf(args[0]);
                }
            }
            // bash start.sh groupID -l
            if (args.length >= 2) {
                groupId = Integer.valueOf(args[0]);
                if ("-l".equals(args[1])) { // input by scanner for log
                    DisableAutoCompleter = true;
                }
            }
            if (args.length == 3) {
                accountInfo = loadAccount(bcosSDK, args);
            }
        } catch (NumberFormatException e) {
            System.out.println("Init BcosSDK failed for invalid groupId \"" + args[0] + "\"");
            System.out.println();
            System.exit(0);
        }
        try {
            this.client = bcosSDK.getClient(groupId);
            if (accountInfo != null) {
                this.client
                        .getCryptoSuite()
                        .loadAccount(
                                accountInfo.accountFileFormat,
                                accountInfo.accountFile,
                                accountInfo.password);
            }
            this.consoleClientFace = new ConsoleClientImpl(client);
            this.precompiledFace = new PrecompiledImpl(client);
            this.permissionFace = new PermissionImpl(client);
            this.consoleContractFace = new ConsoleContractImpl(client);
        } catch (Exception e) {
            System.out.println(
                    "Failed to create BcosSDK failed! Please check the node status and the console configuration, error info: "
                            + e.getMessage());
            logger.error(" message: {}, e: {}", e.getMessage(), e);
            System.exit(0);
        }
    }

    private class AccountInfo {
        private String accountFileFormat;
        private String accountFile;
        private String password;

        public AccountInfo(String accountFileFormat, String accountFile, String password) {
            this.accountFile = accountFile;
            this.accountFileFormat = accountFileFormat;
            this.password = password;
        }

        public String getAccountFileFormat() {
            return accountFileFormat;
        }

        public void setAccountFileFormat(String accountFileFormat) {
            this.accountFileFormat = accountFileFormat;
        }

        public String getAccountFile() {
            return accountFile;
        }

        public void setAccountFile(String accountFile) {
            this.accountFile = accountFile;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    private AccountInfo loadAccount(BcosSDK bcosSDK, String[] params) {
        if (params.length <= 1) {
            return null;
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
        bcosSDK.getConfig().getAccountConfig().clearAccount();
        return new AccountInfo(accountFileFormat, accountFile, password);
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
            // load the original account
            CryptoKeyPair cryptoKeyPair = this.client.getCryptoSuite().getCryptoKeyPair();
            this.client = bcosSDK.getClient(toGroupID);
            this.client.getCryptoSuite().setCryptoKeyPair(cryptoKeyPair);
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

    public void loadAccount(String[] params) throws Exception {
        String accountPath = params[1];
        String accountFormat = params[2];
        if (!accountFormat.equals("pem") && !accountFormat.equals("p12")) {
            System.out.println(
                    "Load account failed! Only support \"pem\" and \"p12\" account now!");
            return;
        }
        if (!new File(accountPath).exists()) {
            // try to load the account from the given address
            if (accountFormat.equals("pem")) {
                accountPath =
                        client.getCryptoSuite()
                                .getCryptoKeyPair()
                                .getPemKeyStoreFilePath(accountPath);
                logger.debug("pemAccountPath: {}", accountPath);
            }
            if (accountFormat.equals("p12")) {
                accountPath =
                        client.getCryptoSuite()
                                .getCryptoKeyPair()
                                .getP12KeyStoreFilePath(accountPath);
                logger.debug("p12AccountPath: {}", accountPath);
            }
            if (!new File(accountPath).exists()) {
                if (client.getCryptoType() == CryptoType.SM_TYPE) {
                    String[] accountPathArray = accountPath.split("\\.");
                    if (accountPathArray.length > 1) {
                        accountPath =
                                accountPathArray[0]
                                        + ConsoleUtils.GM_ACCOUNT_POSTFIX
                                        + "."
                                        + accountPathArray[1];
                    }
                }
                if (!new File(accountPath).exists()) {
                    System.out.println("The account file " + accountPath + " doesn't exist!");
                    return;
                }
            }
        }
        String accountPassword = null;
        if (accountFormat.equals("p12")) {
            System.out.print("Enter p12 Password:");
            Console cons = System.console();
            char[] passwd = cons.readPassword();
            accountPassword = new String(passwd);
        }
        CryptoSuite cryptoSuite = client.getCryptoSuite();
        cryptoSuite.loadAccount(accountFormat, accountPath, accountPassword);
        // update the objects with new CryptoKeyPair
        this.consoleClientFace = new ConsoleClientImpl(client);
        this.precompiledFace = new PrecompiledImpl(client);
        this.permissionFace = new PermissionImpl(client);
        this.consoleContractFace = new ConsoleContractImpl(client);
        System.out.println("Load account " + params[1] + " success!");
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
