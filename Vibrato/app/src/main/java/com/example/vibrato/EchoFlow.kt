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
import okhttp3.*



class EchoFlow : AppCompatActivity() {
    private var echoId: Int = 0
    private var initialY: Float = 0f
    private var previousY: Float = 0f
    private lateinit var imageEcho: ImageView
    private var imageTranslationY: Float = 0f
    private var mediaPlayer: MediaPlayer? = null
    private var currentPreviewUrl: String? = null
    private lateinit var textView: TextView
    private lateinit var artImage: ImageView
    private lateinit var artistName: TextView
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
        textView = findViewById(R.id.echoname)
        artImage = findViewById(R.id.artmagem)
        artistName = findViewById(R.id.artistname)

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

                    imageEcho.translationY += deltaY
                    imageTranslationY += deltaY

                    previousY = newY
                    true
                }

                MotionEvent.ACTION_UP -> {
                    val finalY = event.y
                    val deltaY = finalY - initialY
                    val minDeltaY = 500

                    if (deltaY < -minDeltaY) {
                        echoId++
                        updateEchoValues(echoId)
                    } else {
                        imageEcho.animate().translationY(0f).setDuration(500).start()
                        imageTranslationY = 0f
                    }
                    true
                }

                else -> false
            }
        }

        updateEchoValues(echoId)

    }

    private fun updateEchoValues(newEchoId: Int) {
        val retrofitClient = Conexao.getRetrofitInstance("/echo")
        val endpoint = retrofitClient.create(Endpoint::class.java)

        imageEcho.translationY = 0f
        imageTranslationY = 0f

        endpoint.listarEchoes().enqueue(object : Callback<List<Map<String, Any>>?> {
            override fun onFailure(call: Call<List<Map<String, Any>>?>, t: Throwable) {
                Toast.makeText(baseContext, t.message, Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(
                call: Call<List<Map<String, Any>>?>,
                response: Response<List<Map<String, Any>>?>
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

                        textView.text = selectedEcho.tituloMusica
                        artistName.text = selectedEcho.artista.nome

                        val imageUrl = "https://vibratosimages.blob.core.windows.net/imagens/${selectedEcho.blob}"
                        Glide.with(this@EchoFlow)
                            .load(imageUrl)
                            .centerCrop()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(imageEcho)

                        val imageUrl2 = "https://vibratosimages.blob.core.windows.net/imagens/${selectedEcho.artista.blob}"
                        Glide.with(this@EchoFlow)
                            .load(imageUrl2)
                            .centerCrop()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(artImage)
                        fetchSpotifyData(selectedEcho.otherLink1)
                    }

                } else {
                    Toast.makeText(baseContext, "Erro na resposta", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun fetchSpotifyData(trackId: String) {
        val clientId = "654f22146f75469296716116ef4fe9d9"
        val clientSecret = "4356007d93504cdbbb54d48861567992"

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

                                mediaPlayer?.release()
                                mediaPlayer = null

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

                                currentPreviewUrl = previewUrl
                            }
                        }
                    })
                }
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

                            imageEcho.translationY += deltaY
                            imageTranslationY += deltaY

                            previousY = newY
                            true
                        }

                        MotionEvent.ACTION_UP -> {
                            val finalY = event.y
                            val deltaY = finalY - initialY
                            val minDeltaY = 100

                            if (deltaY < -minDeltaY) {
                                echoId++
                                updateEchoValues(echoId)
                            } else {
                                imageEcho.animate().translationY(0f).setDuration(500).start()
                                imageTranslationY = 0f

                                if (mediaPlayer?.isPlaying == true) {
                                    mediaPlayer?.pause()
                                } else {
                                    mediaPlayer?.start()
                                }
                            }
                            true
                        }

                        else -> false
                    }
                }
            }
        })
    }
}











