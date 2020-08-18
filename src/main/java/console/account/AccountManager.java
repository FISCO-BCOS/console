package console.account;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountManager {

    private static final Logger logger = LoggerFactory.getLogger(AccountManager.class);

    /** Current account used to sign transaction */
    private Account currentAccount;
    /** account mapper used to save all accounts loaded */
    private Map<String, Account> accountMap = new ConcurrentHashMap<>();

    /**
     * check if account current
     *
     * @param account
     * @return
     */
    public boolean isCurrentAccount(Account account) {
        return account.getCredentials()
                .getAddress()
                .equals(getCurrentAccount().getCredentials().getAddress());
    }

    public Account getCurrentAccount() {
        return currentAccount;
    }

    public Credentials getCurrentAccountCredentials() {
        return currentAccount.getCredentials();
    }

    public void setCurrentAccount(Account currentAccount) {
        this.currentAccount = currentAccount;
    }

    public boolean isAccountExist(String account) {
        return accountMap.get(account) != null;
    }

    public boolean addAccount(Account account) {
        Account account1 = accountMap.putIfAbsent(account.getCredentials().getAddress(), account);
        logger.info("addAccount, addresses: {}", accountMap.keySet().toArray(new String[0]));
        return account1 != null;
    }

    public Account getAccount(String account) {
        return accountMap.get(account);
    }

    public Map<String, Account> getAccountMap() {
        return accountMap;
    }

    public void setAccountMap(Map<String, Account> accountMap) {
        this.accountMap = accountMap;
    }
}
