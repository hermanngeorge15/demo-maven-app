package com.kss.proj.app.weather.model

import java.time.LocalDateTime

// Intentional issues: wildcard import, magic numbers, data class with too many parameters

data class WeatherData(
    val city: String,
    val country: String,
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Int,
    val pressure: Int,
    val windSpeed: Double,
    val windDirection: String,
    val description: String,
    val icon: String,
    val timestamp: LocalDateTime,
    val sunrise: LocalDateTime,
    val sunset: LocalDateTime,
)

data class WeatherForecast(
    val city: String,
    val forecasts: List<DailyForecast>,
)

data class DailyForecast(
    val date: LocalDateTime,
    val minTemp: Double,
    val maxTemp: Double,
    val description: String,
    val precipitationChance: Int,
)

data class WeatherAlert(
    val id: String,
    val severity: String,
    val title: String,
    val description: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
)

enum class TemperatureUnit {
    CELSIUS,
    FAHRENHEIT,
    KELVIN,
}

data class WeatherRequest(
    val city: String,
    val unit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val includeForecast: Boolean = false,
    val forecastDays: Int = 5,
)

data class WeatherResponse(
    val success: Boolean,
    val data: WeatherData?,
    val forecast: WeatherForecast?,
    val alerts: List<WeatherAlert>,
    val errorMessage: String?,
)
