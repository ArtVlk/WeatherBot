package org.matmeh.weatherbot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class BotProperties {
    @Autowired
    private Environment env;

    public String getBotToken() {
        return env.getProperty("bot.token");
    }

    public String getBotName() {
        return env.getProperty("bot.name");
    }
    public String getAPIOpenWeather() {
        return env.getProperty("API.OpenWeather");
    }
    public String getAPIOpenCage() {
        return env.getProperty("API.OpenCage");
    }
}
