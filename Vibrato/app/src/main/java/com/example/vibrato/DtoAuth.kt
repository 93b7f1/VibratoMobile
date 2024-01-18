package com.example.vibrato

data class DtoAuth(
    var id: Int? = null,
    var username: String? = null,
    var token: Token? = null,
    var tipoUsuario: String? = null,
    var blob: String? = null
)
