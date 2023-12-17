package org.matmeh.weatherbot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class BotProperties {
    @Autowired
    private final Environment env;

    public BotProperties(Environment env) {
        this.env = env;
    }

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
    public String getUrlBD() { return env.getProperty("url.BD");}

    public String getUserBD() { return env.getProperty("user.BD");}

    public String getPasswordBD() { return env.getProperty("password.BD");}
}
