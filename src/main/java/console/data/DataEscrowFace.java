package console.data;

public interface DataEscrowFace {
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
    void uploadData(String[] params) throws Exception;

    void listData(String[] params) throws Exception;

    void exportData(String[] params) throws Exception;

    void deleteData(String[] params) throws Exception;

    void restoreData(String[] params) throws Exception;
}
