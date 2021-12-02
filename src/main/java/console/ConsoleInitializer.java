package console;

import console.auth.AuthFace;
import console.auth.AuthImpl;
import console.client.ConsoleClientFace;
import console.client.ConsoleClientImpl;
import console.collaboration.CollaborationFace;
import console.collaboration.CollaborationImpl;
import console.common.ConsoleUtils;
import console.contract.ConsoleContractFace;
import console.contract.ConsoleContractImpl;
import console.precompiled.PrecompiledFace;
import console.precompiled.PrecompiledImpl;
import java.io.Console;
import java.io.File;
import java.net.URL;
import java.util.List;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.config.ConfigOption;
import org.fisco.bcos.sdk.config.exceptions.ConfigException;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.crypto.exceptions.LoadKeyStoreException;
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
    private ConsoleContractFace consoleContractFace;
    private AuthFace authFace;
    private CollaborationFace collaborationFace;
    public static boolean DisableAutoCompleter = false;
    private String nodeName = "";

    public void init(String[] args) throws ConfigException {
        AccountInfo accountInfo = null;
        String groupID = null;
        if (args.length < 1) {
            logger.info("Did not set group, use default group in config.");
        } else {
            groupID = args[0];
        }

        if (args.length > 1 && "-l".equals(args[1])) { // input by scanner for log
            DisableAutoCompleter = true;
        }

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
            ConfigOption config = bcosSDK.getConfig();
            List<String> peers = config.getNetworkConfig().getPeers();
            if (peers.size() == 0) {
                System.out.println(
                        "Init BcosSDK failed for the empty peer size of the configuration file: "
                                + configFile);
                System.out.println();
                System.exit(0);
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
            this.client = groupID == null ? bcosSDK.getClient() : bcosSDK.getClient(groupID);
            if (accountInfo != null) {
                this.client
                        .getCryptoSuite()
                        .loadAccount(
                                accountInfo.accountFileFormat,
                                accountInfo.accountFile,
                                accountInfo.password);
            } else if (!client.getCryptoSuite()
                    .getConfig()
                    .getAccountConfig()
                    .isAccountConfigured()) {
                try {
                    accountInfo = loadAccountRandomly(bcosSDK, client);
                    if (accountInfo != null) {
                        this.client
                                .getCryptoSuite()
                                .loadAccount(
                                        accountInfo.accountFileFormat,
                                        accountInfo.accountFile,
                                        accountInfo.password);
                    }
                } catch (LoadKeyStoreException e) {
                    logger.warn(
                            "loadAccountRandomly failed, try to generate and load the random account, error info: {}",
                            e.getMessage(),
                            e);
                }
                if (accountInfo == null) {
                    // save the keyPair
                    client.getCryptoSuite().getCryptoKeyPair().storeKeyPairWithPemFormat();
                }
            }
            this.consoleClientFace = new ConsoleClientImpl(client);
            this.precompiledFace = new PrecompiledImpl(client);
            this.consoleContractFace = new ConsoleContractImpl(client);
            this.collaborationFace = new CollaborationImpl(client);
            this.authFace = new AuthImpl(client);

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

    private AccountInfo loadAccountRandomly(BcosSDK bcosSDK, Client client) {
        ConfigOption config = bcosSDK.getConfig();
        if (config.getAccountConfig() == null) {
            return null;
        }
        String keyStoreDir = config.getAccountConfig().getKeyStoreDir();
        File keyStoreDirPath = new File(keyStoreDir);
        if (!keyStoreDirPath.exists() || !keyStoreDirPath.isDirectory()) {
            return null;
        }
        String subDir = client.getCryptoSuite().getKeyPairFactory().getKeyStoreSubDir();
        String keyStoreFileDir = keyStoreDirPath + File.separator + subDir;
        File keyStoreFileDirPath = new File(keyStoreFileDir);
        logger.debug("loadAccountRandomly, keyStoreFileDirPath:{}", keyStoreFileDir);
        if (!keyStoreFileDirPath.exists() || !keyStoreFileDirPath.isDirectory()) {
            return null;
        }
        // load account from the keyStoreDir
        File[] accountFileList = keyStoreFileDirPath.listFiles();
        ConsoleUtils.sortFiles(accountFileList);
        for (File accountFile : accountFileList) {
            if (accountFile.getName().endsWith(".pem") && !accountFile.getName().contains("pub")) {
                logger.debug("load pem account from {}", accountFile.getAbsoluteFile());
                return new AccountInfo("pem", accountFile.getAbsolutePath(), null);
            }
        }
        return null;
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

    public void switchGroup(String[] params) {
        String group = params[1];
        try {
            // load the original account
            CryptoKeyPair cryptoKeyPair = this.client.getCryptoSuite().getCryptoKeyPair();
            this.client = bcosSDK.getClient(group);
            if (this.client == null) {
                System.out.println("Switch to the group " + group + " failed");
                System.exit(0);
            }
            this.client.getCryptoSuite().setCryptoKeyPair(cryptoKeyPair);
            this.consoleClientFace = new ConsoleClientImpl(client);
            this.precompiledFace = new PrecompiledImpl(client);
            this.consoleContractFace = new ConsoleContractImpl(client);
            this.collaborationFace = new CollaborationImpl(client);
            this.authFace = new AuthImpl(client);
            System.out.println("Switched to group " + group + ".");
            System.out.println();
        } catch (Exception e) {
            System.out.println(
                    "Switch to group "
                            + group
                            + " failed! "
                            + e.getMessage()
                            + ", please check the existence of the group "
                            + group);
        }
    }

    public void loadAccount(String[] params) throws Exception {
        String accountPath = params[1];
        String accountFormat = "pem";
        if (params.length >= 3) {
            accountFormat = params[2];
        }
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
        // update the objects with new CryptoKeyPair
        cryptoSuite.loadAccount(accountFormat, accountPath, accountPassword);
        this.consoleClientFace = new ConsoleClientImpl(client);
        this.precompiledFace = new PrecompiledImpl(client);
        this.consoleContractFace = new ConsoleContractImpl(client);
        this.collaborationFace = new CollaborationImpl(client);
        this.authFace = new AuthImpl(client);
        System.out.println("Load account " + params[1] + " success!");
    }

    public void stop() {
        // this.bcosSDK.stopAll();
        logger.info("stop console initializer");
        System.exit(0);
    }

    public Client getClient() {
        return client;
    }

    public BcosSDK getBcosSDK() {
        return bcosSDK;
    }

    public String getGroupID() {
        return this.client.getGroup();
    }

    public ConsoleClientFace getConsoleClientFace() {
        return consoleClientFace;
    }

    public PrecompiledFace getPrecompiledFace() {
        return precompiledFace;
    }

    public ConsoleContractFace getConsoleContractFace() {
        return consoleContractFace;
    }

    public CollaborationFace getCollaborationFace() {
        return collaborationFace;
    }

    public AuthFace getAuthFace() {
        return authFace;
    }
}
