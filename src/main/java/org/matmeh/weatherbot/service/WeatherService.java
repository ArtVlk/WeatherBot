package org.matmeh.weatherbot.service;

import lombok.extern.slf4j.Slf4j;
import org.matmeh.weatherbot.BotProperties;
import org.matmeh.weatherbot.dto.CityResponse;
import org.matmeh.weatherbot.dto.WeatherResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Slf4j
@Service
@Configuration
public class WeatherService {

    private final BotProperties botProperties;
    private final String API_OpenCage;
    private final String API_OpenWeather;
    private final RestClient httpCityClient;
    private final RestClient httpWeatherClient;

    public WeatherService(RestClient.Builder restClientBuilder, BotProperties botProperties) {
        this.botProperties = botProperties;
        this.API_OpenCage = botProperties.getAPIOpenCage();
        this.API_OpenWeather = botProperties.getAPIOpenWeather();

        httpCityClient = restClientBuilder.baseUrl("https://api.opencagedata.com/geocode/v1").build();
        httpWeatherClient = restClientBuilder.baseUrl("https://api.openweathermap.org/data/2.5").build();
    }



    public String getWeather(double latitude, double longitude) {
        String city;
        try {

            city = getCityByLocation(latitude, longitude);
        } catch (Exception exception) {
            log.error("Error in WeatherService::getWeather");
            exception.printStackTrace();
            return "Город по указанным координатам не найден. Пожалуйста, введите название города:";
        }

        return getWeather(city);
    }

    public boolean doesCityExist(String city) {
        try {
            String weather = httpWeatherClient
                    .get()
                    .uri("/weather?q=" + city + "&appid=" + API_OpenWeather + "&units=metric&lang=ru")
                    .retrieve()
                    .body(WeatherResponse.class)
                    .getWeather();

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

    private String getCityByLocation(double latitude, double longitude) {
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
