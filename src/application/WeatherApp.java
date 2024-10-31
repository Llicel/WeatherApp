package application;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

//retrieves weather data from external APi (Open-Meteo) and used Javafx framework to display it
public class WeatherApp {
	
	//looks through weather data for user input location
	public static JSONObject getWeatherData(String locationName) {
		
		//gathering location  using Geolocation API 
		JSONArray locationData = getLocationData(locationName);
		
		//extract latitude and longitude data
		JSONObject location = (JSONObject) locationData.get(0);
		double latitude = (double) location.get("latitude");
		double longitude = (double) location.get("longitude");
		
		//API request URL with location coordinates
		String urlString = "https://api.open-meteo.com/v1/forecast?"
				+ "latitude="+ latitude +"&longitude="+ longitude
				+ "&hourly=temperature_2m,relative_humidity_2m,apparent_temperature,precipitation_probability,precipitation,rain,showers,snowfall,snow_depth,weather_code,wind_speed_10m&temperature_unit=fahrenheit&wind_speed_unit=mph&precipitation_unit=inch&timezone=auto";
		
		try {
			//calling method fetchAPiResponse for our urlString 
			HttpURLConnection conn = fetchAPiResponse(urlString);
			
			//checking API response status
			//HTTP status code 200 means everything went well
			if(conn.getResponseCode() !=200) {
				System.out.println("Error: Could not connect to API");
				return null;
			}
			//store Json data
			StringBuilder resultJson = new StringBuilder();
			Scanner scanner = new Scanner(conn.getInputStream());
			while(scanner.hasNext()) {
				//reading and storing Json data into string builder
				resultJson.append(scanner.nextLine());
			}
			//close scanner
			scanner.close();
			//close url connection
			conn.disconnect();
			//parse through our data
			JSONParser parser = new JSONParser();
			JSONObject resultJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));
			
			//Retrieving hourly data
			JSONObject hourly =(JSONObject) resultJsonObj.get("hourly");
			
			//to get the current hour's data we will get the index of the current hour
			JSONArray time = (JSONArray) hourly.get("time");
			int index = findIndexOfCurrentTime(time);
			
			//get temperature 
			JSONArray temperatureData = (JSONArray) hourly.get("temperature_2m");
			double temperature = (double) temperatureData.get(index);
			
			//get weather code which gives us the weather description
			JSONArray weathercode = (JSONArray) hourly.get("weather,code");
			String weatherCondition = convertWeatherCode((long) weathercode.get(index));
			
			//get humidity
			JSONArray relativeHumidity = (JSONArray) hourly.get("relativehumidity_2m");
			long humidity = (long) relativeHumidity.get(index);
			
			//get windspeed
			JSONArray windspeedData = (JSONArray) hourly.get("windspeed_10m");
			double windspeed = (double) windspeedData.get(index);
			
			//creating JSONObject to access in frontend
			JSONObject weatherData = new JSONObject();
			weatherData.put("temperature", temperature);
			weatherData.put("weather_condition", weatherCondition);
			weatherData.put("humidity", humidity);
			weatherData.put("windspeed", windspeed);
			
			return weatherData;
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		
		return null;
	}
	
	
	private static int findIndexOfCurrentTime(JSONArray timeList) {
		String currentTime = getCurrentTime();
		
		//iterating through the times list and finding one that matches our current time
		for(int i =0; i<timeList.size(); i++) {
			String time =(String) timeList.get(i);
			if(time.equalsIgnoreCase(currentTime)) {
				//return the index
				return i;
			}
			
		}
		
		return 0;
	}


	public static String getCurrentTime() {
		// get current time and data
		LocalDateTime currentDateTime = LocalDateTime.now();
		
		//formatting date to be year-month-dayTime (this is how it is in the api)
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH':00'");
		
		//formating and printing the current date and time
		String formattedDateTime = currentDateTime.format(formatter);
		
		return formattedDateTime;
	}


	//takes in location and returns latitude and longitude data
	public static JSONArray getLocationData(String locationName) {
		//replacing whitespace in locationName with + to adhere to API's request format
		locationName = locationName.replaceAll(" ", "+");
		
		//API url using our location parameters
		String urlString ="https://geocoding-api.open-meteo.com/v1/search?name="
				+ locationName+"&count=10&language=en&format=json";
		
		try {
			//calling api and fetching a response
			HttpURLConnection conn = fetchAPiResponse(urlString);
			
			//checking API response status
			//HTTP status code 200 means everything went well
			if(conn.getResponseCode() !=200) {
				System.out.println("Error: Could not connect to API");
				return null;
			}else {
				//storing API results
				StringBuilder resultJson = new StringBuilder();
				//Creating Scanner to read Json data
				Scanner scanner = new Scanner(conn.getInputStream());
				
				//reading and storing Json data into string builder
				while(scanner.hasNext()) {
					resultJson.append(scanner.nextLine());
				}
				
				scanner.close();//closing scanner
				conn.disconnect();//closing url connection
				
				//parse the Josn string into a Json obj
				JSONParser parser = new JSONParser();
				JSONObject resultsJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));
				
				//getting the list of location data the API generated from the location name
				JSONArray locationData = (JSONArray) resultsJsonObj.get("results");
				return locationData;
			
			}
			
			
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		//couldn't find location
		return null;
	}


	private static HttpURLConnection fetchAPiResponse(String urlString) {
		try {
			//turning api string into a URL
			URL url = new URL(urlString);
			//opening up a http request to the site (establishing connection)
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			
			//set request method to get (type of API call)
			conn.setRequestMethod("GET");
			
			//connect to our api
			conn.connect();
			return conn;
		}catch(IOException e) {
			e.printStackTrace();
		}
		//Couldn't make connection
		return null;
	}
	
	//making the weather code more readable
	private static String convertWeatherCode(long weathercode) {
		String weatherCondition ="";
		if(weathercode ==0L) {
			weatherCondition ="Clear";
		}else if(weathercode<= 3L && weathercode>0L) {
			weatherCondition ="Cloudy";
		}else if((weathercode>= 51L && weathercode<=67L)
				||(weathercode>=80L && weathercode <=99L)){
			//rain
			weatherCondition = "Rain";
		}else if(weathercode >= 71L && weathercode <= 77L) {
			//snow
			weatherCondition= "Snow";
		}
		return weatherCondition;
		
	}
}
