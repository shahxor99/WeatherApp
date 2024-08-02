package org.example;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class WeatherApp {
    private static final String API_KEY = "161792de7194bbebcfcc624cd6c7702b"; // change API key in case this is not working
    private static final String API_URL = "https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s&units=metric";

    public static void main(String[] args) {
        List<String> cities = readCitiesFromFile(); //add or modify city names to get there current weather in cities.txt file
        List<String> weatherData = new ArrayList<>();

        for (String city : cities) {
            String temperature = getTemperatureForCity(city);
            weatherData.add(city + ": " + temperature + "°C");
        }

        writeWeatherDataToFile(weatherData);
    }

    private static List<String> readCitiesFromFile() {
        List<String> cities = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("resources/cities.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                cities.add(line);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading cities from file", e);
        }
        return cities;
    }

    private static String getTemperatureForCity(String city) {
        try {
            String encodedCity = URLEncoder.encode(city, "UTF-8");
            String urlString = String.format(API_URL, encodedCity, API_KEY);
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            conn.disconnect();

            // Parse the JSON response to extract the temperature
            JsonObject jsonObject = JsonParser.parseString(content.toString()).getAsJsonObject();
            JsonObject mainObject = jsonObject.getAsJsonObject("main");
            double temperature = mainObject.get("temp").getAsDouble();

            return String.valueOf(temperature);
        } catch (IOException e) {
            throw new RuntimeException("Error getting temperature for city: " + city, e);
        }
    }

    private static void writeWeatherDataToFile(List<String> weatherData) {
        try (FileWriter writer = new FileWriter("output/weather_data.txt")) {
            for (String data : weatherData) {
                writer.write(data + "\n");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error writing weather data to file", e);
        }
    }
}
