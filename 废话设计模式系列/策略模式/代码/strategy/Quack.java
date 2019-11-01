package strategy;


/***
 * 
 * 定义鸭子叫声是嘎嘎嘎的行为
 * 
 */


public class Quack implements QuackBehavior {
	public void quack() {
		System.out.println("嘎嘎嘎");
	}
}