package com.example.vibrato

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.graphics.RenderEffect
import android.graphics.Shader
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.Base64
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.example.vibrato.databinding.ActivityEchoLinkBinding
import com.example.vibrato.databinding.ActivityMusicianHomeBinding
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

class EchoLink : AppCompatActivity() {
    private val binding by lazy {
        ActivityEchoLinkBinding.inflate(layoutInflater)
    }

    private var id: Int = 0
    private var idArtista: Int = 0
    private var username: String? = null
    private var tipoUsuario: String? = null
    private var echoId: Int = 0
    private var mediaPlayer: MediaPlayer? = null
    private var teste: String? = null
    private var album: String? = null
    private var musicPhoto: String? = null
    private var musicTitleEcho: String? = null
    private var artistName: String? = null
    private var artistProfilePic: String? = null
    private var spotify: String? = null
    private var youtube: String? = null
    private var deezer: String? = null
    private var amazon: String? = null
    private var apple: String? = null
    private var soundCloud: String? = null
    private var lyrics: String? = null
    private var idMusicSpotify: String? = null
    private var modifiedLyrics: String? = null
    private var outroId: Int = 0

    override fun onBackPressed() {
        super.onBackPressed()
        mediaPlayer?.pause()
        val intent = Intent(this, EchoFlow::class.java)
        startActivity(intent)
        finish()
    }

    fun imageViewEchoLinkClicked() {
        onPause()
        val intent = Intent(this, PerfilArtista2::class.java)
        startActivity(intent)
        finish()
    }

