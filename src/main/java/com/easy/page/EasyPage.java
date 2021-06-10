package com.easy.page;

import com.easy.page.config.EasyPageConfig;
import com.easy.page.pojo.Page;
import com.easy.page.utils.ReflectHelper;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.executor.statement.BaseStatementHandler;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.apache.ibatis.scripting.xmltags.ForEachSqlNode;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.util.StringUtils;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @program: com.easy.page
 * @description:
 * @author: xiaozhang666
 * @create: 2021-06-05 11:54
 **/
@Intercepts({
        @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})
})
public class EasyPage implements Interceptor {

    private Map<String, String> pageConfigMap;

    public EasyPage(Map<String, String> pageConfigMap) {
        this.pageConfigMap = pageConfigMap;
    }

    @Override
    public Object intercept(Invocation ivk) throws Throwable {
        if (ivk.getTarget() instanceof RoutingStatementHandler) {
            //拦截到的目标对象：RoutingStatementHandler对象
            RoutingStatementHandler statementHandler = (RoutingStatementHandler) ivk.getTarget();
            //通过反射获取到当前RoutingStatementHandler对象的delegate属性
            BaseStatementHandler delegate = (BaseStatementHandler) ReflectHelper.getValueByFieldName(statementHandler, "delegate");
            //通过反射获取delegate父类BaseStatementHandler的mappedStatement属性
            MappedStatement mappedStatement = (MappedStatement) ReflectHelper.getValueByFieldName(delegate, "mappedStatement");
            if (mappedStatement.getId().matches(pageConfigMap.get(EasyPageConfig.pageSqlId_Property))) { //拦截需要分页的SQL
                //获取到当前StatementHandler的 boundSql，这里不管是调用handler.getBoundSql()还是直接调用delegate.getBoundSql()结果是一样的
                BoundSql boundSql = delegate.getBoundSql();
                //拿到当前绑定Sql的参数对象，就是我们在调用对应的Mapper映射语句时所传入的参数对象
                Object parameterObject = boundSql.getParameterObject();//分页SQL<select>中parameterType属性对应的实体参数，即Mapper接口中执行分页方法的参数,该参数不得为空
                if (parameterObject == null) {
                    throw new NullPointerException("parameterObject尚未实例化！");
                } else {
                    //拦截到的prepare方法参数是一个Connection对象
                    Connection connection = (Connection) ivk.getArgs()[0];
                    //获取当前要执行的Sql语句，也就是我们直接在Mapper映射语句中写的Sql语句
                    String sql = boundSql.getSql();
                    //String countSql = "select count(0) from (" + sql+ ") as tmp_count"; //记录统计
                    String countSql = "select count(0) from (" + sql + ")  tmp_count"; //记录统计 == oracle 加 as 报错(SQL command not properly ended)
                    //记录统计
                    PreparedStatement countStmt = connection.prepareStatement(countSql);
                    BoundSql countBS = new BoundSql(mappedStatement.getConfiguration(), countSql, boundSql.getParameterMappings(), parameterObject);
                    setParameters(countStmt, mappedStatement, countBS, parameterObject);
                    ResultSet rs = countStmt.executeQuery();
                    int count = 0;
                    if (rs.next()) {
                        count = rs.getInt(1);
                    }
                    rs.close();
                    countStmt.close();
                    //System.out.println(count);
                    Page page = null;
                    if (parameterObject instanceof Page) {    //参数就是Page实体
                        page = (Page) parameterObject;
                        //给当前的page参数对象设置总记录数
                        page.setTotalResult(count);
                    } else {  //参数为某个实体，该实体拥有Page属性
                        Field pageField = ReflectHelper.getFieldByFieldName(parameterObject, "page");
                        if (pageField != null) {
                            page = (Page) ReflectHelper.getValueByFieldName(parameterObject, "page");
                            if (page == null)
                                page = new Page();
                            //给当前的page参数对象设置总记录数
                            page.setTotalResult(count);
                            ReflectHelper.setValueByFieldName(parameterObject, "page", page); //通过反射，对实体对象设置分页对象
                        } else {
                            throw new NoSuchFieldException(parameterObject.getClass().getName() + "不存在 page 属性！");
                        }
                    }
                    //获取分页Sql语句

                    String pageSql = generatePageSql(sql, page);
                    //测试拦截结果直接返回
                    //PreparedStatement preparedStatement = connection.prepareStatement(pageSql);
                    //BoundSql countBS2 = new BoundSql(mappedStatement.getConfiguration(), pageSql, boundSql.getParameterMappings(), parameterObject);
                    //setParameters(preparedStatement, mappedStatement, countBS2, parameterObject);
                    //利用反射设置当前BoundSql对应的sql属性为我们建立好的分页Sql语句
                    ReflectHelper.setValueByFieldName(boundSql, "sql", pageSql); //将分页sql语句反射回BoundSql.
                }
            }
        }
        Object proceed = ivk.proceed();
        return proceed;
    }

