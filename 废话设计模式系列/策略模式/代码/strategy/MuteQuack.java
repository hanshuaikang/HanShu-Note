package strategy;


/***
 * 
 * 定义鸭子叫声行为是不会叫的类
 * 
 *
 */

public class MuteQuack implements QuackBehavior {
	public void quack() {
		System.out.println("<< 我不会叫,我是一个沉默的鸭子>>");
	}
}
