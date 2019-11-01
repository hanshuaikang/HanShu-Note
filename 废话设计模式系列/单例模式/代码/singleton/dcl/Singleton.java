package code.singleton.dcl;

/***
 * 
 * @author 韩数
 * 线程安全，延迟加载方式实现单例模式
 *
 */

public class Singleton {
	
	private volatile static Singleton uniqueInstance;
	
	// 一旦一个共享变量（类的成员变量、类的静态成员变量）被volatile修饰之后，那么就具备了两层语义：
	//保证了不同线程对这个变量进行操作时的可见性，即一个线程修改了某个变量的值，这新值对其他线程来说是       立即可见的。
	//禁止进行指令重排序。
 
	private Singleton() {}
 
	public static Singleton getInstance() {
		//延迟加载
		if (uniqueInstance == null) {
			//加线程锁
			synchronized (Singleton.class) {
				if (uniqueInstance == null) {
					uniqueInstance = new Singleton();
				}
			}
		}
		return uniqueInstance;
	}
}

