package org.matmeh.weatherbot;

import lombok.SneakyThrows;
import org.matmeh.weatherbot.bot.WeatherBot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
public class WeatherBotApplication {
    @SneakyThrows
    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(WeatherBotApplication.class, args);

        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(ctx.getBean(WeatherBot.class));
    }
}
