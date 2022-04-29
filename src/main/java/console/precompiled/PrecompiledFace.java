package console.precompiled;

public interface PrecompiledFace {
    // ConsensusPrecompiled
    void addSealer(String[] params) throws Exception;

    void addObserver(String[] params) throws Exception;

    void removeNode(String[] params) throws Exception;

    // SystemConfigPrecompiled
    void setSystemConfigByKey(String[] params) throws Exception;

    void createTable(String sql, boolean isWasm) throws Exception;

    void insert(String sql) throws Exception;

    void update(String sql) throws Exception;

    void remove(String sql) throws Exception;

    void select(String sql) throws Exception;

    void desc(String[] params) throws Exception;

    void setConsensusNodeWeight(String[] params) throws Exception;

    void changeDir(String[] params) throws Exception;

    void makeDir(String[] params) throws Exception;

    void listDir(String[] params) throws Exception;

    void tree(String[] params) throws Exception;

    void link(String[] params) throws Exception;

    String getPwd();
}
