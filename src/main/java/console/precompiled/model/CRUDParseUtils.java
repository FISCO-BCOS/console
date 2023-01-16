package console.precompiled.model;

import console.exception.ConsoleMessageException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NotExpression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.ComparisonOperator;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.alter.AlterExpression;
import net.sf.jsqlparser.statement.alter.AlterOperation;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.fisco.bcos.sdk.v3.contract.precompiled.crud.common.Common;
import org.fisco.bcos.sdk.v3.contract.precompiled.crud.common.Condition;
import org.fisco.bcos.sdk.v3.contract.precompiled.crud.common.ConditionV320;
import org.fisco.bcos.sdk.v3.contract.precompiled.crud.common.Entry;
import org.fisco.bcos.sdk.v3.contract.precompiled.crud.common.UpdateFields;
import org.fisco.bcos.sdk.v3.model.PrecompiledConstant;
import org.fisco.bcos.sdk.v3.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CRUDParseUtils {
    private static final Logger logger = LoggerFactory.getLogger(CRUDParseUtils.class);
    public static final String PRIMARY_KEY = "primary key";

    public static void parseCreateTable(String sql, Table table)
            throws JSQLParserException, ConsoleMessageException {
        Statement statement = CCJSqlParserUtil.parse(sql);
        CreateTable createTable = (CreateTable) statement;

        // parse table name
        String tableName = createTable.getTable().getName();
        table.setTableName(tableName);

        // parse key from index
        boolean keyFlag = false;
        List<Index> indexes = createTable.getIndexes();
        if (indexes != null) {
            if (indexes.size() > 1) {
                throw new ConsoleMessageException(
                        "Please provide only one primary key for the table.");
            }
            keyFlag = true;
            Index index = indexes.get(0);
            String type = index.getType().toLowerCase();
            if (PRIMARY_KEY.equals(type)) {
                table.setKeyFieldName(index.getColumnsNames().get(0));
            } else {
                throw new ConsoleMessageException(
                        "Please provide only one primary key for the table.");
            }
        }
        List<ColumnDefinition> columnDefinitions = createTable.getColumnDefinitions();
        // parse key from ColumnDefinition
        for (ColumnDefinition definition : columnDefinitions) {
            List<String> columnSpecStrings = definition.getColumnSpecStrings();
            if (columnSpecStrings == null) {
                continue;
            }
            if (columnSpecStrings.size() == 2
                    && "primary".equals(columnSpecStrings.get(0))
                    && "key".equals(columnSpecStrings.get(1))) {
                String key = definition.getColumnName();
                if (keyFlag) {
                    if (!table.getKeyFieldName().equals(key)) {
                        throw new ConsoleMessageException(
                                "Please provide only one primary key for the table.");
                    }
                } else {
                    keyFlag = true;
                    table.setKeyFieldName(key);
                }
                break;
            }
        }
        if (!keyFlag) {
            throw new ConsoleMessageException("Please provide a primary key for the table.");
        }
        // parse value field
        List<String> fieldsList = new ArrayList<>();
        for (ColumnDefinition columnDefinition : columnDefinitions) {
            String columnName = columnDefinition.getColumnName();
            if (Objects.equals(columnName, table.getKeyFieldName())) {
                String dataType = columnDefinition.getColDataType().getDataType();
                if (dataType.toLowerCase().contains("integer")) {
                    table.setKeyOrder(Common.TableKeyOrder.Numerical);
                }
            }
            if (fieldsList.contains(columnName)) {
                throw new ConsoleMessageException(
                        "Please provide the field '" + columnName + "' only once.");
            } else {
                fieldsList.add(columnName);
            }
        }
        if (!fieldsList.contains(table.getKeyFieldName())) {
            throw new ConsoleMessageException(
                    "Please provide the field '"
                            + table.getKeyFieldName()
                            + "' in column definition.");
        } else {
            fieldsList.remove(table.getKeyFieldName());
        }
        table.setValueFields(fieldsList);
    }

    public static Table parseAlterTable(String sql)
            throws JSQLParserException, ConsoleMessageException {
        Table table = new Table();
        Statement statement = CCJSqlParserUtil.parse(sql);
        Alter alterState = (Alter) statement;

        // parse table name
        String tableName = alterState.getTable().getName();
        table.setTableName(tableName);

        List<String> newColumns = new ArrayList<>();

        List<AlterExpression> alterExpressions = alterState.getAlterExpressions();
        for (AlterExpression alterExpression : alterExpressions) {
            if (alterExpression.getOperation() != AlterOperation.ADD) {
                throw new ConsoleMessageException("Alter table only support ADD COLUMN now");
            }
            List<AlterExpression.ColumnDataType> colDataTypeList =
                    alterExpression.getColDataTypeList();
            for (AlterExpression.ColumnDataType columnDataType : colDataTypeList) {
                newColumns.add(columnDataType.getColumnName());
            }
        }
        table.setValueFields(newColumns);
        return table;
    }

    public static String parseTableNameFromSql(String sql)
            throws JSQLParserException, ConsoleMessageException {
        Statement statement = CCJSqlParserUtil.parse(sql);

        // parse table name
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(statement);
        if (tableList.size() != 1) {
            throw new ConsoleMessageException("Please provide only one table name.");
        }
        return tableList.get(0);
    }

    public static Entry parseInsert(String sql, Table table, Map<String, List<String>> tableDesc)
            throws JSQLParserException, ConsoleMessageException {
        Statement statement = CCJSqlParserUtil.parse(sql);
        Insert insert = (Insert) statement;
        List<String> valueFields = tableDesc.get(PrecompiledConstant.VALUE_FIELD_NAME);
        String keyField = tableDesc.get(PrecompiledConstant.KEY_FIELD_NAME).get(0);

        String expectedValueField = keyField + "," + StringUtils.join(valueFields, ",");

        int expectedValueNum = valueFields.size() + 1;

        if (insert.getSelect() != null) {
            throw new ConsoleMessageException("The insert select clause is not supported.");
        }
        // parse table name
        String tableName = insert.getTable().getName();
        table.setTableName(tableName);

        // parse columns
        List<Column> columns = insert.getColumns();
        ItemsList itemsList = insert.getItemsList();

        ExpressionList expressionList = (ExpressionList) itemsList;
        List<Expression> expressions = expressionList.getExpressions();

        String[] itemArr = new String[expressions.size()];
        for (int i = 0; i < expressions.size(); i++) {
            itemArr[i] = expressions.get(i).toString().trim();
        }
        LinkedHashMap<String, String> kv = new LinkedHashMap<>();
        String keyValue = "";
        if (columns != null) {
            if (columns.size() != itemArr.length) {
                throw new ConsoleMessageException("Column count doesn't match value count.");
            }
            if (expectedValueNum != columns.size()) {
                throw new ConsoleMessageException(
                        "Column count doesn't match value count, fields size: "
                                + valueFields.size()
                                + ", provided field value size: "
                                + columns.size()
                                + ", expected field list: "
                                + expectedValueField);
            }
            List<String> columnNames = new ArrayList<>();
            for (Column column : columns) {
                String columnName = trimQuotes(column.toString());
                if (columnNames.contains(columnName)) {
                    throw new ConsoleMessageException(
                            "Please provide the field '" + columnName + "' only once.");
                } else {
                    columnNames.add(columnName);
                }
            }
            for (int i = 0; i < columnNames.size(); i++) {
                if (Objects.equals(columnNames.get(i), keyField)) {
                    keyValue = trimQuotes(itemArr[i]);
                    continue;
                }
                kv.put(columnNames.get(i), trimQuotes(itemArr[i]));
            }
        } else {
            if (expectedValueNum != itemArr.length) {
                throw new ConsoleMessageException(
                        "Column count doesn't match value count, fields size: "
                                + valueFields.size()
                                + ", provided field value size: "
                                + itemArr.length
                                + ", expected field list: "
                                + expectedValueField);
            }
            keyValue = trimQuotes(itemArr[0]);
            for (int i = 1; i < itemArr.length; i++) {
                kv.put(valueFields.get(i - 1), trimQuotes(itemArr[i]));
            }
        }
        return new Entry(valueFields, keyValue, kv);
    }

    public static void parseSelect(
            String sql, Table table, List<String> selectColumns, Condition condition)
            throws JSQLParserException, ConsoleMessageException {
        Statement statement;
        statement = CCJSqlParserUtil.parse(sql);
        Select selectStatement = (Select) statement;

        // parse table name
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(selectStatement);
        if (tableList.size() != 1) {
            throw new ConsoleMessageException("Please provide only one table name.");
        }
        table.setTableName(tableList.get(0));

        // parse where clause
        PlainSelect selectBody = (PlainSelect) selectStatement.getSelectBody();
        if (selectBody.getOrderByElements() != null) {
            throw new ConsoleMessageException("The order clause is not supported.");
        }
        if (selectBody.getGroupBy() != null) {
            throw new ConsoleMessageException("The group clause is not supported.");
        }
        if (selectBody.getHaving() != null) {
            throw new ConsoleMessageException("The having clause is not supported.");
        }
        if (selectBody.getJoins() != null) {
            throw new ConsoleMessageException("The join clause is not supported.");
        }
        if (selectBody.getTop() != null) {
            throw new ConsoleMessageException("The top clause is not supported.");
        }
        if (selectBody.getDistinct() != null) {
            throw new ConsoleMessageException("The distinct clause is not supported.");
        }
        // parse select item
        List<SelectItem> selectItems = selectBody.getSelectItems();
        for (SelectItem item : selectItems) {
            if (item instanceof SelectExpressionItem) {
                SelectExpressionItem selectExpressionItem = (SelectExpressionItem) item;
                Expression expression = selectExpressionItem.getExpression();
                if (expression instanceof Function) {
                    Function func = (Function) expression;
                    throw new ConsoleMessageException(
                            "The " + func.getName() + " function is not supported.");
                }
            }
            selectColumns.add(item.toString());
        }
        if (condition instanceof ConditionV320) {
            parseWhereClause(
                    selectBody.getWhere(),
                    table.getKeyFieldName(),
                    table.getValueFields(),
                    selectBody.getLimit(),
                    (ConditionV320) condition);
        } else {
            parseWhereClause(
                    selectBody.getWhere(),
                    table.getKeyFieldName(),
                    selectBody.getLimit(),
                    condition);
        }
    }

    public static void parseUpdate(
            String sql, Table table, Condition condition, UpdateFields updateFields)
            throws JSQLParserException, ConsoleMessageException {
        Statement statement = CCJSqlParserUtil.parse(sql);
        Update update = (Update) statement;

        // parse table name
        List<net.sf.jsqlparser.schema.Table> tables = update.getTables();
        String tableName = tables.get(0).getName();
        table.setTableName(tableName);

        // parse columns
        List<Column> columns = update.getColumns();
        List<Expression> expressions = update.getExpressions();
        int size = expressions.size();
        String[] values = new String[size];
        for (int i = 0; i < size; i++) {
            values[i] = expressions.get(i).toString();
        }
        for (int i = 0; i < columns.size(); i++) {
            updateFields
                    .getFieldNameToValue()
                    .put(trimQuotes(columns.get(i).toString()), trimQuotes(values[i]));
        }

        // set condition
        if (condition instanceof ConditionV320) {
            parseWhereClause(
                    update.getWhere(),
                    table.getKeyFieldName(),
                    table.getValueFields(),
                    update.getLimit(),
                    (ConditionV320) condition);
        } else {
            parseWhereClause(
                    update.getWhere(), table.getKeyFieldName(), update.getLimit(), condition);
        }
    }

    public static void parseRemove(String sql, Table table, Condition condition)
            throws JSQLParserException, ConsoleMessageException {
        Statement statement = CCJSqlParserUtil.parse(sql);
        Delete delete = (Delete) statement;

        // parse table name
        net.sf.jsqlparser.schema.Table sqlTable = delete.getTable();
        table.setTableName(sqlTable.getName());

        // parse where clause
        if (condition instanceof ConditionV320) {
            parseWhereClause(
                    delete.getWhere(),
                    table.getKeyFieldName(),
                    table.getValueFields(),
                    delete.getLimit(),
                    (ConditionV320) condition);
        } else {
            parseWhereClause(
                    delete.getWhere(), table.getKeyFieldName(), delete.getLimit(), condition);
        }
    }

    private static void parseWhereClause(
            Expression where, String keyFieldName, Limit limit, Condition condition)
            throws ConsoleMessageException {
        // parse where clause
        if (where != null) {
            BinaryExpression expr = (BinaryExpression) (where);
            covertExpressionToCondition(expr, keyFieldName, condition);
        }

        // parse limit
        if (limit != null) {
            LongValue offsetLongValue = (LongValue) limit.getOffset();
            LongValue rowCountLongValue = (LongValue) limit.getRowCount();
            long offset = offsetLongValue == null ? 0 : offsetLongValue.getValue();
            long rowCount = rowCountLongValue == null ? 0 : rowCountLongValue.getValue();
            // TODO: add offset and rowCount check
            condition.setLimit((int) offset, (int) rowCount);
        }
    }

    private static void parseWhereClause(
            Expression where,
            String keyField,
            List<String> valueFields,
            Limit limit,
            ConditionV320 condition)
            throws ConsoleMessageException {
        // parse where clause
        if (where != null) {
            BinaryExpression expr = (BinaryExpression) (where);
            covertExpressionToCondition(expr, keyField, valueFields, condition);
        }

        // parse limit
        if (limit != null) {
            LongValue offsetLongValue = (LongValue) limit.getOffset();
            LongValue rowCountLongValue = (LongValue) limit.getRowCount();
            long offset = offsetLongValue == null ? 0 : offsetLongValue.getValue();
            long rowCount = rowCountLongValue == null ? 0 : rowCountLongValue.getValue();
            // TODO: add offset and rowCount check
            condition.setLimit((int) offset, (int) rowCount);
        }
    }

    private static void covertExpressionToCondition(
            Expression expr, String keyField, Condition condition) throws ConsoleMessageException {
        if (expr instanceof BinaryExpression) {
            Set<String> keySet = new HashSet<>();
            Set<String> eqValue = new HashSet<>();
            Set<String> unsupportedConditions = new HashSet<>();
            expr.accept(
                    new ExpressionVisitorAdapter() {
                        @Override
                        protected void visitBinaryExpression(BinaryExpression expr) {
                            if (expr instanceof ComparisonOperator) {
                                String key = trimQuotes(expr.getLeftExpression().toString());
                                keySet.add(key);
                                String operation = expr.getStringExpression();
                                String value = trimQuotes(expr.getRightExpression().toString());
                                switch (operation) {
                                    case "=":
                                        if (key.equals(keyField)) {
                                            eqValue.add(value);
                                            condition.EQ(value);
                                        }
                                        break;
                                    case ">":
                                        condition.GT(value);
                                        break;
                                    case ">=":
                                        condition.GE(value);
                                        break;
                                    case "<":
                                        condition.LT(value);
                                        break;
                                    case "<=":
                                        condition.LE(value);
                                        break;
                                    default:
                                        break;
                                }
                            } else {
                                try {
                                    checkExpression(expr);
                                } catch (ConsoleMessageException e) {
                                    unsupportedConditions.add(e.getMessage());
                                }
                            }
                            super.visitBinaryExpression(expr);
                        }
                    });
            if (keySet.size() != 1 || !keySet.contains(keyField)) {
                throw new ConsoleMessageException(
                        "Wrong condition! Condition is only supported in Key! The keyField is: "
                                + keyField);
            }
            if (!eqValue.isEmpty() && condition.getConditions().size() > 0) {
                throw new ConsoleMessageException(
                        "Wrong condition! There is an equal comparison, no need to do other comparison! The conflicting condition is: "
                                + condition.getConditions());
            }
            if (!unsupportedConditions.isEmpty()) {
                throw new ConsoleMessageException(
                        "Wrong condition! Find unsupported conditions! message: "
                                + unsupportedConditions);
            }
        }
        checkExpression(expr);
    }

    private static void covertExpressionToCondition(
            Expression expr, String keyField, List<String> valueField, ConditionV320 condition)
            throws ConsoleMessageException {
        if (expr instanceof BinaryExpression) {
            Set<String> undefinedKeys = new HashSet<>();
            Set<String> unsupportedConditions = new HashSet<>();
            expr.accept(
                    new ExpressionVisitorAdapter() {
                        @Override
                        protected void visitBinaryExpression(BinaryExpression expr) {
                            String key = trimQuotes(expr.getLeftExpression().toString());
                            String operation = expr.getStringExpression();
                            String value = trimQuotes(expr.getRightExpression().toString());
                            if (!(expr instanceof AndExpression)
                                    && !valueField.contains(key)
                                    && !keyField.equals(key)) {
                                undefinedKeys.add(key);
                            }
                            if (expr instanceof ComparisonOperator) {
                                switch (operation) {
                                    case "=":
                                        condition.EQ(key, value);
                                        break;
                                    case ">":
                                        condition.GT(key, value);
                                        break;
                                    case ">=":
                                        condition.GE(key, value);
                                        break;
                                    case "<":
                                        condition.LT(key, value);
                                        break;
                                    case "<=":
                                        condition.LE(key, value);
                                        break;
                                    case "!=":
                                        condition.NE(key, value);
                                        break;
                                    default:
                                        break;
                                }
                            } else if (expr instanceof LikeExpression) {
                                boolean startFlag = value.startsWith("%");
                                boolean endFlag = value.endsWith("%");
                                int flag = (startFlag ? 1 : 0) + (endFlag ? 2 : 0);
                                if (value.equals("%")) flag = 0;
                                switch (flag) {
                                    case 0:
                                        condition.EQ(key, value);
                                        break;
                                    case 1:
                                        // %value
                                        condition.ENDS_WITH(key, value.substring(1));
                                        break;
                                    case 2:
                                        // value%
                                        condition.STARTS_WITH(
                                                key, value.substring(0, value.length() - 1));
                                        break;
                                    case 3:
                                        // %value%
                                        condition.CONTAINS(
                                                key, value.substring(1, value.length() - 1));
                                        break;
                                    default:
                                        break;
                                }
                            } else {
                                try {
                                    checkExpression(expr);
                                } catch (ConsoleMessageException e) {
                                    unsupportedConditions.add(e.getMessage());
                                }
                            }
                            super.visitBinaryExpression(expr);
                        }
                    });
            if (!undefinedKeys.isEmpty()) {
                throw new ConsoleMessageException(
                        "Wrong condition! There is an undefined field comparison! The condition is: "
                                + condition.getConditions()
                                + ", undefinedKeys: "
                                + undefinedKeys);
            }
            if (!unsupportedConditions.isEmpty()) {
                throw new ConsoleMessageException(
                        "Wrong condition! Find unsupported conditions! message: "
                                + unsupportedConditions);
            }
        } else {
            checkExpression(expr);
        }
    }

    private static void checkExpression(Expression expression) throws ConsoleMessageException {
        if (expression instanceof OrExpression) {
            throw new ConsoleMessageException("The OrExpression is not supported.");
        }
        if (expression instanceof NotExpression) {
            throw new ConsoleMessageException("The NotExpression is not supported.");
        }
        if (expression instanceof InExpression) {
            throw new ConsoleMessageException("The InExpression is not supported.");
        }
        if (expression instanceof LikeExpression) {
            logger.debug("The LikeExpression is not supported.");
            throw new ConsoleMessageException("The LikeExpression is not supported.");
        }
        if (expression instanceof SubSelect) {
            throw new ConsoleMessageException("The SubSelect is not supported.");
        }
        if (expression instanceof IsNullExpression) {
            throw new ConsoleMessageException("The IsNullExpression is not supported.");
        }
    }

    public static String trimQuotes(String str) {
        char[] value = str.toCharArray();
        int len = value.length;
        int st = 1;
        char[] val = value; /* avoid getfield opcode */

        while ((st < len) && (val[st] == '"' || val[st] == '\'')) {
            st++;
        }
        while ((st < len) && (val[len - 1] == '"' || val[len - 1] == '\'')) {
            len--;
        }
        String string = ((st > 1) || (len < value.length)) ? str.substring(st, len) : str;
        return string;
    }

    public static void invalidSymbol(String sql) throws ConsoleMessageException {
        if (sql.contains("；")) {
            throw new ConsoleMessageException("SyntaxError: Unexpected Chinese semicolon.");
        } else if (sql.contains("“")
                || sql.contains("”")
                || sql.contains("‘")
                || sql.contains("’")) {
            throw new ConsoleMessageException("SyntaxError: Unexpected Chinese quotes.");
        } else if (sql.contains("，")) {
            throw new ConsoleMessageException("SyntaxError: Unexpected Chinese comma.");
        }
    }
}
