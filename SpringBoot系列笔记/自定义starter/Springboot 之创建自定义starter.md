## Springboot 之创建自定义starter

## 前言：

Springboot的出现极大的简化了开发人员的配置，而这之中的一大利器便是springboot的starter，starter是springboot的核心组成部分，springboot官方同时也为开发人员封装了各种各样方便好用的starter模块，例如：

- spring-boot-starter-web//spring MVC相关
- spring-boot-starter-aop //切面编程相关
- spring-boot-starter-cache //缓存相关

starter的出现极大的帮助开发者们从繁琐的框架配置中解放出来，从而更专注于业务代码，而springboot能做的不仅仅停留于此，当面对一些特殊的情况时，我们可以使用我们自定义的**springboot starter**。

在创建我们自定义的starter之前呢，我们先看看官方是怎么说的：

- **模块**

  在springboot官方文档中，特别提到，我们需要创建两个module ，其中一个是**autoconfigure module**  一个是 **starter module** ，其中 starter module 依赖 autoconfigure module

  但是，网上仍然有很多blog在说这块的时候其实会发现他们其实只用了一个module，这当然并没有错，这点官方也有说明：

  ```text
  You may combine the auto-configuration code and the dependency management in a single module if you do not need to separate those two concerns
  
  //如果不需要将自动配置代码和依赖项管理分离开来，则可以将它们组合到一个模块中。
  ```

  

- **命名规范**

   springboot 官方建议springboot官方推出的starter 以spring-boot-starter-xxx的格式来命名，第三方开发者自定义的starter则以xxxx-spring-boot-starter的规则来命名，事实上，很多开发者在自定义starter的时候往往会忽略这个东西(因为不看官方文档很难知道这件事情。同时也不会造成其他的后果，主要是显得不够专业)。



## 看看官方的starter

了解了这两点之后，那么下面让我们一块去探索spingboot starter的奥秘吧。

按照springboot官方给的思路，starter的核心module应该是autoconfigure，所以我们直接去看spring-boot-autoconfigure里面的内容。

当Spring Boot启动时，它会在类路径中查找名为spring.factories的文件。该文件位于META-INF目录中。打开spring.factories文件，文件内容太多了，为了避免我水篇幅，我们只看其中的一部分：

```factories
# Auto Configure
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
org.springframework.boot.autoconfigure.admin.SpringApplicationAdminJmxAutoConfiguration,\
org.springframework.boot.autoconfigure.aop.AopAutoConfiguration,\
org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration,\
org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration,\
org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration,\
```

我们可以发现一些比较眼熟的单词，比如Aop，Rabbit，Cache ，当springboot启动的时候，将会尝试加载这些配置类，如果该路径下存在该类的话，则将运行它，并初始化与该配置类相关的bean。

点开一个看看：

```java
@Configuration
@ConditionalOnClass({RabbitTemplate.class, Channel.class})
@EnableConfigurationProperties({RabbitProperties.class})
@Import({RabbitAnnotationDrivenConfiguration.class})
public class RabbitAutoConfiguration {
    
   //...代码略..
}
```

我们先来了解一下这几个注解:

**@ConditionalOnClass** :条件注解，当classpath下发现该类的情况下进行自动配置。

**@EnableConfigurationProperties**：外部化配置

**@Import** ：引入其他的配置类



当然，这并不是一种通用的套路，查看其他的配置类，我们会发现其标注的注解往往也是有所区别的。



## 自定义自己的starter

首先我们新建一个maven项目，引入以下依赖：

```xml
<dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-autoconfigure</artifactId>
        </dependency>
    </dependencies>
    <dependencyManagement>
        <!-- 我们是基于Springboot的应用 -->
        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>2.1.0.RELEASE</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
```

然后我们创建一个person类，用作后期我们测试的bean

```java
public class Person {

    //属性
    private int age;
    private String name;
    private String gender;
    
    /*此处省略getter and setter and toStering*/

}
```



然后我们也创建一个PersonConfigProperties来完成我们属性的注入

```java

@ConfigurationProperties(prefix = "mystarter.config.student")
public class PersonConfigProperties {


    private String name;
    private int age;
    private String gender;

    /*
    其他的配置信息。。。。
     */
    
   /*此处省略getter and setter and toStering*/
}

```



最后创建我们的自动配置类MyStarterAutoConfiguration.java

```java
@Configuration
@EnableConfigurationProperties(PersonConfigProperties.class)
@ConditionalOnClass(Person.class)
public class MyStarterAutoConfiguration {


    @Bean
    @ConditionalOnProperty(prefix = "mystarter.config", name = "enable", havingValue = "true")
    public Person defaultStudent(PersonConfigProperties personConfigProperties) {
        Person person = new Person();
        person.setAge(personConfigProperties.getAge());
        person.setName(personConfigProperties.getName());
        person.setGender(personConfigProperties.getGender());
        return person;
    }
}
```



我感觉这是不是做好了？

**我不要你觉得，我要我觉得**

最后我们最重要的一步：

在resourecs文件目录下创建META-INF，并创建我们自己的spring.factories，并把我们的 MyStarterAutoConfiguration添加进去

```text
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
com.jdkcb.mystarter.config.MyStarterAutoConfiguration
```

最后打包成jar包，在我们新的项目里面测试：

## 测试：

引入我们的starter，当然也可以在本地直接引入我们的my-spring-boot-starter项目

```xml
    <dependency>
            <groupId>com.jdkcb</groupId>
            <artifactId>my-spring-boot-starter</artifactId>
            <version>0.0.1-SNAPSHOT</version>
            <scope>system</scope>
            <systemPath>${project.basedir}/src/main/resources/lib/my-spring-boot-starter-0.0.1-SNAPSHOT.jar</systemPath>
        </dependency>
```

在application.properties配置文件中添加我们相应的配置

```properties
mystarter.config.enable=true
mystarter.config.person.name=小明
mystarter.config.person.age=5
mystarter.config.person.gender=男
```



新建一个测试的Controller：

```java
@RestController
public class TestController {

    @Autowired
    private Person person;

    @RequestMapping("/getPerson")
    private Person getStudent() {
        return person;
    }

}

```

启动项目，在浏览器地址栏输入 http://127.0.0.1:8080/getPerson ,结果如下

```json
{"age":5,"name":"小明","gender":"男"}
```

大功告成~

最后的最后，大家好，我是韩数，哼，关注我，有你好果子吃(叉腰)。

记得点个赞再走哦~