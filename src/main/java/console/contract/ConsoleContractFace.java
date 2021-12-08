package console.contract;

import console.ConsoleInitializer;
import console.exception.ConsoleMessageException;

public interface ConsoleContractFace {
    void deploy(String[] params, String pwd) throws Exception;

    void call(String[] params, String pwd) throws Exception;

    void deployByCNS(String[] params) throws ConsoleMessageException;

    void callByCNS(String[] params) throws Exception;

    void getDeployLog(String[] params) throws Exception;

    void listAbi(ConsoleInitializer consoleInitializer, String[] params, String pwd)
            throws Exception;
}
