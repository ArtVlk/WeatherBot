package org.matmeh.weatherbot.commands;

import org.matmeh.weatherbot.service.UserChat;
import org.matmeh.weatherbot.service.WeatherService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
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
    public  BotApiMethodMessage answer(AbsSender bot, Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChat().getId());
        Long userId = message.getFrom().getId();
        List<String> cities = userChat.getCitiesFromDatabase(userId);
        if (cities != null && !cities.isEmpty()) {
            List<SendMessage> weatherMessages = new ArrayList<>();
            for (String city : cities) {
                List<SendMessage> cityWeatherList = weatherService.getWeatherForFiveDays(city, message);
                if (cityWeatherList.isEmpty()) {
                    sendMessage.setText("Не удается получить прогноз погоды на 5 дней для ваших городов.\nПожалуйста, попробуйте еще раз.");
                    return sendMessage;
                }
                weatherMessages.addAll(cityWeatherList);
            }
            for (SendMessage cityWeather : weatherMessages) {
                try {
                    bot.execute(cityWeather); // Отправляем прогноз погоды для конкретного города в отдельном сообщении
                } catch (TelegramApiException e) {
                    e.printStackTrace(); // Обрабатываем возможные ошибки при отправке сообщений
                }
            }
            return null;
        } else {
            sendMessage.setText("У вас не создан профиль. Необходимо добавить города в /profile");
            return sendMessage;
        }
    }
}
