# 用java实现一个简单的区块链

## 前言:

之前其实一直有考虑要不要写一篇这篇文章，因为类似的文章在网上实在太多了，而且只要你细读几篇会发现，很多文章除了内容都是出奇的雷同之外，大多数都是源于国外大神的两篇关于java区块链的教程的生硬翻译，这就很容易导致一个问题，就是你明明把他代码跑起来了，最后却还不知道区块链是个啥，比如是如何做到去中心化的？又是如何做到不可篡改行的，以及比特币为什么越挖越少这些问题依然得不到很好的解释，本篇文章呢，依然源用的是那篇教程的代码，除了英文注释我会手动翻译成中文之外，其他的变量名什么的都不会改，毕竟人家思路是对的，没有必要重新造轮子，改个变量就说代码是我自己写的，那我和网上那些抄袭转载的人也没什么区别了。本篇文章并不是对作者源论文的机械翻译，只是借用了相关代码，希望大家看过之后会大致明白区块链具体是一项什么样的技术，我们为什么需要区块链等等。

不说废话，先上东西。

## 什么是区块链：

区块链是分布式数据存储、点对点传输、共识机制、加密算法等计算机技术的新型应用模式。，区块链是比特币的一个重要概念，它本质上是一个去中心化的数据库，同时作为比特币的底层技术，是一串使用密码学方法相关联产生的数据块，每一个数据块中包含了一批次比特币网络交易的信息，用于验证其信息的有效性（防伪）和生成下一个区块 - 百度百科。

？？？ 没看懂，抬走下一个。

区块链到底是什么，**区块链就是谈恋爱**

还不明白，且看下面一个小故事，受限于篇幅，就不写地花里胡哨的了。

明天就是小明和小红在一起100天的纪念日了，小明想给小红一个大大的惊喜，就是把送她自己这三个月辛苦研发的机器人，想到小红收到礼物手舞足蹈的样子，小明就觉得这简直太他妈浪漫了。

当第二天小明把准备好的礼物送给小红的时候，意外发生了。

`小黑`: 你们不能在一起，其实小红...

`小明`：卧槽，难道小红是我亲妹妹？这是什么狗血剧情啊

`小黑`: 不，我想说的是，其实小红是我的女朋友。

`小明`：卧槽？你凭什么说小红是你的女朋友？

`小黑`：那你凭什么说小红是你的女朋友，我说是我的就是我的。

重点来了，知识点呐朋友们,要记住：

`小明:`我们谈恋爱的第一天，我送了小红一个鼠标，第二天，我送了她一个键盘，第三天，我送了她一个屏幕，第四天..... 第一百天，我送了她一个我辛苦开发的机器人，这些，就是证据！

`小黑:`啊啊啊啊啊，我输了，好吧，小红是你的女朋友。

小黑由于故意篡改小红是小明女朋友的事实，被拉入黑名单，从此再也没有找到女朋友。

在这个例子中，`小红`，`小明`，和`小黑`就是区块链中的链，而之前`小明`和`小红`从相识，到相知，再到相爱期间每发生一个故事就会形成一个区块。**而且小明和小红之间发生的所有故事都会以直播的形式被区块链中的所有链知道**（太狠了吧），所以小黑说小红是他的女朋友自然就不可能是真的了，因为整个区块链所有的链都见证了小明和小红是情侣这个事实，如果小黑要篡改事实说小红是他的女朋友，那么他要修改整个区块链中所有链对于小明和小红这对情侣的记忆，这是几乎无法做到的。

这就好比全世界都知道特朗普是现在美国的总统，你现在说特朗普不是美国总统，美国总统是川建国，你这么说外人一看就知道是假的，如果要把它变成真的，就需要改变全世界所有知道这件事的人的记忆才行。

这就是区块链的**不可篡改性**。

同时，如果有一天小明不爱小红了，爱上了小绿，于是把手机上和小红有关的东西全部删掉，告诉小绿，小绿是自己的初恋，小明只爱小绿一个人。这样做有用吗？没用，因为整个区块链中的链都观看过小明和小红的直播，记录着小明和小红曾经在一起过的证据。

这就是区块链中的**不可修改性**。

大家认真想一想，如果区块链应用于金融会怎么样？

之前我们的钱都是存在银行的账户管理系统里面，如果有人侵入银行的账户管理系统，只需要把他账号下代表余额的那串数字改了就可以决定自己有多少钱了，**而在区块链中，每个人都是银行，每个人都是账户管理系统，如果需要修改自己账户的余额，则需要修改全网所有节点的信息才行**，这几乎是不可能实现的，所以大大的提高了安全性。更不要说应用于其他领域了。

而比特币，就是区块链在割韭菜领域的一个重要应用。

## 用java实现一个简单的区块链：

知道区块链是什么了以后，我们接下来回归一个开发者的本心，从技术的角度去简单的看一下区块链是如何实现的。

