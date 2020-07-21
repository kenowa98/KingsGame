package com.kenowa.kingsgame.model

import java.io.Serializable

class Usuario(
    val id: String? = "",
    val correo: String = "",
    val nombre: String = "",
    val apellido: String = "",
    val fecha: String = "",
    val origen: String = "",
    val celular: String = "",
    val genero: Boolean = false,
    val foto: String = "",
    val posicion: String = "",
    val barrio: String = "",
    val comuna: String = "",
    val equipos: Int = 0
) : Serializable