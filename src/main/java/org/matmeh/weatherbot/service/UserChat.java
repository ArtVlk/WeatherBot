package org.matmeh.weatherbot.service;

import lombok.Getter;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

import java.util.Map;
import java.util.HashMap;

@Service
@Getter
public class UserChat {
    private boolean state;
    Map<Long, String> cityMap;

    public UserChat(){
        this.cityMap = new HashMap<>();
    }


    public String getCityList() {
        StringBuilder cityList = new StringBuilder();
        for (String city : cityMap.values()) {
            cityList.append(city).append(", ");
        }

        if (cityList.length() > 0) {
            cityList.setLength(cityList.length() - 2);
        }

        return cityList.toString();
    }
    public void addCity(Long userId, String city) {
        cityMap.put(userId, city);
    }

}
