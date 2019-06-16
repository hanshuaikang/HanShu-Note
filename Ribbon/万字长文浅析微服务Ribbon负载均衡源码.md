# 万字长文浅析微服务Ribbon负载均衡源码

# 前言

## 版本

> 作者：韩数
>
> Github：https://github.com/hanshuaikang
>
> 完成日期：2019-06-16日
>
> jdk：1.8
>
> springboot版本：2.1.3.RELEASE
>
> SpringCould版本：Greenwich.SR1

## 声明：

身为一个刚入门的计算机菜佬，阅读源码自然离不开优秀参考书籍和视频的引导，本篇文章的分析过程中"严重"借鉴了 **翟永超** 前辈的《SpringCloud微服务实战》这本书籍，在这里也向准备学习微服务的小伙伴们强烈推荐这本书，大家可以把这篇文章理解为《SpringCloud微服务实战》Ribbon部分的精简版和电子版，因为个人水平的原因，很多问题不敢妄下定论，以免误人子弟，所有书上很多内容都是精简过后直接放上去的，由于SpringCloud已经迭代到了Greenwich.SR1版本，Ribbon也和书上有了略微的差别，本篇文章的源码采用的是Ribbon最新版本，同时，因为时间原因，有很多额外的子类实现并没有完全顾上，例如PredicateBasedRule类的ZoneAvoidanceRule和AvailabilityFilteringRule 感兴趣的读者可以买《SpringCloud微服务实战》这本书细看，同时强烈推荐小马哥的微服务直播课系列《小马哥微服务实战》。

## 致谢

**翟永超**：博客地址：

http://blog.didispace.com/aboutme/

**小马哥： Java 微服务实践 - Spring Boot / Spring Cloud购买链接：**

https://segmentfault.com/ls/1650000011387052

## 电子版及相关代码下载（欢迎Star）

Github：https://github.com/hanshuaikang/Spring-Note

微信公众号：码上marson



# 快速上手：

## 配置负载均衡

当使用Eureka时，须做如下配置

```properties
## 服务提供方
spring.application.name = spring-cloud-ribbon-client

### 服务端口
server.port = 8080

### 管理安全失效
management.endpoints.web.exposure.include=*

### 暂时性关闭 Eureka 注册
## 当使用 Eureka 服务发现时，请注释掉一下配置
# eureka.client.enabled = false

## 连接 Eureka Sever
eureka.client.serviceUrl.defaultZone = http://localhost:10000/eureka/
eureka.client.registryFetchIntervalSeconds = 5

### 服务提供方主机
serivce-provider.host = localhost
### 服务提供方端口
serivce-provider.port = 9090

serivce-provider.name = spring-cloud-service-provider

```



当不适用Eureka的时候，需要配置如下

```properties
### 配置ribbon 服务地提供方
## 当使用 Eureka 服务发现时，请注释掉一下配置
# spring-cloud-service-provider.ribbon.listOfServers = \
#http://${serivce-provider.host}:${serivce-provider.port}
```





## 激活负载均衡

```java
@SpringBootApplication
@RibbonClients({
        @RibbonClient(name = "spring-cloud-service-provider")
})
@EnableDiscoveryClient
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    //声明 RestTemplate
    @LoadBalanced // RestTemplate 的行为变化
    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }

}
```



## 测试发送请求



```java
 return restTemplate.postForObject("http://" +
                        serviceProviderName +
                        "/greeting",
                user, String.class);
```



# 初探Ribbon源码

## LoadBalancerClient 类

在Spring 中 ，当服务消费端去调用服务提供者的服务的时候，已经封装了一个模板类，叫做RestTemplate.那么Ribbon 又是如何通过RestTemplate来实现负载均衡的呢？

**线索**：  **@LoadBalanced** 注解:

```java
# Annotation to mark a RestTemplate bean to be configured to use a LoadBalancerClient.
  注释，用于标记要配置为使用LoadBalancerClient的RestTemplate bean。
```

### ServiceInstanceChooser接口

```java
public interface ServiceInstanceChooser {

	/**
	 * 从LoadBalancer中为指定的服务选择一个ServiceInstance。
	 * @param serviceId是查找LoadBalancer的服务ID。
	 * @return 一个与serviceId匹配的ServiceInstance。
	 */
	ServiceInstance choose(String serviceId);

}
```



**ServiceInstance choose(String serviceId)** ：根据serviceId 去选择一个对应服务的实例



### **LoadBalancerClient** **类**：

LoadBalancerClient 代码：

```java
package org.springframework.cloud.client.loadbalancer;

import java.io.IOException;
import java.net.URI;
import org.springframework.cloud.client.ServiceInstance;

public interface LoadBalancerClient extends ServiceInstanceChooser {
    <T> T execute(String serviceId, LoadBalancerRequest<T> request) throws IOException;

    <T> T execute(String serviceId, ServiceInstance serviceInstance, LoadBalancerRequest<T> request) throws IOException;

    URI reconstructURI(ServiceInstance instance, URI original);
}
```



 **<T> T execute(String serviceId, ServiceInstance serviceInstance, LoadBalancerRequest<T> request)**

  使用指定的LoadBalancer中的ServiceInstance执行请求

   **serviceInstance **   ： 要执行请求的服务

  **<T> T execute(String serviceId, LoadBalancerRequest<T> request)**  :

   使用从负载均衡器中挑选出来的服务实例来执行请求内容。

  **URI reconstructURI(ServiceInstance instance, URI original);**

 返回一个 一 个 host:port 形式的URL对象用于我们最后像服务端发送请求的地址。而具体的host,port等信息

 则从 instance参数中获取。

### ServiceInstance 类

```java
public interface ServiceInstance {
    default String getInstanceId() {
        return null;
    }

    String getServiceId();

    String getHost();

    int getPort();

    boolean isSecure();

    URI getUri();

    Map<String, String> getMetadata();

    default String getScheme() {
        return null;
    }
}
```



## LoadBalancerAutoConfiguration类

作用：Ribbon 的自动化配置类代码（部分）：

```java
@Configuration
@ConditionalOnClass({RestTemplate.class})
@ConditionalOnBean({LoadBalancerClient.class})
@EnableConfigurationProperties({LoadBalancerRetryProperties.class})
public class LoadBalancerAutoConfiguration {
    @LoadBalanced
    @Autowired(
        required = false
    )
    private List<RestTemplate> restTemplates = Collections.emptyList();
    @Autowired(
        required = false
    )
    private List<LoadBalancerRequestTransformer> transformers = Collections.emptyList();

    public LoadBalancerAutoConfiguration() {
    }

    @Bean
    public SmartInitializingSingleton loadBalancedRestTemplateInitializerDeprecated(final ObjectProvider<List<RestTemplateCustomizer>> restTemplateCustomizers) {
        return () -> {
            restTemplateCustomizers.ifAvailable((customizers) -> {
                Iterator var2 = this.restTemplates.iterator();

                while(var2.hasNext()) {
                    RestTemplate restTemplate = (RestTemplate)var2.next();
                    Iterator var4 = customizers.iterator();

                    while(var4.hasNext()) {
                        RestTemplateCustomizer cutomizer = (RestTemplateCustomizer)var4.next();
                        customizer.customize(restTemplate);
                    }
                }

            });
        };
    }

    
   #中间一大段代码略
    
    
        @Bean
        public LoadBalancerInterceptor ribbonInterceptor(LoadBalancerClient loadBalancerClient, LoadBalancerRequestFactory requestFactory) {
            return new LoadBalancerInterceptor(loadBalancerClient, requestFactory);
        }

        @Bean
        @ConditionalOnMissingBean
        public RestTemplateCustomizer restTemplateCustomizer(final LoadBalancerInterceptor loadBalancerInterceptor) {
            return (restTemplate) -> {
                List<ClientHttpRequestInterceptor> list = new ArrayList(restTemplate.getInterceptors());
                list.add(loadBalancerInterceptor);
                restTemplate.setInterceptors(list);
            };
        }
    }
}

```



**@ConditionalOnClass({RestTemplate.class})**  ： RestTemplate必须位于当前的工程环境中

