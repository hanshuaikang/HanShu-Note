package observer;

/***
 * 
 * @author 韩数
 * 声明通知者通用的接口，定义updata方法
 *
 */
public interface Observer {
	public void update(float temp, float humidity, float pressure);
}
