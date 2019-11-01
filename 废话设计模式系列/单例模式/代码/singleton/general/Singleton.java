package code.singleton.general;


/***
 * 
 * @author 韩数
 * 一般单例模式实现，采用延迟加载方式实现
 *
 */

public class Singleton {
	
	private static Singleton uniqueInstance;
 
	private Singleton() {}
 
	public static Singleton getInstance() {
		if (uniqueInstance == null) {
			uniqueInstance = new Singleton();
		}
		return uniqueInstance;
	}
 
}
