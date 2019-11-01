package strategy;

/***
 * 
 * 
 * @author 韩数
 * 设计模式之策略模式
 * Github地址:https://github.com/hanshuaikang/design-pattern-java
 *
 */


public abstract class Duck {

	/*
	 * 面向超类编程，主类Duck只保留所有鸭子通用不变的特征比如游泳
	 * 变化的部分单独封装，提高代码的弹性，避免因为单一的向下继承
	 * 造成的代码的灵活性降低，避免过于耦合情况的发生。
	 */
	
	FlyBehavior flyBehavior;
	QuackBehavior quackBehavior;
 
	public Duck() {
	}
 
	
	//定义set方法，可以动态的设定鸭子飞行的行为
	public void setFlyBehavior (FlyBehavior fb) {
		flyBehavior = fb;
	}
	
	//定义set方法，可以动态的设定鸭子叫声的行为
	public void setQuackBehavior(QuackBehavior qb) {
		quackBehavior = qb;
	}
 
	//鸭子的外表，这里定义为抽象方法，父类只做声明，不负责实现
	abstract void display();
 
    //鸭子的行为，Duck类不适合实现，交给相应的模块实现。
	public void performFly() {
		flyBehavior.fly();
	}
 
	public void performQuack() {
		quackBehavior.quack();
	}
    
    
    //所有鸭子都会游泳
	public void swim() {
		System.out.println("All ducks float, even decoys!");
	}
}
