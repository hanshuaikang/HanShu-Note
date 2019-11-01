package observer.observerUseJava;

import java.util.Observable;
import java.util.Observer;

import observer.DisplayElement;
	
public class CurrentConditionsDisplay implements Observer, DisplayElement {
	
	Observable observable;//定义一个Observable
	private float temperature;
	private float humidity;
	
	public CurrentConditionsDisplay(Observable observable) {
		this.observable = observable;
		observable.addObserver(this);//注册该观察者
	}
	
	@Override
	public void update(Observable obs, Object arg) {
		if (obs instanceof WeatherData) {//如果obs是WeatherData类的一个实例
			WeatherData weatherData = (WeatherData)obs;
			//获取观察者需要的数据
			this.temperature = weatherData.getTemperature();
			this.humidity = weatherData.getHumidity();
			display();//通知显示
		}
	}
	
	//显示方法
		@Override
		public void display() {
			System.out.println("温度："+temperature+"\n湿度:"+humidity);
		}
}
