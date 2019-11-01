package observer.observerUseJava;

/***
 * 
 * 测试类
 * 模拟天气改变时通知操作
 *
 */

public class WeatherStation {

	public static void main(String[] args) {
		WeatherData weatherData = new WeatherData();
		CurrentConditionsDisplay currentConditions = new CurrentConditionsDisplay(weatherData);
		weatherData.setMeasurements(80, 65, 30.4f);

	}
}
