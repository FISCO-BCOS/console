package console.precompiled.permission;

import console.account.AccountManager;
import org.fisco.bcos.web3j.protocol.Web3j;

public interface PermissionFace {

    void setWeb3j(Web3j web3j);

    void setAccountManager(AccountManager accountManager);

    void grantUserTableManager(String[] params) throws Exception;

    void revokeUserTableManager(String[] params) throws Exception;

    void listUserTableManager(String[] params) throws Exception;

    void grantDeployAndCreateManager(String[] params) throws Exception;

    void revokeDeployAndCreateManager(String[] params) throws Exception;

    void listDeployAndCreateManager(String[] params) throws Exception;

    void grantNodeManager(String[] params) throws Exception;

    void revokeNodeManager(String[] params) throws Exception;

    void listNodeManager(String[] params) throws Exception;

    void grantCNSManager(String[] params) throws Exception;

    void revokeCNSManager(String[] params) throws Exception;

    void listCNSManager(String[] params) throws Exception;

    void grantSysConfigManager(String[] params) throws Exception;

    void revokeSysConfigManager(String[] params) throws Exception;

    void listSysConfigManager(String[] params) throws Exception;

    void listContractWritePermission(String[] params) throws Exception;

    void grantContractWritePermission(String[] params) throws Exception;

    void revokeContractWritePermission(String[] params) throws Exception;

    void grantCommitteeMember(String[] params) throws Exception;

    void revokeCommitteeMember(String[] params) throws Exception;

    void listCommitteeMembers(String[] params) throws Exception;

    void queryCommitteeMemberWeight(String[] params) throws Exception;

    void updateCommitteeMemberWeight(String[] params) throws Exception;

    void updateThreshold(String[] params) throws Exception;

    void queryThreshold(String[] params) throws Exception;

    void grantOperator(String[] params) throws Exception;

    void revokeOperator(String[] params) throws Exception;

    void listOperators(String[] params) throws Exception;

    void freezeAccount(String[] params) throws Exception;

    void unfreezeAccount(String[] params) throws Exception;

    void getAccountStatus(String[] params) throws Exception;
}
