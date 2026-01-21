package com.kss.proj.app.weather.service

import com.kss.proj.app.weather.model.DailyForecast
import com.kss.proj.app.weather.model.TemperatureUnit
import com.kss.proj.app.weather.model.WeatherAlert
import com.kss.proj.app.weather.model.WeatherData
import com.kss.proj.app.weather.model.WeatherForecast
import com.kss.proj.app.weather.model.WeatherResponse
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.Random
import java.util.UUID

@Service
class WeatherService(
    private val weatherRepository: WeatherRepository,
) {
    private val cache = mutableMapOf<String, Pair<WeatherData, Long>>()

    // Magic numbers, long method, complex conditions
    fun getWeather(
        city: String,
        unit: TemperatureUnit,
        includeForecast: Boolean,
        days: Int,
    ): WeatherResponse {
        val cacheKey = city.lowercase()
        val cachedData = cache[cacheKey]

        // Check cache - magic number 300000 (5 minutes)
        if (cachedData != null && System.currentTimeMillis() - cachedData.second < 300000) {
            val data = convertTemperature(cachedData.first, unit)
            return WeatherResponse(true, data, null, emptyList(), null)
        }

        try {
            val rawData = weatherRepository.fetchWeatherData(city)

            if (rawData == null) {
                return WeatherResponse(false, null, null, emptyList(), "City not found: " + city)
            }

            // Cache the data
            cache[cacheKey] = Pair(rawData, System.currentTimeMillis())

            val convertedData = convertTemperature(rawData, unit)

            var forecast: WeatherForecast? = null
            if (includeForecast == true) {
                forecast = getForecast(city, days, unit)
            }

            val alerts = getAlerts(city, rawData)

            return WeatherResponse(true, convertedData, forecast, alerts, null)
        } catch (e: Exception) {
            // Empty catch - bad practice
            return WeatherResponse(false, null, null, emptyList(), "Error fetching weather")
        }
    }

    // Nested complexity, magic numbers
    fun convertTemperature(
        data: WeatherData,
        unit: TemperatureUnit,
    ): WeatherData {
        if (unit == TemperatureUnit.CELSIUS) {
            return data
        } else if (unit == TemperatureUnit.FAHRENHEIT) {
            return data.copy(
                temperature = data.temperature * 9 / 5 + 32,
                feelsLike = data.feelsLike * 9 / 5 + 32,
            )
        } else if (unit == TemperatureUnit.KELVIN) {
            return data.copy(
                temperature = data.temperature + 273.15,
                feelsLike = data.feelsLike + 273.15,
            )
        } else {
            return data
        }
    }

    // Long parameter list, unused parameter
    fun getForecast(
        city: String,
        days: Int,
        unit: TemperatureUnit,
        unused: String = "",
    ): WeatherForecast? {
        val forecasts = mutableListOf<DailyForecast>()

        for (i in 1..days) {
            val forecast =
                DailyForecast(
                    date = LocalDateTime.now().plusDays(i.toLong()),
                    minTemp = 10.0 + Random().nextInt(10),
                    maxTemp = 20.0 + Random().nextInt(15),
                    description = if (i % 2 == 0) "Sunny" else "Cloudy",
                    precipitationChance = Random().nextInt(100),
                )
            forecasts.add(forecast)
        }

        return WeatherForecast(city, forecasts)
    }

    // Complex method with many branches
    fun getAlerts(
        city: String,
        data: WeatherData,
    ): List<WeatherAlert> {
        val alerts = mutableListOf<WeatherAlert>()

        // Magic numbers everywhere
        if (data.temperature > 35) {
            alerts.add(
                WeatherAlert(
                    UUID.randomUUID().toString(),
                    "HIGH",
                    "Heat Warning",
                    "Temperature exceeds 35°C",
                    LocalDateTime.now(),
                    LocalDateTime.now().plusHours(24),
                ),
            )
        }

        if (data.temperature < -10) {
            alerts.add(
                WeatherAlert(
                    UUID.randomUUID().toString(),
                    "HIGH",
                    "Freeze Warning",
                    "Temperature below -10°C",
                    LocalDateTime.now(),
                    LocalDateTime.now().plusHours(24),
                ),
            )
        }

        if (data.windSpeed > 50) {
            alerts.add(
                WeatherAlert(
                    UUID.randomUUID().toString(),
                    "MEDIUM",
                    "Wind Advisory",
                    "Wind speed exceeds 50 km/h",
                    LocalDateTime.now(),
                    LocalDateTime.now().plusHours(12),
                ),
            )
        }

        if (data.humidity > 90 && data.temperature > 25) {
            alerts.add(
                WeatherAlert(
                    UUID.randomUUID().toString(),
                    "LOW",
                    "High Humidity",
                    "Humidity above 90% with high temperature",
                    LocalDateTime.now(),
                    LocalDateTime.now().plusHours(6),
                ),
            )
        }

        return alerts
    }

    // String concatenation instead of template
    fun formatWeatherReport(data: WeatherData): String {
        var report = ""
        report = report + "Weather Report for " + data.city + ", " + data.country + "\n"
        report = report + "Temperature: " + data.temperature + "°C\n"
        report = report + "Feels like: " + data.feelsLike + "°C\n"
        report = report + "Humidity: " + data.humidity + "%\n"
        report = report + "Wind: " + data.windSpeed + " km/h " + data.windDirection + "\n"
        report = report + "Description: " + data.description + "\n"
        return report
    }

    fun clearCache() {
        cache.clear()
    }

    // Spread operator usage
    fun logWeatherData(vararg cities: String) {
        val cityList = listOf(*cities)
        cityList.forEach { println("Fetching weather for: $it") }
    }
}

interface WeatherRepository {
    fun fetchWeatherData(city: String): WeatherData?

    fun fetchForecastData(
        city: String,
        days: Int,
    ): List<DailyForecast>?
}
