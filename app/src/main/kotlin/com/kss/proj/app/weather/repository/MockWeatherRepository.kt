package com.kss.proj.app.weather.repository

import com.kss.proj.app.weather.model.DailyForecast
import com.kss.proj.app.weather.model.WeatherData
import com.kss.proj.app.weather.service.WeatherRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.Random

@Repository
class MockWeatherRepository : WeatherRepository {
    // Mock data with magic numbers
    private val weatherDatabase =
        mapOf(
            "prague" to
                WeatherData(
                    city = "Prague",
                    country = "Czech Republic",
                    temperature = 22.5,
                    feelsLike = 24.0,
                    humidity = 65,
                    pressure = 1013,
                    windSpeed = 12.5,
                    windDirection = "NW",
                    description = "Partly cloudy",
                    icon = "02d",
                    timestamp = LocalDateTime.now(),
                    sunrise = LocalDateTime.now().withHour(5).withMinute(30),
                    sunset = LocalDateTime.now().withHour(20).withMinute(45),
                ),
            "london" to
                WeatherData(
                    city = "London",
                    country = "United Kingdom",
                    temperature = 18.0,
                    feelsLike = 17.5,
                    humidity = 78,
                    pressure = 1008,
                    windSpeed = 22.0,
                    windDirection = "W",
                    description = "Light rain",
                    icon = "10d",
                    timestamp = LocalDateTime.now(),
                    sunrise = LocalDateTime.now().withHour(5).withMinute(15),
                    sunset = LocalDateTime.now().withHour(21).withMinute(0),
                ),
            "new york" to
                WeatherData(
                    city = "New York",
                    country = "United States",
                    temperature = 28.5,
                    feelsLike = 31.0,
                    humidity = 70,
                    pressure = 1015,
                    windSpeed = 8.0,
                    windDirection = "S",
                    description = "Sunny",
                    icon = "01d",
                    timestamp = LocalDateTime.now(),
                    sunrise = LocalDateTime.now().withHour(5).withMinute(45),
                    sunset = LocalDateTime.now().withHour(20).withMinute(30),
                ),
            "tokyo" to
                WeatherData(
                    city = "Tokyo",
                    country = "Japan",
                    temperature = 32.0,
                    feelsLike = 36.0,
                    humidity = 85,
                    pressure = 1010,
                    windSpeed = 5.0,
                    windDirection = "SE",
                    description = "Hot and humid",
                    icon = "01d",
                    timestamp = LocalDateTime.now(),
                    sunrise = LocalDateTime.now().withHour(4).withMinute(30),
                    sunset = LocalDateTime.now().withHour(19).withMinute(0),
                ),
            "moscow" to
                WeatherData(
                    city = "Moscow",
                    country = "Russia",
                    temperature = -15.0,
                    feelsLike = -22.0,
                    humidity = 80,
                    pressure = 1025,
                    windSpeed = 18.0,
                    windDirection = "N",
                    description = "Snow",
                    icon = "13d",
                    timestamp = LocalDateTime.now(),
                    sunrise = LocalDateTime.now().withHour(8).withMinute(0),
                    sunset = LocalDateTime.now().withHour(16).withMinute(30),
                ),
        )

    override fun fetchWeatherData(city: String): WeatherData? {
        val key = city.lowercase().trim()

        // Simulate network delay - magic number
        Thread.sleep(100)

        return weatherDatabase[key]?.copy(timestamp = LocalDateTime.now())
    }

    override fun fetchForecastData(
        city: String,
        days: Int,
    ): List<DailyForecast>? {
        val baseData = weatherDatabase[city.lowercase()]
        if (baseData == null) {
            return null
        }

        val forecasts = mutableListOf<DailyForecast>()
        val random = Random()

        // Generate forecast with magic numbers
        for (i in 1..days) {
            val variation = random.nextInt(10) - 5
            forecasts.add(
                DailyForecast(
                    date = LocalDateTime.now().plusDays(i.toLong()),
                    minTemp = baseData.temperature - 5 + variation,
                    maxTemp = baseData.temperature + 5 + variation,
                    description = listOf("Sunny", "Cloudy", "Rainy", "Partly cloudy")[random.nextInt(4)],
                    precipitationChance = random.nextInt(100),
                ),
            )
        }

        return forecasts
    }

    // Utility function with bad practices
    fun generateRandomWeather(
        city: String,
        country: String,
    ): WeatherData {
        val r = Random()
        return WeatherData(
            city = city,
            country = country,
            temperature = r.nextDouble() * 50 - 10, // -10 to 40
            feelsLike = r.nextDouble() * 50 - 10,
            humidity = r.nextInt(100),
            pressure = 980 + r.nextInt(60),
            windSpeed = r.nextDouble() * 100,
            windDirection = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")[r.nextInt(8)],
            description = "Random weather",
            icon = "01d",
            timestamp = LocalDateTime.now(),
            sunrise = LocalDateTime.now().withHour(6),
            sunset = LocalDateTime.now().withHour(20),
        )
    }
}
