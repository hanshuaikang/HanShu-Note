# Spring 事件驱动编程

谈到Spring 事件驱动模型，我想大家都不陌生，事件驱动模型，通常也可以说是观察者设计模式，对观察者设计模式不熟悉的朋友可以看我之前写的笔记，[设计模式java语言实现之观察者模式](https://zhuanlan.zhihu.com/p/56032704)，在java事件驱动的支持中，EventBus做移动端开发的朋友应该都比较了解，其实，java本身也自带了对事件驱动的支持，但是大部分都是用于我们的客户端开发，比如GUI ，Swing这些，而Spring 则在java的基础上，扩展了对事件驱动的支持。

不说废话，直接上代码

## 1.代码实战

首先，我们新建一个类NotifyEvent 继承ApplicationEvent，用于封装我们事件额外的信息，这里则是String类型的msg，用于记录详细的事件内容。

```java
public class NotifyEvent extends ApplicationEvent {

    private String msg;

    public NotifyEvent(Object source, String msg) {
        super(source);
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }
}

```

其中，ApplicationEvent 是一个抽象类，扩展了java本身的EventObject 类，每一个继承了ApplicationEvent的子类都表示一类事件，可以携带数据。

然后新建一个NotifyPublisher用于我们事件的发布工作，该类实现了ApplicationContextAware并重写了setApplicationContext 方法，这一步的目的是可以获取我们Spring的应用上下文，因为事件的发布是需要应用上下文来做的，不了解应用上下文的同学可以去看我的另外一篇笔记:[到底什么是上下文？](https://juejin.im/post/5d8ebf8ef265da5b633cc90f)

```java
@Component //声明成组件，为了后期注入方便
public class NotifyPublisher implements ApplicationContextAware {

    private ApplicationContext ctx; //应用上下文

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.ctx= applicationContext;
    }

    // 发布一个消息，这里大家可以根据不同的状态实现发布不同的事件，我这里就只写了一个事件类，所以if else
    //都发布NotifyEvent事件。
    public void publishEvent(int status, String msg) {
        if (status == 0) {
            ctx.publishEvent(new NotifyEvent(this, msg));
        } else {
            ctx.publishEvent(new NotifyEvent(this,msg)) ;
        }
    }
}

```

最后一步就是实现一个类作为事件的订阅者啦，当事件发布时，会通知订阅者，然后订阅者做相关的处理，比如新用户注册发送事件自动发送欢迎邮件等等。同时，Spring 4.2 版本更新的EventListener，可以很方便帮助我们实现事件与方法的绑定，只需要在目标方法上加上EventListener即可。

```java
@Component
public class NotifyListener {

    @EventListener
    //参数NotifyEvent ，当有NotifyEvent 类型的事件发生时，交给sayHello方法处理
    public void sayHello(NotifyEvent notifyEvent){
       System.out.println("收到事件:"+notifyEvent.getMsg());
    }

}

```

**测试：**编写我们的测试类TestController。

```java
@RestController
public class TestController {

    @Autowired
    private NotifyPublisher notifyPublisher;

    @GetMapping("/sayHello")
    public String sayHello(){
        notifyPublisher.publishEvent(1, "我发布了一个事件");
        return "Hello Word";

    }

}


```

启动我们的应用，在浏览器中输入http://127.0.0.1:8080/sayHello，控制台输出:

```text
2019-09-28 16:55:51.902  INFO 716 --- [on(4)-127.0.0.1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 12 ms
收到事件:我发布了一个事件
```



划个知识点:

> 如果一个新的事件继承了NotifyEvent，当我们推送NotifyEvent类型的事件时，NotifyEvent和其子类的监听器都可以收到该事件。



完了吗，还没有，日常除了听到过事件驱动编程，偶尔还会见到异步事件驱动编程这几个字，同样的Spring 也提供了@Async 注解来实现异步事件的消费。用起来也很简单，只需要在 @EventListener上加上@Async 就好了。

## 2. Spring 异步事件实现:

**代码如下：**

```java
@Component
public class NotifyListener {

    @Async
    @EventListener
    public void sayHello(NotifyEvent notifyEvent){
       System.out.println("收到事件:"+notifyEvent.getMsg());
    }

}

```

最后配置一个线程池

```java
@Configuration
@EnableAsync
public class AysncListenerConfig implements AsyncConfigurer {
   
    /**
     * 获取异步线程池执行对象
     *
     * @return
     */
    @Override
    @Bean(name = "taskExecutor")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.initialize();
        executor.setCorePoolSize(10); //核心线程数
        executor.setMaxPoolSize(20);  //最大线程数
        executor.setQueueCapacity(1000); //队列大小
        executor.setKeepAliveSeconds(300); //线程最大空闲时间
        executor.setThreadNamePrefix("ics-Executor-"); ////指定用于新创建的线程名称的前缀。
        executor.setRejectedExecutionHandler(
                new ThreadPoolExecutor.CallerRunsPolicy()); // 拒绝策略
        return new ExceptionHandlingAsyncTaskExecutor(executor);
    }
    
    

}
```



```java
public class ExceptionHandlingAsyncTaskExecutor implements AsyncTaskExecutor {

    private AsyncTaskExecutor executor;

    public ExceptionHandlingAsyncTaskExecutor(AsyncTaskExecutor executor) {
        this.executor = executor;
    }

    //用独立的线程来包装，@Async其本质就是如此
    public void execute(Runnable task) {
        executor.execute(createWrappedRunnable(task));
    }
    
    public void execute(Runnable task, long startTimeout) {
        //用独立的线程来包装，@Async其本质就是如此
                executor.execute(createWrappedRunnable(task), startTimeout);
    }
    
    
    public Future submit(Runnable task) { return executor.submit(createWrappedRunnable(task));
        //用独立的线程来包装，@Async其本质就是如此。
    }
    
    
    public Future submit(final Callable task) {
        //用独立的线程来包装，@Async其本质就是如此。
        return executor.submit(createCallable(task));
    }

    
    private Callable createCallable(final Callable task) {
        return new Callable(){

            @Override
            public Object call() throws Exception {
                try {
                    return task.call();
                } catch (Exception ex) {
                    handle(ex);
                    throw ex;
                }
            }
        };
    }

   
    private Runnable createWrappedRunnable(final Runnable task) {
        return new Runnable() {
            public void run() {
                try {
                    task.run();
                } catch (Exception ex) {
                    handle(ex);
                }
            }
        };
    }
    private void handle(Exception ex) {
        //具体的异常逻辑处理的地方
        System.err.println("Error during @Async execution: " + ex);
    }
}
```



**测试：**编写我们的测试类TestController。

```text
2019-09-28 16:55:51.902  INFO 716 --- [on(4)-127.0.0.1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 12 ms
收到事件:我发布了一个事件
```



大功告成啦。

相关电子版笔记已经开源至github（欢迎star哦）:

<https://github.com/hanshuaikang/HanShu-Note>