区块链，区块链，首先我们得有一个区块类，而且这个类要有一个最重要的特征，能够形成链。（??神逻辑）

我们新建一个Block 类，代码如下:

```java
public class Block {

    /**
     * 当前区块的hash
     */
    public String hash;

    /**
     * 前一个区块的hash,本例中，靠这个实现链的
     */
    public String previousHash;

    /**
     * 当前区块的数据，比如交易信息啊等等，在谈恋爱例子中代表小红和小明具体发生的事件
     */
    private String data;

    /**
     * 时间戳
     */
    private long timeStamp;

    private int nonce;

    public Block(String hash, String previousHash, String data) {
        this.hash = hash;
        this.previousHash = previousHash;
        this.data = data;
    }

    public Block(String data, String previousHash) {
        this.previousHash = previousHash;
        this.data = data;
        this.timeStamp = new Date().getTime();
        this.hash = calculateHash();
    }

    public String calculateHash() {
        String calculatedhash = StringUtil.applySha256(
                previousHash +
                        Long.toString(timeStamp) +
                        Integer.toString(nonce) +
                        data);
        return calculatedhash;
    }

    public void mineBlock(int difficulty) {
        //Create a string with difficulty * "0"
        String target = new String(new char[difficulty]).replace('\0', '0'); 
        while(!hash.substring( 0, difficulty).equals(target)) {
            nonce ++;
            hash = calculateHash();
        }
    }
}

```

**变量解释:**

- hash :当前区块的哈希值
- previousHash ：上一个区块的哈希值，就是靠这个实现链的，怎么有点像链表（嘘）
- data：当前区块所存储的信息，比如小明给小红买口红这一件事。
- timeStamp：时间戳， 比如小明给小红买口红这一件事发生的时间信息。
- nonce： 只是一个普通的基数变量。

这个类中需要大家去理解的可能就是`mineBlock ()`和` calculateHash() `这个两个方法了，而`nonce`就是**关键变量**。

在`mineBlock()`方法中会不停的执行hash运算，直到算出符合条件的区块，而`nonce`就是算出来改区块所需要的次数，在`calculateHash()`中，我们加上`nonce`次数，就可以一下子计算出来这个`hash`了。

而且大家看这个`calculateHash()`这个方法，我们在执行hash计算的时候，是以上一个区块的`hash`为参数进行的，一旦上一个区块的`hash`是个假的，或者被篡改了，那么无论怎么计算，`calculateHash()`方法返回的hash值，和该区块本身的hash值是几乎不可能一样的，也就很容易发现区块被人篡改了。

而加密算法呢，作者选用的是SHA-256， 也是比特币所采用的加密算法，被公认为最安全最先进的算法之一 .

`StringUtil `类的代码如下所示,由于这个和我们今天要讲的主题关系不大，所以就不过多阐述了。

```java
public class StringUtil {

    public static String applySha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
}
```

`JsonUtil `类代码如下:

```java
public class JsonUtil {
    public static String toJson(Object object){
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();
        Gson gson = gsonBuilder.create();
        return gson.toJson(object);
    }
}
```

既然最基础的区块已经写好了，那么我们就来简单地测试一下，看看是不是真的能挖到矿。

新建三个Block区块，把`mineBlock()`方法的参数设置为5.

测试类代码如下:

```java
public class BlockChainTest {

    public static void main(String[] args) {
        //第1个区块
        Block firstBlock = new Block("我是第1个区块", "0");
        firstBlock.mineBlock(5);
        System.out.println("第1个区块hash: " + firstBlock.hash);

        //第2个区块
        Block secondBlock = new Block("我是第2个区块", firstBlock.hash);
        secondBlock.mineBlock(5);
        System.out.println("第2个区块hash: " + secondBlock.hash);

        //第3个区块
        Block thirdBlock = new Block("我是第3个区块", secondBlock.hash);
        thirdBlock.mineBlock(5);
        System.out.println("第3个区块hash: " + thirdBlock.hash);
    }
}
```

运行结果如下所示:

```text
第1个区块hash: 0000052659276be66678fd482825b20bd0819a800246d23d171da6270e92589c
第2个区块hash: 000000d1f338b2dc6b02cca8ec158ca7acafa9cbf699ca97fc1ed7b260a65652
第3个区块hash: 0000072381c11b9a160b1b1d93b75cb477c286db63bb541fcede7ab163ac696c
```

发现什么了吗？即我们挖到的三个区块，前五个数字都是0诶，好神奇哦

`00000`52659276b

**所以挖矿的本质其实就是通过哈希计算得到符合条件的hash的过程。**

所以知道比特币为什么越挖越少了吧。

