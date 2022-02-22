package ${basepackage}.dao.mapper;

import ${basepackage}.domain.${className};

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;
/**
 * 持久化接口
 */
@Component
public interface ${className}Mapper {
    public ${className} get(Integer id);

    public int update(${className} ${classNameLowerCase});

    public int insert(${className} ${classNameLowerCase});

    public int delete(Integer id);

    public List<${className}> getList(${className} ${classNameLowerCase});
}