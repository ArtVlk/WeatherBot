package org.matmeh.weatherbot.commands;

import org.matmeh.weatherbot.service.UserChat;
import org.matmeh.weatherbot.service.WeatherService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.List;

@Component
public class WeatherNowCommand extends BaseCommand {
    private final UserChat userChat;
    private final WeatherService weatherService;

    public WeatherNowCommand(UserChat userChat, WeatherService weatherService) {
        this.userChat = userChat;
        this.weatherService = weatherService;
    }


    @Override
    public String getCommandIdentifier() {
        return "weather_now";
    }

    @Override
    public String getDescription() {
        return "Узнать погоду сейчас";
    }

    @Override
    public BotApiMethodMessage answer(AbsSender bot, Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChat().getId());
        Long userId = message.getFrom().getId();
        List<String> cities = userChat.getCitiesFromDatabase(userId);
        if (cities != null && !cities.isEmpty()) {
            StringBuilder weatherInfo = new StringBuilder("Прогноз погоды для ваших городов:\n\n");
            for (String city : cities){
                String cityWeather = weatherService.getWeather(city);
                weatherInfo.append(city).append(": \n").append(cityWeather).append("\n\n");
            }
            sendMessage.setText(weatherInfo.toString());
            return sendMessage;
        }
        sendMessage.setText("У вас не создан профиль. Нужно добавить города добавьте города в /profile");
        return sendMessage;
    }

}

