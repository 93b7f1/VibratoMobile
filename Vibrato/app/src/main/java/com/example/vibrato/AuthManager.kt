package com.example.vibrato
import android.content.Context
import android.content.SharedPreferences
class AuthManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)

    // Função para salvar o token após o login
    fun saveAuthToken(token: String) {
        sharedPreferences.edit().putString("authToken", token).apply()
    }

    // Função para recuperar o token
    fun getAuthToken(): String? {
        return sharedPreferences.getString("authToken", null)
    }

    fun logout() {
        sharedPreferences.edit().remove("authToken").apply()
        // Redirecione o usuário para a tela de login ou qualquer outra ação necessária
    }
}