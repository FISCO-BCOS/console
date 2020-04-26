package console.precompiled.permission;

import console.common.Address;
import console.common.ConsoleUtils;
import console.common.HelpInfo;
import console.exception.ConsoleMessageException;
import io.bretty.console.table.Alignment;
import io.bretty.console.table.ColumnFormatter;
import io.bretty.console.table.Table;
import java.math.BigInteger;
import java.security.InvalidParameterException;
import java.util.List;
import org.fisco.bcos.channel.protocol.ChannelPrococolExceiption;
import org.fisco.bcos.fisco.EnumNodeVersion;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.precompile.common.PrecompiledCommon;
import org.fisco.bcos.web3j.precompile.permission.ChainGovernanceService;
import org.fisco.bcos.web3j.precompile.permission.PermissionInfo;
import org.fisco.bcos.web3j.precompile.permission.PermissionService;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.tuples.generated.Tuple2;

public class PermissionImpl implements PermissionFace {

    private Web3j web3j;
    private Credentials credentials;

    @Override
    public void setWeb3j(Web3j web3j) {
        this.web3j = web3j;
    }

    @Override
    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    @Override
    public void grantUserTableManager(String[] params) throws Exception {
        if (params.length < 2) {
            HelpInfo.promptHelp("grantUserTableManager");
            return;
        }
        if (params.length > 3) {
            HelpInfo.promptHelp("grantUserTableManager");
            return;
        }
        String tableName = params[1];
        if ("-h".equals(tableName) || "--help".equals(tableName)) {
            HelpInfo.grantUserTableManagerHelp();
            return;
        }
        if (params.length < 3) {
            HelpInfo.promptHelp("grantUserTableManager");
            return;
        }
        String address = params[2];
        Address convertAddr = ConsoleUtils.convertAddress(address);
        if (!convertAddr.isValid()) {
            return;
        }
        address = convertAddr.getAddress();
        PermissionService permission = new PermissionService(web3j, credentials);
        String result = null;
        result = permission.grantUserTableManager(tableName, address);
        ConsoleUtils.printJson(result);
        System.out.println();
    }

    @Override
    public void revokeUserTableManager(String[] params) throws Exception {
        if (params.length < 2) {
            HelpInfo.promptHelp("revokeUserTableManager");
            return;
        }
        if (params.length > 3) {
            HelpInfo.promptHelp("revokeUserTableManager");
            return;
        }
        String tableName = params[1];
        if ("-h".equals(tableName) || "--help".equals(tableName)) {
            HelpInfo.revokeUserTableManagerHelp();
            return;
        }
        if (params.length < 3) {
            HelpInfo.promptHelp("revokeUserTableManager");
            return;
        }
        String address = params[2];
        Address convertAddr = ConsoleUtils.convertAddress(address);
        if (!convertAddr.isValid()) {
            return;
        }
        address = convertAddr.getAddress();
        PermissionService permission = new PermissionService(web3j, credentials);
        String result = null;
        result = permission.revokeUserTableManager(tableName, address);
        ConsoleUtils.printJson(result);
        System.out.println();
    }

    @Override
    public void listUserTableManager(String[] params) throws Exception {
        if (params.length < 2) {
            HelpInfo.promptHelp("listUserTableManager");
            return;
        }
        if (params.length > 2) {
            HelpInfo.promptHelp("listUserTableManager");
            return;
        }
        String tableName = params[1];
        if ("-h".equals(tableName) || "--help".equals(tableName)) {
            HelpInfo.listUserTableManagerHelp();
            return;
        }
        PermissionService permissionTableService = new PermissionService(web3j, credentials);
        List<PermissionInfo> permissions = permissionTableService.listUserTableManager(tableName);
        printPermissionInfo(permissions);
    }

