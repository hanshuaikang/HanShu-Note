package code.singleton.stat;

/***
 * 
 * @author 韩数
 * 常用的单例模式实现
 * 线程安全，不足之处，可能会损失一部分性能，非延迟加载
 *     
 */


public class Singleton {
	
	//在类初始化的时候就实例化对象,所以不存在多线程的安全问题
	private static Singleton uniqueInstance = new Singleton();
 
	private Singleton() {}
 
	public static Singleton getInstance() {
		return uniqueInstance;
	}
}
