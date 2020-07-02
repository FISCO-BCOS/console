package console.data.entity;

public class PasswordInfo {

    private String oldAccountPwd;
    private String newAccountPwd;

    public PasswordInfo() {
        super();
    }

    public PasswordInfo(String oldAccountPwd, String newAccountPwd) {
        super();
        this.oldAccountPwd = oldAccountPwd;
        this.newAccountPwd = newAccountPwd;
    }

    public String getOldAccountPwd() {
        return oldAccountPwd;
    }

    public String getNewAccountPwd() {
        return newAccountPwd;
    }

    public void setOldAccountPwd(String oldAccountPwd) {
        this.oldAccountPwd = oldAccountPwd;
    }

    public void setNewAccountPwd(String newAccountPwd) {
        this.newAccountPwd = newAccountPwd;
    }
}
