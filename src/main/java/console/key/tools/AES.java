package console.key.tools;

import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.apache.http.client.methods.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AES {

    private static final Logger logger = LoggerFactory.getLogger(AES.class);

    public static SecretKeySpec generateKey(String password) {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.setSeed(password.getBytes());
            kgen.init(128, secureRandom);
            SecretKey secretKey = kgen.generateKey();
            byte[] enCodeFormat = secretKey.getEncoded();
            return new SecretKeySpec(enCodeFormat, "AES");
        } catch (Exception e) {
            logger.error("AES.generateKey error message: {}, e: {}", e.getMessage(), e);
        }

        return null;
    }

    public static String encrypt(String plainStr, String password) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, generateKey(password));
            byte[] cipherByte = cipher.doFinal(plainStr.getBytes());
            return Common.parseByte2HexStr(cipherByte);
        } catch (Exception e) {
            logger.error("AES.encrypt error message: {}, e: {}", e.getMessage(), e);
        }

        return null;
    }

    public static String decrypt(String cipherStr, String password) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, generateKey(password));
            byte[] plainByte = cipher.doFinal(Common.parseHexStr2Byte(cipherStr));
            return new String(plainByte, "utf-8");
        } catch (Exception e) {
            logger.error("AES.decrypt error message: {}, e: {}", e.getMessage(), e);
        }

        return null;
    }
}
