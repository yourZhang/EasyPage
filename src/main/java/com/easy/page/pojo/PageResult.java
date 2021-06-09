package com.easy.page.pojo;

import java.util.List;

/**
 * @program: mybatis-helper-test
 * @description: 分页返回对象
 * @author: xiaozhang666
 * @create: 2021-06-07 10:04
 **/
public class PageResult {
    private Long total;
    private List res;

    public PageResult() {
    }

    public PageResult(Long total, List res) {
        this.total = total;
        this.res = res;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public List getRes() {
        return res;
    }

    public void setRes(List res) {
        this.res = res;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PageResult{");
        sb.append("total=").append(total);
        sb.append(", res=").append(res);
        sb.append('}');
        return sb.toString();
    }
}
