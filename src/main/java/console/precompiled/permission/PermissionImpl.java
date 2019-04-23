package console.precompiled.permission;

import java.util.List;

import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.precompile.permission.PermissionInfo;
import org.fisco.bcos.web3j.precompile.permission.PermissionService;
import org.fisco.bcos.web3j.protocol.Web3j;

import console.common.Address;
import console.common.ConsoleUtils;
import console.common.HelpInfo;
import io.bretty.console.table.Alignment;
import io.bretty.console.table.ColumnFormatter;
import io.bretty.console.table.Table;

public class PermissionImpl implements PermissionFace {
		
	  private Web3j web3j;
	  private Credentials credentials;
	  
		@Override
		public void setWeb3j(Web3j web3j)
		{
			this.web3j = web3j;
		}
		@Override
		public void setCredentials(Credentials credentials)
		{
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
        if (HelpInfo.promptNoParams(params, "listyDeployAndCreateManager")) {
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

    private void printPermissionInfo(List<PermissionInfo> permissionInfos) {
        if (permissionInfos.isEmpty()) {
            System.out.println("Empty set.");
            System.out.println();
            return;
        }
        ConsoleUtils.singleLineForTable();
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
        ConsoleUtils.singleLineForTable();
        System.out.println();
    }
}
