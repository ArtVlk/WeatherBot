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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Configuration
public class WeatherBot extends TelegramLongPollingCommandBot {
    private final BotProperties botProperties;
    private final WeatherService weatherService;
    private final UserChat userChat;

    private Map<String, Boolean> flags = new HashMap<>();

    @Autowired
    public WeatherBot(StartCommand startCommand, WeatherCommand weatherCommand,
                      ProfileCommand profileCommand, WeatherNowCommand weatherNowCommand,
                      WeatherForFiveDays weatherForFiveDays,
                      WeatherService weatherService, UserChat userChat, BotProperties botProperties) {
        this.botProperties = botProperties;
        this.weatherService = weatherService;
        this.userChat = userChat;
        this.flags.put("addCity", false);
        this.flags.put("getWeather", false);
        this.flags.put("remCity", false);
        this.flags.put("setTime", false);
        registerAll(startCommand, weatherCommand, profileCommand, weatherNowCommand, weatherForFiveDays);
    }

    @SneakyThrows
    @Override
    public void processNonCommandUpdate(@NotNull Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(message.getChatId());
            if (message.hasLocation()) {
                processLocationMessage(message, sendMessage);
            } else {
                processTextMessage(message, sendMessage);
            }
        }
    }

    private void processLocationMessage(Message message, SendMessage sendMessage) {
        double latitude = message.getLocation().getLatitude();
        double longitude = message.getLocation().getLongitude();

        if (flags.get("addCity")) {
            processAddCityRequest(message, sendMessage, latitude, longitude);
        } else {
            processWeatherRequest(latitude, longitude, sendMessage);
        }
    }
    @SneakyThrows
    private void processAddCityRequest(Message message, SendMessage sendMessage, double latitude, double longitude) {
        String city = weatherService.getWeatherAddCity(latitude, longitude);
        if (weatherService.doesCityExist(city)) {
            userChat.addCity(message.getFrom().getId(), city);
            userChat.addCityToDatabase(message.getFrom().getId(), city);
            sendMessage.setText("Город успешно сохранен");
            execute(sendMessage);
        } else {
            sendMessage.setText("Указанный город не найден");
            execute(sendMessage);
        }
        flags.put("addCity", false);
    }
    @SneakyThrows
    private void processWeatherRequest(double latitude, double longitude, SendMessage sendMessage) {
        String weather = weatherService.getWeather(latitude, longitude);
        sendMessage.setText(weather);
        execute(sendMessage);
    }
    @SneakyThrows
    private void processTextMessage(Message message, SendMessage sendMessage) {
        String text = message.getText() != null ? message.getText() : "";
        switch (text) {
            case WeatherCommand.cityRequest:
                if (!flags.get("addCity")) {
                    flags.put("getWeather", true);
                }
                sendMessage.setText("Пожалуйста, введите название города:");
                execute(sendMessage);
                break;
            case ProfileCommand.addCityRequest:
            case "/add":
                AddCityCommand addCityCommand = new AddCityCommand();
                SendMessage response = addCityCommand.answer(this, message);
                execute(response);
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
                    handleAddCity(message, sendMessage);
                } else if (flags.get("remCity")) {
                    handleRemoveCity(message, sendMessage);
                } else {
                    sendMessage.setText("Неверная команда");
                    execute(sendMessage);
                }
                break;
        }
    }
    @SneakyThrows
    private void handleAddCity(Message message, SendMessage sendMessage) {
        flags.put("addCity", false);
        if (weatherService.doesCityExist(message.getText())) {
            userChat.addCity(message.getFrom().getId(), message.getText());
            userChat.addCityToDatabase(message.getFrom().getId(), message.getText());
            sendMessage.setText("Город успешно сохранен");
            Long userId = message.getFrom().getId();
            List<String> cityList = userChat.getCitiesFromDatabase(userId);
            ReplyKeyboardMarkup replyKeyboardMarkup = ProfileCommand.createProfileAndCityKeyboard(cityList);
            sendMessage.setReplyMarkup(replyKeyboardMarkup);
            execute(sendMessage);
        } else {
            sendMessage.setText("Указанный город не найден");
            execute(sendMessage);
        }
    }
    @SneakyThrows
    private void handleRemoveCity(Message message, SendMessage sendMessage) {
        flags.put("remCity", false);
        if (weatherService.doesCityExist(message.getText())
                && userChat.doesCityExistForUser(message.getFrom().getId(), message.getText())) {
            userChat.removeCity(message.getFrom().getId(), message.getText());
            userChat.removeCityFromDatabase(message.getFrom().getId(), message.getText());
            sendMessage.setText("Город успешно удален");
            Long userId = message.getFrom().getId(); // обновление клавиатуры
            List<String> cityList = userChat.getCitiesFromDatabase(userId);
            ReplyKeyboardMarkup replyKeyboardMarkup = ProfileCommand.createProfileAndCityKeyboard(cityList);
            sendMessage.setReplyMarkup(replyKeyboardMarkup);
            execute(sendMessage);
        } else {
            sendMessage.setText("Указанный город не найден");
            execute(sendMessage);
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