package code.builder;

public interface Builder {
	
    void installMainBoard(String mainBoard) ;//安装主板
    void installCpu(String cpu) ;    // 安装 cpu
    void installhardDisk(String hardDisk) ;    // 安装硬盘
    void installMemory(String memory) ;    // 安装内存
    void installKeyBoard(String keyboard) ;    // 安装内存
    void installMouse(String mouse); //安装鼠标
    Computer createComputer() ;    // 组成电脑


}
