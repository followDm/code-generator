package com.common.page;

import java.io.Serializable;
import java.util.List;


/**
 * Page查询结果对象
 * @author YANGLiN
 */
public final class PageList<T> implements Serializable {

    private static final long serialVersionUID = 7636400405542623379L;

    private List<T> datas;

    private Paginator paginator;

    public List<T> getDatas() {
        return datas;
    }

    public void setDatas(List<T> datas) {
        this.datas = datas;
    }

    public Paginator getPaginator() {
        return paginator;
    }

    public void setPaginator(Paginator paginator) {
        this.paginator = paginator;
    }
}
