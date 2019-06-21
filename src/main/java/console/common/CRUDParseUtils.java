package console.common;

import console.exception.ConsoleMessageException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.NotExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.ComparisonOperator;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
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
import org.fisco.bcos.web3j.precompile.crud.Condition;
import org.fisco.bcos.web3j.precompile.crud.Entry;
import org.fisco.bcos.web3j.precompile.crud.EnumOP;
import org.fisco.bcos.web3j.precompile.crud.Table;

public class CRUDParseUtils {

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
                table.setKey(index.getColumnsNames().get(0));
            } else {
                throw new ConsoleMessageException(
                        "Please provide only one primary key for the table.");
            }
        }
        List<ColumnDefinition> columnDefinitions = createTable.getColumnDefinitions();
        // parse key from ColumnDefinition
        for (int i = 0; i < columnDefinitions.size(); i++) {
            List<String> columnSpecStrings = columnDefinitions.get(i).getColumnSpecStrings();
            if (columnSpecStrings == null) {
                continue;
            } else {
                if (columnSpecStrings.size() == 2
                        && "primary".equals(columnSpecStrings.get(0))
                        && "key".equals(columnSpecStrings.get(1))) {
                    String key = columnDefinitions.get(i).getColumnName();
                    if (keyFlag) {
                        if (!table.getKey().equals(key)) {
                            throw new ConsoleMessageException(
                                    "Please provide only one primary key for the table.");
                        }
                    } else {
                        keyFlag = true;
                        table.setKey(key);
                    }
                    break;
                }
            }
        }
        if (!keyFlag) {
            throw new ConsoleMessageException("Please provide a primary key for the table.");
        }
        // parse value field
        List<String> fieldsList = new ArrayList<>();
        for (int i = 0; i < columnDefinitions.size(); i++) {
            String columnName = columnDefinitions.get(i).getColumnName();
            if (fieldsList.contains(columnName)) {
                throw new ConsoleMessageException(
                        "Please provide the field '" + columnName + "' only once.");
            } else {
                fieldsList.add(columnName);
            }
        }
        if (!fieldsList.contains(table.getKey())) {
            throw new ConsoleMessageException(
                    "Please provide the field '" + table.getKey() + "' in column definition.");
        } else {
            fieldsList.remove(table.getKey());
        }
        StringBuffer fields = new StringBuffer();
        for (int i = 0; i < fieldsList.size(); i++) {
            fields.append(fieldsList.get(i));
            if (i != fieldsList.size() - 1) {
                fields.append(",");
            }
        }
        table.setValueFields(fields.toString());
    }

    public static boolean parseInsert(String sql, Table table, Entry entry)
            throws JSQLParserException, ConsoleMessageException {
        Statement statement = CCJSqlParserUtil.parse(sql);
        Insert insert = (Insert) statement;

        if (insert.getSelect() != null) {
            throw new ConsoleMessageException("The insert select clause is not supported.");
        }
        // parse table name
        String tableName = insert.getTable().getName();
        table.setTableName(tableName);

        // parse columns
        List<Column> columns = insert.getColumns();
        ItemsList itemsList = insert.getItemsList();
        String items = itemsList.toString();
        String[] rawItem = items.substring(1, items.length() - 1).split(",");
        String[] itemArr = new String[rawItem.length];
        for (int i = 0; i < rawItem.length; i++) {
            itemArr[i] = rawItem[i].trim();
        }
        if (columns != null) {
            if (columns.size() != itemArr.length) {
                throw new ConsoleMessageException("Column count doesn't match value count.");
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
                entry.put(columnNames.get(i), trimQuotes(itemArr[i]));
            }
            return false;
        } else {
            for (int i = 0; i < itemArr.length; i++) {
                entry.put(i + "", trimQuotes(itemArr[i]));
            }
            return true;
        }
    }

    public static void parseSelect(
            String sql, Table table, Condition condition, List<String> selectColumns)
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
        Expression expr = selectBody.getWhere();
        condition = handleExpression(condition, expr);

        Limit limit = selectBody.getLimit();
        if (limit != null) {
            parseLimit(condition, limit);
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
    }

    private static Condition handleExpression(Condition condition, Expression expr)
            throws ConsoleMessageException {
        if (expr instanceof BinaryExpression) {
            condition = getWhereClause((BinaryExpression) (expr), condition);
        }
        if (expr instanceof OrExpression) {
            throw new ConsoleMessageException("The OrExpression is not supported.");
        }
        if (expr instanceof NotExpression) {
            throw new ConsoleMessageException("The NotExpression is not supported.");
        }
        if (expr instanceof InExpression) {
            throw new ConsoleMessageException("The InExpression is not supported.");
        }
        if (expr instanceof LikeExpression) {
            throw new ConsoleMessageException("The LikeExpression is not supported.");
        }
        if (expr instanceof SubSelect) {
            throw new ConsoleMessageException("The SubSelect is not supported.");
        }
        if (expr instanceof IsNullExpression) {
            throw new ConsoleMessageException("The IsNullExpression is not supported.");
        }
        Map<String, Map<EnumOP, String>> conditions = condition.getConditions();
        Set<String> keys = conditions.keySet();
        for (String key : keys) {
            Map<EnumOP, String> value = conditions.get(key);
            EnumOP operation = value.keySet().iterator().next();
            String itemValue = value.values().iterator().next();
            String newValue = trimQuotes(itemValue);
            value.put(operation, newValue);
            conditions.put(key, value);
        }
        condition.setConditions(conditions);
        return condition;
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

    public static void parseUpdate(String sql, Table table, Entry entry, Condition condition)
            throws JSQLParserException, ConsoleMessageException {
        Statement statement = CCJSqlParserUtil.parse(sql);
        Update update = (Update) statement;

        // parse table name
        List<net.sf.jsqlparser.schema.Table> tables = update.getTables();
        String tableName = tables.get(0).getName();
        table.setTableName(tableName);

        // parse cloumns
        List<Column> columns = update.getColumns();
        List<Expression> expressions = update.getExpressions();
        int size = expressions.size();
        String[] values = new String[size];
        for (int i = 0; i < size; i++) {
            values[i] = expressions.get(i).toString();
        }
        for (int i = 0; i < columns.size(); i++) {
            entry.put(trimQuotes(columns.get(i).toString()), trimQuotes(values[i]));
        }

        // parse where clause
        Expression where = update.getWhere();
        if (where != null) {
            BinaryExpression expr2 = (BinaryExpression) (where);
            handleExpression(condition, expr2);
        }
        Limit limit = update.getLimit();
        parseLimit(condition, limit);
    }

    public static void parseRemove(String sql, Table table, Condition condition)
            throws JSQLParserException, ConsoleMessageException {
        Statement statement = CCJSqlParserUtil.parse(sql);
        Delete delete = (Delete) statement;

        // parse table name
        net.sf.jsqlparser.schema.Table sqlTable = delete.getTable();
        table.setTableName(sqlTable.getName());

        // parse where clause
        Expression where = delete.getWhere();
        if (where != null) {
            BinaryExpression expr = (BinaryExpression) (where);
            handleExpression(condition, expr);
        }
        Limit limit = delete.getLimit();
        parseLimit(condition, limit);
    }

    private static void parseLimit(Condition condition, Limit limit)
            throws ConsoleMessageException {
        if (limit != null) {
            Expression offset = limit.getOffset();
            Expression count = limit.getRowCount();
            try {
                if (offset != null) {
                    condition.Limit(
                            Integer.parseInt(offset.toString()),
                            Integer.parseInt(count.toString()));
                } else {
                    condition.Limit(Integer.parseInt(count.toString()));
                }
            } catch (NumberFormatException e) {
                throw new ConsoleMessageException(
                        "Please provide limit parameters by non-negative integer mode, "
                                + Common.NonNegativeIntegerRange
                                + ".");
            }
        }
    }

    private static Condition getWhereClause(Expression expr, Condition condition) {

        expr.accept(
                new ExpressionVisitorAdapter() {

                    @Override
                    protected void visitBinaryExpression(BinaryExpression expr) {
                        if (expr instanceof ComparisonOperator) {
                            String key = trimQuotes(expr.getLeftExpression().toString());
                            String operation = expr.getStringExpression();
                            String value = trimQuotes(expr.getRightExpression().toString());
                            switch (operation) {
                                case "=":
                                    condition.EQ(key, value);
                                    break;
                                case "!=":
                                    condition.NE(key, value);
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
                                default:
                                    break;
                            }
                        }
                        super.visitBinaryExpression(expr);
                    }
                });
        return condition;
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

    public static void checkTableParams(Table table) throws ConsoleMessageException {
        if (table.getTableName().length() > Common.SYS_TABLE_KEY_MAX_LENGTH) {
            throw new ConsoleMessageException(
                    "The table name length is greater than "
                            + Common.SYS_TABLE_KEY_MAX_LENGTH
                            + ".");
        }
        if (table.getKey().length() > Common.SYS_TABLE_KEY_FIELD_NAME_MAX_LENGTH) {
            throw new ConsoleMessageException(
                    "The table primary key name length is greater than "
                            + Common.SYS_TABLE_KEY_FIELD_NAME_MAX_LENGTH
                            + ".");
        }
        String[] valueFields = table.getValueFields().split(",");
        for (String valueField : valueFields) {
            if (valueField.length() > Common.USER_TABLE_FIELD_NAME_MAX_LENGTH) {
                throw new ConsoleMessageException(
                        "The table field name length is greater than "
                                + Common.USER_TABLE_FIELD_NAME_MAX_LENGTH
                                + ".");
            }
        }
        if (table.getValueFields().length() > Common.SYS_TABLE_VALUE_FIELD_MAX_LENGTH) {
            throw new ConsoleMessageException(
                    "The table total field name length is greater than "
                            + Common.SYS_TABLE_VALUE_FIELD_MAX_LENGTH
                            + ".");
        }
    }

    public static void checkUserTableParam(Entry entry, Table descTable)
            throws ConsoleMessageException {
        Map<String, String> fieldsMap = entry.getFields();
        Set<String> keys = fieldsMap.keySet();
        for (String key : keys) {
            if (key.equals(descTable.getKey())) {
                if (fieldsMap.get(key).length() > Common.USER_TABLE_KEY_VALUE_MAX_LENGTH) {
                    throw new ConsoleMessageException(
                            "The table primary key value length is greater than "
                                    + Common.USER_TABLE_KEY_VALUE_MAX_LENGTH
                                    + ".");
                }
            } else {
                if (fieldsMap.get(key).length() > Common.USER_TABLE_FIELD_VALUE_MAX_LENGTH) {
                    throw new ConsoleMessageException(
                            "The table field '" + key + "' value length is greater than 16M - 1.");
                }
            }
        }
    }
}
