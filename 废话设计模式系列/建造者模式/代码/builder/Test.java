package code.builder;

public class Test {
	
	public static void main(String[] args) {
		Builder builder = new ComputerBuilder();
		Director director = new Director(builder);
		Computer computer = director.createComputer("I9", "三星", "华硕", "三星" ,"罗技", "罗技");
		System.out.println(computer);
		
	}
	
	

}
