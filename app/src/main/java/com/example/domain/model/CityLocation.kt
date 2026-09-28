package com.example.domain.model

data class CityLocation(
    val name: String,
    val stateOrCountry: String,
    val latitude: Double,
    val longitude: Double,
    val timeZoneId: String
)

object PredefinedLocations {
    val CITIES = listOf(
        CityLocation("Delhi", "India", 28.6139, 77.2090, "Asia/Kolkata"),
        CityLocation("Mumbai", "India", 19.0760, 72.8777, "Asia/Kolkata"),
        CityLocation("Hyderabad", "India", 17.3850, 78.4867, "Asia/Kolkata"),
        CityLocation("Junagadh", "Gujarat, India", 21.5222, 70.4579, "Asia/Kolkata"),
        CityLocation("Ahmedabad", "Gujarat, India", 23.0225, 72.5714, "Asia/Kolkata"),
        CityLocation("Lucknow", "Uttar Pradesh, India", 26.8467, 80.9462, "Asia/Kolkata"),
        CityLocation("Bengaluru", "Karnataka, India", 12.9716, 77.5946, "Asia/Kolkata"),
        CityLocation("Kolkata", "West Bengal, India", 22.5726, 88.3639, "Asia/Kolkata"),
        CityLocation("Chennai", "Tamil Nadu, India", 13.0827, 80.2707, "Asia/Kolkata"),
        CityLocation("Srinagar", "Kashmir, India", 34.0837, 74.7973, "Asia/Kolkata"),
        CityLocation("Dubai", "United Arab Emirates", 25.2048, 55.2708, "Asia/Dubai"),
        CityLocation("Makkah", "Saudi Arabia", 21.4225, 39.8262, "Asia/Riyadh"),
        CityLocation("Madinah", "Saudi Arabia", 24.5247, 39.5692, "Asia/Riyadh"),
        CityLocation("London", "United Kingdom", 51.5074, -0.1278, "Europe/London"),
        CityLocation("New York", "United States", 40.7128, -74.0060, "America/New_York"),
        CityLocation("Toronto", "Canada", 43.6532, -79.3832, "America/Toronto")
    )

    val DEFAULT_CITY = CITIES.find { it.name == "Delhi" } ?: CITIES[0]
}
