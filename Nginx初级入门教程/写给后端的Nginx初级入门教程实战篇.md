# 写给后端的Nginx初级入门教程:实战篇

在上一篇的文章[写给后端的Nginx初级入门教程:基础篇](https://juejin.im/post/5da41d41f265da5bb86ac720)中，我们主要说了Nginx是什么，能做什么？以及Nginx最重要的四个基本概念，分别是 **正向代理**,  **反向代理，负载均衡，以及动静分离** 。

本章作为实战篇，将从实际的命令行出发，通过安装，启动，配置Nginx来逐渐认识和使用Nginx，并能够自己实现一些简单的反向代理，负载均衡的配置。

不废话，直接上干货。

## Nginx安装：

Nginx的安装还是比较容易的，有离线安装，在线安装多种安装方式，这里我只说最简单的一种，打开我们的命令行终端，直接输入yum命令进行安装

```shell
yum install -y nginx
```

当终端显示出Complete!字样时，则代表我们的Nginx已经安装成功了。

查看Nginx版本：

```shell
nginx -v
#在这里我安装的是1.16.1版本的nginx
```

## Nginx基本操作:

和我们之前的docker一样，nginx也有一些包括服务的启动，停止，重载等基本操作。

启动nginx：

```shell
##在centos7+ 启动nginx服务
systemctl start nginx.service
#centos6+ 上启动nginx服务
service nginx start
#或，简单粗暴一句
nginx
```

停止nginx:

```shell
##在centos7+ 停止nginx服务
systemctl stop nginx.service
#centos6+ 上停止nginx服务
service nginx stop
#粗鲁的停止，下班了，不干了，就算请求来了我也不接了。
nginx -s stop
##优雅的停止，Nginx在退出前完成已经接受的连接请求。
nginx -s quit
```

重启nginx：

当我们修改了nginx的某些配置，为了使配置生效，我们往往需要重启nginx，同样的，linux下依然有两种方式来重启我们的nginx服务:

```shell
##在centos7+ 重启nginx服务
systemctl restart nginx.service
#centos6+ 上重启nginx服务
service nginx restart
#使用nginx命令停止，推荐这个
nginx -s reload
```

而具体使用nginx原生的nginx -s 操作还是linux提供的systemctl ，这个主要看个人喜好，实际两者的功能是差不多的，并没有什么明显的不同。

其他命令：

查看配置文件是否ok：

```shell
#如果配置文件有问题的话会显示failed，如果没得问题的话，会显示successful
nginx -t
```

显示帮助信息:

```shell
nginx -h 
#或者
nginx -?
```

## Nginx配置：

nginx本身作为一个完成度非常高的负载均衡框架，和很多成熟的开源框架一样，大多数功能都可以通过修改配置文件来完成，使用者只需要简单修改一下nginx配置文件，便可以非常轻松的实现比如反向代理，负载均衡这些常用的功能，同样的，和其他开源框架比如tomcat一样，nginx配置文件也遵循着相应的格式规范，并不能一顿乱配，在讲解如何使用nginx实现反向代理，负载均衡等这些功能的配置前，我们需要先了解一下nginx配置文件的结构。

既然要了解nginx的配置文件，那我总得知道nginx配置文件在哪啊，nginx配置文件默认都放在nginx安装路径下的conf目录，而主配置文件nginx.conf自然也在这里面，我们下面的操作几乎都是对nginx.conf这个配置文件进行修改。

可是，我怎么知道我nginx装哪了？我要是不知道nginx装哪了咋办？

这个，细心的朋友们可能会发现，运行nginx -t命令，下面除了给出nginx配置文件是否OK外，同时也包括了配置文件的路径。诺，就是这个

```shell
[root@izuf61d3ovm5vx1kknakwrz ~]# nginx -t
nginx: the configuration file /etc/nginx/nginx.conf syntax is ok
nginx: configuration file /etc/nginx/nginx.conf test is successful
```

使用vim打开该配置文件，我们一探究竟，不同版本的配置文件可能稍有不同，我的配置文件内容如下：

```shell
# For more information on configuration, see:
#   * Official English Documentation: http://nginx.org/en/docs/
#   * Official Russian Documentation: http://nginx.org/ru/docs/

user nginx;
worker_processes auto;
error_log /var/log/nginx/error.log;
pid /run/nginx.pid;

# Load dynamic modules. See /usr/share/doc/nginx/README.dynamic.
include /usr/share/nginx/modules/*.conf;

events {
    worker_connections 1024;
}

http {
    log_format  main  '$remote_addr - $remote_user [$time_local] "$request" '
                      '$status $body_bytes_sent "$http_referer" '
                      '"$http_user_agent" "$http_x_forwarded_for"';

    access_log  /var/log/nginx/access.log  main;

    sendfile            on;
    tcp_nopush          on;
    tcp_nodelay         on;
    keepalive_timeout   65;
    types_hash_max_size 2048;

    include             /etc/nginx/mime.types;
    default_type        application/octet-stream;

    # Load modular configuration files from the /etc/nginx/conf.d directory.
    # See http://nginx.org/en/docs/ngx_core_module.html#include
    # for more information.
    include /etc/nginx/conf.d/*.conf;

    server {
        listen       80 default_server;
        listen       [::]:80 default_server;
        server_name  _;
        root         /usr/share/nginx/html;

        # Load configuration files for the default server block.
        include /etc/nginx/default.d/*.conf;

        location / {
        }

        error_page 404 /404.html;
            location = /40x.html {
        }

        error_page 500 502 503 504 /50x.html;
            location = /50x.html {
        }
    }

}

```

? ？ ?

这一堆都是啥玩意er，完全没有头绪啊

没关系，下面我们就来详细分析一下nginx.conf这个文件中的内容。

按照功能划分，我们通常将nginx配置文件分为三大块，**全局块，events块，http块**。

### 第一部分：全局块

首先映入眼帘的这一堆：

```shell
user nginx;
worker_processes auto;
error_log /var/log/nginx/error.log;
pid /run/nginx.pid;

# Load dynamic modules. See /usr/share/doc/nginx/README.dynamic.
include /usr/share/nginx/modules/*.conf;
```

我们称之为**全局块**，知识点呐朋友们，要记住，这里呢，主要会设置一些影响 nginx 服务器整体运行的配置指令，主要包括配 置运行 Nginx 服务器的用户（组）、允许生成的 worker process 数，进程 PID 存放路径、日志存放路径和类型以及配置文件的引入等。

比如  worker_processes auto； 这一行，worker_processes 值越大，我们nginx可支持的并发数量就越多，很多人想这不就爽了吗，我设置成正无穷，无限并发flag达成，秒杀问题轻松解决，这个，受自己服务器硬件限制的，不能乱来。

### 第二部分：events 块：

```shell
events {
    worker_connections 1024;
}
```

这一堆，就是我们配置文件的第二部分，**events 块**

起名字这么随意的么，那第三部分是不是叫http块？

wc，这你都知道，是的

events 块涉及的指令主要影响 Nginx 服务器与用户的网络连接，常用的设置包括是否开启对多 work process 
下的网络连接进行序列化，是否允许同时接收多个网络连接，选取哪种事件驱动模型来处理连接请求，每个 word 
process 可以同时支持的最大连接数等。

### 第三部分：http块：

内容太多，略

```shell
http {
    server {
        }
}
```

> 注意：
>
> http是一个大块，里面也可以包括很多小块，比如http全局块，server块等。

http 全局块配置的指令包括文件引入、MIME-TYPE 定义、日志自定义、连接超时时间、单链接请求数上限等。

而http块中的server块则相当于一个虚拟主机，一个http块可以拥有多个server块。

server块又包括**全局server**块，和**location**块。

全局server块主要包括了本虚拟机主机的监听配置和本虚拟主机的名称或 IP 配置

location块则用来对虚拟主机名称之外的字符串进行匹配，对特定的请求进行处理。地址定向、数据缓
存和应答控制等功能，还有许多第三方模块的配置也在这里进行。比如，对/usr相关的请求交给8080来处理，/admin则较给8081处理。

说了这么多，我还是不是特别理解咋办，问题不大，接下来我们通过几个实例来帮助大家更好的理解这些配置在实际中所发挥的作用。

## Nginx配置实战:

接下来我们将通过对nginx配置文件的修改来完成反向代理，负载均衡，动静分离的简单配置。

### nginx配置反向代理：

我发现很多教程说nginx配置反向代理的时候上来就改host文件，这里的话，因为上一篇文章我们有总结过反向代理的精髓，也就是：

**反向代理服务器和目标服务器对外就是一个服务器，暴露的是代理服务器地址，隐藏了真实服务器 IP 地址。** 

所以接下来我们通过一个小栗子，当我们访问服务器的时候，由于我的服务器没备案，阿里云默认80端口没开，所以这里我们设置对外服务的端口为8888，**当我们访问8888端口的时候，实际上是跳转到8080端口的。**

首先我们用docker启动一个tomcat容器，然后配置端口映射为8080。

等等，不会docker怎么办？不会docker的话，可以看韩数**最新的docker初级入门教程（滑稽）**

如果对docker不是很了解的话，可以使用传统的linux下运行tomcat，道理是一样的。

然后修改我们的配置文件nginx.conf里面的server块，修改之后的内容如下：

```shell
    server {
        listen       8888 ; ##设置我们nginx监听端口为8888
        server_name  [服务器的ip地址];

        # Load configuration files for the default server block.
        include /etc/nginx/default.d/*.conf;

        location / {
            proxy_pass http://127.0.0.1:8080; ##需要代理的服务器地址
            index index.html;
        }

        error_page 404 /404.html;
            location = /40x.html {
        }

        error_page 500 502 503 504 /50x.html;
            location = /50x.html {
        }
    }

```

然后在浏览器中输入：服务器ip:8888 发现浏览器显示出来了8080端口tomcat的欢迎界面，从而实现了隐藏真实服务器地址这样一个反向代理的要求。

哦？看着好神奇哦，那，我之前经常有看到那种，就是各种/image /video 不同的链接对应的是不同的网站，那也是这么做的咯？

聪明，这里我们再新建一个tomcat容器，端口为8081，同时把在容器中tomcat webapps目录新建一个我们自己的目录，这里叫hello，里面新建一个hello.html文件，内容为<h1>I am hello<h1>

同时我们在端口为8080的tomcat容器中，在webapps新建我们的文件家hi，并新建hi.html文件，内容为<h1>I am hi<h1>

啊，这样的话配置是不是很难啊?

你想多了，敲简单的。

修改我们配置文件中的server快，如下：

```shell
    server {
        listen       8888 ; ##设置我们nginx监听端口为8888
        server_name  [服务器的ip地址];

        # Load configuration files for the default server block.
        include /etc/nginx/default.d/*.conf;

        location /hi/ {
            proxy_pass http://127.0.0.1:8080; ##需要代理的服务器地址
            index index.html;
        }
        
        location /hello/ {
            proxy_pass http://127.0.0.1:8081; ##需要代理的服务器地址
            index index.html;
        }

        error_page 404 /404.html;
            location = /40x.html {
        }

        error_page 500 502 503 504 /50x.html;
            location = /50x.html {
        }
    }
```

在浏览器中输入：服务器ip:8888/hi/hi.html

浏览器显示  **I am hi** 对应服务器端口为 8080

在浏览器中输入：服务器ip:8888/hello/hello.html

浏览器显示  **I am hello** 对应服务器端口为 8081

从而实现了针对不同url请求分发给不同服务器的功能配置。

少侠，且慢，你是不是忘了什么东西，location /hello/ 是什么意思，只能这么写么？

当然不是。学会location指令匹配路径，随便换姿势

**location指令说明:**

功能：用于匹配URL

语法如下：

```shell
1、= ：用于不含正则表达式的 uri 前，要求请求字符串与 uri 严格匹配，如果匹配
成功，就停止继续向下搜索并立即处理该请求。
2、~：用于表示 uri 包含正则表达式，并且区分大小写。
3、~*：用于表示 uri 包含正则表达式，并且不区分大小写。
4、^~：用于不含正则表达式的 uri 前，要求 Nginx 服务器找到标识 uri 和请求字
符串匹配度最高的 location 后，立即使用此 location 处理请求，而不再使用 location 
块中的正则 uri 和请求字符串做匹配。
```

注意:

> 如果 uri 包含正则表达式，则必须要有 ~ 或者 ~* 标识。

到这里，关于nginx如何简单的配置一个反向代理服务器就大功告成了，下面我们来说一下怎么实现负载均衡的简单配置。

### nginx配置负载均衡：

在nginx中配置负载均衡也是十分容易的，同时还支持了多种负载均衡策略供我们灵活选择。首先依旧是准备两个tomcat服务器，一个端口为8080，一个端口为8081，这里呢，推荐大家用docker部署，太方便了，什么，不会docker，可以移步我的面向后端的docker初级入门教程，真的挺好用，省了很多工作量。

卧槽，广告

然后修改我们的http块如下:

 ```shell
http {
 
 ###此处省略一大堆没有改的配置
 
 
     ##自定义我们的服务列表
     upstream myserver{
        server 127.0.0.1:8080;
        server 127.0.0.1:8090;
      }


    server {
        listen       8888 ; ##设置我们nginx监听端口为8888
        server_name  [服务器的ip地址];

        # Load configuration files for the default server block.
        include /etc/nginx/default.d/*.conf;

        location / {
            proxy_pass http://myserver; ##叮，核心配置在这里
            proxy_connect_timeout 10; #超时时间，单位秒
        }

        error_page 404 /404.html;
            location = /40x.html {
        }

        error_page 500 502 503 504 /50x.html;
            location = /50x.html {
        }
    }

}

 ```

这就完了?当然还没有，之前就有说过，nginx提供了三种不同的负载均衡策略供我们灵活选择，分别是：

- **轮询(默认方式):**  每个请求按时间顺序逐一分配到不同的后端服务器，如果后端服务器 down 掉，能自动剔除。

  用法：啥也不加，上文实例就是默认的方式，就是默认的

- **权重(weight):**  weight 代表权重,默认为 1,权重越高被分配的客户端越多,权重越大，能力越大，责任越大，处理的请求就越多。

  用法:

  ```shell
       upstream myserver{
          server 127.0.0.1:8080 weight =1;
          server 127.0.0.1:8090 weight =2;
        }
  ```

  

- **ip_hash**：每个请求按访问 ip 的 hash 结果分配，这样每个访客固定访问一个后端服务器，可以解决 session 的问题。

  用法：

   ```shell
    upstream myserver{
          ip_hash;#可与weight配合使用
          server 127.0.0.1:8080 weight =1;
          server 127.0.0.1:8090 weight =2;
        }
   ```


### nginx配置动静分离：

接下来说最后一个实例，动静分离的简单配置。

等等，我记得明明是四个，明明还有一个正向代理呢？

这个，爱国守法，人人有责，有需要访问某些国内不能访问的网站需求的同学，可以自行下去查阅资料。

至于怎么配置正向代理，咱也不知道，咱也不敢说，咱也不敢问。

**基础篇回顾：**

> 动静分离就是把很少会发生修改的诸如图像，视频，css样式等静态资源文件放置在单独的服务器上，而动态请求则由另外一台服务器上进行，这样一来，负责动态请求的服务器则可以专注在动态请求的处理上，从而提高了我们程序的运行效率，与此同时，我们也可以针对我们的静态资源服务器做专属的优化，增加我们静态请求的响应速度。

具体的动静分离配置也不是十分的复杂，和负载均衡，反向代理差不多。

为了演示动静分离呢，首先我们需要准备两个文件夹，一个是data文件夹，用来存放我们js，css这些静态资源文件，一个是html文件夹，用来存放我们的html文件。

在html文件夹新建一个html文件，index.html，内容如下

```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>我是一个静态界面</title>
</head>
<script type="text/javascript" src="jquery.js"></script>
<body>
<h1>我是一个静态界面</h1>
<div id="test_div"></div>
</body
</html>

```

注意，这里我们并没有将jquery.js 这个文件放在html目录下，而是将它放在了另外一个目录data里面，当服务器接需要请求jquery.js这个文件时，并不会去index.html所在的那个服务器去请求这个文件，而是会直接去我们配置好的服务器或者路径去寻找这个js文件，在本实例中，会去data文件夹下面去找这个jquery.js这个文件。

修改server的配置如下:

```shell
 server {
        listen      8886 ;
        server_name [你的服务器ip地址];

        # Load configuration files for the default server block.
        include /etc/nginx/default.d/*.conf;

        location / {
            root /html/;
            index index.html;
        }

         #拦截静态资源，static里面存放的我们图片什么的静态资源
        location ~ .*\.(gif|jpg|jpeg|bmp|png|ico|js|css)$ {
        root /data/;
       }

        error_page 404 /404.html;
            location = /40x.html {
        }

        error_page 500 502 503 504 /50x.html;
            location = /50x.html {
        }
    }

```

测试:

在浏览器中输入ip地址:8888/index.html,屏幕上显示我是一个静态界面，同时打开开发者工具

![image-20191030103306596](C:\Users\admin\Desktop\image-20191030103306596.png)

会发现jquery.js已经被正常请求到了。

## 下面开始技术总结：

写到这里实战篇结束了吗？并没有，尽管上面给出了负载均衡，反向代理,动静分离的实例，但仍然只是最基础的配置，比如多层负载均衡，缓存等高级配置，都需要我们在日后的开发生活逐渐的去接触和了解。下一篇呢，我们将深入nginx腹地，去稍微稍微简单不细致大致看一眼那种去了解一下nginx内部是如何保持如此高效率的工作的。

最后，韩数的学习笔记目前已经悉数开源至github，一定要点个**star**啊啊啊啊啊啊啊

**万水千山总是情，给个star行不行**

[韩数的开发笔记](https://github.com/hanshuaikang/HanShu-Note)

欢迎点赞，关注我，有你好果子吃（滑稽）

