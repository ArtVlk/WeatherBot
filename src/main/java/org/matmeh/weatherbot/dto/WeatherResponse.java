package org.matmeh.weatherbot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
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
    public static List<String> WeatherForFiveDays(String city, StringBuffer response) {
        List<String> result = new ArrayList<>();

        try {
            JSONObject jsonObject = new JSONObject(response.toString());
            JSONArray list = jsonObject.getJSONArray("list");

            for (int i = 0; i < 5; i++) {
                StringBuilder resultBuilder = new StringBuilder();
                resultBuilder.append( city).append("\n\n");
                JSONObject dayForecast = list.getJSONObject(i * 8); // Получаем информацию о погоде для каждого 8-го элемента (это примерно раз в день)

                String dateTimeText = dayForecast.getString("dt_txt");
                String dateText = dateTimeText.substring(0, dateTimeText.indexOf(' '));
                JSONObject main = dayForecast.getJSONObject("main");
                double temp = main.getDouble("temp");
                double feelsLike = main.getDouble("feels_like");
                int pressure = (int) (main.getInt("pressure") * 0.75);
                int humidity = main.getInt("humidity");
                JSONArray weatherArray = dayForecast.getJSONArray("weather");
                JSONObject weatherData = weatherArray.getJSONObject(0);
                String weatherDescription = weatherData.getString("description");
                JSONObject wind = dayForecast.getJSONObject("wind");
                double windSpeed = wind.getDouble("speed");

                // Формирование строки с информацией о погоде
                resultBuilder.append("Дата: ").append(dateText).append("\n")
                        .append("Температура: ").append(temp).append("C\u00B0 (Ощущается, как ").append(feelsLike).append("C\u00B0)").append("\n")
                        .append(weatherDescription).append("\n")
                        .append("Атмосферное давление: ").append(pressure).append(" мм.рт.ст.").append("\n")
                        .append("Влажность воздуха: ").append(humidity).append("%").append("\n")
                        .append("Скорость ветра: ").append(windSpeed).append(" м/с");

                result.add(resultBuilder.toString());
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

}
