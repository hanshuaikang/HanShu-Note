# DJango 数据库事务：

Django 默认的事务行为是自动提交。除非事务正在执行，每个查询将会马上自动提交到数据库，Django 自动使用事务或还原点，以确保需多次查询的 ORM 操作的一致性，特别是 [delete()](https://docs.djangoproject.com/zh-hans/2.2/topics/db/queries/#topics-db-queries-delete) 和 [update()](https://docs.djangoproject.com/zh-hans/2.2/topics/db/queries/#topics-db-queries-update) 操作。

Django自带ORM框架，自动的提供了数据库事务相关的支持。



