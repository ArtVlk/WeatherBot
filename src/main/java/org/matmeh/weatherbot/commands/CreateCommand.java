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
public class CreateCommand extends BaseCommand {

    public static final String locationRequest = "Send location";
    public static final String cityRequest = "Enter city";

    @Override
    public String getCommandIdentifier() {
        return "create";
    }

    @Override
    public String getDescription() {
        return "Ввести данные для профиля пользователя";
    }


    @Override
    public SendMessage answer(AbsSender bot, Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChat().getId());
        sendMessage.setText("Выберите способ ввода города:");
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

        KeyboardButton button = new KeyboardButton();
        button.setText(cityRequest);

        row.add(button);
        row.add(locationButton);

        keyboard.add(row);
        replyKeyboardMarkup.setKeyboard(keyboard);
        replyKeyboardMarkup.setOneTimeKeyboard(true);  // Показывать клавиатуру только один раз

        return replyKeyboardMarkup;
    }
}
