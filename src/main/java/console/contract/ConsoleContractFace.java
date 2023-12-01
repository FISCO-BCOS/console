package console.contract;

import console.ConsoleInitializer;

public interface ConsoleContractFace {
    void deploy(String[] params, String pwd) throws Exception;

    void call(String[] params, String pwd) throws Exception;

    void getDeployLog(String[] params) throws Exception;

    void listAbi(ConsoleInitializer consoleInitializer, String[] params, String pwd)
            throws Exception;

    void listDeployContractAddress(ConsoleInitializer consoleInitializer, String[] params)
            throws Exception;

    void transfer(ConsoleInitializer consoleInitializer, String[] params) throws Exception;
}
