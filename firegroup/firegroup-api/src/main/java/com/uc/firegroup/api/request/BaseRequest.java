package com.uc.firegroup.api.request;

import java.io.Serializable;

/**
 * @author 鲁志学 2020年09月14日  新建
 * @since JDK1.8
 */
public class BaseRequest implements Serializable {

    private static final long serialVersionUID = 1445322829134051618L;

    private Integer pageIndex;

    private Integer pageSize;

    private boolean debug;

    public Integer getPageIndex() {
        if (pageIndex == null) {
            setPageIndex(1);
        }
        return pageIndex;
    }

    public void setPageIndex(Integer pageIndex) {
        this.pageIndex = pageIndex;
    }

    public Integer getPageSize() {
        if (pageSize == null) {
            setPageSize(10);
        }
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

}
