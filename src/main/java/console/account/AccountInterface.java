package console.account;

public interface AccountInterface {

    void setAccountManager(AccountManager accountManager);

    void loadAccount(String[] params);

    void switchAccount(String[] params);

    void listAccount(String[] params);

    void newAccount(String[] params);
}
