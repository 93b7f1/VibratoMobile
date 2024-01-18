package com.example.vibrato

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.vibrato.databinding.ActivityCadastroBinding
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.IOException

class Cadastro : AppCompatActivity() {
    private var val_check: Boolean = false

    private val binding by lazy {
        ActivityCadastroBinding.inflate(layoutInflater)
    }

    private fun exibirPopupConfirmacao() {
        val builder = AlertDialog.Builder(this)

        builder.setTitle("Confirmação de uso de dados")
        builder.setMessage(
            "Prezado(a) Usuário(a),\n" +
                    "\n" +
                    "Agradecemos por escolher utilizar nosso aplicativo. A fim de proporcionar a melhor experiência possível e garantir a conformidade com a legislação em vigor, solicitamos o seu consentimento para o processamento dos seus dados pessoais.\n" +
                    "\n" +
                    "1. Finalidade do Tratamento:\n" +
                    "\n" +
                    "Informamos que os dados fornecidos serão utilizados exclusivamente para os seguintes fins:\n" +
                    "\n" +
                    "Registro e autenticação de conta.\n" +
                    "Personalização da sua experiência no aplicativo.\n" +
                    "Envio de informações relevantes sobre o uso do aplicativo.\n" +
                    "Melhoria contínua dos nossos serviços.\n" +
                    "2. Dados Coletados:\n" +
                    "\n" +
                    "Os dados pessoais que poderemos coletar incluem, mas não se limitam a:\n" +
                    "\n" +
                    "Nome completo.\n" +
                    "Endereço de e-mail.\n" +
                    "Nome de usuário.\n" +
                    "Informações relacionadas ao uso do aplicativo.\n" +
                    "3. Consentimento:\n" +
                    "\n" +
                    "Ao continuar a utilizar nosso aplicativo, você concorda expressamente com a coleta, armazenamento, processamento e compartilhamento seguro dos seus dados pessoais, conforme descrito nesta política. Entenda que você tem o direito de revogar esse consentimento a qualquer momento, mediante solicitação por meio dos canais disponíveis em nossa plataforma.\n" +
                    "\n" +
                    "4. Compartilhamento de Dados:\n" +
                    "\n" +
                    "Asseguramos que seus dados não serão compartilhados com terceiros não autorizados. Caso haja a necessidade de compartilhamento, o faremos de acordo com a legislação aplicável.\n" +
                    "\n" +
                    "5. Segurança dos Dados:\n" +
                    "\n" +
                    "Implementamos medidas de segurança apropriadas para proteger seus dados contra acesso não autorizado, alteração, divulgação ou destruição não autorizada.\n" +
                    "\n" +
                    "6. Período de Retenção:\n" +
                    "\n" +
                    "Seus dados serão retidos apenas pelo tempo necessário para cumprir as finalidades para as quais foram coletados, observando as disposições legais.\n" +
                    "\n" +
                    "7. Seus Direitos:\n" +
                    "\n" +
                    "Você possui direitos garantidos pela LGPD, incluindo o acesso, correção, exclusão e portabilidade dos seus dados. Caso deseje exercer esses direitos ou tenha dúvidas, entre em contato conosco através dos canais indicados no aplicativo.\n" +
                    "\n" +
                    "Ao continuar utilizando nosso aplicativo, você reconhece ter lido, compreendido e concordado com esta política de privacidade e termos de uso."
        )

        builder.setPositiveButton("Sim") { dialogInterface: DialogInterface, _: Int ->
            val_check = true
            dialogInterface.dismiss()
            realizarCadastro()
        }

        builder.setNegativeButton("Não") { dialogInterface: DialogInterface, _: Int ->
            val_check = false
            dialogInterface.dismiss()
            val intent2 = Intent(this@Cadastro, MainActivity::class.java)
            startActivity(intent2)
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun realizarCadastro() {
        val nome = binding.editNome.text.toString()
        val username = binding.editUsername.text.toString()
        val email = binding.editEmail.text.toString()
        val senha = binding.editSenha.text.toString()

        val tipoUsuario = if (binding.botaoArtista.isSelected) "artista" else "ouvinte"

        val dadosCadastro = JSONObject()
        dadosCadastro.put("nome", nome)
        dadosCadastro.put("username", username)
        dadosCadastro.put("email", email)
        dadosCadastro.put("senha", senha)

        val client = OkHttpClient()
        val url = "https://vibrato.azurewebsites.net/usuarios/$tipoUsuario"
        val json = dadosCadastro.toString()
        val body =
            RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), json)
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
                val responseStatus = response.code
                val responseBody = response.body?.string()

                runOnUiThread {
                    if (responseStatus == 409) {
                        binding.editUsername.error = "Username já cadastrado"
                    } else if (responseStatus == 409) {
                        binding.editEmail.error = "E-mail já cadastrado"
                    } else if (response.isSuccessful) {
                        if (responseBody != null) {
                            val gson = Gson()
                            val cadastro = gson.fromJson(responseBody, DtoAuth::class.java)
                            val authManager = AuthManager(this@Cadastro)
                            authManager.saveAuthToken(cadastro.token.toString())
                            val intent =
                                Intent(this@Cadastro, MusicianHome::class.java)
                            startActivity(intent)
                        }
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnCadastroVoltaPage.setOnClickListener {
            val intent = Intent(this@Cadastro, MainActivity::class.java)
            startActivity(intent)
        }

        val botaoArtista = findViewById<Button>(R.id.botao_artista)
        val botaoOuvinte = findViewById<Button>(R.id.botao_ouvinte)

        botaoArtista.setOnClickListener {
            if (!botaoArtista.isSelected) {
                botaoArtista.isSelected = true
                botaoOuvinte.isSelected = false
            }
        }

        botaoOuvinte.setOnClickListener {
            if (!botaoOuvinte.isSelected) {
                botaoArtista.isSelected = false
                botaoOuvinte.isSelected = true
            }
        }

        binding.btnCadastro.setOnClickListener {
            if (binding.editNome.text.toString().isEmpty()) {
                binding.editNome.error = "Por favor insira seu nome."
                return@setOnClickListener
            }

            if (binding.editUsername.text.toString().isEmpty()) {
                binding.editUsername.error = "Por favor insira seu sobrenome."
                return@setOnClickListener
            }

            if (binding.editEmail.text.toString().isEmpty() || !binding.editEmail.text.toString()
                    .contains("@")
            ) {
                binding.editEmail.error = "Email inválido"
                return@setOnClickListener
            }

            if (binding.editSenha.text.toString().isEmpty()) {
                binding.editSenha.error = "Por favor insira sua senha."
                return@setOnClickListener
            } else if (binding.editSenha.text.toString().length < 8) {
                binding.editSenha.error = "A senha deve conter mais de 8 caracteres"
                return@setOnClickListener
            }

            if (binding.editSenhaConfirmar.text.toString().isEmpty()) {
                binding.editSenhaConfirmar.error = "Por favor confirme sua senha"
                return@setOnClickListener
            }

            if (!binding.editSenhaConfirmar.text.toString()
                    .equals(binding.editSenha.text.toString())
            ) {
                binding.editSenhaConfirmar.error = "Senhas diferentes"
                return@setOnClickListener
            }

            exibirPopupConfirmacao()
        }
    }
}
