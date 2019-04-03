package console.common;

import java.util.List;

import org.fisco.bcos.web3j.precompile.crud.Condition;
import org.fisco.bcos.web3j.precompile.crud.Entry;
import org.fisco.bcos.web3j.precompile.crud.Table;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.operators.relational.ComparisonOperator;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
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
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.TablesNamesFinder;

public class CRUDParseUtils {
	
	public static void  parseCreateTable(String sql, Table table) throws JSQLParserException {
			Statement statement = CCJSqlParserUtil.parse(sql);
			CreateTable createTable = (CreateTable)statement;
			
			// parse table name
			String tableName = createTable.getTable().getName();
			table.setTableName(tableName);
			
			// parse key
			List<Index> indexes = createTable.getIndexes();
			for (Index index : indexes) {
				table.setKey(index.getColumnsNames().get(0));
			}
			
			// parese value fields
			List<ColumnDefinition> columnDefinitions = createTable.getColumnDefinitions();
			StringBuffer fields = new StringBuffer();
			for (ColumnDefinition columnDefinition : columnDefinitions) {
				if(!columnDefinition.getColumnName().equals(table.getKey()))
				{
					fields.append(columnDefinition.getColumnName() + ",");
				}
			}
			table.setValueFields(fields.toString());
	}
	
	public static void parseInsert(String sql, Table table, Entry entry) throws JSQLParserException {
			Statement statement = CCJSqlParserUtil.parse(sql);
			Insert insert = (Insert)statement;
			
			// parse table name
			String tableName = insert.getTable().getName();
			table.setTableName(tableName);
			
			// parse columns
			List<Column> columns = insert.getColumns();
			ItemsList itemsList = insert.getItemsList();
			String items = itemsList.toString();
			String[] split = items.substring(1, items.length() -1 ).split(",");
			for (int i = 0; i < columns.size(); i++) {
				entry.put(columns.get(i).toString(), split[i].trim());
			}
	}
	
	public static void parseSelect(String sql, Table table, Condition condition, List<String> selectColumns) throws JSQLParserException{
		  Statement statement;
			statement = CCJSqlParserUtil.parse(sql);
			Select selectStatement = (Select) statement;
			
			// parse table name
			TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
			List<String> tableList = tablesNamesFinder.getTableList(selectStatement);
			table.setTableName(tableList.get(0));
			
			// parse where clause
			SelectBody selectBody = selectStatement.getSelectBody();
			Expression expr = ((PlainSelect) selectBody).getWhere();
			if(expr instanceof BinaryExpression){
				condition = getWhereClause((BinaryExpression)(expr), condition);
			}
			Limit limit = ((PlainSelect) selectBody).getLimit();
			if(limit != null)
			{
				parseLimit(condition, limit);
			}
			
			// parse select item
			List<SelectItem> selectItems = ((PlainSelect) selectBody).getSelectItems();
			for (SelectItem item : selectItems) {
				selectColumns.add(item.toString());
			}
	}
	
	public static void parseUpdate(String sql, Table table, Entry entry, Condition condition) throws JSQLParserException {
			Statement statement = CCJSqlParserUtil.parse(sql);
			Update update = (Update)statement;
			
			// parse table name
			List<net.sf.jsqlparser.schema.Table> tables = update.getTables();
			String tableName = tables.get(0).getName();
			table.setTableName(tableName);
			
			// parse cloumns
			List<Column> columns = update.getColumns();
			List<Expression> expressions = update.getExpressions();
			String expr = expressions.toString();
			String[] values = expr.substring(1, expr.length() -1 ).split(",");
			for (int i = 0; i < columns.size(); i++) {
				entry.put(columns.get(i).toString(), values[i].trim());
			}
			
			// parse where clause
		  Expression where = update.getWhere();
			getWhereClause((BinaryExpression)(where), condition);
			Limit limit = update.getLimit();
			parseLimit(condition, limit);
	}
	
	public static void parseRemove(String sql, Table table, Condition condition) throws JSQLParserException {
			Statement statement = CCJSqlParserUtil.parse(sql);
			Delete delete = (Delete)statement;
			
			// parse table name
			net.sf.jsqlparser.schema.Table sqlTable = delete.getTable();
			table.setTableName(sqlTable.getName());
			
			// parse where clause
			Expression where = delete.getWhere();
			getWhereClause((BinaryExpression)(where), condition);
			Limit limit = delete.getLimit();
			parseLimit(condition, limit);
	}

	private static void parseLimit(Condition condition, Limit limit) {
		if(limit != null)
		{
			Expression offset = limit.getOffset();
			Expression count = limit.getRowCount();
			if (offset != null) {
				condition.Limit(Integer.parseInt(offset.toString()), Integer.parseInt(count.toString()));
			}
			else {
				condition.Limit(Integer.parseInt(count.toString()));
			}
		}
	}
	
  private static Condition getWhereClause(Expression expr, Condition condition) {

    expr.accept(new ExpressionVisitorAdapter() {

      @Override
      protected void visitBinaryExpression(BinaryExpression expr) {
          if (expr instanceof ComparisonOperator) {
          		String key = expr.getLeftExpression().toString();
	          	String operation = expr.getStringExpression();
							String value = expr.getRightExpression().toString();
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
  
}
