package org.matmeh.weatherbot.commands;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.matmeh.weatherbot.service.UserChat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
public class ProfileCommand extends BaseCommand {

    public static final String createRequest = "Create profile";
    public static final String changeCityRequest = "Change city";
    public static final String setTimeRequest = "Set time";


    @Autowired
    private UserChat userChat;


    @Override
    public String getCommandIdentifier() { return "profile"; }

    @Override
    public String getDescription() { return "Узнать данные пользователя"; }

    @Override
    public BotApiMethodMessage answer(AbsSender bot, Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChat().getId());
        Long userId = message.getFrom().getId();
        System.out.println(userChat.getCityList());
        if (!userChat.getCityList().isEmpty()) {
            sendMessage.setText("Информация профиля:" + "\n" +
                    "ID: " + userId + "\n" +
                    "Cохраненные города: " + userChat.getCityList());
            return sendMessage;
        }

        sendMessage.setText("У вас пока нет профиля. Для его создания используйте команду /create");
        sendMessage.setReplyMarkup(createProfileKeyboard());
        return sendMessage;
    }

    private static ReplyKeyboardMarkup createProfileKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        KeyboardButton createButton = new KeyboardButton();
        createButton.setText(createRequest);

        KeyboardButton changeButton = new KeyboardButton();
        changeButton.setText(changeCityRequest);

        KeyboardButton setTimeButton = new KeyboardButton();
        setTimeButton.setText(setTimeRequest);


        row.add("/start");
        row.add(changeButton);
        row.add(createButton);
        row.add(setTimeButton);

        keyboard.add(row);
        replyKeyboardMarkup.setKeyboard(keyboard);

        return replyKeyboardMarkup;
    }

}
