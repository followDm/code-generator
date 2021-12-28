package com.common.base;

import org.apache.ibatis.annotations.Param;

import java.io.Serializable;
import java.util.List;


/**
 * 数据库操作基类，如果需要调用下列方法，需要在对应的mapper.xml配置
 * 
 * @author mark
 */
public interface BaseDao<T> {
    /**
     * 插入
     * 
     * @param t
     *            插入的对象
     * @return 插入数据条数
     */
    public int insert(T t);


    /**
     * 批量插入
     * 
     * @param list
     *            数据列表
     * @return 插入数据条数
     */
    public int insertList(List<T> list);


    /**
     * 修改
     * 
     * @param t
     *            修改的数据
     * @return 修改的数据条数
     */
    public int update(T t);


    /**
     * 删除
     * 
     * @param id
     *            数据标识
     * @return 删除的数据条数
     */
    public int delete(Serializable id);


    /**
     * 通过条件查询记录数
     * 
     * @param t
     *            查询条件
     * @return 记录数
     */
    public int getTotal(T t);


    /**
     * 通过条件查询数据列表
     * 
     * @param t
     *            查询条件
     * @return 数据列表
     */
    public List<T> getList(T t);


    /**
     * 查询所有数据
     * 
     * @return 数据列表
     */
    public List<T> getAll();


    /**
     * 通过id查询数据
     * 
     * @param id
     *            数据标识
     * @return 数据对象
     */
    public T get(Serializable id);


    /**
     * 根据条件分页查询
     * 
     * @param t
     *            查询参数
     * @param offset
     *            排序
     * @param rows
     *            分页
     * @return 分页数据列表
     */
    public List<T> getPage(@Param("queryParam") T t, @Param("offset") int offset, @Param("rows") int rows);

}
