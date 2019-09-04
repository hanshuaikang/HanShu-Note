# Hystrix源码解析(一)：快速上手

## 版本：

> 作者：韩数
>
> Github：https://github.com/hanshuaikang
>
> 完成日期：2019-07-01日
>
> jdk：1.8
>
> springboot版本：2.1.3.RELEASE
>
> SpringCould版本：Greenwich.SR1

## 声明：

身为一个刚入门的计算机菜佬，阅读源码自然离不开优秀参考书籍和视频的引导，本篇文章的分析过程中"严重"借鉴了 **翟永超** 前辈的《SpringCloud微服务实战》这本书籍，在这里也向准备学习微服务的小伙伴们强烈推荐这本书，大家可以把这篇文章理解为《SpringCloud微服务实战》Ribbon部分的精简版和电子版，因为个人水平的原因，很多问题不敢妄下定论，以免误人子弟，所有书上很多内容都是精简过后直接放上去的，由于SpringCloud已经迭代到了Greenwich.SR1版本，Ribbon也和书上有了略微的差别，本篇文章的源码采用的是Ribbon最新版本，同时，因为时间原因，有很多额外的子类实现并没有完全顾上，例如PredicateBasedRule类的ZoneAvoidanceRule和AvailabilityFilteringRule 感兴趣的读者可以买《SpringCloud微服务实战》这本书细看，同时强烈推荐小马哥的微服务直播课系列《小马哥微服务实战》。

## 致谢：

**翟永超**：博客地址：

http://blog.didispace.com/aboutme/

**小马哥： Java 微服务实践 - Spring Boot / Spring Cloud购买链接：**

https://segmentfault.com/ls/1650000011387052

## 电子版及相关代码下载（欢迎Star）

Github：https://github.com/hanshuaikang/Spring-Note

微信公众号：码上marson



## Hystrix简介:

> 在分布式环境中，许多服务依赖关系中的一些必然会失败。Hystrix是一个库，它通过添加延迟容忍和容错逻辑来帮助您控制这些分布式服务之间的交互。Hystrix通过隔离服务之间的访问点、停止跨服务的级联故障并提供回退选项来实现这一点，所有这些选项都提高了系统的总体弹性。
>
> 

## 快速上手:

### 1. 引入hystrix依赖

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
     <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
</dependency>
```

注：本实例中，除Hystrix之外，Eureka,Ribbon和Config均已悉数配置完成，如果不知道如何配置Eureka和Ribbon的小伙伴，可以参考我之前的相关笔记。部分组件配置如下。

**Eureka配置:**

```properties
## Spring Cloud Eureka 服务器应用名称
spring.application.name = eureka-server

## Spring Cloud Eureka 服务器服务端口
server.port = 10001

## 管理端口安全失效
management.endpoints.web.exposure.include=*

## Spring Cloud Eureka 服务器作为注册中心
## 通常情况下，不需要再注册到其他注册中心去
## 同时，它也不需要获取客户端信息
### 取消向注册中心注册
eureka.client.register-with-eureka = false
### 取消向注册中心获取注册信息（服务、实例信息）
eureka.client.fetch-registry = false
## 解决 Peer / 集群 连接问题
eureka.instance.hostname = localhost
eureka.client.serviceUrl.defaultZone = http://${eureka.instance.hostname}:${server.port}/eureka
```

**Ribbon配置**

```properties
# 服务端口
server.port = 8080
## 用户 Ribbon 客户端应用
spring.application.name = user-robbon-client

## 配置客户端应用关联的应用
## spring.cloud.config.name 是可选的
## 如果没有配置，采用 ${spring.application.name}
spring.cloud.config.name = user-service
## 关联 profile
spring.cloud.config.profile = default
## 关联 label
spring.cloud.config.label = master
## 激活 Config Server 服务发现
spring.cloud.config.discovery.enabled = true
## Config Server 服务器应用名称
spring.cloud.config.discovery.serviceId = config-server
## Spring Cloud Eureka 客户端 注册到 Eureka 服务器
eureka.client.serviceUrl.defaultZone = http://localhost:10001/eureka

spring.cloud.config.uri = http://127.0.0.1:7070/
## 扩展 IPing 实现

user-service-provider.ribbon.NFLoadBalancerPingClassName = \
  com.jdkcb.user.ribbon.client.ping.MyPing
  
  
```

**config配置:**

```properties
## Spring Cloud Config Server 应用名称
spring.application.name = config-server

## 服务器服务端口
server.port = 7070

## 管理端口安全失效
management.endpoints.web.exposure.include=*

## Spring Cloud Eureka 客户端 注册到 Eureka 服务器
eureka.client.serviceUrl.defaultZone = http://localhost:10001/eureka


### 配置服务器文件系统git 仓库
### ${user.dir} 减少平台文件系统的不一致
### 目前 ${user.dir}/config-server/src/main/resources/configs
spring.cloud.config.server.git.uri = ${user.dir}/config-server/src/main/resources/configs
```

**user-services.properties**:

```properties
## 提供方服务名称
provider.service.name = user-service-provider
## 提供方服务主机
provider.service.host = localhost
## 提供方服务端口
provider.service.port = 9090

