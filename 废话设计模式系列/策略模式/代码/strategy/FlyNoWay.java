package strategy;



/***
 * 
 * 定义鸭子飞行行为为不会飞的类
 *
 */
public class FlyNoWay implements FlyBehavior {
	public void fly() {
		System.out.println("我不会飞");
	}
}
