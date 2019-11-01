package demo;


/***
 * 
 * @author 韩数
 * demo:浅谈java回调机制
 * Github:https://github.com/hanshuaikang
 *
 */


//定义一个教师类并且实现CallBack接口
public class Teacher implements CallBack {

	//构造器中传入一个学生对象
    public Teacher(Student s) {
		// TODO Auto-generated constructor stub
    	//把当前类传递给Student
    	s.doHomeWork(this);
	}

    

    //实现接口中的checkUpHomeWord方法
	@Override
	public void checkUpHomeWork() {
		// TODO Auto-generated method stub
		System.out.println("老师检查作业");
		
	}

}