**@ConditionalOnBean({LoadBalancerClient.class})**   ：工程中必须存在实现LoadBalancerClient的**Bean**

```java
@LoadBalanced
@Autowired(
    required = false
)

private List<RestTemplate> restTemplates = Collections.emptyList();
```
**private List<RestTemplate> restTemplates = Collections.emptyList();**

维护一个被@LoadBalanced的修饰的RestTemplate实例列表。

```java
   @Bean
        public LoadBalancerInterceptor ribbonInterceptor(LoadBalancerClient loadBalancerClient, LoadBalancerRequestFactory requestFactory) {
            return new LoadBalancerInterceptor(loadBalancerClient, requestFactory);
  }

```

创建一个拦截器 LoadBalancerInterceptor，用于在发起请求的时候进行拦截。

```java
 @Bean
        @ConditionalOnMissingBean
        public RestTemplateCustomizer restTemplateCustomizer(final LoadBalancerInterceptor loadBalancerInterceptor) {
            return (restTemplate) -> {
                List<ClientHttpRequestInterceptor> list = new ArrayList(restTemplate.getInterceptors());
                list.add(loadBalancerInterceptor);
                restTemplate.setInterceptors(list);
            };
        }
    }
}

```

为RestTemplate实例列表的请求restTemplate添加一个LoadBalancerInterceptor拦截器。



##  LoadBalancerInterceptor  类

作用:拦截RestTemplate请求，实现负载均衡

```java
public class LoadBalancerInterceptor implements ClientHttpRequestInterceptor {

	private LoadBalancerClient loadBalancer;

	private LoadBalancerRequestFactory requestFactory;

	public LoadBalancerInterceptor(LoadBalancerClient loadBalancer,
			LoadBalancerRequestFactory requestFactory) {
		this.loadBalancer = loadBalancer;
		this.requestFactory = requestFactory;
	}

	public LoadBalancerInterceptor(LoadBalancerClient loadBalancer) {
		// for backwards compatibility
		this(loadBalancer, new LoadBalancerRequestFactory(loadBalancer));
	}

    
	@Override
	public ClientHttpResponse intercept(final HttpRequest request, final byte[] body,
			final ClientHttpRequestExecution execution) throws IOException {
		final URI originalUri = request.getURI();
		String serviceName = originalUri.getHost();
		Assert.state(serviceName != null,
				"Request URI does not contain a valid hostname: " + originalUri);
		return this.loadBalancer.execute(serviceName,
				this.requestFactory.createRequest(request, body, execution));
	}

}

```



#当一个被@LoadBalanced修饰过的RestTemplate对象发送请求时，会被 LoadBalancerInterceptor拦截，通过request拿到URL，通过URL拿到服务名，最后再选择对应的实例发起请求。



##  RibbonLoadBalancerClient 类

作用:LoadBalancerClient 接口的具体实现

```java
public class RibbonLoadBalancerClient implements LoadBalancerClient {
    private SpringClientFactory clientFactory;
    
    @Override
	public <T> T execute(String serviceId, LoadBalancerRequest<T> request)
			throws IOException {
		return execute(serviceId, request, null);
	}

   
    public <T> T execute(String serviceId, LoadBalancerRequest<T> request, Object hint)
			throws IOException {
		ILoadBalancer loadBalancer = getLoadBalancer(serviceId);
		Server server = getServer(loadBalancer, hint);
		if (server == null) {
			throw new IllegalStateException("No instances available for " + serviceId);
		}
		RibbonServer ribbonServer = new RibbonServer(serviceId, server,
				isSecure(server, serviceId),
				serverIntrospector(serviceId).getMetadata(server));

		return execute(serviceId, ribbonServer, request);
	}

    

    public <T> T execute(String serviceId, ServiceInstance serviceInstance, LoadBalancerRequest<T> request) throws IOException {
        Server server = null;
        if (serviceInstance instanceof RibbonLoadBalancerClient.RibbonServer) {
            server = ((RibbonLoadBalancerClient.RibbonServer)serviceInstance).getServer();
        }

        if (server == null) {
            throw new IllegalStateException("No instances available for " + serviceId);
        } else {
            RibbonLoadBalancerContext context = this.clientFactory.getLoadBalancerContext(serviceId);
            RibbonStatsRecorder statsRecorder = new RibbonStatsRecorder(context, server);

            try {
                T returnVal = request.apply(serviceInstance);
                statsRecorder.recordStats(returnVal);
                return returnVal;
            } catch (IOException var8) {
                statsRecorder.recordStats(var8);
                throw var8;
            } catch (Exception var9) {
                statsRecorder.recordStats(var9);
                ReflectionUtils.rethrowRuntimeException(var9);
                return null;
            }
        }
    }
    
    
    protected Server getServer(ILoadBalancer loadBalancer) {
        return this.getServer(loadBalancer, (Object)null);
    }

    protected Server getServer(ILoadBalancer loadBalancer, Object hint) {
        return loadBalancer == null ? null : loadBalancer.chooseServer(hint != null ? hint : "default");
    }
 
    
    }
```



 注:

> 到此处代码和SpringCloud微服务实战书中版本的源码已经有了些许不同，实现上更加高效了。

首先通过默认的execute实现将参数传递到第二个  

**public <T> T execute(String serviceId, LoadBalancerRequest<T> request, Object hint)**

在第二个方法我们发现根据serviceId获取了对应的服务实例，并且封装到了RibbonServer对象中。

最终交付到第三个方法

 **public <T> T execute(String serviceId, ServiceInstance serviceInstance, LoadBalancerRequest<T> request)**

完成具体的执行操作。 



同时可以发现getServer的参数并不是根据之前的LoadBalancerClient的choose方法，而是使用了Ribbon本身ILoadBalancer接口定义的函数。

```java
protected Server getServer(ILoadBalancer loadBalancer, Object hint) {
		if (loadBalancer == null) {
			return null;
		}
		// Use 'default' on a null hint, or just pass it on?
		return loadBalancer.chooseServer(hint != null ? hint : "default");
	}
```



**一探究竟:**

## ILoadBalancer : 接口

```java
public interface ILoadBalancer {

    //向负载均衡器中维护的服务列表中添加新的服务实例
	public void addServers(List<Server> newServers);
	
    //通过某种策略，选择一个服务实例
	public Server chooseServer(Object key);
	//用来标识某个服务已经停止服务
	public void markServerDown(Server server);
	//获取当前服务器列表。如果availableOnly为true的话，将会返回活跃的服务列表
	@Deprecated
	public List<Server> getServerList(boolean availableOnly);

    //只返回正在启动的可返回的服务列表
    public List<Server> getReachableServers();
    //返回所有已知的服务列表
	public List<Server> getAllServers();
}

```





通过查看ILoadBalancer 的具体实现得知

ILoadBalancer  ->  **BaseLoadBalancer**(基础实现) ->**DynamicServerListLoadBalancer**（扩展实现）

->**ZoneAwareLoadBalancer**(扩展实现)



那Ribbon默认使用的哪种实现呢？

```java
@Configuration
@EnableConfigurationProperties
// Order is important here, last should be the default, first should be optional
// see
// https://github.com/spring-cloud/spring-cloud-netflix/issues/2086#issuecomment-316281653
@Import({ HttpClientConfiguration.class, OkHttpRibbonConfiguration.class,
      RestClientRibbonConfiguration.class, HttpClientRibbonConfiguration.class })
public class RibbonClientConfiguration {
    
    
    @Bean
	@ConditionalOnMissingBean
	public ILoadBalancer ribbonLoadBalancer(IClientConfig config,
			ServerList<Server> serverList, ServerListFilter<Server> serverListFilter,
			IRule rule, IPing ping, ServerListUpdater serverListUpdater) {
		if (this.propertiesFactory.isSet(ILoadBalancer.class, name)) {
			return this.propertiesFactory.get(ILoadBalancer.class, config, name);
		}
		return new ZoneAwareLoadBalancer<>(config, rule, ping, serverList,
				serverListFilter, serverListUpdater);
	}
    
    
    
}
```

通过查看Ribbon的配置类，我们发现Ribbon默认采用的是ZoneAwareLoadBalancer实现



