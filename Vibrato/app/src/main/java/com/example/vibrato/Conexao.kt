package com.example.vibrato

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Conexao{
    companion object {

        /** Retorna uma Instância do Client Retrofit para Requisições
         * @param path Caminho Principal da API
         */
        fun getRetrofitInstance(path : String) : Retrofit {
            return Retrofit.Builder()
                .baseUrl("https://vibrato.azurewebsites.net/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
    }
}