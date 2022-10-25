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
import org.fisco.bcos.sdk.v3.contract.precompiled.bfs.BFSPrecompiled.BfsInfo;
import org.fisco.bcos.sdk.v3.contract.precompiled.bfs.BFSService;
import org.fisco.bcos.sdk.v3.contract.precompiled.consensus.ConsensusService;
import org.fisco.bcos.sdk.v3.contract.precompiled.crud.TableCRUDService;
import org.fisco.bcos.sdk.v3.contract.precompiled.crud.common.Condition;
import org.fisco.bcos.sdk.v3.contract.precompiled.crud.common.Entry;
import org.fisco.bcos.sdk.v3.contract.precompiled.crud.common.UpdateFields;
import org.fisco.bcos.sdk.v3.contract.precompiled.sysconfig.SystemConfigService;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.v3.model.PrecompiledConstant;
import org.fisco.bcos.sdk.v3.model.PrecompiledRetCode;
import org.fisco.bcos.sdk.v3.model.RetCode;
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
    private BFSService bfsService;
    private String pwd = "/apps";

    public PrecompiledImpl(Client client) {
        this.client = client;
        CryptoKeyPair cryptoKeyPair = client.getCryptoSuite().getCryptoKeyPair();
        this.consensusService = new ConsensusService(client, cryptoKeyPair);
        this.systemConfigService = new SystemConfigService(client, cryptoKeyPair);
        this.tableCRUDService = new TableCRUDService(client, cryptoKeyPair);
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
        Map<String, List<String>> tableDesc = tableCRUDService.desc(tableName);
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
            Map<String, List<String>> descTable = tableCRUDService.desc(tableName);
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
            Condition condition = CRUDParseUtils.parseUpdate(sql, table, updateFields);

            String keyValue = condition.getEqValue();
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
            Condition condition = CRUDParseUtils.parseRemove(sql, table);
            String keyValue = condition.getEqValue();
            RetCode removeResult =
                    keyValue.isEmpty()
                            ? tableCRUDService.remove(table.getTableName(), condition)
                            : tableCRUDService.remove(table.getTableName(), keyValue);

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
                && !descTable.get(PrecompiledConstant.KEY_FIELD_NAME).get(0).equals("");
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
            Condition condition = CRUDParseUtils.parseSelect(sql, table, selectColumns);
            String keyValue = condition.getEqValue();
            List<Map<String, String>> result = new ArrayList<>();
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
        try {
            BigInteger fileLeft;
            BigInteger offset = BigInteger.ZERO;
            do {
                Tuple2<BigInteger, List<BfsInfo>> fileInfoList =
                        bfsService.list(listPath, offset, Common.LS_DEFAULT_COUNT);
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
        } catch (ContractException e) {
            RetCode precompiledResponse =
                    PrecompiledRetCode.getPrecompiledResponse(e.getErrorCode(), e.getMessage());
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
        if (!linkPath.startsWith(ContractCompiler.BFS_APPS_PREFIX)) {
            System.out.println("Link must locate in /apps.");
            System.out.println("Example: ln /apps/Name 0x1234567890");
            return;
        }
        String contractName = FilenameUtils.getBaseName(linkPath);
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

        ConsoleUtils.printJson(
                bfsService
                        .link(
                                linkPath.substring(ContractCompiler.BFS_APPS_PREFIX.length()),
                                contractAddress,
                                abi)
                        .toString());
        System.out.println();
    }

    @Override
    public String getPwd() {
        return pwd;
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
}
