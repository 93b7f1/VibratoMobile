package com.example.vibrato

import java.io.Serializable

data class DataMusicDefault(
    val spotify: String,
    val youtube: String,
    val soundClooud: String,
    val deezer: String,
    val amazonMusic: String,
    val appleMusic: String,
    val linkPerfil: String,
    val linkMusica: String
) :Serializable{

}