    @Override
    public void grantDeployAndCreateManager(String[] params) throws Exception {
        if (params.length < 2) {
            HelpInfo.promptHelp("grantDeployAndCreateManager");
            return;
        }
        if (params.length > 2) {
            HelpInfo.promptHelp("grantDeployAndCreateManager");
            return;
        }
        String address = params[1];
        if ("-h".equals(address) || "--help".equals(address)) {
            HelpInfo.grantDeployAndCreateManagerHelp();
            return;
        }
        Address convertAddr = ConsoleUtils.convertAddress(address);
        if (!convertAddr.isValid()) {
            return;
        }
        address = convertAddr.getAddress();
        PermissionService permission = new PermissionService(web3j, credentials);
        String result;
        result = permission.grantDeployAndCreateManager(address);
        ConsoleUtils.printJson(result);
        System.out.println();
    }

    @Override
    public void revokeDeployAndCreateManager(String[] params) throws Exception {
        if (params.length < 2) {
            HelpInfo.promptHelp("revokeDeployAndCreateManager");
            return;
        }
        if (params.length > 2) {
            HelpInfo.promptHelp("revokeDeployAndCreateManager");
            return;
        }
        String address = params[1];
        if ("-h".equals(address) || "--help".equals(address)) {
            HelpInfo.revokeDeployAndCreateManagerHelp();
            return;
        }
        Address convertAddr = ConsoleUtils.convertAddress(address);
        if (!convertAddr.isValid()) {
            return;
        }
        address = convertAddr.getAddress();
        PermissionService permission = new PermissionService(web3j, credentials);
        String result;
        result = permission.revokeDeployAndCreateManager(address);
        ConsoleUtils.printJson(result);
        System.out.println();
    }

    @Override
    public void listDeployAndCreateManager(String[] params) throws Exception {
        if (HelpInfo.promptNoParams(params, "listDeployAndCreateManager")) {
            return;
        }
        PermissionService permissionTableService = new PermissionService(web3j, credentials);
        List<PermissionInfo> permissions = permissionTableService.listDeployAndCreateManager();
        printPermissionInfo(permissions);
    }

    @Override
    public void grantPermissionManager(String[] params) throws Exception {
        if (params.length < 2) {
            HelpInfo.promptHelp("grantPermissionManager");
            return;
        }
        if (params.length > 2) {
            HelpInfo.promptHelp("grantPermissionManager");
            return;
        }
        String address = params[1];
        if ("-h".equals(address) || "--help".equals(address)) {
            HelpInfo.grantPermissionManagerHelp();
            return;
        }
        Address convertAddr = ConsoleUtils.convertAddress(address);
        if (!convertAddr.isValid()) {
            return;
        }
        address = convertAddr.getAddress();
        PermissionService permission = new PermissionService(web3j, credentials);
        String result;
        result = permission.grantPermissionManager(address);
        ConsoleUtils.printJson(result);
        System.out.println();
    }

    @Override
    public void revokePermissionManager(String[] params) throws Exception {
        if (params.length < 2) {
            HelpInfo.promptHelp("revokePermissionManager");
            return;
        }
        if (params.length > 2) {
            HelpInfo.promptHelp("revokePermissionManager");
            return;
        }
        String address = params[1];
        if ("-h".equals(address) || "--help".equals(address)) {
            HelpInfo.revokePermissionManagerHelp();
            return;
        }
        Address convertAddr = ConsoleUtils.convertAddress(address);
        if (!convertAddr.isValid()) {
            return;
        }
        address = convertAddr.getAddress();
        PermissionService permission = new PermissionService(web3j, credentials);
        String result;
        result = permission.revokePermissionManager(address);
        ConsoleUtils.printJson(result);
        System.out.println();
    }

    @Override
    public void listPermissionManager(String[] params) throws Exception {
        if (HelpInfo.promptNoParams(params, "listPermissionManager")) {
            return;
        }
        PermissionService permissionTableService = new PermissionService(web3j, credentials);
        List<PermissionInfo> permissions = permissionTableService.listPermissionManager();
        printPermissionInfo(permissions);
    }

    @Override
    public void grantNodeManager(String[] params) throws Exception {
        if (params.length < 2) {
            HelpInfo.promptHelp("grantNodeManager");
            return;
        }
        if (params.length > 2) {
            HelpInfo.promptHelp("grantNodeManager");
            return;
        }
        String address = params[1];
        if ("-h".equals(address) || "--help".equals(address)) {
            HelpInfo.grantNodeManagerHelp();
            return;
        }
        Address convertAddr = ConsoleUtils.convertAddress(address);
        if (!convertAddr.isValid()) {
            return;
        }
        address = convertAddr.getAddress();
        PermissionService permission = new PermissionService(web3j, credentials);
        String result;
        result = permission.grantNodeManager(address);
        ConsoleUtils.printJson(result);
        System.out.println();
    }