**因为在所有计算结果之中，符合条件的hash是有限的，而且越算越少，刚开始计算的时候，由于符合条件的hash实在是太多了，所以所需的算力比较小，很容易就计算出来了，而越往后，未被计算出来过的符合条件的hash值就越少，算出来所需要的算力越大，当最后只剩一个符合条件的hash的时候，那时候就真的无异于大海捞针了。这可能就是为什么十年前随便一台电脑都能轻轻松松地挖出来比特币，而现在却需要几千台矿机的矿场才能挖出来的原因吧**

也就是刚开始沙漠里有五百万个宝藏，全世界的人都去找，刚开始宝藏很多，大家走一步就发现一个宝藏，后来宝藏被找的只剩几个了，那么大的沙漠，为了挖到宝藏，就不得不派出更多的人去找。

这个时候可能有人会提出疑问了，我怎么知道你这个区块是不是合法的，看着是这样，万一他不合法我也不知道啊。被改了我也不知道。别慌我们慢慢来。

```java
public class BlockChainListTest {

    //这玩意就是我们的区块链，存储我们所有的区块信息。（简陋版）
    public static ArrayList<Block> blockChain = new ArrayList();

    //挖矿的难度，就是计算出来的hash前几个字符是0才是合法的。
    public static int difficulty = 5;

    public static void main(String[] args) {
        blockChain.add(new Block("我是第1个区块", "0"));
        blockChain.get(0).mineBlock(difficulty);

        blockChain.add(new Block("我是第2个区块", blockChain.get(blockChain.size() - 1).hash));
        blockChain.get(1).mineBlock(difficulty);

        blockChain.add(new Block("我是第3个区块", blockChain.get(blockChain.size() - 1).hash));
        blockChain.get(2).mineBlock(difficulty);

        System.out.println("区块链是否合法: " + isChainValid());
        System.out.println(JsonUtil.toJson(blockChain));
    }
    

    public static Boolean isChainValid(){

        Block currentBlock;
        Block previousBlock;
        boolean flag = true;
        String hashTarget = new String(new char[difficulty]).replace('\0', '0');

        //循环遍历列表检验hash
        for(int i=1;i<blockChain.size();i++){
            currentBlock = blockChain.get(i);
            previousBlock = blockChain.get(i-1);
            //比较注册的hash和计算的hash
            if(!currentBlock.hash.equals(currentBlock.calculateHash())){
                System.out.println("当前hash不相等");
                flag=false;
            }
            //比较当前的前一个hash与注册的前一个hash
            if(!previousBlock.hash.equals(currentBlock.previousHash)){
                System.out.println("前一个hash不相等");
                flag=false;
            }

            //检查该区块是不是已经被算出来过了。
            if(!currentBlock.hash.substring( 0, difficulty).equals(hashTarget)) {
                System.out.println("这个区块还没有被开采，也就是你这个区块他不是合格的");
                flag=false;
            }
        }

        return flag;
    }
}

```

执行之后结果如下所示:

```json
区块链是否合法: true
[
  {
    "hash": "00000dd5f9b665c79f454adb1491af1042883f43818938191a8337202929cdeb",
    "previousHash": "0",
    "data": "我是第1个区块",
    "timeStamp": 1574822479084,
    "nonce": 614266
  },
  {
    "hash": "0000063312c3a5832a8990ad064e962c98e8ef9ffca969cc0c049cfd84773fdc",
    "previousHash": "00000dd5f9b665c79f454adb1491af1042883f43818938191a8337202929cdeb",
    "data": "我是第2个区块",
    "timeStamp": 1574822480646,
    "nonce": 429710
  },
  {
    "hash": "00000304ecc09cca5eac2aed4875c2097a34efcab3518f4f886708a133c513db",
    "previousHash": "0000063312c3a5832a8990ad064e962c98e8ef9ffca969cc0c049cfd84773fdc",
    "data": "我是第3个区块",
    "timeStamp": 1574822481635,
    "nonce": 262515
  }
]
```

`isChainValid()`就是们检查区块链是否合法的方法了。

## 下面开始技术总结:

今天这篇文章呢，花费了大量的篇幅在如何去理解区块链这个概念上，我们得先知道这玩意是什么，能干什么，我们再学习的过程中思路就会清晰许多，后期的文章呢，如果还有的话（马上课就多了），依然沿着国外大佬的思路，去实现一个可以交易的区块链，当然，代码依然不是下一篇文章的重点，考虑到大多数人只是扩展自己的知识，去了解一个区块链这项技术，并没有什么打算去转行搞区块链什么的，所以下一篇会集中在 **UTXO（Unspent Transaction Outputs )**  未花费的交易输出这个比特币核心概念的理解上，去简单的了解一下区块链是如何去中心化交易的。

最后我们则将会将我们的程序迁移到web上，实现一个跨时代的区块链产品-**别逼币**

相关代码已经上传至本人github。一定要点个**star**啊啊啊啊啊啊啊

**万水千山总是情，给个star行不行**

[韩数的开发笔记](https://github.com/hanshuaikang/HanShu-Note)

欢迎点赞，关注我，**有你好果子吃**（滑稽）