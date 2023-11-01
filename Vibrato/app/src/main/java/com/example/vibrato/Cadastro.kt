package com.example.vibrato

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vibrato.databinding.ActivityCadastroBinding
import com.google.gson.Gson
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.IOException


class Cadastro : AppCompatActivity() {
    val binding by lazy{
        ActivityCadastroBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnCadastroVoltaPage.setOnClickListener {
            val intent = Intent(this@Cadastro, MainActivity::class.java)
            startActivity(intent)
        }
//        val resultadoTeste: TextView = findViewById(R.id.RESPOSTA)
        val authManager = AuthManager(this)

            var tipoUsuario: String? = null

            val botaoArtista = findViewById<Button>(R.id.botao_artista)
            val botaoOuvinte = findViewById<Button>(R.id.botao_ouvinte)


            botaoArtista.setOnClickListener {
                if (!botaoArtista.isSelected) {
                    botaoArtista.isSelected = true
                    botaoOuvinte.isSelected = false
                    tipoUsuario = "artista"
                    botaoArtista.isSelected != botaoArtista.isSelected
                }
            }

            botaoOuvinte.setOnClickListener {
                if (!botaoOuvinte.isSelected) {
                    botaoArtista.isSelected = false
                    botaoOuvinte.isSelected = true
                    tipoUsuario = "ouvinte"
                    botaoOuvinte.isSelected != botaoArtista.isSelected
                }
            }
            binding.btnCadastro.setOnClickListener{
                if (binding.editNome.text.toString().isEmpty()) {
                    binding.editNome.error = "Por favor insira seu nome."
                    return@setOnClickListener
                }

                if (binding.editUsername.text.toString().isEmpty()) {
                    binding.editUsername.error = "Por favor insira seu sobrenome."
                    return@setOnClickListener
                }

                if (binding.editEmail.text.toString().isEmpty() || !binding.editEmail.text.toString().contains("@")) {
                    binding.editEmail.error = "Email inválido"
                    return@setOnClickListener
                }

                if (binding.editSenha.text.toString().isEmpty()) {
                    binding.editSenha.error = "Por favor insira sua senha."
                    return@setOnClickListener
                }else if(binding.editSenha.text.toString().length < 8){
                    binding.editSenha.error = "A senha deve conter mais de 8 caracteres"
                    return@setOnClickListener
                }

                if (binding.editSenhaConfirmar.text.toString().isEmpty()) {
                    binding.editSenhaConfirmar.error = "Por favor confirme sua senha"
                    return@setOnClickListener
                }

                if (!binding.editSenhaConfirmar.text.toString().equals(binding.editSenha.text.toString())) {
                    binding.editSenhaConfirmar.error = "Senhas diferentes"
                    return@setOnClickListener
                }

                val nome = binding.editNome.text.toString()
                val username = binding.editUsername.text.toString()
                val email = binding.editEmail.text.toString()
                val senha = binding.editSenha.text.toString()


                val dadosCadastro = JSONObject()
                dadosCadastro.put("nome", nome)
                dadosCadastro.put("username", username)
                dadosCadastro.put("email", email)
                dadosCadastro.put("senha", senha)

                val client = OkHttpClient()
                val url = "https://vibrato.azurewebsites.net/usuarios/$tipoUsuario"
                val json = dadosCadastro.toString()
                val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json)
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
                            if (responseStatus == 409) {
                                binding.editUsername.error = "Username já cadastrado"
                            } else if (responseStatus == 409) {
                                binding.editEmail.error = "E-mail já cadastrado"
                            } else if (response.isSuccessful) {
                                if (responseBody != null) {
                                    val gson = Gson()
                                    val cadastro = gson.fromJson(responseBody, DtoAuth::class.java)
                                    authManager.saveAuthToken(cadastro.token.toString())
                                    val intent = Intent(this@Cadastro, EchoFlow::class.java)
                                    startActivity(intent)
                                }
                            } else {
                                Toast.makeText(baseContext, "Erro na resposta, tente novamente mais tarde", Toast.LENGTH_SHORT).show()
//                                resultadoTeste.text = url
                            }
                        }
                    }
                })
            }


        }

    }
