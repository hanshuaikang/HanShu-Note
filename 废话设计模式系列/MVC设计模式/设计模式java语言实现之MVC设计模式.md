# 设计模式java语言实现(七)之MVC设计模式

### 前言：

作者：韩数

Github：https://github.com/hanshuaikang

个人技术博客:http://jdkcb.com/

本篇文章电子版和配套代码下载地址:https://github.com/hanshuaikang/design-pattern-java

时间:2019-07-31

Jdk版本：1.8

### MVC设计模式定义：

MVC 模式代表 Model-View-Controller（模型-视图-控制器） 模式。这种模式用于应用程序的分层开发。

其中MV将传统的应用分为三层，分别为Model(模型)，View(视图)  Controller(控制器)，其中：

- **Model（模型）** - 模型代表一个存取数据的对象或 JAVA POJO。它也可以带有逻辑，在数据变化时更新控制器。
- **View（视图）** - 视图代表模型包含的数据的可视化,例如表单、网页等。
- **Controller（控制器）** - 控制器作用于模型和视图上。它控制数据流向模型对象，并在数据变化时更新视图。它使视图与模型分离开。

### 优缺点：

**优点**：简化开发，提高代码复用，降低代码耦合，部署快，容易维护，可以做到在不修改任何后端逻辑的情况下修改前端视图。

**缺点**：不适用于小项目，会增加项目的复杂性，会增加代码量。

### 应用场景：

java 的SpringMVC struts 框架 python 的flask框架都对mvc设计模式提供了显著的支持。

### 微剧场：

为了庆祝韩数今天王者荣耀终于上了钻石，阿呆决定在小区里面做一个黑板报状告天下，由于阿呆小时候受过严格的训练，很快韩数上荣耀钻石的事情就路人皆知。每次韩数走在街上，路人无不投来羡慕的眼光。同样的，在作者韩数的巧妙安排下，阿呆画黑板报的水平在方圆五米名声大噪（不这样的话这篇文章写不下去了），于是就有很多铂金菜弟弟慕名而来，想请阿呆去他们小区画黑板报。刚开始的适合来找的人比较少，所以阿呆一个人做文案，设计，绘画倒还忙的过来，但随着需求量的增加，阿呆一个人已经忙不过来了，尽管每个黑板报的内容都大同小异，无非都是一些**"总有一天我也会像韩数大人那样成为荣耀钻石的！"**这些一听就很搞笑的豪言壮语。但是阿呆仍然都要每次自己重复整个流程。

每当夜深人静的时候阿呆就会趁着月光独自思考，怎么样才能提高做黑板报的效率呢？当然以阿呆的智商自己是想不出来的，这点光看前面的文章就可以猜到。于是阿呆找到了我这个世界上最聪明最善良最智慧的宝藏男孩寻求帮助，扭捏了很久我告诉他家传绝学，**MVC设计模式。**

传统的黑板报生产方式，都是一个人负责所有流程，这当然没有什么问题，可是大家想，随着韩数荣耀钻石名气的增加，越来越多的人都会来找阿呆做黑板报，这样的需求量单靠阿呆一个人必然是无法处理完的，所以往往呢，阿呆就会找来很多做黑板报的人去对接，因为每个人要负责做黑板报的整个流程，这样就会导致什么问题呢，如果只有20个黑板报制作者，但是却有100个人要做黑板报，即使全员出动，仍然是有80个人是需要等待的，传统的思路解决这样的问题的方案就是把黑板报制作者增加到100的人，看过我前面文章的小伙伴肯定已经明白，这么硬刚是不行的，为了解决这个问题呢，MVC设计模式应运而生了。



/***

之前写了一段mvc设计模式改造之后的黑板生产流程，写完发现不太理想，大家直接看代码吧。

***/

### 代码实战：

首先 新建一个黑板类作为我们的Model层，负责数据的存储和处理。

```java
//黑板报作为我们的的Model层
public class BlackBoard {
	
	
	private String content;//黑板报的内容
	private String style;//黑板报的风格
	
	
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getStyle() {
		return style;
	}
	public void setStyle(String style) {
		this.style = style;
	}



}

```

然后新建一个Draw类，作为我们的view层，用于数据的显示。

```java
public class Draw {	
	public void show(BlackBoard b) {
		System.out.println("这是一个"+b.getStyle()+"的黑板报内容是："+b.getContent());
	}

}
```

新建一个Controller类，作为我们的控制层，用于控制数据的存储和显示

```java
public class Controller {
	
	private BlackBoard blackBoard = new BlackBoard();
	private Draw draw;
	

	public void setDraw(Draw draw) {
		this.draw = draw;
	}

	public void setContent(String content) {
		blackBoard.setContent(content);
	}

	public void setStyle(String style) {
		blackBoard.setStyle(style);
	}
	
	
	public void show() {
		draw.show(blackBoard);
		
	}
	

}
```

**测试:**

```java
//测试实例，前端给Controller层提交属性，Controller交给model层保存，然后再由view层打印出来。
public class MVCTest {
	
   public static void main(String[] args) {
	   
	   Controller c = new Controller();
	   Draw d = new Draw();
	   c.setContent("葬爱家族");
	   c.setStyle("杀马特风格的");
	   c.setDraw(d);
	   c.show();
	   
	   //当需要改风格的时候
	   c.setStyle("唯美风格的");
	   c.show();
	
	   
  }
}
```



**输出**:

```text
这是一个杀马特风格的的黑板报内容是：葬爱家族
这是一个唯美风格的的黑板报内容是：葬爱家族
```



同样的：**可以做到在不修改任何后端逻辑的情况下修改前端视图** 也就很容易实现了，我们可以新建一个view类替换 或者 直接修改原来的view代码就好，而不会对model层和controller层产生任何影响。从而大大降低了我们代码之间的耦合。

```java
public class Draw {	
	public void show(BlackBoard b) {
		System.out.println("这是一个新的黑板报，风格是"+b.getStyle()+"内容是："+b.getContent());
	}

}
```



很多同学看到这里依然没有办法去理解MVC设计模式相较于传统的方式具体提升在哪里，那么我们现在以传统的非MVC方式来实现这个黑板报例子：

```java
public class tradition {
	
	private String content;//黑板报的内容
	private String style;//黑板报的风格
	
	
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getStyle() {
		return style;
	}
	public void setStyle(String style) {
		this.style = style;
	}

	private void show() {
		System.out.println("这是一个"+ style +"的黑板报内容是："+content);

	}
	
}
```



这个时候很多同学纳闷了，心理卧槽，我怎么感觉传统的更好？？？！

所以对于小项目来说，使用mvc模式就只是存粹提高代码量了，并没有办法起到很好的作用，但是大家不妨这样想，如果把model view controller 所有的逻辑都封装到一个类中，当项目很大的时候，势必会造成单个代码文件十分臃肿，每次修改代码都要修改整个文件，带来维护上的难度，而且同样的，如果此刻来了一个新的类，也需要某些通用的操作，那么他实例化一个黑板类去调用它的保存数据对应的方法显然是不合适的。所以一般项目比较大的时候，我们通常会使用MVC设计模式，把一些数据库操作封装到model层中，作为我们的共用类，所有controller都可以调用，同样比如我们需要修改数据库存储的逻辑，也同样只需要单独修改model类对应的方法内部的代码就好了。做过java web开发的小伙伴们，SpringMVC中的service层，controller层，和我们前端的jsp/html，就是使用的MVC设计模式。



本期笔记没有总结，我们下篇文章再见！



















