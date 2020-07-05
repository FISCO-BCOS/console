package console;

import console.common.Common;
import console.common.ContractClassFactory;
import console.common.HelpInfo;
import console.contract.ContractFace;
import console.contract.ContractImpl;
import console.data.DataEscrowFace;
import console.data.DataEscrowImpl;
import console.data.tools.SafeKeeperUrl;
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
import java.security.interfaces.ECPrivateKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.InvalidKeySpecException;
import org.bouncycastle.jce.spec.ECNamedCurveSpec;
import org.fisco.bcos.channel.client.P12Manager;
import org.fisco.bcos.channel.client.PEMManager;
import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.ECKeyPair;
import org.fisco.bcos.web3j.crypto.EncryptType;
import org.fisco.bcos.web3j.crypto.Keys;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
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
import org.springframework.web.client.HttpClientErrorException;

public class ConsoleInitializer {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleInitializer.class);

    private ChannelEthereumService channelEthereumService;
    private ApplicationContext context;
    private ECKeyPair keyPair;
    private StaticGasProvider gasProvider =
            new StaticGasProvider(new BigInteger("300000000"), new BigInteger("300000000"));
    private Web3j web3j = null;
    private Credentials credentials;
    private String privateKey = "";
    private int groupID;
    public static final int InvalidRequest = 40009;
    public static final String ACCOUNT_DIR1 = "accounts/";
    public static final String ACCOUNT_DIR2 = "./accounts/";

    private Web3jFace web3jFace;
    private PrecompiledFace precompiledFace;
    private PermissionFace permissionFace;
    private ContractFace contractFace;
    private DataEscrowFace dataEscrowFace;
    private String accountInfo;

    public int init(String[] args)
            throws InvalidAlgorithmParameterException, NoSuchAlgorithmException,
                    NoSuchProviderException, UnrecoverableKeyException, KeyStoreException,
                    InvalidKeySpecException {
        context = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
        Service service = context.getBean(Service.class);
        groupID = service.getGroupId();

        if (args.length > 0 && "-safekeeper".equals(args[0])) {
            if (args.length == 3) {
                try {
                    dataEscrowFace = new DataEscrowImpl();
                    SafeKeeperUrl safekeeperUrl = context.getBean(SafeKeeperUrl.class);
                    String urlPrefix = "https://" + safekeeperUrl.getUrl() + safekeeperUrl.getServer();
                    logger.info(" safekeeper service url prefix: {}", urlPrefix);
                    dataEscrowFace.setURLPrefix(urlPrefix);
                    accountInfo = dataEscrowFace.login(args);
                } catch (HttpClientErrorException e) {
                    System.out.println("Fail, " + e.getResponseBodyAsString());
                    logger.error(" message: {}, e: {}", e.getMessage(), e);
                    close();
                } catch (Exception e) {
                    System.out.println("Fail, " + e.getMessage());
                    logger.error(" message: {}, e: {}", e.getMessage(), e);
                    close();
                }
                return Common.INIT_SAFEKEEPER;
            } else {
                HelpInfo.startHelp();
                close();
            }
        }

        switch (args.length) {
            case 0: // bash start.sh
                useDefaultCredentials();
                break;
            case 1: // bash start.sh groupID
                if ("-l".equals(args[0])) { // input by scanner for log
                    ConsoleClient.INPUT_FLAG = 1;
                } else {
                    groupID = setGroupID(args[0]);
                }
                useDefaultCredentials();
                break;
            case 2: // bash start.sh groupID -l
                if ("-l".equals(args[1])) { // input by scanner for log
                    ConsoleClient.INPUT_FLAG = 1;
                    groupID = setGroupID(args[0]);
                    useDefaultCredentials();
                } else {
                    HelpInfo.startHelp();
                    close();
                }
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

        if (credentials == null) {
            System.out.println("Please provide a valid account.");
            close();
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
            web3jFace = new Web3jImpl();
            web3jFace.setWeb3j(web3j);
            web3jFace.setGasProvider(gasProvider);
            web3jFace.setCredentials(credentials);

            precompiledFace = new PrecompiledImpl();
            precompiledFace.setWeb3j(web3j);
            precompiledFace.setCredentials(credentials);

            permissionFace = new PermissionImpl();
            permissionFace.setWeb3j(web3j);
            permissionFace.setCredentials(credentials);

            contractFace = new ContractImpl();
            contractFace.setGroupID(groupID);
            contractFace.setGasProvider(gasProvider);
            contractFace.setCredentials(credentials);
            contractFace.setWeb3j(web3j);

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

        return Common.INIT_CHAIN;
    }

    private void checkAccountPrivateKey(ECParameterSpec ecParameterSpec, int encryptType) {

        String name = ((ECNamedCurveSpec) ecParameterSpec).getName();
        boolean accountPrivateKeyMatch =
                ((EncryptType.ECDSA_TYPE != EncryptType.encryptType) && name.contains("sm2"))
                        || ((EncryptType.ECDSA_TYPE == EncryptType.encryptType)
                                && name.contains("secp256k1"));

        logger.info(" name: {}, encrypt: {}", name, encryptType);

        if (!accountPrivateKeyMatch) {
            if (EncryptType.ECDSA_TYPE == EncryptType.encryptType) {
                throw new IllegalArgumentException(
                        " Load SM2 escrow data while configuration is ECSDA type");
            } else {
                throw new IllegalArgumentException(
                        " Load ECSDA escrow data while configuration is SM2 type");
            }
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
            credentials = Credentials.create(keyPair);

            checkAccountPrivateKey(
                    ((ECPrivateKey) pem.getPrivateKey()).getParams(), EncryptType.encryptType);
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
                credentials = Credentials.create(keyPair);
            } catch (Exception e) {
                System.out.println("The name for p12 account is error.");
                close();
            }

            checkAccountPrivateKey(
                    ((ECPrivateKey) p12Manager.getPrivateKey()).getParams(),
                    EncryptType.encryptType);
        } else if ("-l".equals(args[1])) {
            groupID = setGroupID(args[0]);
            ConsoleClient.INPUT_FLAG = 1;
            useDefaultCredentials();
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

    private void useDefaultCredentials()
            throws InvalidAlgorithmParameterException, NoSuchAlgorithmException,
                    NoSuchProviderException {
        keyPair = Keys.createEcKeyPair();
        privateKey = keyPair.getPrivateKey().toString(16);
        credentials = GenCredential.create(privateKey);
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

    public Credentials getCredentials() {
        return credentials;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    public int getGroupID() {
        return this.groupID;
    }

    public void setGroupID(int groupID) {
        this.groupID = groupID;
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

    public DataEscrowFace getKeyFace() {
        return dataEscrowFace;
    }

    public String getAccountInfo() {
        return accountInfo;
    }
}