    @Override
    public void revokeNodeManager(String[] params) throws Exception {
        if (params.length < 2) {
            HelpInfo.promptHelp("revokeNodeManager");
            return;
        }
        if (params.length > 2) {
            HelpInfo.promptHelp("revokeNodeManager");
            return;
        }
        String address = params[1];
        if ("-h".equals(address) || "--help".equals(address)) {
            HelpInfo.revokeNodeManagerHelp();
            return;
        }
        Address convertAddr = ConsoleUtils.convertAddress(address);
        if (!convertAddr.isValid()) {
            return;
        }
        address = convertAddr.getAddress();
        PermissionService permission = new PermissionService(web3j, credentials);
        String result;
        result = permission.revokeNodeManager(address);
        ConsoleUtils.printJson(result);
        System.out.println();
    }

    @Override
    public void listNodeManager(String[] params) throws Exception {
        if (HelpInfo.promptNoParams(params, "listNodeManager")) {
            return;
        }
        PermissionService permissionTableService = new PermissionService(web3j, credentials);
        List<PermissionInfo> permissions = permissionTableService.listNodeManager();
        printPermissionInfo(permissions);
    }

    @Override
    public void grantCNSManager(String[] params) throws Exception {
        if (params.length < 2) {
            HelpInfo.promptHelp("grantCNSManager");
            return;
        }
        if (params.length > 2) {
            HelpInfo.promptHelp("grantCNSManager");
            return;
        }
        String address = params[1];
        if ("-h".equals(address) || "--help".equals(address)) {
            HelpInfo.grantCNSManagerHelp();
            return;
        }
        Address convertAddr = ConsoleUtils.convertAddress(address);
        if (!convertAddr.isValid()) {
            return;
        }
        address = convertAddr.getAddress();
        PermissionService permission = new PermissionService(web3j, credentials);
        String result;
        result = permission.grantCNSManager(address);
        ConsoleUtils.printJson(result);
        System.out.println();
    }

    @Override
    public void revokeCNSManager(String[] params) throws Exception {
        if (params.length < 2) {
            HelpInfo.promptHelp("revokeCNSManager");
            return;
        }
        if (params.length > 2) {
            HelpInfo.promptHelp("revokeCNSManager");
            return;
        }
        String address = params[1];
        if ("-h".equals(address) || "--help".equals(address)) {
            HelpInfo.revokeCNSManagerHelp();
            return;
        }
        Address convertAddr = ConsoleUtils.convertAddress(address);
        if (!convertAddr.isValid()) {
            return;
        }
        address = convertAddr.getAddress();
        PermissionService permission = new PermissionService(web3j, credentials);
        String result;
        result = permission.revokeCNSManager(address);
        ConsoleUtils.printJson(result);
        System.out.println();
    }

    @Override
    public void listCNSManager(String[] params) throws Exception {
        if (HelpInfo.promptNoParams(params, "listCNSManager")) {
            return;
        }
        PermissionService permissionTableService = new PermissionService(web3j, credentials);
        List<PermissionInfo> permissions = permissionTableService.listCNSManager();
        printPermissionInfo(permissions);
    }

    @Override
    public void grantSysConfigManager(String[] params) throws Exception {
        if (params.length < 2) {
            HelpInfo.promptHelp("grantSysConfigManager");
            return;
        }
        if (params.length > 2) {
            HelpInfo.promptHelp("grantSysConfigManager");
            return;
        }
        String address = params[1];
        if ("-h".equals(address) || "--help".equals(address)) {
            HelpInfo.grantSysConfigManagerHelp();
            return;
        }
        Address convertAddr = ConsoleUtils.convertAddress(address);
        if (!convertAddr.isValid()) {
            return;
        }
        address = convertAddr.getAddress();
        PermissionService permission = new PermissionService(web3j, credentials);
        String result;
        result = permission.grantSysConfigManager(address);
        ConsoleUtils.printJson(result);
        System.out.println();
    }

