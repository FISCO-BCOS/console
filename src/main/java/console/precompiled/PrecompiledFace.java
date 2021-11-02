package console.precompiled;

public interface PrecompiledFace {
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

    void revokeContractStatusManager(String[] params) throws Exception;

    void getContractStatus(String[] params) throws Exception;

    void listContractStatusManager(String[] params) throws Exception;

    void queryCNS(String[] params) throws Exception;

    void registerCNS(String[] params) throws Exception;

    void setConsensusNodeWeight(String[] params) throws Exception;

    void changeDir(String[] params, String pwd) throws Exception;

    void makeDir(String[] params, String pwd) throws Exception;

    void listDir(String[] params, String pwd) throws Exception;

    void pwd(String[] params) throws Exception;
}
