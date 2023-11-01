package com.example.vibrato

data class Echo(
    val idEcho: Int,
    val artista: Artista,
    val tituloMusica: String,
    val tituloAlbum: String,
    val compositor: String,
    val genero: String,
    val lyrics: String,
    val spotify: String,
    val youtube: String,
    val soundCloud: String,
    val deezer: String,
    val amazonMusic: String,
    val otherPlataform1: String,
    val otherLink1: String,
    val blob: String,
    val visualizacao: Int,
    val streams: Int,
    val redirecionamento: Int,
    val curtidas: Int

    )

data class Artista(
    val idUsuario: Int,
    val nome: String,
    val senha: String,
    val email: String,
    val username: String,
    val twitter: String,
    val instagram: String,
    val spotify: String,
    val soundcloud: String,
    val genero: String,
    val biografia: String,
    val blob: String,
    val visualizacao: Int,
    val novaSenha: String?,
    val tipoUsuario: String
)
