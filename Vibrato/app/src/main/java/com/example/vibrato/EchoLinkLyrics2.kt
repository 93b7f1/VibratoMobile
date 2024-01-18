package com.example.vibrato

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.RenderEffect
import android.graphics.Shader
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.Base64
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.example.vibrato.databinding.ActivityEchoLinkLyrics2Binding
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class EchoLinkLyrics2 : AppCompatActivity() {
    val binding by lazy {
        ActivityEchoLinkLyrics2Binding.inflate(layoutInflater)
    }

    private var echoId: Int = 0
    private var idEcho: Int = 0
    private var musicPhoto: String? = null
    private var musicTitleEcho: String? = null
    private var artistName: String? = null
    private var modifiedLyrics: String? = null
    private var artistProfilePic: String? = null
    private var album: String? = null
    private var idMusicSpotify: String? = null
    private var mediaPlayer: MediaPlayer? = null
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }

    private fun applyBlur(imageView: ImageView, radius: Float,urlImagem:String) {
        val imageUrl = urlImagem
        Glide.with(this@EchoLinkLyrics2)
            .asBitmap()
            .load(imageUrl)
            .into(object : SimpleTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    val rs = RenderScript.create(this@EchoLinkLyrics2)
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
        idEcho = sharedPreferences.getInt("idEchoo", 0)
        musicPhoto = sharedPreferences.getString("musicPhoto", null)
        musicTitleEcho = sharedPreferences.getString("musicTitleEcho", null)
        artistName = sharedPreferences.getString("artistName", null)
        modifiedLyrics = sharedPreferences.getString("lyrics", null)
        artistProfilePic = sharedPreferences.getString("artistProfilePic", null)
        album = sharedPreferences.getString("album", null)
        idMusicSpotify = sharedPreferences.getString("idMusicSpotify", null)

        binding.tvMusicTitleLyrics.text = musicTitleEcho
        binding.musicTitle.text = musicTitleEcho
        binding.lyrics.text = modifiedLyrics
        binding.musicTitleLyrics.text = musicTitleEcho
        binding.albumTitleLyrics.text = album
        binding.tvArtistNameLyrics.text = artistName

        val imagemUrlProfile = "https://vibratosimages.blob.core.windows.net/imagens/$artistProfilePic"
        val imagemUrl = "https://vibratosimages.blob.core.windows.net/imagens/$musicPhoto"

        val imagee = findViewById<ImageView>(R.id.image_background)
        applyBlur(imagee, 20f,"https://vibratosimages.blob.core.windows.net/imagens/${musicPhoto}")

        Glide.with(this@EchoLinkLyrics2)
            .load(imagemUrl)
            .into(binding.ivMusicImage)

        Glide.with(this@EchoLinkLyrics2)
            .load(imagemUrl)
            .into(binding.ivFotoMusicaLyrics)


        Glide.with(this@EchoLinkLyrics2)
            .load(imagemUrlProfile)
            .into(binding.artistImageLyrics)

        binding.ivStartPreviewLyrics.setOnClickListener {
            fetchSpotifyData(idMusicSpotify)
        }

        binding.artistImageLyrics.setOnClickListener {
            mediaPlayer?.pause()
            val intent = Intent(this, PerfilArtista2::class.java)
            val sharedPreferences2 = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences2.edit()
            editor.putString("echoAccess4", "echoAccess4")
            editor.apply()
            startActivity(intent)
        }
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
