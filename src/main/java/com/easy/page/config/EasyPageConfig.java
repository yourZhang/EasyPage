package com.easy.page.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * @program: com.easy.page
 * @description: 自定义分页器配置类
 * @author: xiaozhang666
 * @create: 2021-06-05 15:52
 **/
@ConfigurationProperties(
        prefix = "easypage"
)
public class EasyPageConfig {

    private String dialect = "mysql"; //数据库方言，不同的数据库有不同的分页方法
    private String pageSqlId = ".*ListPage.*"; //mapper.xml中需要拦截的ID(正则匹配)

    public EasyPageConfig() {
    }

    public String getDialect() {
        return this.dialect;
    }

    public void setDialect(String dialect) {
        this.dialect = dialect;
    }

    public String getPageSqlId() {
        return this.pageSqlId;
    }

    public void setPageSqlId(String pageSqlId) {
        this.pageSqlId = pageSqlId;
    }
}