    @Override
    public void revokeSysConfigManager(String[] params) throws Exception {
        if (params.length < 2) {
            HelpInfo.promptHelp("revokeSysConfigManager");
            return;
        }
        if (params.length > 2) {
            HelpInfo.promptHelp("revokeSysConfigManager");
            return;
        }
        String address = params[1];
        if ("-h".equals(address) || "--help".equals(address)) {
            HelpInfo.revokeSysConfigManagerHelp();
            return;
        }
        Address convertAddr = ConsoleUtils.convertAddress(address);
        if (!convertAddr.isValid()) {
            return;
        }
        address = convertAddr.getAddress();
        PermissionService permission = new PermissionService(web3j, credentials);
        String result;
        result = permission.revokeSysConfigManager(address);
        ConsoleUtils.printJson(result);
        System.out.println();
    }

    @Override
    public void listSysConfigManager(String[] params) throws Exception {
        if (HelpInfo.promptNoParams(params, "listSysConfigManager")) {
            return;
        }
        PermissionService permissionTableService = new PermissionService(web3j, credentials);
        List<PermissionInfo> permissions = permissionTableService.listSysConfigManager();
        printPermissionInfo(permissions);
    }

    private void checkVersionForGrantWrite() throws ConsoleMessageException {
        String version = PrecompiledCommon.BCOS_VERSION;
        try {
            final EnumNodeVersion.Version classVersion = EnumNodeVersion.getClassVersion(version);

            if (!((classVersion.getMajor() == 2) && classVersion.getMinor() >= 3)) {
                throw new ConsoleMessageException(
                        "The fisco-bcos node version below 2.3.0 not support the command.");
            }

        } catch (ChannelPrococolExceiption channelPrococolExceiption) {
            throw new ConsoleMessageException(" The fisco-bcos node version is unknown.");
        }
    }

    @Override
    public void listContractWritePermission(String[] params) throws Exception {
        checkVersionForGrantWrite();

        if (params.length < 2) {
            HelpInfo.promptHelp("listContractWritePermission");
            return;
        }
        if (params.length > 2) {
            HelpInfo.promptHelp("listContractWritePermission");
            return;
        }

        if ("-h".equals(params[1]) || "--help".equals(params[1])) {
            HelpInfo.revokeSysConfigManagerHelp();
            return;
        }

        String address = params[1];
        Address convertAddr = ConsoleUtils.convertAddress(address);
        if (!convertAddr.isValid()) {
            return;
        }
        address = convertAddr.getAddress();

        PermissionService permissionTableService = new PermissionService(web3j, credentials);
        List<PermissionInfo> permissions = permissionTableService.queryPermission(address);
        printPermissionInfo(permissions);
    }

    @Override
    public void grantContractWritePermission(String[] params) throws Exception {
        checkVersionForGrantWrite();

        if ((params.length > 1) && ("-h".equals(params[1]) || "--help".equals(params[1]))) {
            HelpInfo.grantContractWritePermissionHelp();
            return;
        }

        if (params.length < 3) {
            HelpInfo.promptHelp("grantContractWritePermission");
            return;
        }
        if (params.length > 3) {
            HelpInfo.promptHelp("grantContractWritePermission");
            return;
        }

        String contractAddress = params[1];
        String userAddress = params[2];
        Address convertAddr = ConsoleUtils.convertAddress(contractAddress);
        if (!convertAddr.isValid()) {
            return;
        }
        contractAddress = convertAddr.getAddress();

        Address convertUserAddr = ConsoleUtils.convertAddress(userAddress);
        if (!convertUserAddr.isValid()) {
            return;
        }
        userAddress = convertUserAddr.getAddress();

        PermissionService permission = new PermissionService(web3j, credentials);
        String result = permission.grantWrite(contractAddress, userAddress);
        ConsoleUtils.printJson(result);
        System.out.println();
    }

