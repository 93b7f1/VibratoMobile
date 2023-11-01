package com.example.vibrato

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.gson.Gson
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class EchoFlow : AppCompatActivity() {
    private var echoId: Int = 0
    private var initialY: Float = 0f
    private var previousY: Float = 0f
    private lateinit var imageEcho: ImageView
    private var imageTranslationY: Float = 0f
    private var mediaPlayer: MediaPlayer? = null

    fun imageViewClicked(view: View) {
        val authManager = AuthManager(this)

        if (authManager.getAuthToken() != null) {
            authManager.logout()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_echo_flow)

        echoId = intent.getIntExtra("echoId", 0)

        imageEcho = findViewById(R.id.imageEcho)

        imageEcho.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialY = event.y
                    previousY = initialY
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    val newY = event.y
                    val deltaY = newY - previousY

                    // Atualize a posição vertical da imagem
                    imageEcho.translationY += deltaY
                    imageTranslationY += deltaY // Atualize a variável de controle

                    previousY = newY
                    true
                }

                MotionEvent.ACTION_UP -> {
                    val finalY = event.y
                    val deltaY = finalY - initialY
                    val minDeltaY = 200

                    if (deltaY < -minDeltaY) {
                        // Atualize os valores e o echoId
                        echoId++
                        updateEchoValues(echoId)
                    } else {
                        // Animação para mover a imagem de volta para a posição de origem
                        imageEcho.animate().translationY(0f).setDuration(500).start()
                        imageTranslationY = 0f // Redefina a variável de controle
                    }
                    true
                }

                else -> false
            }
        }

        updateEchoValues(echoId)
    }

    private fun fetchSpotifyData() {
        val clientId = "654f22146f75469296716116ef4fe9d9"
        val clientSecret = "4356007d93504cdbbb54d48861567992"
        val trackId = "58ijfpSyfBjA5TK79fa914" // ID da faixa específica

        val client = OkHttpClient()
        val requestBody = FormBody.Builder()
            .add("grant_type", "client_credentials")
            .build()

        val authHeader = "Basic " + Base64.encodeToString("$clientId:$clientSecret".toByteArray(), Base64.NO_WRAP)

        val tokenRequest = Request.Builder()
            .url("https://accounts.spotify.com/api/token")
            .post(requestBody)
            .addHeader("Authorization", authHeader)
            .build()

        client.newCall(tokenRequest).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    val json = response.body()?.string()
                    val token = JSONObject(json).getString("access_token")

                    // Agora que você tem o token, pode fazer a chamada GET para obter informações da faixa
                    val trackUrl = "https://api.spotify.com/v1/tracks/$trackId"
                    val trackRequest = Request.Builder()
                        .url(trackUrl)
                        .addHeader("Authorization", "Bearer $token")
                        .build()

                    client.newCall(trackRequest).enqueue(object : okhttp3.Callback {
                        override fun onFailure(call: okhttp3.Call, e: IOException) {
                            e.printStackTrace()
                        }

                        override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                            if (response.isSuccessful) {
                                val trackJson = response.body()?.string()
                                val trackObject = JSONObject(trackJson)

                                val previewUrl = trackObject.getString("preview_url")

                                mediaPlayer = MediaPlayer()
                                mediaPlayer?.setAudioAttributes(
                                    AudioAttributes.Builder()
                                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                        .build()
                                )

                                try {
                                    mediaPlayer?.setDataSource(previewUrl)
                                    mediaPlayer?.prepare()
                                } catch (e: IOException) {
                                    e.printStackTrace()
                                }

                                mediaPlayer?.start()

                                mediaPlayer?.setOnCompletionListener {
                                    mediaPlayer?.seekTo(0)
                                    mediaPlayer?.start()
                                }
                            }
                        }
                    })
                }
            }
        })
    }

    private fun updateEchoValues(newEchoId: Int) {
        val retrofitClient = Conexao.getRetrofitInstance("/echo")
        val endpoint = retrofitClient.create(Endpoint::class.java)
        val callback = endpoint.listarEchoes()
        val textView: TextView = findViewById(R.id.echoname)
        val imageEchoes: ImageView = findViewById(R.id.imageEcho)
        val ArtistImage: ImageView = findViewById(R.id.artmagem)
        val artname: TextView = findViewById(R.id.artistname)
        imageEcho.translationY = 0f
        imageTranslationY = 0f
        callback.enqueue(object : Callback<List<Map<String, Any>>> {
            override fun onFailure(call: Call<List<Map<String, Any>>>, t: Throwable) {
                Toast.makeText(baseContext, t.message, Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(
                call: Call<List<Map<String, Any>>>,
                response: Response<List<Map<String, Any>>>
            ) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        val gson = Gson()
                        val echoesList = mutableListOf<Echo>()

                        for (echoMap in responseBody) {
                            val echoJson = gson.toJson(echoMap)
                            val echo = gson.fromJson(echoJson, Echo::class.java)
                            echoesList.add(echo)
                        }

                        val echoIndex = newEchoId % echoesList.size
                        val selectedEcho = echoesList[echoIndex]
                        val idEcho = selectedEcho.idEcho
                        val artista = selectedEcho.artista
                        val tituloMusica = selectedEcho.tituloMusica
                        val artistaImageee = selectedEcho.artista.blob
                        val namee = selectedEcho.artista.nome
                        val images = selectedEcho.blob
                        textView.text = tituloMusica
                        artname.text = namee

                        val imageUrl = "https://vibratosimages.blob.core.windows.net/imagens/$images"
                        Glide.with(this@EchoFlow)
                            .load(imageUrl)
                            .centerCrop()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(imageEchoes)

                        val imageUrl2 = "https://vibratosimages.blob.core.windows.net/imagens/$artistaImageee"
                        Glide.with(this@EchoFlow)
                            .load(imageUrl2)
                            .centerCrop()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(ArtistImage)
                    }
                } else {
                    Toast.makeText(baseContext, "Erro na resposta", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
    private fun updateEchoValues2(newEchoId: Int) {
        val retrofitClient = Conexao.getRetrofitInstance("/echo")
        val endpoint = retrofitClient.create(Endpoint::class.java)
        val callback = endpoint.listarEchoes()
        val textView: TextView = findViewById(R.id.echoname)
        val imageEchoes: ImageView = findViewById(R.id.imageEcho)
        val ArtistImage: ImageView = findViewById(R.id.artmagem)
        val artname: TextView = findViewById(R.id.artistname)
        imageEcho.translationY = 0f
        imageTranslationY = 0f
        callback.enqueue(object : Callback<List<Map<String, Any>>> {
            override fun onFailure(call: Call<List<Map<String, Any>>>, t: Throwable) {
                Toast.makeText(baseContext, t.message, Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(
                call: Call<List<Map<String, Any>>>,
                response: Response<List<Map<String, Any>>>
            ) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        val gson = Gson()
                        val echoesList = mutableListOf<Echo>()

                        for (echoMap in responseBody) {
                            val echoJson = gson.toJson(echoMap)
                            val echo = gson.fromJson(echoJson, Echo::class.java)
                            echoesList.add(echo)
                        }

                        val echoIndex = newEchoId % echoesList.size
                        val selectedEcho = echoesList[echoIndex]
                        val idEcho = selectedEcho.idEcho
                        val artista = selectedEcho.artista
                        val tituloMusica = selectedEcho.tituloMusica
                        val artistaImageee = selectedEcho.artista.blob
                        val namee = selectedEcho.artista.nome
                        val images = selectedEcho.blob
                        textView.text = tituloMusica
                        artname.text = namee

                        val imageUrl = "https://vibratosimages.blob.core.windows.net/imagens/$images"
                        Glide.with(this@EchoFlow)
                            .load(imageUrl)
                            .centerCrop()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(imageEchoes)

                        val imageUrl2 = "https://vibratosimages.blob.core.windows.net/imagens/$artistaImageee"
                        Glide.with(this@EchoFlow)
                            .load(imageUrl2)
                            .centerCrop()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(ArtistImage)
                    }
                } else {
                    Toast.makeText(baseContext, "Erro na resposta", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}













