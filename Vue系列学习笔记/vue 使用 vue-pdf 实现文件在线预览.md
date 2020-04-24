# vue 使用 vue-pdf 实现pdf在线预览

## 背景
之前的demo增加了图片预览，于是今天下午追完番剧就突然想到能不能把pdf在线预览也做了，说干就干，刚开始查了很多教程，我发现很多人都在说什么pdf.js这个库，这当然没什么问题，pdf.js的确可以非常完美的实现pdf在线预览的过程，但是感觉这样直接进去有点不太优雅，感觉硬拼凑进去的，应该还是尽量用VUE体系之内的东西，于是多方查阅资料，发现有vue-pdf这个组件，虽然说它没有原生那样强大，比如不支持pdf 复制，打印会乱码，但是我感觉已经足以满足我的需求了。本篇笔记循序渐进，从基础的demo，到一个可用的程度，文末列出了大家在实际使用的过程中可能会遇到的问题和解决方案。

## 安装：
这个没有啥背景知识可讲，我们直接跳到安装环节，vue-pdf 和其他vue组件的安装并无不同，打开命令行，敲入:
```bash
npm install --save vue-pdf
```
注意路径。别在桌面调出来个终端安装了，这种直接打回去重学Vue。

## vue-pdf 初体验：
安装完之后，使用vue-pdf非常简单，和其他的组件并没有什么不同，上代码：

首先我们需要引入这个组件：
```html
<script>
import pdf from 'vue-pdf'
export default {
  components:{
      pdf
  },
  data(){
      return {
          url:"http://storage.xuetangx.com/public_assets/xuetangx/PDF/PlayerAPI_v1.0.6.pdf",
      }
  }
</script>   
```
然后在页面使用vue-pdf,只需要添加标签：
```html
<template>
<div>
  <pdf 
    ref="pdf"
    :src="url">
  </pdf>
</div>
</template>
```
重启你的项目，访问这个界面，你大概率会发现pdf已经成功显示在你的界面上了。这没有任何问题，但是，正当你准备拿起一根烟，点上，伴着舒适的《美丽的梭罗河》，欣赏你成功的杰作的时候，你会发现，我**擦，为啥只有一页**，当玻璃杯碰在一起，满世界都是梦破碎的声音。

所以，这只是初体验，如果你的pdf只有一页，这样写当然没什么问题，但是当我们呢pdf 有很多页的时候，你会发现，这行不通了。所以，接下来，我们来看看怎么让它显示多页。

## vue-pdf 渐入佳境：
其实，想要显示多页也没那么复杂，你每次就显示一页，我，直接v-for 循环，直接显示完，简单粗暴。

页面代码:
```html
<template>
	<div>
	<pdf  v-for="i in numPages" :key="i"  :src="url" :page="i"></pdf>	
	</div>
</template>

<script>
	import pdf from 'vue-pdf'
	export default {
		components: {
			pdf
		},
		data(){
			 return{
				    url: '',
				    numPages:1,
			 }
		 },
		mounted: function() {
		  this.getNumPages("http://storage.xuetangx.com/public_assets/xuetangx/PDF/PlayerAPI_v1.0.6.pdf")
		  
		}, 
		methods: {
			getNumPages(url) {
				var loadingTask = pdf.createLoadingTask(url)
				loadingTask.then(pdf => {
					this.url = loadingTask
					this.numPages = pdf.numPages
				}).catch((err) => {
					console.error('pdf加载失败')
				})
			},
		}
	}
</script>

```
各个属性：
- url ：pdf 文件的路径，可以是本地路径，也可以是在线路径。
- numPages ： pdf 文件总页数。

getNumPages 计算总页数，顺便给url和numPages赋值。

唯一需要大家注意的是这句：
```js
this.getNumPages("http://storage.xuetangx.com/public_assets/xuetangx/PDF/PlayerAPI_v1.0.6.pdf")
```
注意啊，这句不一定非要写到mounted里面，你想写哪就写哪，比如你前端请求后端，后端返回一个pdf 的url，在那里写就行，写在你需要的地方。

## vue-pdf 轻车熟路：

很多人看到这，就这，就这?万一，我pdf有一千页，我浏览器还不得裂开，我追求的是那种在微醺的下午，一页一页的翻看的感觉，你能给我吗？

我不能，才怪，保证满足你。

