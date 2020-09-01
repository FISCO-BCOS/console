package console.precompiled;

import console.common.CRUDParseUtils;
import console.common.Common;
import console.common.ConsoleUtils;
import console.exception.ConsoleMessageException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sf.jsqlparser.JSQLParserException;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.client.exceptions.ClientException;
import org.fisco.bcos.sdk.contract.exceptions.ContractException;
import org.fisco.bcos.sdk.contract.precompiled.cns.CnsService;
import org.fisco.bcos.sdk.contract.precompiled.consensus.ConsensusService;
import org.fisco.bcos.sdk.contract.precompiled.contractmgr.ContractLifeCycleService;
import org.fisco.bcos.sdk.contract.precompiled.crud.TableCRUDService;
import org.fisco.bcos.sdk.contract.precompiled.crud.common.Condition;
import org.fisco.bcos.sdk.contract.precompiled.crud.common.ConditionOperator;
import org.fisco.bcos.sdk.contract.precompiled.crud.common.Entry;
import org.fisco.bcos.sdk.contract.precompiled.model.PrecompiledConstant;
import org.fisco.bcos.sdk.contract.precompiled.model.PrecompiledRetCode;
import org.fisco.bcos.sdk.contract.precompiled.sysconfig.SystemConfigService;
import org.fisco.bcos.sdk.model.RetCode;
import org.fisco.bcos.sdk.model.TransactionReceiptStatus;
import org.fisco.bcos.sdk.utils.ObjectMapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrecompiledImpl implements PrecompiledFace {

    private static final Logger logger = LoggerFactory.getLogger(PrecompiledImpl.class);

    private Client client;
    private ConsensusService consensusService;
    private SystemConfigService systemConfigService;
    private TableCRUDService tableCRUDService;
    private ContractLifeCycleService contractLifeCycleService;
    private CnsService cnsService;

    public PrecompiledImpl(Client client) {
        this.client = client;
        this.consensusService = new ConsensusService(client, client.getCryptoInterface());
        this.systemConfigService = new SystemConfigService(client, client.getCryptoInterface());
        this.tableCRUDService = new TableCRUDService(client, client.getCryptoInterface());
        this.contractLifeCycleService =
                new ContractLifeCycleService(client, client.getCryptoInterface());
        this.cnsService = new CnsService(client, client.getCryptoInterface());
        client.getCryptoInterface().getCryptoKeyPair().storeKeyPairWithPemFormat();
    }

    @Override
    public void addSealer(String[] params) throws Exception {
        String nodeId = params[1];
        if (nodeId.length() != 128) {
            ConsoleUtils.printRetCode(PrecompiledRetCode.CODE_INVALID_NODEID);
        } else {
            ConsoleUtils.printRetCode(this.consensusService.addSealer(nodeId));
        }
        System.out.println();
    }

    @Override
    public void addObserver(String[] params) throws Exception {
        String nodeId = params[1];
        if (nodeId.length() != 128) {
            ConsoleUtils.printRetCode(PrecompiledRetCode.CODE_INVALID_NODEID);
        } else {
            ConsoleUtils.printRetCode(this.consensusService.addObserver(nodeId));
        }
        System.out.println();
    }

    @Override
    public void removeNode(String[] params) throws Exception {
        String nodeId = params[1];
        if (nodeId.length() != 128) {
            ConsoleUtils.printRetCode(PrecompiledRetCode.CODE_INVALID_NODEID);
        } else {
            ConsoleUtils.printRetCode(this.consensusService.removeNode(nodeId));
        }
        System.out.println();
    }

    @Override
    public void setSystemConfigByKey(String[] params) throws Exception {
        String key = params[1];
        if (Common.TxCountLimit.equals(key)
                || Common.TxGasLimit.equals(key)
                || Common.RPBFTEpochSealerNum.equals(key)
                || Common.RPBFTEpochBlockNum.equals(key)
                || Common.ConsensusTimeout.equals(key)) {
            String valueStr = params[2];
            int value = 1;
            try {
                value = Integer.parseInt(valueStr);
                if (Common.TxCountLimit.equals(key)
                        || Common.RPBFTEpochSealerNum.equals(key)
                        || Common.RPBFTEpochBlockNum.equals(key)) {
                    if (value <= 0) {
                        System.out.println(
                                "Please provide value by positive integer mode, "
                                        + Common.PositiveIntegerRange
                                        + ".");
                        System.out.println();
                        return;
                    }
                } else if (Common.TxGasLimit.equals(key) && value < Common.TxGasLimitMin) {
                    System.out.println(
                            "Please provide value by positive integer mode, "
                                    + Common.TxGasLimitRange
                                    + ".");
                    System.out.println();
                    return;
                } else if (Common.ConsensusTimeout.equals(key)
                        && (value < Common.ConsensusTimeoutMin
                                || value >= Common.ConsensusTimeoutMax)) {
                    System.out.println(
                            "Please provide value by positive integer mode, "
                                    + Common.ConsensusTimeoutRange
                                    + ".");
                    System.out.println();
                    return;
                }

                if (Common.RPBFTEpochSealerNum.equals(key)
                        || Common.RPBFTEpochBlockNum.equals(key)) {
                    System.out.println("Note: " + key + " only takes effect when rPBFT is used!");
                }
                ConsoleUtils.printRetCode(this.systemConfigService.setValueByKey(key, value + ""));
            } catch (NumberFormatException e) {
                if (Common.TxCountLimit.equals(key)
                        || Common.RPBFTEpochSealerNum.equals(key)
                        || Common.RPBFTEpochBlockNum.equals(key)) {
                    System.out.println(
                            "Please provide value by positive integer mode, "
                                    + Common.PositiveIntegerRange
                                    + ".");
                } else if (Common.TxGasLimit.equals(key)) {
                    System.out.println(
                            "Please provide value by positive integer mode, "
                                    + Common.TxGasLimitRange
                                    + ".");
                } else if (Common.ConsensusTimeout.equals(key)) {
                    System.out.println(
                            "Please provide a value no smaller than "
                                    + Common.ConsensusTimeoutRange
                                    + ".");
                }
                System.out.println();
                return;
            }
        } else {
            System.out.println(
                    "Please provide a valid key, for example: "
                            + Common.TxCountLimit
                            + " or "
                            + Common.TxGasLimit
                            + " or "
                            + Common.RPBFTEpochSealerNum
                            + " or "
                            + Common.RPBFTEpochBlockNum
                            + " or "
                            + Common.ConsensusTimeout
                            + " .");
        }
        System.out.println();
    }

    @Override
    public void desc(String[] params) throws Exception {
        String tableName = params[1];
        if (tableName.length() > Common.SYS_TABLE_KEY_MAX_LENGTH) {
            throw new ConsoleMessageException(
                    "The table name length is greater than "
                            + Common.SYS_TABLE_KEY_MAX_LENGTH
                            + ".");
        }
        CRUDParseUtils.invalidSymbol(tableName);
        if (tableName.endsWith(";")) {
            tableName = tableName.substring(0, tableName.length() - 1);
        }
        try {
            List<Map<String, String>> tableDesc = tableCRUDService.desc(tableName);
            if (!checkTableExistence(tableName, tableDesc)) {
                return;
            }
            String tableInfo = ObjectMapperFactory.getObjectMapper().writeValueAsString(tableDesc);
            ConsoleUtils.printJson(tableInfo);
            System.out.println();
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void freezeContract(String[] params) throws Exception {
        String address = params[1];
        ConsoleUtils.printRetCode(contractLifeCycleService.freeze(address));
        System.out.println();
    }

    @Override
    public void unfreezeContract(String[] params) throws Exception {
        String address = params[1];
        ConsoleUtils.printRetCode(contractLifeCycleService.unfreeze(address));
        System.out.println();
    }

    @Override
    public void grantContractStatusManager(String[] params) throws Exception {
        String contractAddr = params[1];
        String userAddr = params[2];
        ConsoleUtils.printJson(
                contractLifeCycleService.grantManager(contractAddr, userAddr).getMessage());
        System.out.println();
    }

    @Override
    public void getContractStatus(String[] params) throws Exception {
        String address = params[1];
        ConsoleUtils.printJson(contractLifeCycleService.getContractStatus(address));
        System.out.println();
    }

    @Override
    public void listContractStatusManager(String[] params) throws Exception {
        String address = params[1];
        ConsoleUtils.printJson(
                ObjectMapperFactory.getObjectMapper()
                        .writeValueAsString(contractLifeCycleService.listManager(address)));
        System.out.println();
    }

    @Override
    public void createTable(String sql) throws Exception {
        Table table = new Table();
        try {
            CRUDParseUtils.parseCreateTable(sql, table);
        } catch (ConsoleMessageException e) {
            System.out.println(e.getMessage());
            System.out.println();
            logger.error(" message: {}, e: {}", e.getMessage(), e);
            return;
        } catch (JSQLParserException | NullPointerException e) {
            System.out.println("Could not parse SQL statement.");
            CRUDParseUtils.invalidSymbol(sql);
            System.out.println();
            return;
        }
        try {
            RetCode result =
                    tableCRUDService.createTable(
                            table.getTableName(), table.getKey(), table.getValueFields());
            // parse the result
            if (result.getCode() == PrecompiledRetCode.CODE_SUCCESS.getCode()) {
                System.out.println("Create '" + table.getTableName() + "' Ok.");
            } else {
                System.out.println("Create '" + table.getTableName() + "' failed ");
                System.out.println(" ret message: " + result.getMessage());
                System.out.println(" ret code: " + result.getCode());
            }
            System.out.println();
        } catch (ContractException e) {
            outputErrorMessageForTableCRUD(table, null, sql, e.getErrorCode(), e.getMessage());
        } catch (ClientException e) {
            outputErrorMessageForTableCRUD(table, null, sql, e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            throw e;
        }
    }

    private void outputErrorMessageForTableCRUD(
            Table table, Entry entry, String command, int code, String message) {
        System.out.println("call " + command + " failed!");
        System.out.println("* code: " + code);
        System.out.println("* message: " + message);
        System.out.println();
        if (code != TransactionReceiptStatus.PrecompiledError.getCode()) {
            return;
        }
        if (table == null) {
            System.out.println();
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
        if (table.getKey().length() > PrecompiledConstant.TABLE_KEY_VALUE_MAX_LENGTH) {
            System.out.println("Invalid key name " + table.getKey());
            System.out.println(
                    "* The key length must be no greater than "
                            + PrecompiledConstant.TABLE_KEY_VALUE_MAX_LENGTH
                            + " , current length of the table is "
                            + table.getKey().length());
        }
        if (table.getValueFields() != null) {
            for (String field : table.getValueFields()) {
                if (field.length() > PrecompiledConstant.TABLE_FIELD_NAME_MAX_LENGTH) {
                    System.out.println("Invalid filed " + field);

                    System.out.println(
                            "* Field length must be no greater than "
                                    + PrecompiledConstant.TABLE_FIELD_NAME_MAX_LENGTH
                                    + ", current length is: "
                                    + field.length());
                }
            }
        }
        if (entry == null) {
            System.out.println();
            return;
        }
        Map<String, String> filedNameToValue = entry.getFieldNameToValue();
        for (String key : filedNameToValue.keySet()) {
            if (key.length() > PrecompiledConstant.TABLE_FIELD_NAME_MAX_LENGTH) {
                System.out.println("Invalid field name " + key);
                System.out.println(
                        "* Field length must be no greater than "
                                + PrecompiledConstant.TABLE_FIELD_NAME_MAX_LENGTH
                                + ", current length:"
                                + key.length());
            }
            String value = filedNameToValue.get(key);
            if (value.length() > PrecompiledConstant.USER_TABLE_FIELD_VALUE_MAX_LENGTH) {
                System.out.println("Invalid field value for " + key);
                System.out.println(
                        "* Value of Field must be no greater than: "
                                + PrecompiledConstant.USER_TABLE_FIELD_VALUE_MAX_LENGTH
                                + ", current length is "
                                + value.length());
            }
        }
        System.out.println();
    }

    @Override
    public void insert(String sql) throws Exception {
        Table table = new Table();
        Entry entry = new Entry();
        try {
            String tableName = CRUDParseUtils.parseInsertedTableName(sql);
            List<Map<String, String>> descTable = tableCRUDService.desc(tableName);
            if (!checkTableExistence(table.getTableName(), descTable)) {
                return;
            }
            logger.debug(
                    "insert, tableName: {}, descTable: {}", tableName, descTable.get(0).toString());
            CRUDParseUtils.parseInsert(sql, table, entry, descTable.get(0));
            String keyName = descTable.get(0).get(PrecompiledConstant.KEY_FIELD_NAME);
            String keyValue = entry.getFieldNameToValue().get(keyName);
            logger.debug(
                    "fieldNameToValue: {}, keyName: {}, keyValue: {}",
                    entry.getFieldNameToValue(),
                    keyName,
                    keyValue);
            if (keyValue == null) {
                throw new ConsoleMessageException("Please insert the key field '" + keyName + "'.");
            }
            table.setKey(keyValue);
            RetCode insertResult =
                    tableCRUDService.insert(table.getTableName(), table.getKey(), entry, null);

            if (insertResult.getCode() == PrecompiledRetCode.CODE_SUCCESS.getCode()
                    || insertResult.getCode() == 1) {
                System.out.println("Insert OK: ");
                ConsoleUtils.printRetCode(insertResult);
                System.out.println(insertResult.getCode() + " row affected.");
            } else {
                System.out.println("Insert failed");
                System.out.println("Ret message:" + insertResult.getMessage());
                System.out.println("Ret code: " + insertResult.getCode());
            }
            System.out.println();
        } catch (ConsoleMessageException e) {
            System.out.println(e.getMessage());
            System.out.println();
            logger.error(" message: {}, e: {}", e.getMessage(), e);
            return;
        } catch (JSQLParserException | NullPointerException e) {
            System.out.println("Could not parse SQL statement, error message: " + e.getMessage());
            CRUDParseUtils.invalidSymbol(sql);
            System.out.println();
            return;
        } catch (ContractException e) {
            outputErrorMessageForTableCRUD(table, entry, sql, e.getErrorCode(), e.getMessage());
        } catch (ClientException e) {
            outputErrorMessageForTableCRUD(table, entry, sql, e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void update(String sql) throws Exception {
        Table table = new Table();
        Entry entry = new Entry();
        Condition condition = new Condition();
        try {
            CRUDParseUtils.parseUpdate(sql, table, entry, condition);
        } catch (ConsoleMessageException e) {
            System.out.println(e.getMessage());
            System.out.println();
            logger.error(" message: {}, e: {}", e.getMessage(), e);
            return;
        } catch (JSQLParserException | NullPointerException e) {
            System.out.println("Could not parse SQL statement.");
            CRUDParseUtils.invalidSymbol(sql);
            System.out.println();
            return;
        }
        try {
            String tableName = table.getTableName();
            List<Map<String, String>> descTable = tableCRUDService.desc(tableName);
            if (!checkTableExistence(table.getTableName(), descTable)) {
                return;
            }
            String keyName = descTable.get(0).get(PrecompiledConstant.KEY_FIELD_NAME);
            table.setKey(keyName);
            handleKey(table, condition);
            RetCode updateResult =
                    tableCRUDService.update(table.getTableName(), table.getKey(), entry, condition);
            if (updateResult.getCode() == PrecompiledRetCode.CODE_SUCCESS.getCode()
                    || updateResult.getCode() == 1) {
                System.out.println("Update OK: ");
                ConsoleUtils.printRetCode(updateResult);
            } else {
                System.out.println("Result of update " + tableName + " :");
                ConsoleUtils.printRetCode(updateResult);
            }
            System.out.println(updateResult.getCode() + " row affected.");
            System.out.println();
        } catch (ContractException e) {
            outputErrorMessageForTableCRUD(table, entry, sql, e.getErrorCode(), e.getMessage());
        } catch (ClientException e) {
            outputErrorMessageForTableCRUD(table, entry, sql, e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void remove(String sql) throws Exception {
        Table table = new Table();
        Condition condition = new Condition();
        try {
            CRUDParseUtils.parseRemove(sql, table, condition);
        } catch (ConsoleMessageException e) {
            System.out.println(e.getMessage());
            System.out.println();
            logger.error(" message: {}, e: {}", e.getMessage(), e);
            return;
        } catch (JSQLParserException | NullPointerException e) {
            System.out.println("Could not parse SQL statement.");
            CRUDParseUtils.invalidSymbol(sql);
            System.out.println();
            return;
        }
        try {
            List<Map<String, String>> descTable = tableCRUDService.desc(table.getTableName());
            if (!checkTableExistence(table.getTableName(), descTable)) {
                return;
            }
            table.setKey(descTable.get(0).get(PrecompiledConstant.KEY_FIELD_NAME));
            handleKey(table, condition);
            RetCode removeResult =
                    tableCRUDService.remove(table.getTableName(), table.getKey(), condition);
            if (removeResult.getCode() == PrecompiledRetCode.CODE_SUCCESS.getCode()
                    || removeResult.getCode() == 1) {
                System.out.println("Remove OK, " + removeResult + " row affected.");
            } else {
                System.out.println("Result of remove " + table.getTableName() + " :");
                ConsoleUtils.printRetCode(removeResult);
            }
            System.out.println();
        } catch (ContractException e) {
            outputErrorMessageForTableCRUD(table, null, sql, e.getErrorCode(), e.getMessage());
        } catch (ClientException e) {
            outputErrorMessageForTableCRUD(table, null, sql, e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            throw e;
        }
    }

    private boolean checkTableExistence(String tableName, List<Map<String, String>> descTable) {
        if (descTable.size() == 0
                || descTable.get(0).get(PrecompiledConstant.KEY_FIELD_NAME).equals("")) {
            System.out.println("The table \"" + tableName + "\" doesn't exist!");
            System.out.println();
            return false;
        }
        return true;
    }

    @Override
    public void select(String sql) throws Exception {
        Table table = new Table();
        Condition condition = new Condition();
        List<String> selectColumns = new ArrayList<>();
        try {
            CRUDParseUtils.parseSelect(sql, table, condition, selectColumns);
        } catch (ConsoleMessageException e) {
            System.out.println(e.getMessage());
            System.out.println();
            logger.error(" message: {}, e: {}", e.getMessage(), e);
            return;
        } catch (JSQLParserException | NullPointerException e) {
            System.out.println("Could not parse SQL statement.");
            CRUDParseUtils.invalidSymbol(sql);
            System.out.println();
            return;
        }
        try {
            List<Map<String, String>> descTable = tableCRUDService.desc(table.getTableName());
            if (!checkTableExistence(table.getTableName(), descTable)) {
                return;
            }
            table.setKey(descTable.get(0).get(PrecompiledConstant.KEY_FIELD_NAME));
            handleKey(table, condition);
            List<Map<String, String>> result =
                    tableCRUDService.select(table.getTableName(), table.getKey(), condition);
            int rows = 0;
            if (result.size() == 0) {
                System.out.println("Empty set.");
                System.out.println();
                return;
            }
            result = filterSystemColum(result);
            if ("*".equals(selectColumns.get(0))) {
                selectColumns.clear();
                selectColumns.add(table.getKey());
                String[] valueArr =
                        descTable.get(0).get(PrecompiledConstant.VALUE_FIELD_NAME).split(",");
                selectColumns.addAll(Arrays.asList(valueArr));
                result = getSelectedColumn(selectColumns, result);
                rows = result.size();
            } else {
                List<Map<String, String>> selectedResult = getSelectedColumn(selectColumns, result);
                rows = selectedResult.size();
            }
            if (rows == 1) {
                System.out.println(rows + " row in set.");
            } else {
                System.out.println(rows + " rows in set.");
            }
        } catch (ContractException e) {
            outputErrorMessageForTableCRUD(table, null, sql, e.getErrorCode(), e.getMessage());
        } catch (ClientException e) {
            outputErrorMessageForTableCRUD(table, null, sql, e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            throw e;
        }
        System.out.println();
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
        selectedResult.stream().forEach(System.out::println);
        return selectedResult;
    }

    private List<Map<String, String>> filterSystemColum(List<Map<String, String>> result) {

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

    private void handleKey(Table table, Condition condition) throws Exception {

        String keyName = table.getKey();
        String keyValue = "";
        Map<ConditionOperator, String> keyMap = condition.getConditions().get(keyName);
        if (keyMap == null) {
            throw new ConsoleMessageException(
                    "Please provide a equal condition for the key field '"
                            + keyName
                            + "' in where clause.");
        } else {
            Set<ConditionOperator> keySet = keyMap.keySet();
            for (ConditionOperator enumOP : keySet) {
                if (enumOP != ConditionOperator.eq) {
                    throw new ConsoleMessageException(
                            "Please provide a equal condition for the key field '"
                                    + keyName
                                    + "' in where clause.");
                } else {
                    keyValue = keyMap.get(enumOP);
                }
            }
        }
        table.setKey(keyValue);
    }

    public void queryCNS(String[] params) throws Exception {
        if (params.length == 2) {
            // get contract name
            ConsoleUtils.printJson(cnsService.selectByName(params[1]).toString());
        }
        if (params.length == 3) {
            ConsoleUtils.printJson(
                    cnsService.selectByNameAndVersion(params[1], params[2]).toString());
        }
    }
}
