package org.matmeh.weatherbot.bot;

import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.matmeh.weatherbot.commands.StartCommand;
import org.matmeh.weatherbot.commands.WeatherCommand;
import org.matmeh.weatherbot.service.WeatherService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import static org.matmeh.weatherbot.commands.WeatherCommand.cityRequest;
import static org.matmeh.weatherbot.commands.WeatherCommand.locationRequest;

@Component
public class WeatherBot extends TelegramLongPollingCommandBot {
    private final WeatherService weatherService;
    public WeatherBot(StartCommand startCommand, WeatherCommand weatherCommand, WeatherService weatherService){
        super("BotToken");
        this.weatherService = weatherService;

        registerAll(startCommand, weatherCommand);
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
                execute(sendMessage);
                return;
            }

            if (!message.hasText()) return;
            if (message.getText().equals(cityRequest)) {
                sendMessage.setText("Пожалуйста, введите название города:");
                execute(sendMessage);
                return;
            }

            sendMessage.setText(weatherService.getWeather(message.getText()));
            execute(sendMessage);
        } else if (update.hasCallbackQuery()) {
            sendMessage.setChatId(update.getCallbackQuery().getMessage().getChatId());
            String callbackText = update.getCallbackQuery().getData();

            if (cityRequest.equals(callbackText)) {
                sendMessage.setText("Пожалуйста, введите название города:");
                execute(sendMessage);
                return;
            }
            if (locationRequest.equals(callbackText)) {
                sendMessage.setText("Пожалуйста, отправьте свое местоположение:");
                execute(sendMessage);
            }
        }
    }

    @Override
    public String getBotUsername() {
        return "botName";
    }
}
