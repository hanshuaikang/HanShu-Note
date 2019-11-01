package observer;

/***
 * 
 * @author 韩数
 * 定义一个主题的超类，规定方法行为
 *
 */
public interface Subject {
	//注册一个观察者(被通知者)，这里Observer也是所有类的超类
	public void registerObserver(Observer o);
	//移除一个观察者(被通知者)，
	public void removeObserver(Observer o);
	//通知所有观察者(被通知者)，
	public void notifyObservers();
}
