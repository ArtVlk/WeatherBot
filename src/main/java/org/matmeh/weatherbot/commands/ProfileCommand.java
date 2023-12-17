package org.matmeh.weatherbot.commands;

import org.matmeh.weatherbot.service.UserChat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Component
public class ProfileCommand extends BaseCommand {
    public static final String addCityRequest = "Add city";
    public static final String remCityRequest = "Remove city";


    @Autowired
    private UserChat userChat;


    @Override
    public String getCommandIdentifier() { return "profile"; }

    @Override
    public String getDescription() { return "Узнать данные пользователя"; }

    @Override
    public BotApiMethodMessage answer(AbsSender bot, Message message) {
        Long userId = message.getChatId();
        List<String> cities = userChat.getCitiesFromDatabase(userId);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId());

        if (cities != null && !cities.isEmpty()) {
            sendMessage.setText("Информация профиля:" + "\n" +
                    "ID: " + userId + "\n" +
                    "Сохраненные города:");
            ReplyKeyboardMarkup replyKeyboardMarkup = createProfileAndCityKeyboard(cities);
            sendMessage.setReplyMarkup(replyKeyboardMarkup);

        } else {
            sendMessage.setText("У вас пока нет профиля. Добавьте города, нажав на кнопку Add city");
            ReplyKeyboardMarkup replyKeyboardMarkup = createProfileKeyboard();
            sendMessage.setReplyMarkup(replyKeyboardMarkup);
        }

        return sendMessage;
    }
    public static ReplyKeyboardMarkup createProfileAndCityKeyboard(List<String> cities) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        KeyboardButton addCityButton = new KeyboardButton();
        addCityButton.setText(addCityRequest);

        KeyboardButton remCityButton = new KeyboardButton();
        remCityButton.setText(remCityRequest);

        row.add("/start");
        row.add(addCityButton);
        row.add(remCityButton);

        keyboard.add(row);

        Set<String> set = new HashSet<>();
        for (String city : cities) {
            set.add(city);
        }
        for (String city : set) {
            row = new KeyboardRow();
            KeyboardButton cityButton = new KeyboardButton(city);
            row.add(cityButton);
            keyboard.add(row);
        }

        replyKeyboardMarkup.setKeyboard(keyboard);

        return replyKeyboardMarkup;
    }
    private static ReplyKeyboardMarkup createProfileKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        KeyboardButton addCityButton = new KeyboardButton();
        addCityButton.setText(addCityRequest);

        KeyboardButton remCityButton = new KeyboardButton();
        remCityButton.setText(remCityRequest);


        row.add("/start");
        row.add(addCityButton);
        row.add(remCityButton);

        keyboard.add(row);
        replyKeyboardMarkup.setKeyboard(keyboard);

        return replyKeyboardMarkup;
    }

}
