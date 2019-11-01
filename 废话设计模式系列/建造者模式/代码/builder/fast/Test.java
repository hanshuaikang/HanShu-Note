package code.builder.fast;

public class Test {
	
	public static void main(String[] args) {
		
		Computer computer = new Computer.ComputerBuilder("i9", "三星", "华硕", "金士顿")
				.buildMouse("罗技")
				.bulidKeyBoard("雷蛇")
				.create();
		
		System.out.println(computer);
	
		
		
		
	}

}
