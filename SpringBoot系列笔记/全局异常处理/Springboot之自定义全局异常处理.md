## Springboot之自定义全局异常处理

本文首发至java技术博客[**码上**]：http://jdkcb.com/
## 前言：

在实际的应用开发中，很多时候往往因为一些不可控的因素导致程序出现一些错误，这个时候就要及时把异常信息反馈给客户端，便于客户端能够及时地进行处理，而针对代码导致的异常，我们一般有两种处理方式，一种是throws直接抛出，一种是使用try..catch捕获，一般的话，如果逻辑的异常，需要知道异常信息，我们往往选择将异常抛出，如果只是要保证程序在出错的情况下 依然可以继续运行，则使用try..catch来捕获。

但是try..catch会导致代码量的增加,让后期我们的代码变得臃肿且难以维护。当然，springboot作为一个如此优秀的框架，肯定不会坐视不管的，通过springboot自带的注解，我们可以方便的自定义我们的全局异常处理器，并且以json格式返回给我们的客户端。



## 代码实战：

### 捕获全局异常：

首先呢，我们新建我们负责全局异常捕捉处理的类:MyControllerAdvice，代码如下:

```java
@ControllerAdvice
public class MyControllerAdvice {


    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public Map<String,Object> exceptionHandler(Exception ex){
        Map<String,Object> map  = new HashMap<String,Object>();
        map.put("code",100);
        map.put("msg",ex.getMessage());
        //这里可以加上我们其他的异常处理代码，比如日志记录，，，
        return map;
    }

}

```



### 注解说明：

@ControllerAdvice 通过AOP的方式配合@ExceptionHandler()注解捕获在Controller层面发生的异常。如果需要扫描自定路径下的Controller，添加basePackages属性

```java
@ControllerAdvice(basePackages ="com.example.demo.controller")
```

@RestControllerAdvice : 和@ControllerAdvice作用相同，可以理解为  @ResponseBody+@ControllerAdvice 的组合使用。

@ExceptionHandler()：该注解作用主要在于声明一个或多个类型的异常，当符合条件的Controller抛出这些异常之后将会对这些异常进行捕获，然后按照其标注的方法的逻辑进行处理，从而改变返回的视图信息。

### 测试：

```java

@RestController
public class UserController {


    @GetMapping("/test")
    public String test(){
        int num = 1/0;
        return "Hello World";
    }


}

```

### 结果：

```json
{"msg":"/ by zero","code":100}
```



## 捕获自定义异常：

自定义我们的异常信息类MyException 并继承RuntimeException：

```java
public class MyException extends RuntimeException {

    private String errorCode;
    private String errorMsg;

    public MyException(String errorCode, String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }


    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
    
}

```



修改我们的MyControllerAdvice，将MyException添加进去：

```java
    @ResponseBody
    @ExceptionHandler(value = MyException.class)
    public Map<String,Object> myExceptionHandler(MyException mex){
        Map<String,Object> map  = new HashMap<String,Object>();
        map.put("code",mex.getErrorCode());
        map.put("msg",mex.getErrorMsg());
        //其他业务代码...
        return map;
    }
```

### 测试：

```java
    @GetMapping("/test1")
    public String test1(){
        String name = null;
        if(name == null){
            throw new MyException("101","用户名为空");
        }
        return "Hello World";
    }
```

### 输出：

```json
{"msg":"用户名为空","code":"101"}
```



完成~，关注我，有你好果子吃，哼。