    @Override
    public void revokeContractWritePermission(String[] params) throws Exception {
        checkVersionForGrantWrite();

        if ((params.length > 1) && ("-h".equals(params[1]) || "--help".equals(params[1]))) {
            HelpInfo.revokeContractWritePermissionHelp();
            return;
        }

        if (params.length < 3) {
            HelpInfo.promptHelp("revokeContractWritePermission");
            return;
        }
        if (params.length > 3) {
            HelpInfo.promptHelp("revokeContractWritePermission");
            return;
        }

        String contractAddress = params[1];
        String userAddress = params[2];
        Address convertAddr = ConsoleUtils.convertAddress(contractAddress);
        if (!convertAddr.isValid()) {
            return;
        }
        contractAddress = convertAddr.getAddress();

        Address convertUserAddr = ConsoleUtils.convertAddress(userAddress);
        if (!convertUserAddr.isValid()) {
            return;
        }
        userAddress = convertUserAddr.getAddress();

        PermissionService permission = new PermissionService(web3j, credentials);
        String result = permission.revokeWrite(contractAddress, userAddress);
        ConsoleUtils.printJson(result);
        System.out.println();
    }

    @Override
    public void grantCommitteeMember(String[] params) throws Exception {

        if (params.length < 2) {
            HelpInfo.promptHelp("grantCommitteeMember");
            return;
        }
        if (params.length > 2) {
            HelpInfo.promptHelp("grantCommitteeMember");
            return;
        }

        if ("-h".equals(params[1]) || "--help".equals(params[1])) {
            HelpInfo.grantCommitteeMemberHelp();
            return;
        }

        String accountAddress = params[1];
        Address convertAddr = ConsoleUtils.convertAddress(accountAddress);
        if (!convertAddr.isValid()) {
            return;
        }

        ChainGovernanceService chainGovernanceService =
                new ChainGovernanceService(web3j, credentials);
        String result = chainGovernanceService.grantCommitteeMember(accountAddress);
        ConsoleUtils.printJson(result);
        System.out.println();
    }

    @Override
    public void revokeCommitteeMember(String[] params) throws Exception {
        if (params.length < 2) {
            HelpInfo.promptHelp("revokeCommitteeMember");
            return;
        }
        if (params.length > 2) {
            HelpInfo.promptHelp("revokeCommitteeMember");
            return;
        }

        if ("-h".equals(params[1]) || "--help".equals(params[1])) {
            HelpInfo.revokeCommitteeMemberHelp();
            return;
        }

        String accountAddress = params[1];
        Address convertAddr = ConsoleUtils.convertAddress(accountAddress);
        if (!convertAddr.isValid()) {
            return;
        }

        ChainGovernanceService chainGovernanceService =
                new ChainGovernanceService(web3j, credentials);
        String result = chainGovernanceService.revokeCommitteeMember(accountAddress);
        ConsoleUtils.printJson(result);
        System.out.println();
    }

    @Override
    public void listCommitteeMembers(String[] params) throws Exception {
        if (HelpInfo.promptNoParams(params, "listCommitteeMembers")) {
            return;
        }

        ChainGovernanceService chainGovernanceService =
                new ChainGovernanceService(web3j, credentials);
        List<PermissionInfo> permissionInfos = chainGovernanceService.listCommitteeMembers();
        printPermissionInfo(permissionInfos);
    }

    @Override
    public void queryCommitteeMemberWeight(String[] params) throws Exception {
        if (params.length < 2) {
            HelpInfo.promptHelp("queryCommitteeMemberWeight");
            return;
        }
        if (params.length > 2) {
            HelpInfo.promptHelp("queryCommitteeMemberWeight");
            return;
        }

        if ("-h".equals(params[1]) || "--help".equals(params[1])) {
            HelpInfo.queryCommitteeMemberWeightHelp();
            return;
        }

        String accountAddress = params[1];
        Address convertAddr = ConsoleUtils.convertAddress(accountAddress);
        if (!convertAddr.isValid()) {
            return;
        }

        ChainGovernanceService chainGovernanceService =
                new ChainGovernanceService(web3j, credentials);
        Tuple2<Boolean, BigInteger> weight =
                chainGovernanceService.queryCommitteeMemberWeight(accountAddress);
        if (weight.getValue1()) {
            System.out.println("Account: " + accountAddress + " Weight: " + weight.getValue2());
            System.out.println();
        } else {
            System.out.println(PrecompiledCommon.transferToJson(weight.getValue2().intValue()));
            System.out.println();
        }
    }

