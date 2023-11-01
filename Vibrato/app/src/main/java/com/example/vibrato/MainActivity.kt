package com.example.vibrato
import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView

import android.widget.Toast
import com.example.vibrato.databinding.ActivityMainBinding
import com.google.gson.Gson
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.POST
import java.io.IOException


class MainActivity : AppCompatActivity() {


    val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }


    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val tituloteste: TextView = findViewById(R.id.Titulo)
        val authManager = AuthManager(this)

        binding.btLogin.setOnClickListener {
            if (binding.editEmail.text.toString().isEmpty()) {
                binding.editEmail.error = "Preencha todos os campos"
            } else if (binding.editSenha.text.toString().isEmpty()) {
                binding.editSenha.error = "Preencha todos os campos"
            } else if (binding.editSenha.text.toString().length < 8) {
                binding.editSenha.error = "Senha inválida"
            } else if (!binding.editEmail.text.toString().contains("@")) {
                binding.editEmail.error = "Email inválido"
            } else {
                val email = binding.editEmail.text.toString()
                val senha = binding.editSenha.text.toString()

                val dadosLogin = JSONObject()
                dadosLogin.put("email", email)
                dadosLogin.put("senha", senha)

                val client = OkHttpClient()
                val url = "https://vibrato.azurewebsites.net/usuarios/login"
                val json = dadosLogin.toString()
                val body =
                    RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json)
                val request = Request.Builder()
                    .url(url)
                    .post(body)
                    .build()

                client.newCall(request).enqueue(object : okhttp3.Callback {
                    override fun onFailure(call: okhttp3.Call, e: IOException) {
                        runOnUiThread {
                            Toast.makeText(baseContext, e.message, Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                        val responseStatus = response.code()
                        val responseBody = response.body()?.string()

                        runOnUiThread {
                            if (responseStatus == 404) {
                                binding.editEmail.error = "Email não cadastrado"
                            } else if (responseStatus == 401) {
                                binding.editSenha.error = "Senha incorreta"
                            } else if (response.isSuccessful) {
                                if (responseBody != null) {
                                    val gson = Gson()
                                    val login = gson.fromJson(responseBody, DtoAuth::class.java)
                                    authManager.saveAuthToken(login.token.toString())
//                                    tituloteste.text = responseBody.toString()
                                    val intent = Intent(this@MainActivity, EchoFlow::class.java)
                                    startActivity(intent)
                                }
                            } else {
                                Toast.makeText(
                                    baseContext,
                                    "Erro na resposta, tente novamente mais tarde",
                                    Toast.LENGTH_SHORT
                                ).show()
                                tituloteste.text = dadosLogin.toString()
                                println(dadosLogin)
                            }
                        }
                    }
                })
            }
        }
        }
    fun abrirSegundaTela(view: View) {
        val intent = Intent(this@MainActivity, Cadastro::class.java)
        startActivity(intent)
    }
    }





