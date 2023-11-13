package org.matmeh.weatherbot.bot;

import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.matmeh.weatherbot.BotProperties;
import org.matmeh.weatherbot.commands.*;
import org.matmeh.weatherbot.service.WeatherService;
import org.matmeh.weatherbot.service.UserChat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import static org.matmeh.weatherbot.commands.WeatherCommand.cityRequest;
import static org.matmeh.weatherbot.commands.WeatherCommand.locationRequest;
import static org.matmeh.weatherbot.commands.ProfileCommand.createRequest;

@Component
@Configuration
public class WeatherBot extends TelegramLongPollingCommandBot {
    private final WeatherService weatherService;
    private final ProfileCommand profileCommand;
    private final UserChat userChat;
    private final BotProperties botProperties;
    private boolean addCity = false;
    private boolean getWeather = false;
    @Value("${bot.token}")
    private String botToken;

    @Autowired
    public WeatherBot(StartCommand startCommand, WeatherCommand weatherCommand, ProfileCommand profileCommand, WeatherService weatherService, UserChat userChat, BotProperties botProperties) {
        this.weatherService = weatherService;
        this.profileCommand = profileCommand;
        this.userChat = userChat;
        registerAll(startCommand, weatherCommand, profileCommand);
        this.botProperties = botProperties;
    }
    @SneakyThrows
    @Override
    public void processNonCommandUpdate(@NotNull Update update) {
        SendMessage sendMessage = new SendMessage();
        if (update.hasMessage()) {
            Message message = update.getMessage();
            sendMessage.setChatId(message.getChatId());

            if (message.hasLocation()) {
                sendMessage.setText(weatherService.getWeather(
                        message.getLocation().getLatitude(),
                        message.getLocation().getLongitude()
                ));
                sendMessage.setText(weatherService.getWeather(message.getText()));
                execute(sendMessage);
                return;
            }

            if (!message.hasText()) return;

            if (message.getText().equals(cityRequest)) {
                sendMessage.setText("Пожалуйста, введите название города:");
                getWeather = true;
                execute(sendMessage);
                return;

            }

            else if (message.getText().equals(createRequest) || message.getText().equals("/create")){

                sendMessage.setText("Пожалуйста, введите название города для регистрации:");
                execute(sendMessage);
                addCity = true;
                return;
            }

            if (getWeather){
                getWeather = false;
                sendMessage.setText(weatherService.getWeather(message.getText()));
                execute(sendMessage);
                return;
            }

            else if (addCity){
                addCity = false;
                if (weatherService.doesCityExist(message.getText())){
                    userChat.addCity(message.getFrom().getId(), message.getText());
                    sendMessage.setText("Город успешно сохранен");
                    execute(sendMessage);
                    return;
                }
                sendMessage.setText("Указанный город не найден");
                execute(sendMessage);
                return;
            }

        }
        else if (update.hasCallbackQuery()) {
            sendMessage.setChatId(update.getCallbackQuery().getMessage().getChatId());
            String callbackText = update.getCallbackQuery().getData();

            if (createRequest.equals(callbackText)){
                sendMessage.setText("Пожалуйста, введите название города для регистрации:");
                execute(sendMessage);
                return;
            }

            else if (cityRequest.equals(callbackText)) {
                sendMessage.setText("Пожалуйста, введите название города:");
                execute(sendMessage);
                return;
            }
            else if (locationRequest.equals(callbackText)) {
                sendMessage.setText("Пожалуйста, отправьте свое местоположение:");
                execute(sendMessage);
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
