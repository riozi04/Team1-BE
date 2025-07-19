package com.example.weatherapp.controller;

import com.example.weatherapp.service.WeatherService;
import org.springframework.web.bind.annotation.*;
import org.json.JSONArray;
import org.json.JSONObject;

@RestController
@RequestMapping("/weather")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping
    public String getWeather(@RequestParam String city) {
        JSONObject weatherJson = weatherService.getWeatherByCity(city);
        JSONArray array = new JSONArray();
        array.put(weatherJson);
        return array.toString(); // JSON 배열로 반환
    }



}
