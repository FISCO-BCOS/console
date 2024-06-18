package console.precompiled;

import console.ConsoleInitializer;
import console.common.Common;
import console.common.ConsoleUtils;
import console.contract.model.AbiAndBin;
import console.contract.utils.ContractCompiler;
import console.exception.ConsoleMessageException;
import console.precompiled.model.CRUDParseUtils;
import console.precompiled.model.Table;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import net.sf.jsqlparser.JSQLParserException;
import org.apache.commons.io.FilenameUtils;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.client.protocol.response.Abi;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.v3.contract.precompiled.balance.BalanceService;
import org.fisco.bcos.sdk.v3.contract.precompiled.bfs.BFSInfo;
import org.fisco.bcos.sdk.v3.contract.precompiled.bfs.BFSPrecompiled.BfsInfo;
import org.fisco.bcos.sdk.v3.contract.precompiled.bfs.BFSService;
import org.fisco.bcos.sdk.v3.contract.precompiled.consensus.ConsensusService;
import org.fisco.bcos.sdk.v3.contract.precompiled.crud.TableCRUDService;
import org.fisco.bcos.sdk.v3.contract.precompiled.crud.common.Condition;
import org.fisco.bcos.sdk.v3.contract.precompiled.crud.common.ConditionV320;
import org.fisco.bcos.sdk.v3.contract.precompiled.crud.common.Entry;
import org.fisco.bcos.sdk.v3.contract.precompiled.crud.common.UpdateFields;
import org.fisco.bcos.sdk.v3.contract.precompiled.sharding.ShardingService;
import org.fisco.bcos.sdk.v3.contract.precompiled.sysconfig.SystemConfigService;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.v3.model.EnumNodeVersion;
import org.fisco.bcos.sdk.v3.model.PrecompiledConstant;
import org.fisco.bcos.sdk.v3.model.PrecompiledRetCode;
import org.fisco.bcos.sdk.v3.model.RetCode;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;
import org.fisco.bcos.sdk.v3.transaction.tools.Convert;
import org.fisco.bcos.sdk.v3.utils.AddressUtils;
import org.fisco.bcos.sdk.v3.utils.Numeric;
import org.fisco.bcos.sdk.v3.utils.ObjectMapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrecompiledImpl implements PrecompiledFace {

    private static final Logger logger = LoggerFactory.getLogger(PrecompiledImpl.class);
    private Client client;
    private ConsensusService consensusService;
    private SystemConfigService systemConfigService;
    private TableCRUDService tableCRUDService;
    private BFSService bfsService;
    private ShardingService shardingService;
    private BalanceService balanceService;
    private String pwd = "/apps";

    public PrecompiledImpl(Client client) {
        this.client = client;
        CryptoKeyPair cryptoKeyPair = client.getCryptoSuite().getCryptoKeyPair();
        this.consensusService = new ConsensusService(client, cryptoKeyPair);
        this.systemConfigService = new SystemConfigService(client, cryptoKeyPair);
        this.tableCRUDService = new TableCRUDService(client, cryptoKeyPair);
        this.bfsService = new BFSService(client, cryptoKeyPair);
        this.shardingService = new ShardingService(client, cryptoKeyPair);
        this.balanceService = new BalanceService(client, cryptoKeyPair);
    }

    @Override
    public void addSealer(String[] params) throws Exception {
        String nodeId = params[1];
        int weight = ConsoleUtils.processNonNegativeNumber("consensusWeight", params[2]);
        if (nodeId.length() != 128) {
            ConsoleUtils.printJson(PrecompiledRetCode.CODE_INVALID_NODEID.toString());
        } else {
            RetCode retCode = this.consensusService.addSealer(nodeId, BigInteger.valueOf(weight));
            ConsoleUtils.printJson(retCode.toString());
            if (retCode.getCode() == PrecompiledRetCode.CODE_NO_AUTHORIZED.getCode()) {
                System.out.println(
                        "Maybe you should use 'addSealerProposal' command to change system config.");
            }
        }
    }

    @Override
    public void addObserver(String[] params) throws Exception {
        String nodeId = params[1];
        if (nodeId.length() != 128) {
            ConsoleUtils.printJson(PrecompiledRetCode.CODE_INVALID_NODEID.toString());
        } else {
            RetCode retCode = this.consensusService.addObserver(nodeId);
            ConsoleUtils.printJson(retCode.toString());
            if (retCode.getCode() == PrecompiledRetCode.CODE_NO_AUTHORIZED.getCode()) {
                System.out.println(
                        "Maybe you should use 'addObserverProposal' command to change system config.");
            }
        }
    }

    @Override
    public void removeNode(String[] params) throws Exception {
        String nodeId = params[1];
        if (nodeId.length() != 128) {
            ConsoleUtils.printJson(PrecompiledRetCode.CODE_INVALID_NODEID.toString());
        } else {
            RetCode retCode = this.consensusService.removeNode(nodeId);
            ConsoleUtils.printJson(retCode.toString());
            if (retCode.getCode() == PrecompiledRetCode.CODE_NO_AUTHORIZED.getCode()) {
                System.out.println(
                        "Maybe you should use 'removeNodeProposal' command to change system config.");
            }
        }
    }

    @Override
    public void setConsensusNodeWeight(String[] params) throws Exception {
        String nodeId = params[1];
        int weight = ConsoleUtils.processNonNegativeNumber("consensusWeight", params[2]);
        if (nodeId.length() != 128) {
            ConsoleUtils.printJson(PrecompiledRetCode.CODE_INVALID_NODEID.toString());
        } else {
            RetCode retCode = this.consensusService.setWeight(nodeId, BigInteger.valueOf(weight));
            ConsoleUtils.printJson(retCode.toString());
            if (retCode.getCode() == PrecompiledRetCode.CODE_NO_AUTHORIZED.getCode()) {
                System.out.println(
                        "Maybe you should use 'setConsensusNodeWeightProposal' command to change system config.");
            }
        }
    }

    @Override
    public void setSystemConfigByKey(ConsoleInitializer consoleInitializer, String[] params)
            throws Exception {
        String key = params[1];
        String value = params[2];
        if (params.length > 3 && key.equals(SystemConfigService.TX_GAS_PRICE)) {
            Convert.Unit unit = Convert.Unit.fromString(params[3]);
            BigDecimal weiValue = Convert.toWei(value, unit);
            value = weiValue.toBigIntegerExact().toString();
        }
        RetCode retCode = this.systemConfigService.setValueByKey(key, value);
        ConsoleUtils.printJson(retCode.toString());
        if (retCode.getCode() == PrecompiledRetCode.CODE_NO_AUTHORIZED.getCode()) {
            System.out.println(
                    "Maybe you should use 'setSysConfigProposal' command to change system config.");
        }
        if (key.equals(SystemConfigService.COMPATIBILITY_VERSION)
                && retCode.code == PrecompiledRetCode.CODE_SUCCESS.code) {
            String[] param = new String[2];
            param[1] = consoleInitializer.getGroupID();
            consoleInitializer.switchGroup(param);
        }
    }

    @Override
    public void desc(String[] params) throws Exception {
        String tableName = params[1];
        CRUDParseUtils.invalidSymbol(tableName);
        if (tableName.endsWith(";")) {
            tableName = tableName.substring(0, tableName.length() - 1);
        }
        Map<String, List<String>> tableDesc;
        EnumNodeVersion.Version supportedVersion =
                EnumNodeVersion.valueFromCompatibilityVersion(tableCRUDService.getCurrentVersion());
        if (supportedVersion.compareTo(EnumNodeVersion.BCOS_3_2_0.toVersionObj()) >= 0) {
            tableDesc = tableCRUDService.descWithKeyOrder(tableName);
        } else {
            tableDesc = tableCRUDService.desc(tableName);
        }
        ConsoleUtils.printJson(ObjectMapperFactory.getObjectMapper().writeValueAsString(tableDesc));
    }

    @Override
    public void createTable(String sql) throws Exception {
        Table table = new Table();
        try {
            CRUDParseUtils.parseCreateTable(sql, table);
        } catch (ConsoleMessageException e) {
            System.out.println(e.getMessage());
            logger.error(" message: {}, e:", e.getMessage(), e);
            return;
        } catch (JSQLParserException | NullPointerException e) {
            logger.error(" message: {}, e:", e.getMessage(), e);
            System.out.println("Could not parse SQL statement.");
            CRUDParseUtils.invalidSymbol(sql);
            return;
        }
        CRUDParseUtils.parseCreateTable(sql, table);
        EnumNodeVersion.Version supportedVersion =
                EnumNodeVersion.valueFromCompatibilityVersion(tableCRUDService.getCurrentVersion());
        RetCode result;
        if (supportedVersion.compareTo(EnumNodeVersion.BCOS_3_2_0.toVersionObj()) >= 0) {
            result =
                    tableCRUDService.createTable(
                            table.getTableName(),
                            table.getKeyOrder(),
                            table.getKeyFieldName(),
                            table.getValueFields());
        } else {
            result =
                    tableCRUDService.createTable(
                            table.getTableName(), table.getKeyFieldName(), table.getValueFields());
        }

        // parse the result
        if (result.getCode() == PrecompiledRetCode.CODE_SUCCESS.getCode()) {
            System.out.println("Create '" + table.getTableName() + "' Ok.");
        } else {
            System.out.println("Create '" + table.getTableName() + "' failed ");
            ConsoleUtils.printJson(result.toString());
        }
    }

    @Override
    public void alterTable(String sql) throws Exception {
        try {
            Table table = CRUDParseUtils.parseAlterTable(sql);
            RetCode result =
                    tableCRUDService.appendColumns(table.getTableName(), table.getValueFields());
            // parse the result
            if (result.getCode() == PrecompiledRetCode.CODE_SUCCESS.getCode()) {
                System.out.println("Alter '" + table.getTableName() + "' Ok.");
            } else {
                System.out.println("Alter '" + table.getTableName() + "' failed ");
                ConsoleUtils.printJson(result.toString());
            }
        } catch (ConsoleMessageException e) {
            System.out.println(e.getMessage());
            logger.error(" message: {}, e:", e.getMessage(), e);
        } catch (JSQLParserException | NullPointerException e) {
            logger.error(" message: {}, e:", e.getMessage(), e);
            System.out.println("Could not parse SQL statement.");
            CRUDParseUtils.invalidSymbol(sql);
        }
    }

    @Override
    public void insert(String sql) throws Exception {
        try {
            Table table = new Table();
            String tableName = CRUDParseUtils.parseTableNameFromSql(sql);
            EnumNodeVersion.Version supportedVersion =
                    EnumNodeVersion.valueFromCompatibilityVersion(
                            tableCRUDService.getCurrentVersion());
            Map<String, List<String>> descTable;
            if (supportedVersion.compareTo(EnumNodeVersion.BCOS_3_2_0.toVersionObj()) >= 0) {
                descTable = tableCRUDService.descWithKeyOrder(tableName);
            } else {
                descTable = tableCRUDService.desc(tableName);
            }
            table.setTableName(tableName);
            if (!checkTableExistence(descTable)) {
                System.out.println("The table \"" + tableName + "\" doesn't exist!");
                return;
            }
            logger.debug("insert, tableName: {}, descTable: {}", tableName, descTable);
            Entry entry = CRUDParseUtils.parseInsert(sql, table, descTable);
            String keyName = descTable.get(PrecompiledConstant.KEY_FIELD_NAME).get(0);
            String keyValue = entry.getKey();
            logger.debug(
                    "fieldNameToValue: {}, keyName: {}, keyValue: {}",
                    entry.getFieldNameToValue(),
                    keyName,
                    keyValue);
            if (keyValue == null) {
                throw new ConsoleMessageException("Please insert the key field '" + keyName + "'.");
            }
            RetCode insertResult = tableCRUDService.insert(table.getTableName(), entry);

            if (insertResult.getCode() >= 0) {
                System.out.println("Insert OK: ");
                System.out.println(insertResult.getCode() + " row(s) affected.");
            } else {
                System.out.println("Result of insert for " + table.getTableName() + ":");
                ConsoleUtils.printJson(insertResult.toString());
            }

        } catch (ConsoleMessageException e) {
            System.out.println(e.getMessage());
            logger.error(" message: {}, e:", e.getMessage(), e);
        } catch (JSQLParserException | NullPointerException e) {
            System.out.println("Could not parse SQL statement, error message: " + e.getMessage());
            CRUDParseUtils.invalidSymbol(sql);
        }
    }

    @Override
    public void update(String sql) throws Exception {
        try {
            Table table = new Table();
            UpdateFields updateFields = new UpdateFields();
            String tableName = CRUDParseUtils.parseTableNameFromSql(sql);
            Map<String, List<String>> descTable = tableCRUDService.desc(tableName);
            if (!checkTableExistence(descTable)) {
                System.out.println("The table \"" + tableName + "\" doesn't exist!");
                return;
            }
            String keyName = descTable.get(PrecompiledConstant.KEY_FIELD_NAME).get(0);
            if (updateFields.getFieldNameToValue().containsKey(keyName)) {
                System.out.println("Please don't set the key field \"" + keyName + "\".");
                return;
            }
            table.setKeyFieldName(keyName);
            table.setValueFields(descTable.get(PrecompiledConstant.VALUE_FIELD_NAME));

            EnumNodeVersion.Version supportedVersion =
                    EnumNodeVersion.valueFromCompatibilityVersion(
                            tableCRUDService.getCurrentVersion());
            RetCode updateResult;
            if (supportedVersion.compareTo(EnumNodeVersion.BCOS_3_2_0.toVersionObj()) >= 0) {
                ConditionV320 conditionV320 = new ConditionV320();
                CRUDParseUtils.parseUpdate(sql, table, conditionV320, updateFields);
                updateResult = tableCRUDService.update(tableName, conditionV320, updateFields);
            } else {
                Condition condition = new Condition();
                CRUDParseUtils.parseUpdate(sql, table, condition, updateFields);

                String keyValue = condition.getEqValue();
                updateResult =
                        keyValue.isEmpty()
                                ? tableCRUDService.update(tableName, condition, updateFields)
                                : tableCRUDService.update(tableName, keyValue, updateFields);
            }

            if (updateResult.getCode() >= 0) {
                System.out.println(updateResult.getCode() + " row affected.");
            } else {
                System.out.println("Result of update " + tableName + " :");
                ConsoleUtils.printJson(updateResult.toString());
            }
        } catch (ConsoleMessageException e) {
            System.out.println(e.getMessage());
            logger.error(" message: {}, e: ", e.getMessage(), e);
        } catch (JSQLParserException | NullPointerException e) {
            System.out.println("Could not parse SQL statement.");
            logger.error(" message: {}, e: ", e.getMessage(), e);
            CRUDParseUtils.invalidSymbol(sql);
        }
    }

    @Override
    public void remove(String sql) throws Exception {
        try {
            Table table = new Table();
            String tableName = CRUDParseUtils.parseTableNameFromSql(sql);
            Map<String, List<String>> descTable = tableCRUDService.desc(tableName);
            table.setTableName(tableName);
            if (!checkTableExistence(descTable)) {
                System.out.println("The table \"" + table.getTableName() + "\" doesn't exist!");
                return;
            }
            table.setKeyFieldName(descTable.get(PrecompiledConstant.KEY_FIELD_NAME).get(0));
            table.setValueFields(descTable.get(PrecompiledConstant.VALUE_FIELD_NAME));

            EnumNodeVersion.Version supportedVersion =
                    EnumNodeVersion.valueFromCompatibilityVersion(
                            tableCRUDService.getCurrentVersion());

            RetCode removeResult;
            if (supportedVersion.compareTo(EnumNodeVersion.BCOS_3_2_0.toVersionObj()) >= 0) {
                ConditionV320 conditionV320 = new ConditionV320();
                CRUDParseUtils.parseRemove(sql, table, conditionV320);
                removeResult = tableCRUDService.remove(table.getTableName(), conditionV320);
            } else {
                Condition condition = new Condition();
                CRUDParseUtils.parseRemove(sql, table, condition);
                String keyValue = condition.getEqValue();
                removeResult =
                        keyValue.isEmpty()
                                ? tableCRUDService.remove(table.getTableName(), condition)
                                : tableCRUDService.remove(table.getTableName(), keyValue);
            }

            if (removeResult.getCode() >= 0) {
                System.out.println("Remove OK, " + removeResult.getCode() + " row(s) affected.");
            } else {
                System.out.println("Result of Remove " + table.getTableName() + " :");
                ConsoleUtils.printJson(removeResult.toString());
            }
        } catch (ConsoleMessageException e) {
            System.out.println(e.getMessage());
            logger.error(" message: {}, e: {}", e.getMessage(), e);
        } catch (JSQLParserException | NullPointerException e) {
            System.out.println("Could not parse SQL statement.");
            logger.error(" message: {}, e: ", e.getMessage(), e);
            CRUDParseUtils.invalidSymbol(sql);
        }
    }

    private boolean checkTableExistence(Map<String, List<String>> descTable) {
        return descTable.size() != 0
                && !descTable.get(PrecompiledConstant.KEY_FIELD_NAME).get(0).isEmpty();
    }

    @Override
    public void select(String sql) throws ConsoleMessageException, ContractException {
        try {
            Table table = new Table();
            List<String> selectColumns = new ArrayList<>();
            String tableName = CRUDParseUtils.parseTableNameFromSql(sql);
            table.setTableName(tableName);
            Map<String, List<String>> descTable = tableCRUDService.desc(tableName);
            if (!checkTableExistence(descTable)) {
                System.out.println("The table \"" + table.getTableName() + "\" doesn't exist!");
                return;
            }
            String keyField = descTable.get(PrecompiledConstant.KEY_FIELD_NAME).get(0);
            table.setKeyFieldName(keyField);
            table.setValueFields(descTable.get(PrecompiledConstant.VALUE_FIELD_NAME));

            EnumNodeVersion.Version supportedVersion =
                    EnumNodeVersion.valueFromCompatibilityVersion(
                            tableCRUDService.getCurrentVersion());
            List<Map<String, String>> result = new ArrayList<>();
            if (supportedVersion.compareTo(EnumNodeVersion.BCOS_3_2_0.toVersionObj()) >= 0) {
                ConditionV320 conditionV320 = new ConditionV320();
                CRUDParseUtils.parseSelect(sql, table, selectColumns, conditionV320);
                result = tableCRUDService.select(table.getTableName(), descTable, conditionV320);
            } else {
                Condition condition = new Condition();
                CRUDParseUtils.parseSelect(sql, table, selectColumns, condition);
                String keyValue = condition.getEqValue();
                if (keyValue.isEmpty()) {
                    result = tableCRUDService.select(table.getTableName(), descTable, condition);
                } else {
                    Map<String, String> select =
                            tableCRUDService.select(table.getTableName(), descTable, keyValue);
                    if (select.isEmpty()) {
                        System.out.println("Empty set.");
                        return;
                    }
                    result.add(select);
                }
            }
            int rows;
            if (result.isEmpty()) {
                System.out.println("Empty set.");
                return;
            }
            if ("*".equals(selectColumns.get(0))) {
                selectColumns.clear();
                selectColumns.add(keyField);
                selectColumns.addAll(descTable.get(PrecompiledConstant.VALUE_FIELD_NAME));
                result = getSelectedColumn(selectColumns, result);
                rows = result.size();
            } else {
                List<Map<String, String>> selectedResult = getSelectedColumn(selectColumns, result);
                rows = selectedResult.size();
            }
            System.out.println(rows + " row(s) in set.");
        } catch (ConsoleMessageException e) {
            System.out.println(e.getMessage());
            logger.error(" message: {}, e:", e.getMessage(), e);
        } catch (JSQLParserException | NullPointerException e) {
            System.out.println("Could not parse SQL statement.");
            logger.error(" message: {}, e: ", e.getMessage(), e);
            CRUDParseUtils.invalidSymbol(sql);
        }
    }

    private List<Map<String, String>> getSelectedColumn(
            List<String> selectColumns, List<Map<String, String>> result) {
        List<Map<String, String>> selectedResult = new ArrayList<>(result.size());
        Map<String, String> selectedRecords;
        for (Map<String, String> records : result) {
            selectedRecords = new LinkedHashMap<>();
            for (String column : selectColumns) {
                Set<String> recordKeys = records.keySet();
                for (String recordKey : recordKeys) {
                    if (recordKey.equals(column)) {
                        selectedRecords.put(recordKey, records.get(recordKey));
                    }
                }
            }
            selectedResult.add(selectedRecords);
        }
        selectedResult.forEach(System.out::println);
        return selectedResult;
    }

    @Override
    public void changeDir(String[] params) throws Exception {
        if (params.length == 1) {
            System.out.println("cd: change dir to /apps");
            pwd = "/apps";
            return;
        }
        String path = ConsoleUtils.fixedBfsParam(params[1], pwd);
        if ("/".equals(path)) {
            pwd = path;
            return;
        }
        EnumNodeVersion.Version supportedVersion = bfsService.getCurrentVersion();
        if (supportedVersion.compareTo(EnumNodeVersion.BCOS_3_1_0.toVersionObj()) >= 0) {
            BFSInfo bfsInfo = bfsService.isExist(path);
            if (bfsInfo != null) {
                if (!bfsInfo.getFileType().equals(Common.BFS_TYPE_DIR)) {
                    throw new Exception("cd: not a directory: " + bfsInfo.getFileName());
                }
            } else {
                logger.error("cd: no such file or directory: '{}'", path);
                throw new Exception("cd: no such file or directory: " + params[1]);
            }
        } else {
            Tuple2<String, String> parentAndBase = ConsoleUtils.getParentPathAndBaseName(path);
            String parentDir = parentAndBase.getValue1();
            String baseName = parentAndBase.getValue2();
            List<BfsInfo> listResult = bfsService.list(parentDir);
            if (!listResult.isEmpty()) {
                boolean findFlag = false;
                for (BfsInfo bfsInfo : listResult) {
                    if (bfsInfo.getFileName().equals(baseName)) {
                        findFlag = true;
                        if (!bfsInfo.getFileType().equals(Common.BFS_TYPE_DIR)) {
                            throw new Exception("cd: not a directory: " + bfsInfo.getFileName());
                        }
                    }
                }
                if (!findFlag) {
                    logger.error("cd: no such file or directory: '{}'", path);
                    throw new Exception("cd: no such file or directory: " + baseName);
                }
            } else {
                throw new Exception("cd: no such file or directory: " + params[1]);
            }
        }
        pwd = path;
    }

    @Override
    public void makeDir(String[] params) throws Exception {
        String[] fixedBfsParams = ConsoleUtils.fixedBfsParams(params, pwd);
        String path = fixedBfsParams[1];
        RetCode mkdir = bfsService.mkdir(path);
        logger.info("mkdir: make new dir {}, retCode {}", path, mkdir);
        if (mkdir.getCode() == PrecompiledRetCode.CODE_FILE_INVALID_PATH.getCode()) {
            if (!path.startsWith("/apps/") && !path.startsWith("/tables/")) {
                System.out.println("Only permitted to mkdir in '/apps/' and '/tables/'");
                return;
            }
        }
        System.out.println(mkdir.getMessage());
    }

    @Override
    public void listDir(String[] params) throws Exception {
        String[] fixedBfsParams = ConsoleUtils.fixedBfsParams(params, pwd);

        String listPath = fixedBfsParams.length == 1 ? pwd : fixedBfsParams[1];
        BigInteger fileLeft = BigInteger.ZERO;
        BigInteger offset = BigInteger.ZERO;
        do {
            Tuple2<BigInteger, List<BfsInfo>> fileInfoList;
            EnumNodeVersion.Version supportedVersion = bfsService.getCurrentVersion();
            if (supportedVersion.compareTo(EnumNodeVersion.BCOS_3_1_0.toVersionObj()) >= 0) {
                fileInfoList = bfsService.list(listPath, offset, Common.LS_DEFAULT_COUNT);
            } else {
                fileInfoList = new Tuple2<>(BigInteger.ZERO, bfsService.list(listPath));
            }
            fileLeft = fileInfoList.getValue1();
            String baseName = FilenameUtils.getBaseName(listPath);
            int newLineCount = 0;
            for (BfsInfo fileInfo : fileInfoList.getValue2()) {
                newLineCount++;
                switch (fileInfo.getFileType()) {
                    case Common.BFS_TYPE_CON:
                        System.out.print("\033[31m" + fileInfo.getFileName() + "\033[m" + '\t');
                        break;
                    case Common.BFS_TYPE_DIR:
                        System.out.print("\033[36m" + fileInfo.getFileName() + "\033[m" + '\t');
                        break;
                    case Common.BFS_TYPE_LNK:
                        System.out.print("\033[35m" + fileInfo.getFileName() + "\033[m");
                        if (fileInfoList.getValue2().size() == 1
                                && fileInfo.getFileName().equals(baseName)) {
                            System.out.print(" -> " + fileInfo.getExt().get(0));
                            System.out.println();
                            if (listPath.startsWith(ContractCompiler.BFS_SYS_PREFIX)) {
                                // /sys/ bfsInfo
                                System.out.println(
                                        listPath
                                                + ": built-in contract, you can use it's address in contract to call interfaces.");
                            }
                        }
                        System.out.print('\t');
                        break;
                    default:
                        System.out.println();
                        break;
                }
                if (newLineCount % 6 == 0) {
                    System.out.println();
                }
            }
            if (fileLeft.compareTo(BigInteger.ZERO) > 0) {
                offset = offset.add(Common.LS_DEFAULT_COUNT);
                System.out.println();
                ConsoleUtils.singleLine();
                System.out.print(
                        "----------------------- "
                                + fileLeft
                                + " File(s) left, continue to scan? (Y/N)-----------------------");
                Scanner sc = new Scanner(System.in);
                String nextString = sc.nextLine().toLowerCase().replace("\n", "");
                if (!"y".equals(nextString)) {
                    break;
                }
            }
        } while (fileLeft.compareTo(BigInteger.ZERO) > 0);

        if (fileLeft.compareTo(BigInteger.ZERO) < 0) {
            RetCode precompiledResponse =
                    PrecompiledRetCode.getPrecompiledResponse(fileLeft.intValue(), "");
            throw new ContractException(
                    precompiledResponse.getMessage(), precompiledResponse.getCode());
        }
    }

    @Override
    public void tree(String[] params) throws Exception {
        String absolutePath = ConsoleUtils.fixedBfsParam(params[1], pwd);
        int limit = 3;
        try {
            if (params.length > 2) {
                limit = Integer.parseInt(params[2]);
                if (limit <= 0 || limit > 5) {
                    System.out.println("Limit should be in range (0,5]");
                    return;
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("NumberFormatException: Please check the number you input.");
            return;
        }
        System.out.println(absolutePath);
        Tuple2<Integer, Integer> treeCount = travelBfs(absolutePath, "", 0, limit);
        System.out.println(
                "\n"
                        + treeCount.getValue1()
                        + " directory, "
                        + treeCount.getValue2()
                        + " contracts.");
    }

    @Override
    public void link(String[] params) throws Exception {
        String linkPath = ConsoleUtils.fixedBfsParam(params[1], pwd);
        String contractAddress =
                client.isWASM()
                        ? ConsoleUtils.fixedBfsParam(params[2], pwd)
                                .substring(ContractCompiler.BFS_APPS_PREFIX.length())
                        : params[2];
        if (!client.isWASM() && !AddressUtils.isValidAddress(contractAddress)) {
            System.out.println("Contract address is invalid, address: " + contractAddress);
        }
        if (!linkPath.startsWith(ContractCompiler.BFS_APPS_PREFIX)) {
            System.out.println("Link must locate in /apps.");
            System.out.println("Example: ln /apps/Name 0x1234567890abcd");
            return;
        }
        String abi = "";
        String contractName = FilenameUtils.getBaseName(linkPath);
        // get ABI
        try {
            String wasmAbiAddress = "";
            if (client.isWASM()) {
                wasmAbiAddress =
                        Base64.getUrlEncoder()
                                .withoutPadding()
                                .encodeToString((contractAddress).getBytes(StandardCharsets.UTF_8));
            }
            AbiAndBin abiAndBin =
                    ContractCompiler.loadAbi(
                            client.getGroup(),
                            contractName,
                            client.isWASM()
                                    ? wasmAbiAddress
                                    : Numeric.prependHexPrefix(contractAddress));
            abi = abiAndBin.getAbi();
        } catch (IOException e) {
            if (logger.isErrorEnabled()) {
                logger.error(
                        "load abi for contract failed, contract name: {}, address: {}, e: ",
                        contractAddress,
                        contractAddress,
                        e);
            }
        }
        if (abi.isEmpty()) {
            // abi still empty, get abi on chain
            Abi remoteAbi = client.getABI(contractAddress);
            abi = remoteAbi.getABI();
            if (abi.isEmpty()) {
                System.out.println(
                        "Warn: \nPlease make sure the existence of the contract, abi is empty. contractName: "
                                + contractName
                                + ", contractAddress: "
                                + contractAddress);
            }
        }

        RetCode retCode;
        EnumNodeVersion.Version supportedVersion = bfsService.getCurrentVersion();
        if (supportedVersion.compareTo(EnumNodeVersion.BCOS_3_1_0.toVersionObj()) >= 0) {
            retCode =
                    bfsService.link(
                            linkPath.substring(ContractCompiler.BFS_APPS_PREFIX.length()),
                            contractAddress,
                            abi);
        } else {
            List<String> levels = ConsoleUtils.path2Level(linkPath);
            if (levels.size() != 3) {
                retCode = PrecompiledRetCode.CODE_FILE_INVALID_PATH;
            } else {
                String name = levels.get(1);
                String version = levels.get(2);
                retCode = bfsService.link(name, version, contractAddress, abi);
            }
        }
        ConsoleUtils.printJson(retCode.toString());
        System.out.println();
    }

    @Override
    public void getContractShard(String[] params) throws Exception {
        String shard = this.shardingService.getContractShard(params[1]);
        if (shard.isEmpty()) {
            shard = "default";
        } else {
            shard = "/shards/" + shard;
        }

        System.out.println(shard);
    }

    @Override
    public void makeShard(String[] params) throws Exception {
        String shardName = params[1];
        RetCode retCode = this.shardingService.makeShard(shardName);

        logger.info("makeShard: {}, retCode {}", shardName, retCode);
        // parse the result
        if (retCode.getCode() == PrecompiledRetCode.CODE_SUCCESS.getCode()) {
            System.out.println("make shard " + shardName + " Ok. You can use 'ls' to check");
        } else {
            System.out.println("make shard " + shardName + " failed ");
            ConsoleUtils.printJson(retCode.toString());
        }
    }

    @Override
    public void linkShard(String[] params) throws Exception {
        String address = params[1];
        String shardName = params[2];
        RetCode retCode = this.shardingService.linkShard(shardName, address);

        logger.info("linkShard: add {} to {}, retCode {}", address, shardName, retCode);
        // parse the result
        if (retCode.getCode() == PrecompiledRetCode.CODE_SUCCESS.getCode()) {
            System.out.println(
                    "Add " + address + " to " + shardName + " Ok. You can use 'ls' to check");
        } else {
            System.out.println("Add " + address + " to " + shardName + " failed ");
            ConsoleUtils.printJson(retCode.toString());
        }
    }

    @Override
    public void fixBFS(String[] params) throws Exception {
        ConsoleUtils.printJson(bfsService.fixBfs().toString());
    }

    @Override
    public void getBalance(String[] params) throws Exception {
        String address = params[1];
        if (!AddressUtils.isValidAddress(address)) {
            System.out.println("Invalid address: " + address);
            return;
        }
        BigInteger result = this.balanceService.getBalance(address);
        System.out.println("balance: " + result + " wei");
    }

    @Override
    public void addBalance(String[] params) throws Exception {
        String address = params[1];
        String amount = params[2];
        Convert.Unit unit = Convert.Unit.WEI;
        if (params.length > 3) {
            unit = Convert.Unit.fromString(params[3]);
        }
        if (!AddressUtils.isValidAddress(address)) {
            System.out.println("Invalid address: " + address);
            return;
        }
        if (!ConsoleUtils.isValidNumber(amount)) {
            System.out.println("Invalid amount: " + amount);
            return;
        }
        BigDecimal value = new BigDecimal(amount);
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            System.out.println("Invalid amount: " + amount);
            return;
        }
        if (value.compareTo(BigDecimal.ZERO) == 0) {
            System.out.println("Amount is zero, no need to addBalance.");
            return;
        }
        RetCode retCode = this.balanceService.addBalance(address, amount, unit);

        logger.info("addBalance: {}, retCode {}", address, retCode);
        // parse the result
        if (retCode == PrecompiledRetCode.CODE_SUCCESS) {
            System.out.println(
                    "transaction hash:" + retCode.getTransactionReceipt().getTransactionHash());
            System.out.println(
                    "add balance " + address + " success. You can use 'getBalance' to check");
        } else {
            System.out.println("add balance " + address + " failed.");
            ConsoleUtils.printJson(retCode.toString());
        }
    }

    @Override
    public void subBalance(String[] params) throws Exception {
        String address = params[1];
        String amount = params[2];
        Convert.Unit unit = Convert.Unit.WEI;
        if (params.length > 3) {
            unit = Convert.Unit.fromString(params[3]);
        }
        if (!AddressUtils.isValidAddress(address)) {
            System.out.println("Invalid address: " + address);
            return;
        }
        if (!ConsoleUtils.isValidNumber(amount)) {
            System.out.println("Invalid amount: " + amount);
            return;
        }
        BigDecimal value = new BigDecimal(amount);
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            System.out.println("Invalid amount: " + amount);
            return;
        }
        if (value.compareTo(BigDecimal.ZERO) == 0) {
            System.out.println("Amount is zero, no need to subBalance.");
            return;
        }
        RetCode retCode = this.balanceService.subBalance(address, amount, unit);

        logger.info("subBalance: {}, retCode {}", address, retCode);
        // parse the result
        if (retCode == PrecompiledRetCode.CODE_SUCCESS) {
            System.out.println(
                    "transaction hash:" + retCode.getTransactionReceipt().getTransactionHash());
            System.out.println(
                    "sub balance " + address + " success. You can use 'getBalance' to check");
        } else {
            System.out.println("sub balance " + address + " failed. receipt.");
            ConsoleUtils.printJson(retCode.toString());
        }
    }

    public void transferBalance(String[] params) throws Exception {
        String from = params[1];
        String to = params[2];
        String amount = params[3];
        Convert.Unit unit = Convert.Unit.WEI;
        if (params.length > 4) {
            unit = Convert.Unit.fromString(params[4]);
        }
        if (!AddressUtils.isValidAddress(from)) {
            System.out.println("Invalid from address: " + from);
            return;
        }
        if (!AddressUtils.isValidAddress(to)) {
            System.out.println("Invalid to address: " + to);
            return;
        }
        if (!ConsoleUtils.isValidNumber(amount)) {
            System.out.println("Invalid amount: " + amount);
            return;
        }
        BigDecimal value = new BigDecimal(amount);
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            System.out.println("Invalid amount: " + amount);
            return;
        }
        if (value.compareTo(BigDecimal.ZERO) == 0) {
            System.out.println("Amount is zero, no need to transferBalance.");
            return;
        }
        RetCode retCode = this.balanceService.transfer(from, to, amount, unit);

        logger.info("transferBalance: {}, retCode {}", from, retCode);
        // parse the result
        if (retCode == PrecompiledRetCode.CODE_SUCCESS) {
            System.out.println(
                    "transaction hash:" + retCode.getTransactionReceipt().getTransactionHash());
            System.out.println(
                    "transfer "
                            + amount
                            + unit.toString()
                            + " from "
                            + from
                            + " to "
                            + to
                            + " success. You can use 'getBalance' to check");
        } else {
            System.out.println("transfer " + amount + " from " + from + " to " + to + " failed.");
            ConsoleUtils.printJson(retCode.toString());
        }
    }

    @Override
    public void registerBalanceGovernor(String[] params) throws Exception {
        String address = params[1];
        if (!AddressUtils.isValidAddress(address)) {
            System.out.println("Invalid address: " + address);
            return;
        }
        RetCode retCode = this.balanceService.registerCaller(address);

        logger.info("registerBalanceGovernor: {}, retCode {}", address, retCode);
        // parse the result
        if (retCode == PrecompiledRetCode.CODE_SUCCESS) {
            System.out.println(
                    "transaction hash:" + retCode.getTransactionReceipt().getTransactionHash());
            System.out.println("register balanceGovernor " + address + " success.");
        } else {
            System.out.println("register balanceGovernor " + address + " failed. ");
        }
    }

    @Override
    public void unregisterBalanceGovernor(String[] params) throws Exception {
        String address = params[1];
        if (!AddressUtils.isValidAddress(address)) {
            System.out.println("Invalid address: " + address);
            return;
        }
        RetCode retCode = this.balanceService.unregisterCaller(address);

        logger.info("unregisterBalanceGovernor: {}, retCode {}", address, retCode);
        // parse the result
        if (retCode == PrecompiledRetCode.CODE_SUCCESS) {
            System.out.println(
                    "transaction hash:" + retCode.getTransactionReceipt().getTransactionHash());
            System.out.println("unregister balanceGovernor " + address + " success.");
        } else {
            System.out.println("unregister balanceGovernor " + address + " failed.");
        }
    }

    @Override
    public void listBalanceGovernor() throws Exception {
        List<String> result = this.balanceService.listCaller();
        System.out.println("listBalanceGovernor: " + result.toString());
    }

    @Override
    public String getPwd() {
        return pwd;
    }

    private Tuple2<Integer, Integer> travelBfs(
            String absolutePath, String prefix, int deep, int limit) throws ContractException {
        if (deep >= limit) return new Tuple2<>(0, 0);
        int dirCount = 0;
        int contractCount = 0;
        BigInteger offset = BigInteger.ZERO;
        EnumNodeVersion.Version supportedVersion = bfsService.getCurrentVersion();
        Tuple2<BigInteger, List<BfsInfo>> fileInfoList;
        if (supportedVersion.compareTo(EnumNodeVersion.BCOS_3_1_0.toVersionObj()) >= 0) {
            fileInfoList = bfsService.list(absolutePath, offset, Common.LS_DEFAULT_COUNT);
        } else {
            fileInfoList = new Tuple2<>(BigInteger.ZERO, bfsService.list(absolutePath));
        }
        BigInteger fileLeft = fileInfoList.getValue1();
        List<BfsInfo> children = fileInfoList.getValue2();
        for (int i = 0; i < children.size(); i++) {
            String thisPrefix = "";
            String nextPrefix = "";
            if (deep >= 0) {
                if ((i + 1) < children.size()) {
                    nextPrefix = prefix + "│ ";
                    thisPrefix = prefix + "├─";
                } else {
                    nextPrefix = prefix + "  ";
                    if (fileLeft.compareTo(BigInteger.ZERO) > 0) {
                        thisPrefix = prefix + "├─";
                    } else {
                        thisPrefix = prefix + "└─";
                    }
                }
                System.out.println(thisPrefix + children.get(i).getFileName());
                if (children.get(i).getFileType().equals(Common.BFS_TYPE_DIR)) {
                    dirCount++;
                    Tuple2<Integer, Integer> childCount =
                            travelBfs(
                                    absolutePath
                                            + (absolutePath.equals("/") ? "" : "/")
                                            + children.get(i).getFileName(),
                                    nextPrefix,
                                    deep + 1,
                                    limit);
                    dirCount += childCount.getValue1();
                    contractCount += childCount.getValue2();
                } else {
                    contractCount++;
                }
                if (fileLeft.compareTo(BigInteger.ZERO) > 0 && i == children.size() - 1) {
                    System.out.println(prefix + "└─" + "... " + fileLeft + " left files...");
                }
            }
        }
        return new Tuple2<>(dirCount, contractCount);
    }
}
