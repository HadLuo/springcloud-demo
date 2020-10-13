package com.uc.framework.db;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.util.CollectionUtils;

/**
 * 分页数据包装器
 * 
 * @author HadLuo
 * @since JDK1.7
 * @history 2020年5月11日 新建
 */
@ApiModel("分页实体")
public final class PageInfo<T> implements Serializable {

    /** xx */
    private static final long serialVersionUID = -5071101603040085033L;
    /** 总数据条数 */
    @ApiModelProperty("总数据条数")
    private long total;
    /** 当前数据量 */
    @ApiModelProperty("当前数据实体")
    private List<T> datas;

    public PageInfo(long total, List<T> datas) {
        this.total = total;
        this.datas = datas;
    }

    public PageInfo() {
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<T> getDatas() {
        if (datas == null) {
            return Collections.emptyList();
        }
        return datas;
    }

    public void setDatas(List<T> datas) {
        this.datas = datas;
    }

    public boolean isEmpty() {
        return CollectionUtils.isEmpty(datas);
    }

    public int currentSize() {
        return getDatas().size();
    }

    @Override
    public String toString() {
        return "PageInfo [total=" + total + ", datas=" + datas + "]";
    }

}
