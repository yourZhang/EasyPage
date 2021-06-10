# EasyPage
### Mybatis自定义分页器插件

#### 请把以下都当成是yy

一般都是用PageHelper，但是随着数据量变大，SQL变复杂效率低了不少，度娘的答案都是从SQL入手想办法提高效率，可是我的问题不是在SQL上，仅仅是PH封装SQL就用了3秒，我的SQL在服务器上跑也就0.003m啊，😅😅😅

然后就去翻PH源码，打断点看啊看，逻辑还是不少的...

#### 第一个想法

然后想自己实现一个，开始是想要原生的分页器就是那种一个实体类，然后执行一条COUNT之类的，又考虑到自己的强迫症（COUNT不能复用啊，有木有😬能复用的）。

以下是废话啧啧，最初的想法，我动态代理Mapper接口，然后哼哼改SQL，提前执行COUNT，若COUNT=0那咱们就不查询了，这样效率还高啧啧，天才！😎（后来发现PH已经有这个功能了，打脸打脸🤬）,但是有个问题就是....就算我能代理Mapper对象，强行拿SQL，强行改，我强行....怎么执行呢，套你猴子的😂，总不能拿JDBC跑一下吧，再仿一个Mybatis日志，精彩！~~~领导以为我用Mybatis执行的🤣（领导：以为我傻？😁）。

#### 下一步

很不想去写一个PH类似的插件，因为...我还要重新复习Mybatis源码有木有，还得学习一下PH的实现逻辑..呕（内心：我要划水！🤗），最终败在自己的强迫症下。

最终DeBug了PageHepler，复习了Mybatis源码，了解了内置对象、执行过程等等等等...然后从网上扒了一份试了试，嗯可以的不错，那我就只封装一个SpringBootStarter吧，真懒啊🤨。就这样就这样大概三天时间就出来了。

### 配置和使用

因为封装了AutoConfiguration类，所以让他自己装自己就好了😁，我装我自己。

以下配置不一定必须要有，因为有默认配置，约定大于配置。

但是也可以自己配置，就像PH的方言一样，我是谁我在哪

#### 配置

```yml
easypage: #yml文件
  dialect: mysql  #支持mysql、oracle，其实一切支持limit分页的数据库都可以用mysql
  page-sql-id: .*ListPage.* # 案例 *.PageList.* 左右通配的意思 这个要单独演示一下  
```

```java
//查询所有用户 mapper.java
List<User> getUserListPage(User user);

//mapper.xml
<select id="getUserListPage" resultType="User">
   select * from user
</select>
```

只有在Mapper中写成这种格式才能生命要使用EasyPage分页，反之不添加ListPage的查询就不使用。

比如，这种不使用分页的写法就明白了吧

```java
//查询所有用户 mapper.java
List<User> getUser(User user);

//mapper.xml
<select id="getUser" resultType="User">
   select * from user
</select>
```

我们也可以写成这样

```java
//getUserLListPage,getUserListPageUser...等等只要你的配置能对上的就行了
```

#### 实体的封装

分页肯定要有分页参数的，这里提供了两种分页参数封装的方式

继承和持有 Page类  **com.easy.page.pojo.Page**

```java
public class Page {
    private int showCount;      //每页显示记录数
    private int totalPage;      //总页数
    private int totalResult;    //总记录数
    private int currentPage;    //当前页
    private int currentResult;  //当前记录起始索引
    //Get/Set..
```

##### 继承Page

```java
@Data
@ToString
public class User extends Page implements Serializable {
    private Integer id;
    private String username;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date birthday;
}
```

继承以后会把Total丢回到totalResult属性中。

##### 持有Page

```java
@Data
@ToString
public class User extends Page implements Serializable {
    private Integer id;
    private String username;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date birthday;
    private Page page = new Page();
}
```

一样会丢Total丢回到持有的Page对象的totalResult属性中，属性名必须为page。

##### 简单的案例

```java
@Test
void testMyPageUtils() {
    Page page = new Page();
    page.setCurrentPage(1);
    page.setShowCount(3);
    List<User> byPage = userService.findByPage(page);
    byPage.forEach(System.out::println);
}
```

```xml
<select id="findByPageListPage" resultType="User">
    select * from users
</select>
```

```java
public interface UserMapper {
    //因为sql没有where条件，所以不用继承也不用持有，直接传Page分页对象
    List<User> findByPageListPage(Page page);
}
```

##### 备注

一对多，一对一，条件查询都测试过了

### 总结 

是不是感觉上手很配置都麻烦，还不如PH呢，但是我取的名字又叫Easy，就是因为逻辑比PH少吧。懒还是🤫

PH返回一个 **Page<T>**，但是源码里是拦截了 **Executor.class**，马上吃饭了明天见...不能耽误下班🤣。

打开 **Page<T>** 类可以发现继承了 **List** 类，大家常用的 **getResult**方法也只是 **return this**。

这就是PH能直接结果的原因，还有**PageHepler.startPage()**就是将参数配置上（全是废话...😮）

看了一会PH的拦截和执行，人家和咱们拦截的内置对象不同，导致获取到的返回结果不同，**Executor**.**class**直接就把 **List**结果集拿回来，我们拦截的 **Statment.class**只能拿Mybatis的另一个内置对象 **ResultSet.class**

我可不想再用while方式去取值了，代码太丑了。

上面说的如果COUNT为0就不去查询的操作，貌似还在写，直接模仿PH返回Page对象也在考虑，不忙的时候一起写完就这样。

Oracle的没有测试过....保重