package code.builder;

public class Director {
	
	private Builder builder ;
	 
	 public Director(Builder builder){
	        this.builder = builder ;
	    }
	 
	   public Computer createComputer(String cpu,String hardDisk,String mainBoard,String memory,String keyboard,String mouse){
	        // 具体的工作是装机工去做
	        this.builder.installMainBoard(mainBoard);
	        this.builder.installCpu(cpu) ;
	        this.builder.installMemory(memory);
	        this.builder.installhardDisk(hardDisk);
	        this.builder.installKeyBoard(keyboard);
	        this.builder.installMouse(mouse);
	        return this.builder.createComputer() ;
	    }
	  
	   
	    


}