    @Override
    public void updateCommitteeMemberWeight(String[] params) throws Exception {
        if (params.length < 2) {
            HelpInfo.promptHelp("updateCommitteeMemberWeight");
            return;
        }
        if (params.length > 3) {
            HelpInfo.promptHelp("updateCommitteeMemberWeight");
            return;
        }

        if ("-h".equals(params[1]) || "--help".equals(params[1])) {
            HelpInfo.updateCommitteeMemberWeightHelp();
            return;
        }

        String accountAddress = params[1];

        Integer weight = null;
        try {
            weight = Integer.parseInt(params[2]);
            if (weight <= 0) {
                throw new InvalidParameterException(" invalid weight .");
            }
        } catch (NumberFormatException e) {
            System.out.println("Please provide weight by non-negative integer mode .");
            System.out.println();
            return;
        }

        Address convertAddr = ConsoleUtils.convertAddress(accountAddress);
        if (!convertAddr.isValid()) {
            return;
        }

        ChainGovernanceService chainGovernanceService =
                new ChainGovernanceService(web3j, credentials);
        String result = chainGovernanceService.updateCommitteeMemberWeight(accountAddress, weight);
        ConsoleUtils.printJson(result);
        System.out.println();
    }

    @Override
    public void updateThreshold(String[] params) throws Exception {
        if (params.length < 2) {
            HelpInfo.promptHelp("updateThreshold");
            return;
        }
        if (params.length > 2) {
            HelpInfo.promptHelp("updateThreshold");
            return;
        }

        if ("-h".equals(params[1]) || "--help".equals(params[1])) {
            HelpInfo.updateThresholdHelp();
            return;
        }

        Integer threshold = null;
        try {
            threshold = Integer.parseInt(params[1]);
            if (threshold < 0 || threshold > 100) {
                throw new InvalidParameterException(" invalid threshold .");
            }
        } catch (Exception e) {
            System.out.println(
                    "Please provide threshold by non-negative integer mode, "
                            + " from 0 to 100 "
                            + ".");
            System.out.println();
            return;
        }

        ChainGovernanceService chainGovernanceService =
                new ChainGovernanceService(web3j, credentials);
        String result = chainGovernanceService.updateThreshold(threshold);
        ConsoleUtils.printJson(result);
        System.out.println();
    }

    @Override
    public void queryThreshold(String[] params) throws Exception {
        if (HelpInfo.promptNoParams(params, "queryThreshold")) {
            return;
        }

        ChainGovernanceService chainGovernanceService =
                new ChainGovernanceService(web3j, credentials);
        BigInteger threshold = chainGovernanceService.queryThreshold();
        System.out.println("Effective threshold : " + threshold + "%");
        System.out.println();
    }

    @Override
    public void grantOperator(String[] params) throws Exception {
        if (params.length < 2) {
            HelpInfo.promptHelp("grantOperator");
            return;
        }
        if (params.length > 2) {
            HelpInfo.promptHelp("grantOperator");
            return;
        }

        if ("-h".equals(params[1]) || "--help".equals(params[1])) {
            HelpInfo.grantOperatorHelp();
            return;
        }

        String accountAddress = params[1];
        Address convertAddr = ConsoleUtils.convertAddress(accountAddress);
        if (!convertAddr.isValid()) {
            return;
        }

        ChainGovernanceService chainGovernanceService =
                new ChainGovernanceService(web3j, credentials);
        String result = chainGovernanceService.grantOperator(accountAddress);
        ConsoleUtils.printJson(result);
        System.out.println();
    }

