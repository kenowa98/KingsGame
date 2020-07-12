package com.kenowa.kingsgame.model

class Usuario(
    val id: String? = "",
    val correo: String = "",
    val nombre: String = "",
    val apellido: String = "",
    val edad: Int = 0,
    val origen: String = "",
    val celular: String = "",
    val genero: Boolean = false,
    val foto: String = "",
    val posicion: String = "",
    val barrio: String = ""
)