现在回到具体的RibbonLoadBalancerClient 类的execute方法中，可以大概知道Ribbon负载均衡的一个简单的流程，即

getServer方法**->**ZoneAwareLoadBalancer的chooseServer方法获取一个具体的服务实例

->包装成一个RibbonServer对象

->LoadBalancerRequest的apply向一个具体的实例发送一个请求。



## ServiceInstance 接口

```java
public interface ServiceInstance {

	default String getInstanceId() {
		return null;
	}

	String getServiceId();

	String getHost();

	int getPort();

	boolean isSecure();

	URI getUri();

	Map<String, String> getMetadata();
    
	default String getScheme() {
		return null;
	}

}

```



## ServiceInstance 的具体实现RibbonServer类

包含了server对象，服务名，是否使用https等标识。

```java
public static class RibbonServer implements ServiceInstance {

		private final String serviceId;

		private final Server server;

		private final boolean secure;

		private Map<String, String> metadata;

		public RibbonServer(String serviceId, Server server) {
			this(serviceId, server, false, Collections.emptyMap());
		}

		public RibbonServer(String serviceId, Server server, boolean secure,
				Map<String, String> metadata) {
			this.serviceId = serviceId;
			this.server = server;
			this.secure = secure;
			this.metadata = metadata;
		}

		@Override
		public String getInstanceId() {
			return this.server.getId();
		}

		@Override
		public String getServiceId() {
			return this.serviceId;
		}

		@Override
		public String getHost() {
			return this.server.getHost();
		}

		@Override
		public int getPort() {
			return this.server.getPort();
		}

		@Override
		public boolean isSecure() {
			return this.secure;
		}

		@Override
		public URI getUri() {
			return DefaultServiceInstance.getUri(this);
		}

		@Override
		public Map<String, String> getMetadata() {
			return this.metadata;
		}

		public Server getServer() {
			return this.server;
		}

		@Override
		public String getScheme() {
			return this.server.getScheme();
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder("RibbonServer{");
			sb.append("serviceId='").append(serviceId).append('\'');
			sb.append(", server=").append(server);
			sb.append(", secure=").append(secure);
			sb.append(", metadata=").append(metadata);
			sb.append('}');
			return sb.toString();
		}

	}

}
```



把思路回到LoadBalancerClient接口的apply方法上，然后突然发现，之前SpringCloud微服务书上的实现早已不同，通过查看接口的实现关系，发现最终apply方法是 AsyncLoadBalancerInterceptor类来完成具体的实现的。

##  AsyncLoadBalancerInterceptor类



```java
public class AsyncLoadBalancerInterceptor implements AsyncClientHttpRequestInterceptor {
    private LoadBalancerClient loadBalancer;

    public AsyncLoadBalancerInterceptor(LoadBalancerClient loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    public ListenableFuture<ClientHttpResponse> intercept(final HttpRequest request, final byte[] body, final AsyncClientHttpRequestExecution execution) throws IOException {
        URI originalUri = request.getURI();
        String serviceName = originalUri.getHost();
        return (ListenableFuture)this.loadBalancer.execute(serviceName, new LoadBalancerRequest<ListenableFuture<ClientHttpResponse>>() {
            public ListenableFuture<ClientHttpResponse> apply(final ServiceInstance instance) throws Exception {
                HttpRequest serviceRequest = new ServiceRequestWrapper(request, instance, AsyncLoadBalancerInterceptor.this.loadBalancer);
                return execution.executeAsync(serviceRequest, body);
            }
        });
    }
}
```

由于官方代码并没有提供注释说明这个类的具体作用，通过类名称大概可以猜出为一个异步的负载均衡拦截器，拦截Restplate请求，并实现apply方法向一个具体的实例发送请求。

具体执行的代码

```java
HttpRequest serviceRequest = new ServiceRequestWrapper(request,
								instance, AsyncLoadBalancerInterceptor.this.loadBalancer);
						return execution.executeAsync(serviceRequest, body);
```

发现具体实现的时候，还传入了一个ServiceRequestWrapper对象。



## ServiceRequestWrapper类



```java
public class ServiceRequestWrapper extends HttpRequestWrapper {

	private final ServiceInstance instance;

	private final LoadBalancerClient loadBalancer;

	public ServiceRequestWrapper(HttpRequest request, ServiceInstance instance,
			LoadBalancerClient loadBalancer) {
		super(request);
		this.instance = instance;
		this.loadBalancer = loadBalancer;
	}

	@Override
	public URI getURI() {
		URI uri = this.loadBalancer.reconstructURI(this.instance, getRequest().getURI());
		return uri;
	}

```



可以发现这个类继承了HttpRequestWrapper 类，并且重写了getURI()方法，同时在 getURI() 方法中，具体采纳了RibbonLoadBalancerClient 的reconstructURI方法来组织具体请求的URL实例地址。

```java
@Override
	public URI reconstructURI(ServiceInstance instance, URI original) {
		Assert.notNull(instance, "instance can not be null");
		String serviceId = instance.getServiceId();
		RibbonLoadBalancerContext context = this.clientFactory
				.getLoadBalancerContext(serviceId);

		URI uri;
		Server server;
		if (instance instanceof RibbonServer) {
			RibbonServer ribbonServer = (RibbonServer) instance;
			server = ribbonServer.getServer();
			uri = updateToSecureConnectionIfNeeded(original, ribbonServer);
		}
		else {
			server = new Server(instance.getScheme(), instance.getHost(),
					instance.getPort());
			IClientConfig clientConfig = clientFactory.getClientConfig(serviceId);
			ServerIntrospector serverIntrospector = serverIntrospector(serviceId);
			uri = updateToSecureConnectionIfNeeded(original, clientConfig,
					serverIntrospector, server);
		}
		return context.reconstructURIWithServer(server, uri);
	}
```



而在**reconstructURIWithServer**方法中，我们可以发现这样一个执行逻辑，首先从Server对象中获得Host和port信息，然后从URI original对象中，获取其他的请求信息，最终拼接成要访问的具体的实例地址。

```java
  public URI reconstructURIWithServer(Server server, URI original) {
        String host = server.getHost();
        int port = server.getPort();
        String scheme = server.getScheme();
        
        if (host.equals(original.getHost()) 
                && port == original.getPort()
                && scheme == original.getScheme()) {
            return original;
        }
        if (scheme == null) {
            scheme = original.getScheme();
        }
        if (scheme == null) {
            scheme = deriveSchemeAndPortFromPartialUri(original).first();
        }

        try {
            StringBuilder sb = new StringBuilder();
            sb.append(scheme).append("://");
            if (!Strings.isNullOrEmpty(original.getRawUserInfo())) {
                sb.append(original.getRawUserInfo()).append("@");
            }
            sb.append(host);
            if (port >= 0) {
                sb.append(":").append(port);
            }
            sb.append(original.getRawPath());
            if (!Strings.isNullOrEmpty(original.getRawQuery())) {
                sb.append("?").append(original.getRawQuery());
            }
            if (!Strings.isNullOrEmpty(original.getRawFragment())) {
                sb.append("#").append(original.getRawFragment());
            }
            URI newURI = new URI(sb.toString());
            return newURI;            
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
```



# 负载均衡器

## AbstractLoadBalancer 类

```java
import java.util.List;

public abstract class AbstractLoadBalancer implements ILoadBalancer {
    
    //一个关于服务实例的分组枚举类，定义了三种不同的级别
    public enum ServerGroup{
        ALL,
        STATUS_UP,
        STATUS_NOT_UP        
    }
        
    /**
     * 选择一个服务实例，key为null，忽略key的条件判断
     */
    public Server chooseServer() {
    	return chooseServer(null);
    }

    /**
     * 根据不同的分组类型来选择返回不同的服务实例的列表
     */
    public abstract List<Server> getServerList(ServerGroup serverGroup);
    
    /**
     * 获取与负载均衡器相关的统计信息
     */
    public abstract LoadBalancerStats getLoadBalancerStats();    
}

```

AbstractLoadBalancer是 ILoadBalancer的一个抽象实现，同时也维护了一个关于服务实例的分组枚举类，ServerGroup  同时呢，定义了三种类型，用来针对不同的情况。

