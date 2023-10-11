package org.example;


import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {

    public static void main(String[] args) {

        TelegramBotsApi telegramBotsApi = null;

        try {
            telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(new WeatherBot());
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
/*
public class Main {
    public static void main(String[] args) {

        String botName = "Trainingfittinbot";
        String botToken = "6349838473:AAF-gJurw3Mgo1u37l0DyaoaOLIVB2znAtI";
        TelegramBotsApi telegramBotsApi = null;
        try {
            telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(new EchoJavaTelegramBot(botName, botToken));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);

        }
    }
}

 */