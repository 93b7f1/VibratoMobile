package com.example.vibrato

import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface Endpoint {

    @GET("/echo/buscar-por-texto/{termo}")
    fun listarPesquisa(@Path("termo") termo: String) : Call<List<Map<String, Any>>>

    @GET("/echo/artista/{userId}")
    fun listaDash(@Path("userId") userId: String) : Call<List<Map<String, Any>>>

    @GET("/usuarios")
    fun listarTodosUsuarios() : Call<List<Map<String, Any>>>

    @GET("/echo")
    fun listarEchoes() : Call<List<Map<String, Any>>>

    @GET("/echo/explore")
    fun listarExplore() : Call<List<Map<String, Any>>>
    @Headers("Content-Type: application/json")
    @POST("logarasrasd")
    fun requsicaoParaLogin(@Body dadosLogin: JSONObject): Call<List<Map<String, Any>>>

    @GET("/echo/top5")
    fun listarTop5() : Call<List<Map<String, Any>>>

    @GET("/usuarios/perfil/{id}")
    fun getUsuario(@Path("id") id: Int) : Call<Artista>

    @GET("/echo/echolink/{id}")
    fun getEcholink(@Path("id") id: Int) : Call<Echo>

    @GET("/echo/desc-list")
    fun listarEchoes2() : Call<List<Map<String, Any>>>

    @GET("/echo/last3/{userId}")
    fun listarEchoesArtista(@Path("userId") id : Int) : Call<List<Map<String, Any>>>

    @GET("/echo/visuu/{username}")
    fun listarEchoesMaisView(@Path("username") username : String) : Call<List<Map<String, Any>>>







}