-   **ALL**  ：所有服务实例
-   **STATUS_UP** ：正常服务的实例
-  **STATUS_NOT_UP**  ：停止服务的实例



## BaseLoadBalancer类

作用：负载均衡的基础负载均衡器，定义了很多负载均衡器的基本内容

接下来看BaseLoadBalancer针对负载均衡都做了哪些工作呢？

- 维护了两个服务实例列表，其中一个用于存放所有的实例，一个用于存放正常服务的实例

```java
@Monitor(name = PREFIX + "AllServerList", type = DataSourceType.INFORMATIONAL)
protected volatile List<Server> allServerList = Collections
        .synchronizedList(new ArrayList<Server>());
@Monitor(name = PREFIX + "UpServerList", type = DataSourceType.INFORMATIONAL)
protected volatile List<Server> upServerList = Collections
        .synchronizedList(new ArrayList<Server>());
```

- 定义了服务检查的IPing对象，默认为null

  ```java
  protected IPing ping = null;
  ```

- 定义了实施服务检查的执行策略对象，采用默认策略实现。

  ```java
  protected IPingStrategy pingStrategy = DEFAULT_PING_STRATEGY
  ```

  源码部分：

  ```java
  /**
   * Default implementation for <c>IPingStrategy</c>, performs ping
   * serially, which may not be desirable, if your <c>IPing</c>
   * implementation is slow, or you have large number of servers.
   */
  private static class SerialPingStrategy implements IPingStrategy {
  
      @Override
      public boolean[] pingServers(IPing ping, Server[] servers) {
          int numCandidates = servers.length;
          boolean[] results = new boolean[numCandidates];
  
          logger.debug("LoadBalancer:  PingTask executing [{}] servers configured", numCandidates);
  
          for (int i = 0; i < numCandidates; i++) {
              results[i] = false; /* Default answer is DEAD. */
              try {
                  // NOTE: IFF we were doing a real ping
                  // assuming we had a large set of servers (say 15)
                  // the logic below will run them serially
                  // hence taking 15 times the amount of time it takes
                  // to ping each server
                  // A better method would be to put this in an executor
                  // pool
                  // But, at the time of this writing, we dont REALLY
                  // use a Real Ping (its mostly in memory eureka call)
                  // hence we can afford to simplify this design and run
                  // this
                  // serially
                  if (ping != null) {
                      results[i] = ping.isAlive(servers[i]);
                  }
              } catch (Exception e) {
                  logger.error("Exception while pinging Server: '{}'", servers[i], e);
              }
          }
          return results;
      }
  }
  ```

  根据注释的意思我们大概知道，如果Server列表过大时，采用默认线性遍历的方式可能会影响系统的性能，       

  这个时候就需要 实现 IPingStrategy 并重写 pingServers 采用更为灵活的方式。

- 定义了服务选择器IRule对象，这里默认采用**RoundRobinRule**实现

  RoundRobinRule代码部分：

  ```java
  public Server choose(ILoadBalancer lb, Object key) {
      if (lb == null) {
          log.warn("no load balancer");
          return null;
      }
  
      Server server = null;
      int count = 0;
      while (server == null && count++ < 10) {
          List<Server> reachableServers = lb.getReachableServers();
          List<Server> allServers = lb.getAllServers();
          int upCount = reachableServers.size();
          int serverCount = allServers.size();
  
          if ((upCount == 0) || (serverCount == 0)) {
              log.warn("No up servers available from load balancer: " + lb);
              return null;
          }
  
          int nextServerIndex = incrementAndGetModulo(serverCount);
          server = allServers.get(nextServerIndex);
  
          if (server == null) {
              /* Transient. */
              Thread.yield();
              continue;
          }
  
          if (server.isAlive() && (server.isReadyToServe())) {
              return (server);
          }
  
          // Next.
          server = null;
      }
  
      if (count >= 10) {
          log.warn("No available alive servers after 10 tries from load balancer: "
                  + lb);
      }
      return server;
  }
  ```

  这里可以看出Ribbon默认的服务选择策略是线性选择策略。

  举个例子:第一次请求分发到了 9090 端口 第二次则会分发到 9091 然后 9092这样来

- 启动Ping服务，定时检查当前Server是否健康，默认10秒

  ```java
  protected int pingIntervalSeconds = 10;
  ```

- 实现了ILoadBalancer的一系列操作

  ```java
  //向服务列表中添加一个新的服务  
  @Override
      public void addServers(List<Server> newServers) {
          if (newServers != null && newServers.size() > 0) {
              try {
                  ArrayList<Server> newList = new ArrayList<Server>();
                  newList.addAll(allServerList);
                  newList.addAll(newServers);
                  setServersList(newList);
              } catch (Exception e) {
                  logger.error("LoadBalancer [{}]: Exception while adding Servers", name, e);
              }
          }
      }
  
  
  //根据特定的key选择一个服务实例
  public Server chooseServer(Object key) {
          if (counter == null) {
              counter = createCounter();
          }
          counter.increment();
          if (rule == null) {
              return null;
          } else {
              try {
                  return rule.choose(key);
              } catch (Exception e) {
                  logger.warn("LoadBalancer [{}]:  Error choosing server for key {}", name, key, e);
                  return null;
              }
          }
      }
  
  
  //返回一个服务列表
      @Override
      public List<Server> getServerList(boolean availableOnly) {
          return (availableOnly ? getReachableServers() : getAllServers());
      }
  
  
  //返回可用的实例列表
      @Override
      public List<Server> getReachableServers() {
          return Collections.unmodifiableList(upServerList);
      }
  
  
  //返回所有的实例列表
  
      @Override
      public List<Server> getAllServers() {
          return Collections.unmodifiableList(allServerList);
      }
  
  //标记一个服务暂停服务
   public void markServerDown(Server server) {
          if (server == null || !server.isAlive()) {
              return;
          }
  
          logger.error("LoadBalancer [{}]:  markServerDown called on [{}]", name, server.getId());
          server.setAlive(false);
          // forceQuickPing();
  
          notifyServerStatusChangeListener(singleton(server));
      }
  
  ```

  

## DynamicServerListLoadBalancer类

作用：对基础的负载均衡器BaseLoadBalancer做了扩展，使其拥有服务实例清单在运行期的动态更新的能力。同时也具备了对服务实例清单的过滤功能。

在DynamicServerListLoadBalancer类的成员定义中，我们发现新增了一个成员

ServerList<T> serverListImpl 对象，源码如下：

```java
public interface ServerList<T extends Server> {

    //获取初始化时的服务列表
    public List<T> getInitialListOfServers();
    
    /**
     *获取更新时的服务列表
     */
    public List<T> getUpdatedListOfServers();   

}
```

通过查看ServerList的继承关系图，我们发现ServerList接口的实现不止一个，那 具体是使用了哪一个实现呢？

可以从如下思路入手，既然DynamicServerListLoadBalancer类实现了服务实例清单的动态更新，那Ribbon势必要和Eureka整合，所以我们从Eureka对Ribbon的支持下手。

###  EurekaRibbonClientConfiguration类:

```java
@Bean
@ConditionalOnMissingBean
public ServerList<?> ribbonServerList(IClientConfig config,
      Provider<EurekaClient> eurekaClientProvider) {
   if (this.propertiesFactory.isSet(ServerList.class, serviceId)) {
      return this.propertiesFactory.get(ServerList.class, config, serviceId);
   }
   DiscoveryEnabledNIWSServerList discoveryServerList = new DiscoveryEnabledNIWSServerList(
         config, eurekaClientProvider);
   DomainExtractingServerList serverList = new DomainExtractingServerList(
         discoveryServerList, config, this.approximateZoneFromHostname);
   return serverList;
}
```

可以看到默认采用的DiscoveryEnabledNIWSServerList 实现。

#### DomainExtractingServerList类：

