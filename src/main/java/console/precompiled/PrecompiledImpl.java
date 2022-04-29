package console.precompiled;

import console.common.Common;
import console.common.ConsoleUtils;
import console.contract.model.AbiAndBin;
import console.contract.utils.ContractCompiler;
import console.exception.ConsoleMessageException;
import console.precompiled.model.CRUDParseUtils;
import console.precompiled.model.Table;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sf.jsqlparser.JSQLParserException;
import org.apache.commons.io.FilenameUtils;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.client.exceptions.ClientException;
import org.fisco.bcos.sdk.v3.client.protocol.response.Abi;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.v3.codegen.exceptions.CodeGenException;
import org.fisco.bcos.sdk.v3.contract.precompiled.bfs.BFSPrecompiled.BfsInfo;
import org.fisco.bcos.sdk.v3.contract.precompiled.bfs.BFSService;
import org.fisco.bcos.sdk.v3.contract.precompiled.consensus.ConsensusService;
import org.fisco.bcos.sdk.v3.contract.precompiled.crud.KVTableService;
import org.fisco.bcos.sdk.v3.contract.precompiled.crud.TableCRUDService;
import org.fisco.bcos.sdk.v3.contract.precompiled.crud.common.Condition;
import org.fisco.bcos.sdk.v3.contract.precompiled.crud.common.Entry;
import org.fisco.bcos.sdk.v3.contract.precompiled.crud.common.UpdateFields;
import org.fisco.bcos.sdk.v3.contract.precompiled.sysconfig.SystemConfigService;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.v3.model.PrecompiledConstant;
import org.fisco.bcos.sdk.v3.model.PrecompiledRetCode;
import org.fisco.bcos.sdk.v3.model.RetCode;
import org.fisco.bcos.sdk.v3.model.TransactionReceiptStatus;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;
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
    private KVTableService kvTableService;
    private BFSService bfsService;
    private String pwd = "/apps";

    public PrecompiledImpl(Client client) {
        this.client = client;
        CryptoKeyPair cryptoKeyPair = client.getCryptoSuite().getCryptoKeyPair();
        this.consensusService = new ConsensusService(client, cryptoKeyPair);
        this.systemConfigService = new SystemConfigService(client, cryptoKeyPair);
        this.tableCRUDService = new TableCRUDService(client, cryptoKeyPair);
        this.kvTableService = new KVTableService(client, cryptoKeyPair);
        this.bfsService = new BFSService(client, cryptoKeyPair);
    }

    @Override
    public void addSealer(String[] params) throws Exception {
        String nodeId = params[1];
        int weight = ConsoleUtils.processNonNegativeNumber("consensusWeight", params[2]);
        if (nodeId.length() != 128) {
            ConsoleUtils.printJson(PrecompiledRetCode.CODE_INVALID_NODEID.toString());
        } else {
            ConsoleUtils.printJson(
                    this.consensusService.addSealer(nodeId, BigInteger.valueOf(weight)).toString());
        }
    }

    @Override
    public void addObserver(String[] params) throws Exception {
        String nodeId = params[1];
        if (nodeId.length() != 128) {
            ConsoleUtils.printJson(PrecompiledRetCode.CODE_INVALID_NODEID.toString());
        } else {
            ConsoleUtils.printJson(this.consensusService.addObserver(nodeId).toString());
        }
    }

    @Override
    public void removeNode(String[] params) throws Exception {
        String nodeId = params[1];
        if (nodeId.length() != 128) {
            ConsoleUtils.printJson(PrecompiledRetCode.CODE_INVALID_NODEID.toString());
        } else {
            ConsoleUtils.printJson(this.consensusService.removeNode(nodeId).toString());
        }
    }

    @Override
    public void setConsensusNodeWeight(String[] params) throws Exception {
        String nodeId = params[1];
        int weight = ConsoleUtils.processNonNegativeNumber("consensusWeight", params[2]);
        if (nodeId.length() != 128) {
            ConsoleUtils.printJson(PrecompiledRetCode.CODE_INVALID_NODEID.toString());
        } else {
            ConsoleUtils.printJson(
                    this.consensusService.setWeight(nodeId, BigInteger.valueOf(weight)).toString());
        }
    }

    @Override
    public void setSystemConfigByKey(String[] params) throws Exception {
        String key = params[1];
        String value = params[2];
        ConsoleUtils.printJson(this.systemConfigService.setValueByKey(key, value).toString());
    }

    @Override
    public void desc(String[] params) throws Exception {
        String tableName = params[1];
        CRUDParseUtils.invalidSymbol(tableName);
        if (tableName.endsWith(";")) {
            tableName = tableName.substring(0, tableName.length() - 1);
        }
        Map<String, String> tableDesc = kvTableService.desc(tableName);
        if (tableDesc.get(PrecompiledConstant.KEY_FIELD_NAME).equals("")) {
            System.out.println("The table \"" + tableName + "\" doesn't exist!");
            return;
        }
        ConsoleUtils.printJson(ObjectMapperFactory.getObjectMapper().writeValueAsString(tableDesc));
    }

    @Override
    public void createTable(String sql, boolean isWasm) throws Exception {
        Table table = new Table();
        try {
            CRUDParseUtils.parseCreateTable(sql, table);
        } catch (ConsoleMessageException e) {
            System.out.println(e.getMessage());
            logger.error(" message: {}, e:", e.getMessage(), e);
            return;
        } catch (JSQLParserException | NullPointerException e) {
            System.out.println("Could not parse SQL statement.");
            CRUDParseUtils.invalidSymbol(sql);
            return;
        }
        try {
            RetCode result =
                    tableCRUDService.createTable(
                            table.getTableName(), table.getKeyFieldName(), table.getValueFields());

            // parse the result
            if (result.getCode() == PrecompiledRetCode.CODE_SUCCESS.getCode()) {
                System.out.println("Create '" + table.getTableName() + "' Ok.");
            } else {
                System.out.println("Create '" + table.getTableName() + "' failed ");
                ConsoleUtils.printJson(result.toString());
            }
        } catch (ContractException e) {
            outputErrorMessageForTableCRUD(
                    table,
                    table.getKeyFieldName(),
                    null,
                    sql,
                    e.getErrorCode(),
                    e.getMessage(),
                    null);
        } catch (ClientException e) {
            outputErrorMessageForTableCRUD(
                    table,
                    table.getKeyFieldName(),
                    null,
                    sql,
                    e.getErrorCode(),
                    e.getMessage(),
                    null);
        }
    }

    private boolean checkTableField(Map<String, List<String>> descTable, Entry entry) {
        if (descTable == null || descTable.size() == 0) {
            return true;
        }
        // check field
        if (entry != null) {
            Set<String> fieldSet = entry.getFieldNameToValue().keySet();
            for (String field : fieldSet) {
                if (!descTable.get(PrecompiledConstant.VALUE_FIELD_NAME).contains(field)) {
                    System.out.println(
                            "Unknown field \""
                                    + field
                                    + "\", current supported fields are "
                                    + descTable
                                            .get(PrecompiledConstant.VALUE_FIELD_NAME)
                                            .toString());
                    return false;
                }
            }
        }
        return true;
    }

    private void outputErrorMessageForTableCRUD(
            Table table,
            String keyValue,
            Entry entry,
            String command,
            int code,
            String message,
            Map<String, List<String>> descTable) {
        System.out.println("call " + command + " failed!");
        System.out.println("* code: " + code);
        System.out.println("* message: " + message);

        if (code != TransactionReceiptStatus.PrecompiledError.getCode()) {
            return;
        }
        if (!checkTableField(descTable, entry)) {
            return;
        }
        if (table == null) {
            return;
        }
        String regexTableName = "[\\da-zA-z,$,_,@]+";
        if (!table.getTableName().matches(regexTableName)) {
            System.out.println("Invalid table name " + table.getTableName());
            System.out.println(
                    "* The table name must contain only numbers, letters or [$','_','@']");
        }
        if (table.getTableName().length() > PrecompiledConstant.USER_TABLE_NAME_MAX_LENGTH) {
            System.out.println("Invalid table name " + table.getTableName());
            System.out.println(
                    "* The length of the table name must be no greater than "
                            + PrecompiledConstant.USER_TABLE_NAME_MAX_LENGTH
                            + ", current length of the table is "
                            + table.getTableName().length());
        }
        if (table.getKeyFieldName() != null
                && table.getKeyFieldName().length()
                        > PrecompiledConstant.TABLE_FIELD_NAME_MAX_LENGTH) {
            System.out.println("Invalid key \"" + table.getKeyFieldName() + "\"");
            System.out.println(
                    "* The length of the key must be no greater than "
                            + PrecompiledConstant.TABLE_FIELD_NAME_MAX_LENGTH
                            + " , current length of the table is "
                            + table.getKeyFieldName().length());
        }
        if (keyValue.length() > PrecompiledConstant.TABLE_KEY_VALUE_MAX_LENGTH) {
            System.out.println("Invalid key value " + keyValue);
            System.out.println(
                    "* The value of the key must be no greater than "
                            + PrecompiledConstant.TABLE_KEY_VALUE_MAX_LENGTH
                            + " , current length of the table is "
                            + keyValue.length());
        }
        if (table.getValueFields() != null) {
            for (String field : table.getValueFields()) {
                if (field.length() > PrecompiledConstant.TABLE_FIELD_NAME_MAX_LENGTH) {
                    System.out.println("Invalid field: " + field);

                    System.out.println(
                            "* Field length must be no greater than "
                                    + PrecompiledConstant.TABLE_FIELD_NAME_MAX_LENGTH
                                    + ", current length is: "
                                    + field.length());
                }
            }
        }
        if (entry == null) {
            return;
        }
        Map<String, String> fieldNameToValue = entry.getFieldNameToValue();
        for (Map.Entry<String, String> kvEntry : fieldNameToValue.entrySet()) {
            if (kvEntry.getKey().length() > PrecompiledConstant.TABLE_FIELD_NAME_MAX_LENGTH) {
                System.out.println("Invalid field name " + kvEntry.getKey());
                System.out.println(
                        "* Field length must be no greater than "
                                + PrecompiledConstant.TABLE_FIELD_NAME_MAX_LENGTH
                                + ", current length:"
                                + kvEntry.getKey().length());
            }
            if (kvEntry.getValue().length()
                    > PrecompiledConstant.USER_TABLE_FIELD_VALUE_MAX_LENGTH) {
                System.out.println("Invalid field value for " + kvEntry.getKey());
                System.out.println(
                        "* Value of Field must be no greater than: "
                                + PrecompiledConstant.USER_TABLE_FIELD_VALUE_MAX_LENGTH
                                + ", current length is "
                                + kvEntry.getValue().length());
            }
        }
    }

    @Override
    public void insert(String sql) throws Exception {
        Table table = new Table();
        Entry entry = null;
        Map<String, List<String>> descTable = null;
        String keyValue = "";
        try {
            String tableName = CRUDParseUtils.parseInsertedTableName(sql);
            descTable = tableCRUDService.desc(tableName);
            if (!checkTableExistence(tableName, descTable)) {
                return;
            }
            logger.debug("insert, tableName: {}, descTable: {}", tableName, descTable);
            entry = CRUDParseUtils.parseInsert(sql, table, descTable);
            String keyName = descTable.get(PrecompiledConstant.KEY_FIELD_NAME).get(0);
            keyValue = entry.getKey();
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
                System.out.println(insertResult.getCode() + " row affected.");
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
        } catch (ContractException e) {
            outputErrorMessageForTableCRUD(
                    table, keyValue, entry, sql, e.getErrorCode(), e.getMessage(), descTable);
        } catch (ClientException e) {
            outputErrorMessageForTableCRUD(
                    table, keyValue, entry, sql, e.getErrorCode(), e.getMessage(), descTable);
        }
    }

    @Override
    public void update(String sql) throws Exception {
        Table table = new Table();
        UpdateFields updateFields = new UpdateFields();
        Condition condition = new Condition();
        String keyValue = "";
        Map<String, List<String>> descTable = null;
        try {
            condition = CRUDParseUtils.parseUpdate(sql, table, updateFields);
        } catch (ConsoleMessageException e) {
            System.out.println(e.getMessage());
            logger.error(" message: {}, e: ", e.getMessage(), e);
            return;
        } catch (JSQLParserException | NullPointerException e) {
            System.out.println("Could not parse SQL statement.");
            CRUDParseUtils.invalidSymbol(sql);
            return;
        }
        try {
            String tableName = table.getTableName();
            descTable = tableCRUDService.desc(tableName);
            if (!checkTableExistence(table.getTableName(), descTable)) {
                return;
            }
            String keyName = descTable.get(PrecompiledConstant.KEY_FIELD_NAME).get(0);
            if (updateFields.getFieldNameToValue().containsKey(keyName)) {
                System.out.println("Please don't set the key field \"" + keyName + "\".");
                return;
            }
            table.setKeyFieldName(keyName);
            keyValue = condition.getEqValue();
            RetCode updateResult =
                    keyValue.isEmpty()
                            ? tableCRUDService.update(tableName, condition, updateFields)
                            : tableCRUDService.update(tableName, keyValue, updateFields);
            if (updateResult.getCode() >= 0) {
                System.out.println(updateResult.getCode() + " row affected.");
            } else {
                System.out.println("Result of update " + tableName + " :");
                ConsoleUtils.printJson(updateResult.toString());
            }
        } catch (ContractException e) {
            outputErrorMessageForTableCRUD(
                    table, keyValue, null, sql, e.getErrorCode(), e.getMessage(), descTable);
        } catch (ClientException e) {
            outputErrorMessageForTableCRUD(
                    table, keyValue, null, sql, e.getErrorCode(), e.getMessage(), descTable);
        }
    }

    @Override
    public void remove(String sql) throws Exception {
        Table table = new Table();
        Condition condition = new Condition();
        Map<String, List<String>> descTable = null;
        String keyValue = "";
        try {
            condition = CRUDParseUtils.parseRemove(sql, table);
        } catch (ConsoleMessageException e) {
            System.out.println(e.getMessage());
            logger.error(" message: {}, e: {}", e.getMessage(), e);
            return;
        } catch (JSQLParserException | NullPointerException e) {
            System.out.println("Could not parse SQL statement.");
            CRUDParseUtils.invalidSymbol(sql);
            return;
        }
        try {
            descTable = tableCRUDService.desc(table.getTableName());
            if (!checkTableExistence(table.getTableName(), descTable)) {
                return;
            }
            table.setKeyFieldName(descTable.get(PrecompiledConstant.KEY_FIELD_NAME).get(0));
            keyValue = condition.getEqValue();
            RetCode removeResult =
                    keyValue.isEmpty()
                            ? tableCRUDService.remove(table.getTableName(), condition)
                            : tableCRUDService.remove(table.getTableName(), keyValue);

            if (removeResult.getCode() >= 0) {
                System.out.println("Remove OK, " + removeResult.getCode() + " row affected.");
            } else {
                System.out.println("Result of Remove " + table.getTableName() + " :");
                ConsoleUtils.printJson(removeResult.toString());
            }
        } catch (ContractException e) {
            outputErrorMessageForTableCRUD(
                    table, keyValue, null, sql, e.getErrorCode(), e.getMessage(), descTable);
        } catch (ClientException e) {
            outputErrorMessageForTableCRUD(
                    table, keyValue, null, sql, e.getErrorCode(), e.getMessage(), descTable);
        }
    }

    private boolean checkTableExistence(String tableName, Map<String, List<String>> descTable) {
        if (descTable.size() == 0
                || descTable.get(PrecompiledConstant.KEY_FIELD_NAME).get(0).equals("")) {
            System.out.println("The table \"" + tableName + "\" doesn't exist!");
            return false;
        }
        return true;
    }

    @Override
    public void select(String sql) throws ConsoleMessageException {
        Table table = new Table();
        Condition condition = new Condition();
        List<String> selectColumns = new ArrayList<>();
        Map<String, List<String>> descTable = null;
        String keyValue = "";
        try {
            descTable = tableCRUDService.desc(table.getTableName());
            if (!checkTableExistence(table.getTableName(), descTable)) {
                return;
            }
            String keyField = descTable.get(PrecompiledConstant.KEY_FIELD_NAME).get(0);
            table.setKeyFieldName(keyField);
            condition = CRUDParseUtils.parseSelect(sql, table, selectColumns);
            keyValue = condition.getEqValue();
            List<Map<String, String>> result =
                    keyValue.isEmpty()
                            ? tableCRUDService.select(table.getTableName(), condition)
                            : Collections.singletonList(
                                    tableCRUDService.select(table.getTableName(), keyValue));
            int rows = 0;
            if (result.isEmpty()) {
                System.out.println("Empty set.");
                return;
            }
            result = filterSystemColumn(result);
            if ("*".equals(selectColumns.get(0))) {
                selectColumns.clear();
                selectColumns.add(keyField);
                selectColumns.addAll(descTable.get(PrecompiledConstant.VALUE_FIELD_NAME));
                result.get(0).put(keyField, keyValue);
                result = getSelectedColumn(selectColumns, result);
                rows = result.size();
            } else {
                if (selectColumns.contains(keyField)) {
                    result.get(0).put(keyField, keyValue);
                }
                List<Map<String, String>> selectedResult = getSelectedColumn(selectColumns, result);
                rows = selectedResult.size();
            }
            if (rows == 1) {
                System.out.println(rows + " row in set.");
            } else {
                System.out.println(rows + " rows in set.");
            }
        } catch (ConsoleMessageException e) {
            System.out.println(e.getMessage());
            logger.error(" message: {}, e:", e.getMessage(), e);
        } catch (JSQLParserException | NullPointerException e) {
            System.out.println("Could not parse SQL statement.");
            CRUDParseUtils.invalidSymbol(sql);
        } catch (ContractException e) {
            outputErrorMessageForTableCRUD(
                    table, keyValue, null, sql, e.getErrorCode(), e.getMessage(), descTable);
        } catch (ClientException e) {
            outputErrorMessageForTableCRUD(
                    table, keyValue, null, sql, e.getErrorCode(), e.getMessage(), descTable);
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

    private List<Map<String, String>> filterSystemColumn(List<Map<String, String>> result) {

        List<String> filteredColumns = Arrays.asList("_id_", "_hash_", "_status_", "_num_");
        List<Map<String, String>> filteredResult = new ArrayList<>(result.size());
        Map<String, String> filteredRecords;
        for (Map<String, String> records : result) {
            filteredRecords = new LinkedHashMap<>();
            Set<String> recordKeys = records.keySet();
            for (String recordKey : recordKeys) {
                if (!filteredColumns.contains(recordKey)) {
                    filteredRecords.put(recordKey, records.get(recordKey));
                }
            }
            filteredResult.add(filteredRecords);
        }
        return filteredResult;
    }

    @Override
    public void changeDir(String[] params) throws Exception {
        if (params.length == 1) {
            System.out.println("cd: change dir to /apps");
            pwd = "/apps";
            return;
        }
        String path = ConsoleUtils.fixedBfsParam(params[1], pwd);
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
                throw new Exception("cd: no such file or directory: " + baseName);
            }
        } else {
            throw new Exception("cd: no such file or directory: " + params[1]);
        }
        pwd = path;
    }

    @Override
    public void makeDir(String[] params) throws Exception {
        String[] fixedBfsParams = ConsoleUtils.fixedBfsParams(params, pwd);
        String path = fixedBfsParams[1];
        RetCode mkdir = bfsService.mkdir(path);
        logger.info("mkdir: make new dir {}", path);
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
        List<BfsInfo> fileInfoList = bfsService.list(listPath);
        String baseName = FilenameUtils.getBaseName(listPath);
        int newLineCount = 0;
        for (BfsInfo fileInfo : fileInfoList) {
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
                    if (fileInfoList.size() == 1 && fileInfo.getFileName().equals(baseName)) {
                        System.out.print(" -> " + fileInfo.getExt().get(0));
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
        List<String> path2Level = ConsoleUtils.path2Level(linkPath);
        if (path2Level.size() != 3 || !path2Level.get(0).equals("apps")) {
            System.out.println("Link must in /apps, and not support multi-level directory.");
            System.out.println("Example: ln /apps/Name/Version 0x1234567890");
            return;
        }
        String contractName = path2Level.get(1);
        String contractVersion = path2Level.get(2);
        if (!client.isWASM() && !AddressUtils.isValidAddress(contractAddress)) {
            System.out.println("Contract address is invalid, address: " + contractAddress);
        }
        String abi = "";
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
        } catch (IOException | CodeGenException e) {
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

        ConsoleUtils.printJson(
                bfsService.link(contractName, contractVersion, contractAddress, abi).toString());
        System.out.println();
    }

    private Tuple2<Integer, Integer> travelBfs(
            String absolutePath, String prefix, int deep, int limit) throws ContractException {
        if (deep >= limit) return new Tuple2<>(0, 0);
        Integer dirCount = 0;
        Integer contractCount = 0;
        List<BfsInfo> children = bfsService.list(absolutePath);
        for (int i = 0; i < children.size(); i++) {
            String thisPrefix = "";
            String nextPrefix = "";
            if (deep >= 0) {
                if ((i + 1) < children.size()) {
                    nextPrefix = prefix + "│ ";
                    thisPrefix = prefix + "├─";
                } else {
                    nextPrefix = prefix + "  ";
                    thisPrefix = prefix + "└─";
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
            }
        }
        return new Tuple2<>(dirCount, contractCount);
    }

    @Override
    public String getPwd() {
        return pwd;
    }
}
