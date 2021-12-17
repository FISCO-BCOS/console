package console.auth;

import console.ConsoleInitializer;
import console.common.ConsoleUtils;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.contract.auth.manager.AuthManager;
import org.fisco.bcos.sdk.contract.auth.po.AuthType;
import org.fisco.bcos.sdk.contract.auth.po.CommitteeInfo;
import org.fisco.bcos.sdk.contract.auth.po.GovernorInfo;
import org.fisco.bcos.sdk.contract.auth.po.ProposalInfo;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.model.PrecompiledRetCode;
import org.fisco.bcos.sdk.model.RetCode;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.transaction.codec.decode.ReceiptParser;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;
import org.fisco.bcos.sdk.transaction.model.exception.TransactionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthImpl implements AuthFace {

    private static final Logger logger = LoggerFactory.getLogger(AuthImpl.class);
    private AuthManager authManager;
    private boolean authAvailable = false;

    public AuthImpl(Client client) throws ContractException {
        CryptoKeyPair cryptoKeyPair = client.getCryptoSuite().getCryptoKeyPair();
        this.authAvailable = client.isAuthCheck();
        if (this.authAvailable) {
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
            if (weight.compareTo(BigInteger.ZERO) < 0) {
                throw new TransactionException(
                        "weight is less than 0, please use a weight LE than 0");
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
    public void getProposalInfo(String[] params) throws Exception {
        try {
            BigInteger proposalId = BigInteger.valueOf(Long.parseLong(params[1]));
            showProposalInfo(proposalId);
        } catch (NumberFormatException e) {
            System.out.println("Number convert error, please check proposal id you input.");
        }
    }

    private void showProposalInfo(BigInteger proposalId) throws ContractException {
        ProposalInfo proposalInfo = authManager.getProposalInfo(proposalId);
        if (proposalInfo.getProposalType() == 0 && proposalInfo.getStatus() == 0) {
            System.out.println("Proposal not found in committee, please check id: " + proposalId);
            return;
        }
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
            if (admin.equals("0x0000000000000000000000000000000000000000")) {
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
        BigInteger setResult;
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
            RetCode precompiledResponse =
                    PrecompiledRetCode.getPrecompiledResponse(setResult.intValue(), "Success");
            ConsoleUtils.printJson(
                    "{\"code\":"
                            + precompiledResponse.getCode()
                            + ", \"msg\":"
                            + "\""
                            + precompiledResponse.getMessage()
                            + "\"}");
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
            BigInteger openResult = authManager.setMethodAuth(contract, func, account, true);
            RetCode precompiledResponse =
                    PrecompiledRetCode.getPrecompiledResponse(openResult.intValue(), "Success");
            ConsoleUtils.printJson(
                    "{\"code\":"
                            + precompiledResponse.getCode()
                            + ", \"msg\":"
                            + "\""
                            + precompiledResponse.getMessage()
                            + "\"}");
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
            BigInteger closeResult = authManager.setMethodAuth(contract, func, account, false);
            RetCode precompiledResponse =
                    PrecompiledRetCode.getPrecompiledResponse(closeResult.intValue(), "Success");
            ConsoleUtils.printJson(
                    "{\"code\":"
                            + precompiledResponse.getCode()
                            + ", \"msg\":"
                            + "\""
                            + precompiledResponse.getMessage()
                            + "\"}");
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
