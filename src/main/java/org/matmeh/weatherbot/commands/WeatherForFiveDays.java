package org.matmeh.weatherbot.commands;

import org.matmeh.weatherbot.service.UserChat;
import org.matmeh.weatherbot.service.WeatherService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
@Component
public class WeatherForFiveDays extends BaseCommand {
    private final UserChat userChat;
    private final WeatherService weatherService;

    public WeatherForFiveDays(UserChat userChat, WeatherService weatherService) {
        this.userChat = userChat;
        this.weatherService = weatherService;
    }


    @Override
    public String getCommandIdentifier() {
        return "weather_5days";
    }

    @Override
    public String getDescription() {
        return "Узнать погоду на 5 дней";
    }

    @Override
    public BotApiMethodMessage answer(AbsSender bot, Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChat().getId());
        Long userId = message.getFrom().getId();
        List<String> cities = userChat.getCitiesFromDatabase(userId);
        if (cities != null && !cities.isEmpty()) {
            for (String city : cities) {
                String cityWeather = weatherService.getWeatherForFiveDays(city);
                SendMessage cityWeatherMessage = new SendMessage();
                cityWeatherMessage.setChatId(message.getChatId());
                cityWeatherMessage.setText(cityWeather);
                try {
                    bot.execute(cityWeatherMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        } else {
            sendMessage.setText("У вас не создан профиль. Нужно добавить города добавьте города в /profile");
            return sendMessage;
        }
        return null; // Возвращаем null, так как сообщения для каждого города отправляются отдельно
    }
}
