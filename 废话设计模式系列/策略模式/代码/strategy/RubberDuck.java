package strategy;



/***
 * 
 * demo1：橡皮鸭，特征，不会飞，吱吱叫
 *
 */

public class RubberDuck extends Duck {
 
	public RubberDuck() {
		
		
		/*
		 * 注:因为RubberDuck继承Duck类，所有Duck类中定义的
		 * flyBehavior和quackBehavior可以直接赋值
		 */
		
		//定义橡皮鸭不会飞的行为
		flyBehavior = new FlyNoWay();
		//定义橡皮鸭吱吱叫的行为
		quackBehavior = new Squeak();
	}
 
	public void display() {
		System.out.println("我是一个橡皮鸭，我的身体是橡皮做哒");
	}
}