package console.precompiled.permission;

import console.common.Address;
import console.common.ConsoleUtils;
import console.common.HelpInfo;
import io.bretty.console.table.Alignment;
import io.bretty.console.table.ColumnFormatter;
import io.bretty.console.table.Table;
import java.math.BigInteger;
import java.security.InvalidParameterException;
import java.util.List;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.contract.exceptions.ContractException;
import org.fisco.bcos.sdk.contract.precompiled.permission.ChainGovernanceService;
import org.fisco.bcos.sdk.contract.precompiled.permission.PermissionInfo;
import org.fisco.bcos.sdk.contract.precompiled.permission.PermissionService;

public class PermissionImpl implements PermissionFace {

    private Client client;
    private PermissionService permissionService;
    private ChainGovernanceService chainGovernanceService;

    public PermissionImpl(Client client) {
        this.client = client;
        this.permissionService = new PermissionService(client, client.getCryptoInterface());
        this.chainGovernanceService =
                new ChainGovernanceService(client, client.getCryptoInterface());
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
        ConsoleUtils.printRetCode(this.permissionService.grantPermission(tableName, address));
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
        ConsoleUtils.printRetCode(this.permissionService.revokePermission(tableName, address));
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
        List<PermissionInfo> permissions =
                this.permissionService.queryPermissionByTableName(tableName);
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
        ConsoleUtils.printRetCode(this.permissionService.grantDeployAndCreateManager(address));
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
        ConsoleUtils.printRetCode(this.permissionService.revokeDeployAndCreateManager(address));
        System.out.println();
    }

    @Override
    public void listDeployAndCreateManager(String[] params) throws Exception {
        if (HelpInfo.promptNoParams(params, "listDeployAndCreateManager")) {
            return;
        }
        List<PermissionInfo> permissions = this.permissionService.listDeployAndCreateManager();
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
        ConsoleUtils.printRetCode(this.permissionService.grantNodeManager(address));
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
        ConsoleUtils.printRetCode(this.permissionService.revokeNodeManager(address));
        System.out.println();
    }

    @Override
    public void listNodeManager(String[] params) throws Exception {
        if (HelpInfo.promptNoParams(params, "listNodeManager")) {
            return;
        }
        List<PermissionInfo> permissions = this.permissionService.listNodeManager();
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
        ConsoleUtils.printRetCode(this.permissionService.grantCNSManager(address));
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
        ConsoleUtils.printRetCode(
                this.permissionService.revokeCNSManager(convertAddr.getAddress()));
        System.out.println();
    }

    @Override
    public void listCNSManager(String[] params) throws Exception {
        if (HelpInfo.promptNoParams(params, "listCNSManager")) {
            return;
        }
        List<PermissionInfo> permissions = this.permissionService.listCNSManager();
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
        ConsoleUtils.printRetCode(
                this.permissionService.grantSysConfigManager(convertAddr.getAddress()));
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
        ConsoleUtils.printRetCode(
                this.permissionService.revokeSysConfigManager(convertAddr.getAddress()));
        System.out.println();
    }

    @Override
    public void listSysConfigManager(String[] params) throws Exception {
        if (HelpInfo.promptNoParams(params, "listSysConfigManager")) {
            return;
        }
        List<PermissionInfo> permissions = this.permissionService.listSysConfigManager();
        printPermissionInfo(permissions);
    }

    @Override
    public void listContractWritePermission(String[] params) throws Exception {

        if (params.length < 2) {
            HelpInfo.promptHelp("listContractWritePermission");
            return;
        }
        if (params.length > 2) {
            HelpInfo.promptHelp("listContractWritePermission");
            return;
        }

        if ("-h".equals(params[1]) || "--help".equals(params[1])) {
            HelpInfo.listContractWritePermissionHelp();
            return;
        }

        String address = params[1];
        Address convertAddr = ConsoleUtils.convertAddress(address);
        if (!convertAddr.isValid()) {
            return;
        }
        address = convertAddr.getAddress();
        List<PermissionInfo> permissions = this.permissionService.queryPermission(address);
        printPermissionInfo(permissions);
    }

