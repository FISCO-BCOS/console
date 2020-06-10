package console.key.entity;

public class AccountInfo {

    private String account;
    private String accountPwd;
    private String publicKey;
    private int roleId;

    public AccountInfo(String account, String accountPwd, String publicKey, int roleId) {
        super();
        this.account = account;
        this.accountPwd = accountPwd;
        this.publicKey = publicKey;
        this.roleId = roleId;
    }

    public String getAccount() {
        return account;
    }

    public String getAccountPwd() {
        return accountPwd;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setAccountPwd(String accountPwd) {
        this.accountPwd = accountPwd;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }
}
