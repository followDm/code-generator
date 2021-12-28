package com.common.generator.provider.db.table.model;


import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import com.common.generator.provider.db.table.TableFactory;
import com.common.generator.GeneratorProperties;
import com.common.generator.util.StringHelper;
/**
 * 用于生成代码的Table对象.对应数据库的table
 * @modify diyvan
 */
public class Table implements java.io.Serializable,Cloneable {
	//数据库中表的表名称
	String sqlName;
	//数据库中表的表备注
	String remarks;
	//根据sqlName得到的类名称，示例值: UserInfo
	String className;
	//表示className加上所属模块的前缀比如SysUserInfo
	String classNameWithM;
	
	/** the name of the owner of the synonym if this table is a synonym */
	private String ownerSynonymName = null;
	/** real table name for oracle SYNONYM */
	private String tableSynonymName = null; 
	
	LinkedHashSet<Column> columns = new LinkedHashSet<Column>();
	List<Column> primaryKeyColumns = new ArrayList<Column>();
	
	
	public Table() {}
	
	public Table(Table t) {
		setSqlName(t.getSqlName());
		this.remarks = t.getRemarks();
		this.className = t.getClassName();
		this.classNameWithM = t.getClassNameWithM();
		this.ownerSynonymName = t.getOwnerSynonymName();
		this.columns = t.getColumns();
		this.primaryKeyColumns = t.getPrimaryKeyColumns();
		this.tableAlias = t.getTableAlias();
		this.exportedKeys = t.exportedKeys;
		this.importedKeys = t.importedKeys;
	}
	
	public LinkedHashSet<Column> getColumns() {
		return columns;
	}
	public void setColumns(LinkedHashSet<Column> columns) {
		this.columns = columns;
	}
	public String getOwnerSynonymName() {
		return ownerSynonymName;
	}
	public void setOwnerSynonymName(String ownerSynonymName) {
		this.ownerSynonymName = ownerSynonymName;
	}
	public String getTableSynonymName() {
		return tableSynonymName;
	}
	public void setTableSynonymName(String tableSynonymName) {
		this.tableSynonymName = tableSynonymName;
	}

	/** 使用 getPkColumns() 替换*/
	@Deprecated
	public List<Column> getPrimaryKeyColumns() {
		return primaryKeyColumns;
	}
	/** 使用 setPkColumns() 替换*/
	@Deprecated
	public void setPrimaryKeyColumns(List<Column> primaryKeyColumns) {
		this.primaryKeyColumns = primaryKeyColumns;
	}
	/** 数据库中表的表名称,其它属性很多都是根据此属性派生 */
	public String getSqlName() {
		return sqlName;
	}
	public void setSqlName(String sqlName) {
		this.sqlName = sqlName;
	}

	public static String removeTableSqlNamePrefix(String sqlName) {
		String prefixs = GeneratorProperties.getProperty("tableRemovePrefixes", "");
		for(String prefix : prefixs.split(",")) {
			String removedPrefixSqlName = StringHelper.removePrefix(sqlName, prefix,true);
			if(!removedPrefixSqlName.equals(sqlName)) {
				return removedPrefixSqlName;
			}
		}
		return sqlName;
	}
	
	/** 数据库中表的表备注 */
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	public void addColumn(Column column) {
		columns.add(column);
	}
	
	public void setClassName(String customClassName) {
		this.className = customClassName;
	}
	/**
	 * 根据sqlName得到的类名称，示例值: UserInfo
	 * @return
	 */
	public String getClassName() {
	    if(StringHelper.isBlank(className)) {
	        String removedPrefixSqlName = removeTableSqlNamePrefix(sqlName);
	        return StringHelper.makeAllWordFirstLetterUpperCase(StringHelper.toUnderscoreName(removedPrefixSqlName));
	    }else {
	    	return className;
	    }
	}
	/**
	 * get实现，加上模块的前缀
	 * modify by liuyatao 2015年1月20日
	 * @return
	 */
	public String getClassNameWithM() {
		//获取模块前缀，并首字母大写
		String prefix = StringHelper.changeFirstCharacterCase(GeneratorProperties.getProperty("modulename"),true);
	    if(StringHelper.isBlank(className)) {
	        String removedPrefixSqlName = removeTableSqlNamePrefix(sqlName);
	        return prefix+StringHelper.makeAllWordFirstLetterUpperCase(StringHelper.toUnderscoreName(removedPrefixSqlName));
	    }else {
	    	return classNameWithM;
	    }
	}
	/** 数据库中表的别名，等价于:  getRemarks().isEmpty() ? getClassName() : getRemarks() */
	public String getTableAlias() {
		if(StringHelper.isNotBlank(tableAlias)) return tableAlias;
		return StringHelper.removeCrlf(StringHelper.defaultIfEmpty(getRemarks(), getClassName()));
	}
	public void setTableAlias(String v) {
		this.tableAlias = v;
	}
	
