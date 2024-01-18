package com.example.vibrato

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.view.MotionEvent
import android.view.animation.AccelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private var echoId2: Int = 0
    private var initialY: Float = 0f
    private var previousY: Float = 0f
    private lateinit var imageEcho: ImageView
    private var imageTranslationY: Float = 0f
    private var mediaPlayer: MediaPlayer? = null
    private var currentPreviewUrl: String? = null
    private lateinit var textView: TextView
    private lateinit var artImage: ImageView
    private lateinit var artistName: TextView
    private var id: Int = 0
    private var username: String? = null
    private var tipoUsuario: String? = null
    private var echoIdChosen: Int = 0
    private var usernameArtist: String = ""
    private var echoesList: List<Echo> = emptyList()
    private var isLoading: Boolean = false

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MusicianHome::class.java)
        startActivity(intent)
        finish()
    }
    override fun onPause() {
        super.onPause()
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        id = sharedPreferences.getInt("id", 0)
        username = sharedPreferences.getString("username", null)
        tipoUsuario = sharedPreferences.getString("tipoUsuario", null)
    }

    fun imageViewHomeClicked() {
        onPause()
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        id = sharedPreferences.getInt("id", 0)
        username = sharedPreferences.getString("username", null)
        tipoUsuario = sharedPreferences.getString("tipoUsuario", null)
        if (tipoUsuario == "Ouvinte") {
            val intent = Intent(this, UserHome::class.java)
            startActivity(intent)
        } else {
            val intent2 = Intent(this, MusicianHome::class.java)
            startActivity(intent2)
        }
        finish()
    }

    fun pegarDados(idArtista: Int, usernameArtista: String) {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("idEchoChosen", idArtista.toString().toInt())
        editor.putString("usernameArtist", usernameArtista)
        editor.apply()
    }

    fun imageViewArtistClicked() {
        onPause()
        val intent = Intent(this, PerfilArtista2::class.java)
        pegarDados(echoIdChosen, usernameArtist)
        startActivity(intent)
        finish()
    }

    fun imageViewEchoLinkClicked() {
        onPause()
        val intent = Intent(this, EchoLink::class.java)
        intent.putExtra("echoId", echoId2.toString().toInt())
        startActivity(intent)
        finish()
    }
    private fun enableControls() {
        disableControls(false)
    }

    private fun disableControls(disable: Boolean = true) {
        findViewById<ImageView>(R.id.btn_home).isEnabled = !disable
        findViewById<ImageView>(R.id.iv_echolink_share).isEnabled = !disable
        findViewById<ImageView>(R.id.artmagem).isEnabled = !disable
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_echo_flow)

        val echolink = findViewById<ImageView>(R.id.iv_echolink_share)
        val home = findViewById<ImageView>(R.id.btn_home)
        val artist = findViewById<ImageView>(R.id.artmagem)

        val sharedPreferences2 = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences2.edit()
        editor.putInt("outroId", 0)
        editor.apply()

        val imageView: ImageView = findViewById(R.id.loadingflow)
        Glide.with(this)
            .asGif()
            .load(R.drawable.loadingflow)
            .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.DATA))
            .into(imageView)

        home.setOnClickListener {
            mediaPlayer?.pause()
            imageViewHomeClicked()
        }
        echolink.setOnClickListener {
            mediaPlayer?.pause()
            imageViewEchoLinkClicked()
        }
        artist.setOnClickListener {
            mediaPlayer?.pause()
            imageViewArtistClicked()
        }

        echoId = intent.getIntExtra("echoId", 0)

        imageEcho = findViewById(R.id.imageEcho)
        textView = findViewById(R.id.echoname)
        artImage = findViewById(R.id.artmagem)
        artistName = findViewById(R.id.artistname)



        lifecycleScope.launchWhenStarted {
            try {
                echoesList = getEchoesList()
                isLoading = true

                if (!echoesList.isNullOrEmpty()) {
                    val selectedEcho = getSelectedEcho(echoId, echoesList)
                    updateEchoValues(selectedEcho)
                } else {
                    Toast.makeText(baseContext, "Erro na resposta", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(baseContext, e.message, Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
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

                    val sensitivityFactor = 0.2f
                    imageEcho.translationY += deltaY * sensitivityFactor
                    imageTranslationY += deltaY

                    previousY = newY
                    true
                }

                MotionEvent.ACTION_UP -> {
                    val finalY = event.y
                    val deltaY = finalY - initialY
                    val minDeltaY = 300

                    lifecycleScope.launch {
                        if (deltaY < -minDeltaY) {
                            echoId++
                            mediaPlayer?.pause()
                            val selectedEcho = getSelectedEcho(echoId, echoesList)
                            updateEchoValues(selectedEcho)
                        } else if (deltaY > minDeltaY) {
                            echoId--

                            if (echoId < 0) {
                                echoId = 0
                            }
                            mediaPlayer?.pause()
                            val selectedEcho = getSelectedEcho(echoId, echoesList)
                            updateEchoValues(selectedEcho)
                        } else {

                            if (mediaPlayer?.isPlaying == true) {
                                mediaPlayer?.pause()
                            } else {
                                mediaPlayer?.start()
                            }
                        }

                        imageEcho.animate().translationY(0f).setDuration(300).start()
                        imageTranslationY = 0f
                    }

                    true
                }

                else -> false
            }
        }
    }


    private suspend fun getEchoesList(): List<Echo> {
        return withContext(Dispatchers.IO) {
            val retrofitClient = Conexao.getRetrofitInstance("/echo/desc-list")
            val endpoint = retrofitClient.create(Endpoint::class.java)

            try {
                val response = endpoint.listarEchoes2().execute()

                if (response.isSuccessful) {
                    response.body()?.map { echoMap ->
                        Gson().fromJson(Gson().toJson(echoMap), Echo::class.java)
                    }?.toMutableList()?.apply {
                        shuffle()
                    } ?: emptyList()
                } else {
                    emptyList()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    private fun getSelectedEcho(newEchoId: Int, echoesList: List<Echo>): Echo {
        val echoIndex = newEchoId % echoesList.size
        return echoesList[echoIndex]
    }
    private suspend fun fetchSpotifyData(trackId: String) {
        val clientId = "654f22146f75469296716116ef4fe9d9"
        val clientSecret = "4356007d93504cdbbb54d48861567992"

        try {
            val token = fetchSpotifyToken(clientId, clientSecret)
            val previewUrl = fetchSpotifyPreviewUrl(token, trackId)

            mediaPlayer?.release()
            mediaPlayer = MediaPlayer()
            mediaPlayer?.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )

            mediaPlayer?.setDataSource(previewUrl)
            mediaPlayer?.prepare()

            mediaPlayer?.start()

            currentPreviewUrl = previewUrl
        } catch (e: Exception) {
            e.printStackTrace()
            mediaPlayer?.pause()
        }
    }

    private suspend fun updateEchoValues(selectedEcho: Echo) {
        withContext(Dispatchers.Main) {
            imageEcho.translationY = 0f
            imageTranslationY = 0f

            textView.text = selectedEcho.tituloMusica
            artistName.text = selectedEcho.artista.username

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

            mediaPlayer?.setOnCompletionListener {
                mediaPlayer?.seekTo(0)
                mediaPlayer?.start()
            }

            echoId2 = selectedEcho.idEcho
            echoIdChosen = selectedEcho.artista.idUsuario
            usernameArtist = selectedEcho.artista.username

            pegarDados(echoIdChosen, usernameArtist)
            enableControls()
        }
    }

    private suspend fun fetchSpotifyToken(clientId: String, clientSecret: String): String {
        return withContext(Dispatchers.IO) {
            val client = OkHttpClient()
            val requestBody = FormBody.Builder()
                .add("grant_type", "client_credentials")
                .build()

            val authHeader = "Basic " + Base64.encodeToString(
                "$clientId:$clientSecret".toByteArray(),
                Base64.NO_WRAP
            )

            val tokenRequest = Request.Builder()
                .url("https://accounts.spotify.com/api/token")
                .post(requestBody)
                .addHeader("Authorization", authHeader)
                .build()

            val response = client.newCall(tokenRequest).execute()
            if (!response.isSuccessful) {
                throw IOException("Failed to get Spotify token")
            }

            val json = response.body?.string()
            return@withContext JSONObject(json).getString("access_token")
        }
    }

    private suspend fun fetchSpotifyPreviewUrl(token: String, trackId: String): String {
        return withContext(Dispatchers.IO) {
            val client = OkHttpClient()
            val trackUrl = "https://api.spotify.com/v1/tracks/$trackId"
            val trackRequest = Request.Builder()
                .url(trackUrl)
                .addHeader("Authorization", "Bearer $token")
                .build()

            val response = client.newCall(trackRequest).execute()
            if (!response.isSuccessful) {
                throw IOException("Failed to get Spotify track data")
            }

            val trackJson = response.body?.string()
            return@withContext JSONObject(trackJson).getString("preview_url")
        }
    }

}