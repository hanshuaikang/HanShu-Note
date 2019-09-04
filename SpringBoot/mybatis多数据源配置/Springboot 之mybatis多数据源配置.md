# Springboot 之mybatis多数据源配置

## 前言:

随着应用用户量的增加，相应的并发量也会跟着不断增加，逐渐的，单个数据库已经没有办法满足我们频繁的数据库操作请求了，某些场景下，我们可能会需要配置多个数据源，动态切换数据源来缓解系统的压力等，同样的，Springboot官方提供了相应的实现来帮助开发者们配置多数据源，一般分为两种方式(我知道的)，分包和AOP，其中利用AOP实现动态多数据源到时候会另开一篇文章来写。考虑到mybatis是java开发者们使用较为频繁的数据库框架，所以本篇文章使用Springboot+Mybatis来实现多数据源的配置。

废话不多说，走起。

## 1. 数据库准备:

既然是配置多数据源，那么我们就要先把相应的数据源给准备好，这里呢，我本地新建了两个数据库，如下表：

| 数据库 | testdatasource1            | testdatasource2 |
| ------ | -------------------------- | --------------- |
| 数据表 | sys_user                   | sys_user2       |
| 字段   | user_id user_name user_age | 同              |

并分别插入两条记录，为了方便对比，其中testdatasource1为张三，  testdatasource2为李四



## 2.环境准备

首先新建一个Springboot项目，我这里版本是2.1.7.RELEASE，并引入相关依赖：关键依赖如下:

```xml
        <dependency>
            <groupId>org.mybatis.spring.boot</groupId>
            <artifactId>mybatis-spring-boot-starter</artifactId>
            <version>1.3.2</version>
        </dependency>

        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
        </dependency>

```

到这里我们的环境已经基本配置完成了。

## 3.代码部分

### 1. 多数据源配置

首先呢，在我们Springboot的配置文件中配置我们的datasourse，和以往不一样的是，这次我们要配置两个，所以要指定名称，如下:

```java
#配置主数据库
spring.datasource.primary.jdbc-url=jdbc:mysql://localhost:3306/testdatasource1?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC&useSSL=false
spring.datasource.primary.username=root
spring.datasource.primary.password=root
spring.datasource.primary.driver-class-name=com.mysql.cj.jdbc.Driver

##配置次数据库
spring.datasource.secondary.jdbc-url=jdbc:mysql://localhost:3306/testdatasource2?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC&useSSL=false
spring.datasource.secondary.username=root
spring.datasource.secondary.password=root
spring.datasource.secondary.driver-class-name=com.mysql.cj.jdbc.Driver


spring.http.encoding.charset=UTF-8
spring.http.encoding.enabled=true
spring.http.encoding.force=true
```



> 需要我们注意的是，Springboot2.0 在配置数据库连接的时候需要使用jdbc-url，如果只使用url的话会报
>
> jdbcUrl is required with driverClassName.错误。



新建一个配置类PrimaryDataSourceConfig，用于配置我们的主数据库相关的bean，代码如下:

```java
@Configuration
@MapperScan(basePackages = "com.jdkcb.mybatisstuday.mapper.one", sqlSessionFactoryRef = "PrimarySqlSessionFactory")
public class PrimaryDataSourceConfig {

    @Bean(name = "PrimaryDataSource")
    // 表示这个数据源是默认数据源
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.primary")
    public DataSource getPrimaryDateSource() {
        return DataSourceBuilder.create().build();
    }


    @Bean(name = "PrimarySqlSessionFactory")
    @Primary
    public SqlSessionFactory primarySqlSessionFactory(@Qualifier("PrimaryDataSource") DataSource datasource)
            throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(datasource);
        bean.setMapperLocations( 
                new PathMatchingResourcePatternResolver().getResources("classpath*:mapping/one/*.xml"));
        return bean.getObject();// 设置mybatis的xml所在位置
    }
    
    
    @Bean("PrimarySqlSessionTemplate")
    // 表示这个数据源是默认数据源
    @Primary
    public SqlSessionTemplate primarySqlSessionTemplate(
            @Qualifier("PrimarySqlSessionFactory") SqlSessionFactory sessionfactory) {
        return new SqlSessionTemplate(sessionfactory);
    }

}

```

