# django 自定义登陆拦截器

django 没有提供类似于Spring中Interceptor的概念，但是提供了一系列的中间件来解决这个问题。

django的中间件主要分为五种:

- 请求(Request)中间件->对应函数process_request
- 视图(View)中间件->对应函数process_view
- 模板(Template)中间件->对应函数`process_template_response`
- 响应(Response)中间件->对应函数`process_response`
- 异常(Exception)中间件->对应函数`process_exception`

执行流程大概为：

![3](G:\蓝鲸学习笔记\img\3.png)

代码实现：

新建一个python文件，代码如下 `process_request`为具体请求拦截的逻辑实现：

```python
# !/usr/bin/env python
# -*- coding: utf-8 -*-
from django.shortcuts import HttpResponseRedirect, redirect
from django.urls import reverse

try:
    from django.utils.deprecation import MiddlewareMixin  # Django 1.10.x
except ImportError:
    MiddlewareMixin = object  # Django 1.4.x - Django 1.9.x


class UserMiddleware(MiddlewareMixin):

    def process_request(self, request):
        ## 判断来源请求是否需要用户登陆
        if request.path != '/transfer/login/' and request.path != '/transfer/register/':
            # 如果需要用户登陆，则判断session中是否存在用户id(也可以判断其他的，不一定是seesion，tocken也可以)
            if request.session.get('user_id') is not None:
                ##放行请求
                pass
            else:
                # 请求重定向到登陆界面
                return redirect(reverse('transfer:login'))
```



第二步就是在setting文件中，添加我们自定义的请求拦截器：

```python

MIDDLEWARE = [
    'django.middleware.security.SecurityMiddleware',
    'django.contrib.sessions.middleware.SessionMiddleware',
    'django.middleware.common.CommonMiddleware',
    'django.middleware.csrf.CsrfViewMiddleware',
    'django.contrib.auth.middleware.AuthenticationMiddleware',
    'django.contrib.messages.middleware.MessageMiddleware',
    'django.middleware.clickjacking.XFrameOptionsMiddleware',
    'orangeproject.usermiddleware.UserMiddleware',## 这里
]
```

重启项目，大功告成

