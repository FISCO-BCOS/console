package console.auth;

import console.ConsoleInitializer;

public interface AuthFace {

    void createUpdateGovernorProposal(String[] params) throws Exception;

    void createSetRateProposal(String[] params) throws Exception;

    void createSetDeployAuthTypeProposal(String[] params) throws Exception;

    void createOpenDeployAuthProposal(String[] params) throws Exception;

    void createCloseDeployAuthProposal(String[] params) throws Exception;

    void createResetAdminProposal(String[] params) throws Exception;

    void createSetConsensusWeightProposal(String[] params) throws Exception;

    void createAddSealerProposal(String[] params) throws Exception;

    void createAddObserverProposal(String[] params) throws Exception;

    void createRemoveNodeProposal(String[] params) throws Exception;

    void createSetSysConfigProposal(String[] params) throws Exception;

    void revokeProposal(String[] params) throws Exception;

    void voteProposal(String[] params) throws Exception;

    void getProposalInfoList(String[] params) throws Exception;

    void getCommitteeInfo(String[] params) throws Exception;

    void getContractAdmin(String[] params) throws Exception;

    void getDeployStrategy(String[] params) throws Exception;

    void checkDeployAuth(ConsoleInitializer consoleInitializer, String[] params) throws Exception;

    void setMethodAuthType(ConsoleInitializer consoleInitializer, String[] params) throws Exception;

    void openMethodAuth(ConsoleInitializer consoleInitializer, String[] params) throws Exception;

    void closeMethodAuth(ConsoleInitializer consoleInitializer, String[] params) throws Exception;

    void checkMethodAuth(ConsoleInitializer consoleInitializer, String[] params) throws Exception;

    void getMethodAuth(ConsoleInitializer consoleInitializer, String[] params) throws Exception;

    void getLatestProposal(String[] params) throws Exception;

    void freezeContract(String[] params) throws Exception;

    void unfreezeContract(String[] params) throws Exception;

    void getContractStatus(String[] params) throws Exception;
}