```java
public class DomainExtractingServerList implements ServerList<DiscoveryEnabledServer> {

	private ServerList<DiscoveryEnabledServer> list;

	private final RibbonProperties ribbon;

	private boolean approximateZoneFromHostname;

	public DomainExtractingServerList(ServerList<DiscoveryEnabledServer> list,
			IClientConfig clientConfig, boolean approximateZoneFromHostname) {
		this.list = list;
		this.ribbon = RibbonProperties.from(clientConfig);
		this.approximateZoneFromHostname = approximateZoneFromHostname;
	}

	@Override
	public List<DiscoveryEnabledServer> getInitialListOfServers() {
		List<DiscoveryEnabledServer> servers = setZones(
				this.list.getInitialListOfServers());
		return servers;
	}

	@Override
	public List<DiscoveryEnabledServer> getUpdatedListOfServers() {
		List<DiscoveryEnabledServer> servers = setZones(
				this.list.getUpdatedListOfServers());
		return servers;
	}

	private List<DiscoveryEnabledServer> setZones(List<DiscoveryEnabledServer> servers) {
		List<DiscoveryEnabledServer> result = new ArrayList<>();
		boolean isSecure = this.ribbon.isSecure(true);
		boolean shouldUseIpAddr = this.ribbon.isUseIPAddrForServer();
		for (DiscoveryEnabledServer server : servers) {
			result.add(new DomainExtractingServer(server, isSecure, shouldUseIpAddr,
					this.approximateZoneFromHostname));
		}
		return result;
	}

}

...略

}

```

可以看到DomainExtractingServerList的具体实现是委托于其内部list来实现的，内部list通过DomainExtractingServerList构造器传入的DiscoveryEnabledNIWSServerList获得。



## DiscoveryEnabledNIWSServerList 类：

源码部分:(部分代码略)

```java
public class DiscoveryEnabledNIWSServerList extends AbstractServerList<DiscoveryEnabledServer> {
    
    
     public List<DiscoveryEnabledServer> getInitialListOfServers() {
        return this.obtainServersViaDiscovery();
    }

    public List<DiscoveryEnabledServer> getUpdatedListOfServers() {
        return this.obtainServersViaDiscovery();
    }

    
    rivate List<DiscoveryEnabledServer> obtainServersViaDiscovery() {
        List<DiscoveryEnabledServer> serverList = new ArrayList();
        if (this.eurekaClientProvider != null && this.eurekaClientProvider.get() != null) {
            EurekaClient eurekaClient = (EurekaClient)this.eurekaClientProvider.get();
            if (this.vipAddresses != null) {
                String[] var3 = this.vipAddresses.split(",");
                int var4 = var3.length;

                for(int var5 = 0; var5 < var4; ++var5) {
                    String vipAddress = var3[var5];
                    List<InstanceInfo> listOfInstanceInfo = eurekaClient.getInstancesByVipAddress(vipAddress, this.isSecure, this.targetRegion);
                    Iterator var8 = listOfInstanceInfo.iterator();

                    while(var8.hasNext()) {
                        InstanceInfo ii = (InstanceInfo)var8.next();
                        if (ii.getStatus().equals(InstanceStatus.UP)) {
                            if (this.shouldUseOverridePort) {
                                if (logger.isDebugEnabled()) {
                                    logger.debug("Overriding port on client name: " + this.clientName + " to " + this.overridePort);
                                }

                                InstanceInfo copy = new InstanceInfo(ii);
                                if (this.isSecure) {
                                    ii = (new Builder(copy)).setSecurePort(this.overridePort).build();
                                } else {
                                    ii = (new Builder(copy)).setPort(this.overridePort).build();
                                }
                            }

                            DiscoveryEnabledServer des = this.createServer(ii, this.isSecure, this.shouldUseIpAddr);
                            serverList.add(des);
                        }
                    }

                    if (serverList.size() > 0 && this.prioritizeVipAddressBasedServers) {
                        break;
                    }
                }
            }

            return serverList;
        } else {
            logger.warn("EurekaClient has not been initialized yet, returning an empty list");
            return new ArrayList();
        }
    }
    
    
}
```



- 第一步,通过eureka获取服务实例listOfInstanceInfo列表

  ```java
  List<InstanceInfo> listOfInstanceInfo = eurekaClient.getInstancesByVipAddress(vipAddress, isSecure, targetRegion)
  ```

- 第二步，遍历listOfInstanceInfo列表，如果该服务实例状态为UP，则转化成DiscoveryEnabledServer对象，然后添加到serverList里面。

- 返回serverList服务实例列表。



通过查看上面的代码大概知道了Ribbon是如何从Eureka注册中心获取最新的服务列表的，那Ribbon又是如何将获取到的服务列表更新到本地的呢，这一切的关键是在DynamicServerListLoadBalancer类上，因为我们知道DynamicServerListLoadBalancer类具体实现了动态更新服务列表的功能。

通过查看源码：

```java
    protected final ServerListUpdater.UpdateAction updateAction = new ServerListUpdater.UpdateAction() {
        //更新的具体实现
        @Override
        public void doUpdate() {
            updateListOfServers();
        }
    };
```



```java
public interface ServerListUpdater {

    /**
     * an interface for the updateAction that actually executes a server list update
     */
    public interface UpdateAction {
        void doUpdate();
    }


    /**
     * start the serverList updater with the given update action
     * This call should be idempotent.
     * 启动服务更新器
     *
     * @param updateAction
     */
    void start(UpdateAction updateAction);

    /**
     * stop the serverList updater. This call should be idempotent
     *停止服务更新器
     */
    void stop();

    /**
     * @return the last update timestamp as a {@link java.util.Date} string
     *获取最近一次更新的时间
     */
    String getLastUpdate();

    /**
     * @return the number of ms that has elapsed since last update
     * 获取上一次更新到现在的时间间隔，单位为Ms毫秒
     */
    long getDurationSinceLastUpdateMs();

    /**
     * @return the number of update cycles missed, if valid
     */
    int getNumberMissedCycles();

    /**
     * @return the number of threads used, if vaid
     * 获取核心线程数
     */
    int getCoreThreads();
}

```

通过查看ServerListUpdater 接口实现关系图，我们大概发现Ribbon内置了两个实现。

- PollingServerListUpdater ：默认采用的更新策略，采用定时任务的方式动态更新服务列表

  ```java
  // msecs; 延迟一秒开始执行  
  private static long LISTOFSERVERS_CACHE_UPDATE_DELAY = 1000; 
  // msecs;以30秒为周期重复执行
  private static int LISTOFSERVERS_CACHE_REPEAT_INTERVAL = 30 * 1000; 
  ```

  

-  EurekaNotificationServerListUpdater :基于Eureka事件机制来驱动服务列表更新的实现。

那么，我们Ribbon默认具体采用了哪一种更新策略呢，通过查看DynamicServerListLoadBalancer类的代码，我们发现Ribbon采用的默认服务更新器是PollingServerListUpdater

```java
 @Deprecated
    public DynamicServerListLoadBalancer(IClientConfig clientConfig, IRule rule, IPing ping, 
            ServerList<T> serverList, ServerListFilter<T> filter) {
        this(
                clientConfig,
                rule,
                ping,
                serverList,
                filter,
                new PollingServerListUpdater()
        );
    }
```
既然了解了默认更新策略，那么我们再次回到我们的主角DynamicServerListLoadBalancer类上。

```java
    protected final ServerListUpdater.UpdateAction updateAction = new ServerListUpdater.UpdateAction() {
        @Override
        public void doUpdate() {
            updateListOfServers();
        }
    };
```

通过代码我们发现实际履行更新职责的方法是   updateListOfServers() ，不废话，上代码:

```java
  @VisibleForTesting
    public void updateListOfServers() {
        List<T> servers = new ArrayList<T>();
        if (serverListImpl != null) {
            servers = serverListImpl.getUpdatedListOfServers();
            LOGGER.debug("List of Servers for {} obtained from Discovery client: {}",
                    getIdentifier(), servers);

            if (filter != null) {
                servers = filter.getFilteredListOfServers(servers);
                LOGGER.debug("Filtered List of Servers for {} obtained from Discovery client: {}",
                        getIdentifier(), servers);
            }
        }
        updateAllServerList(servers);
    }
```

通过查看代码，我们发现流程大致如下：

