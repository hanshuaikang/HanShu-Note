# 写给后端的Nginx初级入门教程:配置高可用集群

在上一篇[写给后端的Nginx初级入门教程:实战篇](https://juejin.im/post/5db8f8c3f265da4d3e173c62)文章中我们从实际的代码出发，比较粗略地讲解了Nginx配置文件的结构，以及常用的功能比如复杂均衡，反向代理，动静分离的简单配置，事情到这里就结束了吗，当然没有，就拿负载均衡为例，还记得我们基础篇关于反向代理给的那张图吗？我们将它做一个小小的修改，变成我们的负载均衡图，如下图所示：

![image-20191101142112493](C:\Users\admin\Desktop\image-20191101142112493.png)

刚开始这么一看，也没发现什么问题啊，请求经过我们的负载均衡服务器，通过不同的策略分发到不同的服务器上进行处理，就算一台服务器挂了，也会有其他的服务器继续顶上，这个逻辑简直满分有木有，妈妈再也不用担心巨大的请求把我的服务器累崩啦。

等等，**看似平静的湖面下面其实往往波涛汹涌**，知识点呐，朋友们，面试可能要考的

可是，，万一，，我们负载均衡服务器挂了，，那不就彻底GG了？

卧槽，卧槽，卧槽，我怎么没有想到，那怎么办?

别慌，上有上策，下有对策，兵来将挡，水来土掩，为了防止这种被人掐脖子情况的发生，就有人提供了另外一种思路，是什么呢? 其实很简单，你原来不是只有一个负载均衡服务器么，挂了玩不起，我弄两台负载均衡服务器不就得了，一台挂了，我继续用另外一台不就好了吗，哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈，臭弟弟，没招了吧，于是上面的那张图就变成了这样:

![image-20191101142556157](C:\Users\admin\AppData\Roaming\Typora\typora-user-images\image-20191101142556157.png)

当我们向服务端发起请求的时候，用的地址并不是我们配置的两台Nginx服务器的IP地址，而是我们设置的虚拟IP地址，而两台Nginx服务器的地址则称为我们的工作IP，当比如5个请求（看我这没见过大场面的样子）来到我们的主Nginx服务器，我们主Nginx服务器一看来了这么多请求，于是。

**啊，好多请求，我死了。**

然后，由于两台Nginx服务器都是设置的这一个虚拟IP，当主Nginx服务器挂了之后，当我们再次访问的时候，实际上就是访问我们的**从Nginx服务器**来做具体的负载均衡了，**具体提供服务的IP地址也从主Nginx服务器的IP切换到了从Nginx服务器的Ip**，整个IP切换的过程对于用户来说是无感知的，其实有点类似于我们经常说的主从数据库。

然后整个工作IP地址变化的过程，业界起了一个特cool的名字，叫**IP漂移**。

当然，这个虚拟IP也不是说句话就配置好的，同样也需要第三方软件的支持，在这里我们使用的是keepalived这个软件来保证我们的nginx实现高可用集群配置的。

>  keepalived是什么？
>
>  keepalived是集群管理中保证集群高可用的一个服务软件，其功能类似于[heartbeat](https://github.com/chenzhiwei/linux/tree/master/heartbeat)，主要用来防止单点故障。 

你这不是脱裤子xx，多此一举吗，是，Nginx服务器挂了是没事了，可是你keepalived挂了怎么办？

卧，我早就想到可能会有人问这个问题，首先，我们的keepalived只是简单的做一个负载均衡的作用，真实的请求还是交给Nginx来负责的，所以，keepalived挂的概率要远远小于Nginx挂的概率，如果真挂了，那也真的没办法了。

说了这么多，具体怎么玩？好，那接下来，我们不废话，直接看东西。

## keepalived安装及配置文件详解：

### keepalived安装:

既然我们说的是nginx高可用集群的配置，首先，有很多先决条件我们得有吧，集群集群，你只有一台linux服务器搞不成啊，当然也不要求你准备几十台服务器出来，两台就好，毕竟成本高昂，当然对于朱一旦那样的劳力士男人这点服务器并不在话下，其次，最最最基本的，我们两台服务器要把Nginx装好，具体Nginx怎么装的，可以去看我之前的实战篇，所以为了实现Nginx的高可用集群配置，我们需要准备:

- 服务器两台，我这里是本地虚拟机，IP分别是**192.168.17.119 **和 **192.168.17.120** 
- 我们两台服务器都需要安装好**nginx**和**keepalived** 

过程略，骗你的，按照我们之前的习惯，这里只列举最简单的安装方法，打开我们两台虚拟机的命令行终端，输入yum命令进行安装:

```shell
yum install keepalived –y
```

### keepalived配置文件详解：

装好了之后呢，就是说配置了，我们keepalived的配置文件在哪呢？通常来说在etc/keepalived/keepalived.conf这个文件下，我们打开这个文件，由于内容实在太多，我这里就不一一列举了，但是这么多的配置并不都是需要我我们去配置的，相反，我们简单的配置**主从Nginx服务器**只需要其中的一小部分就可以了，于是呢，我们对keepalived.conf这个文件进行精简，精简过后的内容如下，大家可以直接把这部分内容替换掉原来的配置文件:

```shell
! Configuration File for keepalived

global_defs {
    
    ##邮件相关的配置
    notification_email {
    ###设置报警邮件地址，可以设置多个，每行一个。 需开启本机的sendmail服务
    acassen@firewall.loc
    failover@firewall.loc
    sysadmin@firewall.loc
    }
    #keepalived在发生诸如切换操作时需要发送email通知地址，表示发送通知的邮件源地址是谁  
    notification_email_from Alexandre.Cassen@firewall.loc
    #指定发送email的smtp服务器  
    smtp_server 192.168.17.129
    #设置连接smtp server的超时时间 
    smtp_connect_timeout 30
    
    ##这个比较重要， router_id 用来标识我们这台主机，故障发生时，发邮件时显示在邮件主题中的信息
    router_id Master_Nginx 
}

##检测脚本和权重参数
vrrp_script chk_http_port {
    script "/usr/local/src/nginx_check.sh" ##配置脚本的路径
    interval 2 #（检测脚本执行的间隔）
    weight 2 ##权重
}

vrrp_instance VI_1 {

    #指定keepalived的角色，MASTER表示此主机是主服务器，BACKUP表示此主机是备用服务器。注意这里的state指      定 instance(Initial)的初始状态，就是说在配置好后，这台服务器的初始状态就是这里指定的，  
    #但这里指定的不算，还是得要通过竞选通过优先级来确定。如果这里设置为MASTER，但如若他的优先级不及另外一       台，那么这台在发送通告时，会发送自己的优先级，另外一台发现优先级不如自己的高，  
    #那么他会就回抢占为MASTER   
    state MASTER  # 备份服务器上将 MASTER 改为 BACKUP 
    
    interface eth0 ###指定HA监测网络的接口。与本机 IP 地址所在的网络接口相同，可通过ip addr 查看
    virtual_router_id 51 #虚拟路由标识，这个标识是一个数字，同一个vrrp实例使用唯一的标识。即同一vrrp_instance下，MASTER和BACKUP必须是一致的  
    priority 100 # 主、备机取不同的优先级，主机值较大，备份机值较小，一般来说，主100 备 80
    advert_int 1 #设定MASTER与BACKUP负载均衡器之间同步检查的时间间隔，单位是秒     
	authentication { #设置验证类型和密码。主从必须一样
        auth_type PASS   #设置vrrp验证类型，主要有PASS和AH两种  
        auth_pass 123456 #设置vrrp验证密码，在同一个vrrp_instance下，MASTER与BACKUP必须使用相同的密码才能正常通信  
}

virtual_ipaddress {
    192.168.17.50 ## VRRP H 虚拟地址
    } 


}
```

在我们本例子中，配置文件主要分为三大块，分别是**global_defs** ，**vrrp_script chk_http_port**和

**vrrp_instance VI_1**，下面我们一个一个来说明它们具体在其中起到的作用:

#### global_defs 块:

全局配置，邮件通知等配置都在这里完成，比较重要的是这个router_id 这个选项，他用来标识我们这台主机，

那本例中Master_Nginx 是怎么来的呢,在我的host文件中：

```host
127.0.0.1 Master_Nginx
```

当然Master_Nginx 是我自己取的，大家也可以根据自己的习惯给自己的主机起名字。

#### vrrp_script chk_http_port 块:

看到script ，这不是脚本的意思么，是的，这一块呢，就主要是们的keepalived脚本配置，之前不是说了吗，我得有个方法知道你Nginx服务器挂了啊，这样我才能去切换成备用的服务器，这个脚本就是干这个事儿的。具体每一行是什么意思，看注释:

```shell
##检测脚本和权重参数
vrrp_script chk_http_port {
    script "/usr/local/src/nginx_check.sh" ##配置脚本的路径
    interval 2 #检测脚本执行的间隔，脚本每隔两秒执行一次，
    weight 2 ##权重，这个权重是什么意思呢，就是当我们主Nginx服务器挂了之后，就把该服务器的权重设置成2
}
```

而nginx_check.sh中的内容，我们会在配置文件的最后会贴出来。

#### vrrp_instance VI_1块：

第三块就比较重要了，这个主要用来我们虚拟IP的配置，具体的解释呢，我已经放在了上文代码注释中，唯一需要额外补充的一点是什么呢，virtual_ipaddress 我们的虚拟IP地址是可以有多个的，比如我有两个虚拟IP怎么写，直接换行就行:

```shell
virtual_ipaddress {
    192.168.17.50 ## VRRP H 虚拟地址
    192.168.17.51 
    } 
```



#### 附:

在/usr/local/src/ 路径下创建我们的nginx_check.sh 脚本。

脚本内容大致意思是，当我们发现我们当前nginx服务器挂了的时候，就顺便把keepalived也干没了，因为我们两台nginx服务器都同时装了keepalived，一台挂了，所以自然就切换到另外一台备用服务器上了，**这跟古代皇帝驾崩了，重任自然就落到太子身上是一个道理。**

```shell
#!/bin/bash
A=`ps -C nginx –no-header |wc -l`
if [ $A -eq 0 ];then
    /usr/local/nginx/sbin/nginx
    sleep 2
    if [ `ps -C nginx --no-header |wc -l` -eq 0 ];then
        killall keepalived
    fi
fi
```

弄完之后，记得这个脚本的路径要和配置文件中  script 值一致，别回来写好了keepalived找不着。

讲到这里，就已经把keepalived配置文件比较简单的部分讲完了，大家千万不要说看了我的教程之后以为学到了全部精髓，其实没有，这只是一部分，后面更多详细的配置需要大家在日后的使用过程中学习和了解。

## 配置高可用集群实战:

其实，在上面讲解配置文件的过程中，我们已经不知不觉间把我们主Nginx服务器的keepalived给悄咪咪配置好了，是吧，简单的配置其实没那么复杂的，下面就是配置我们的备用Nginx服务器了。大体上步骤依然是一样的。

依旧是替换我们的配置文件，不过需要大家主要的是，备用Nginx服务器和主Nginx服务器配置文件还是有那么一点点需要改动的地方，在本例中主要需要将 state 从MASTER 改为 BACKUP  ，优先级 priority 从100 改为 90 ，改过之后的内容如下:

```shell
vrrp_instance VI_1 {
    state BACKUP   # 备份服务器上将 MASTER 改为 BACKUP 
    interface eth0 ##网卡，可以从ipconfig这个命令查到
    virtual_router_id 51 # 主、备机的 virtual_router_id 必须相同
    priority 90 # 主、备机取不同的优先级，主机值较大，备份机值较小，一般来说，主100 备 80
    advert_int 1 ##心跳，秒，每隔一秒发送一个心态确认我们的Nginx服务器的存活情况
	authentication { ##密码验证
        auth_type PASS
        auth_pass 123456
}

virtual_ipaddress {
    192.168.17.50 ## VRRP H 虚拟地址
    } 

}
```

脚本内容是不需要修改的，直接上传到指定目录就行。

> 配置过程中需要注意的是，主服务器和备服务器之间的虚拟IP 地址也就是virtual_ipaddress  一定要是同一个。



### 测试:

```shell
#两台服务器分别启动nginx
nginx

#分别启动keepalived 服务
#centos 7 
systemctl start keepalived.service
#centos 6+
service keepalived start
```

在浏览器地址输入192.168.17.50 ，可以看到我们熟悉的Nginx欢迎界面。

为了验证keepalived在其中确实是起了作用的，我们手动把主Nginx服务器的keepalived和nginx服务关掉，然后再访问192.168.17.50

于是我们发现依然是可以正常访问的，熟悉的Nginx界面又出现在了我们眼中，可见我们的备用Nginx服务器排上用场了。

到这里就大功告成啦。

### 等等，貌似还有一点问题？

是的，今天当我充满自信容光焕发迈着六亲不认的步伐准备在阿里云上大展身手配个keepalived的时候，我愣住了，什么，阿里云现在不支持虚拟IP？？

卧槽，那这玩意儿你的意思说只能自己再本地玩？

当然不是，虽然配置稍微有那么一丢丢不同，但是整体还是大同小异的，于是我在网上找了两篇阿里云和腾讯云的keepalived的配置教程，当然，普通的ECS对此注定绝缘了，不过转念一想，都上集群了，公司买个vpc服务器的钱应该还是有的。教程链接如下:

阿里云:[在VPC环境中利用keepalived实现主备双机高可用](https://yq.aliyun.com/articles/438705/)

腾讯云:[VPC 内通过 keepalived 搭建高可用主备集群](https://cloud.tencent.com/document/product/215/20186)

### 下面开始技术总结：

本篇文章呢，我们通过使用keepalived简单实现了Nginx服务的高可用集群的配置，虽然配置很简陋，但是对于初学者我想已经可以实现通过keepalive和Nginx来实现自己项目的高可用运行了。但这并不意味着这就是全部，**keepalived同时支持双主模式**，碍于本文的篇幅，这里就不额外补充了，但是掌握了主备模式的配置，对于双主模式的配置我想也是很容易学会的事情，最后，非常感谢阅读本篇文章的小伙伴们，能够帮助到你们对于我来说是一件非常开心的事儿，如果有什么疑问或者批评欢迎留言到本篇文章下方，有时间的话我会一一回复。

韩数的学习笔记目前已经悉数开源至github，一定要点个**star**啊啊啊啊啊啊啊

**万水千山总是情，给个star行不行**

[韩数的开发笔记](https://github.com/hanshuaikang/HanShu-Note)

欢迎点赞，关注我，**有你好果子吃**（滑稽）







