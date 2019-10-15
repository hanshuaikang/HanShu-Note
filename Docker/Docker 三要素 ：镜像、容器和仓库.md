# Docker 三要素 ：镜像、容器和仓库 

上一篇笔记 [面向初学者的Docker学习教程:基础篇](https://juejin.im/post/5d96fd6fe51d45781d5e4bac)  我们从三个方面大致的介绍了一下Docker

1. 什么是Docker？
2. 为什么是Docker？
3. Docker 具体解决了什么样的问题？

相信看过这篇文章的小伙伴已经对Docker有了一个初步的认识，只有我们充分认识了Docker的奇妙之处，后面才能更好的使用它，接下来呢，我们将说一下Docker最重要的三个基本概念，**镜像，容器和仓库**

## 镜像

在了解镜像这个概念之前，我们需要先大致了解一下联合文件系统-UnionFS，它是Docker镜像的基础，联合文件系统是一种分层，轻量级并且高性能的文件系统，它支持对文件系统的修改作为一次提交来一层一层的叠加，同时可以将不同目录挂载到同一个虚拟文件系统下，镜像可以通过分层来进行集成，我们可以基于一个基础的镜像，然后制作出各种各样满足我们需求的应用镜像。

同时，对于一个精简的OS，rootfs可以很小，有常见的命令就行，同时，底层又是直接使用的操作系统的内核，所以往往Docker中一个镜像的体积相对来说可以很小，比如一个完整版的centos可能要几个G，但是Docker中的centos大概只有300M.

对于docker镜像，官方的定义如下:

> An image is a read-only template with instructions for creating a Docker container. Often, an image is based on another image, with some additional customization. For example, you may build an image which is based on the ubuntu image, but installs the Apache web server and your application, as well as the configuration details needed to make your application run.‘
>
> 映像是一个只读模板，带有创建Docker容器的指令。通常，一个映像是基于另一个映像的，还需要进行一些额外的定制。例如，您可以构建一个基于ubuntu映像的映像，但是安装Apache web服务器和您的应用程序，以及使您的应用程序运行所需的配置细节。



PS:一个镜像可以创建多个容器。



## 容器：

容器是用镜像创建的运行实例。

每个容器都可以被启动，开始，停止，删除，同时容器之间相互隔离，保证应用运行期间的安全。

我们可以把容器理解为一个精简版的linux操作系统，包括root用户权限，进程空间，用户空间和网络空间等等这些，然后加上再它之上运行的应用程序。

比如我们现在基于mysql镜像创建了一个容器，那么，这个容器其实并不是只有一个mysql程序，而是mysql同样也是安装运行在我们容器内的linux环境中的。

### 容器和镜像的关系:

再说这个问题之前，我们不妨先来看一下下面这段java代码:

```java
Person p = new Person();
Person p1 = new Person();
Person p2 = new Person();
```

镜像在这里就是我们的Person，容器就是一个个Person类的实例。一个Person可以创建多个实例，一个镜像也可以创建多个容器。



## 仓库:

仓库相对来说就比较容易理解了，仓库（Repository）是集中存放镜像文件的场所。

仓库分为公开仓库和私有仓库，目前的话，全世界最大的仓库是Docker官方的 [Docker Hub](https://hub.docker.com/)

由于一些不可抗拒的因素，导致我们如果从Docker Hub上下载公开的镜像是非常蛋疼的，这点大家可以参考你用百度网盘官方下载时的感觉。所以，国内我们一般使用阿里云或者网易云的镜像仓库。



镜像 容器 仓库 他们三者之间的关系图如下:

![1100338-20181011200343656-1972949758](image\1100338-20181011200343656-1972949758.png)





## 总结:

今天呢，我们简单的描述了一下Docker三要素，镜像，容器和仓库，在之后的学习中我们会经常看到镜像和容器这两个概念，同时也会编写我们自己的DockerFile构建我们自定义的镜像文件。

最后，相关笔记已经同步开源至Github:

有需要的同学可以前去下载：























