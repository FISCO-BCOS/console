package console.key;

public interface KeyFace {
    // service
    void setURLPrefix(String urlPrefix);

    // account
    String login(String[] params) throws Exception;

    void addAdminAccount(String[] params) throws Exception;

    void addVisitorAccount(String[] params) throws Exception;

    void deleteAccount(String[] params) throws Exception;

    void listAccount(String[] params) throws Exception;

    void updatePwd(String[] params) throws Exception;

    // key escrow
    void uploadPrivateKey(String[] params) throws Exception;

    void listPrivateKey(String[] params) throws Exception;

    void exportPrivateKey(String[] params) throws Exception;

    void deletePrivateKey(String[] params) throws Exception;

    void restorePrivateKey(String[] params) throws Exception;
}
