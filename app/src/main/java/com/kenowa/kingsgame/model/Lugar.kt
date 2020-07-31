package com.kenowa.kingsgame.model

class Lugar(
    val id: String = "",
    val address: String = "",
    val logo: String = "",
    val calificacion: Double = 0.0,
    val lunes: String = "",
    val martes: String = "",
    val miercoles: String = "",
    val jueves: String = "",
    val viernes: String = "",
    val sabado: String = "",
    val domingo: String = "",
    val festivo: String = "",
    val parqueadero: Boolean = false,
    val tienda: Boolean = false,
    val locker: Boolean = false,
    val vestier: Boolean = false,
    val peto: Boolean = false,
    val balon: Boolean = false,
    val latitud: Double = 0.0,
    val longitud: Double = 0.0
)