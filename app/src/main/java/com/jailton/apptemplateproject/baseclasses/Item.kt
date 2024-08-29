package com.jailton.apptemplateproject.baseclasses

data class Item(
    val cnpj: String = "",
    val name: String = "",
    val email: String = "",
    val imageUrl: String = "",
    val endereco: String = "",
    val userId: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    var distancia: String = ""
)

