# 写给后端的Hadoop初级入门教程（三）：Hadoop运行环境搭建

## 前言：

在在上一篇文章[写给后端的Hadoop初级入门教程：Hadoop组成部分](https://juejin.im/post/5df1e43451882512670ed134)中我们初略地叙述了`Hadoop`的三个重要组成部分 `Map - Reduce` ， `Yarn` ， `HDFS` 。难道到此一切都结束了吗， no no no，好戏才刚刚开始，既然要学习`Hadoop`，那么首先我们要解决的就是运行环境的问题，毕竟我个人认为学习编程最好的方式应该边看书边敲键盘，如果仅仅看书云学习的话，理论性的东西还好，但是代码性质的内容如果不敲一遍的话就会一个导致很明显的问题，就是自己觉得明明会了，写出来的程序却一直报错，整体学习效率是十分感人的。

不废话，直接上东西。

## 虚拟机：

首先，在学习`hadoop`之前。你需要一个虚拟机，**当然如果你恰好财力雄厚**，也可以自己买个服务器，或者后期买一组服务器做个集群,这样学习起来就方便多了。

因为考虑到我们之后要搭建集群，可能要同时启动多台linux主机，这个时候虚拟机就是一个非常合适的选择，特别是对于我们大家主要以学习为目的的人来说。然后这就要求你的电脑配置可能要稍微好一点，以我本人为例，I5+8G配置的台式机，同时运行三台虚拟机还是有点吃力的。

创建一个新的用户，我这里是 `hanshu`,并配置`hanshu`用户具有`root`权限。

在/opt目录下创建两个文件夹，分别是`module`和`software`

```shell
sudo mkdir module
sudo mkdir software
```

修改`module`和`software`文件夹所有者为`hanshu`

```shell
sudo chown hanshu:hanshu module/ software/
```

到此，我们虚拟机的基本准备就已经算是完成了。

## 设置java环境：

我们本次选择使用的`linux`发行版是`centos7`系统，`centos7`默认是带了`java`环境的，但由于`centos7`自带的`openjdk`并没有增加对`java `监控命令` jps`的支持。目前有两种方案可以解决这个问题，第一种是卸载原有的`openjdk`进行重装，第二个是通过yum安装`jdk`开发插件。

首先我们查看我们本机的`Openjdk`版本：

```shell
rpm -qa | grep openjdk
```

我这里是`java 1.8 `版本，然后执行yum命令安装我们对应版本的`jdk`开发插件：

```shell
yum install -y  java-1.8.0-openjdk-devel
```

第三步则是在我们`/etc/profile`文件添加我们`java`的环境变量，具体的操作我就不列出来了，最后我会把我`/etc/profile`的内容贴出来供大家进行参考。

## 安装Hadoop:

首先第一步是下载我们的`Hadoop`，我这里选用的`Hadoop2.7.2`版本，我知道到这里很多小伙伴可能会问了:

`Hadoop3.x`既然都已经出来了，那为啥不用`3.x`呢，

这里我想说的是，我们学会了一个版本做知识更新的成本是很低的，比如你掌握了`java 1.6` ，再去使用`java 1.8`的时候，其实是很快就可以过度完成的。而且以目前我了解到的情况来说，目前企业使用的最多的版本还是`Hadoop2.x`版本，毕竟企业追求的是开发的稳定性，但未来`Hadoop 3.x`版本一定会是一个趋势。

`Hadoop`下载地址:

https://archive.apache.org/dist/hadoop/common/hadoop-2.7.2/

使用`Xshel`l或者其他的linux终端管理工具将我们下载好的`Hadoop`安装包上传至我们上文创建好的/opt/software目录下。

解压该压缩包至/opt/module目录:

```shell
tar -zxvf hadoop-2.7.2.tar.gz -C /opt/module/
```

### 将Hadoop添加到环境变量：

在这里我就不一一展示具体的过程了，无非是把目录添加至/etc/profile文件里面，我直接贴出来我的/etc/profile相关的配置文件信息，如下:

```shell
##JAVA_HOME
export JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk/jre/
export JRE_HOME=$JAVA_HOME/jre
export CLASSPATH=$JAVA_HOME/lib:$JRE_HOME/lib:$CLASSPATH
export PATH=$JAVA_HOME/bin:$JRE_HOME/bin:$PATH

##HADOOP_HOME 
export HADOOP_HOME=/opt/module/hadoop-2.7.2 
export PATH=$PATH:$HADOOP_HOME/bin 
export PATH=$PATH:$HADOOP_HOME/sbin
```

执行命令使配置生效:

```shell
source /etc/profile
```

终端执行`hadoop version`命令，查看`hadoop`是否安装成功:

```shell
[hanshu@hadoop100 ~]$ hadoop version
Hadoop 2.7.2
```

当出现`hadoop`版本信息时，则代表我们`hadoop`运行环境已经配置成功了。

### Hadoop目录结构:

前面光想着解压了，也忘了点进去看看里面都有些啥，和`java`一样，`Hadoop`也有着清晰的目录结构用来堆放对应的内容，接下来我们列几个重要目录简单地阐述一下它们的作用:

- **bin目录**:存放对Hadoop相关服务（HDFS,YARN）进行操作的脚本.
- **etc目录**：Hadoop的配置文件目录，存放Hadoop的配置文件等信息。
- **lib目录**：存放Hadoop的本地库（对数据进行压缩解压缩功能）。
- **sbin目录**：存放启动或停止Hadoop相关服务的脚本。
- **share目录：**存放Hadoop的依赖jar包、文档、和官方案例，比如wordCount等。

## 下面开始技术总结:

今天这篇文章，我们简单地过了一遍`Hadoop`基本运行环境的配置。因为很多操作实在是太过于基础，比如查看文件目录，配置环境变量，使用vim编辑器等等这些操作都应该是一个java程序员的基本操作，所以就没有做非常详细的叙述，当然，如果有不明白的同学可以去谷歌或者百度查阅相关资料，整体配置成功还是不复杂的。下一节呢，我们将通过修改`Hadoop`的配置文件，实现`hadoop`伪分布式环境的搭建，等我周六考完试，后面更新频率大概会维持在两天一更这样的进度，比较马上要放寒假了，随我好多年的笔记本跑不起来集群了。

非常感谢能读到这里的朋友，你们的支持和关注是我坚持高质量分享下去的动力。

相关代码已经上传至本人github。一定要点个**star**啊啊啊啊啊啊啊

**万水千山总是情，给个star行不行**

[韩数的开发笔记](https://github.com/hanshuaikang/HanShu-Note)

欢迎点赞，关注我，**有你好果子吃**（滑稽）









