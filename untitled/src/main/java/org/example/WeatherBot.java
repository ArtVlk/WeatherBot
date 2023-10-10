package org.example;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;

import java.io.IOException;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WeatherBot extends TelegramLongPollingBot {
    private static final Map<String, UserState> userStates = new ConcurrentHashMap<>();
    private static final String OPENCAGE_API_KEY = "13518bc35d664d7da39fd63e71dc2550"; // API OpenCage Geocoding
    private enum UserState {
        EXPECTING_CITY
    }
    // Этот код используется для хранения состояний пользователей (может быть чат-бот или другая программа),
// где каждый пользователь идентифицируется по своему уникальному ID (строка),
// а состояние указывает, что ожидается некоторое событие или значение, связанное с городом.
// P.S. для умных (Этот код является частью Java-программы. В нём создаётся объект userStates (ассоциативный массив или словарь) для хранения пар ключ-значение.
// В данном случае ключами являются строки (String), а значениями — элементы перечисления UserState.
// Перечисление UserState содержит только одно значение — EXPECTING_CITY.
// Перечисления в Java обычно используются, когда набору значений нужно задать определенный набор возможных состояний.
// В данном коде создаётся карта (map) с потокобезопасным классом ConcurrentHashMap, что позволяет правильно,
// без ошибок и искажений изменять и получать данные из карты даже при работе с ней нескольких параллельных потоков.
// Это может быть полезно, если ваша программа предполагает многопоточность. )
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            String chatId = update.getMessage().getChatId().toString(); //Получение ID чата из сообщения для идентификации пользователя.
            if (update.getMessage().hasLocation()) { //Проверка, содержит ли сообщение геокоординаты.
                double latitude = update.getMessage().getLocation().getLatitude(); //Получение широты из геолокации в сообщении.
                double longitude = update.getMessage().getLocation().getLongitude(); //Получение долготы из геолокации в сообщении.
                String city = getCityFromLocation(latitude, longitude, chatId); //Получение названия города по геокоординатам с помощью функции getCityFromLocation().
                String weather = getWeather(city);
                sendMsg(chatId, weather);
            } else if (update.getMessage().hasText()) { // Если сообщение не содержит геокоординат, тогда проверка на наличие текста в сообщении.
                String messageText = update.getMessage().getText();
                if (messageText.startsWith("/start")) { //Если текст начинается с "/start", отправка приветственного сообщения на основе имени пользователя.
                    String name = update.getMessage().getChat().getFirstName();
                    sendMsg(chatId, "Привет, " + name + ", если хочешь узнать температуру введи /weather");
                } else if (messageText.startsWith("/weather")) { //Если текст начинается с "/weather", отправка просьбы о выборе способа определения города по кнопке
                    sendMsgWithKeyboard(chatId, "Выберите, как вы хотите определить город:", createWeatherKeyboard());
                } else if (messageText.equalsIgnoreCase("Enter city")) { //Если текст равен "Enter city", переключение состояния пользователя на ожидание названия города
                    // и подсказка о вводе названия города.
                    userStates.put(chatId, UserState.EXPECTING_CITY);
                    sendMsg(chatId, "Пожалуйста, введите название города:");
                } else if (userStates.get(chatId) == UserState.EXPECTING_CITY) { //Если текущее состояние пользователя - ожидание названия города,
                    // получение погоды для введенного имя города и отправление информации о погоде.
                    String weather = getWeather(messageText);
                    sendMsg(chatId, weather);
                    userStates.remove(chatId);
                }
            }
        } else if (update.hasCallbackQuery()) { //Если обновление содержит CallbackQuery (Запрос обратного вызова)
            // (это специальные обратные запросы основанные на взаимодействиях пользователя с инлайн клавиатурой).
            // Строки обрабатывают CallbackQuery, получают информацию о колбэке и ID чата.
            String callData = update.getCallbackQuery().getData(); //Эта строка кода получает данные (строку),
            // которые были отправлены в ответ на нажатие пользователя на кнопку InlineKeyboardButton в Telegram.
            // CallbackQuery - это объект, который Telegram Bot API отправляет в ответ на нажатие пользователем кнопки с обратным вызовом.
            // Метод getData() этого объекта возвращает данные (обычно представленные в виде строки), которые были добавлены к кнопке при ее создании.
            String chatId = update.getCallbackQuery().getMessage().getChatId().toString(); //Эта строка кода получает идентификатор чата, из которого был отправлен запрос обратного вызова.
            // Это может быть личный чат между пользователем и ботом, групповой чат или канал.
            // Получение идентификатора чата может быть полезно для ответа на запрос обратного вызова в правильном контексте.
            // Метод getChatId() возвращает Long, который затем преобразовывается в строку с помощью метода toString().
            //
            // В общем, эти строки кода используются для обработки ответов на кнопки InlineKeyboardButton в Telegram Bot API,
            // получения данных, связанных с ответом, и определения контекста этого ответа.
            if (callData.equals("input_city")) { //Если данные колбэка равны "input_city", переключение состояния пользователя на ожидание названия города
                // и подсказка о вводе названия города.
                userStates.put(chatId, UserState.EXPECTING_CITY); //переключение состояния пользователя на ожидание названия города
                sendMsg(chatId, "Пожалуйста, введите название города:");
            } else if (callData.equals("send_location")) { //Если данные колбэка равны "send_location", отправка запроса к пользователю на предоставление геолокации.
                sendMsg(chatId, "Пожалуйста, отправьте свое местоположение:");
            }
        }

    }

    public String getCityFromLocation(double latitude, double longitude, String chatId) {
        HttpClient httpClient = HttpClients.createDefault(); //Создание объекта класса HttpClient для отправки HTTP-запросов.
        HttpGet request = new HttpGet("https://api.opencagedata.com/geocode/v1/json?q=" + latitude + "," + longitude +
                "&key=" + OPENCAGE_API_KEY+ "&language=ru"); //Создание HTTP GET запроса к API OpenCage, где указаны координаты (широта и долгота) и ключ API.
        try { //Начало блока исключения.
            HttpResponse response = httpClient.execute(request); //Выполнение HTTP-запроса к API и сохранение ответа.
            String responseBody = EntityUtils.toString(response.getEntity()); //Преобразование ответа из HTTP-запроса в строку.
            JSONObject json = new JSONObject(responseBody); //Преобразование строки ответа в JSON-объект.
            JSONArray results = json.getJSONArray("results"); //Извлечение массива "results" из JSON-объекта.
            JSONObject firstResult = results.getJSONObject(0); //Извлечение первого объекта из массива "results".
            JSONObject components = firstResult.getJSONObject("components"); //Извлечение объекта "components" из первого результата.
            String city = null;
            // Извлекаем город из списка компонентов
            if(components.has("city")){ //Проверка, содержит ли объект "components" поле "city".
                city = components.getString("city"); //Если да, то извлекаем значение этого поля и сохраняем в переменную "city".
            // Аналогичные действия производятся и для полей "town" и "village". Если поле присутствует, значением переменной "city" станет значение этого поля.
            } else if(components.has("town")) {
                city = components.getString("town");
            } else if(components.has("village")) {
                city = components.getString("village");
            }
            if (city == null) {
                userStates.put(chatId, UserState.EXPECTING_CITY); // Меняем состояние пользователя на 'EXPECTING_CITY' - ожидаем ввода города.
                sendMsg(chatId, "Город по указанным координатам не найден. Пожалуйста, введите название города:");
            } else {
                return city;
            }
        } catch (IOException e) {
            e.printStackTrace(); //Код обработки исключений. Если при выполнении запроса или обработке ответа возникла ошибка, выводим информацию об этой ошибке.
        }

        return null;
    }

    private String getWeather(String city) {

        // Здесь нужно добавить ваш API ключ OpenWeatherMap
        String apiKey = "68c759cf1f7c764454e6af8aa88b049c";

        String url = "https://api.openweathermap.org/data/2.5/weather?q="
                + city + "&appid=" + apiKey + "&units=metric&lang=ru";

        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection(); //Открытие HTTP-соединения с помощью созданного объекта URL
            con.setRequestMethod("GET"); //Установка HTTP-метода на GET

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream())); //Создание объекта BufferedReader для чтения ответа от сервера
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            } //Чтение строки за строкой из потока ответа сервера и их последовательное добавление к объекту response (типа StringBuffer)
            in.close(); //Закрытие объекта BufferedReader

            JSONObject jsonObject = new JSONObject(response.toString()); //Конвертация ответа сервера, сохраненного в response, из строки в JSON-объект
            JSONObject main = jsonObject.getJSONObject("main"); // Эта строка кода извлекает JSON-объект, связанный с ключом "main", из объекта jsonObject и сохраняет его в переменной main
            double temp = main.getDouble("temp");
            double feelsLike = main.getDouble("feels_like");
            int pressure = (main.getInt("pressure"));
            pressure= (int) (pressure*(0.75));
            int humidity = main.getInt("humidity");

            JSONArray weatherArray = jsonObject.getJSONArray("weather");
            JSONObject weatherData = weatherArray.getJSONObject(0);
            String weatherDescription = weatherData.getString("description");

            JSONObject wind = jsonObject.getJSONObject("wind");
            double windSpeed = wind.getDouble("speed");
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
            String formattedNow = now.format(formatter);

            return "Погода в " + city + "е на " + formattedNow + "\n"
                    +"Температура: " + temp + "C° (Ощущается, как " + feelsLike + "C°)" + "\n"
                    + weatherDescription + "\n"
                    + "Атмосферное давление: " + pressure + " мм.рт.ст.\n"
                    + "Влажность воздуха: " + humidity + "%\n"
                    + "Скорость ветра: " + windSpeed + " м/с";
        } catch (Exception e) {
            return "Не удается получить прогноз погоды для " + city + ". Пожалуйста, попробуйте еще раз.";
        }
    }

    private KeyboardButton locationButton;

    private ReplyKeyboardMarkup createWeatherKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        // Создаем кнопку с запросом местоположения
        locationButton = new KeyboardButton();
        locationButton.setText("Send location");
        locationButton.setRequestLocation(true);

        // Создаем обычную кнопку
        KeyboardButton button1 = new KeyboardButton();
        button1.setText("Enter city");

        // Добавляем кнопки в строку клавиатуры
        row.add(button1);
        row.add(locationButton);

        // Добавляем строку в клавиатуру
        keyboard.add(row);

        // Устанавливаем клавиатуру
        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }


    private void sendMsgWithKeyboard(String chatId, String message, ReplyKeyboardMarkup keyboard) {
        SendMessage Smessage = new SendMessage();
        Smessage.setChatId(chatId);
        Smessage.setText(message);
        Smessage.setReplyMarkup(keyboard); // устанавливаем клавиатуру

        try {
            execute(Smessage);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private static ReplyKeyboardMarkup createKeyboard() { //кнопки
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setOneTimeKeyboard(false); // Установка свойства "Одноразовая клавиатура" (one-time keyboard) в false:
        // Это свойство определяет, будет ли клавиатура скрыта после использования одной из ее кнопок.
        // Значение false означает, что клавиатура будет отображаться постоянно.
        replyKeyboardMarkup.setResizeKeyboard(true); //Установка свойства "Изменение размера клавиатуры" (resize keyboard) в true:
        // Это свойство определяет, будет ли клавиатура автоматически подгоняться под размер панели ввода текста пользователя.
        // Значение true позволяет клавиатуре автоматически изменять размер.
        replyKeyboardMarkup.setSelective(true); //Установка свойства "Выборочная клавиатура" (selective) в true:
        //Это свойство определяет, кому будет доступна клавиатура.
        // Если установлено значение true, клавиатура будет отображаться только определенным пользователям, например тем, кто упомянут в сообщении.

        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        // Добавляем команды в строку клавиатуры
        row.add("/start");
        row.add("/weather");
        // Добавляем строку кнопок в список клавиатур
        keyboardRows.add(row);

        // Устанавливаем клавиатуру для объекта ReplyKeyboardMarkup
        replyKeyboardMarkup.setKeyboard(keyboardRows);

        return replyKeyboardMarkup;
    }
    private void sendMsg(String chatId, String message) {
        SendMessage Smessage = new SendMessage(); // Создаем обект-сообщение
        Smessage.setChatId(chatId); //Передаем чат ид
        Smessage.setText(message);
        Smessage.setReplyMarkup(createKeyboard());

        try {
            execute(Smessage);
        } catch (Exception e) { //Затем начинается блок catch, который перехватывает любые исключения, которые могут возникнуть в блоке try.
            throw new RuntimeException(e); //Если произошло исключение, то оно "пакуется" в новое исключение типа RuntimeException и затем "бросается" дальше.
            // Таким образом, если произошла ошибка при выполнении execute(Smessage), ошибка передаётся выше по стеку вызовов,
            // позволяя управляющему коду принять решение о том, как её обработать.
        }
    }

    @Override
    public String getBotUsername() {

        return "Trainingfittinbot";
    }

    @Override
    public String getBotToken() {

        return "6349838473:AAF-gJurw3Mgo1u37l0DyaoaOLIVB2znAtI";
    }
}