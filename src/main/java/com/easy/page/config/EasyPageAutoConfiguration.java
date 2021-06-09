package com.easy.page.config;

import com.easy.page.EasyPage;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Iterator;
import java.util.List;

/**
 * @program: com.easy.page
 * @description: 自动装配类
 * @author: xiaozhang666
 * @create: 2021-06-05 12:12
 **/
@Configuration
@ConditionalOnBean({SqlSessionFactory.class})
@AutoConfigureAfter({MybatisAutoConfiguration.class})
@EnableConfigurationProperties({EasyPageConfig.class})
public class EasyPageAutoConfiguration {

    @Autowired
    private List<SqlSessionFactory> sqlSessionFactoryList;

    @Autowired
    private EasyPageConfig easyPageConfig;

    public EasyPageAutoConfiguration() {
    }

    @PostConstruct
    public void addEasyPageInterceptor() {
        Iterator var3 = this.sqlSessionFactoryList.iterator();
        EasyPage e = new EasyPage(easyPageConfig.getConfigMap());
        while (var3.hasNext()) {
            SqlSessionFactory sqlSessionFactory = (SqlSessionFactory) var3.next();
            sqlSessionFactory.getConfiguration().addInterceptor(e);
        }
    }
}
