package code.singleton.threadsafe;

/**
 * 
 * @author 韩数
 * 线程安全方式实现单例模式，缺点是，每次都会调用synchronized的关键字修饰的方法，会损失一定的性能
 *
 */

public class Singleton {
	
	
	private static Singleton uniqueInstance;
 
	private Singleton() {}
 
	//synchronized修饰该方法，保证每次只有一个线程进入该方法，从而避免产生多个Singleton对象实例。
	public static synchronized Singleton getInstance() {
		if (uniqueInstance == null) {
			uniqueInstance = new Singleton();
		}
		return uniqueInstance;
	}
}
