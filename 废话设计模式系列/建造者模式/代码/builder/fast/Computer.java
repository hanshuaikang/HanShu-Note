package code.builder.fast;

public class Computer {

	private String cpu ; // cpu
    private String hardDisk ; //硬盘
    private String mainBoard ; // 主板
    private String memory ; // 内存
    private String keyboard;//键盘
    private String mouse;//鼠标
    
    //定义成private，使其不能被直接创建出来
    private Computer() {}
    
    
	public String getCpu() {
		return cpu;
	}

	public void setCpu(String cpu) {
		this.cpu = cpu;
	}

	public String getHardDisk() {
		return hardDisk;
	}

	public void setHardDisk(String hardDisk) {
		this.hardDisk = hardDisk;
	}

	public String getMainBoard() {
		return mainBoard;
	}

	public void setMainBoard(String mainBoard) {
		this.mainBoard = mainBoard;
	}

	public String getMemory() {
		return memory;
	}

	public void setMemory(String memory) {
		this.memory = memory;
	}

	public String getKeyboard() {
		return keyboard;
	}

	public void setKeyboard(String keyboard) {
		this.keyboard = keyboard;
	}

	public String getMouse() {
		return mouse;
	}

	public void setMouse(String mouse) {
		this.mouse = mouse;
	}


	@Override
	public String toString() {
		return "Computer [cpu=" + cpu + ", hardDisk=" + hardDisk + ", mainBoard=" + mainBoard + ", memory=" + memory
				+ ", keyboard=" + keyboard + ", mouse=" + mouse + "]";
	}

    
	//Builder 静态内部类
    public static class ComputerBuilder {
    	
    	
    	 //创建computer实例
        private Computer computer = new Computer();
        //必须的参数
        public ComputerBuilder(String cpu,String hardDisk,String mainBoard,String memory) {
        	computer.setCpu(cpu);
        	computer.setHardDisk(hardDisk);
        	computer.setMainBoard(mainBoard);
        	computer.setMemory(memory);	
        }
        
        //以下是额外可附加的配置
        public ComputerBuilder bulidKeyBoard(String keyboard) {
        	computer.setKeyboard(keyboard);
        	return this;
        }
        
        public  ComputerBuilder buildMouse(String mouse) {
        	computer.setMouse(mouse);
        	return this;
        }
        
        
        public Computer create() {
            if (computer==null){
                throw  new IllegalStateException("computer is null");
            }
            return computer;
        }

    	
    }
    
    

}