    @Override
    public void grantContractWritePermission(String[] params) throws Exception {

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
        ConsoleUtils.printRetCode(this.permissionService.grantWrite(contractAddress, userAddress));
        System.out.println();
    }

    @Override
    public void revokeContractWritePermission(String[] params) throws Exception {
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
        ConsoleUtils.printRetCode(this.permissionService.revokeWrite(contractAddress, userAddress));
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
        ConsoleUtils.printRetCode(chainGovernanceService.grantCommitteeMember(accountAddress));
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
        ConsoleUtils.printRetCode(
                this.chainGovernanceService.revokeCommitteeMember(accountAddress));
        System.out.println();
    }

    @Override
    public void listCommitteeMembers(String[] params) throws Exception {
        if (HelpInfo.promptNoParams(params, "listCommitteeMembers")) {
            return;
        }
        List<PermissionInfo> permissionInfos = this.chainGovernanceService.listCommitteeMembers();
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
        try {
            BigInteger weight =
                    this.chainGovernanceService.queryCommitteeMemberWeight(accountAddress);
            System.out.println("Account: " + accountAddress + " Weight: " + weight);
        } catch (ContractException e) {
            System.out.println("queryCommitteeMemberWeight failed: " + e.getMessage());
        }
    }

    @Override
    public void updateCommitteeMemberWeight(String[] params) throws Exception {

        if ((params.length >= 2) && ("-h".equals(params[1]) || "--help".equals(params[1]))) {
            HelpInfo.updateCommitteeMemberWeightHelp();
            return;
        }

        if (params.length < 3) {
            HelpInfo.promptHelp("updateCommitteeMemberWeight");
            return;
        }
        if (params.length > 3) {
            HelpInfo.promptHelp("updateCommitteeMemberWeight");
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
            System.out.println(
                    "Please provide weight by non-negative integer mode(from 1 to 2147483647) .");
            System.out.println();
            return;
        }

        Address convertAddr = ConsoleUtils.convertAddress(accountAddress);
        if (!convertAddr.isValid()) {
            return;
        }
        ConsoleUtils.printRetCode(
                this.chainGovernanceService.updateCommitteeMemberWeight(
                        accountAddress, BigInteger.valueOf(weight)));
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
            if (threshold < 0 || threshold >= 100) {
                throw new InvalidParameterException(" invalid threshold .");
            }
        } catch (Exception e) {
            System.out.println(
                    "Please provide threshold by non-negative integer mode, "
                            + " from 0 to 99 "
                            + ".");
            System.out.println();
            return;
        }
        ConsoleUtils.printRetCode(
                this.chainGovernanceService.updateThreshold(BigInteger.valueOf(threshold)));
        System.out.println();
    }

    @Override
    public void queryThreshold(String[] params) throws Exception {
        if (HelpInfo.promptNoParams(params, "queryThreshold")) {
            return;
        }

        BigInteger threshold = this.chainGovernanceService.queryThreshold();
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
        ConsoleUtils.printRetCode(chainGovernanceService.grantOperator(accountAddress));
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
        ConsoleUtils.printRetCode(chainGovernanceService.revokeOperator(accountAddress));
        System.out.println();
    }

    @Override
    public void listOperators(String[] params) throws Exception {
        if (HelpInfo.promptNoParams(params, "listOperators")) {
            return;
        }
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
        ConsoleUtils.printRetCode(chainGovernanceService.freezeAccount(accountAddress));
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
        ConsoleUtils.printRetCode(chainGovernanceService.unfreezeAccount(accountAddress));
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
        ConsoleUtils.printRetCode(chainGovernanceService.getAccountStatus(accountAddress));
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
