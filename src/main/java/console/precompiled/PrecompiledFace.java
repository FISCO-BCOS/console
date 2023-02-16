package console.precompiled;

import console.ConsoleInitializer;

public interface PrecompiledFace {
    // ConsensusPrecompiled
    void addSealer(String[] params) throws Exception;

    void addObserver(String[] params) throws Exception;

    void removeNode(String[] params) throws Exception;

    void setSystemConfigByKey(ConsoleInitializer consoleInitializer, String[] params)
            throws Exception;

    void createTable(String sql) throws Exception;

    void alterTable(String sql) throws Exception;

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

    void getContractShard(String[] params) throws Exception;

    void makeShard(String[] params) throws Exception;

    void linkShard(String[] params) throws Exception;

    String getPwd();
}