- 通过 ServerList的getUpdatedListOfServers() 方法获取到最新的服务实例列表
- 如果之前定义了过滤器，则按照某种规则实施过滤，最后返回
- updateAllServerList(servers); 完成最后的更新操作。



```java
public interface ServerListFilter<T extends Server> {

    public List<T> getFilteredListOfServers(List<T> servers);

}

```

通过查看继承实现关系图，发现ServerListFilter的直接实现类为：AbstractServerListFilter 

 其中ZoneAffinityServerListFilter 继承了 AbstractServerListFilter  ，然后得ZoneAffinityServerListFilter 真传的子类又有好多，这里着重介绍**AbstractServerListFilter** 和 **ZoneAffinityServerListFilter** 实现

- AbstractServerListFilter ：抽象过滤器，依赖LoadBalancerStats对象实现过滤。LoadBalancerStats存储了负载均衡器的一些属性和统计信息。
- ZoneAffinityServerListFilter：此服务器列表筛选器处理基于区域关联性筛选服务器。它会过滤掉一些服务实例和消费者不在一个Zone（区域）的实例。



## ZoneAwareLoadBalancer类

**功能：**ZoneAwareLoadBalancer负载均衡器是对DynamicServerListLoadBalancer类的扩展和补充，该负载混合器实现了Zone(区域)的概念，避免了因为跨区域而导致的区域性故障，从而实现了服务的高可用。

那么ZoneAwareLoadBalancer具体做了哪些工作来实现这些功能的呢？

第一：重写了DynamicServerListLoadBalancer的setServerListForZones方法：

**原版：**

```java
 protected void setServerListForZones(
    Map<String, List<Server>> zoneServersMap) {
    LOGGER.debug("Setting server list for zones: {}", zoneServersMap);
    getLoadBalancerStats().updateZoneServerMapping(zoneServersMap);
 }
```

**ZoneAwareLoadBalancer类版:**

```java
    @Override
    protected void setServerListForZones(Map<String, List<Server>> zoneServersMap) {
        super.setServerListForZones(zoneServersMap);
        if (balancers == null) {
            //balancers  用来存储每个String对应的Zone
            balancers = new ConcurrentHashMap<String, BaseLoadBalancer>();
        }
        //设置对应zone下面的实例清单
        for (Map.Entry<String, List<Server>> entry: zoneServersMap.entrySet()) {
        	String zone = entry.getKey().toLowerCase();
            getLoadBalancer(zone).setServersList(entry.getValue());
        }
        //检查是否有不再拥有服务器的区域
        //并将列表设置为空，以便与区域相关的度量不为空
        //包含过时的数据
        // 防止因为Zone的信息过时而干扰具体实例的选择算法。
        for (Map.Entry<String, BaseLoadBalancer> existingLBEntry: balancers.entrySet()) {
            if (!zoneServersMap.keySet().contains(existingLBEntry.getKey())) {
                existingLBEntry.getValue().setServersList(Collections.emptyList());
            }
        }
    }  
```



那ZoneAwareLoadBalancer类是具体如何来选择具体的服务实例呢，

```java
    @Override
    public Server chooseServer(Object key) {
        if (!ENABLED.get() || getLoadBalancerStats().getAvailableZones().size() <= 1) {
            logger.debug("Zone aware logic disabled or there is only one zone");
            return super.chooseServer(key);
        }
        Server server = null;
        try {
            LoadBalancerStats lbStats = getLoadBalancerStats();
            //为所有Zone都创建一个快照
            Map<String, ZoneSnapshot> zoneSnapshot = ZoneAvoidanceRule.createSnapshot(lbStats);
            logger.debug("Zone snapshots: {}", zoneSnapshot);
            if (triggeringLoad == null) {
                triggeringLoad = DynamicPropertyFactory.getInstance().getDoubleProperty(
                        "ZoneAwareNIWSDiscoveryLoadBalancer." + this.getName() + ".triggeringLoadPerServerThreshold", 0.2d);
            }

            if (triggeringBlackoutPercentage == null) {
                triggeringBlackoutPercentage = DynamicPropertyFactory.getInstance().getDoubleProperty(
                        "ZoneAwareNIWSDiscoveryLoadBalancer." + this.getName() + ".avoidZoneWithBlackoutPercetage", 0.99999d);
            }
            Set<String> availableZones = ZoneAvoidanceRule.getAvailableZones(zoneSnapshot, triggeringLoad.get(), triggeringBlackoutPercentage.get());
            logger.debug("Available zones: {}", availableZones);
            if (availableZones != null &&  availableZones.size() < zoneSnapshot.keySet().size()) {
                String zone = ZoneAvoidanceRule.randomChooseZone(zoneSnapshot, availableZones);
                logger.debug("Zone chosen: {}", zone);
                if (zone != null) {
                    BaseLoadBalancer zoneLoadBalancer = getLoadBalancer(zone);
                    server = zoneLoadBalancer.chooseServer(key);
                }
            }
        } catch (Exception e) {
            logger.error("Error choosing server using zone aware logic for load balancer={}", name, e);
        }
        if (server != null) {
            return server;
        } else {
            logger.debug("Zone avoidance logic is not invoked.");
            return super.chooseServer(key);
        }
    }
```



从源码中可以看出来， getLoadBalancerStats().getAvailableZones().size() <= 1 只有在当前的Zone区域的数量大于1的时候才会采用区域选择策略，否则的话，则'**return super.chooseServer(key)**' 什么也不做，采用父类的实现。

在选择具体的服务实例中，ZoneAwareLoadBalancer主要做了以下几件事：

- 为所有Zone区域分别创建一个快照，存储在zoneSnapshot 里面

- 通过Zone快照中的信息，按照某种策略例如Zone的服务实例数量，故障率等等来筛选掉不符合条件的Zone区域。

- 如果发现没有符合剔除要求的区域，同时实例最大平均负载小于阈值（默认百分之20），就直接返回所有可以的Zone区域，否则，随机剔除一个最坏的Zone。

- 获得的可用的Zone列表不为空，并且数量小于之前快照中的总数量，则根据IRule规则随机选一个Zone区域

  ```java
             if (availableZones != null &&  availableZones.size() < zoneSnapshot.keySet().size()) {
                  String zone = ZoneAvoidanceRule.randomChooseZone(zoneSnapshot, availableZones);
                  logger.debug("Zone chosen: {}", zone);
                  if (zone != null) {
                      BaseLoadBalancer zoneLoadBalancer = getLoadBalancer(zone);
                      server = zoneLoadBalancer.chooseServer(key);
                  }
         
            }
  ```

- 确定了最终的Zone之后，最终调用 BaseLoadBalancer的chooseServer来选择一个合适的服务实例。



# 负载均衡策略

通过上面的分析，我们发现当一个请求过来时，会被拦截交给相应的负载均衡器，然后不同的负载均衡器根据不同的策略来选择合适的服务实例。在这里我们是知道Ribbon是根据不同的Rule来实现对实例的一个选择的，那么Ribbon具体提供了哪些规则供我们使用呢？通过查看Ribbon的IRule接口的实现集成关系图，我们最终可以发现，Ribbon主要提供了以下几个规则实现的。

- RandomRule 类：该策略实现了从服务实例清单中随机选择一个服务实例的功能

- RoundRobinRule类：该策略实现了轮询的方式从服务实例清单中依次选择服务实例的功能RetryRule

- RetryRule类：该策略实现了具备重试机制的实例选择功能

- WeightedResponseTimeRule类：根据权重来选择实例

- BestAvailableRule类：选择一个最空闲的实例

- PredicateBasedRule 类：先过滤，然后再以轮询的方式选择实例

  ...

## IRule接口:

```java
public interface IRule{

    public Server choose(Object key);
    
    public void setLoadBalancer(ILoadBalancer lb);
    
    public ILoadBalancer getLoadBalancer();    
}

```

## AbstractLoadBalancerRule抽象类:

```java
public abstract class AbstractLoadBalancerRule implements IRule, IClientConfigAware {

    private ILoadBalancer lb;
        
    @Override
    public void setLoadBalancer(ILoadBalancer lb){
        this.lb = lb;
    }
    
    @Override
    public ILoadBalancer getLoadBalancer(){
        return lb;
    }      
}
```

