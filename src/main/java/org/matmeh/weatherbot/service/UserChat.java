package org.matmeh.weatherbot.service;

import lombok.Getter;
import org.matmeh.weatherbot.BotProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;

@Service
@Getter
@Component
public class UserChat {
    private final BotProperties botProperties;
    private final WeatherService weatherService;
    private final UserChat userChat;
    public Map<Long, List<String>> cityMap;
    HashMap<String, Boolean> enterCommand = new HashMap<>();

    private final String url;

    private final String user;

    private final String password;

    @Autowired
    public UserChat(WeatherService weatherService, BotProperties botProperties) {
        this.weatherService = weatherService;
        this.botProperties = botProperties;
        this.userChat = this;
        this.cityMap = new HashMap<>();
        this.enterCommand.put("addCityCommand", false);
        this.enterCommand.put("getWeatherCommand", false);
        url = botProperties.getUrlBD();
        user = botProperties.getUserBD();
        password = botProperties.getPasswordBD();
    }



    public List<Long> getAllUserIds() {
        return new ArrayList<>(cityMap.keySet()); // Возвращаем список всех ключей (идентификаторов пользователей)
    }

    public List<String> getCitiesFromDatabase(Long userId) {
        List<String> cities = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(url, user, password)) {

            String sql = "SELECT city FROM new_table WHERE id = ? ORDER BY city";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setLong(1, userId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                cities.add(resultSet.getString("city"));
            }
        } catch (SQLException e) {
            // Обработка ошибок подключения или выполнения запроса
            e.printStackTrace();
        }
        return cities;
    }


    public void addCityToDatabase(Long userId, String city) {
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            // Проверка существующего города для пользователя с данным id
            String selectSql = "SELECT city FROM new_table WHERE id = ? AND city = ?";
            PreparedStatement selectStatement = conn.prepareStatement(selectSql);
            selectStatement.setLong(1, userId);
            selectStatement.setString(2, city);
            ResultSet resultSet = selectStatement.executeQuery();

            if (!resultSet.next()) {
                // Город не существует для данного пользователя, добавляем с заглавной буквы
                city = city.substring(0, 1).toUpperCase() + city.substring(1);
                String insertSql = "INSERT INTO new_table (id, city) VALUES (?, ?) " +
                        "ON DUPLICATE KEY UPDATE id=VALUES(id), city=VALUES(city)";
                PreparedStatement insertStatement = conn.prepareStatement(insertSql);
                insertStatement.setLong(1, userId);
                insertStatement.setString(2, city);
                insertStatement.executeUpdate();
            }
        } catch (SQLException e) {
            // Обработка ошибки подключения или выполнения запроса
            e.printStackTrace();
        }
    }

    public void removeCityFromDatabase(Long userId, String city) {
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            String sql = "DELETE FROM new_table WHERE id = ? AND city = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setLong(1, userId);
            statement.setString(2, city);
            statement.executeUpdate();
        } catch (SQLException e) {
            // Обработка ошибки подключения или выполнения запроса
            e.printStackTrace();
        }
    }


    public String getCityList(Long userId) {
        List<String> cities = cityMap.get(userId);

        StringBuilder cityList = new StringBuilder();
        for (String city : cities) {
            cityList.append(city).append(", ");
        }

        cityList.setLength(cityList.length() - 2); // Удалить завершающую запятую и прел
        return cityList.toString();
    }
    public void addCity(Long userId, String city) {
        List<String> cities = cityMap.computeIfAbsent(userId, k -> new ArrayList<>());
        cities.add(city);
        cities.sort(Comparator.naturalOrder()); // Сортировка городов в естественном порядке
    }

    public void removeCity(Long userId, String city) {
        List<String> cities = cityMap.get(userId);
        if (cities != null) {
            cities.remove(city);
        }
    }

    public boolean hasCity(Long userId){
        List<String> cities = cityMap.get(userId);
        System.out.println(cities != null);
        return (cities != null);
    }

    public boolean doesCityExistForUser(Long userId, String city) {
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            String sql = "SELECT city FROM new_table WHERE id = ? AND city = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setLong(1, userId);
            statement.setString(2, city);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
