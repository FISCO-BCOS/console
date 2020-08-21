package console.account;

import java.security.KeyPair;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.EncryptType;

public class Account {

    private Credentials credentials;
    private KeyPair keyPair;
    /** EncryptType.SM2_TYPE or EncryptType.ECDSA */
    private int privateKeyType;
    /** if this account temporary account */
    private boolean isTempAccount = false;

    public Account(Credentials credentials) {
        this.credentials = credentials;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    public boolean isTempAccount() {
        return isTempAccount;
    }

    public void setTempAccount(boolean tempAccount) {
        isTempAccount = tempAccount;
    }

    public int getPrivateKeyType() {
        return privateKeyType;
    }

    public void setPrivateKeyType(int privateKeyType) {
        this.privateKeyType = privateKeyType;
    }

    /**
     * Check if the current account is available <br>
     * SM2 private key is available when console work in SM mode <br>
     * ECDSA private key is available when console work in NonSM mode <br>
     *
     * @return
     */
    public boolean isTypeMatchingAccount() {
        return privateKeyType == EncryptType.encryptType;
    }

    @Override
    public String toString() {
        return "Account{"
                + "address="
                + credentials.getAddress()
                + ", privateKeyType="
                + privateKeyType
                + ", newAccount="
                + isTempAccount
                + '}';
    }

    public KeyPair getKeyPair() {
        return this.keyPair;
    }

    public void setKeyPair(final KeyPair keyPair) {
        this.keyPair = keyPair;
    }
}