	/**
	 * 等价于getClassName().toLowerCase()
	 * @return
	 */
	public String getClassNameLowerCase() {
		return getClassName().toLowerCase();
	}
	/**
	 * 得到用下划线分隔的类名称，如className=UserInfo,则underscoreName=user_info
	 * @return
	 */
	public String getUnderscoreName() {
		return StringHelper.toUnderscoreName(getClassName()).toLowerCase();
	}
	/**
	 * 返回值为getClassName()的第一个字母小写,如className=UserInfo,则ClassNameFirstLower=userInfo
	 * @return
	 */
	public String getClassNameFirstLower() {
		return StringHelper.uncapitalize(getClassName());
	}
	
	/**
	 * 根据getClassName()计算而来,用于得到常量名,如className=UserInfo,则constantName=USER_INFO
	 * @return
	 */
	public String getConstantName() {
		return StringHelper.toUnderscoreName(getClassName()).toUpperCase();
	}
	
	/** 使用 getPkCount() 替换*/
	@Deprecated
	public boolean isSingleId() {
		return getPkCount() == 1 ? true : false;
	}
	
	/** 使用 getPkCount() 替换*/
	@Deprecated
	public boolean isCompositeId() {
		return getPkCount() > 1 ? true : false;
	}

	/** 使用 getPkCount() 替换*/
	@Deprecated
	public boolean isNotCompositeId() {
		return !isCompositeId();
	}
	
	/**
	 * 得到主键总数
	 * @return
	 */
	public int getPkCount() {
		int pkCount = 0;
		for(Column c : columns){
			if(c.isPk()) {
				pkCount ++;
			}
		}
		return pkCount;
	}
	/**
	 * use getPkColumns()
	 * @deprecated 
	 */
	public List getCompositeIdColumns() {
		return getPkColumns();
	}
	
	/**
	 * 得到是主键的全部column
	 * @return
	 */	
	public List<Column> getPkColumns() {
		List results = new ArrayList();
		for(Column c : getColumns()) {
			if(c.isPk())
				results.add(c);
		}
		return results;
	}
	
	/**
	 * 得到是主键的全部column,并将主键拼成逗号格式
	 * @return
	 */	
	public String getPkColumnName() {
		StringBuffer results = new StringBuffer();
		//if(getPkCount()>1){
			results.append("id");
			return results.toString();
		/*}else{
			for(Column c : getColumns()) {
				if(c.isPk()){
					results.append(StringHelper.uncapitalize(c.getColumnName())).append(",");
				}
			}
			return results.substring(0, results.length()-1).toString();
		}*/
	}
	
	/**
	 * 得到是主键的全部column,并生成主键对应id的注解
	 * @return
	 */	
	public String getIdAnnotation() {
		StringBuffer results = new StringBuffer();
		StringBuffer subStr = new StringBuffer();
		if(getPkCount()>0){//存在主键
			results.append("    @EmbeddedId").append("\n");
			results.append("    @AttributeOverrides( {");			
			for(Column c : getColumns()) {
				if(c.isPk()){
					subStr.append("\n      @AttributeOverride(name = \"").append(c.getColumnNameFirstLower())
					.append("\", column = @Column(name = \"").append(c.getColumnNameFirstLower())
					.append("\")),");
				}
			}
			results.append(subStr.substring(0, subStr.length()-1)).append("\n").append("    })");
			return results.toString();
		}else{
			return "";
		}
	}
	
	/**
	 * 得到是主键的全部column,并将主键拼成逗号格式
	 * @return
	 */	
	public String getPkColumnTypeAndName() {
		StringBuffer results = new StringBuffer();
		if(getPkCount()>1){
			results.append(getClassName()).append("Id").append(" ").append(getClassNameFirstLower()).append("Id");
			return results.toString();
		}else{
			for(Column c : getColumns()) {
				if(c.isPk()){
					results.append(c.getJavaType()).append(" ").append(StringHelper.uncapitalize(c.getColumnName())).append(",");
				}
			}
			return results.substring(0, results.length()-1).toString();
		}
	}
	
	/**
	 * 得到不是主键的全部column
	 * @return
	 */
	public List<Column> getNotPkColumns() {
		List results = new ArrayList();
		for(Column c : getColumns()) {
			if(!c.isPk())
				results.add(c);
		}
		return results;
	}
	/** 得到单主键，等价于getPkColumns().get(0)  */
	public Column getPkColumn() {
		if(getPkColumns().isEmpty()) {
			throw new IllegalStateException("not found primary key on table:"+getSqlName());
		}
		return getPkColumns().get(0);
	}
	
	/**使用 getPkColumn()替换 */
	@Deprecated
	public Column getIdColumn() {
		return getPkColumn();
	}
	
	public List<Column> getEnumColumns() {
        List results = new ArrayList();
        for(Column c : getColumns()) {
            if(!c.isEnumColumn())
                results.add(c);
        }
        return results;	    
	}
	
