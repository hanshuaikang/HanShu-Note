## Spring Boot之定义注解扫描器@XXXScan

## 前言:

最近在学习Netty的过程中，跟着前辈们的思路用Netty作为底层通信开发了一个非常牛逼，宇宙第一(实际超级垃圾)的Netty Rpc Demo。为啥不叫框架叫Demo呢，一个好的框架是需要非常长时间的开发和优化的，离不开大佬们的全情投入，我这种级别的菜鸟，充其量叫demo。好，废话不多说，原本的思路呢，是需要手动配置一个接口与实现类的映射map,类似于下面这样:

```java
    @Bean("handlerMap")
    public Map<String, Object> handlerMap(){
        Map<String, Object> handlerMap = new ConcurrentHashMap<String, Object>();
        handlerMap.put("com.jdkcb.mystarter.service.PersonService",new PersonServiceImpl());
        return handlerMap;
    }
```

大佬们勿喷，当我自信满满的把代码交给前辈们看的时候，前辈非常耐心(不留情面)地指出了我这样做的问题。的确，在接口类数量非常多的时候，光配置Map就是一件非常麻烦的事情了，于是我回去冥思苦想,睡的特香，七七四十九分钟之后，脑袋里灵光乍现，想到了前天写Mybatis配置多数据源时候见到的一个注解，@MapperScan

ohohohohohohoh,这就是我独享的moment，就决定是你啦。

接下来，我们将模仿Mybatis的实现，来做一个注解扫描器，将我们自定义的注解扫描并注册成为Spring管理的Bean。

战士上战场，直接开始干。(好吧，这其实是一个梗，我猜很多人都get不到)

## 代码实战：

既然是要扫描我们自定义的注解，那首先我们得有个自定义的注解才行。来，小二，上自定义的注解，结合我个人的需求，我将它自定义为NRpcServer ,代码如下:

```java
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NRpcServer {
    //服务的名称,用来RPC的时候调用指定名称的 服务
    String name() default "";
    String value() default "";
}


```

可是，我有个问题，你是怎么知道加@Target({ElementType.TYPE, ElementType.METHOD})这几个注解的呢?

hhh，看来还是被你发现了，不会写，我还不会抄吗，既然是模仿@MapperScan()这个注解，那直接点开@Mapper注解,看看它加了什么注解不就行了吗，嘿嘿

```java
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
public @interface Mapper {
}

```

Nice,去掉我们不需要的注解，用不到的属性，改个名字就是我们自己的注解了。(滑稽)



那@Mapper是怎么样被扫描到的呢，通过@MapperScan这个注解，我想，在这里我们估计可以达成一致了，**抄一个**,篇幅有限，后面我就不贴Mybatis的代码了，需要了解的朋友可以打开mybatis的源码查看。

```java
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
//spring中的注解,加载对应的类
@Import(NRpcScannerRegistrar.class)//这个是我们的关键，实际上也是由这个类来扫描的
@Documented
public @interface NRpcScan {

    String[] basePackage() default {};

}

```

当然，这个注解本身是没什么东西的，最核心的地方在哪呢？答案是NRpcScannerRegistrar这个类上，实际上我们注解的扫描过滤主要是交给这个类来实现的。

新建一个代码 NRpcScannerRegistrar 类并继承ImportBeanDefinitionRegistrar, ResourceLoaderAware 这两个接口。

其中： ResourceLoaderAware是一个标记接口，用于通过ApplicationContext上下文注入ResourceLoader

**代码如下:**

