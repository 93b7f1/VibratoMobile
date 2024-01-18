package com.example.vibrato

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import com.example.vibrato.databinding.ActivityCadastroMusica2Binding
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException

class cadastroMusica2 : AppCompatActivity() {
    val binding by lazy{
        ActivityCadastroMusica2Binding.inflate(layoutInflater)
    }
    private var spotify: String? = null
    private var youtube: String? = null
    private var soundCloud : String? = null
    private var deezer: String? = null
    private var amazon: String? = null
    private var apple: String? = null
    private var linkPerfil: String? = null
    private var linkMusica: String? = null
    private var imagem: String? = null
    private val maxCharacters = 3000
    private var id: Int? = 0
    val generos = arrayOf("Rock", "Pop", "Future Core",
        "Kpop", "Jrock", "Hip Hop/ Rap", "RbSoul",
        "Indie", "Edm", "EDM kawaii", "Jazz",
        "Sertanejo", "Eletro Swing", "Pagode")
    private var chosenGenre: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val spinner = findViewById<Spinner>(R.id.spinner_generos)
        val arrayAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, generos)
        spinner.adapter = arrayAdapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                Toast.makeText(applicationContext, "selected genre is = "+ generos[position], Toast.LENGTH_SHORT).show()
                chosenGenre = generos[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }


        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        spotify = sharedPreferences.getString("spotify", null)
        youtube = sharedPreferences.getString("youtube", null)
        soundCloud = sharedPreferences.getString("soundCloud", null)
        deezer = sharedPreferences.getString("deezer", null)
        amazon = sharedPreferences.getString("amazonMusic", null)
        apple = sharedPreferences.getString("appleMusic", null)
        linkPerfil = sharedPreferences.getString("linkPerfil", null)
        linkMusica = sharedPreferences.getString("linkMusica", null)
        imagem = sharedPreferences.getString("imagem", null)
        id = sharedPreferences.getInt("id", 0)

        binding.btnCadastroMusica.setOnClickListener {
            if (binding.etTituloMusica.text.toString().isEmpty()){
                binding.etTituloMusica.error = "Por favor insira o título da música"
                return@setOnClickListener
            }
            if (binding.etTituloAlbum.text.toString().isEmpty()){
                binding.etTituloAlbum.error = "Por favor insira o título do álbum"
                return@setOnClickListener
            }
            if (binding.etCompositor.text.toString().isEmpty()){
                binding.etCompositor.error = "Por favor insira o nome do compositor"
                return@setOnClickListener
            }

            if (binding.etLyrics.text.toString().isEmpty()|| binding.etLyrics.text.toString().length > maxCharacters){
                binding.etLyrics.error = "Por favor insira a lyrics da música"
                return@setOnClickListener
            }

            val minhaUri: Uri? = Uri.parse(imagem)
            val imagem: Uri? = minhaUri
            if (imagem != null) {
                val bitmap = if (Build.VERSION.SDK_INT < 28) {
                    MediaStore.Images.Media.getBitmap(contentResolver, imagem)
                } else {
                    val source = ImageDecoder.createSource(contentResolver, imagem)
                    ImageDecoder.decodeBitmap(source)
                }

                val imageBody = criarImageBody(bitmap)
                val novoEchoBody = criarNovoEchoBody()

                enviarDadosParaServidor(imageBody, novoEchoBody)
            }
        }

    }
    private fun criarImageBody(bitmap: Bitmap): MultipartBody.Part {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val imageByteArray = byteArrayOutputStream.toByteArray()

        return MultipartBody.Part.createFormData(
            "imagem",
            "imagem.jpg",
            imageByteArray.toRequestBody("image/*".toMediaTypeOrNull())
        )
    }
    private fun criarNovoEchoBody(): MultipartBody.Part {
        val tituloMusica = binding.etTituloMusica.text.toString()
        val tituloAlbum = binding.etTituloAlbum.text.toString()
        val nomeCompositor = binding.etCompositor.text.toString()
        val lyrics = binding.etLyrics.text.toString()


        val novoEcho = JSONObject().apply {
            put("artista", JSONObject().apply {
                put("idUsuario", id)
            })
            put("spotify", spotify)
            put("soundCloud", soundCloud)
            put("amazonMusic", amazon)
            put("otherPlataform1", linkPerfil)
            put("youtube", youtube)
            put("deezer", deezer)
            put("appleMusic", apple)
            put("otherLink1", linkMusica)
            put("tituloMusica", tituloMusica)
            put("tituloAlbum", tituloAlbum)
            put("compositor", nomeCompositor)
            put("genero", chosenGenre)
            put("lyrics", lyrics)
        }

        return MultipartBody.Part.createFormData(
            "novoEcho",
            null,
            novoEcho.toString().toRequestBody("multipart/form-data".toMediaTypeOrNull())
        )
    }
    private fun enviarDadosParaServidor(imageBody: MultipartBody.Part, novoEchoBody: MultipartBody.Part) {
        val clienteOkHttp = OkHttpClient()

        val request = Request.Builder()
            .url("https://vibrato.azurewebsites.net/echo")
            .post(MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addPart(imageBody)
                .addPart(novoEchoBody)
                .build())
            .build()

        clienteOkHttp.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val intent = Intent(this@cadastroMusica2, MusicianHome::class.java)
                startActivity(intent)
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e("API_ERROR", "Erro na requisição: ${e.message}", e)
                Toast.makeText(baseContext, "Erro na requisição: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}