package com.example.geowarning.Registro

data class Registro(
    val latitude: Double,
    val longitude: Double,
    val title: String,
    val description: String? = null,
    val riskLevel: String? = null,
    val imageBase64: String? = null,
    val userId: String
)
