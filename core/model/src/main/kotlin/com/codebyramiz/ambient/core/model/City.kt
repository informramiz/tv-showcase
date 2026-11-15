package com.codebyramiz.ambient.core.model

/** A place the weather overlay can show. Coordinates feed the forecast request directly. */
data class City(val id: String, val name: String, val country: String, val latitude: Double, val longitude: Double) {
    companion object {
        val DEFAULT = City("zurich", "Zurich", "Switzerland", 47.3769, 8.5417)

        val ALL: List<City> = listOf(
            DEFAULT,
            City("london", "London", "United Kingdom", 51.5072, -0.1276),
            City("paris", "Paris", "France", 48.8566, 2.3522),
            City("berlin", "Berlin", "Germany", 52.5200, 13.4050),
            City("madrid", "Madrid", "Spain", 40.4168, -3.7038),
            City("reykjavik", "Reykjavik", "Iceland", 64.1466, -21.9426),
            City("dubai", "Dubai", "United Arab Emirates", 25.2048, 55.2708),
            City("mumbai", "Mumbai", "India", 19.0760, 72.8777),
            City("singapore", "Singapore", "Singapore", 1.3521, 103.8198),
            City("tokyo", "Tokyo", "Japan", 35.6762, 139.6503),
            City("sydney", "Sydney", "Australia", -33.8688, 151.2093),
            City("new_york", "New York", "United States", 40.7128, -74.0060),
            City("sao_paulo", "Sao Paulo", "Brazil", -23.5505, -46.6333),
        )

        fun fromIdOrDefault(id: String?): City = ALL.firstOrNull { it.id == id } ?: DEFAULT
    }
}
