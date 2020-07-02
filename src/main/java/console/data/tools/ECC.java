package console.data.tools;

import java.math.BigInteger;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.fisco.bcos.web3j.crypto.tool.ECCDecrypt;
import org.fisco.bcos.web3j.crypto.tool.ECCEncrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ECC {

    static {
        Security.setProperty("crypto.policy", "unlimited");
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final Logger logger = LoggerFactory.getLogger(ECC.class);

    public static String encrypt(String plainStr, String publicKeyStr) {
        try {
            ECCEncrypt encrypt = new ECCEncrypt(new BigInteger(publicKeyStr, 16));
            byte[] encryptData = encrypt.encrypt(plainStr.getBytes("utf-8"));
            return Common.parseByte2HexStr(encryptData);
        } catch (Exception e) {
            logger.error("ECC.encrypt error message: {}, e: {}", e.getMessage(), e);
        }

        return null;
    }

    public static String decrypt(String cipherStr, String privateKeyStr) {
        try {
            byte[] cipher = Common.parseHexStr2Byte(cipherStr);
            ECCDecrypt encrypt = new ECCDecrypt(new BigInteger(privateKeyStr, 16));
            byte[] decryptData = encrypt.decrypt(cipher);
            return new String(decryptData, "utf-8");
        } catch (Exception e) {
            logger.error("ECC.decrypt error message: {}, e: {}", e.getMessage(), e);
        }

        return null;
    }
}
