package console.common;

import java.util.List;

import org.fisco.bcos.web3j.precompile.crud.Condition;
import org.fisco.bcos.web3j.precompile.crud.Entry;
import org.fisco.bcos.web3j.precompile.crud.Table;

import console.exception.ConsoleMessageException;
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

public class CRUDParseUtils {
	
	public static void  parseCreateTable(String sql, Table table) throws JSQLParserException, ConsoleMessageException {
			Statement statement = CCJSqlParserUtil.parse(sql);
			CreateTable createTable = (CreateTable)statement;
			
			// parse table name
			String tableName = createTable.getTable().getName();
			table.setTableName(tableName);
			
			// parse key
			List<Index> indexes = createTable.getIndexes();
			if(indexes != null)
			{
				for (Index index : indexes) {
					table.setKey(index.getColumnsNames().get(0));
					break;
				}
			}
			else 
			{
				throw new ConsoleMessageException("Please provide a key for the table.");
			}
			// parese value fields
			List<ColumnDefinition> columnDefinitions = createTable.getColumnDefinitions();
			StringBuffer fields = new StringBuffer();
			for (int i = 0; i < columnDefinitions.size(); i++) 
			{
				if(!columnDefinitions.get(i).getColumnName().equals(table.getKey()))
				{
					fields.append(columnDefinitions.get(i).getColumnName());
					if(i != columnDefinitions.size() - 1)
					{
						fields.append(",");
					}
				}
			}
			table.setValueFields(fields.toString());
	}
	
	public static boolean parseInsert(String sql, Table table, Entry entry) throws JSQLParserException {
			Statement statement = CCJSqlParserUtil.parse(sql);
			Insert insert = (Insert)statement;
			
			// parse table name
			String tableName = insert.getTable().getName();
			table.setTableName(tableName);
			
			// parse columns
			List<Column> columns = insert.getColumns();
			ItemsList itemsList = insert.getItemsList();
			String items = itemsList.toString();
			String[] itemArr = items.substring(1, items.length() -1 ).split(",");
			if(columns != null)
			{
				for (int i = 0; i < itemArr.length; i++) {
					entry.put(columns.get(i).toString(), itemArr[i].trim());
				}
				return false;
			}
			else 
			{
				for (int i = 0; i < itemArr.length; i++) {
					entry.put(i+"", itemArr[i].trim());
				}
				return true;
			}
	}
	
	public static void parseSelect(String sql, Table table, Condition condition, List<String> selectColumns) throws JSQLParserException, ConsoleMessageException{
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
			PlainSelect selectBody = (PlainSelect)selectStatement.getSelectBody();
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
			if(limit != null)
			{
				parseLimit(condition, limit);
			}
			
			// parse select item
			List<SelectItem> selectItems = selectBody.getSelectItems();
			for (SelectItem item : selectItems) {
				if (item instanceof SelectExpressionItem) {
					SelectExpressionItem selectExpressionItem = (SelectExpressionItem)item;
					Expression expression = selectExpressionItem.getExpression();
					if(expression instanceof Function)
					{
						Function func = (Function)expression;
						throw new ConsoleMessageException("The " + func.getName() + " function is not supported.");
					}
				}
				selectColumns.add(item.toString());
			}
	}

	private static Condition handleExpression(Condition condition, Expression expr) throws ConsoleMessageException {
		if(expr instanceof BinaryExpression){
			condition = getWhereClause((BinaryExpression)(expr), condition);
		}
		if(expr instanceof OrExpression)
		{
			throw new ConsoleMessageException("The OrExpression is not supported.");
		}
		if(expr instanceof NotExpression)
		{
			throw new ConsoleMessageException("The NotExpression is not supported.");
		}
		if(expr instanceof InExpression)
		{
			throw new ConsoleMessageException("The InExpression is not supported.");
		}
		if(expr instanceof LikeExpression)
		{
			throw new ConsoleMessageException("The LikeExpression is not supported.");
		}
		if(expr instanceof SubSelect)
		{
			throw new ConsoleMessageException("The SubSelect is not supported.");
		}
		if(expr instanceof IsNullExpression)
		{
			throw new ConsoleMessageException("The IsNullExpression is not supported.");
		}
		return condition;
	}
	
	public static void parseUpdate(String sql, Table table, Entry entry, Condition condition) throws JSQLParserException, ConsoleMessageException {
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
		  if(where != null)
		  {
		  	BinaryExpression expr2 = (BinaryExpression)(where);
		  	handleExpression(condition, expr2);
		  	getWhereClause(expr2, condition);
		  }
			Limit limit = update.getLimit();
			parseLimit(condition, limit);
	}
	
	public static void parseRemove(String sql, Table table, Condition condition) throws JSQLParserException, ConsoleMessageException {
			Statement statement = CCJSqlParserUtil.parse(sql);
			Delete delete = (Delete)statement;
			
			// parse table name
			net.sf.jsqlparser.schema.Table sqlTable = delete.getTable();
			table.setTableName(sqlTable.getName());
			
			// parse where clause
			Expression where = delete.getWhere();
			if(where != null)
			{
				BinaryExpression expr = (BinaryExpression)(where);
				handleExpression(condition, expr);
				getWhereClause(expr, condition);
			}
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
