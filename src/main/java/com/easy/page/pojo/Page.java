package com.easy.page.pojo;


/**
 * @program: com.easy.page
 * @description: 分页实体类
 * @author: xiaozhang666
 * @create: 2021-06-05 20:23
 **/
public class Page {
    private int showCount;      //每页显示记录数
    private int totalPage;      //总页数
    private int totalResult;    //总记录数
    private int currentPage;    //当前页
    private int currentResult;  //当前记录起始索引

    public Page() {
    }

    public int getTotalPage() {
        if (totalResult % showCount == 0)
            totalPage = totalResult / showCount;
        else
            totalPage = totalResult / showCount + 1;
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public int getTotalResult() {
        return totalResult;
    }

    public void setTotalResult(int totalResult) {
        this.totalResult = totalResult;
    }

    public int getCurrentPage() {
        if (currentPage <= 0)
            currentPage = 1;
        if (currentPage > getTotalPage())
            currentPage = getTotalPage();
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }


    public int getShowCount() {
        return showCount;
    }

    public void setShowCount(int showCount) {

        this.showCount = showCount;
    }

    public int getCurrentResult() {
        currentResult = (getCurrentPage() - 1) * getShowCount();
        if (currentResult < 0)
            currentResult = 0;
        return currentResult;
    }

    public void setCurrentResult(int currentResult) {
        this.currentResult = currentResult;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Page{");
        sb.append("showCount=").append(showCount);
        sb.append(", totalPage=").append(totalPage);
        sb.append(", totalResult=").append(totalResult);
        sb.append(", currentPage=").append(currentPage);
        sb.append(", currentResult=").append(currentResult);
        sb.append('}');
        return sb.toString();
    }
}
