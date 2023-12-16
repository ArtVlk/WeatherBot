package org.matmeh.weatherbot.service;

import lombok.extern.slf4j.Slf4j;
import org.matmeh.weatherbot.BotProperties;
import org.matmeh.weatherbot.dto.CityResponse;
import org.matmeh.weatherbot.dto.WeatherResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Slf4j
@Service
@Configuration
@Component
public class WeatherService {

    private final String API_OpenCage;
    private final String API_OpenWeather;
    private final RestClient httpCityClient;
    private final RestClient httpWeatherClient;


    public WeatherService(RestClient.Builder restClientBuilder, BotProperties botProperties) {
        this.API_OpenCage = botProperties.getAPIOpenCage();
        this.API_OpenWeather = botProperties.getAPIOpenWeather();

        httpCityClient = restClientBuilder.baseUrl("https://api.opencagedata.com/geocode/v1").build();
        httpWeatherClient = restClientBuilder.baseUrl("https://api.openweathermap.org/data/2.5").build();
    }

    public String getWeatherAddCity(double latitude, double longitude) {
        String city;
        try {

            city = getCityByLocation(latitude, longitude);
        } catch (Exception exception) {
            log.error("Error in WeatherService::getWeather");
            exception.printStackTrace();
            return "Город по указанным координатам не найден. Пожалуйста, введите название города:";
        }
        return city;
    }

    public String getWeather(double latitude, double longitude) {
        String city;
        try {

            city = getCityByLocation(latitude, longitude);
        } catch (Exception exception) {
            log.error("Error in WeatherService::getWeather");
            exception.printStackTrace();
            return "Город по указанным координатам не найден. Пожалуйста, введите название города вручную";
        }

        return getWeather(city);
    }

    public boolean doesCityExist(String city) {
        try {
            System.out.println("https://api.openweathermap.org/data/2.5"+ "/weather?q=" + city + "&appid=" + API_OpenWeather + "&units=metric&lang=ru");
            String weather = httpWeatherClient
                    .get()
                    .uri("/weather?q=" + city + "&APPID=" + API_OpenWeather + "&units=metric&lang=ru")
                    .retrieve()
                    .body(WeatherResponse.class)
                    .getWeather();
            System.out.println(weather);
            if (weather != null && !weather.isBlank()) {
                return true;
            }
        } catch (Exception exception) {
            log.error("Error in WeatherService::getWeather");
            exception.printStackTrace();
        }

        return false;

    }

    public String getWeather(String city) {
        log.trace("Getting weather for city: {}", city);

        try {
            String weather = httpWeatherClient
                    .get()
                    .uri("/weather?q=" + city + "&appid=" + API_OpenWeather + "&units=metric&lang=ru")
                    .retrieve()
                    .body(WeatherResponse.class)
                    .getWeather();

            if (weather != null && !weather.isBlank()) {
                return "Погода в городе " + city + " на " + getCurrantTime() + ":\n" + weather;
            }
        } catch (Exception exception) {
            log.error("Error in WeatherService::getWeather");
            exception.printStackTrace();
        }

        return "Не удается получить прогноз погоды для  города " + city + ". Пожалуйста, попробуйте еще раз.";
    }
    public String getWeatherForFiveDays(String city) {
        log.trace("Getting weather for city for next five days: {}", city);

        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append("Прогноз погоды на 5 дней для города " + city + ":\n");

        try {
            URL url = new URL( "https://api.openweathermap.org/data/2.5/forecast?q=" + city + "&appid=" + API_OpenWeather + "&units=metric&lang=ru");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                String weatherDetails = WeatherResponse.getWeatherFive(city, response);
                if (weatherDetails != null && !weatherDetails.isBlank()) {
                    resultBuilder.append(weatherDetails + "\n");
                }
            } else {
                log.warn("Got an empty response from the Weather API for city {}", city);
            }

            return resultBuilder.toString();

        } catch (Exception exception) {
            log.error("Error in WeatherService::getWeatherForFiveDays", exception);
        }

        return "Не удается получить прогноз погоды на 5 дней для города " + city + ".\nПожалуйста, попробуйте еще раз.";
    }
    protected String getCityByLocation(double latitude, double longitude) {
        log.trace("Getting city for coords: {}, {}", latitude, longitude);

        return httpCityClient
                .get()
                .uri("/json?q=" + latitude + "," + longitude + "&key=" + API_OpenCage + "&language=ru")
                .retrieve()
                .body(CityResponse.class)
                .getCity();
    }

    private static String getCurrantTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
    }
}
