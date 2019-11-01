package strategy;


/***
 * 
 * 
 * @author 韩数
 * 测试类
 *
 */
public class Text {
	
	public static void main(String[] args) {
		Text t = new Text();
		t.rubberDuckDemoText();
		
		
		System.out.println("\n现在我们欢迎活的鸭子登场！\n");
		
		t.liveDuckDemoText();
	}
	
	
	
	public void rubberDuckDemoText() {
		
		RubberDuck rubberDuck = new RubberDuck();
		rubberDuck.display();
		rubberDuck.performFly();
		rubberDuck.performQuack();

		
		
		
	}
	
	public void liveDuckDemoText() {
		
		LiveDuck liveDuck = new LiveDuck();
		liveDuck.display();
		liveDuck.performFly();
		liveDuck.performQuack();
	
		
		
		
	}
	

}
