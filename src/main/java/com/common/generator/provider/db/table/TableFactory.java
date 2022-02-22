package com.common.generator.provider.db.table;


import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.common.generator.GeneratorProperties;
import com.common.generator.provider.db.DataSourceProvider;
import com.common.generator.provider.db.table.model.Column;
import com.common.generator.provider.db.table.model.Table;
import com.common.generator.util.BeanHelper;
import com.common.generator.util.FileHelper;
import com.common.generator.util.GLogger;
import com.common.generator.util.StringHelper;
import com.common.generator.util.XMLHelper;
import com.common.generator.util.XMLHelper.NodeData;
/**
 * 
 * 根据数据库表的元数据(metadata)创建Table对象
 * 
 * <pre>
 * getTable(sqlName) : 根据数据库表名,得到table对象
 * getAllTable() : 搜索数据库的所有表,并得到table对象列表
 * </pre>
 * @modify diyvan
 */
public class TableFactory {
	
	private DbHelper dbHelper = new DbHelper();
	private static TableFactory instance = null;
	//加入数据库类型字段
	//modified by liuyatao 2015年1月21日
	public static final String dbtype = GeneratorProperties.getProperty("jdbc.dbtype");
	private TableFactory() {
	}
	
	public synchronized static TableFactory getInstance() {
		if(instance == null) instance = new TableFactory();
		return instance;
	}
	
	public String getCatalog() {
		return GeneratorProperties.getNullIfBlank("jdbc.catalog");
	}

	public String getSchema() {
		return GeneratorProperties.getNullIfBlank("jdbc.schema").toUpperCase();
	}

	private Connection getConnection() {
		return DataSourceProvider.getConnection();
	}

