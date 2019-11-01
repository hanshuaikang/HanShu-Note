package observer;

/***
 * 
 * 测试类
 * 模拟天气改变时通知操作
 *
 */
public class WeatherStation {

	public static void main(String[] args) {
		
		WeatherData weatherData = new WeatherData();
		CurrentConditionsDisplay conditionsDisplay = new CurrentConditionsDisplay(weatherData);
		weatherData.setMeasurements(80, 90, 100l);
		weatherData.setMeasurements(90, 90, 100l);
	}
}
