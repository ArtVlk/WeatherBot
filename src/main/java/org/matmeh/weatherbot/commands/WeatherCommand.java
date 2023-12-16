package org.matmeh.weatherbot.commands;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.ArrayList;
import java.util.List;

@Component
public class WeatherCommand extends BaseCommand {

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
        sendMessage.setText("Выбери, какую погоду тебе нужно");
        sendMessage.setReplyMarkup(createWeatherKeyboard());

        return sendMessage;
    }

    private static ReplyKeyboardMarkup createWeatherKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);

        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        row.add("/start");
        row.add("/weather_now");
        row.add("/weather_5days");

        keyboardRows.add(row);
        replyKeyboardMarkup.setKeyboard(keyboardRows);

        return replyKeyboardMarkup;
    }
}
