# 面向后端的Docker初级入门教程：实战篇

在之前的两篇文章中，我们在 [面向初学者的Docker学习教程：基础篇](https://juejin.im/post/5d96fd6fe51d45781d5e4bac)中通过

1. 什么是Docker？
2. 为什么是Docker？
3. Docker 具体解决了什么样的问题？

这三个问题来帮助大家更好的了解Docker，之后在第二篇文章[Docker 三要素 ：镜像、容器和仓库](https://juejin.im/post/5d9ac7596fb9a04e1135e85d)中解释了docker中最核心的三个概念，通过这两篇的学习，我想大家对于Docker是什么？这个问题已经有了自己的答案，那么再了解过这些概念之后，我们将通过实际的命令行来使用Docker，并能够完成一些基础的操作。

不废话，直接上代码:

## Docker 安装:

在使用Docker之前，我们需要先安装Docker，在这里 ，我只列举最简单的那一种安装方式，如果需要其他方式安装的话，可以参照官方文档-快速上手，上面有更为详细的安装教程。

**Docker 安装(yum):**

```shell
#在终端输入yum命令，安装docker及相关依赖
yum install docker

#启动docker 服务：centos 7+
systemctl start docker 

#启动docker 服务：centos 6+
service docker restart

```

**验证Docker是否安装成功:**

```shell
#从docker官方仓库拉取docker hello-world镜像
docker pull hello-world

#运行hello-world镜像
docker run hello-world
```

如果出现 Hello from Docker! 则说明我们的Docker已经正确安装完成啦

注:

> docker 官方的镜像源由于在国外，导致国内很多在本低虚拟机学习docker的同学下载镜像特别特别的慢，实测阿里云服务器速度还可以，如下载较慢的同学可以尝试更换国内阿里云，或者网易云的镜像仓库，需要的同学可以自行去网上搜索相关教程。

## Docker 镜像操作：

Docker完美融合Linux，所以Docker命令行的风格和Linux还是比较接近的，相对来说比较容易上手，首先，我们先说镜像相关的命令:

```shell
#搜索镜像:
docker search 镜像名
#例：docker search centos 搜索centos相关的镜像。

#拉取镜像到本地服务器 默认TAG是lastet即最新版本
docker pull 镜像名:TAG

#例：docker pull mysql or docker pull mysql:5.6 拉取最新版本或者5.6版本的mysql镜像

#查看所有镜像:
docker images 

##查看顶层镜像
docker image ls

#查看中间层镜像
docker image ls -a

#在之前的基础篇中有说到，docker的镜像是分层来存储的，为了加速镜像构建、重复利用资源，Docker 会利用 中间层镜像。所以在使用一段时间后，可能会看到一些依赖的中间层镜像，这样会看到很多无标签的镜像，与之前的虚悬镜像不同，这些无标签的镜像很多都是中间层镜像，是其它镜像所依赖的镜像。这些无标签镜像不应该删除，否则会导致上层镜像因为依赖丢失而出错。实际上，这些镜像也没必要删除，因为之前说过，相同的层只会存一遍，而这些镜像是别的镜像的依赖，因此并不会因为它们被列出来而多存了一份，无论如何你也会需要它们。只要删除那些依赖它们的镜像后，这些依赖的中间层镜像也会被连带删除。

#列出部分镜像，如果不指定的话，TAG部分可以省略
docker image ls ubuntu:18.04

#删除镜像
docker image rm 镜像ID

#批量删除镜像:
docker image rm $(docker image ls -q redis) 

```

执行docker images 命令,如下图所示：

 ![img](https://user-gold-cdn.xitu.io/2019/10/25/16e01665a5025005?imageView2/0/w/1280/h/960/ignore-error/1) 

列表包含了 **仓库名、标签、镜像 ID、创建时间** 以及 **所占用的空间。**



## Docker 容器操作

在之前我们有说到过，镜像与容器之间的关系类似于类与实例之间的关系，在Docker中，我们通过运行镜像来获得一个的容器，在这里我们以centos为例:

```shell
docker run [选项] [镜像名或者IMAGEID]
例:
docker run -it centos 
-it 交互式终端运行

#比如很多比如mysql镜像，我们需要这个容器在后台运行，并不是十分需要进入容器终端，这个时候我们只需要将-it 替换成 -d 即可启动守护式容器:
docker run -d centos 
```

通过执行上面的命令，我们会自动以交互模式进入到容器中，如图所示:

 ![img](https://user-gold-cdn.xitu.io/2019/10/25/16e01679f7184a8d?imageView2/0/w/1280/h/960/ignore-error/1) 

可以发现，我们实际上已经进入centos容器内部的bin/bash了，在这里，我们可以输入相关的命令来操作我们的容器。如果只是单纯的想创建一个容器，并不怎么着急启动的话，可以使用**create**命令:

```shell
#创建一个容器但是不立即启动
docker creat 镜像名或者镜像ID

#例:docker create centos

#docker容器默认命名规则是科学家+他的发现，如果我们需要自定义自己的别名，比如说centos容器叫mycentos，我们需要加入--name选项
docker run -it --name [别名] [镜像名]

#例:docker run -it --name mycentos centos
```

当我们有某些应用比如tomcat需要使用特定端口向外部提供服务时，我们可以使用 **-p** 选项配置宿主机与容器之间的端口映射，以tomcat为例:

```shell
docker run -it -p 8899:8080 tomcat
-p 配置端口映射
```

这样，当我们访问ip:8899 时，就会发现浏览器出现了tomcat的首页，这个时候端口就已经正确映射到容器里面8080端口上了。

当然，退出交互式终端，Docker同样也提供了两种方式供我们选择:

```shell
#退出交互式命令环境：这种是比较优雅的退出，退出后容器仍然在运行
Ctrl+P+Q

exit 
#不太优雅的方式，退出之后容器也会停止
```

 如果退出了之后，我后悔了，又突然想再进去容器终端内改一些东西，重新进入容器交互式终端，Docker提供了如下两种方式:

```shell
docker attach  [别名或IMAGEID] 

##推荐第二种
docker exec -it centos bash
##docker exec ：在运行的容器中执行命令

```

除去上面这些，Docker的容器还支持启动，重启，删除等操作:

```shell
#停止容器: 温柔式，正常关机
docker stop [别名或IMAGEID]
#例 
docker stop mycentos

#强制停止容器: 拔插头
docker kill [别名或IMAGEID] 

#启动容器:
docker start  [别名或IMAGEID] 
#重启容器:
docker restart  [别名或IMAGEID] 
#删除容器,只能删除停止的容器
docker rm [别名或IMAGEID] 
```

如果想查看正在运行的容器，可以使用以下命令:

```shell
#查看正在运行的容器
docker ps

#查看所有容器，包括已经停止的容器
docker ps -a
```

获取容器输出信息:

```shell
docker logs container 
```

## 容器数据卷:

在某些情况下，我们需要实时备份容器内的数据到我们的宿主机上，同样在Docker中，是支持容器与宿主机之间建立共享目录的，使用起来也非常简单,只需要加一个 **-v** 选项就可以了。

```shell
#例，执行过之后，我们在宿主机目录中创建一个新文件，容器内目录也会相应的出现这个文件，所以就可以把mysql数据存放的目录通过这种方式和宿主机共享，达到实时备份数据的目的。
docker run -it -v /宿主机目录:/容器内目录 centos /bin/bash
 
#如果需要给容器内目录设定权限，则只需要加上 ro 即可，read -only 的缩写
docker run -it -v /宿主机目录:/容器内目录:ro /bin/bash
ro 只读，这样我们宿主机在共享目录里面创建的文件在容器内是只读的
 
#查看容器是否挂载成功：
docker inspect 容器ID
```



## 制作Docker镜像:

### 使用commit命令制作镜像:

很多时候我们不可避免的会对容器有所修改，比如修改了tomcat，mysql等等的配置文件，但是如果再配置一台一模一样的tomcat容器的话，则又要重新修改配置文件，Docker可以通过**commit命令**将现在的容器重新打包成一个镜像，如果你现在修改了tomcat容器内端口为8081的话，那么我们使用commit将该容器打包成镜像的话，之后我们运行这个新镜像，会发现镜像里面tomcat默认端口是8081.

我们定制好了变化，并且希望能将其保存下来形成镜像。要知道，当我们运行一个容器的时候（如果不使用卷的话），我们做的任何文件修改都会被记录于容器存储层里。而 Docker 提供了一个 docker commit 命令，可以将容器的存储层保存下来成为镜像。换句话说，就是在原有镜像的基础上，再叠加上容器的存储层，并构成新的镜像。以后我们运行这个新镜像的时候，就会拥有原有容器最后的文件变化。

```shell
docker commit [选项] <容器 ID 或容器名> [<仓库名>[:<标签>]]

#例:
docker commit \
--author "Tao Wang <twang2218@gmail.com>" \
--message "修改了默认网页" \
webserver \
nginx:v2

##其中 --author 是指定修改的作者，而 --message 则是记录本次修改的内容。这点和 git 版本控制相似，不过这里这些信息可以省略留空。
```



### 使用DockerFile来定制镜像:

即使Docker提供了commit来制作镜像，可是依然感觉麻烦了点。如果我们可以把每一层修改、安装、构建、操作的命令 都写入一个脚本，用这个脚本来构建、定制镜像，那么之前提及的无法重复的问题、镜像构建透明性的问题、体积的问题就都会解决。这个脚本就是 Dockerfile。 Dockerfile 是一个文本文件，其内包含了一条条的指令(Instruction)，每一条指 令构建一层，因此每一条指令的内容，就是描述该层应当如何构建。 

由于DockerFile整块内容还是很多的，所以后期我打算单独拿出来做成笔记，本篇文章作为入门教程，将使用一些简单的案例来帮助大家初识DockerFile，复杂镜像的构建，则会单独放在一篇文章中说明。

由于我们之前的centos镜像只保留了centos的核心功能，很多常用的软件都没有，于是我们打算制作一个简单的centos镜像，在原来的基础上使其拥有vim编辑器和net-tools工具。

首先新建一个DockerFile文件，写入以下内容:

```shell
from centos  //继承至centos
ENV mypath /tmp  //设置环境变量
WORKDIR $mypath //指定工作目录

RUN yum -y install vim //执行yum命令安装vim
RUN yum -y install net-tools //执行yum命令安装net-tools

EXPOSE 80 //对外默认暴露的端口是80
CMD /bin/bash //CMD 容器启动命令，在运行容器的时候会自动执行这行命令，比如当我们 docker run -it centos 的时候，就会直接进入bash

```

然后编译该镜像:

```shell
然后编译该镜像
docker build -f ./DockerFile -t mycentos:1.3.
-t 新镜像名字:版本
-f 文件 -d 文件夹
```

之后我们执行docker images命令就会出现我们构建好的新镜像， mycentos:1.3 ，运行该镜像，会发现vim和net-tools 是可以正常使用的。

最后就大功告成啦。

## 下面开始技术总结:

本篇文章呢，我们讲解了Docker常见的命令行的使用和解释，以及DockerFile的简单入门，下一篇呢，我们将从DockerFile入手，开始详细的了解去怎么去制作和发布一个自己的镜像。

最后，相关笔记已经同步开源至Github(**欢迎star**)：:
https://github.com/hanshuaikang/HanShu-Note





 