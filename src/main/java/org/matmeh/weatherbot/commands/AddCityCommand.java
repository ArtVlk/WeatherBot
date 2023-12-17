package org.matmeh.weatherbot.commands;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.ArrayList;
import java.util.List;

@Component
public class AddCityCommand extends BaseCommand {

    public static final String locationRequest = "Send location";
    public static final String cityRequest = "Enter city";

    @Override
    public String getCommandIdentifier() {
        return "add";
    }

    @Override
    public String getDescription() {
        return "Add a new city to your profile";
    }

    @Override
    public SendMessage answer(AbsSender bot, Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChat().getId());
        sendMessage.setText("Выберите способ добавления нового города:");
        sendMessage.setReplyMarkup(createKeyboard());

        return sendMessage;
    }

    private static ReplyKeyboardMarkup createKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        KeyboardButton locationButton = new KeyboardButton();
        locationButton.setText(locationRequest);
        locationButton.setRequestLocation(true);

        KeyboardButton cityButton = new KeyboardButton();
        cityButton.setText(cityRequest);

        row.add("/start");
        row.add(cityButton);
        row.add(locationButton);

        keyboard.add(row);
        replyKeyboardMarkup.setKeyboard(keyboard);
        replyKeyboardMarkup.setOneTimeKeyboard(true);

        return replyKeyboardMarkup;
    }
}