    private fun applyBlur(imageView: ImageView, radius: Float, urlImagem: String) {
        lifecycleScope.launch {
            val imageUrl = urlImagem
            try {
                val resource = withContext(Dispatchers.IO) {
                    Glide.with(this@EchoLink)
                        .asBitmap()
                        .load(imageUrl)
                        .submit()
                        .get()
                }

                val rs = RenderScript.create(this@EchoLink)
                val input = Allocation.createFromBitmap(rs, resource)
                val output = Allocation.createTyped(rs, input.type)

                val blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
                blurScript.setInput(input)
                blurScript.setRadius(radius)
                blurScript.forEach(output)

                output.copyTo(resource)

                imageView.setImageBitmap(resource)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        id = sharedPreferences.getInt("id", 0)
        username = sharedPreferences.getString("username", null)
        tipoUsuario = sharedPreferences.getString("tipoUsuario", null)
        outroId = sharedPreferences.getInt("outroId", 0)

        if (outroId!=0){
            lifecycleScope.launch {
                buscarEchoPorId(outroId)
            }
        }else{
            echoId = intent.getIntExtra("echoId", 0)
            lifecycleScope.launch {
                buscarEchoPorId(echoId)
                val sharedPreferences2 = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                val editor = sharedPreferences2.edit()
                editor.putInt("outroId", echoId)
                editor.apply()

            }
        }
//        if (!intent.extras!!.isEmpty) {
//        }



        binding.linkSpotify.setOnClickListener {
            val urlSpotify = spotify
            if (urlSpotify != null) {
                openExternalLink(urlSpotify)
                mediaPlayer?.pause()
            }
        }

        binding.linkYoutube.setOnClickListener {
            val urlYoutube = youtube
            if (urlYoutube != null) {
                openExternalLink(urlYoutube)
                mediaPlayer?.pause()
            }
        }

        binding.linkAmazon.setOnClickListener {
            val urlAmazon = amazon
            if (urlAmazon != null) {
                openExternalLink(urlAmazon)
                mediaPlayer?.pause()
            }
        }

        binding.linkApple.setOnClickListener {
            val urlApple = apple
            if (urlApple != null) {
                openExternalLink(urlApple)
                mediaPlayer?.pause()
            }
        }

        binding.linkDeezer.setOnClickListener {
            val urlDeezer = deezer
            if (urlDeezer != null) {
                openExternalLink(urlDeezer)
                mediaPlayer?.pause()
            }
        }

        binding.linkSoundcloud.setOnClickListener {
            val urlSoundCloud = soundCloud
            if (urlSoundCloud != null) {
                openExternalLink(urlSoundCloud)
                mediaPlayer?.pause()
            }
        }

        binding.ivStartPreview.setOnClickListener {
            fetchSpotifyData(teste)
        }

        binding.tvLyrics.setOnClickListener {
            mediaPlayer?.pause()
            val intent = Intent(this@EchoLink, EchoLinkLyrics::class.java)
            intent.putExtra("echoId", echoId.toString().toInt())
            startActivity(intent)
        }

        binding.ivArtistPhoto.setOnClickListener {
            mediaPlayer?.pause()
            val intent = Intent(this, PerfilArtista2::class.java)
            val sharedPreferences2 = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences2.edit()
            editor.putInt("idEchoChosen", idArtista.toString().toInt())
            editor.putString("usernameArtist", artistName.toString())
            editor.putString("echoAccess", "echoAccess")
            editor.apply()
            startActivity(intent)
        }
    }

    private suspend fun buscarEchoPorId(id: Int) {
        val retrofitClient = Conexao.getRetrofitInstance("/echo/echolink/$id")
        val endpoint = retrofitClient.create(Endpoint::class.java)

        try {
            val response = withContext(Dispatchers.IO) {
                endpoint.getEcholink(id).execute()
            }

            if (response.isSuccessful) {
                val usuario: Echo? = response.body()
                usuario?.let {
                    musicPhoto = it.blob.toString()
                    musicTitleEcho = it.tituloMusica.toString()
                    artistName = it.artista.username.toString()
                    artistProfilePic = it.artista.blob.toString()
                    spotify = it.spotify.toString()
                    youtube = it.youtube.toString()
                    deezer = it.deezer.toString()
                    amazon = it.amazonMusic.toString()
                    apple = it.appleMusic.toString()
                    soundCloud = it.soundCloud.toString()
                    teste = it.otherLink1
                    album = it.tituloAlbum.toString()
                    idArtista = it.artista.idUsuario
                    lyrics = it.lyrics
                    idMusicSpotify = it.otherLink1.toString()

                   val regexPattern = """<br\s{0,3}/>""".toRegex()
                    modifiedLyrics = (lyrics as? String)?.let { regexPattern.replace(it, "\n") } ?: ""

                    val imagemUrlProfile = "https://vibratosimages.blob.core.windows.net/imagens/$artistProfilePic"
                    val imagemUrl = "https://vibratosimages.blob.core.windows.net/imagens/$musicPhoto"

                    Glide.with(this@EchoLink)
                        .load(if (artistProfilePic.isNullOrBlank()) R.drawable.semimagem else imagemUrlProfile)
                        .into(binding.ivArtistPhoto)

                    Glide.with(this@EchoLink)
                        .load(imagemUrl)
                        .into(binding.ivAlbumPhoto)

                    Glide.with(this@EchoLink)
                        .load(imagemUrl)
                        .into(binding.ivAlbumPhotoPreview)

                    applyBlur(binding.imageBackground, 20f, imagemUrl)

                    if (spotify != "") {
                        binding.llSpotify.visibility = View.VISIBLE
                    }
                    if (youtube != "") {
                        binding.llYoutube.visibility = View.VISIBLE
                    }
                    if (amazon != "") {
                        binding.llAmazon.visibility = View.VISIBLE
                    }
                    if (apple != "") {
                        binding.llApple.visibility = View.VISIBLE
                    }
                    if (deezer != "") {
                        binding.linkDeezer.visibility = View.VISIBLE
                    }
                    if (soundCloud != "") {
                        binding.llSoundcloud.visibility = View.VISIBLE
                    }

                    binding.tvMusicTitle.text = musicTitleEcho
                    binding.tvMusicTitlePreview.text = musicTitleEcho
                    binding.tvAlbumNamePreview.text = album
                    binding.tvArtistName.text = artistName
                    val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString("musicPhoto", musicPhoto)
                    editor.putString("musicTitleEcho", musicTitleEcho)
                    editor.putString("artistName", artistName)
                    editor.putString("lyrics", modifiedLyrics)
                    editor.putString("artistProfilePic", artistProfilePic)
                    editor.putString("album", album)
                    editor.putString("idMusicSpotify", idMusicSpotify)
                    editor.apply()
                }
            } else {
                val mensagemErro = "Erro na requisição: ${response.message()}"
            }
        } catch (e: Exception) {
            val mensagemFalha = "Falha na requisição: ${e.message}"
        }
    }

    private fun openExternalLink(url: String) {
        val openURL = Intent(Intent.ACTION_VIEW)
        openURL.data = Uri.parse(url)

        if (openURL.resolveActivity(packageManager) != null) {
            startActivity(openURL)
        } else {
            showToast("Não foi possível abrir a URL.")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun fetchSpotifyData(trackId: String?) {
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
                    val json = response.body?.string()
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
                                val trackJson = response.body?.string()
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
                            }
                        }
                    })
                }
            }
        })
    }
}
