package console.key.entity;

public class KeyInfo {

    private String keyAlias;
    private String cipherText;
    private String privateKey;

    public KeyInfo() {
        super();
    }

    public KeyInfo(String keyAlias, String cipherText, String privateKey) {
        super();
        this.keyAlias = keyAlias;
        this.cipherText = cipherText;
        this.privateKey = privateKey;
    }

    public String getKeyAlias() {
        return keyAlias;
    }

    public String getCipherText() {
        return cipherText;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setKeyAlias(String account) {
        this.keyAlias = keyAlias;
    }

    public void setCipherText(String accountPwd) {
        this.cipherText = cipherText;
    }

    public void setPrivateKey(String accountPwd) {
        this.privateKey = privateKey;
    }
}
