# Springboot学习笔记:嵌入式Web容器

## 前言：

## 版本

> 作者：韩数
>
> Github：https://github.com/hanshuaikang
>
> 微信公众平台:码上Marson
>
> 完成日期：2019-07-02日
>
> jdk：1.8
>
> springboot版本：2.1.6.RELEASE

## 致谢：

**小马哥： Java 微服务实践 - Spring Boot / Spring Cloud购买链接：**

https://segmentfault.com/ls/1650000011387052

## 电子版及相关代码下载（欢迎Star）:

Github：https://github.com/hanshuaikang/Spring-Note

微信公众号：**码上marson**

## 1.项目准备：

- 新建一个Springboot项目，包含最基础的Web组件
- 新建一个Controller包，新建HelloController作为我们的URL映射测试类

**HelloController.java**代码如下:

```java
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {



    @GetMapping("/")
    public String helloWord(){
        return "Hello World";
    }
}

```



## 2.嵌入式Servlet Web容器

**前言:**在Springboot中，Tomcat作为Springboot的一个组件由Spring驱动，支持jar包直接运行web项目简化了我们在本地工程环境下的开发流程。而Springboot当然作为一个灵活的框架，也为开发者们提供了其他的选择，如jetty，Undertow和Tomcat，已经2.0+版本新增的Netty Web Server。



### 1.使用Tomact作为Web容器

> 因为Springboot默认采用的就是Tomcat容器，所以如何配置Tomcat作为Web 容器这里不再多讲。

### 2.使用Jetty作为Web容器

Eclipse Jetty Web Server提供了一个HTTP服务器和Servlet容器，能够提供来自独立实例或嵌入式实例的静态和动态内容。从jetty-7开始，jetty web服务器和其他核心组件由Eclipse Foundation托管。jetty支持:

- 异步HTTP服务器

- 基于标准的Servlet容器（最新版本支持Servlet3.1规范）

- websocket服务器

- http / 2服务器

- 异步客户机(http/1.1、http/2、websocket)

- OSGI、JNDI、JMX、JASPI、AJP支持

如何从Tomcat切换到Jetty容器，Springboot官方文档中已经提供了方法，只需将spring-boot-starter-web依赖中的Tomcat依赖排除掉，引入spring-boot-starter-jetty依赖即可，xml代码如下:

```xml
 <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
            <exclusions>
                <!-- 排除Tomcat依赖项-->
                <exclusion>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-starter-tomcat</artifactId>
                </exclusion>
            </exclusions>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-jetty</artifactId>
        </dependency>

```

重启Springboot项目，访问http://127.0.0.1:8080/ ，屏幕上显示Hello World ，正常。



### 3.使用Undertow作为嵌入式Servlet Web容器：

Undertow 是红帽公司开发的一款**基于 NIO 的高性能 Web 嵌入式服务器**

Untertow 的特点：

- **轻量级**：它是一个 Web 服务器，但不像传统的 Web 服务器有容器概念，它由两个核心 Jar 包组成，加载一个 Web 应用可以小于 10MB 内存。

- **Servlet3.1 支持**：它提供了对 Servlet3.1 的支持。

- **WebSocket 支持**：对 Web Socket 完全支持，用以满足 Web 应用巨大数量的客户端。

- **嵌套性**：它不需要容器，只需通过 API 即可快速搭建 Web 服务器。



切换方式和Jetty一致，代码如下：

```xml
  <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
            <exclusions>
                <!-- Exclude the Tomcat dependency -->
                <exclusion>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-starter-tomcat</artifactId>
                </exclusion>
            </exclusions>
        </dependency>

        
        <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-undertow</artifactId>
        </dependency>
```



## 3.嵌入式Reactive Web 容器

**前言**：嵌入式Reactive Web 容器作为springboot2.0的新特性，一般来说并不是系统默认的选择。在某些情况下函数式编程的确会大大提高我们的开发效率。而jetty，tomcat，和undertow也响应的对Reactive做了支持。

### 1.使用Undertow作为嵌入式Reactive Web 容器:

注:当**spring-boot-starter-web**和**spring-boot-starter-webflux**同时存在时，**spring-boot-starter-webflux**实际上是会被默认忽略掉的，真正其作用的是**spring-boot-starter-web**，所以在使用**spring-boot-starter-webflux**的时候，我们需要把**spring-boot-starter-web**注释掉。修改后的pom文件如下。

```xml
 <dependencies>
        <!--<dependency>-->
            <!--<groupId>org.springframework.boot</groupId>-->
            <!--<artifactId>spring-boot-starter-web</artifactId>-->
            <!--<exclusions>-->
                <!--&lt;!&ndash; Exclude the Tomcat dependency &ndash;&gt;-->
                <!--<exclusion>-->
                    <!--<groupId>org.springframework.boot</groupId>-->
                    <!--<artifactId>spring-boot-starter-tomcat</artifactId>-->
                <!--</exclusion>-->
            <!--</exclusions>-->
        <!--</dependency>-->
        <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-undertow</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webflux</artifactId>
        </dependency>
    </dependencies>

```

现在新增一个MyRoute类，通过函数式编程我们编写一个helloword路由:

```java
@Configuration
public class MyRoute {

    @Bean
    public RouterFunction<ServerResponse> helloWord(){
        return route(GET("/"),
                request -> ok().body(Mono.just("helloworld"),String.class)
        );
    }
}

```

重启Springboot项目，访问http://127.0.0.1:8080/ ，屏幕上显示helloworld ，正常。

### 2.使用jetty作为嵌入式Reactive Web 容器:

同理，只需要简单修改pom文件就可以了。

```xml
 <dependencies>
        <!--<dependency>-->
            <!--<groupId>org.springframework.boot</groupId>-->
            <!--<artifactId>spring-boot-starter-web</artifactId>-->
            <!--<exclusions>-->
                <!--&lt;!&ndash; Exclude the Tomcat dependency &ndash;&gt;-->
                <!--<exclusion>-->
                    <!--<groupId>org.springframework.boot</groupId>-->
                    <!--<artifactId>spring-boot-starter-tomcat</artifactId>-->
                <!--</exclusion>-->
            <!--</exclusions>-->
        <!--</dependency>-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-jetty</artifactId>
        </dependency>
     
         <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webflux</artifactId>
        </dependency>
    </dependencies>

```

### 3.使用Tomcat作为嵌入式Reactive Web 容器:

```xml
 <dependencies>
        <!--<dependency>-->
            <!--<groupId>org.springframework.boot</groupId>-->
            <!--<artifactId>spring-boot-starter-web</artifactId>-->
            <!--<exclusions>-->
                <!--&lt;!&ndash; Exclude the Tomcat dependency &ndash;&gt;-->
                <!--<exclusion>-->
                    <!--<groupId>org.springframework.boot</groupId>-->
                    <!--<artifactId>spring-boot-starter-tomcat</artifactId>-->
                <!--</exclusion>-->
            <!--</exclusions>-->
        <!--</dependency>-->

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webflux</artifactId>
        </dependency>
         <dependency>
        <groupId>org.springframework.boot</groupId>
          <artifactId>spring-boot-starter-tomcat</artifactId>
      </dependency>
 </dependencies>
```



## 4.总结

本文较为简单的记录了springboot开发中不同Web 容器的切换，并简单的阐述了每个容器的特点，就像恋爱一样，合适才是最好的，本系列文章属于Spring学习笔记中boot部分的学习笔记，markdown笔记文件以及代码会陆续开源到Github上，欢迎大家下载和点Star，如果笔记中有疏漏或者错误的地方，还请各位大佬批评改正。