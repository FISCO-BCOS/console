package console.auth;

import console.ConsoleInitializer;

public interface AuthFace {

    void createUpdateGovernorProposal(String[] params) throws Exception;

    void createSetRateProposal(String[] params) throws Exception;

    void createSetDeployAuthTypeProposal(String[] params) throws Exception;

    void createOpenDeployAuthProposal(String[] params) throws Exception;

    void createCloseDeployAuthProposal(String[] params) throws Exception;

    void createResetAdminProposal(String[] params) throws Exception;

    void revokeProposal(String[] params) throws Exception;

    void voteProposal(String[] params) throws Exception;

    void getProposalInfo(String[] params) throws Exception;

    void getCommitteeInfo(String[] params) throws Exception;

    void getContractAdmin(String[] params) throws Exception;

    void getDeployStrategy(String[] params) throws Exception;

    void checkDeployAuth(ConsoleInitializer consoleInitializer, String[] params) throws Exception;

    void setMethodAuthType(ConsoleInitializer consoleInitializer, String[] params) throws Exception;

    void openMethodAuth(ConsoleInitializer consoleInitializer, String[] params) throws Exception;

    void closeMethodAuth(ConsoleInitializer consoleInitializer, String[] params) throws Exception;

    void checkMethodAuth(ConsoleInitializer consoleInitializer, String[] params) throws Exception;
}
