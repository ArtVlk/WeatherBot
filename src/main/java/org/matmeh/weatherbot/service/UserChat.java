package org.matmeh.weatherbot.service;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.time.LocalTime;
import java.util.*;

@Service
@Getter
@Component
public class UserChat {
    private final WeatherService weatherService;
    private final UserChat userChat;
    public Map<Long, List<String>> cityMap;
    HashMap<String, Boolean> enterCommand = new HashMap<>();

    private final String url = "your url";

    private final String user = "your name";

    private final String password = "your password";

    @Autowired
    public UserChat(WeatherService weatherService) {
        this.weatherService = weatherService;
        this.userChat = this;
        this.cityMap = new HashMap<>();
        this.enterCommand.put("addCityCommand", false);
        this.enterCommand.put("getWeatherCommand", false);
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

    public void setUserTimeForCity(Long userId, String city, LocalTime time) {
        int timeAsInt = convertTimeToInt(time);
        try (Connection conn = getConnection()) {
            String sql = "UPDATE new_table SET time = ? WHERE id = ? AND city = ?";
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setInt(1, timeAsInt);
                statement.setLong(2, userId);
                statement.setString(3, city);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    public void removeUserTime(Long userId) {
        try (Connection conn = getConnection()) {
            String sql = "UPDATE new_table SET time = NULL WHERE id = ?";
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setLong(1, userId);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    private void handleSQLException(SQLException e) {
        // Log the exception or throw a custom exception
        e.printStackTrace();
        // You might also want to throw a custom exception or notify the user
    }

    private int convertTimeToInt(LocalTime time) {
        int hours = time.getHour();
        int minutes = time.getMinute();
        return hours * 100 + minutes; // Преобразование в формат "HHMM"
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
            return resultSet.next(); // If the result set has a next entry, the city exists for the user
        } catch (SQLException e) {
            // Handle the SQL exception
            e.printStackTrace();
            return false; // Return false in case of an exception
        }
    }
}
