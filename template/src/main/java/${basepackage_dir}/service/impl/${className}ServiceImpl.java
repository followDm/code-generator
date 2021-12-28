<#assign className = table.className>
<#assign sqlName = table.sqlName>
<#assign classNameLower = className?uncap_first> 
package ${basepackage}.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uxin.common.base.BaseServiceImpl;
import com.uxin.common.base.BaseDao;

import ${basepackage}.domain.${className};
import ${basepackage}.dao.${className}Mapper;
import ${basepackage}.service.${className}Service;

/**
 * ${table.remarks}业务逻辑实现
 * @author ${author}
 */
@Service
public class ${className}ServiceImpl extends BaseServiceImpl<${className}> implements ${className}Service{

	@AutoWird
	private ${className}Mapper ${classNameLower}Mapper;
	
	@Override
	public BaseDao<${className}> getDao() {
		return ${classNameLower}Mapper;
	}
}