## RandomRule类

功能：该策略实现了从服务实例清单中随机选择一个服务实例的功能。

查看代码发现具体的实例选择并没有由默认的choose(Object key)来实现，而是委托给了同类下的choose(ILoadBalancer lb, Object key)方法来完成实际的实例选择工作。

```java
   public Server choose(ILoadBalancer lb, Object key) {
        if (lb == null) {
            return null;
        }
        Server server = null;

        while (server == null) {
            if (Thread.interrupted()) {
                return null;
            }
            List<Server> upList = lb.getReachableServers();
            List<Server> allList = lb.getAllServers();

            int serverCount = allList.size();
            if (serverCount == 0) {
                /*
                 * No servers. End regardless of pass, because subsequent passes
                 * only get more restrictive.
                 */
                return null;
            }

            int index = chooseRandomInt(serverCount);
            server = upList.get(index);

            if (server == null) {
                /*
                 * The only time this should happen is if the server list were
                 * somehow trimmed. This is a transient condition. Retry after
                 * yielding.
                 */
                Thread.yield();
                continue;
            }

            if (server.isAlive()) {
                return (server);
            }

            // Shouldn't actually happen.. but must be transient or a bug.
            server = null;
            Thread.yield();
        }

        return server;

    }

```

> 注：如果获取不到服务实例，则可能存在并发的bug



## RoundRobinRule类

功能：该策略实现了轮询的方式从服务实例清单中依次选择服务实例的功能

```java
   public Server choose(ILoadBalancer lb, Object key) {
        if (lb == null) {
            log.warn("no load balancer");
            return null;
        }

        Server server = null;
        int count = 0;
        while (server == null && count++ < 10) {
            //reachableServers 可用的服务实例清单
            List<Server> reachableServers = lb.getReachableServers();
            //allServers 获取所有可用的服务列表
            List<Server> allServers = lb.getAllServers();
            
            int upCount = reachableServers.size();
            int serverCount = allServers.size();
            
            if ((upCount == 0) || (serverCount == 0)) {
                log.warn("No up servers available from load balancer: " + lb);
                return null;
            }

            int nextServerIndex = incrementAndGetModulo(serverCount);
            server = allServers.get(nextServerIndex);

            if (server == null) {
                /* Transient. */
                Thread.yield();
                continue;
            }

            if (server.isAlive() && (server.isReadyToServe())) {
                return (server);
            }

            // Next.
            server = null;
        }

        if (count >= 10) {
            log.warn("No available alive servers after 10 tries from load balancer: "
                    + lb);
        }
        return server;
    }



   private int incrementAndGetModulo(int modulo) {
        for (;;) {
            int current = nextServerCyclicCounter.get();
            int next = (current + 1) % modulo;
            if (nextServerCyclicCounter.compareAndSet(current, next))
                return next;
        }
    }

```



源码分析：可以发现RoundRobinRule的实现逻辑和RandomRule非常类似，我们可以看出来，RoundRobinRule定义了一个计数器变量count，该计数器会在每次循环后自动叠加，当获取不到Server的次数超过十次时，会结束尝试，并发出警告：No available alive servers after 10 tries from load balancer。

而线性轮询的实现则是通过 incrementAndGetModulo(int modulo)来实现的.

## RetryRule类:

功能：该策略实现了具备重试机制的实例选择功能

```java
public Server choose(ILoadBalancer lb, Object key) {
        //请求时间
		long requestTime = System.currentTimeMillis();
        //deadline 截止期限
		long deadline = requestTime + maxRetryMillis;

		Server answer = null;

		answer = subRule.choose(key);

		if (((answer == null) || (!answer.isAlive()))
				&& (System.currentTimeMillis() < deadline)) {

			InterruptTask task = new InterruptTask(deadline
					- System.currentTimeMillis());

			while (!Thread.interrupted()) {
				answer = subRule.choose(key);

				if (((answer == null) || (!answer.isAlive()))
						&& (System.currentTimeMillis() < deadline)) {
					/* pause and retry hoping it's transient */
					Thread.yield();
				} else {
					break;
				}
			}

			task.cancel();
		}

		if ((answer == null) || (!answer.isAlive())) {
			return null;
		} else {
			return answer;
		}
	}

```

默认使用的是RoundRobinRule策略。期间如果能选择到实例就返回，如果选择不到就根据设置的尝试结束时间为阈值，如果超过截止期限则直接返回null。

## WeightedResponseTimeRule类

功能：根据权重来选择实例

主要有以下三个核心内容：

- 定时任务
- 权重计算
- 实例选择



### 1. 定时任务

```java
  void initialize(ILoadBalancer lb) {        
        if (serverWeightTimer != null) {
            serverWeightTimer.cancel();
        }
        serverWeightTimer = new Timer("NFLoadBalancer-serverWeightTimer-"
                + name, true);
        //启动定时任务
        serverWeightTimer.schedule(new DynamicServerWeightTask(), 0,
                serverWeightTaskTimerInterval);
        // do a initial run
        ServerWeight sw = new ServerWeight();
        sw.maintainWeights();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                logger
                        .info("Stopping NFLoadBalancer-serverWeightTimer-"
                                + name);
                serverWeightTimer.cancel();
            }
        }));
    }

```



WeightedResponseTimeRule类在初始化的时候会先定义一个计时器，然后会启动一个定时任务，用来为每个服务实例计算权重，该任务默认每30秒执行一次。

```java
  class DynamicServerWeightTask extends TimerTask {
        public void run() {
            ServerWeight serverWeight = new ServerWeight();
            try {
                serverWeight.maintainWeights();
            } catch (Exception e) {
                logger.error("Error running DynamicServerWeightTask for {}", name, e);
            }
        }
    }
```



### 2.权重计算

通过上面的DynamicServerWeightTask的代码呢，我们可以大致了解到，权重计算的功能呢实际是由ServerWeight的maintainWeights()来执行的。少废话，上代码。

```java
   public void maintainWeights() {
            ILoadBalancer lb = getLoadBalancer();
            if (lb == null) {
                return;
            }
            
            if (!serverWeightAssignmentInProgress.compareAndSet(false,  true))  {
                return; 
            }
            
            try {
                logger.info("Weight adjusting job started");
                AbstractLoadBalancer nlb = (AbstractLoadBalancer) lb;
                LoadBalancerStats stats = nlb.getLoadBalancerStats();
                if (stats == null) {
                    // no statistics, nothing to do
                    return;
                }
                double totalResponseTime = 0;
                // find maximal 95% response time
                for (Server server : nlb.getAllServers()) {
                    // this will automatically load the stats if not in cache
                    ServerStats ss = stats.getSingleServerStat(server);
                    totalResponseTime += ss.getResponseTimeAvg();
                }
                // weight for each server is (sum of responseTime of all servers - responseTime)
                // so that the longer the response time, the less the weight and the less likely to be chosen
                Double weightSoFar = 0.0;
                
                // create new list and hot swap the reference
                List<Double> finalWeights = new ArrayList<Double>();
                for (Server server : nlb.getAllServers()) {
                    ServerStats ss = stats.getSingleServerStat(server);
                    double weight = totalResponseTime - ss.getResponseTimeAvg();
                    weightSoFar += weight;
                    finalWeights.add(weightSoFar);   
                }
                setWeights(finalWeights);
            } catch (Exception e) {
                logger.error("Error calculating server weights", e);
            } finally {
                serverWeightAssignmentInProgress.set(false);
            }

        }
    }

```

那WeightedResponseTimeRule是如何计算权重的呢？主要分为以下两步：

1. 先遍历服务器列表，并得到每个服务器的平均响应时间，遍历过程中对其求和，遍历结束后得到总响应时间totalResponseTime。
2. 再一次遍历服务器列表，并将总响应时间totalResponseTime减去每个服务器的平均响应时间作为权重weight，再将这之前的所以权重累加到weightSoFar 变量中，并且保存到finalWeights供choose使用。

### 3.实例选择

