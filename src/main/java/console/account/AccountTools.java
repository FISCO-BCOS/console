package console.account;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.InvalidKeySpecException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveSpec;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.fisco.bcos.channel.client.P12Manager;
import org.fisco.bcos.channel.client.PEMManager;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.ECKeyPair;
import org.fisco.bcos.web3j.crypto.EncryptType;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.fisco.bcos.web3j.crypto.gm.sm2.crypto.asymmetric.SM2KeyGenerator;
import org.fisco.bcos.web3j.crypto.gm.sm2.crypto.asymmetric.SM2PrivateKey;
import org.fisco.bcos.web3j.crypto.gm.sm2.crypto.asymmetric.SM2PublicKey;
import org.fisco.bcos.web3j.crypto.gm.sm2.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountTools {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final Logger logger = LoggerFactory.getLogger(AccountTools.class);

    /** @return */
    public static Account newAccount()
            throws NoSuchAlgorithmException, NoSuchProviderException,
                    InvalidAlgorithmParameterException {

        KeyPair keyPair = null;
        ECKeyPair ecKeyPair = null;
        if (EncryptType.encryptType == EncryptType.SM2_TYPE) {
            SM2KeyGenerator generator = new SM2KeyGenerator();
            keyPair = generator.generateKeyPair();

            SM2PrivateKey sm2PrivateKey = (SM2PrivateKey) keyPair.getPrivate();
            SM2PublicKey sm2PublicKey = (SM2PublicKey) keyPair.getPublic();

            final byte[] privateKey = sm2PrivateKey.getEncoded();
            final byte[] publicKey = sm2PublicKey.getEncoded();

            BigInteger biPrivate = new BigInteger(Hex.toHexString(privateKey), 16);
            BigInteger biPublic = new BigInteger(Hex.toHexString(publicKey), 16);

            ecKeyPair = new ECKeyPair(biPrivate, biPublic);
        } else {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECDSA", "BC");
            ECGenParameterSpec ecGenParameterSpec = new ECGenParameterSpec("secp256k1");
            SecureRandom secureRandom = new SecureRandom();
            keyPairGenerator.initialize(ecGenParameterSpec, secureRandom);
            keyPair = keyPairGenerator.generateKeyPair();
            ecKeyPair = ECKeyPair.create(keyPair);
        }

        Credentials credentials = GenCredential.create(ecKeyPair); // GM or normal

        if (logger.isDebugEnabled()) {
            int type = getPrivateKeyType(keyPair.getPrivate());
            logger.debug(" type: {}", type);
        }

        Account account = new Account(credentials);
        account.setNewAccount(true);
        account.setKeyPair(keyPair);
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
        ECKeyPair ecKeyPair = null;
        int privateKeyType = 0;

        if (accountPath.endsWith("p12")) {
            // p12
            P12Manager p12Manager = new P12Manager();
            p12Manager.setP12File(accountPath);
            p12Manager.setPassword(password);
            p12Manager.load();
            ecKeyPair = p12Manager.getECKeyPair();
            privateKeyType = getPrivateKeyType(p12Manager.getPrivateKey());
        } else {
            // pem
            PEMManager pem = new PEMManager();
            pem.setPemFile(accountPath);
            pem.load();

            PrivateKey privateKey = pem.getPrivateKey();
            PublicKey publicKey = pem.getPublicKey();

            BigInteger biPrivate = new BigInteger(Hex.toHexString(privateKey.getEncoded()), 16);
            BigInteger biPublic = new BigInteger(Hex.toHexString(publicKey.getEncoded()), 16);

            ecKeyPair = new ECKeyPair(biPrivate, biPublic);
            privateKeyType = getPrivateKeyType(privateKey);
        }

        Credentials credentials = GenCredential.create(ecKeyPair.getPrivateKey().toString(16));
        logger.info(" loadAccount file: {}, address: {}", accountPath, credentials.getAddress());

        Account account = new Account(credentials);
        account.setPrivateKeyType(privateKeyType);
        account.setNewAccount(false);

        return account;
    }

    /**
     * @param
     * @param
     */
    public static void saveAccount(Account account, String path) {

        String fileName =
                account.getCredentials().getAddress()
                        + (account.getPrivateKeyType() == EncryptType.SM2_TYPE
                                ? "_gm.pem"
                                : ".pem");

        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String fullPath = path + File.separator + fileName;
        File file = new File(fullPath);
        if (file.exists()) {
            logger.warn("{} already exist", fullPath);
            throw new UnsupportedOperationException(fullPath + " already exist");
        }

        KeyPair keyPair = account.getKeyPair();

        try (PemWriter pemWriter = new PemWriter(new FileWriter(fullPath))) {
            pemWriter.writeObject(new PemObject("PRIVATE KEY", keyPair.getPrivate().getEncoded()));
        } catch (IOException e) {
            logger.error("writePemObject e: ", e);
            throw new UnsupportedOperationException(
                    "write " + fullPath + " failed, e: " + e.getMessage());
        }

        logger.info(
                " save account successfully, account: {}, path: {}",
                account.getCredentials().getAddress(),
                fullPath);

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

    public static int getPrivateKeyType(PrivateKey privateKey) {

        logger.debug(" privateKey: {}", privateKey.getClass().getClass().getName());

        if (privateKey instanceof ECPrivateKey) {
            ECParameterSpec ecParameterSpec = ((ECPrivateKey) privateKey).getParams();
            String name = ((ECNamedCurveSpec) ecParameterSpec).getName();

            if (name.contains("secp256k1")) {
                return EncryptType.ECDSA_TYPE;
            } else if (name.contains("sm2")) {
                return EncryptType.SM2_TYPE;
            }

            throw new UnsupportedOperationException(
                    "Unsupported ec private key type, name: " + name);
        } else if (privateKey instanceof SM2PrivateKey) {
            return EncryptType.SM2_TYPE;
        }

        throw new UnsupportedOperationException(
                " Unknown privateKey object, type name:" + privateKey.getClass().getName());
    }
}
