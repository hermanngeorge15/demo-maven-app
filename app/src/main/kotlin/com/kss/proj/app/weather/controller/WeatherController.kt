package com.kss.proj.app.weather.controller

import com.kss.proj.app.weather.model.TemperatureUnit
import com.kss.proj.app.weather.model.WeatherAlert
import com.kss.proj.app.weather.model.WeatherResponse
import com.kss.proj.app.weather.service.WeatherService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/weather")
class WeatherController(
    private val weatherService: WeatherService,
) {
    // Long method with many responsibilities
    @GetMapping("/{city}")
    fun getWeather(
        @PathVariable city: String,
        @RequestParam(defaultValue = "CELSIUS") unit: String,
        @RequestParam(defaultValue = "false") includeForecast: Boolean,
        @RequestParam(defaultValue = "5") days: Int,
    ): ResponseEntity<WeatherResponse> {
        // Validate city - magic number 2
        if (city.length < 2) {
            return ResponseEntity.badRequest().body(
                WeatherResponse(false, null, null, emptyList(), "City name too short"),
            )
        }

        // Validate days - magic numbers
        if (days < 1 || days > 14) {
            return ResponseEntity.badRequest().body(
                WeatherResponse(false, null, null, emptyList(), "Days must be between 1 and 14"),
            )
        }

        val temperatureUnit =
            try {
                TemperatureUnit.valueOf(unit.uppercase())
            } catch (e: IllegalArgumentException) {
                TemperatureUnit.CELSIUS
            }

        val response = weatherService.getWeather(city, temperatureUnit, includeForecast, days)

        if (response.success) {
            return ResponseEntity.ok(response)
        } else {
            return ResponseEntity.status(404).body(response)
        }
    }

    @GetMapping("/{city}/forecast")
    fun getForecast(
        @PathVariable city: String,
        @RequestParam(defaultValue = "7") days: Int,
        @RequestParam(defaultValue = "CELSIUS") unit: String,
    ): ResponseEntity<Any> {
        // Duplicated validation logic
        if (city.length < 2) {
            return ResponseEntity.badRequest().body(mapOf("error" to "City name too short"))
        }

        if (days < 1 || days > 14) {
            return ResponseEntity.badRequest().body(mapOf("error" to "Days must be between 1 and 14"))
        }

        val temperatureUnit =
            try {
                TemperatureUnit.valueOf(unit.uppercase())
            } catch (e: IllegalArgumentException) {
                TemperatureUnit.CELSIUS
            }

        val forecast = weatherService.getForecast(city, days, temperatureUnit)

        if (forecast != null) {
            return ResponseEntity.ok(forecast)
        } else {
            return ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/{city}/alerts")
    fun getAlerts(
        @PathVariable city: String,
    ): ResponseEntity<List<WeatherAlert>> {
        val response = weatherService.getWeather(city, TemperatureUnit.CELSIUS, false, 0)

        if (response.success && response.data != null) {
            val alerts = weatherService.getAlerts(city, response.data)
            return ResponseEntity.ok(alerts)
        }

        return ResponseEntity.ok(emptyList())
    }

    @PostMapping("/compare")
    fun compareWeather(
        @RequestBody cities: List<String>,
    ): ResponseEntity<Map<String, Any>> {
        // Complex nested logic
        val results = mutableMapOf<String, Any>()
        var warmestCity = ""
        var warmestTemp = Double.MIN_VALUE
        var coldestCity = ""
        var coldestTemp = Double.MAX_VALUE

        for (city in cities) {
            val response = weatherService.getWeather(city, TemperatureUnit.CELSIUS, false, 0)
            if (response.success && response.data != null) {
                results[city] = response.data
                if (response.data.temperature > warmestTemp) {
                    warmestTemp = response.data.temperature
                    warmestCity = city
                }
                if (response.data.temperature < coldestTemp) {
                    coldestTemp = response.data.temperature
                    coldestCity = city
                }
            }
        }

        results["warmest"] = mapOf("city" to warmestCity, "temperature" to warmestTemp)
        results["coldest"] = mapOf("city" to coldestCity, "temperature" to coldestTemp)

        return ResponseEntity.ok(results)
    }

    @GetMapping("/report/{city}")
    fun getReport(
        @PathVariable city: String,
    ): ResponseEntity<String> {
        val response = weatherService.getWeather(city, TemperatureUnit.CELSIUS, false, 0)

        if (response.success && response.data != null) {
            val report = weatherService.formatWeatherReport(response.data)
            return ResponseEntity.ok(report)
        }

        return ResponseEntity.notFound().build()
    }

    @DeleteMapping("/cache")
    fun clearCache(): ResponseEntity<Map<String, String>> {
        weatherService.clearCache()
        return ResponseEntity.ok(mapOf("status" to "Cache cleared"))
    }
}