注解说明:

**@MapperScan** ：配置mybatis的接口类放的地方

**@Primary** :表示使用的是默认数据库，这个一个要加，否则会因为不知道哪个数据库是默认数据库而报错 

**@ConfigurationProperties**：读取application.properties中的配置参数映射成为一个对象，其中prefix表示参数的前缀

大功告成~    ~  了吗？并没有，然后配置我们的第二个数据源的配置类,代码如下：

```java

@Configuration
@MapperScan(basePackages = "com.jdkcb.mybatisstuday.mapper.two", sqlSessionFactoryRef = "SecondarySqlSessionFactory")
public class SecondaryDataSourceConfig {

    @Bean(name = "SecondaryDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.secondary")
    public DataSource getSecondaryDataSource() {
        return DataSourceBuilder.create().build();
    }
    @Bean(name = "SecondarySqlSessionFactory")
    public SqlSessionFactory secondarySqlSessionFactory(@Qualifier("SecondaryDataSource") DataSource datasource)
            throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(datasource);
        bean.setMapperLocations(
                new PathMatchingResourcePatternResolver().getResources("classpath*:mapping/two/*.xml"));
        return bean.getObject();
    }
    @Bean("SecondarySqlSessionTemplate")
    public SqlSessionTemplate secondarySqlSessionTemplate(
            @Qualifier("SecondarySqlSessionFactory") SqlSessionFactory sessionfactory) {
        return new SqlSessionTemplate(sessionfactory);
    }

```

剩下的就是编写我们相应的xml文件和接口类了，代码如下:

```java
@Component
@Mapper
public interface PrimaryUserMapper {
    List<User> findAll();
}


@Component
@Mapper
public interface SecondaryUserMapper {
    List<User> findAll();
}
```

xml文件如下:

```java
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.jdkcb.mybatisstuday.mapper.one.PrimaryUserMapper">

    <select id="findAll" resultType="com.jdkcb.mybatisstuday.pojo.User">
                select * from sys_user;
    </select>
</mapper>


<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.jdkcb.mybatisstuday.mapper.two.SecondaryUserMapper">

    <select id="findAll" resultType="com.jdkcb.mybatisstuday.pojo.User">
                select * from sys_user2;
    </select>

</mapper>
```



> 注:其中xml文件在本实例中目录为：resources/mapping



### 2.测试

编写一个Controller用于测试，因为是测试实例且代码相对来说较为简单，所以这里就不写Service层了。

代码如下:

```java
@RestController
public class UserController {

    @Autowired
    private PrimaryUserMapper primaryUserMapper;
    @Autowired
    private SecondaryUserMapper secondaryUserMapper;
    @RequestMapping("primary")
    public Object primary(){
        List<User> list = primaryUserMapper.findAll();
        return list;
    }
    @RequestMapping("secondary")
    public Object secondary  (){
        List<User> list = secondaryUserMapper.findAll();
        return list;
    }
    

}
```

在浏览器分别输入:http://127.0.0.1:8080/primary 和 http://127.0.0.1:8080/secondary

结果如下:

```json
[{"user_id":1,"user_name":"张三","user_age":25}] //primary 
[{"user_id":1,"user_name":"李四","user_age":30}] //secondary
```

到此，Springboot结合mybatis配置多数据源就大功告成啦。

最后的最后，大家好，我是韩数，哼，关注我，有你好果子吃(叉腰)。

记得点个赞再走哦~

等一下：

> 相关源码欢迎去我的github下载(欢迎star)：
>
> 



