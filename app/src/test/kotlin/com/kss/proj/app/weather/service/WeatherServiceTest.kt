package com.kss.proj.app.weather.service

import com.kss.proj.app.weather.model.DailyForecast
import com.kss.proj.app.weather.model.TemperatureUnit
import com.kss.proj.app.weather.model.WeatherData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.time.LocalDateTime

class WeatherServiceTest {
    private lateinit var weatherRepository: WeatherRepository
    private lateinit var weatherService: WeatherService

    private val sampleWeatherData =
        WeatherData(
            city = "Prague",
            country = "Czech Republic",
            temperature = 20.0,
            feelsLike = 22.0,
            humidity = 65,
            pressure = 1013,
            windSpeed = 10.0,
            windDirection = "NW",
            description = "Sunny",
            icon = "01d",
            timestamp = LocalDateTime.now(),
            sunrise = LocalDateTime.now().withHour(6),
            sunset = LocalDateTime.now().withHour(20),
        )

    @BeforeEach
    fun setUp() {
        weatherRepository = mock(WeatherRepository::class.java)
        weatherService = WeatherService(weatherRepository)
        weatherService.clearCache()
    }

    @Nested
    @DisplayName("getWeather tests")
    inner class GetWeatherTests {
        @Test
        fun `should return weather data for valid city`() {
            `when`(weatherRepository.fetchWeatherData("Prague")).thenReturn(sampleWeatherData)

            val result = weatherService.getWeather("Prague", TemperatureUnit.CELSIUS, false, 0)

            assertTrue(result.success)
            assertNotNull(result.data)
            assertEquals("Prague", result.data?.city)
            assertEquals(20.0, result.data?.temperature)
        }

        @Test
        fun `should return error for unknown city`() {
            `when`(weatherRepository.fetchWeatherData("Unknown")).thenReturn(null)

            val result = weatherService.getWeather("Unknown", TemperatureUnit.CELSIUS, false, 0)

            assertTrue(!result.success)
            assertNull(result.data)
            assertTrue(result.errorMessage?.contains("not found") == true)
        }

        @Test
        fun `should include forecast when requested`() {
            `when`(weatherRepository.fetchWeatherData("Prague")).thenReturn(sampleWeatherData)

            val result = weatherService.getWeather("Prague", TemperatureUnit.CELSIUS, true, 3)

            assertTrue(result.success)
            assertNotNull(result.forecast)
            assertEquals(3, result.forecast?.forecasts?.size)
        }

        @Test
        fun `should use cached data for repeated requests`() {
            `when`(weatherRepository.fetchWeatherData("Prague")).thenReturn(sampleWeatherData)

            // First call
            val result1 = weatherService.getWeather("Prague", TemperatureUnit.CELSIUS, false, 0)
            // Second call - should use cache
            val result2 = weatherService.getWeather("Prague", TemperatureUnit.CELSIUS, false, 0)

            assertTrue(result1.success)
            assertTrue(result2.success)
        }
    }

    @Nested
    @DisplayName("convertTemperature tests")
    inner class ConvertTemperatureTests {
        @Test
        fun `should return same data for Celsius`() {
            val result = weatherService.convertTemperature(sampleWeatherData, TemperatureUnit.CELSIUS)

            assertEquals(20.0, result.temperature)
            assertEquals(22.0, result.feelsLike)
        }

        @Test
        fun `should convert to Fahrenheit correctly`() {
            val result = weatherService.convertTemperature(sampleWeatherData, TemperatureUnit.FAHRENHEIT)

            // 20°C = 68°F
            assertEquals(68.0, result.temperature)
            // 22°C = 71.6°F
            assertEquals(71.6, result.feelsLike)
        }

        @Test
        fun `should convert to Kelvin correctly`() {
            val result = weatherService.convertTemperature(sampleWeatherData, TemperatureUnit.KELVIN)

            // 20°C = 293.15K
            assertEquals(293.15, result.temperature)
            // 22°C = 295.15K
            assertEquals(295.15, result.feelsLike)
        }
    }

    @Nested
    @DisplayName("getForecast tests")
    inner class GetForecastTests {
        @Test
        fun `should return correct number of forecast days`() {
            val result = weatherService.getForecast("Prague", 5, TemperatureUnit.CELSIUS)

            assertNotNull(result)
            assertEquals(5, result?.forecasts?.size)
            assertEquals("Prague", result?.city)
        }

        @Test
        fun `should return forecasts with future dates`() {
            val result = weatherService.getForecast("Prague", 3, TemperatureUnit.CELSIUS)

            assertNotNull(result)
            result?.forecasts?.forEachIndexed { index, forecast ->
                val expectedDate = LocalDateTime.now().plusDays((index + 1).toLong()).toLocalDate()
                assertEquals(expectedDate, forecast.date.toLocalDate())
            }
        }
    }

    @Nested
    @DisplayName("getAlerts tests")
    inner class GetAlertsTests {
        @Test
        fun `should return heat warning for high temperature`() {
            val hotWeather = sampleWeatherData.copy(temperature = 40.0)

            val alerts = weatherService.getAlerts("Prague", hotWeather)

            assertTrue(alerts.isNotEmpty())
            assertTrue(alerts.any { it.title == "Heat Warning" })
        }

        @Test
        fun `should return freeze warning for low temperature`() {
            val coldWeather = sampleWeatherData.copy(temperature = -15.0)

            val alerts = weatherService.getAlerts("Prague", coldWeather)

            assertTrue(alerts.isNotEmpty())
            assertTrue(alerts.any { it.title == "Freeze Warning" })
        }

        @Test
        fun `should return wind advisory for high wind speed`() {
            val windyWeather = sampleWeatherData.copy(windSpeed = 60.0)

            val alerts = weatherService.getAlerts("Prague", windyWeather)

            assertTrue(alerts.isNotEmpty())
            assertTrue(alerts.any { it.title == "Wind Advisory" })
        }

        @Test
        fun `should return humidity alert for high humidity and temperature`() {
            val humidWeather = sampleWeatherData.copy(humidity = 95, temperature = 30.0)

            val alerts = weatherService.getAlerts("Prague", humidWeather)

            assertTrue(alerts.isNotEmpty())
            assertTrue(alerts.any { it.title == "High Humidity" })
        }

        @Test
        fun `should return no alerts for normal weather`() {
            val alerts = weatherService.getAlerts("Prague", sampleWeatherData)

            assertTrue(alerts.isEmpty())
        }
    }

    @Nested
    @DisplayName("formatWeatherReport tests")
    inner class FormatWeatherReportTests {
        @Test
        fun `should format report with all data`() {
            val report = weatherService.formatWeatherReport(sampleWeatherData)

            assertTrue(report.contains("Prague"))
            assertTrue(report.contains("Czech Republic"))
            assertTrue(report.contains("20.0"))
            assertTrue(report.contains("22.0"))
            assertTrue(report.contains("65"))
            assertTrue(report.contains("10.0"))
            assertTrue(report.contains("NW"))
            assertTrue(report.contains("Sunny"))
        }
    }
}