```java

public class NRpcScannerRegistrar  implements ImportBeanDefinitionRegistrar, ResourceLoaderAware{

    ResourceLoader resourceLoader;

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;

    }


    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {

        //获取所有注解的属性和值
        AnnotationAttributes annoAttrs = AnnotationAttributes.fromMap(annotationMetadata.getAnnotationAttributes(NRpcScan.class.getName()));
        //获取到basePackage的值
        String[] basePackages = annoAttrs.getStringArray("basePackage");
        //如果没有设置basePackage 扫描路径,就扫描对应包下面的值
        if(basePackages.length == 0){
            basePackages = new String[]{((StandardAnnotationMetadata) annotationMetadata).getIntrospectedClass().getPackage().getName()};
        }

        //自定义的包扫描器
        FindNRpcServiceClassPathScanHandle scanHandle = new  FindNRpcServiceClassPathScanHandle(beanDefinitionRegistry,false);

        if(resourceLoader != null){
            scanHandle.setResourceLoader(resourceLoader);
        }
        //这里实现的是根据名称来注入
        scanHandle.setBeanNameGenerator(new RpcBeanNameGenerator());
        //扫描指定路径下的接口
        Set<BeanDefinitionHolder> beanDefinitionHolders = scanHandle.doScan(basePackages);


    }

}

```

这里涉及到一个 FindNRpcServiceClassPathScanHandle类，是我们自定义的包扫描器，我们可以在这个扫描器中添加我们的过滤条件。

```java
public class FindNRpcServiceClassPathScanHandle extends ClassPathBeanDefinitionScanner {

    public FindNRpcServiceClassPathScanHandle(BeanDefinitionRegistry registry, boolean useDefaultFilters) {
        super(registry, useDefaultFilters);
    }

    @Override
    protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
        //添加过滤条件，这里是只添加了@NRpcServer的注解才会被扫描到
        addIncludeFilter(new AnnotationTypeFilter(NRpcServer.class));
        //调用spring的扫描
        Set<BeanDefinitionHolder> beanDefinitionHolders = super.doScan(basePackages);
        return beanDefinitionHolders;
    }
}
```

看到这里很多读者几乎都会发现，我其实并没有做什么东西，所有核心的骚操作都是由spring提供的实现来完成的，其实是的，最核心的代码其实就在

super.doScan(basePackages);

这一句，我们所做的，其实就是根据Spring的扫描结果做一下二次的处理，在我的demo中，之前提到的map其实就是在这一步自动生成的，由于这次主要讲包扫描器的实现，所以就把那部分逻辑给去掉了。

同时呢，上面代码中的RpcBeanNameGenerator这个类则是实现了根据我们的名称来注入指定bean，这里其实做的就是获取到注解里面属性所设置的值。代码如下:

```java

public class RpcBeanNameGenerator extends AnnotationBeanNameGenerator {

    @Override
    public String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {
        //从自定义注解中拿name
        String name = getNameByServiceFindAnntation(definition,registry);
        if(name != null && !"".equals(name)){
            return name;
        }
        //走父类的方法
        return super.generateBeanName(definition, registry);
    }
    
    private String getNameByServiceFindAnntation(BeanDefinition definition, BeanDefinitionRegistry registry) {
        String beanClassName = definition.getBeanClassName();
        try {
            Class<?> aClass = Class.forName(beanClassName);
            NRpcServer annotation = aClass.getAnnotation(NRpcServer.class);
            if(annotation == null){
                return null;
            }
            //获取到注解name的值并返回
            return annotation.name();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}
```



到这里，几乎就已经大功告成了。

## 测试：

首先我们准备一个PersonService类，并打上我们的@NRpcServer注解，代码如下:

```java
@NRpcServer(name="PersonService")
public class PersonService {

    public String getName(){
        return "helloword";
    }
}

```

然后在Springboot启动类上添加@NRpcScan注解，并指定我们需要扫描的包

```java
@NRpcScan(basePackage = {"com.jdkcb.mybatisstuday.service"})
```

新建一个Controller，用于测试，代码如下:

```java
@RestController
public class TestController {

    @Autowired
    @Qualifier("PersonService")
    private PersonService personService;

    @RequestMapping("test")
    public String getName(){
        return personService.getName();
    }
    
}

```

在浏览器中输入http://127.0.0.1:8080/test

输出结果:

```java
helloword
```

到此，就算真正的大功告成啦。



最后的最后，大家好，我是韩数，哼，关注我，有你好果子吃(叉腰)。

记得点个赞再走哦~

等一下：

> 相关源码欢迎去我的github下载(欢迎star)：
>
> <https://github.com/hanshuaikang/Spring-Note>



