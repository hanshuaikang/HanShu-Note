# 写给后端的Hadoop初级入门教程（四）：Hadoop伪分布式环境搭建:

## 前言:

我又回来了，这两天学校事情有点多，所以，没有怎么勤快更新，昨天看到有小伙伴留言催更，别说，第一次被催更的感觉还是挺幸福的，回到正题，在上一篇文章[写给后端的Hadoop初级入门教程（三）：Hadoop运行环境搭建](https://juejin.im/post/5df30b84e51d455824270e38)中，我们主要讲了一下Hadoop的基本环境的搭建，这个虽然简单，但是是非常重要的，如果基本环境没有，后面代码跑不起来说啥都是扯蛋，Hadoop虽然是大数据分布式框架，但是不是谁都能搞起来一套分布式环境，有的电脑配置比较低，跑一台虚拟机就已经不堪重负了，跑个集群就炸了，但是也想使用hadoop。这怎么办呢，于是Hadoop就支持另外一种模式运行，那就是伪分布式运行模式，就是不是分布式运行，但是比较像分布式。今天呢，我们就从伪分布式运行模式的配置入手，通过一步一步修改配置文件，来实现我们Hadoop的伪分布式运行的一个配置过程。

不说废话，直接上东西。

## 启动HDFS

首先我们要做的就是启动配置并启动我们的Hadoop分布式文件系统，找到我们的hadoop环境配置文件，`hadoop-env.sh`,在我这里路径如下： 

```shell
/opt/module/hadoop-2.7.2/etc/hadoop
```

然后就是我们需要拿到我们之前配置的JAVA_HOME地址，命令如下：

```shell
[hanshu@hadoop100 hadoop]$ echo $JAVA_HOME
/usr/lib/jvm/java-1.8.0-openjdk/jre/
```

使用vim编辑器打开hadoop-env.sh文件,我这里只列举一部分

```shell
# The java implementation to use.
export JAVA_HOME=${JAVA_HOME}

# The jsvc implementation to use. Jsvc is required to run secure datanodes
# that bind to privileged ports to provide authentication of data transfer
# protocol.  Jsvc is not required if SASL is configured for authentication of
# data transfer protocol using non-privileged ports.
#export JSVC_HOME=${JSVC_HOME}

export HADOOP_CONF_DIR=${HADOOP_CONF_DIR:-"/etc/hadoop"}

```

修改JAVA_HOME的值为我们上文获取到的java安装目录：

```shell
# The java implementation to use.
export JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk/jre/
```

保存，配置OK。

然后我们需要配置我们的NameNode 和我们的DataNode，这个需要在core-site.xml这个配置文件里面配置，这个配置文件仍然在我们的etc/hadoop目录下





使用vim打开，内容如下:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>
<!--
 省去一堆注释
-->
<configuration>
</configuration>

```

可以看到里面空空如也，如一块尚未开垦的荒地，我们略微一发善心，将它充分利用，在里面添加如下内容:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>
<!--
 省去一堆注释
-->
<configuration>


<!-- 指定HDFS中NameNode的地址 -->
<property>
<name>fs.defaultFS</name>
    <value>hdfs://localhost:9000</value>
</property>

<!-- 指定Hadoop运行时产生文件的存储目录 -->
<property>
	<name>hadoop.tmp.dir</name>
	<value>/opt/module/hadoop-2.7.2/data/tmp</value>
</property>

</configuration>
```



这还没完，之前我们好像提到过，默认hadoop文件是会创建三个副本，这我现在伪分布式，只有一个电脑，只有一个地方存东西，变不出来仨，所以接下来我们需要修改hadoop默认副本数量为1，这个配置文件叫:hdfs-site.xml 还在这个目录下，使用vim打开一探究竟:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>
<!-- 空空如也. -->
<configuration>

</configuration>

```

啥也没有，没关系，加上我们的配置不就有了吗，如下:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>
<!-- 空空如也. -->
<configuration>
<!-- 指定HDFS副本的数量 -->
<property>
	<name>dfs.replication</name>
	<value>1</value>
</property>
</configuration>
```

这个时候是不是就能启动我们的HDFS系统了？理论上来说是没问题的，但是第一次启动，还是建议大家先将NameNode格式化一下，这一步的作用就类似于很多时候我们买来U盘，硬盘需要先格式化一下再用是一个道理，当然，没人没事儿就格式化自己U盘吧，所以除了第一次也不建议大家没事儿就给NameNode卡卡格式化一下，因为**格式化NameNode，会产生新的集群id,导致NameNode和DataNode的集群id不一致，集群找不到已往数据。所以，格式NameNode时，一定记得要先删除data数据和log日志，然后再格式化NameNode，要不就有可能导致集群启动失败**

格式化NameNode:

```linux
[hanshu@hadoop100 hadoop-2.7.2]$ bin/hdfs namenode -format
```

输出的内容太多了，我随便复制给你们看这篇文章分分钟成万字长文了，所以如果最后没有报错，那就是格式化成功了，很少有格式化失败的情况发生。

启动NameNode:

```linux
[hanshu@hadoop100 hadoop-2.7.2]$ sbin/hadoop-daemon.sh start namenode
starting namenode, logging to /opt/module/hadoop-2.7.2/logs/hadoop-hanshu-namenode-hadoop100.out
```

启动DataNode:

```linux
[hanshu@hadoop100 hadoop-2.7.2]$ sbin/hadoop-daemon.sh start datanode
starting datanode, logging to /opt/module/hadoop-2.7.2/logs/hadoop-hanshu-datanode-hadoop100.out
```

弄完之后，这下我们之前点名配置的java 开发插件 jps就派上用场了，用一下子如下:

```linux
[hanshu@hadoop100 hadoop-2.7.2]$ jps
4243 Jps
4038 NameNode
4157 DataNode
```

至此，我们的Hdfs算是配置完成了，当然，这还不够，你口口声声说什么分布式文件系统，那难道就没有什么文件管理器这种让我能看着（zao）的东西吗？当然有，hadoop提供了web端来帮助我们直观的查看HDFS文件系统，网址如下:

http://localhost:50070/dfshealth.html#tab-overview

打开后长这样

图

不过首页我们一般不怎么用，真正查看文件的地方在，算了，懒得打字了，直接看图吧:

图2

**课外小知识:**

> hadoop 日志文件在 /opt/module/hadoop-2.7.2/logs 路径下面，如果集群启动失败，或者出现错误，可以在这里查看错误信息。



既然是文件系统，那肯定得支持什么创建文件夹，创建文件这种常规操作吧，那必须的，由于今天我们的重点是配置，不是使用，所以我就简单列举几个先，日后我写到HDFS的时候，再详细地列举文件操作的命令 ，说话算话。

```linux
创建一个文件夹
[hanshu@hadoop100 hadoop-2.7.2]$ bin/hdfs dfs -mkdir -p /user/hanshu/input
把我刚刚创建的hanshuzuishuai这个文件传到刚才创建的文件夹里面去:
[hanshu@hadoop100 hadoop-2.7.2]$ bin/hdfs dfs -put hanshuzuishuai.txt /user/hanshu/input/
查看上传的文件是否正确:
[hanshu@hadoop100 hadoop-2.7.2]$ bin/hdfs dfs -ls  /user/hanshu/input/
Found 1 items
-rw-r--r--   1 hanshu supergroup         13 2019-12-20 15:07 /user/hanshu/input/hanshuzuishuai.txt
[hanshu@hadoop100 hadoop-2.7.2]$ 

查看文件内容:
[hanshu@hadoop100 hadoop-2.7.2]$ bin/hdfs dfs -cat  /user/hanshu/input/hanshuzuishuai.txt
韩数最帅

你看看，这虚拟机，瞎说什么大实话，哎，管不住了，大家见谅，别和小虚拟机一般见识。

下载文件内容:
[hanshu@hadoop100 hadoop-2.7.2]$ hdfs dfs -get /user/hanshu/input/hanshuzuishuai.txt

删除文件内容:
[hanshu@hadoop100 hadoop-2.7.2]$ hdfs dfs -rm -r /user/hanshu/input/hanshuzuishuai.txt
```

到这才算Hdfs写完了，累死我了，从两点敲到三点多，才把Hdfs写完，早知道出个三部曲了，分开写，一天写一点，算了，今天写完吧，继续干：

## 配置并启动Yarn

HDFS我们是配置完了，剩下的就是配置我们的Yarn了，真的，如果现在你Yarn还不知道是啥，我建议你把我上上一篇组成部分看看再来看这块内容，配置Yarn也不复杂，也是修改那几个配置文件就行了。

首先第一个落网的就是yarn-env.sh，第一个就改它，还在那个etc/hadoop 路径下面。使用vim打开：

```shell
# User for YARN daemons
export HADOOP_YARN_USER=${HADOOP_YARN_USER:-yarn}

# resolve links - $0 may be a softlink
export YARN_CONF_DIR="${YARN_CONF_DIR:-$HADOOP_YARN_HOME/conf}"

# some Java parameters
# export JAVA_HOME=/home/y/libexec/jdk1.6.0/

```

配置JAVA_HOME:

```shell
 export JAVA_HOME= /usr/lib/jvm/java-1.8.0-openjdk/jre/
```

第二个不幸要被改的是yarn-site.xml，使用vim打开：

```xml
<?xml version="1.0"?>
<configuration>

<!-- 依然空空如也 -->

</configuration>
```

没关系，马上就不空了，添加如下内容:

```xml
<?xml version="1.0"?>
<configuration>
<!-- Reducer获取数据的方式 -->
<property>
 		<name>yarn.nodemanager.aux-services</name>
 		<value>mapreduce_shuffle</value>
</property>

<!-- 指定YARN的ResourceManager的地址 -->
<property>
<name>yarn.resourcemanager.hostname</name>
<value>localhost</value>
</property>
</configuration>
```

第三个轮到我们的mapred-env.sh 配置文件了，使用vim打开,

看到JAVA_HOME了吗，OMG，兄弟们，朋友们，就这个，配他！（李佳奇梗）

```shell
export JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk/jre/
```

最后就是修改mapred-site.xml了，这个时候有人要问了，我没找着啊，完了，我没这个文件，我和韩数脱轨了，这以后咋学啊，学不会了，回去卖哈密瓜去了。

淡定，看到那个mapred-site.xml.template文件了吗，我们把它改成mapred-site.xml，然后再配它：

```linux
[hanshu@hadoop100 hadoop]$ mv mapred-site.xml.template mapred-site.xml
```

使用vim打开，大家猜里面会是什么？每错，空空如也，修改配置文件如下：

```xml
<?xml version="1.0"?>
<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>

<configuration>
<!-- 指定MR运行在YARN上 -->
<property>
      <name>mapreduce.framework.name</name>
      <value>yarn</value>
</property>
</configuration>

```

到这，我们的yarn也算配好了，那是不是能启动了？

且慢，我们之前说过，Yarn是干啥的，做资源调度的，所以启动Yarn之前我们的NameNode 和 DataNode一定要启动起来，要不Yarn启动的时候一看，没资源让自己调度，那还不如回家睡觉呢。

启动ResourceManager：

```linux
[hanshu@hadoop100 hadoop-2.7.2]$ sbin/yarn-daemon.sh start resourcemanager
starting resourcemanager, logging to /opt/module/hadoop-2.7.2/logs/yarn-hanshu-resourcemanager-hadoop100.out
```

启动NodeManager:

```linux
[hanshu@hadoop100 hadoop-2.7.2]$ sbin/yarn-daemon.sh start nodemanager
starting nodemanager, logging to /opt/module/hadoop-2.7.2/logs/yarn-hanshu-nodemanager-hadoop100.out
```

用jps命令看看启动成功了没：

```linux
[hanshu@hadoop100 hadoop-2.7.2]$ jps
5986 Jps
5620 ResourceManager
4038 NameNode
4157 DataNode
5870 NodeManager
```

欧了，这个时候肯定会有人心里面想，你怎么这么熟练呢，hhh，原因很简单，我怎么可能把我中间配错的那些坑写文章里（一脸委屈）。

同样的，Yarn也提供了相关的web端来帮助我们查看和使用：

http://localhost:8088/cluster



到这一步，我们Yarn也算是配置完成了。



## 配置并启动历史服务器

为了查看程序的历史运行情况，需要配置一下历史服务器。这个就好配多了，打开我们的`mapred-site.xml`配置文件，加入以下内容:

```xml
<!-- 历史服务器端地址 -->
<property>
<name>mapreduce.jobhistory.address</name>
<value>localhost:10020</value>
</property>
<!-- 历史服务器web端地址 -->
<property>
    <name>mapreduce.jobhistory.webapp.address</name>
    <value>localhost:19888</value>
</property>
```

**启动历史服务器**:

```linux
[hanshu@hadoop100 hadoop-2.7.2]$ sbin/mr-jobhistory-daemon.sh start historyserver
starting historyserver, logging to /opt/module/hadoop-2.7.2/logs/mapred-hanshu-historyserver-hadoop100.out
```

使用jps查看是否启动成功:

```linux
[hanshu@hadoop100 hadoop-2.7.2]$ jps
5620 ResourceManager
6565 Jps
4038 NameNode
4157 DataNode
5870 NodeManager
6479 JobHistoryServer
```

不用多说，web端，也有

http://localhost:19888/jobhistory

结束了吗？没有，感觉有好多东西要配啊。

## 配置并启动日志聚集

什么是日志聚集呢，就是班委收作业，我们把日志都收集起来放一块，以后程序再出了什么问题，方便调试:

因为要重新开始聚集日志，所以要重新启动NodeManager 、ResourceManager和HistoryManager。

同样需要修改配置文件，感觉自己又学会了一项编程方法：

**面向配置编程**。

配置`yarn-site.xml`

增加如下配置：

```xml
<!-- 日志聚集功能使能 -->
<property>
<name>yarn.log-aggregation-enable</name>
<value>true</value>
</property>

<!-- 日志保留时间设置7天 -->
<property>
<name>yarn.log-aggregation.retain-seconds</name>
<value>604800</value>
</property>
```

关闭NodeManager

```linux
[hanshu@hadoop100 hadoop-2.7.2]$ sbin/yarn-daemon.sh stop resourcemanager
stopping resourcemanager
```

关闭ResourceManager：

```linux
[hanshu@hadoop100 hadoop-2.7.2]$ sbin/yarn-daemon.sh stop nodemanager
stopping nodemanager
```

关闭HistoryManager：

```linux
[hanshu@hadoop100 hadoop-2.7.2]$ sbin/mr-jobhistory-daemon.sh stop historyserver
stopping historyserver
```

启动NodeManager：

```linux
[hanshu@hadoop100 hadoop-2.7.2]$ sbin/yarn-daemon.sh start nodemanager
starting nodemanager, logging to /opt/module/hadoop-2.7.2/logs/yarn-hanshu-nodemanager-hadoop100.out
```

启动ResourceManager：

```linux
[hanshu@hadoop100 hadoop-2.7.2]$ sbin/yarn-daemon.sh start resourcemanager
starting resourcemanager, logging to /opt/module/hadoop-2.7.2/logs/yarn-hanshu-resourcemanager-hadoop100.out
```

启动HistoryManager：

```linux
[hanshu@hadoop100 hadoop-2.7.2]$ sbin/mr-jobhistory-daemon.sh start historyserver
starting historyserver, logging to /opt/module/hadoop-2.7.2/logs/mapred-hanshu-historyserver-hadoop100.out
```

到这里我们的伪分布式环境已经配置完成啦。



