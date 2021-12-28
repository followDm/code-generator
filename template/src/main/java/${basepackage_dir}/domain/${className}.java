<#assign className = table.className>
<#assign classNameLower = className?uncap_first> 
package ${basepackage}.domain;

import java.util.Date;

public class ${className} implements java.io.Serializable{
	private static final long serialVersionUID = 1L;
	
	/**	构造函数	**/
	public ${className}() {
	}
	
	/**属性*/
	<@generateFields/>
	
	/**getter setter方法*/
	<@genGetSetProperties/>
	
	@Override
	public String toString() {
		String result =  "${className} [";
		<#list table.columns as column>
			result += "${column.columnNameFirstLower}="+${column.columnNameFirstLower}+",";
		</#list>
		result = result.substring(0,result.lastIndexOf(","));
		result += "]";
		return result;
	}
}