	public List getAllTables() {
		try {
			Connection conn = getConnection();
			return getAllTables(conn);
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public Table getTable(String tableName) {
		return getTable(getSchema(),tableName);
	}

	/**
	 * 构建一个用于生成代码的Table对象.对应数据库的table
	 * @param schema
	 * @param tableName
	 * @return
	 */
	private Table getTable(String schema,String tableName) {
		return getTable(getCatalog(),schema,tableName);
	}
	
	private Table getTable(String catalog,String schema,String tableName) {
		Table t = null;
		//表名大小写处理
		try {
			//根据catalog,schema,tablename 参数构建一个用于生成代码的Table对象.对应数据库的table
			t = _getTable(catalog,schema,tableName.toUpperCase());
			if(t == null && !tableName.equals(tableName.toLowerCase())) {
				t = _getTable(catalog,schema,tableName.toLowerCase());
			}
			if(t == null) {
				t = _getTable(catalog,schema,tableName);
			}
			if(t!=null){
				t.setSqlName(tableName);
			}
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
		if(t == null) {
			throw new NotFoundTableException("not found table with give name:"+tableName+ (dbHelper.isOracleDataBase() ? " \n databaseStructureInfo:"+getDatabaseStructureInfo() : ""));
		}
		return t;
	}
	
	public static class NotFoundTableException extends RuntimeException {
		private static final long serialVersionUID = 5976869128012158628L;
		public NotFoundTableException(String message) {
			super(message);
		}
	}

	/**
	 * 根据catalog,schema,tablename 参数构建一个用于生成代码的Table对象.对应数据库的table
	 * @param catalog
	 * @param schema
	 * @param tableName
	 * @return
	 * @throws SQLException
	 * @author gwhuang
	 */
	private Table _getTable(String catalog,String schema,String tableName) throws SQLException {
	    if(tableName== null || tableName.trim().length() == 0) 
	         throw new IllegalArgumentException("tableName must be not empty");
	    catalog = StringHelper.defaultIfEmpty(catalog, null);
	    schema = StringHelper.defaultIfEmpty(schema, null);
	    
	    //建立数据库连接
		ResultSet rs = null;
		Connection conn  = null;
		try{
			conn = getConnection();
			//DatabaseMetaData类中提供了许多方法用于获得数据源的各种信息，通过这些方法可以非常详细的了解数据库的信息
			DatabaseMetaData dbMetaData = conn.getMetaData();
			//获取表的信息
			// 结果集(ResultSet)是数据中查询结果返回的一种对象，可以说结果集是一个存储查询结果的对象，但是结果集并不仅仅具有存储的功能，他同时还具有操纵数据的功能，可能完成对数据的更新等。
			rs = dbMetaData.getTables(catalog, schema, tableName, null);
			while(rs.next()) {
				//数据库中表对应到生成代码的Table对象
				Table table = createTable(conn, rs);
				return table;
			}
			
		}catch(Exception e){
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		finally{
			if(rs!=null)
				rs.close();
			if(rs!=null)
				conn.close();
		}
		return null;
	}

	/**
	 * 数据库中表对应到生成代码的Table对象
	 * @param conn
	 * @param rs
	 * @return
	 * @throws SQLException
	 * @author gwhuang
	 */
	private Table createTable(Connection conn, ResultSet rs){
		String realTableName = null;
		try {
			//获取关于 ResultSet 表对象中列的类型和属性信息的对象。
			//ResultSetMetaData对象可以查找ResultSet中列的类型和特征,利用ResultSet的getMetaData的方法可以获得ResultSetMetaData对象 
			ResultSetMetaData rsMetaData = rs.getMetaData();
			
			String schemaName = rs.getString("TABLE_SCHEM") == null ? "" : rs.getString("TABLE_SCHEM");
			realTableName = rs.getString("TABLE_NAME");
			String tableType = rs.getString("TABLE_TYPE");
			String remarks = rs.getString("REMARKS");
			//to do ...
			/*
			if("informix".equals(dbtype)){
				schemaName = rs.getString("owner") == null ? "" : rs.getString("owner");
				realTableName = rs.getString("tabname");
				tableType = rs.getString("tabtype");
				remarks = rs.getString("REMARKS");
			}
			*/
			if(remarks == null && dbHelper.isOracleDataBase()) {
				remarks = getOracleTableComments(realTableName);
			}
			if(remarks == null && dbHelper.isMysqlDataBase()) {
				remarks = getMysqlTableComments(realTableName);
			}
			/* to do ...
			if(remarks == null && "informix".equals(dbtype)) {
				remarks = getInformixTableComments(realTableName);
			}
			*/
			Table table = new Table();
			//数据库中表的表名称,其它属性很多都是根据此属性派生
			table.setSqlName(realTableName);
			//数据库中表的表备注
			table.setRemarks(remarks);
			//表是同义词
			if ("SYNONYM".equals(tableType) && dbHelper.isOracleDataBase()) {
			    table.setOwnerSynonymName(getSynonymOwner(realTableName));
			}
			
			//把表中列信息写入Column对象中，并把Column对象放入Table对象的LinkedHashSet<Column>集合中
			retriveTableColumns(table);
			//获取引用给定表的主键列（表导入的外键）的外键列的描述。
			table.initExportedKeys(conn.getMetaData(),getOracleTableOwner(realTableName));
			//获取由给定表的外键列（表导入的主键）引用的主键列的描述
			table.initImportedKeys(conn.getMetaData(),getOracleTableOwner(realTableName));
			//用表自定义配置信息中的表信息重写Table对象
			BeanHelper.copyProperties(table, TableOverrideValuesProvider.getTableOverrideValues(table.getSqlName()));
			return table;
		}catch(SQLException e) {
			throw new RuntimeException("create table object error,tableName:"+realTableName,e);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private List getAllTables(Connection conn) throws SQLException {
		DatabaseMetaData dbMetaData = conn.getMetaData();
		ResultSet rs = dbMetaData.getTables(getCatalog(), getSchema(), null, null);
		List tables = new ArrayList();
		while(rs.next()) {
			tables.add(createTable(conn, rs));
		}
		return tables;
	}

	private String getSynonymOwner(String synonymName)  {
	      PreparedStatement ps = null;
	      ResultSet rs = null;
	      String ret = null;
	      try {
	         ps = getConnection().prepareStatement("select table_owner from sys.all_synonyms where table_name=? and owner=?");
	         ps.setString(1, synonymName);
	         ps.setString(2, getSchema());
	         rs = ps.executeQuery();
	         if (rs.next()) {
	            ret = rs.getString(1);
	         }
	         else {
	            String databaseStructure = getDatabaseStructureInfo();
	            throw new RuntimeException("Wow! Synonym " + synonymName + " not found. How can it happen? " + databaseStructure);
	         }
	      } catch (SQLException e) {
	         String databaseStructure = getDatabaseStructureInfo();
	         GLogger.error(e.getMessage(), e);
	         throw new RuntimeException("Exception in getting synonym owner " + databaseStructure);
	      } finally {
	         dbHelper.close(rs,ps);
	      }
	      return ret;
	   }
   
   private String getDatabaseStructureInfo() {
	      ResultSet schemaRs = null;
	      ResultSet catalogRs = null;
	      String nl = System.getProperty("line.separator");
	      StringBuffer sb = new StringBuffer(nl);
	      // Let's give the user some feedback. The exception
	      // is probably related to incorrect schema configuration.
	      sb.append("Configured schema:").append(getSchema()).append(nl);
	      sb.append("Configured catalog:").append(getCatalog()).append(nl);

	      try {
	         schemaRs = getMetaData().getSchemas();
	         sb.append("Available schemas:").append(nl);
	         while (schemaRs.next()) {
	            sb.append("  ").append(schemaRs.getString("TABLE_SCHEM")).append(nl);
	         }
	      } catch (SQLException e2) {
	         GLogger.warn("Couldn't get schemas", e2);
	         sb.append("  ?? Couldn't get schemas ??").append(nl);
	      } finally {
	         dbHelper.close(schemaRs,null);
	      }

	      try {
	         catalogRs = getMetaData().getCatalogs();
	         sb.append("Available catalogs:").append(nl);
	         while (catalogRs.next()) {
	            sb.append("  ").append(catalogRs.getString("TABLE_CAT")).append(nl);
	         }
	      } catch (SQLException e2) {
	         GLogger.warn("Couldn't get catalogs", e2);
	         sb.append("  ?? Couldn't get catalogs ??").append(nl);
	      } finally {
	         dbHelper.close(catalogRs,null);
	      }
	      return sb.toString();
    }
	   
	private DatabaseMetaData getMetaData() throws SQLException {
		return getConnection().getMetaData();
	}
	
	/**
	 * 获取表的所有字段
	 * @param table
	 * @throws SQLException
	 */
	private void retriveTableColumns(Table table) throws SQLException {
	      GLogger.trace("-------setColumns(" + table.getSqlName() + ")");
	      
	      //得到表的主键
	      List primaryKeys = getTablePrimaryKeys(table);
	      table.setPrimaryKeyColumns(primaryKeys);
	      
	      // 所有索引的List集合
	      List indices = new LinkedList();
	      // 唯一索引的hashmap集合(columnName,indexName)
	      Map uniqueIndices = new HashMap();
	      // 唯一索引的hashmap集合(indexName,ArrayList)其中ArrayList中是columnName
	      Map uniqueColumns = new HashMap();
	      ResultSet indexRs = null;

	      try {

	         if (table.getOwnerSynonymName() != null) {
	            indexRs = getMetaData().getIndexInfo(getCatalog(), table.getOwnerSynonymName(), table.getSqlName(), false, true);
	         }
	         else {
	        	 //获取给定表的索引和统计信息的描述
	            indexRs = getMetaData().getIndexInfo(getCatalog(), getSchema(), table.getSqlName(), false, true);
	         }
	         while (indexRs.next()) {
	            String columnName = indexRs.getString("COLUMN_NAME");
	            if (columnName != null) {
	               GLogger.trace("index:" + columnName);
	               indices.add(columnName);
	            }

	            // now look for unique columns
	            String indexName = indexRs.getString("INDEX_NAME");
	            boolean nonUnique = indexRs.getBoolean("NON_UNIQUE");

	            if (!nonUnique && columnName != null && indexName != null) {
	            	//唯一索引并且存在
	               List l = (List)uniqueColumns.get(indexName);
	               if (l == null) {
	                  l = new ArrayList();
	                  uniqueColumns.put(indexName, l);
	               }
	               l.add(columnName);
	               uniqueIndices.put(columnName, indexName);
	               GLogger.trace("unique:" + columnName + " (" + indexName + ")");
	            }
	         }
	      } catch (Throwable t) {
	         // Bug #604761 Oracle getIndexInfo() needs major grants
	         // http://sourceforge.net/tracker/index.php?func=detail&aid=604761&group_id=36044&atid=415990
	      } finally {
	         dbHelper.close(indexRs,null);
	      }
	      
	      //把表字段信息及自定义表配置信息中的字段信息，映射到Column对象并把Column对象存入List中
	      List columns = null;
		try {
			columns = getTableColumns(table, primaryKeys, indices, uniqueIndices, uniqueColumns);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	      for (Iterator i = columns.iterator(); i.hasNext(); ) {
	         Column column = (Column)i.next();
	         //Column对象加入到Table对象的LinkedHashSet 集合中
	         table.addColumn(column);
	      }

	      // In case none of the columns were primary keys, issue a warning.
	      if (primaryKeys.size() == 0) {
	         GLogger.warn("WARNING: The JDBC driver didn't report any primary key columns in " + table.getSqlName());
	      }
	}

	private List getTableColumns(Table table, List primaryKeys, List indices, Map uniqueIndices, Map uniqueColumns) throws Exception {
		// get the columns
	      List columns = new LinkedList();
	      //得到指定表的所有列信息
	      ResultSet columnRs = getColumnsResultSet(table);
	      String remark=null;

	      while (columnRs.next()) {
	         int sqlType = columnRs.getInt("DATA_TYPE");
	         String sqlTypeName = columnRs.getString("TYPE_NAME");
	         String columnName = columnRs.getString("COLUMN_NAME");
	         String columnDefaultValue = columnRs.getString("COLUMN_DEF");
	         String humpColumnName = columnName;
	         
	         String remarks = columnRs.getString("REMARKS");
	         if(remarks == null && dbHelper.isOracleDataBase()) {
	        	 remarks = getOracleColumnComments(table.getSqlName(), columnName);
	         }
	         /**
	          * add by mark 2015年3月14日
	          */
	         //若为mysql数据库
	         if(remarks == null && dbHelper.isMysqlDataBase()) {
	        	 remarks = getMysqlColumnComments(table.getSqlName(), columnName);
	         }
		      /**处理字段驼峰和字段名称，借用oracle中的comments,格式为UserCode,用户代码**/
	         /*
			 if(remarks!=null&&!"".equals(remarks)){
				 if (remarks != null && !remarks.equals("")) {
						String[] arrRemark = remarks.split(",");
						if(arrRemark.length>=1){
							humpColumnName = arrRemark[0];
						}
						if(arrRemark.length>=2){
							remarks = arrRemark[1];
						}
						/*for (int i = 0; i < arrRemark.length; i++) {
							remark = arrRemark[i];
							int index = remark.indexOf("=");
							if (index < 0){
								continue;
							}
								
							String param = remark.substring(0, index).trim();
							if (param.toUpperCase().equals("CODE")) {
								columnName = remark.substring(index + 1).trim();
								break;
							}

							if (param.toUpperCase().equals("NAME")) {
								remarks = remark.substring(index + 1).trim();
								break;
							}
						}
					}
			 }
			 */
	         // if columnNoNulls or columnNullableUnknown assume "not nullable"
	         boolean isNullable = (DatabaseMetaData.columnNullable == columnRs.getInt("NULLABLE"));
	         int size = columnRs.getInt("COLUMN_SIZE");
	         int decimalDigits = columnRs.getInt("DECIMAL_DIGITS");

	         boolean isPk = primaryKeys.contains(columnName);
	         boolean isIndexed = indices.contains(columnName);
	         String uniqueIndex = (String)uniqueIndices.get(columnName);
	         List columnsInUniqueIndex = null;
	         if (uniqueIndex != null) {
	            columnsInUniqueIndex = (List)uniqueColumns.get(uniqueIndex);
	         }

	         boolean isUnique = columnsInUniqueIndex != null && columnsInUniqueIndex.size() == 1;
	         if (isUnique) {
	            GLogger.trace("unique column:" + columnName);
	         }
	         //用于生成代码的Columb对象.对应数据库表column
/*	         Column column = new Column(
	               table,
	               sqlType,
	               sqlTypeName,
	               columnName,
	               size,
	               decimalDigits,
	               isPk,
	               isNullable,
	               isIndexed,
	               isUnique,
	               columnDefaultValue,
	               remarks);*/
	         Column column = new Column(
		               table,
		               sqlType,
		               sqlTypeName,
		               humpColumnName,
		               size,
		               decimalDigits,
		               isPk,
		               isNullable,
		               isIndexed,
		               isUnique,
		               columnDefaultValue,
		               remarks);
	         //用表自定义配置信息中的列信息重写Column对象
	         BeanHelper.copyProperties(column,TableOverrideValuesProvider.getColumnOverrideValues(table,column));
	         columns.add(column);
	    }
	    columnRs.close();
		return columns;
	}
	
	private ResultSet getColumnsResultSet(Table table) throws SQLException {
		ResultSet columnRs = null;
	    if (table.getOwnerSynonymName() != null) {
	         columnRs = getMetaData().getColumns(getCatalog(), table.getOwnerSynonymName(), table.getSqlName(), null);
	    } else {
	         columnRs = getMetaData().getColumns(getCatalog(), getSchema(), table.getSqlName(), null);
	    }
		return columnRs;
	}

	private List<String> getTablePrimaryKeys(Table table) throws SQLException {
		// get the primary keys
	      List primaryKeys = new LinkedList();
	      ResultSet primaryKeyRs = null;
	      if (table.getOwnerSynonymName() != null) {
	         primaryKeyRs = getMetaData().getPrimaryKeys(getCatalog(), table.getOwnerSynonymName(), table.getSqlName());
	      }
	      else {
	    	  //得到表中所有主键
	         primaryKeyRs = getMetaData().getPrimaryKeys(getCatalog(), getSchema(), table.getSqlName());
	      }
	      while (primaryKeyRs.next()) {
	         String columnName = primaryKeyRs.getString("COLUMN_NAME");
	         GLogger.trace("primary key:" + columnName);
	         primaryKeys.add(columnName);
	      }
	      primaryKeyRs.close();
		return primaryKeys;
	}
/**
 * oracle数据库
 * @param table
 * @return
 */
	private String getOracleTableComments(String table)  {
//		String sql = "SELECT comments FROM user_tab_comments WHERE table_name='"+table+"'";
		String sql = "SELECT comments FROM all_tab_comments WHERE table_name='"+table+"'";
		return dbHelper.queryForString(sql);
	}
	
	private String getOracleColumnComments(String table,String column)  {
//		String sql = "SELECT comments FROM user_col_comments WHERE table_name='"+table+"' AND column_name = '"+column+"'";
		String sql = "SELECT comments FROM ALL_COL_COMMENTS WHERE table_name='"+table+"' AND column_name = '"+column+"'";
		return dbHelper.queryForString(sql);
				
	}
	/**
	 * informix数据库
	 * @param table
	 * @author mark
	 * @return
	 */
	private String getInformixTableComments(String table)  {
//		String sql = "SELECT comments FROM user_tab_comments WHERE table_name='"+table+"'";
		String sql = "SELECT comments FROM user_tab_comments WHERE table_name='"+table+"'";
		return dbHelper.queryForString(sql);
	}

	private String getInformixColumnComments(String table,String column)  {
//		String sql = "SELECT comments FROM user_col_comments WHERE table_name='"+table+"' AND column_name = '"+column+"'";
		String sql = "SELECT comments FROM user_col_comments WHERE table_name='"+table+"' AND column_name = '"+column+"'";
		return dbHelper.queryForString(sql);
				
	}
	/**
	 * mysql数据库
	 * @param table
	 * @author mark
	 * @date 2015年3月14日
	 * @return
	 * @throws Exception 
	 */
	//取得表注释
	private String getMysqlTableComments(String table) throws Exception  {
		return ReadMysqlComments.getTableCommentByTableName(table);
	}
	//取得列注释
	private String getMysqlColumnComments(String table,String column) {
		Map<String, String> map = new HashMap<String,String>();
		try {
			map = ReadMysqlComments.parseColumn(ReadMysqlComments.getColumnCommentByTableName(table));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return map.get(column);
	}
	
	/**
	 * 根据表名获取表的owner
	 * @param tableName
	 * @return
	 */
	private String getOracleTableOwner(String tableName){
		//判断数据库类型
		//dbtype = GeneratorProperties.getProperty("jdbc.dbtype");
		String sql = null;
		if("informix".equals(dbtype)){
			sql = "SELECT owner FROM systables WHERE table_name='"+tableName+"'";

		}
		if("oracle".equals(dbtype)){
			sql = "SELECT owner FROM all_tables WHERE table_name='"+tableName+"'";
		}
		return dbHelper.queryForString(sql);
	}
	
	/** 得到表的自定义配置信息 */
	public static class TableOverrideValuesProvider {
		
		private static Map getTableOverrideValues(String tableSqlName){
			NodeData nd = getTableConfigXmlNodeData(tableSqlName);
			if(nd == null) {
			    return new HashMap();
			}
			return nd == null ? new HashMap() : nd.attributes;
		}
	
		private static Map getColumnOverrideValues(Table table, Column column) {
			NodeData root = getTableConfigXmlNodeData(table.getSqlName());
			if(root != null){
				 for(NodeData item : root.childs) {
					 if(item.nodeName.equals("column")) {
					     if(column.getSqlName().equalsIgnoreCase(item.attributes.get("sqlName"))) {
					         return item.attributes;
					     }
					 }
			     }
			}
			return new HashMap();
		}
		
		private static NodeData getTableConfigXmlNodeData(String tableSqlName){
			NodeData nd = getTableConfigXmlNodeData0(tableSqlName);
			if(nd == null) {
				nd = getTableConfigXmlNodeData0(tableSqlName.toLowerCase());
				if(nd == null) {
					nd = getTableConfigXmlNodeData0(tableSqlName.toUpperCase());
				}
			}
			return nd;
		}

		private static NodeData getTableConfigXmlNodeData0(String tableSqlName) {
			try {
				File file = FileHelper.getFileByClassLoader("src/main/table/" +tableSqlName+".xml");
				GLogger.trace("getTableConfigXml() load nodeData by tableSqlName:"+tableSqlName+".xml");
				return new XMLHelper().parseXML(file);
			}catch(Exception e) {//ignore
				GLogger.trace("not found config xml for table:"+tableSqlName+", exception:"+e);
				return null;
			}
		}
	}
	
	class DbHelper {
		public void close(ResultSet rs,PreparedStatement ps,Statement... statements) {
			try {
				if(ps != null) ps.close();
				if(rs != null) rs.close();
				for(Statement s : statements) {s.close();}
			}catch(Exception e){
			}
		}
		public boolean isOracleDataBase() {
			try {
				return DatabaseMetaDataUtils.isOracleDataBase(getMetaData());
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
		
		public boolean isMysqlDataBase() {
			try {
				return DatabaseMetaDataUtils.isMysqlDataBase(getMetaData());
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}

		public String queryForString(String sql) {
			Statement s = null;
			ResultSet rs = null;
			try {
				s =  getConnection().createStatement();
				rs = s.executeQuery(sql);
				if(rs.next()) {
					return rs.getString(1);
				}
				return null;
			}catch(SQLException e) {
//				e.printStackTrace();
				return null;
			}finally {
				close(rs,null,s);
			}
		}		
	}
	
	public static class DatabaseMetaDataUtils {
		public static boolean isOracleDataBase(DatabaseMetaData metadata) {
			try {
				boolean ret = false;
				ret = (metadata.getDatabaseProductName().toLowerCase()
							.indexOf("oracle") != -1);
				return ret;
			}catch(SQLException s) {
				return false;
			}
		}
		public static boolean isMysqlDataBase(DatabaseMetaData metadata) {
			try {
				boolean ret = false;
				ret = (metadata.getDatabaseProductName().toLowerCase()
							.indexOf("mysql") != -1);
				return ret;
			}catch(SQLException s) {
				return false;
			}
		}
		public static boolean isHsqlDataBase(DatabaseMetaData metadata) {
			try {
				boolean ret = false;
				ret = (metadata.getDatabaseProductName().toLowerCase()
							.indexOf("hsql") != -1);
				return ret;
			}catch(SQLException s) {
				return false;
			}
		}		
	}
}
