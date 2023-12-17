package org.matmeh.weatherbot.bot;

import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.matmeh.weatherbot.BotProperties;
import org.matmeh.weatherbot.commands.*;
import org.matmeh.weatherbot.service.UserChat;
import org.matmeh.weatherbot.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.Map;

@Component
@Configuration
public class WeatherBot extends TelegramLongPollingCommandBot {
    private final WeatherService weatherService;
    private final UserChat userChat;
    private final BotProperties botProperties;
    Map<String, Boolean> flags = new HashMap<>();

    @Autowired
    public WeatherBot(StartCommand startCommand, WeatherCommand weatherCommand,
                      ProfileCommand profileCommand, WeatherNowCommand weatherNowCommand,
                      WeatherForFiveDays weatherForFiveDays,
                      WeatherService weatherService, UserChat userChat, BotProperties botProperties) {
        this.weatherService = weatherService;
        this.userChat = userChat;
        registerAll(startCommand, weatherCommand, profileCommand, weatherNowCommand, weatherForFiveDays);
        this.botProperties = botProperties;
        flags.put("addCity", false);
        flags.put("getWeather", false);
        flags.put("remCity", false);
        flags.put("setTime", false);
    }

    @SneakyThrows
    @Override
    public void processNonCommandUpdate(@NotNull Update update) {
        SendMessage sendMessage = new SendMessage();
        if (update.hasMessage()) {
            Message message = update.getMessage();
            sendMessage.setChatId(message.getChatId());
            if (message.hasLocation()) {
                double latitude = message.getLocation().getLatitude();
                double longitude = message.getLocation().getLongitude();

                if (flags.get("addCity")) {
                    String city = weatherService.getWeatherAddCity(latitude, longitude);

                    if (weatherService.doesCityExist(city)) {
                        userChat.addCity(message.getFrom().getId(), city);
                        sendMessage.setText("Город успешно сохранен");
                        execute(sendMessage);
                    } else {
                        sendMessage.setText("Указанный город не найден");
                        execute(sendMessage);
                    }

                    flags.put("addCity", false);
                } else {
                    String weather = weatherService.getWeather(latitude, longitude);
                    sendMessage.setText(weather);
                    execute(sendMessage);
                }
            }
            else {
                switch (message.getText() != null ? message.getText() : "") {
                    case WeatherCommand.cityRequest:
                        if (!flags.get("getWeather")) {
                            flags.put("getWeather", true);
                        }
                        sendMessage.setText("Пожалуйста, введите название города:");
                        execute(sendMessage);
                        break;
                    case ProfileCommand.addCityRequest:
                    case "/add":
                        sendMessage.setText("Пожалуйста, введите название города для добавления:");
                        execute(sendMessage);
                        flags.put("addCity", true);
                        break;
                    case ProfileCommand.remCityRequest:
                    case "/remove":
                        sendMessage.setText("Пожалуйста, введите название города, который хотите удалить");
                        execute(sendMessage);
                        flags.put("remCity", true);
                        break;

                    default:
                        if (flags.get("getWeather")) {
                            flags.put("getWeather", false);
                            sendMessage.setText(weatherService.getWeather(message.getText()));
                            execute(sendMessage);
                        } else if (flags.get("addCity")) {
                            flags.put("addCity", false);
                            if (weatherService.doesCityExist(message.getText())) {
                                userChat.addCity(message.getFrom().getId(), message.getText());
                                userChat.addCityToDatabase(message.getFrom().getId(), message.getText());
                                sendMessage.setText("Город успешно сохранен");
                                execute(sendMessage);
                            } else {
                                sendMessage.setText("Указанный город не найден");
                                execute(sendMessage);
                            }
                        } else if (flags.get("remCity")) {
                            flags.put("remCity", false);
                            if (weatherService.doesCityExist(message.getText())
                                    && userChat.doesCityExistForUser(message.getFrom().getId(), message.getText())) {
                                userChat.removeCity(message.getFrom().getId(), message.getText());
                                userChat.removeCityFromDatabase(message.getFrom().getId(), message.getText());
                                sendMessage.setText("Город успешно удален");
                                execute(sendMessage);
                            } else {
                                sendMessage.setText("Указанный город не найден");
                                execute(sendMessage);
                            }
                            break;
                        }
                        break;
                }
            }

        }
    }


    @Override
    public String getBotUsername() {
        return botProperties.getBotName();
    }

    @Override
    public String getBotToken() {
        return botProperties.getBotToken();
    }
}