    /**
     * 根据数据库方言，生成特定的分页sql
     *
     * @param sql
     * @param page
     * @return
     */
    private String generatePageSql(String sql, Page page) {
        if (page != null && !StringUtils.isEmpty(sql)) {
            StringBuffer pageSql = new StringBuffer();
            if ("mysql".equals(pageConfigMap.get(EasyPageConfig.dialect_Property))) {
                pageSql.append(sql);
                pageSql.append(" limit " + page.getCurrentResult() + "," + page.getShowCount());
            } else if ("oracle".equals(pageConfigMap.get(EasyPageConfig.dialect_Property))) {
                pageSql.append("select * from (select tmp_tb.*,ROWNUM row_id from (");
                pageSql.append(sql);
                //pageSql.append(") as tmp_tb where ROWNUM<=");
                pageSql.append(") tmp_tb where ROWNUM<=");
                pageSql.append(page.getCurrentResult() + page.getShowCount());
                pageSql.append(") where row_id>");
                pageSql.append(page.getCurrentResult());
            }
            return pageSql.toString();
        } else {
            return sql;
        }
    }


    /**
     * 对SQL参数(?)设值,参考org.apache.ibatis.executor.parameter.DefaultParameterHandler
     *
     * @param ps
     * @param mappedStatement
     * @param boundSql
     * @param parameterObject
     * @throws SQLException
     */
    private void setParameters(PreparedStatement ps, MappedStatement mappedStatement, BoundSql boundSql, Object parameterObject) throws SQLException {
        ErrorContext.instance().activity("setting parameters").object(mappedStatement.getParameterMap().getId());
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        if (parameterMappings != null) {
            Configuration configuration = mappedStatement.getConfiguration();
            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            MetaObject metaObject = parameterObject == null ? null : configuration.newMetaObject(parameterObject);
            for (int i = 0; i < parameterMappings.size(); i++) {
                ParameterMapping parameterMapping = parameterMappings.get(i);
                if (parameterMapping.getMode() != ParameterMode.OUT) {
                    Object value;
                    String propertyName = parameterMapping.getProperty();
                    PropertyTokenizer prop = new PropertyTokenizer(propertyName);
                    if (parameterObject == null) {
                        value = null;
                    } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                        value = parameterObject;
                    } else if (boundSql.hasAdditionalParameter(propertyName)) {
                        value = boundSql.getAdditionalParameter(propertyName);
                    } else if (propertyName.startsWith(ForEachSqlNode.ITEM_PREFIX) && boundSql.hasAdditionalParameter(prop.getName())) {
                        value = boundSql.getAdditionalParameter(prop.getName());
                        if (value != null) {
                            value = configuration.newMetaObject(value).getValue(propertyName.substring(prop.getName().length()));
                        }
                    } else {
                        value = metaObject == null ? null : metaObject.getValue(propertyName);
                    }
                    TypeHandler typeHandler = parameterMapping.getTypeHandler();
                    if (typeHandler == null) {
                        throw new ExecutorException("There was no TypeHandler found for parameter " + propertyName + " of statement " + mappedStatement.getId());
                    }
                    typeHandler.setParameter(ps, i + 1, value, parameterMapping.getJdbcType());
                }
            }
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }
}
