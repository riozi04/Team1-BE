package com.example.weatherapp.service;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class WeatherService {

    private final OkHttpClient client = new OkHttpClient();
    private final String API_KEY = "699129953b73feb27b045d654727e75f"; // 🔑 OpenWeatherMap API Key

    public JSONObject getWeatherByCity(String city) {
        JSONObject result = new JSONObject();

        try {
            // 1. 도시 이름 → 위도/경도 변환
            String geoUrl = "http://api.openweathermap.org/geo/1.0/direct?q=" + city + "&limit=1&appid=" + API_KEY;
            Request geoRequest = new Request.Builder().url(geoUrl).build();
            Response geoResponse = client.newCall(geoRequest).execute();

            JSONArray geoArray = new JSONArray(geoResponse.body().string());
            if (geoArray.length() == 0) {
                result.put("error", "❌ 도시를 찾을 수 없습니다.");
                return result;
            }

            JSONObject location = geoArray.getJSONObject(0);
            double lat = location.getDouble("lat");
            double lon = location.getDouble("lon");

            // 2. 위도/경도로 날씨 정보 가져오기
            String weatherUrl = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat +
                    "&lon=" + lon + "&appid=" + API_KEY + "&units=metric&lang=kr";
            Request weatherRequest = new Request.Builder().url(weatherUrl).build();
            Response weatherResponse = client.newCall(weatherRequest).execute();

            JSONObject weatherJson = new JSONObject(weatherResponse.body().string());
            JSONObject main = weatherJson.getJSONObject("main");
            JSONObject wind = weatherJson.getJSONObject("wind");
            String description = weatherJson.getJSONArray("weather").getJSONObject(0).getString("description");

            // 3. 결과 JSON 구성
            result.put("지역", city);
            result.put("온도", main.getDouble("temp"));
            result.put("체감온도", main.getDouble("feels_like"));
            result.put("날씨", description);
            result.put("풍속", wind.getDouble("speed"));

        } catch (Exception e) {
            result.put("error", "🚨 오류 발생: " + e.getMessage());
        }

        return result;
    }
}
