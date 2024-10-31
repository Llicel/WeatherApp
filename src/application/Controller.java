package application;

import org.json.simple.JSONObject;

public class Controller {

	public JSONObject search(String locationName) {
		 return application.WeatherApp.getWeatherData(locationName);
	}
}
