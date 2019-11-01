package strategy;


/***
 * 
 * 定义鸭子飞行行为为会飞的类
 * 
 */

public class FlyWithWings implements FlyBehavior {
	public void fly() {
		System.out.println("我会飞,哈哈哈!");
	}
}
