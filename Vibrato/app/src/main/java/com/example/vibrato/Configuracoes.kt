package com.example.vibrato

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.vibrato.databinding.ActivityConfiguracoesBinding
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.IOException


class Configuracoes : AppCompatActivity() {

    val binding by lazy {
        ActivityConfiguracoesBinding.inflate(layoutInflater)
    }

    private var id: Int = 0
    private var username: String? = null
    private var tipoUsuario: String? = null

    var dadosUsuario : DtoUsuario? = null

    var clique : Boolean = false
    var cliqueSenha : Boolean = false
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MusicianHome::class.java)
        startActivity(intent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val editEmail = findViewById<EditText>(R.id.textEmail)

        editEmail.isEnabled = false
        editEmail.isFocusableInTouchMode = false


        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        id = sharedPreferences.getInt("id", 0)
        username = sharedPreferences.getString("username", null)
        tipoUsuario = sharedPreferences.getString("tipoUsuario", null)

        val client = OkHttpClient()

        val request = Request.Builder()
            .url("https://vibrato.azurewebsites.net/usuarios/perfil/$id")
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                runOnUiThread {
                    Log.e("Configuracoes", "Falha na requisição: ${e.message}")
                    Toast.makeText(
                        baseContext,
                        "Falha na requisição: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val responseBody = response.body?.string()

                if (response.isSuccessful && !responseBody.isNullOrBlank()) {
                    val gson = Gson()
                    dadosUsuario = gson.fromJson(responseBody, DtoUsuario::class.java)

                    val editableEmail =
                        Editable.Factory.getInstance().newEditable(dadosUsuario!!.email)


                    runOnUiThread {
                        binding.textEmail.text = editableEmail
                    }
                }
            }
        })




        binding.btSac.setOnClickListener {
            val url = "https://proxmoove.tomticket.com/?account=3669056P13052022065948"

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                exibirMensagemDeErro("Não há navegador disponível para a ação")
            }
        }


        binding.btEditarEmail.setOnClickListener {

            if (clique == false) {
                editEmail.isEnabled = true
                editEmail.isFocusableInTouchMode = true
                editEmail.requestFocus()

                clique = true
            } else {
                val novoEmail = binding.textEmail.text
                if (!novoEmail.contains("@")) {
                    exibirToast(this, "Email inváido")
                } else {
                    val emailAtualizacao = JSONObject()
                    emailAtualizacao.put("email", novoEmail)

                    val client2 = OkHttpClient()

                    val url =
                        "https://vibrato.azurewebsites.net/usuarios/atualizar/perfil/email/${id}"

                    val json = emailAtualizacao.toString()

                    val body = RequestBody.create(
                        "application/json; charset=utf-8".toMediaTypeOrNull(),
                        json
                    )

                    val request2 = Request.Builder()
                        .url(url)
                        .patch(body)
                        .build()

                    client2.newCall(request2).enqueue(object : okhttp3.Callback {
                        override fun onFailure(call: okhttp3.Call, e: IOException) {
                            runOnUiThread {
                                Toast.makeText(baseContext, e.message, Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                            val responseBody = response.body?.string()
                            runOnUiThread {
                                if (response.isSuccessful) {
                                    val gson = Gson()
                                    val atualizado =
                                        gson.fromJson(responseBody, DtoUsuario::class.java)
                                    editEmail.isEnabled = false
                                    editEmail.isFocusableInTouchMode = false
                                    clique = false
                                    exibirToast(
                                        this@Configuracoes,
                                        "Email ${atualizado.email} atualizado"
                                    )
                                } else {
                                    Toast.makeText(
                                        baseContext,
                                        "Erro na resposta, tente novamente mais tarde",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    })
                }
            }
        }
        binding.btExcluirConta.setOnClickListener{
            exibirPopupConfirmacao()

        }
    }



    private fun exibirMensagemDeErro(mensagem: String) {
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show()
    }
    private fun exibirToast(context: Context, mensagem: String) {
        Toast.makeText(context, mensagem, Toast.LENGTH_SHORT).show()
    }
    private fun exibirPopupConfirmacao() {
        val builder = AlertDialog.Builder(this)

        builder.setTitle("Confirmação")
        builder.setMessage("Você tem certeza que deseja excluir sua conta?")

        builder.setPositiveButton("Sim") { dialogInterface: DialogInterface, _: Int ->
            // Lógica a ser executada se o usuário clicar em "Sim"
            excluirConta()
            dialogInterface.dismiss() // Fecha o pop-up
        }

        builder.setNegativeButton("Não") { dialogInterface: DialogInterface, _: Int ->
            // Lógica a ser executada se o usuário clicar em "Não"
            dialogInterface.dismiss() // Fecha o pop-up
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun excluirConta() {
        val client3 = OkHttpClient()

        val url =
            "https://vibrato.azurewebsites.net/usuarios/deletar/${id}"

        val request3 = Request.Builder()
            .url(url)
            .delete()
            .build()

        client3.newCall(request3).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(baseContext, e.message, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val responseBody = response.body?.string()
                runOnUiThread {
                    if (response.isSuccessful) {
                        exibirToast(
                            this@Configuracoes,
                            "Conta deletada"
                        )
                        val intent = Intent(this@Configuracoes, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(
                            baseContext,
                            "Erro na resposta, tente novamente mais tarde",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }
}