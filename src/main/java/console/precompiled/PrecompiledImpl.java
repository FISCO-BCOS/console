package console.precompiled;

import console.common.Common;
import console.common.ConsoleUtils;
import console.contract.exceptions.CompileContractException;
import console.contract.model.AbiAndBin;
import console.contract.utils.ContractCompiler;
import console.exception.ConsoleMessageException;
import console.precompiled.model.CRUDParseUtils;
import console.precompiled.model.Table;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sf.jsqlparser.JSQLParserException;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.client.exceptions.ClientException;
import org.fisco.bcos.sdk.contract.precompiled.cns.CnsInfo;
import org.fisco.bcos.sdk.contract.precompiled.cns.CnsService;
import org.fisco.bcos.sdk.contract.precompiled.consensus.ConsensusService;
import org.fisco.bcos.sdk.contract.precompiled.contractmgr.ContractLifeCycleService;
import org.fisco.bcos.sdk.contract.precompiled.crud.TableCRUDService;
import org.fisco.bcos.sdk.contract.precompiled.crud.common.Condition;
import org.fisco.bcos.sdk.contract.precompiled.crud.common.ConditionOperator;
import org.fisco.bcos.sdk.contract.precompiled.crud.common.Entry;
import org.fisco.bcos.sdk.contract.precompiled.gaschargemgr.GasChargeManageService;
import org.fisco.bcos.sdk.contract.precompiled.sysconfig.SystemConfigService;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.model.PrecompiledConstant;
import org.fisco.bcos.sdk.model.PrecompiledRetCode;
import org.fisco.bcos.sdk.model.RetCode;
import org.fisco.bcos.sdk.model.TransactionReceiptStatus;
import org.fisco.bcos.sdk.transaction.model.dto.TransactionResponse;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;
import org.fisco.bcos.sdk.utils.Numeric;
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
    private GasChargeManageService gasChargeManageService;

    public PrecompiledImpl(Client client) throws ContractException {
        this.client = client;
        CryptoKeyPair cryptoKeyPair = client.getCryptoSuite().getCryptoKeyPair();
        this.consensusService = new ConsensusService(client, cryptoKeyPair);
        this.systemConfigService = new SystemConfigService(client, cryptoKeyPair);
        this.tableCRUDService = new TableCRUDService(client, cryptoKeyPair);
        this.contractLifeCycleService = new ContractLifeCycleService(client, cryptoKeyPair);
        this.cnsService = new CnsService(client, cryptoKeyPair);
        this.gasChargeManageService = new GasChargeManageService(client, cryptoKeyPair);
    }

    @Override
    public void addSealer(String[] params) throws Exception {
        String nodeId = params[1];
        if (nodeId.length() != 128) {
            ConsoleUtils.printJson(PrecompiledRetCode.CODE_INVALID_NODEID.toString());
        } else {
            ConsoleUtils.printJson(this.consensusService.addSealer(nodeId).toString());
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
                        return;
                    }
                } else if (Common.TxGasLimit.equals(key) && value < Common.TxGasLimitMin) {
                    System.out.println(
                            "Please provide value by positive integer mode, "
                                    + Common.TxGasLimitRange
                                    + ".");
                    return;
                } else if (Common.ConsensusTimeout.equals(key)
                        && (value < Common.ConsensusTimeoutMin
                                || value >= Common.ConsensusTimeoutMax)) {
                    System.out.println(
                            "Please provide value by positive integer mode, "
                                    + Common.ConsensusTimeoutRange
                                    + ".");
                    return;
                }

                if (Common.RPBFTEpochSealerNum.equals(key)
                        || Common.RPBFTEpochBlockNum.equals(key)) {
                    System.out.println("Note: " + key + " only takes effect when rPBFT is used!");
                }
                ConsoleUtils.printJson(
                        this.systemConfigService.setValueByKey(key, value + "").toString());
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
    }

    @Override
    public void desc(String[] params) throws Exception {
        String tableName = params[1];
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
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void freezeContract(String[] params) throws Exception {
        String address = params[1];
        ConsoleUtils.printJson(contractLifeCycleService.freeze(address).toString());
    }

    @Override
    public void unfreezeContract(String[] params) throws Exception {
        String address = params[1];
        ConsoleUtils.printJson(contractLifeCycleService.unfreeze(address).toString());
    }

    @Override
    public void grantContractStatusManager(String[] params) throws Exception {
        String contractAddr = params[1];
        String userAddr = params[2];
        ConsoleUtils.printJson(
                contractLifeCycleService.grantManager(contractAddr, userAddr).toString());
    }

    @Override
    public void revokeContractStatusManager(String[] params) throws Exception {
        String contractAddr = params[1];
        String userAddr = params[2];
        ConsoleUtils.printJson(
                contractLifeCycleService.revokeManager(contractAddr, userAddr).toString());
    }

    @Override
    public void getContractStatus(String[] params) throws Exception {
        String address = params[1];
        ConsoleUtils.printJson(contractLifeCycleService.getContractStatus(address));
    }

    @Override
    public void listContractStatusManager(String[] params) throws Exception {
        String address = params[1];
        ConsoleUtils.printJson(
                ObjectMapperFactory.getObjectMapper()
                        .writeValueAsString(contractLifeCycleService.listManager(address)));
    }

    @Override
    public void createTable(String sql) throws Exception {
        Table table = new Table();
        try {
            CRUDParseUtils.parseCreateTable(sql, table);
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
            RetCode result =
                    tableCRUDService.createTable(
                            table.getTableName(), table.getKey(), table.getValueFields());
            // parse the result
            if (result.getCode() == PrecompiledRetCode.CODE_SUCCESS.getCode()) {
                System.out.println("Create '" + table.getTableName() + "' Ok.");
            } else {
                System.out.println("Create '" + table.getTableName() + "' failed ");
                ConsoleUtils.printJson(result.toString());
            }
        } catch (ContractException e) {
            outputErrorMessageForTableCRUD(
                    table, null, sql, e.getErrorCode(), e.getMessage(), null);
        } catch (ClientException e) {
            outputErrorMessageForTableCRUD(
                    table, null, sql, e.getErrorCode(), e.getMessage(), null);
        } catch (Exception e) {
            throw e;
        }
    }

    private boolean checkTableField(List<Map<String, String>> descTable, Entry entry) {
        if (descTable == null || descTable.size() == 0) {
            return true;
        }
        List<String> descFieldList = new ArrayList<String>();
        Collections.addAll(
                descFieldList,
                descTable.get(0).get(PrecompiledConstant.VALUE_FIELD_NAME).split(","));
        String key = descTable.get(0).get(PrecompiledConstant.KEY_FIELD_NAME);
        // check field
        if (entry != null) {
            Set<String> fieldSet = entry.getFieldNameToValue().keySet();
            for (String field : fieldSet) {
                if (field.equals(key)) {
                    continue;
                }
                if (!descFieldList.contains(field)) {
                    System.out.println(
                            "Unknown field \""
                                    + field
                                    + "\", current supported fields are "
                                    + descFieldList.toString());
                    return false;
                }
            }
        }
        return true;
    }

    private void outputErrorMessageForTableCRUD(
            Table table,
            Entry entry,
            String command,
            int code,
            String message,
            List<Map<String, String>> descTable) {
        System.out.println("call " + command + " failed!");
        System.out.println("* code: " + code);
        System.out.println("* message: " + message);

        if (code != TransactionReceiptStatus.PrecompiledError.getCode()) {
            return;
        }
        // when supported_version >= v2.7.0, no need try to get the precompiled message
        if (ConsoleUtils.checkVersion("tryToGuessPrecompiledMessage", client, "2.7.0", false)) {
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
        if (table.getKey().length() > PrecompiledConstant.TABLE_KEY_VALUE_MAX_LENGTH) {
            System.out.println("Invalid key value " + table.getKey());
            System.out.println(
                    "* The value of the key must be no greater than "
                            + PrecompiledConstant.TABLE_KEY_VALUE_MAX_LENGTH
                            + " , current length of the table is "
                            + table.getKey().length());
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
        for (String key : fieldNameToValue.keySet()) {
            if (key.length() > PrecompiledConstant.TABLE_FIELD_NAME_MAX_LENGTH) {
                System.out.println("Invalid field name " + key);
                System.out.println(
                        "* Field length must be no greater than "
                                + PrecompiledConstant.TABLE_FIELD_NAME_MAX_LENGTH
                                + ", current length:"
                                + key.length());
            }
            String value = fieldNameToValue.get(key);
            if (value.length() > PrecompiledConstant.USER_TABLE_FIELD_VALUE_MAX_LENGTH) {
                System.out.println("Invalid field value for " + key);
                System.out.println(
                        "* Value of Field must be no greater than: "
                                + PrecompiledConstant.USER_TABLE_FIELD_VALUE_MAX_LENGTH
                                + ", current length is "
                                + value.length());
            }
        }
    }

    @Override
    public void insert(String sql) throws Exception {
        Table table = new Table();
        Entry entry = new Entry();
        List<Map<String, String>> descTable = null;
        try {
            String tableName = CRUDParseUtils.parseInsertedTableName(sql);
            descTable = tableCRUDService.desc(tableName);
            if (!checkTableExistence(tableName, descTable)) {
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
                    tableCRUDService.insert(table.getTableName(), table.getKey(), entry);

            if (insertResult.getCode() >= 0) {
                System.out.println("Insert OK: ");
                System.out.println(insertResult.getCode() + " row affected.");
            } else {
                System.out.println("Result of insert for " + table.getTableName() + ":");
                ConsoleUtils.printJson(insertResult.toString());
            }

        } catch (ConsoleMessageException e) {
            System.out.println(e.getMessage());
            logger.error(" message: {}, e: {}", e.getMessage(), e);
            return;
        } catch (JSQLParserException | NullPointerException e) {
            System.out.println("Could not parse SQL statement, error message: " + e.getMessage());
            CRUDParseUtils.invalidSymbol(sql);
            return;
        } catch (ContractException e) {
            outputErrorMessageForTableCRUD(
                    table, entry, sql, e.getErrorCode(), e.getMessage(), descTable);
        } catch (ClientException e) {
            outputErrorMessageForTableCRUD(
                    table, entry, sql, e.getErrorCode(), e.getMessage(), descTable);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void update(String sql) throws Exception {
        Table table = new Table();
        Entry entry = new Entry();
        Condition condition = new Condition();
        List<Map<String, String>> descTable = null;
        try {
            CRUDParseUtils.parseUpdate(sql, table, entry, condition);
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
            String tableName = table.getTableName();
            descTable = tableCRUDService.desc(tableName);
            if (!checkTableExistence(table.getTableName(), descTable)) {
                return;
            }
            String keyName = descTable.get(0).get(PrecompiledConstant.KEY_FIELD_NAME);
            if (entry.getFieldNameToValue().containsKey(keyName)) {
                System.out.println("Please don't set the key field \"" + keyName + "\".");
                return;
            }
            table.setKey(keyName);
            handleKey(table, condition);
            RetCode updateResult =
                    tableCRUDService.update(table.getTableName(), table.getKey(), entry, condition);
            if (updateResult.getCode() >= 0) {
                System.out.println(updateResult.getCode() + " row affected.");
            } else {
                System.out.println("Result of update " + tableName + " :");
                ConsoleUtils.printJson(updateResult.toString());
            }

        } catch (ContractException e) {
            outputErrorMessageForTableCRUD(
                    table, entry, sql, e.getErrorCode(), e.getMessage(), descTable);
        } catch (ClientException e) {
            outputErrorMessageForTableCRUD(
                    table, entry, sql, e.getErrorCode(), e.getMessage(), descTable);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void remove(String sql) throws Exception {
        Table table = new Table();
        Condition condition = new Condition();
        List<Map<String, String>> descTable = null;
        try {
            CRUDParseUtils.parseRemove(sql, table, condition);
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
            table.setKey(descTable.get(0).get(PrecompiledConstant.KEY_FIELD_NAME));
            handleKey(table, condition);
            RetCode removeResult =
                    tableCRUDService.remove(table.getTableName(), table.getKey(), condition);

            if (removeResult.getCode() >= 0) {
                System.out.println("Remove OK, " + removeResult.getCode() + " row affected.");
            } else {
                System.out.println("Result of Remove " + table.getTableName() + " :");
                ConsoleUtils.printJson(removeResult.toString());
            }
        } catch (ContractException e) {
            outputErrorMessageForTableCRUD(
                    table, null, sql, e.getErrorCode(), e.getMessage(), descTable);
        } catch (ClientException e) {
            outputErrorMessageForTableCRUD(
                    table, null, sql, e.getErrorCode(), e.getMessage(), descTable);
        } catch (Exception e) {
            throw e;
        }
    }

    private boolean checkTableExistence(String tableName, List<Map<String, String>> descTable) {
        if (descTable.size() == 0
                || descTable.get(0).get(PrecompiledConstant.KEY_FIELD_NAME).equals("")) {
            System.out.println("The table \"" + tableName + "\" doesn't exist!");
            return false;
        }
        return true;
    }

    @Override
    public void select(String sql) throws Exception {
        Table table = new Table();
        Condition condition = new Condition();
        List<String> selectColumns = new ArrayList<>();
        List<Map<String, String>> descTable = null;
        try {
            CRUDParseUtils.parseSelect(sql, table, condition, selectColumns);
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
            String keyField = descTable.get(0).get(PrecompiledConstant.KEY_FIELD_NAME);
            table.setKey(keyField);
            handleKey(table, condition);
            List<Map<String, String>> result =
                    tableCRUDService.select(table.getTableName(), table.getKey(), condition);
            int rows = 0;
            if (result.size() == 0) {
                System.out.println("Empty set.");
                return;
            }
            result = filterSystemColum(result);
            if ("*".equals(selectColumns.get(0))) {
                selectColumns.clear();
                selectColumns.add(keyField);
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
            outputErrorMessageForTableCRUD(
                    table, null, sql, e.getErrorCode(), e.getMessage(), descTable);
        } catch (ClientException e) {
            outputErrorMessageForTableCRUD(
                    table, null, sql, e.getErrorCode(), e.getMessage(), descTable);
        } catch (Exception e) {
            throw e;
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

    @Override
    public void queryCNS(String[] params) throws Exception {
        String contractNameOrPath = ConsoleUtils.resolveContractPath(params[1]);
        String contractName = ConsoleUtils.getContractName(contractNameOrPath);
        List<CnsInfo> cnsInfos = null;
        if (params.length == 2) {
            // get contract name
            cnsInfos = cnsService.selectByName(contractName);
        }
        if (params.length == 3) {
            cnsInfos = cnsService.selectByNameAndVersion(contractName, params[2]);
        }
        ConsoleUtils.singleLine();
        if (cnsInfos == null || cnsInfos.isEmpty()) {
            System.out.println("Empty set.");
            ConsoleUtils.singleLine();
            return;
        }
        for (int i = 0; i < cnsInfos.size(); i++) {
            System.out.println("* contract address: " + cnsInfos.get(i).getAddress());
            System.out.println("* contract version: " + cnsInfos.get(i).getVersion());
            ConsoleUtils.singleLine();
        }
    }

    @Override
    public void registerCNS(String[] params) throws Exception {
        String contractNameOrPath = ConsoleUtils.resolveContractPath(params[1]);
        String contractName = ConsoleUtils.getContractName(contractNameOrPath);
        String contractAddress = params[2];
        String contractVersion = params[3];
        String abi = "";
        try {
            AbiAndBin abiAndBin =
                    ContractCompiler.loadAbiAndBin(
                            client.getGroupId(),
                            contractName,
                            contractNameOrPath,
                            contractAddress,
                            false);
            abi = abiAndBin.getAbi();
        } catch (CompileContractException e) {
            logger.warn(
                    "load abi for contract failed, contract name: {}, address: {}, e: {}",
                    contractAddress,
                    contractAddress,
                    e.getMessage());
        }
        if (abi.equals("")) {
            System.out.println(
                    "Warn: \nPlease make sure the existence of the contract, contractName: "
                            + contractName
                            + ", contractAddress: "
                            + contractAddress
                            + "!");
        }
        ConsoleUtils.printJson(
                cnsService
                        .registerCNS(contractName, contractVersion, contractAddress, abi)
                        .toString());
        System.out.println();
    }

    @Override
    public void chargeGas(String[] params) throws Exception {
        String accountAddress = params[1];
        String chargeGas = params[2];
        BigInteger chargeGasValue = BigInteger.ZERO;
        try {
            chargeGasValue = Numeric.decodeQuantity(chargeGas);
        } catch (Exception e) {
            System.out.println("Invalid charge gas, expected to integer, provided " + chargeGas);
        }
        TransactionResponse response =
                this.gasChargeManageService.charge(accountAddress, chargeGasValue);
        ConsoleUtils.printPrecompiledResponse(response);
    }

    @Override
    public void deductGas(String[] params) throws Exception {
        String accountAddress = params[1];
        String chargeGas = params[2];
        BigInteger chargeGasValue = BigInteger.ZERO;
        try {
            chargeGasValue = Numeric.decodeQuantity(chargeGas);
        } catch (Exception e) {
            System.out.println("Invalid deduct gas, expected to integer, provided " + chargeGas);
        }
        TransactionResponse response =
                this.gasChargeManageService.deduct(accountAddress, chargeGasValue);
        ConsoleUtils.printPrecompiledResponse(response);
    }

    @Override
    public void queryRemainGas(String[] params) throws Exception {
        String accountAddress = params[1];
        ConsoleUtils.singleLine();
        System.out.println("* queryRemainGas");
        ConsoleUtils.singleLine();
        System.out.println("* accountAddress: " + accountAddress);
        System.out.println(
                "* remainGas: " + this.gasChargeManageService.queryRemainGas(accountAddress));
        ConsoleUtils.singleLine();
    }

    @Override
    public void grantCharger(String[] params) throws Exception {
        String accountAddress = params[1];
        ConsoleUtils.singleLine();
        System.out.println("* grantCharger");
        ConsoleUtils.singleLine();
        System.out.println("grantedAddress: " + accountAddress);
        ConsoleUtils.singleLine();
        System.out.println("* result:");
        ConsoleUtils.printJson(this.gasChargeManageService.grantCharger(accountAddress).toString());
    }

    @Override
    public void revokeCharger(String[] params) throws Exception {
        String accountAddress = params[1];
        ConsoleUtils.singleLine();
        System.out.println("* revokeCharger");
        ConsoleUtils.singleLine();
        System.out.println("revokedAddress: " + accountAddress);
        ConsoleUtils.singleLine();
        System.out.println("* result:");
        ConsoleUtils.printJson(
                this.gasChargeManageService.revokeCharger(accountAddress).toString());
    }

    @Override
    public void listChargers(String[] params) throws Exception {
        ConsoleUtils.singleLine();
        System.out.println("* listChargers");
        ConsoleUtils.singleLine();
        List<String> chargerList = this.gasChargeManageService.listChargers();
        if (chargerList.isEmpty()) {
            System.out.println("Empty set.");
            return;
        }
        for (String charger : chargerList) {
            System.out.println("* " + charger);
        }
        ConsoleUtils.singleLine();
    }
}
