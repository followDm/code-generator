package com.common.generator.util.typemapping;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("all")
public class OracleDataTypeForJdbcAndJava {
	
	//数据库数据类型和jdbc类型对应关系
	private final static Map _preferredSqlTypeForDataType = new HashMap();
	
	//数据库类型和java类型对应关系
	private final static Map _preferredJavaTypeForDataType = new HashMap();
	
	//根据oracle类型得到对应JDBC数据类型
	public static String getPreferredJdbcType(String dataType){
		String jdbcType = (String)_preferredSqlTypeForDataType.get(dataType);		
		return jdbcType;
	}
	
	//根据oracle类型得到对应java数据类型
	public static String getPreferredJavaType(String dataType){
		String javaType = (String)_preferredJavaTypeForDataType.get(dataType);		
		return javaType;
	}
	
	//根据数据类型得到该参数的种类
	public static String getPreferredParameterKind(String dataType){
		/*String kind = ProcedureParameterKind.NORMAL.getId();
		String jdbcType = (String)_preferredSqlTypeForDataType.get(dataType);
		if(jdbcType == null){
			kind = ProcedureParameterKind.ARRAY.getId();				
		}else if("ORACLECURSOR".equals(jdbcType)){
			kind =ProcedureParameterKind.CURSOR.getId();
		}
		return kind;*/
		return null;
	}
	
	//oracle类型和jdbc类型对应关系
	static{
		_preferredSqlTypeForDataType.put("blob", "BLOB");
		_preferredSqlTypeForDataType.put("clob", "CLOB");
		_preferredSqlTypeForDataType.put("date", "TIMESTAMP");
		_preferredSqlTypeForDataType.put("number", "DECIMAL");
		_preferredSqlTypeForDataType.put("long", "VARBINARY");
		_preferredSqlTypeForDataType.put("timestamp", "TIMESTAMP");
		_preferredSqlTypeForDataType.put("varchar2", "VARCHAR");
		_preferredSqlTypeForDataType.put("varchar", "VARCHAR");
		_preferredSqlTypeForDataType.put("nvarchar2", "VARCHAR");
		_preferredSqlTypeForDataType.put("nclob", "OTHER");
		_preferredSqlTypeForDataType.put("smallint", "SMALLINT");
		_preferredSqlTypeForDataType.put("char", "CHAR");
		_preferredSqlTypeForDataType.put("raw", "VARBINARY");		
		_preferredSqlTypeForDataType.put("float", "FLOAT");
		_preferredSqlTypeForDataType.put("integer", "INTEGER");
		_preferredSqlTypeForDataType.put("double", "DOUBLE");
		_preferredSqlTypeForDataType.put("real", "REAL");
		_preferredSqlTypeForDataType.put("ref cursor", "ORACLECURSOR");
	}
	
	//oracle类型和java类型对应关系
	static{
		_preferredJavaTypeForDataType.put("blob", "byte[]");
		_preferredJavaTypeForDataType.put("clob", "java.lang.String");
		_preferredJavaTypeForDataType.put("date", "java.sql.Date");
		_preferredJavaTypeForDataType.put("number", "java.math.BigDecimal");
		_preferredJavaTypeForDataType.put("long", "java.math.BigDecimal");
		_preferredJavaTypeForDataType.put("timestamp", "java.sql.Timestamp");
		_preferredJavaTypeForDataType.put("varchar2", "java.lang.String");
		_preferredJavaTypeForDataType.put("varchar", "java.lang.String");
		_preferredJavaTypeForDataType.put("nvarchar2", "java.lang.String");
		_preferredJavaTypeForDataType.put("nclob", "java.lang.String");
		_preferredJavaTypeForDataType.put("smallint", "java.lang.Short");
		_preferredJavaTypeForDataType.put("char", "java.lang.String");
		_preferredJavaTypeForDataType.put("raw", "byte[]");		
		_preferredJavaTypeForDataType.put("float", "java.lang.Double");
		_preferredJavaTypeForDataType.put("integer", "java.lang.Integer");
		_preferredJavaTypeForDataType.put("double", "java.lang.Double");
		_preferredJavaTypeForDataType.put("real", "java.lang.Float");
		_preferredJavaTypeForDataType.put("ref cursor", "cursor");
	}
	
	
	/**
	 * 根据传入的数据类型判断是不是oracle的基本数据类型
	 * @param dataType
	 * @return
	 */
	public static boolean isBaseDataType(String dataType){
		if("char".equals(dataType) || "nchar".equals(dataType)|| "varchar2".equals(dataType)
		   || "nvarchar2".equals(dataType) || "date".equals(dataType) || "long".equals(dataType) 
		   || "raw".equals(dataType) || "blob".equals(dataType) || "clob".equals(dataType) 
		   || "nclob".equals(dataType) || "bfile".equals(dataType) || "rowid".equals(dataType) 
		   || "nrowid".equals(dataType) || "number".equals(dataType) || "decimal".equals(dataType)
		   || "integer".equals(dataType) || "float".equals(dataType) || "real".equals(dataType) 
		   || "timestamp".equals(dataType) || "binary_double".equals(dataType) || "smallint".equals(dataType) 
		   || "ref".equals(dataType) || "varchar".equals(dataType) 
		   || "binary_float".equals(dataType)){
			return true;
		}else{
			return false;
		}
			
	}
	
}
