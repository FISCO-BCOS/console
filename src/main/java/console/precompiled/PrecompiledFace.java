package console.precompiled;

import console.account.AccountManager;
import org.fisco.bcos.web3j.protocol.Web3j;

public interface PrecompiledFace {

    void setWeb3j(Web3j web3j);

    void setAccountManager(AccountManager accountManager);

    // ConsensusPrecompiled
    void addSealer(String[] params) throws Exception;

    void addObserver(String[] params) throws Exception;

    void removeNode(String[] params) throws Exception;

    // SystemConfigPrecompiled
    void setSystemConfigByKey(String[] params) throws Exception;

    // CRUDPrecompiled
    void createTable(String sql) throws Exception;

    void insert(String sql) throws Exception;

    void update(String sql) throws Exception;

    void remove(String sql) throws Exception;

    void select(String sql) throws Exception;

    void desc(String[] params) throws Exception;

    // ContractLifeCyclecompiled
    void freezeContract(String[] params) throws Exception;

    void unfreezeContract(String[] params) throws Exception;

    void grantContractStatusManager(String[] params) throws Exception;

    void getContractStatus(String[] params) throws Exception;

    void listContractStatusManager(String[] params) throws Exception;
}
