package com.kss.proj.app.weather.controller

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class WeatherControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Nested
    @DisplayName("GET /api/weather/{city}")
    inner class GetWeatherTests {
        @Test
        fun `should return weather data for known city`() {
            mockMvc
                .perform(get("/api/weather/prague"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.city").value("Prague"))
                .andExpect(jsonPath("$.data.country").value("Czech Republic"))
        }

        @Test
        fun `should return 404 for unknown city`() {
            mockMvc
                .perform(get("/api/weather/unknowncity"))
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.success").value(false))
        }

        @Test
        fun `should return bad request for short city name`() {
            mockMvc
                .perform(get("/api/weather/a"))
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMessage").value("City name too short"))
        }

        @Test
        fun `should convert temperature to Fahrenheit`() {
            mockMvc
                .perform(get("/api/weather/prague?unit=FAHRENHEIT"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.temperature").isNumber)
        }

        @Test
        fun `should include forecast when requested`() {
            // Clear cache first to ensure fresh request
            mockMvc.perform(delete("/api/weather/cache"))

            mockMvc
                .perform(get("/api/weather/london?includeForecast=true&days=3"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.forecast").isNotEmpty)
                .andExpect(jsonPath("$.forecast.forecasts.length()").value(3))
        }

        @Test
        fun `should return bad request for invalid days parameter`() {
            mockMvc
                .perform(get("/api/weather/prague?days=20"))
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.errorMessage").value("Days must be between 1 and 14"))
        }

        @Test
        fun `should default to CELSIUS for invalid unit`() {
            mockMvc
                .perform(get("/api/weather/prague?unit=INVALID"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
        }
    }

    @Nested
    @DisplayName("GET /api/weather/{city}/forecast")
    inner class GetForecastTests {
        @Test
        fun `should return forecast for known city`() {
            mockMvc
                .perform(get("/api/weather/london/forecast"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.city").value("london"))
                .andExpect(jsonPath("$.forecasts").isArray)
        }

        @Test
        fun `should return specified number of forecast days`() {
            mockMvc
                .perform(get("/api/weather/london/forecast?days=5"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.forecasts.length()").value(5))
        }

        @Test
        fun `should return bad request for short city name`() {
            mockMvc
                .perform(get("/api/weather/a/forecast"))
                .andExpect(status().isBadRequest)
        }
    }

    @Nested
    @DisplayName("GET /api/weather/{city}/alerts")
    inner class GetAlertsTests {
        @Test
        fun `should return alerts for city`() {
            mockMvc
                .perform(get("/api/weather/tokyo/alerts"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isArray)
        }

        @Test
        fun `should return freeze warning for Moscow`() {
            mockMvc
                .perform(get("/api/weather/moscow/alerts"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$[0].title").value("Freeze Warning"))
        }
    }

    @Nested
    @DisplayName("POST /api/weather/compare")
    inner class CompareWeatherTests {
        @Test
        fun `should compare weather for multiple cities`() {
            mockMvc
                .perform(
                    post("/api/weather/compare")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""["prague", "london", "tokyo"]"""),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.warmest").exists())
                .andExpect(jsonPath("$.coldest").exists())
        }

        @Test
        fun `should identify warmest and coldest cities`() {
            mockMvc
                .perform(
                    post("/api/weather/compare")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""["moscow", "tokyo"]"""),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.warmest.city").value("tokyo"))
                .andExpect(jsonPath("$.coldest.city").value("moscow"))
        }
    }

    @Nested
    @DisplayName("GET /api/weather/report/{city}")
    inner class GetReportTests {
        @Test
        fun `should return formatted report for known city`() {
            mockMvc
                .perform(get("/api/weather/report/prague"))
                .andExpect(status().isOk)
        }

        @Test
        fun `should return 404 for unknown city`() {
            mockMvc
                .perform(get("/api/weather/report/unknowncity"))
                .andExpect(status().isNotFound)
        }
    }

    @Nested
    @DisplayName("DELETE /api/weather/cache")
    inner class ClearCacheTests {
        @Test
        fun `should clear cache successfully`() {
            mockMvc
                .perform(delete("/api/weather/cache"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.status").value("Cache cleared"))
        }
    }
}