	public Column getColumnByName(String name) {
	    Column c = getColumnBySqlName(name);
	    if(c == null) {
	    	c = getColumnBySqlName(StringHelper.toUnderscoreName(name));
	    }
	    return c;
	}
	
	public Column getColumnBySqlName(String sqlName) {
	    for(Column c : getColumns()) {
	        if(c.getSqlName().equalsIgnoreCase(sqlName)) {
	            return c;
	        }
	    }
	    return null;
	}
	
   public Column getRequiredColumnBySqlName(String sqlName) {
       if(getColumnBySqlName(sqlName) == null) {
           throw new IllegalArgumentException("not found column with sqlName:"+sqlName+" on table:"+getSqlName());
       }
       return getColumnBySqlName(sqlName);
    }
	
	/**
	 * 忽略过滤掉某些关键字的列,关键字不区分大小写,以逗号分隔
	 * @param ignoreKeywords
	 * @return
	 */
	public List<Column> getIgnoreKeywordsColumns(String ignoreKeywords) {
		List results = new ArrayList();
		for(Column c : getColumns()) {
			String sqlname = c.getSqlName().toLowerCase();
			if(StringHelper.contains(sqlname,ignoreKeywords.split(","))) {
				continue;
			}
			results.add(c);
		}
		return results;
	}
	
	/**
	 * This method was created in VisualAge.
	 */
	public void initImportedKeys(DatabaseMetaData dbmd,String owner) throws java.sql.SQLException {
		
			   // 获取由给定表的外键列（表导入的主键）引用的主键列的描述	
//			   ResultSet fkeys = dbmd.getImportedKeys(catalog,schema,this.sqlName);
			   ResultSet fkeys = dbmd.getImportedKeys(catalog,owner,this.sqlName);

			   while ( fkeys.next()) {
				 String pktable = fkeys.getString(PKTABLE_NAME);// 被导入的主键表名称
				 String pkcol   = fkeys.getString(PKCOLUMN_NAME);//被导入的主键列名称
				 String fktable = fkeys.getString(FKTABLE_NAME);//外键表名称 
				 String fkcol   = fkeys.getString(FKCOLUMN_NAME);//外键列名称
				 String seq     = fkeys.getString(KEY_SEQ);//外键中的序列号（值 1 表示外键中的第一列，值 2 表示外键中的第二列）。 
				 Integer iseq   = new Integer(seq);
				 getImportedKeys().addForeignKey(pktable,pkcol,fkcol,iseq);
			   }
			   fkeys.close();
	}
	
	/**
	 * This method was created in VisualAge.
	 */
	public void initExportedKeys(DatabaseMetaData dbmd,String owner) throws java.sql.SQLException {
			   // 检索引用给定表的主键列的外键列的说明。				
//			   ResultSet fkeys = dbmd.getExportedKeys(catalog,schema,this.sqlName);
			   ResultSet fkeys = dbmd.getExportedKeys(catalog,owner,this.sqlName);
			   while ( fkeys.next()) {
				 String pktable = fkeys.getString(PKTABLE_NAME);//主键表的名称
				 String pkcol   = fkeys.getString(PKCOLUMN_NAME);//主键的列名称
				 String fktable = fkeys.getString(FKTABLE_NAME);//外键表的名称
				 String fkcol   = fkeys.getString(FKCOLUMN_NAME);//外键的列名称
				 String seq     = fkeys.getString(KEY_SEQ);//多列主键中列的序列号
				 Integer iseq   = new Integer(seq);
				 getExportedKeys().addForeignKey(fktable,fkcol,pkcol,iseq);
			   }
			   fkeys.close();
	}
	
	/**
	 * @return Returns the exportedKeys.
	 */
	public ForeignKeys getExportedKeys() {
		if (exportedKeys == null) {
			exportedKeys = new ForeignKeys(this);
		}
		return exportedKeys;
	}
	/**
	 * @return Returns the importedKeys.
	 */
	public ForeignKeys getImportedKeys() {
		if (importedKeys == null) {
			importedKeys = new ForeignKeys(this);
		}
		return importedKeys;
	}
	
	public String toString() {
		return "Database Table:"+getSqlName()+" to ClassName:"+getClassName();
	}
	
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			//ignore
			return null;
		}
	}
	
	String catalog = TableFactory.getInstance().getCatalog();
	String schema = TableFactory.getInstance().getSchema();
	
	private String tableAlias;
	private ForeignKeys exportedKeys;
	private ForeignKeys importedKeys;
	
	public    static final String PKTABLE_NAME  = "PKTABLE_NAME";
	public    static final String PKCOLUMN_NAME = "PKCOLUMN_NAME";
	public    static final String FKTABLE_NAME  = "FKTABLE_NAME";
	public    static final String FKCOLUMN_NAME = "FKCOLUMN_NAME";
	public    static final String KEY_SEQ       = "KEY_SEQ";
}