```html
<template>
	<div>
		<div class="tools">
			<bk-button :theme="'default'" type="submit" :title="'基础按钮'" @click.stop="prePage" class="mr10"> 上一页</bk-button>
			<bk-button :theme="'default'" type="submit" :title="'基础按钮'" @click.stop="nextPage" class="mr10"> 下一页</bk-button>
			<div class="page">{{pageNum}}/{{pageTotalNum}} </div>
			<bk-button :theme="'default'" type="submit" :title="'基础按钮'" @click.stop="clock" class="mr10"> 顺时针</bk-button>
			<bk-button :theme="'default'" type="submit" :title="'基础按钮'" @click.stop="counterClock" class="mr10"> 逆时针</bk-button>
		</div>
		<pdf ref="pdf" 
		:src="url" 
		:page="pageNum"
		:rotate="pageRotate"  
		@progress="loadedRatio = $event"
		@page-loaded="pageLoaded($event)" 
		@num-pages="pageTotalNum=$event" 
		@error="pdfError($event)" 
		@link-clicked="page = $event">
		</pdf>
	</div>
</template>
```
接下来，我们一一介绍这些都是个啥。

参数介绍：
- `page `: 当前显示的页数，比如第一页page=1
- `rotate` ： 旋转角度，比如0就是不旋转，+90，-90 就是水平旋转。
- `progress` ：当前页面的加载进度，范围是0-1 ，等于1的时候代表当前页已经完全加载完成了。
- `page-loaded` ：页面加载成功的回调函数，不咋能用到。
- `num-pages` ：总页数
- `error` ：加载错误的回调
- `link-clicked `：单机pdf内的链接会触发。

其他：
- print 这个是打印函数。
注意：谷歌浏览器会出现乱码，这个和字体有关系。

来，js代码走一个:

```js
<script>
	import pdf from 'vue-pdf'
	export default {
		name: 'Home',
		components: {
			pdf
		},
		data() {
			return {
				url: "http://storage.xuetangx.com/public_assets/xuetangx/PDF/PlayerAPI_v1.0.6.pdf",
				pageNum: 1,
				pageTotalNum: 1,
				pageRotate: 0,
				// 加载进度
				loadedRatio: 0,
				curPageNum: 0,
			}
		},
		mounted: function() {},
		methods: {
            // 上一页函数，
			prePage() {
				var page = this.pageNum
				page = page > 1 ? page - 1 : this.pageTotalNum
				this.pageNum = page
			},
            // 下一页函数
			nextPage() {
				var page = this.pageNum
				page = page < this.pageTotalNum ? page + 1 : 1
				this.pageNum = page
			},
            // 页面顺时针翻转90度。
			clock() {
				this.pageRotate += 90
			},
            // 页面逆时针翻转90度。
			counterClock() {
				this.pageRotate -= 90
			},
            // 页面加载回调函数，其中e为当前页数
			pageLoaded(e) {
				this.curPageNum = e
			},
            // 其他的一些回调函数。
			pdfError(error) {
				console.error(error)
			},
		}
	}
</script>
```
其他骚操作：
```js
// 打印全部
pdfPrintAll() {
	this.$refs.pdf.print()
		},
// 打印指定部分
pdfPrint() {
	this.$refs.pdf.print(100, [1, 2])
	},
```



具体样式什么的我就不贴出来了，这些都不是重点，完全可以改成自己喜欢的。

成品展示：
![img](img/6.png)

## 其他问题以及解决方案：

### 跨域问题：
网上用pdf.js 很多都会遇到跨域问题，这个我今天实际应用到自己的项目里面了，我服务端设置了跨域，所以没有出现跨域的问题，如果出现跨域需要修改你后端的请求头。

### 打印界面字符乱码：
这个我倒是碰到了，谷歌浏览器打印的时候，预览界面真的变成了 真·方块字 ，全是方块。这个问题是因为你pdf中使用了自定义字体导致的，具体解决方案如下：

首先，找到这个文件：node_modules/vue-pdf/src/pdfjsWrapper.js

然后根据github上这个issue，其中红色的是要删掉的，绿色的是要加上去的，照着改就可以解决了。

地址： https://github.com/FranckFreiburger/vue-pdf/pull/130/commits/253f6186ff0676abf9277786087dda8d95dd8ea7

根据我的实际测试，是可以解决打印乱码的问题的。

非常感谢能读到这里的朋友，你们的支持和关注是我坚持高质量分享下去的动力。

相关代码和文档已经上传至本人github。一定要点个**star**啊啊啊啊啊啊啊

**万水千山总是情，给个star行不行**

[韩数的开发笔记](https://github.com/hanshuaikang/HanShu-Note)

欢迎点赞，关注我，**有你好果子吃**（滑稽）


