## Django项目 uwsgi+Nginx 部署教程

## 背景：

最近在学习django的过程中，用django做了一个小demo，所以试试看能不能部署到服务器上，自己顺便也熟悉一下Django整个部署的流程，因为之前学习flask的时候，就使用的uwsgi来作为python web服务器来部署的，所以这次也就选择了nginx+uwsgi这样的一个组合。

当时买完云服务器之后，就顺手把宝塔装上了，之前用宝塔部署php项目是真的很容易，所以就偷懒试试宝塔能不能部署django，苦试一个小时，无果，遂放弃，还是老老实实用原生的uwsgi配合Nginx来部署吧。然后下面的流程我会争取把有可能是坑的地方重点标注出来，将来如果发博客或者自己需要再次部署的时候，也可以少写很多配置文件。

## 环境：

`系统版本`：centos7.5 

`python`版本：3.6.5

`nginx `版本：1.16.1

`uwsgi`版本：2.0.18 

如环境不同，还请查阅其他资料另行配置。

## 环境准备：

在部署之前，请确保你的linux服务器正确安装了对应的python版本，如果需要python3版本，请自行查阅资料进行升级安装。

首先为了更好的独立部署，避免对其他的项目产生干扰，我们需要安装python 虚拟环境：

```linux
sudo pip install virtualenv
sudo pip install virtualenvwrapper
```

> virtualenvwrapper 是virtualenv的扩展管理包，可以将所有的虚拟环境整合在一个目录下。

**配置虚拟环境：**

```bash
mkdir ~/.virtualenvs
```

打开.bashrc:

```bash
sudo vim ~/.bashrc
```

在.bashrc的末尾增加下面内容：

```bash
export WORKON_HOME=$HOME/.virtualenvs  # 所有虚拟环境存储的目录
source /usr/local/python3/bin/virtualenvwrapper.sh
```

> 注意！：这里的/usr/local/bin/virtualenvwrapper.sh只是针对于我当前系统环境的一个位置，并不是所有的服务器都是在这个位置，如果自己不知道virtualenvwrapper.sh在哪里，可以搜索文件来找到它在系统中的位置，并且修改.bashrc。

启用配置文件:

```bash
source ~/.bashrc
```

这个时候如果不报错，就代表我们的虚拟环境配置成功了，一般常见的报错就是virtualenvwrapper.sh文件找不对。

**创建虚拟环境：**

找一个你自己觉得能记住的地方，新建一个env 文件夹：

```bash
cd /www
mkdir env
cd env #进入env目录
```

新建一个虚拟环境：

```bash
mkvirtualenv -p /usr/bin/python3 orange_env    # my_env是虚拟环境的名称
```

注意：如果你的软连接/usr/bin/python3没有的话，会报错误，找不到/usr/bin/python3，这个时候就需要你自己新建一个软连接：

如果报错：

```bash
ln -s /usr/local/python3/bin/python3 /usr/bin/python3 # 路径要改成自己的python安装路径
```

之后便可以进入我们的虚拟环境了：

```bash
source /www/env/orange_env/bin/activate
```

进去虚拟环境之后,前面会出现一个括号，里面是你虚拟环境的名字：

```bash
(orange_env) [root@iz2ze1cvux96riiwfh05qqz ~]# 
```

在虚拟环境中安装uwsgi：

```bash
pip install uwsgi
```

**退出虚拟环境：**

```bash
deactivate
```

再次在主环境中安装uwsgi:

```bash
pip install uwsgi
```

> 注意：如果你有其他的依赖，比如django,msqlclient这些，记得一定要在虚拟环境里pip安装一下。

## 部署过程：

找一个你认为比较合适的地方，新建一个文件夹，将你的Django项目上传进去：

以我为例：

```bash
cd /www
mkdir orange
```

上传解压操作略，记得是上传项目根目录，就是直接带manage.py的那个目录。

新建一个uswgi配置文件，uswgi支持多种配置文件类型，比如yaml，xml，json，ini，这里我选的是xml。

```bash
vim mysite.xml #记得mysite.xml 要和你项目的manage.py 在一个目录下。
```

mysite.xml内容如下：

```xml
<uwsgi>    
   <socket>127.0.0.1:8080</socket><!-- 内部端口，自定义 --> 
   <chdir>/www/orange/</chdir><!-- 项目路径 -->            
   <module>orangeproject.wsgi</module> <!-- 一般模块名 项目名+wsgi -->  
   <processes>4</processes> <!-- 进程数 -->     
   <daemonize>uwsgi.log</daemonize><!-- 日志文件 -->
</uwsgi>
```



**安装Nginx：**

Nginx 我之前有写过一系列的基础入门教程，如果对安装启动重启这些不是很熟练的可以看下面这篇文章：

链接：[写给后端的Nginx初级入门教程：实战篇](https://juejin.im/post/5db8f8c3f265da4d3e173c62)

查看nginx 配置文件路径：

```bash
nginx -t
```

记得备份nginx之前的配置文件，然后把之前的配置全部删了，直接加入下面内容：

```conf
worker_processes  1;
events {
    worker_connections  1024;
}
http {
    include       mime.types;
    default_type  application/octet-stream;
    sendfile        on;
    server {
        listen       8000;
        server_name 你的域名;
        charset utf-8;
        location / {
           include uwsgi_params;
           uwsgi_pass 127.0.0.1:8080;
           uwsgi_param UWSGI_SCRIPT orangeproject.wsgi;
           uwsgi_param UWSGI_CHDIR /www/orange/;
           
        }
        location /static/ {
        alias /www/orange/transfer/static/; 
        }
    }
}
```

> 注意模块名要保持一致，而且你nginx监听的端口不能和你django启动的端口一样，要不uswgi会因为nginx占用端口启动失败。alias /www/orange/transfer/static/; 这个是你的静态文件地址，css，img这些。

检查nginx是否配置成功：

```bash
nginx -t
```

重启nginx：

```bash
nginx -s reload
```

之后，再次进入我们的虚拟环境orange_env中，启动我们的uwsgi服务器：

```bash
cd /www/orange
uwsgi -x mysite.xml
```

然后打开我们的本地浏览器，输入：域名：8000，备案过的可以改nginx配置文件成80。

**大功告成：**

![5](G:\蓝鲸学习笔记\img\5.png)











