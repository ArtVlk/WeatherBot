package org.matmeh.weatherbot.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CityResponse {
    private List<HashMap<String, JsonNode>> results;

    public String getCity() {
        return results.get(0).get("components").get("city").asText();
    }
}
