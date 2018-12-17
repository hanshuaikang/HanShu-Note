package demo;

/***
 * 
 * @author 韩数
 * demo:浅谈java回调机制
 * Github:https://github.com/hanshuaikang
 *
 */

//定义一个学生类
public class Student {
    /***
     * 	定义一个学生做作业的方法
     * @param c 传入一个CallBack接口用来供Student中的doHomeworK方法调用完成回调
    */
	public void doHomeWork(CallBack c) {
		System.out.println("学生做作业。。。");
		System.out.println("---开始回调通知老师--- ");
		c.checkUpHomeWork();//回调函数
	}
	
	

}
