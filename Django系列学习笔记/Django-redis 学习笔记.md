# Django-redis 学习笔记

安装：

```linux
pip install django-redis
```

在项目中setting文件中配置缓存redis链接：

```python
CACHES = {
    "default": {
        "BACKEND": "django_redis.cache.RedisCache",
        "LOCATION": "redis://ip:6379",##地址
        "OPTIONS": {
            "CLIENT_CLASS": "django_redis.client.DefaultClient",
            "CONNECTION_POOL_KWARGS": {"max_connections": 100},
            "PASSWORD": "123456",##密码
        }
    }
}
```

在视图中调用：

```python
from django_redis import get_redis_connection
from django.core.cache import cache

#常用方法
##向缓存中放入key value
cache.set("key", "value", timeout=None)
##timeout=0 立即过期 timeout=None 永不超时 时间以秒为单位

##ttl 任何有超时设置的 key 的超时值.
cache.ttl("foo")

##通配符搜索key
cache.keys("foo_*")

##获取key对应的value
cache.get("value")


```

