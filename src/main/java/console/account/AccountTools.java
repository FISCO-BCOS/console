package console.account;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.InvalidKeySpecException;
import org.bouncycastle.jce.spec.ECNamedCurveSpec;
import org.fisco.bcos.channel.client.P12Manager;
import org.fisco.bcos.channel.client.PEMManager;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.ECKeyPair;
import org.fisco.bcos.web3j.crypto.EncryptType;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountTools {

    private static final Logger logger = LoggerFactory.getLogger(AccountTools.class);

    /** @return */
    public static Account newAccount() {
        Credentials credentials = GenCredential.create();
        Account account = new Account(credentials);
        account.setNewAccount(true);
        account.setPrivateKeyType(EncryptType.encryptType);
        logger.info(" newAccount: {}", account);
        return account;
    }

    /**
     * @param accountPath
     * @param password
     * @return
     * @throws UnrecoverableKeyException
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws NoSuchProviderException
     * @throws CertificateException
     * @throws IOException
     */
    public static Account loadAccount(String accountPath, String password)
            throws UnrecoverableKeyException, InvalidKeySpecException, NoSuchAlgorithmException,
                    KeyStoreException, NoSuchProviderException, CertificateException, IOException {
        ECKeyPair keyPair = null;
        int privateKeyType = 0;

        if (accountPath.endsWith("p12")) {
            // p12
            P12Manager p12Manager = new P12Manager();
            p12Manager.setP12File(accountPath);
            p12Manager.setPassword(password);
            p12Manager.load();
            keyPair = p12Manager.getECKeyPair();
            privateKeyType = getPrivateKeyType((ECPrivateKey) p12Manager.getPrivateKey());
        } else {
            // pem
            PEMManager pem = new PEMManager();
            pem.setPemFile(accountPath);
            pem.load();
            keyPair = pem.getECKeyPair();
            privateKeyType = getPrivateKeyType((ECPrivateKey) pem.getPrivateKey());
        }

        Credentials credentials = GenCredential.create(keyPair.getPrivateKey().toString(16));
        logger.info(
                " loadAccount accountFile: {}, address: {}", accountPath, credentials.getAddress());

        Account account = new Account(credentials);
        account.setPrivateKeyType(privateKeyType);
        account.setNewAccount(false);

        return account;
    }

    /**
     * @param
     * @param credentials
     */
    public static void saveAccount(Credentials credentials, String dirPath) {
        /*
        String fileName = credentials.getAddress() + (isGMAccount(credentials)? "_gm.pem" : ".pem");
        String path = dirPath + File.pathSeparator + fileName;
        logger.info(" saveAccount, account: {}, path: {}", credentials.getAddress(), path);

        ECPrivateKeySpec secretKeySpec =
                new ECPrivateKeySpec(credentials.getEcKeyPair().getPrivateKey(), ECCParams.ecNamedCurveSpec);
        BCECPrivateKey bcecPrivateKey =
                new BCECPrivateKey("ECDSA", secretKeySpec, BouncyCastleProvider.CONFIGURATION);
                */
        return;
    }

    public static String getPrivateKeyTypeAsString(int encryptType) {
        switch (encryptType) {
            case EncryptType.SM2_TYPE:
                return "sm";
            case EncryptType.ECDSA_TYPE:
                return "ecdsa";
            default:
                {
                    return "unknown";
                }
        }
    }

    public static int getPrivateKeyType(ECPrivateKey ecPrivateKey) {

        ECParameterSpec ecParameterSpec = ecPrivateKey.getParams();
        String name = ((ECNamedCurveSpec) ecParameterSpec).getName();

        if (logger.isTraceEnabled()) {
            logger.trace(" private key type name: {}", name);
        }

        if (name.contains("sm2")) {
            return EncryptType.SM2_TYPE;
        } else if (name.contains("secp256k1")) {
            return EncryptType.ECDSA_TYPE;
        }

        throw new UnsupportedOperationException("Unsupported private key type, name: " + name);
    }
}
