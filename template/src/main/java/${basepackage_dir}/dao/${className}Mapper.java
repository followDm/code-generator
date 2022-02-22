<#assign className = table.className>
<#assign sqlName = table.sqlName>
<#assign classNameLower = className?uncap_first> 
package ${basepackage}.dao;

import com.uxin.common.base.BaseDao;
import ${basepackage}.domain.${className};

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;
/**
 * 持久化接口
 */
public interface ${className}Mapper  extends BaseDao<${className}>{
    public ${className} get(Integer id);

    public int update(${className} ${classNameLowerCase});

    public int insert(${className} ${classNameLowerCase});

    public int delete(Integer id);

    public List<${className}> getList(${className} ${classNameLowerCase});
}