package strategy;


/***
 * 
 * @author韩数
 * demo1：活鸭子，特征是会飞，嘎嘎叫
 *
 */


public class LiveDuck extends Duck {
	
	
	
	public LiveDuck() {	
			
		/*
		 * 注:因为RubberDuck继承Duck类，所有Duck类中定义的
		 * flyBehavior和quackBehavior可以直接赋值
		 */
		
		//定义鸭子会飞的行为
		flyBehavior = new FlyWithWings();
		//定义鸭子嘎嘎叫的行为
		quackBehavior = new Quack();
		
		
	}
	
	

	@Override
	void display() {
		System.out.println("我是一只栩栩如生的鸭子！");
		
	}

}
