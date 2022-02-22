package com.common.base;

import com.ytx.datastream.common.page.PageList;
import com.ytx.datastream.common.page.Paginator;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;


/**
 * service抽象类，实现部分公共方法 update 2015年9月10日 重构分页方法
 * 
 * @author liuyatao
 */
public abstract class BaseServiceImpl<T> implements BaseService<T> {
    /**
     * 抽象方法，必须实现，返回DAO实例
     * 
     * @return DAO实例
     */
    public abstract BaseDao<T> getDao();


    @Override

    @Transactional(readOnly = false)
    public int insert(T t) {
        return getDao().insert(t);
    }


    @Override
    @Transactional(readOnly = false)
    public void insertList(List<T> list) {
        getDao().insertList(list);
    }


    @Override
    @Transactional(readOnly = false)
    public void update(T t) {
        getDao().update(t);
    }


    @Override
    @Transactional(readOnly = false)
    public void delete(Serializable id) {
        getDao().delete(id);
    }


    @Override
    @Transactional(readOnly = true)
    public List<T> getAll() {
        return getDao().getAll();
    }


    @Override
    @Transactional(readOnly = true)
    public List<T> getList(T t) {
        return getDao().getList(t);
    }


    @Override
    @Transactional(readOnly = true)
    public T get(Serializable id) {
        return getDao().get(id);
    }


    @Override
    @Transactional(readOnly = true)
    public PageList<T> getPage(T t, int pageNo, int pageSize) {
        int items = getDao().getTotal(t);

        Paginator p = new Paginator(pageSize);
        p.setItems(items);
        p.setPage(pageNo);
        int offset = p.getOffset();
        int rows = p.getLength();
        List<T> list = getDao().getPage(t, offset, rows);
        PageList<T> pageList = new PageList<T>();
        pageList.setDatas(list);
        pageList.setPaginator(p);
        return pageList;
    }

}
