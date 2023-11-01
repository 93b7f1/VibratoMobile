package com.example.vibrato

import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface Endpoint {
    @GET("/usuarios")
    fun listarTodosUsuarios() : Call<List<Map<String, Any>>>

    @GET("/echo")
    fun listarEchoes() : Call<List<Map<String, Any>>>

    @Headers("Content-Type: application/json")
    @POST("logarasrasd")
    fun requsicaoParaLogin(@Body dadosLogin: JSONObject): Call<List<Map<String, Any>>>

}