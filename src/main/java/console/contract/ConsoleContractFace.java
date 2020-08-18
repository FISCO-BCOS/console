package console.contract;

import console.exception.ConsoleMessageException;

public interface ConsoleContractFace {
    void deploy(String[] params) throws ConsoleMessageException;

    void call(String[] params) throws Exception;

    void deployByCNS(String[] params) throws ConsoleMessageException;

    void callByCNS(String[] params) throws Exception;

    void getDeployLog(String[] params) throws Exception;
}
