package org.matmeh.weatherbot.commands;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.ArrayList;
import java.util.List;

@Component
public class WeatherCommand extends BaseCommand {
    public static final String locationRequest = "Send location";
    public static final String cityRequest = "Enter city";

    @Override
    public String getCommandIdentifier() {
        return "weather";
    }

    @Override
    public String getDescription() {
        return "Узнать погоду";
    }

    @Override
    public BotApiMethodMessage answer(AbsSender bot, Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChat().getId());
        sendMessage.setText("Выбери, как ты хочешь определить город:");
        sendMessage.setReplyMarkup(createWeatherKeyboard());

        return sendMessage;
    }

    private static ReplyKeyboardMarkup createWeatherKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        KeyboardButton locationButton = new KeyboardButton();
        locationButton.setText(locationRequest);
        locationButton.setRequestLocation(true);

        KeyboardButton button = new KeyboardButton();
        button.setText(cityRequest);

        row.add("/start");
        row.add(button);
        row.add(locationButton);

        keyboard.add(row);
        replyKeyboardMarkup.setKeyboard(keyboard);

        return replyKeyboardMarkup;
    }
}