user.service.name = ${provider.service.name}
```





### 2.通过@EnableHystrix注解激活服务提供方短路

```java
@SpringBootApplication
@EnableHystrix
@EnableDiscoveryClient
public class UserServiceProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceProviderApplication.class, args);
    }
}

```

### 3.通过`@HystrixCommand`实现服务提供方短路。

修改服务提供方(user-service-provider)项目的 UserServiceProviderController的findAll()方法，在方法上添加@HystrixCommand注解，并设置参数如下：

```java
 @HystrixCommand(
            commandProperties = { // Command 配置
                    // 设置操作时间为 100 毫秒
                    @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "100")
            },
            fallbackMethod = "fallbackForGetUsers" // 设置 fallback 方法
    )
    // 通过方法继承，URL 映射 ："/user/find/all"
    @Override
    public List<User> findAll() {
        return userService.findAll();
    }


    /**
     * {@link #getUsers()} 的 fallback 方法
     *
     * @return 空集合
     */
    public List<User> fallbackForGetUsers() {
        return Collections.emptyList();
    }

```

功能:当服务请求方发来一个请求时，如果findAll路由响应时间在100毫秒内，则返回findAll的内容，如果100毫秒后发现还是没有及时响应，则返回我们之前有设置好的fallbackForGetUsers方法所提供的内容。



### 4.使用`@EnableCircuitBreaker` 实现服务调用方短路：

```java
@RibbonClient("user-service-provider") // 指定目标应用名称
@EnableCircuitBreaker // 使用服务短路
@EnableFeignClients(clients = UserService.class)
@EnableDiscoveryClient
@SpringBootApplication
@EnableBinding(UserMessage.class)
public class UserRibbonClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserRibbonClientApplication.class, args);
    }
    
        
    /**
    激活具有负载能力的RestTemplate
    */
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }


    //自定义Rule
    @Bean
    public IRule myRule() {
        return new MyRule();
    }


}

```

### 5. 服务调用：

```java
    /**
     * 调用 user-service-provider "/user/list" REST 接口，并且直接返回内容
     * 增加 短路功能
     */
    @GetMapping("/user-service-provider/user/list")
    public Collection<User> getUsersList() {
        return restTemplate.getForObject("http://" + providerServiceName + "/user/list", Collection.class);
    }
```

运行程序：我们可以看到，当我们访问服务调用方的**/user-service-provider/user/list**路由时，如果**user-service-provider**的**/user/list**一百毫秒内完成响应，则会返回一个我们之前有添加内容的集合，否则，返回一个空的集合。

### 6.使用编程方式自定义短路实现：

```java
public class UserRibbonClientHystrixCommand extends HystrixCommand<Collection> {

    //定义providerServiceName:服务名
    private final String providerServiceName;
     //定义 RestTemplate发送请求
    private final RestTemplate restTemplate;

    //构造函数
    public UserRibbonClientHystrixCommand(String providerServiceName, RestTemplate restTemplate) {
        super(HystrixCommandGroupKey.Factory.asKey(
                "User-Ribbon-Client"),
                100);//设置超时时间
        this.providerServiceName = providerServiceName;
        this.restTemplate = restTemplate;
    }

    /**
     * 主逻辑实现
     *
     * @return
     * @throws Exception
     */
    @Override
    protected Collection run() throws Exception {
        return restTemplate.getForObject("http://" + providerServiceName + "/user/list", Collection.class);
    }

    /**
     * Fallback 实现
     *
     * @return 空集合
     */
    protected Collection getFallback() {
        return Collections.emptyList();
    }

}

```

通过编程方法实现短路的时候，前台调用的方式也要相应的做出调整:

```java
    @GetMapping("/user-service-provider/user/list")
    public Collection<User> getUsersList() {
        return new UserRibbonClientHystrixCommand(providerServiceName, restTemplate).execute();
    }
```

### 7.服务监控Hystrix Dashboard仪表盘：

功能:用来监控Hystrix的各项指标信息，通过Hystrix Dashboard反馈的实时信息,帮助我们更容易找到程序中的问题，该组件目前已经被弃用。

新建一个项目**"hystrix-ashboard"** 

#### 1.引入依赖：

```xml
<dependency>
  <groupId>org.springframework.cloud</groupId>
   <artifactId>spring-cloud-netflix-hystrix-dashboard</artifactId>
</dependency>
```

#### 2.使用@EnableHystrixDashboard注解激活Hystrix Dashboard仪表盘

```java
@SpringBootApplication
@EnableHystrixDashboard
public class HystrixDashboardApplication {

    public static void main(String[] args) {
        SpringApplication.run(HystrixDashboardApplication.class, args);
    }

}
```

#### 3.配置该项目：

```properties
## Hystrix Dashboard 应用
spring.application.name = hystrix-dashboard
## 服务端口
server.port = 10000
```

hystrix作为一个服务容错保护组件，可以避免因为请求得不到及时响应而可能出现的大量请求挤压，甚至引发雪崩效应的情况，适得一个服务不可用之后直接熔断服务，而不至于导致整个分布式应用都受到影响。





