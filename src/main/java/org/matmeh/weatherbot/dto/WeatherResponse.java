package org.matmeh.weatherbot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