```java
 public Server choose(ILoadBalancer lb, Object key) {
        if (lb == null) {
            return null;
        }
        Server server = null;

        while (server == null) {
           //获取当前引用，以防它被其他线程更改
            List<Double> currentWeights = accumulatedWeights;
            if (Thread.interrupted()) {
                return null;
            }
            List<Server> allList = lb.getAllServers();

            int serverCount = allList.size();

            if (serverCount == 0) {
                return null;
            }

            int serverIndex = 0;

            // 列表中的最后一个是所有权重的和
            double maxTotalWeight = currentWeights.size() == 0 ? 0 :                 currentWeights.get(currentWeights.size() - 1); 
            //尚未命中任何服务器，且未初始化总重量
            //使用循环操作
            if (maxTotalWeight < 0.001d || serverCount != currentWeights.size()) {
                server =  super.choose(getLoadBalancer(), key);
                if(server == null) {
                    return server;
                }
            } else {
                //生成一个从0(含)到maxTotalWeight(不含)之间的随机权重
                double randomWeight = random.nextDouble() * maxTotalWeight;
                //根据随机索引选择服务器索引
                int n = 0;
                for (Double d : currentWeights) {
                    if (d >= randomWeight) {
                        serverIndex = n;
                        break;
                    } else {
                        n++;
                    }
                }

                server = allList.get(serverIndex);
            }

            if (server == null) {
                /* Transient. */
                Thread.yield();
                continue;
            }

            if (server.isAlive()) {
                return (server);
            }

            // Next.
            server = null;
        }
        return server;
    }
```

执行步骤：

- 生成一个从0(含)到maxTotalWeight(不含)之间的随机权重
- 遍历权重列表，比较权重值与随机数的大小，如果权重值大于等于随机数，就当前权重列表的索引值去服务实例列表中列表中获取具体的实例。



## BestAvailableRule类

功能：选择一个最空闲的实例

```java
   @Override
    public Server choose(Object key) {
        if (loadBalancerStats == null) {
            return super.choose(key);
        }
        List<Server> serverList = getLoadBalancer().getAllServers();
        //minimalConcurrentConnections：最小并发连接数
        int minimalConcurrentConnections = Integer.MAX_VALUE;
        long currentTime = System.currentTimeMillis();
        Server chosen = null;
        for (Server server: serverList) {
            ServerStats serverStats = loadBalancerStats.getSingleServerStat(server);
            if (!serverStats.isCircuitBreakerTripped(currentTime)) {
                //concurrentConnections:并发连接数
                int concurrentConnections = serverStats.getActiveRequestsCount(currentTime);
                if (concurrentConnections < minimalConcurrentConnections) {
                    minimalConcurrentConnections = concurrentConnections;
                    chosen = server;
                }
            }
        }
        if (chosen == null) {
            return super.choose(key);
        } else {
            return chosen;
        }
    }
```



通过查看源码可以得知BestAvailableRule大致采用了如下策略来选择服务实例，根据loadBalancerStats中的统计信息通过遍历负载均衡器维护的所有服务实例 选出并发连接数最少的那一个，即最空闲的实例。

如果loadBalancerStats为空的话，则直接调用父类ClientConfigEnabledRoundRobinRule的实现，即RoundRobinRule，线性轮询的方式。

 

## PredicateBasedRule 类

功能：先过滤，然后再以轮询的方式选择实例

```java
   @Override
    public Server choose(Object key) {
        ILoadBalancer lb = getLoadBalancer();
        Optional<Server> server = getPredicate().chooseRoundRobinAfterFiltering(lb.getAllServers(), key);
        if (server.isPresent()) {
            return server.get();
        } else {
            return null;
        }       
    }
```



实现逻辑：通过子类中实现的predicate逻辑来过滤一部分服务实例，然后再以线性轮询的方式从过滤之后的服务实例清单中选择一个。



当然，PredicateBasedRule本身是一个抽象类，必然Ribbon提供了相应的子类实现，我们看到有ZoneAvoidanceRule和AvailabilityFilteringRule，分别对PredicateBasedRule做了相应的扩展，有兴趣的小伙伴可以下去自行研究。

# 配置详解：

## 自动化配置：

同样，得益于Springboot的自动化配置，大大降低了开发者上手的难度，在引入Spring-Clould-Ribbon依赖之后，便能够自动构建下面这些接口的实现。

- IClientConfig：Ribbon客户端配置接口类，默认实现：com.netflix.client.config.DefaultClientConfigImpl
- IRule: Ribbon：服务实例选择策略接口类，默认采用的实现：com.netflix.loadbalancer.ZoneAvoidanceRule
- IPing:Ribbon：实例检查策略接口类,默认实现：NoOpPing 即不检查
-  ServerList<T extends Server>：服务实例清单维护机制接口类，默认实现ConfigurationBasedServerList 当整合Eureka的情况下，则使用DiscoveryEnabledNIWSServerList类
- ServerListFilter<T extends Server>：服务实例过滤策略接口类，默认实现：ZoneAffinityServerListFilter  根据区域过滤，
- ILoadBalancer：负载均衡器接口类，默认实现：ZoneAwareLoadBalancer 具备区域感知



## 替换默认配置

Ribbon同时支持部分默认配置的替换，这为使用针对不同场景的定制化方案提供了可能。目前的话支持两种方式的替换（我只知道这两种）。

- 创建实例覆盖默认实现

- 配置文件配置

  

### 创建实例覆盖默认实现

例：将默认的负载均衡策略替换成自己自定义的策略。

```java
    @Bean
    public IRule myRule() {
        return new MyRule();
    }
```



### 配置文件配置

通过使用<service-name>.ribbon.<key> = value 方式

在application.properties中添加如下代码，即可以将默认的IPing策略替换成自己自定义的策略。

```properties
### 扩展 IPing 实现
user-service-provider.ribbon.NFLoadBalancerPingClassName = \
  com.xxxx.demo.user.ribbon.client.ping.MyPing
```

MyPing代码（小马哥微服务实战版）：

```java
public class MyPing implements IPing {

    @Override
    public boolean isAlive(Server server) {

        String host = server.getHost();
        int port = server.getPort();
        // /health endpoint
        // 通过 Spring 组件来实现URL 拼装
        UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
        builder.scheme("http");
        builder.host(host);
        builder.port(port);
        builder.path("/actuator/health");
        URI uri = builder.build().toUri();

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity responseEntity = restTemplate.getForEntity(uri, String.class);
        // 当响应状态等于 200 时，返回 true ，否则 false
        return HttpStatus.OK.equals(responseEntity.getStatusCode());
    }

}

```

MyRule代码（小马哥微服务实战版）：

```java
public class MyRule extends AbstractLoadBalancerRule {

    @Override
    public void initWithNiwsConfig(IClientConfig clientConfig) {

    }

    @Override
    public Server choose(Object key) {

        ILoadBalancer loadBalancer = getLoadBalancer();

        //获取所有可达服务器列表
        List<Server> servers = loadBalancer.getReachableServers();
        if (servers.isEmpty()) {
            return null;
        }

        // 永远选择最后一台可达服务器
        Server targetServer = servers.get(servers.size() - 1);
        return targetServer;
    }

}

```



# 总结：

通过本次对Ribbon源码的一个简单初探，慢慢明白一个优秀的框架的优秀之处了，再看看自己之前写的代码就有些难以直视了，一个框架的设计往往不仅仅是实现了某些功能，也同时考虑到了各种不同的使用场景，这样可以保证框架可以胜任大多数简单的项目和大型项目。同时框架内部有很多实现都很高效，很少出现有什么极度不合理的地方，同时代码复用性也很高，看似几十上百个类实则职责分明，井井有条，在保证功能的情况下同时又有良好的扩展性。因为平常学业繁忙（主要是懒还爱玩儿），刻苦学习（期末全靠水过去），所以Ribbon这篇磕磕绊绊写了有半个多月的时间。好在自己终于坚持把它给看完了。后面的打算呢，将会陆续把自己学习java微服务的笔记整理好开源至本人的github上，希望可以帮助到一些刚开始入门的小伙伴们，也骗一些star（滑稽），最后，我是韩数，计算机小白，本科在读，我喜欢唱，跳...

