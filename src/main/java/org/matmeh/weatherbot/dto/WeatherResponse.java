package org.matmeh.weatherbot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeatherResponse {
    private List<HashMap<String, String>> weather;

    private HashMap<String, Float> main;

    private HashMap<String, Float> wind;

    public String getWeather() {
        return "Температура: " + main.get("temp") + "C° (Ощущается, как " + main.get("feels_like") + "C°)" + "\n" +
                weather.get(0).get("description") + "\n" +
                "Атмосферное давление: " + main.get("pressure") * 0.75 + " мм.рт.ст.\n" +
                "Влажность воздуха: " + main.get("humidity") + "%\n" +
                "Скорость ветра: " + wind.get("speed") + " м/с";

    }
    public static String getWeatherFive(String city, StringBuffer response) {
        JSONObject jsonObject = new JSONObject(response.toString());
        JSONArray list = jsonObject.getJSONArray("list");
        StringBuilder resultBuilder = new StringBuilder();
        for (int i = 0; i < 40; i += 8) { // считывается прогноз каждые 3 часа, поэтому считываем только 1 раз в день
            JSONObject dayForecast = list.getJSONObject(i);
            String dateText = dayForecast.getString("dt_txt");
            JSONObject main = dayForecast.getJSONObject("main");
            double temp = main.getDouble("temp");
            double feelsLike = main.getDouble("feels_like");
            int pressure = (main.getInt("pressure"));
            pressure = (int) (pressure * (0.75));
            int humidity = main.getInt("humidity");
            JSONArray weatherArray = dayForecast.getJSONArray("weather");
            JSONObject weatherData = weatherArray.getJSONObject(0);
            String weatherDescription = weatherData.getString("description");
            JSONObject wind = dayForecast.getJSONObject("wind");
            double windSpeed = wind.getDouble("speed");

            resultBuilder.append("\nДата и время: " + dateText + "\n"
                    + "Температура: " + temp + "C° (Ощущается, как " + feelsLike + "C°)" + "\n"
                    + weatherDescription + "\n"
                    + "Атмосферное давление: " + pressure + " мм.рт.ст.\n"
                    + "Влажность воздуха: " + humidity + "%\n"
                    + "Скорость ветра: " + windSpeed + " м/с\n");
        }
        return String.valueOf(resultBuilder);
    }

}