    @Override
    public void revokeOperator(String[] params) throws Exception {
        if (params.length < 2) {
            HelpInfo.promptHelp("revokeOperator");
            return;
        }
        if (params.length > 2) {
            HelpInfo.promptHelp("revokeOperator");
            return;
        }

        if ("-h".equals(params[1]) || "--help".equals(params[1])) {
            HelpInfo.revokeOperatorHelp();
            return;
        }

        String accountAddress = params[1];
        Address convertAddr = ConsoleUtils.convertAddress(accountAddress);
        if (!convertAddr.isValid()) {
            return;
        }

        ChainGovernanceService chainGovernanceService =
                new ChainGovernanceService(web3j, credentials);
        String result = chainGovernanceService.revokeOperator(accountAddress);
        ConsoleUtils.printJson(result);
        System.out.println();
    }

    @Override
    public void listOperators(String[] params) throws Exception {
        if (HelpInfo.promptNoParams(params, "listOperators")) {
            return;
        }

        ChainGovernanceService chainGovernanceService =
                new ChainGovernanceService(web3j, credentials);
        List<PermissionInfo> permissionInfos = chainGovernanceService.listOperators();
        printPermissionInfo(permissionInfos);
    }

    @Override
    public void freezeAccount(String[] params) throws Exception {
        if (params.length < 2) {
            HelpInfo.promptHelp("freezeAccount");
            return;
        }
        if (params.length > 2) {
            HelpInfo.promptHelp("freezeAccount");
            return;
        }

        if ("-h".equals(params[1]) || "--help".equals(params[1])) {
            HelpInfo.freezeAccountHelp();
            return;
        }

        String accountAddress = params[1];
        Address convertAddr = ConsoleUtils.convertAddress(accountAddress);
        if (!convertAddr.isValid()) {
            return;
        }

        ChainGovernanceService chainGovernanceService =
                new ChainGovernanceService(web3j, credentials);
        String result = chainGovernanceService.freezeAccount(accountAddress);
        ConsoleUtils.printJson(result);
        System.out.println();
    }

    @Override
    public void unfreezeAccount(String[] params) throws Exception {
        if (params.length < 2) {
            HelpInfo.promptHelp("unfreezeAccount");
            return;
        }
        if (params.length > 2) {
            HelpInfo.promptHelp("unfreezeAccount");
            return;
        }

        if ("-h".equals(params[1]) || "--help".equals(params[1])) {
            HelpInfo.unfreezeAccountHelp();
            return;
        }

        String accountAddress = params[1];
        Address convertAddr = ConsoleUtils.convertAddress(accountAddress);
        if (!convertAddr.isValid()) {
            return;
        }

        ChainGovernanceService chainGovernanceService =
                new ChainGovernanceService(web3j, credentials);
        String result = chainGovernanceService.unfreezeAccount(accountAddress);
        ConsoleUtils.printJson(result);
        System.out.println();
    }

    @Override
    public void getAccountStatus(String[] params) throws Exception {
        if (params.length < 2) {
            HelpInfo.promptHelp("getAccountStatus");
            return;
        }
        if (params.length > 2) {
            HelpInfo.promptHelp("getAccountStatus");
            return;
        }

        if ("-h".equals(params[1]) || "--help".equals(params[1])) {
            HelpInfo.getAccountStatusHelp();
            return;
        }

        String accountAddress = params[1];
        Address convertAddr = ConsoleUtils.convertAddress(accountAddress);
        if (!convertAddr.isValid()) {
            return;
        }

        ChainGovernanceService chainGovernanceService =
                new ChainGovernanceService(web3j, credentials);
        String result = chainGovernanceService.getAccountStatus(accountAddress);
        ConsoleUtils.printJson(result);
        System.out.println();
    }

    private void printPermissionInfo(List<PermissionInfo> permissionInfos) {
        if (permissionInfos.isEmpty()) {
            System.out.println("Empty set.");
            System.out.println();
            return;
        }
        ConsoleUtils.singleLine();
        String[] headers = {"address", "enable_num"};
        int size = permissionInfos.size();
        String[][] data = new String[size][2];
        for (int i = 0; i < size; i++) {
            data[i][0] = permissionInfos.get(i).getAddress();
            data[i][1] = permissionInfos.get(i).getEnableNum();
        }
        ColumnFormatter<String> cf = ColumnFormatter.text(Alignment.CENTER, 45);
        Table table = Table.of(headers, data, cf);
        System.out.println(table);
        ConsoleUtils.singleLine();
        System.out.println();
    }
}
