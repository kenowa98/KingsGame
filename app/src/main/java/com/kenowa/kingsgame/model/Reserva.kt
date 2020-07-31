package com.kenowa.kingsgame.model

import java.io.Serializable

class Reserva(
    val id: String? = "",
    val idUser: String = "",
    val idCancha: String = "",
    val idLugar: String = "",
    val fecha: String = "",
    val inicioHora: Int = 0,
    val finHora: Int = 0,
    val precio: Int = 0
) : Serializable