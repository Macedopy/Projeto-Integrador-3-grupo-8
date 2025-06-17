package com.example.geowarning.Location

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val userId: String,
    val imageBase64: String,
    val title: String? = null,
    val description: String? = null,
    val riskLevel: String? = null // "Estável", "Moderado", or "Perigoso"
)