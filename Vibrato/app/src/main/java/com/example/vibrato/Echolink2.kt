package com.example.vibrato

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.RenderEffect
import android.graphics.Shader
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.Base64
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.example.vibrato.databinding.ActivityEcholink2Binding
import kotlinx.coroutines.launch
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class Echolink2 : AppCompatActivity() {
    val binding by lazy {
        ActivityEcholink2Binding.inflate(layoutInflater)
    }

    private var id: Int = 0
    private var username: String? = null
    private var tipoUsuario: String? = null
    private var echoId: Int = 0
    private var idArtista: Int = 0
    private var idEcho: Int = 0
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
    private var outroId2: Int = 0

    override fun onBackPressed() {
        super.onBackPressed()
        mediaPlayer?.pause()
        val intent = Intent(this, Explore::class.java)
        startActivity(intent)
        finish()
    }
    private fun applyBlur(imageView: ImageView, radius: Float,urlImagem:String) {
        val imageUrl = urlImagem
        Glide.with(this@Echolink2)
            .asBitmap()
            .load(imageUrl)
            .into(object : SimpleTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    val rs = RenderScript.create(this@Echolink2)
                    val input = Allocation.createFromBitmap(rs, resource)
                    val output = Allocation.createTyped(rs, input.type)

                    val blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
                    blurScript.setInput(input)
                    blurScript.setRadius(radius)
                    blurScript.forEach(output)

                    output.copyTo(resource)

                    imageView.setImageBitmap(resource)
                }
            })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        id = sharedPreferences.getInt("id", 0)
        idEcho = sharedPreferences.getInt("idEchoo", 0)
        tipoUsuario = sharedPreferences.getString("tipoUsuario", null)
        outroId2 = sharedPreferences.getInt("outroId", 0)
        val imagee = findViewById<ImageView>(R.id.image_background)
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.S) {
            imagee.setRenderEffect(RenderEffect.createBlurEffect(20f, 20f, Shader.TileMode.MIRROR));
        }


        if (outroId2!=0){
            lifecycleScope.launch {
                buscarEchoPorId(outroId2)

            }
        }else{
            echoId = intent.getIntExtra("echoId", 0)
            lifecycleScope.launch {
                buscarEchoPorId(idEcho)
                val sharedPreferences2 = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                val editor = sharedPreferences2.edit()
                editor.putInt("outroId2", idEcho)
                editor.apply()
            }
        }
        binding.linkSpotify.setOnClickListener {
            val urlSpotify = spotify
            if (urlSpotify!=null){
                openExternalLink(urlSpotify)
                mediaPlayer?.pause()

            }
        }
        binding.linkYoutube.setOnClickListener {
            val urlYoutube = youtube
            if (urlYoutube!=null){
                openExternalLink(urlYoutube)
                mediaPlayer?.pause()

            }
        }
        binding.linkAmazon.setOnClickListener {
            val urlAmazon = amazon
            if (urlAmazon!= null){
                openExternalLink(urlAmazon)
                mediaPlayer?.pause()

            }
        }
        binding.linkApple.setOnClickListener {
            val urlApple = apple
            if (urlApple!= null){
                openExternalLink(urlApple)
                mediaPlayer?.pause()

            }
        }
        binding.linkDeezer.setOnClickListener {
            val urlDeezer = deezer
            if (urlDeezer!=null){
                openExternalLink(urlDeezer)
                mediaPlayer?.pause()

            }
        }
        binding.linkSoundcloud.setOnClickListener {
            val urlSoundCloud = soundCloud
            if (urlSoundCloud != null){
                openExternalLink(urlSoundCloud)
                mediaPlayer?.pause()

            }
        }

        binding.ivStartPreview.setOnClickListener {
            fetchSpotifyData(teste)

        }

        binding.tvLyrics.setOnClickListener{
            mediaPlayer?.pause()
            val intent = Intent(this@Echolink2, EchoLinkLyrics2::class.java)
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
            editor.putString("echoAccess3", "echoAccess3")
            editor.apply()
            startActivity(intent)
        }


    }

    private fun buscarEchoPorId(id: Int) {
        val retrofitClient = Conexao.getRetrofitInstance("/echo/echolink/${id}")
        val endpoint = retrofitClient.create(Endpoint::class.java)
        val call = endpoint.getEcholink(id)
        call.enqueue(object : Callback<Echo> {
            override fun onResponse(call: Call<Echo>, response: Response<Echo>) {
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

                        val imagemUrl =
                            "https://vibratosimages.blob.core.windows.net/imagens/${musicPhoto}"
                        if (!imagemUrl.isNullOrBlank()) {
                            Glide.with(this@Echolink2)
                                .load(imagemUrl)
                                .into(binding.ivAlbumPhoto)
                        }
                        if (!imagemUrl.isNullOrBlank()) {
                            Glide.with(this@Echolink2)
                                .load(imagemUrl)
                                .into(binding.ivAlbumPhotoPreview)
                        }
                        if (!imagemUrl.isNullOrBlank()) {
                            val imagee = findViewById<ImageView>(R.id.image_background)
                            applyBlur(imagee, 20f,"https://vibratosimages.blob.core.windows.net/imagens/${musicPhoto}")

                        }
                        val imagemUrlProfile =
                            "https://vibratosimages.blob.core.windows.net/imagens/${artistProfilePic}"
                        if (!imagemUrlProfile.isNullOrBlank()) {
                            Glide.with(this@Echolink2)
                                .load(imagemUrlProfile)
                                .into(binding.ivArtistPhoto)
                        }
                        if (spotify != "") {
                            // Both components are present, make the LinearLayout visible
                            binding.llSpotify.visibility = View.VISIBLE
                        }
                        if (youtube != ""){
                            binding.llYoutube.visibility = View.VISIBLE
                        }
                        if (amazon != "") {
                            // Both components are present, make the LinearLayout visible
                            binding.llAmazon.visibility = View.VISIBLE
                        }
                        if (apple != "") {
                            // Both components are present, make the LinearLayout visible
                            binding.llApple.visibility = View.VISIBLE
                        }
                        if (deezer != "") {
                            // Both components are present, make the LinearLayout visible
                            binding.linkDeezer.visibility = View.VISIBLE
                        }
                        if (soundCloud != "") {
                            // Both components are present, make the LinearLayout visible
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
                        editor.putInt("idEchoChosen", idArtista.toString().toInt())
                        editor.apply()
                    }
                } else {
                    val mensagemErro = "Erro na requisição: ${response.message()}"
                }
            }

            override fun onFailure(call: Call<Echo>, t: Throwable) {
                val mensagemFalha = "Falha na requisição: ${t.message}"
            }
        })

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
