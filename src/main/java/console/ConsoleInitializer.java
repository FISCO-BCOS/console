package console;

import console.account.Account;
import console.account.AccountImpl;
import console.account.AccountInterface;
import console.account.AccountManager;
import console.account.AccountTools;
import console.common.Common;
import console.common.ContractClassFactory;
import console.common.DeployContractManager;
import console.common.HelpInfo;
import console.common.PathUtils;
import console.contract.ContractFace;
import console.contract.ContractImpl;
import console.precompiled.PrecompiledFace;
import console.precompiled.PrecompiledImpl;
import console.precompiled.permission.PermissionFace;
import console.precompiled.permission.PermissionImpl;
import console.web3j.Web3jFace;
import console.web3j.Web3jImpl;
import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.spec.InvalidKeySpecException;
import org.fisco.bcos.channel.client.P12Manager;
import org.fisco.bcos.channel.client.PEMManager;
import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.ECKeyPair;
import org.fisco.bcos.web3j.crypto.EncryptType;
import org.fisco.bcos.web3j.precompile.common.PrecompiledCommon;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.channel.ChannelEthereumService;
import org.fisco.bcos.web3j.protocol.channel.ResponseExcepiton;
import org.fisco.bcos.web3j.protocol.core.methods.response.NodeVersion.Version;
import org.fisco.bcos.web3j.tx.gas.StaticGasProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractRefreshableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ConsoleInitializer {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleInitializer.class);

    private ChannelEthereumService channelEthereumService;
    private ApplicationContext context;
    private StaticGasProvider gasProvider =
            new StaticGasProvider(new BigInteger("300000000"), new BigInteger("300000000"));
    private Web3j web3j = null;
    private Account account = null;;
    private AccountManager accountManager = null;
    private DeployContractManager deployContractManager = null;
    private int groupID;
    public static final int InvalidRequest = 40009;
    public static final String ACCOUNT_DIR1 = "accounts/";
    public static final String ACCOUNT_DIR2 = "./accounts/";

    private Web3jFace web3jFace;
    private PrecompiledFace precompiledFace;
    private PermissionFace permissionFace;
    private ContractFace contractFace;
    private AccountInterface accountInterface;

    public void init(String[] args)
            throws InvalidAlgorithmParameterException, NoSuchAlgorithmException,
                    NoSuchProviderException, UnrecoverableKeyException, KeyStoreException,
                    InvalidKeySpecException {
        context = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
        Service service = context.getBean(Service.class);
        groupID = service.getGroupId();
        accountManager = new AccountManager();
        deployContractManager = DeployContractManager.newGroupDeployedContractManager();

        switch (args.length) {
            case 0: // bash start.sh
                account = AccountTools.newAccount();
                AccountTools.saveAccount(account, PathUtils.ACCOUNT_DIRECTORY);
                break;
            case 1: // bash start.sh groupID
                if ("-l".equals(args[0])) { // input by scanner for log
                    ConsoleClient.INPUT_FLAG = 1;
                } else {
                    groupID = setGroupID(args[0]);
                }
                account = AccountTools.newAccount();
                AccountTools.saveAccount(account, PathUtils.ACCOUNT_DIRECTORY);
                break;
            case 2: // bash start.sh groupID -l
                if ("-l".equals(args[1])) { // input by scanner for log
                    ConsoleClient.INPUT_FLAG = 1;
                    groupID = setGroupID(args[0]);
                } else {
                    HelpInfo.startHelp();
                    close();
                }
                account = AccountTools.newAccount();
                AccountTools.saveAccount(account, PathUtils.ACCOUNT_DIRECTORY);
                break;
            case 3: // ./start.sh groupID -pem pemName
                handleAccountParam(args);
                break;
            default:
                if (args.length == 4 && "-l".equals(args[3])) {
                    handleAccountParam(args);
                    ConsoleClient.INPUT_FLAG = 1;
                } else {
                    HelpInfo.startHelp();
                    close();
                }
        }

        if (account == null) {
            System.out.println("Please provide a valid account.");
            close();
        }

        if (!account.isTypeMatchingAccount()) {
            System.out.println(
                    " the loading private key is not available, private key type:"
                            + AccountTools.getPrivateKeyTypeAsString(account.getPrivateKeyType())
                            + " ,console configuration encryptType: "
                            + AccountTools.getPrivateKeyTypeAsString(EncryptType.encryptType));
        }

        service.setGroupId(groupID);
        try {
            service.run();
        } catch (Exception e) {
            logger.error(" message: {}, e: {}", e.getMessage(), e);
            System.out.println("Failed to init the console！ " + e.getMessage());
            close();
        }

        try {
            ContractClassFactory.initClassLoad();
        } catch (MalformedURLException e) {
            logger.error(" message: {}, e: {}", e.getMessage(), e);
            System.out.println("Failed to init class load, " + e.getMessage());
            close();
        }

        channelEthereumService = new ChannelEthereumService();
        channelEthereumService.setChannelService(service);
        channelEthereumService.setTimeout(60000);
        web3j = Web3j.build(channelEthereumService, groupID);
        try {
            web3j.getBlockNumber().sendForReturnString();

            Version nodeVersion = web3j.getNodeVersion().send().getNodeVersion();
            String version = nodeVersion.getSupportedVersion();
            PrecompiledCommon.BCOS_VERSION = version;
            if (version == null || PrecompiledCommon.BCOS_RC1.equals(version)) {
                Common.PermissionCode = PrecompiledCommon.PermissionDenied_RC1;
            } else if (PrecompiledCommon.BCOS_RC2.equals(version)) {
                Common.PermissionCode = PrecompiledCommon.PermissionDenied;
                Common.TableExist = PrecompiledCommon.TableExist;
            } else {
                Common.PermissionCode = PrecompiledCommon.PermissionDenied_RC3;
                Common.TableExist = PrecompiledCommon.TableExist_RC3;
            }

            accountManager.addAccount(account);
            accountManager.setCurrentAccount(account);

            web3jFace = new Web3jImpl();
            web3jFace.setWeb3j(web3j);
            web3jFace.setGasProvider(gasProvider);
            web3jFace.setAccountManager(accountManager);

            precompiledFace = new PrecompiledImpl();
            precompiledFace.setWeb3j(web3j);
            precompiledFace.setAccountManager(accountManager);

            permissionFace = new PermissionImpl();
            permissionFace.setWeb3j(web3j);
            permissionFace.setAccountManager(accountManager);

            contractFace = new ContractImpl();
            contractFace.setGroupID(groupID);
            contractFace.setGasProvider(gasProvider);
            contractFace.setWeb3j(web3j);
            contractFace.setAccountManager(accountManager);
            contractFace.setDeployContractManager(deployContractManager);

            accountInterface = new AccountImpl();
            accountInterface.setAccountManager(accountManager);

            deployContractManager.setGroupId(String.valueOf(groupID));
        } catch (ResponseExcepiton e) {
            if (e.getCode() == InvalidRequest) {
                System.out.println("Don't connect a removed node.");
            } else {
                System.out.println(e.getMessage());
                logger.error(" message: {}, e: {}", e.getMessage(), e);
            }
            close();
        } catch (Exception e) {
            System.out.println(
                    "Failed to connect to the node. Please check the node status and the console configuration.");
            logger.error(" message: {}, e: {}", e.getMessage(), e);
            close();
        }
    }

    private void handleAccountParam(String[] args)
            throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException,
                    InvalidKeySpecException, NoSuchProviderException,
                    InvalidAlgorithmParameterException {
        if ("-pem".equals(args[1])) {
            groupID = setGroupID(args[0]);
            String pemName = args[2];
            PEMManager pem = new PEMManager();

            InputStream in = readAccountFile(pemName);
            try {
                pem.load(in);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                logger.error(" message: {}, e: {}", e.getMessage(), e);
                close();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                        logger.error(" message: {}, e: {}", e.getMessage(), e);
                    }
                }
            }

            ECKeyPair keyPair = pem.getECKeyPair();
            Credentials credentials = Credentials.create(keyPair);
            account = new Account(credentials);
            account.setPrivateKeyType(AccountTools.getPrivateKeyType(pem.getPrivateKey()));
        } else if ("-p12".equals(args[1])) {
            groupID = setGroupID(args[0]);
            String p12Name = args[2];

            InputStream in = readAccountFile(p12Name);
            if (null == in) {
                return;
            }

            System.out.print("Enter Export Password:");
            Console cons = System.console();
            char[] passwd = cons.readPassword();
            String password = new String(passwd);
            P12Manager p12Manager = new P12Manager();
            p12Manager.setPassword(password);

            try {
                p12Manager.load(in, password);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                logger.error(" message: {}, e: {}", e.getMessage(), e);
                close();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                        logger.error(" message: {}, e: {}", e.getMessage(), e);
                    }
                }
            }

            ECKeyPair keyPair;
            try {
                keyPair = p12Manager.getECKeyPair();
                Credentials credentials = Credentials.create(keyPair);
                account = new Account(credentials);
                account.setPrivateKeyType(
                        AccountTools.getPrivateKeyType(p12Manager.getPrivateKey()));
            } catch (Exception e) {
                System.out.println("The name for p12 account is error.");
                close();
            }
        } else if ("-l".equals(args[1])) {
            groupID = setGroupID(args[0]);
            ConsoleClient.INPUT_FLAG = 1;
        } else {
            HelpInfo.startHelp();
            close();
        }
    }

    private InputStream readAccountFile(String fileName) {

        try {
            return Files.newInputStream(Paths.get(fileName));
        } catch (IOException e) {
            System.out.println(
                    "["
                            + Paths.get(fileName).toAbsolutePath()
                            + "]"
                            + " cannot be opened because it does not exist.");
            close();
        }
        return null;
    }

    public void switchGroupID(String[] params) throws IOException {
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
        ((AbstractRefreshableApplicationContext) context).refresh();
        Service service = context.getBean(Service.class);
        service.setGroupId(toGroupID);
        try {
            service.run();
            groupID = toGroupID;
        } catch (Exception e) {
            System.out.println("Switch to group " + toGroupID + " failed! " + e.getMessage());
            logger.error(" message: {}, e: {}", e.getMessage(), e);
            System.out.println();
            service.setGroupId(groupID);
            try {
                service.run();
            } catch (Exception e1) {
                logger.error(" message: {}, e: {}", e1.getMessage(), e1);
            }
            return;
        }
        ChannelEthereumService channelEthereumService = new ChannelEthereumService();
        channelEthereumService.setChannelService(service);
        channelEthereumService.setTimeout(60000);
        web3j = Web3j.build(channelEthereumService, groupID);

        web3jFace.setWeb3j(web3j);
        precompiledFace.setWeb3j(web3j);
        permissionFace.setWeb3j(web3j);
        contractFace.setWeb3j(web3j);
        contractFace.setGroupID(groupID);
        deployContractManager.setGroupId(String.valueOf(groupID));

        System.out.println("Switched to group " + groupID + ".");
        System.out.println();
    }

    private int setGroupID(String groupIDStr) {
        try {
            groupID = Integer.parseInt(groupIDStr);
            if (groupID <= 0 || groupID > Common.MaxGroupID) {
                System.out.println(
                        "Please provide groupID by non-negative integer mode, "
                                + Common.GroupIDRange
                                + ".");
                close();
            }
        } catch (NumberFormatException e) {
            System.out.println(
                    "Please provide groupID by non-negative integer mode, "
                            + Common.GroupIDRange
                            + ".");
            close();
        }
        return groupID;
    }

    public void close() {
        try {
            if (channelEthereumService != null) {
                channelEthereumService.close();
            }
            System.exit(0);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            logger.error(" message: {}, e: {}", e.getMessage(), e);
        }
    }

    public ApplicationContext getContext() {
        return context;
    }

    public void setContext(ApplicationContext context) {
        this.context = context;
    }

    public StaticGasProvider getGasProvider() {
        return gasProvider;
    }

    public Web3j getWeb3j() {
        return web3j;
    }

    public void setWeb3j(Web3j web3j) {
        this.web3j = web3j;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public int getGroupID() {
        return this.groupID;
    }

    public Web3jFace getWeb3jFace() {
        return web3jFace;
    }

    public PrecompiledFace getPrecompiledFace() {
        return precompiledFace;
    }

    public PermissionFace getPermissionFace() {
        return permissionFace;
    }

    public ContractFace getContractFace() {
        return contractFace;
    }

    public AccountManager getAccountManager() {
        return accountManager;
    }

    public void setAccountManager(AccountManager accountManager) {
        this.accountManager = accountManager;
    }

    public AccountInterface getAccountInterface() {
        return accountInterface;
    }

    public void setAccountInterface(AccountInterface accountInterface) {
        this.accountInterface = accountInterface;
    }
}
