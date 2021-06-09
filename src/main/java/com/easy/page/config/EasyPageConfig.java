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

//    private String dialect; //数据库方言，不同的数据库有不同的分页方法
//    private String pageSqlId; //mapper.xml中需要拦截的ID(正则匹配)

    public static String dialect_Property = "dialect";
    public static String pageSqlId_Property = "pageSqlId";

    private Map<String, String> configMap = new HashMap<String, String>();

    public String getDialect() {
        return this.configMap.get(dialect_Property);
    }

    public void setDialect(String dialect) {
        this.configMap.put(dialect_Property, dialect);
    }

    public String getPageSqlId() {
        return this.configMap.get(pageSqlId_Property);
    }

    public void setPageSqlId(String pageSqlId) {
        this.configMap.put(pageSqlId_Property, pageSqlId);
    }

    public Map<String, String> getConfigMap() {
        return configMap;
    }

    public void setConfigMap(Map<String, String> configMap) {
        this.configMap = configMap;
    }
}
