### Django 使用分页插件实现数据库分页查询：

`Django`内置了数据库`orm`框架，无须写`sql`语句便可以进行复杂的数据库增删改查操作，我们可以使用`orm`来进行分页查询来手动实现分页，为了方便开发人员，`Django`提供了非常容易上手的分页插件`Paginator`。

使用方法也很简单,首先从数据库种获取对象列表，之后封装一个`Paginator`对象，就可以自动的完成分页操作。

```python
user = User.objects.get(user_id=user_id)
# 获取当前用户下的所有文件列表
file_list_all = user.file_set.all()
# 封装Paginator对象，其中第一个参数是列表，第二个参数是每页显示的数据数。
paginator = Paginator(file_list_all, 5)
# 传入当前页数就可以获得当前页的数据
# 参数 ：当前页
file_list = paginator.page(1)
```

`Paginator`（*object_list*，*per_page*，*orphans = 0*，*allow_empty_first_page = True*）

**` Paginator`参数**：

其中：orphans 和 allow_empty_first_page 为可选参数。

- `orphans` ：是否需要最后一页有比较少的记录，默认值为0，比如数据库中有11条记录，每页5条记录，这样第三页就只有一条记录，这个时候设置 `orphans` 为 1 最后数据将只有两页，其中第一页，五条记录，第二页 六条记录。
- allow_empty_first_page : 是否允许第一页为空，默认为 True，如果设置为False，当第一页为空则会引`EmptyPage`错误。

**` Paginator`属性：**

` Paginator` 提供了一些属性，来帮助我们进行分页：

- `Paginator.count`：数据总数。
- `Paginator.num_pages`：总页数
- `Paginator.page_range`：返回页码的范围迭代器，比如你有四页数据，则返回[1,2,3,4]

其中： `page = paginator.page(1)`会返回一个当前页的Page对象.Page对象同样包含了如下的属性和方法：

**Page属性**：

- `number `  : 当前页的页码
- `object_list ` : 当前页的数据查询集
- `paginator` : 当前页的` paginator `对象

**Page方法：**

- `has_previous ` : 判断当前页是否有前一页，如果有 返回True。
- `has_next`  : 判断当前页是否有下一页，如果有 返回True。
- `previous_page_number `：返回前一页的页码。
- `previous_page_number` ：返回下一页的页码。

其他方法详见官方文档：[官方文档](https://docs.djangoproject.com/zh-hans/2.2/topics/pagination/)











