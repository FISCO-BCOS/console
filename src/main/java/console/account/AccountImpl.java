package console.account;

import console.common.HelpInfo;
import console.common.PathUtils;
import java.util.Map;
import org.fisco.bcos.web3j.crypto.EncryptType;
import org.fisco.bcos.web3j.utils.Numeric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountImpl implements AccountInterface {

    private static final Logger logger = LoggerFactory.getLogger(AccountImpl.class);

    private AccountManager accountManager;

    public AccountManager getAccountManager() {
        return accountManager;
    }

    @Override
    public void setAccountManager(AccountManager accountManager) {
        this.accountManager = accountManager;
    }

    @Override
    public void loadAccount(String[] params) {
        if (params.length < 2) {
            HelpInfo.promptHelp("loadAccount");
            return;
        }

        if (params.length > 4) {
            HelpInfo.promptHelp("loadAccount");
            return;
        }

        if ("-h".equals(params[1]) || "--help".equals(params[1])) {
            HelpInfo.loadAccountHelp();
            return;
        }

        String accountPath = params[1];
        String password = null;
        if (params.length > 2) {
            password = params[2];
        }

        try {
            Account account = AccountTools.loadAccount(accountPath, password);
            if (!account.isAccountAvailable()) {
                System.out.println(
                        " the loading private key is not available, private key type:"
                                + AccountTools.getPrivateKeyTypeAsString(
                                        account.getPrivateKeyType())
                                + " ,console encryptType: "
                                + AccountTools.getPrivateKeyTypeAsString(EncryptType.encryptType));
            } else if (accountManager.addAccount(account)) {
                System.out.println(
                        " load "
                                + accountPath
                                + " successfully, account address: "
                                + account.getCredentials().getAddress());
            } else {
                System.out.println(
                        " account: " + account.getCredentials().getAddress() + " has been loaded.");
            }

        } catch (Exception e) {
            logger.error("e: ", e);
            System.out.println("load " + accountPath + " failed, error: " + e.getMessage());
        }

        System.out.println();
    }

    @Override
    public void switchAccount(String[] params) {
        if (params.length < 2) {
            HelpInfo.promptHelp("switchAccount");
            return;
        }

        if (params.length > 3) {
            HelpInfo.promptHelp("switchAccount");
            return;
        }

        if ("-h".equals(params[1]) || "--help".equals(params[1])) {
            HelpInfo.switchAccountHelp();
            return;
        }

        String accountAddress = params[1];
        try {
            accountAddress = Numeric.prependHexPrefix(accountAddress);
            Account account = accountManager.getAccount(accountAddress);
            if (account == null) {
                System.out.println(" account:" + accountAddress + " not exist.");
                System.out.println();
                System.out.println(" account list:");
                for (String address : accountManager.getAccountMap().keySet()) {
                    System.out.println("\t " + address);
                }
            } else {
                accountManager.setCurrentAccount(account);
                System.out.println("switch to account: " + accountAddress + " successfully.");
            }

        } catch (Exception e) {
            logger.error("e: ", e);
            System.out.println("load " + accountAddress + " failed, error: " + e.getMessage());
        }

        System.out.println();
    }

    @Override
    public void listAccount(String[] params) {
        if ((params.length > 1) && ("-h".equals(params[1]) || "--help".equals(params[1]))) {
            HelpInfo.listAccountHelp();
            return;
        }

        Map<String, Account> accountMap = accountManager.getAccountMap();
        System.out.println(" account list:");
        for (Account account : accountMap.values()) {
            System.out.println(
                    "\t "
                            + account.getCredentials().getAddress()
                            + (account.isNewAccount() ? " (temporary account)" : "")
                            + (accountManager.isCurrentAccount(account) ? " * " : ""));
        }

        System.out.println();
    }

    @Override
    public void saveAccount(String[] params) {
        if (params.length < 2) {
            HelpInfo.promptHelp("saveAccount");
            return;
        }

        if (params.length > 3) {
            HelpInfo.promptHelp("saveAccount");
            return;
        }

        // saveAccount [accountAddress] [savePath]
        if ("-h".equals(params[1]) || "--help".equals(params[1])) {
            HelpInfo.saveAccountHelp();
            return;
        }

        String accountAddress = params[1];
        accountAddress = Numeric.prependHexPrefix(accountAddress);

        String path = null;
        if (params.length > 2) {
            path = params[2];
        } else {
            path = PathUtils.ACCOUNT_DIRECTORY;
        }

        try {
            Account account = accountManager.getAccount(accountAddress);
            if (account == null) {
                System.out.println(" account:" + accountAddress + " not exist.");
            } else {
                AccountTools.saveAccount(account, path);
                System.out.println(
                        " save account: " + accountAddress + " successfully, path: " + path);
            }
        } catch (Exception e) {
            logger.error("e: ", e);
            System.out.println("load " + accountAddress + " failed, error: " + e.getMessage());
        }

        System.out.println();
    }

    @Override
    public void newAccount(String[] params) {

        if ((params.length > 1) && ("-h".equals(params[1]) || "--help".equals(params[1]))) {
            HelpInfo.newAccountHelp();
            return;
        }

        try {
            Account account = AccountTools.newAccount();
            accountManager.addAccount(account);
            System.out.println(
                    " new account successfully, account address:"
                            + account.getCredentials().getAddress());
        } catch (Exception e) {
            logger.error("e: ", e);
            System.out.println(" new account failed, error: " + e.getMessage());
        }

        System.out.println();
    }
}
