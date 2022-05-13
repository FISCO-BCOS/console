package console.auth;

import console.ConsoleInitializer;
import console.common.ConsoleUtils;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.tuples.generated.Tuple3;
import org.fisco.bcos.sdk.v3.contract.auth.manager.AuthManager;
import org.fisco.bcos.sdk.v3.contract.auth.po.AuthType;
import org.fisco.bcos.sdk.v3.contract.auth.po.CommitteeInfo;
import org.fisco.bcos.sdk.v3.contract.auth.po.GovernorInfo;
import org.fisco.bcos.sdk.v3.contract.auth.po.ProposalInfo;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.v3.model.PrecompiledRetCode;
import org.fisco.bcos.sdk.v3.model.RetCode;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.transaction.codec.decode.ReceiptParser;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;
import org.fisco.bcos.sdk.v3.transaction.model.exception.TransactionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthImpl implements AuthFace {

    private static final Logger logger = LoggerFactory.getLogger(AuthImpl.class);
    private AuthManager authManager;
    private static final long WEIGHT_MAX = 10000;
    private static final int GOVERNOR_NUM_MAX = 1000;

    public AuthImpl(Client client) {
        CryptoKeyPair cryptoKeyPair = client.getCryptoSuite().getCryptoKeyPair();
        boolean authAvailable = client.isAuthCheck() && !client.isWASM();
        if (authAvailable) {
            this.authManager = new AuthManager(client, cryptoKeyPair);
        } else {
            logger.info("Auth check disable, not use auth.");
        }
    }

    @Override
    public void createUpdateGovernorProposal(String[] params) throws Exception {
        try {
            String account = params[1];
            checkValidAddress(account, "account");
            BigInteger weight = BigInteger.valueOf(Long.parseLong(params[2]));
            if (weight.compareTo(BigInteger.ZERO) < 0
                    || weight.compareTo(BigInteger.valueOf(WEIGHT_MAX)) > 0) {
                throw new TransactionException(
                        "Weight is limit in [0, "
                                + WEIGHT_MAX
                                + "], please use a weight in this range.");
            }
            List<GovernorInfo> governorList = authManager.getCommitteeInfo().getGovernorList();
            if (governorList.size() > GOVERNOR_NUM_MAX
                    && governorList
                            .stream()
                            .noneMatch(
                                    governorInfo ->
                                            account.equals(governorInfo.getGovernorAddress()))) {
                throw new TransactionException(
                        "The number of governor is over "
                                + GOVERNOR_NUM_MAX
                                + ", can not add new governor.");
            }
            BigInteger proposalId = authManager.updateGovernor(account, weight);
            System.out.println("Update governor proposal created, ID is: " + proposalId);
            showProposalInfo(proposalId);
        } catch (NumberFormatException e) {
            System.out.println("Number convert error, please check number you input");
        } catch (TransactionException e) {
            logger.error("createUpdateGovernorProposal, e:", e);
            System.out.println("Error: " + e.getMessage());
        }
    }

    @Override
    public void createSetRateProposal(String[] params) throws Exception {
        try {
            BigInteger participatesRate = BigInteger.valueOf(Long.parseLong(params[1]));
            BigInteger winRate = BigInteger.valueOf(Long.parseLong(params[2]));
            checkValidRate(participatesRate, "participatesRate");
            checkValidRate(winRate, "winRate");
            BigInteger proposalId = authManager.setRate(participatesRate, winRate);
            System.out.println("Set rate proposal created, ID is: " + proposalId);
            showProposalInfo(proposalId);
        } catch (NumberFormatException e) {
            System.out.println("Number convert error, please check number you input");
        } catch (TransactionException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    @Override
    public void createSetDeployAuthTypeProposal(String[] params) throws Exception {
        String deployAuthStr = params[1];
        BigInteger proposalId;
        try {
            if (deployAuthStr.equals("white_list")) {
                proposalId = authManager.setDeployAuthType(AuthType.WHITE_LIST);
            } else if (deployAuthStr.equals("black_list")) {
                proposalId = authManager.setDeployAuthType(AuthType.BLACK_LIST);
            } else {
                throw new Exception("Error authType, auth type is white_list or black_list");
            }
            System.out.println("Set deploy auth type proposal created, ID is: " + proposalId);
            showProposalInfo(proposalId);
        } catch (TransactionException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    @Override
    public void createOpenDeployAuthProposal(String[] params) throws Exception {
        try {
            String account = params[1];
            checkValidAddress(account, account);
            BigInteger proposalId = authManager.modifyDeployAuth(account, true);
            System.out.println("Open deploy auth proposal created, ID is: " + proposalId);
            showProposalInfo(proposalId);
        } catch (TransactionException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    @Override
    public void createCloseDeployAuthProposal(String[] params) throws Exception {
        try {
            String account = params[1];
            checkValidAddress(account, account);
            BigInteger proposalId = authManager.modifyDeployAuth(account, false);
            System.out.println("Close deploy auth proposal created, ID is: " + proposalId);
            showProposalInfo(proposalId);
        } catch (TransactionException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    @Override
    public void createResetAdminProposal(String[] params) throws Exception {
        try {
            String newAdmin = params[1];
            String contractAddr = params[2];
            checkValidAddress(newAdmin, "newAdmin");
            checkValidAddress(contractAddr, "contractAddress");
            BigInteger proposalId = authManager.resetAdmin(newAdmin, contractAddr);
            System.out.println("Reset contract admin proposal created, ID is: " + proposalId);
            showProposalInfo(proposalId);
        } catch (TransactionException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    @Override
    public void createSetConsensusWeightProposal(String[] params) throws Exception {
        String nodeId = params[1];
        BigInteger weight =
                ConsoleUtils.processNonNegativeBigNumber(
                        "consensusWeight",
                        params[2],
                        BigInteger.ONE,
                        BigInteger.valueOf(Integer.MAX_VALUE));
        if (nodeId.length() != 128) {
            ConsoleUtils.printJson(PrecompiledRetCode.CODE_INVALID_NODEID.toString());
            return;
        }
        BigInteger setConsensusWeightProposal =
                authManager.createSetConsensusWeightProposal(nodeId, weight, false);
        showProposalInfo(setConsensusWeightProposal);
    }

    @Override
    public void createAddSealerProposal(String[] params) throws Exception {
        String nodeId = params[1];
        BigInteger weight =
                ConsoleUtils.processNonNegativeBigNumber(
                        "consensusWeight",
                        params[2],
                        BigInteger.ONE,
                        BigInteger.valueOf(Integer.MAX_VALUE));
        if (nodeId.length() != 128) {
            ConsoleUtils.printJson(PrecompiledRetCode.CODE_INVALID_NODEID.toString());
            return;
        }
        BigInteger setConsensusWeightProposal =
                authManager.createSetConsensusWeightProposal(nodeId, weight, true);
        showProposalInfo(setConsensusWeightProposal);
    }

    @Override
    public void createAddObserverProposal(String[] params) throws Exception {
        String nodeId = params[1];
        if (nodeId.length() != 128) {
            ConsoleUtils.printJson(PrecompiledRetCode.CODE_INVALID_NODEID.toString());
            return;
        }
        BigInteger setConsensusWeightProposal =
                authManager.createSetConsensusWeightProposal(nodeId, BigInteger.ZERO, true);
        showProposalInfo(setConsensusWeightProposal);
    }

    @Override
    public void createRemoveNodeProposal(String[] params) throws Exception {
        String nodeId = params[1];
        if (nodeId.length() != 128) {
            ConsoleUtils.printJson(PrecompiledRetCode.CODE_INVALID_NODEID.toString());
            return;
        }
        BigInteger rmNodeProposal = authManager.createRmNodeProposal(nodeId);
        showProposalInfo(rmNodeProposal);
    }

    @Override
    public void createSetSysConfigProposal(String[] params) throws Exception {
        String key = params[1];
        BigInteger value =
                ConsoleUtils.processNonNegativeBigNumber(
                        "sysConfigValue",
                        params[2],
                        BigInteger.ONE,
                        BigInteger.valueOf(Integer.MAX_VALUE));

        BigInteger setSysConfigProposal = authManager.createSetSysConfigProposal(key, value);
        showProposalInfo(setSysConfigProposal);
    }

    @Override
    public void revokeProposal(String[] params) throws Exception {
        try {
            BigInteger proposalId = BigInteger.valueOf(Long.parseLong(params[1]));
            TransactionReceipt receipt = authManager.revokeProposal(proposalId);
            RetCode retCode = ReceiptParser.parseTransactionReceipt(receipt);
            if (retCode.code == 0) {
                System.out.println("Revoke proposal success.");
            } else {
                System.out.println("Revoke proposal failed, msg: " + retCode.getMessage());
            }
            showProposalInfo(proposalId);
        } catch (NumberFormatException e) {
            System.out.println("Number convert error, please check number you input");
        }
    }

    @Override
    public void voteProposal(String[] params) throws Exception {
        try {
            BigInteger proposalId = BigInteger.valueOf(Long.parseLong(params[1]));
            boolean agree = true;
            if (params.length == 3) {
                if ("true".equals(params[2])) {
                    agree = true;
                } else if ("false".equals(params[2])) {
                    agree = false;
                } else {
                    System.out.println("Please provide true or false for the second parameter.");
                    return;
                }
            }
            TransactionReceipt receipt = authManager.voteProposal(proposalId, agree);
            RetCode retCode = ReceiptParser.parseTransactionReceipt(receipt);
            if (retCode.code == 0) {
                System.out.println("Vote proposal success.");
            } else {
                System.out.println("Vote proposal failed, msg: " + retCode.getMessage());
            }
            showProposalInfo(proposalId);
        } catch (NumberFormatException e) {
            System.out.println("Number convert error, please check number you input");
        }
    }

    @Override
    public void getProposalInfoList(String[] params) throws Exception {
        if (params.length == 2) {
            try {
                BigInteger proposalId = BigInteger.valueOf(Long.parseLong(params[1]));
                showProposalInfo(proposalId);
            } catch (NumberFormatException e) {
                System.out.println("Number convert error, please check proposal id you input.");
            }
            return;
        }
        BigInteger from =
                ConsoleUtils.processNonNegativeBigNumber(
                        "proposalFrom",
                        params[1],
                        BigInteger.ZERO,
                        BigInteger.valueOf(Integer.MAX_VALUE));
        BigInteger to =
                ConsoleUtils.processNonNegativeBigNumber(
                        "proposalTo",
                        params[2],
                        BigInteger.ZERO,
                        BigInteger.valueOf(Integer.MAX_VALUE));
        List<ProposalInfo> proposalInfoList = authManager.getProposalInfoList(from, to);
        for (ProposalInfo proposalInfo : proposalInfoList) {
            showProposalInfo(proposalInfo);
        }
    }

    private void showProposalInfo(BigInteger proposalId) throws ContractException {
        ProposalInfo proposalInfo = authManager.getProposalInfo(proposalId);
        if (proposalInfo.getProposalType() == 0 && proposalInfo.getStatus() == 0) {
            System.out.println("Proposal not found in committee, please check id: " + proposalId);
            return;
        }
        showProposalInfo(proposalInfo);
    }

    private void showProposalInfo(ProposalInfo proposalInfo) {
        ConsoleUtils.singleLine();
        System.out.println("Proposer: " + proposalInfo.getProposer());
        System.out.println("Proposal Type   : " + proposalInfo.getProposalTypeString());
        System.out.println("Proposal Status : " + proposalInfo.getStatusString());
        ConsoleUtils.singleLine();
        System.out.println("Agree Voters:");
        for (String agreeVoter : proposalInfo.getAgreeVoters()) {
            System.out.println(agreeVoter);
        }
        ConsoleUtils.singleLine();
        System.out.println("Against Voters:");
        for (String againstVoter : proposalInfo.getAgainstVoters()) {
            System.out.println(againstVoter);
        }
    }

    @Override
    public void getCommitteeInfo(String[] params) throws Exception {
        ConsoleUtils.singleLine();
        System.out.println("Committee address   : " + authManager.getCommitteeAddress());
        System.out.println("ProposalMgr address : " + authManager.getProposalManagerAddress());
        ConsoleUtils.singleLine();
        CommitteeInfo committeeInfo = authManager.getCommitteeInfo();
        System.out.println(
                "ParticipatesRate: "
                        + committeeInfo.getParticipatesRate()
                        + "% , WinRate: "
                        + committeeInfo.getWinRate()
                        + "%");
        ConsoleUtils.singleLine();
        System.out.println("Governor Address                                        | Weight");
        List<GovernorInfo> governorList = committeeInfo.getGovernorList();
        for (int i = 0; i < governorList.size(); i++) {
            System.out.println(
                    "index"
                            + i
                            + " : "
                            + governorList.get(i).getGovernorAddress()
                            + "     | "
                            + governorList.get(i).getWeight());
        }
    }

    @Override
    public void getContractAdmin(String[] params) throws Exception {
        String contractAddress = params[1];
        try {
            checkValidAddress(contractAddress, "");
            String admin = authManager.getAdmin(contractAddress);
            if (admin.equals(ConsoleUtils.EMPTY_ADDRESS)) {
                System.out.println(
                        "Contract address not exist, please check address: " + contractAddress);
                return;
            }
            System.out.println("Admin for contract " + contractAddress + " is: " + admin);
        } catch (TransactionException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    @Override
    public void getDeployStrategy(String[] params) throws Exception {
        BigInteger deployAuthType = authManager.getDeployAuthType();
        if (Objects.equals(deployAuthType, BigInteger.ZERO)) {
            System.out.println("There is no deploy strategy, everyone can deploy contracts.");
        } else if (Objects.equals(deployAuthType, BigInteger.ONE)) {
            System.out.println("Deploy strategy is White List Access.");
        } else if (Objects.equals(deployAuthType, BigInteger.valueOf(2))) {
            System.out.println("Deploy strategy is Black List Access.");
        } else {
            System.out.println("Deploy strategy is UNKNOWN, please check node status.");
        }
    }

    @Override
    public void checkDeployAuth(ConsoleInitializer consoleInitializer, String[] params)
            throws Exception {
        String accountAddress =
                (params.length == 1)
                        ? consoleInitializer
                                .getClient()
                                .getCryptoSuite()
                                .getCryptoKeyPair()
                                .getAddress()
                        : params[1];
        checkValidAddress(accountAddress, "accountAddress");
        try {
            checkValidAddress(accountAddress, "accountAddress");
            Boolean hasDeployAuth = authManager.checkDeployAuth(accountAddress);
            System.out.println(
                    "Deploy : "
                            + ((hasDeployAuth)
                                    ? "\033[32m" + "ACCESS" + "\033[m"
                                    : "\033[31m" + "PERMISSION DENIED" + "\033[m"));
            System.out.println("Account: " + accountAddress);
        } catch (TransactionException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    @Override
    public void setMethodAuthType(ConsoleInitializer consoleInitializer, String[] params)
            throws Exception {
        // contract, func, type
        String address = params[1];
        String funcStr = params[2];
        byte[] hash = consoleInitializer.getClient().getCryptoSuite().hash(funcStr.getBytes());
        byte[] func = Arrays.copyOfRange(hash, 0, 4);
        String type = params[3];
        RetCode setResult;
        try {
            checkValidAddress(address, "contractAddress");
            if (type.equals("white_list")) {
                setResult = authManager.setMethodAuthType(address, func, AuthType.WHITE_LIST);
            } else if (type.equals("black_list")) {
                setResult = authManager.setMethodAuthType(address, func, AuthType.BLACK_LIST);
            } else {
                System.out.println("Error authType, auth type is white_list or black_list.");
                return;
            }
            ConsoleUtils.printJson(setResult.toString());
        } catch (TransactionException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    @Override
    public void openMethodAuth(ConsoleInitializer consoleInitializer, String[] params)
            throws Exception {
        // contract, func, address
        String contract = params[1];
        String funcStr = params[2];
        byte[] hash = consoleInitializer.getClient().getCryptoSuite().hash(funcStr.getBytes());
        byte[] func = Arrays.copyOfRange(hash, 0, 4);
        String account = params[3];
        try {
            checkValidAddress(contract, "contractAddress");
            checkValidAddress(account, "accountAddress");
            RetCode openResult = authManager.setMethodAuth(contract, func, account, true);
            ConsoleUtils.printJson(openResult.toString());
        } catch (TransactionException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    @Override
    public void closeMethodAuth(ConsoleInitializer consoleInitializer, String[] params)
            throws Exception {
        // contract, func, address
        String contract = params[1];
        String funcStr = params[2];
        byte[] hash = consoleInitializer.getClient().getCryptoSuite().hash(funcStr.getBytes());
        byte[] func = Arrays.copyOfRange(hash, 0, 4);
        String account = params[3];
        try {
            checkValidAddress(contract, "contractAddress");
            checkValidAddress(account, "accountAddress");
            RetCode closeResult = authManager.setMethodAuth(contract, func, account, false);
            ConsoleUtils.printJson(closeResult.toString());
        } catch (TransactionException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    @Override
    public void checkMethodAuth(ConsoleInitializer consoleInitializer, String[] params)
            throws Exception {
        // contract, func, address
        String contract = params[1];
        String funcStr = params[2];
        byte[] hash = consoleInitializer.getClient().getCryptoSuite().hash(funcStr.getBytes());
        byte[] func = Arrays.copyOfRange(hash, 0, 4);
        String account =
                (params.length == 3)
                        ? consoleInitializer
                                .getClient()
                                .getCryptoSuite()
                                .getCryptoKeyPair()
                                .getAddress()
                        : params[3];
        try {
            checkValidAddress(contract, "contractAddress");
            checkValidAddress(account, "accountAddress");
            Boolean hasAuth = authManager.checkMethodAuth(contract, func, account);
            logger.debug(
                    "checkMethodAuth: account:{}, funcStr:{}, func:{}, contract:{}",
                    account,
                    funcStr,
                    func,
                    contract);
            System.out.println(
                    "Method   : "
                            + ((hasAuth)
                                    ? "\033[32m" + "ACCESS" + "\033[m"
                                    : "\033[31m" + "PERMISSION DENIED" + "\033[m"));
            System.out.println("Account  : " + account);
            System.out.println("Interface: " + funcStr);
            System.out.println("Contract : " + contract);
        } catch (TransactionException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    @Override
    public void getMethodAuth(ConsoleInitializer consoleInitializer, String[] params)
            throws Exception {
        String contract = params[1];
        String funcStr = params[2];
        byte[] hash = consoleInitializer.getClient().getCryptoSuite().hash(funcStr.getBytes());
        byte[] func = Arrays.copyOfRange(hash, 0, 4);
        checkValidAddress(contract, "contractAddress");
        Tuple3<AuthType, List<String>, List<String>> methodAuth =
                authManager.getMethodAuth(contract, func);
        ConsoleUtils.singleLine();
        System.out.println("Contract address: " + contract);
        System.out.println("Contract method : " + funcStr);
        System.out.println("Method auth type: " + methodAuth.getValue1().toString());
        ConsoleUtils.singleLine();
        System.out.println("Access address:");
        for (String s : methodAuth.getValue2()) {
            System.out.println(s);
        }
        ConsoleUtils.singleLine();
        System.out.println("Block address :");
        for (String s : methodAuth.getValue3()) {
            System.out.println(s);
        }
    }

    @Override
    public void getLatestProposal(String[] params) throws Exception {
        BigInteger proposalId = this.authManager.proposalCount();
        if (proposalId.equals(BigInteger.ZERO)) {
            System.out.println("No proposal exists currently, try to propose one.");
            return;
        }
        System.out.println("Latest proposal ID: " + proposalId.toString());
        if (proposalId.compareTo(BigInteger.ZERO) > 0) {
            showProposalInfo(proposalId);
        }
    }

    @Override
    public void freezeContract(String[] params) throws Exception {
        String contract = params[1];
        checkValidAddress(contract, "contractAddress");
        RetCode result = authManager.setContractStatus(contract, true);
        ConsoleUtils.printJson(result.toString());
    }

    @Override
    public void unfreezeContract(String[] params) throws Exception {
        String contract = params[1];
        checkValidAddress(contract, "contractAddress");
        RetCode result = authManager.setContractStatus(contract, false);
        ConsoleUtils.printJson(result.toString());
    }

    @Override
    public void getContractStatus(String[] params) throws Exception {
        String contract = params[1];
        checkValidAddress(contract, "contractAddress");
        Boolean isAvailable = authManager.contractAvailable(contract);
        System.out.println(isAvailable ? "Available" : "Freeze");
    }

    void checkValidAddress(String address, String valueName) throws TransactionException {
        if (!ConsoleUtils.isValidAddress(address)) {
            throw new TransactionException(
                    "Invalid address "
                            + (valueName.isEmpty() ? "" : ("for " + valueName))
                            + ": "
                            + address);
        }
    }

    void checkValidRate(BigInteger rate, String valueName) throws TransactionException {
        if (rate.compareTo(BigInteger.ZERO) < 0 || rate.compareTo(BigInteger.valueOf(100)) > 0) {

            throw new TransactionException(
                    "Invalid rate number "
                            + (valueName.isEmpty() ? "" : ("for " + valueName))
                            + ": "
                            + rate);
        }
    }
}
