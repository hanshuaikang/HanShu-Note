package code.builder;

public class ComputerBuilder implements Builder{
	
  private Computer computer = new Computer() ;

	@Override
	public void installMainBoard(String mainBoard) {
		computer.setMainBoard(mainBoard);
		
	}

	@Override
	public void installCpu(String cpu) {
		computer.setCpu(cpu);
		
	}

	@Override
	public void installhardDisk(String hardDisk) {
		computer.setHardDisk(hardDisk);
	}

	@Override
	public void installMemory(String memory) {
		computer.setMemory(memory);
	}

	@Override
	public void installKeyBoard(String keyboard) {
		computer.setKeyboard(keyboard);
	}

	@Override
	public void installMouse(String mouse) {
		computer.setMouse(mouse);
	}

	@Override
	public Computer createComputer() {
		// TODO Auto-generated method stub
		return computer;
	}

}
