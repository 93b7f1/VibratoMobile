package com.example.vibrato

import android.app.Activity
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
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.vibrato.databinding.ActivityCadastroMusicaBinding
import com.example.vibrato.databinding.ActivityPersonalizarBinding
import com.google.android.material.imageview.ShapeableImageView
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.IOException

class personalizar : AppCompatActivity() {
    val binding by lazy {
        ActivityPersonalizarBinding.inflate(layoutInflater)
    }
    private var imagem: String? =null
    private var username: String? = null
    private var tipoUsuario: String? = null
    private var x: String? = null
    private var instagram: String? = null
    private var spotify: String? = null
    private var soundcloud: String? = null
    private var genero: String? = null
    private var biografia: String? = null
    private var idUser: Int = 0
    private lateinit var linearArtistEchoes: LinearLayout
    private var selectedImageUri: Uri? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            handleImageResult(data)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        idUser = sharedPreferences.getInt("id", 0)
        username = sharedPreferences.getString("username", null)
        tipoUsuario = sharedPreferences.getString("tipoUsuario", null)
        linearArtistEchoes = findViewById(R.id.llArtistsEchoes)
        linearArtistEchoes.orientation = LinearLayout.HORIZONTAL

        getUsuarioPorId(idUser)


        binding.btnAlterarFotoPerfil.setOnClickListener {
            openGallery()
        }
        getArtistEchoes()

        binding.btnAtualizarPerfil.setOnClickListener {
            val imagem: Uri? = selectedImageUri
            if (imagem != null) {
                val bitmap = if (Build.VERSION.SDK_INT < 28) {
                    MediaStore.Images.Media.getBitmap(contentResolver, imagem)
                } else {
                    val source = ImageDecoder.createSource(contentResolver, imagem)
                    ImageDecoder.decodeBitmap(source)
                }

                val imageBody = criarImageBody(bitmap)
                val updateUser = updateUserBody()

                enviarDadosParaServidor(imageBody, updateUser)
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
    private fun enviarDadosParaServidor(imageBody: MultipartBody.Part, updateUser: MultipartBody.Part) {
        val clienteOkHttp = OkHttpClient()

        val request = Request.Builder()
            .url("https://vibrato.azurewebsites.net/usuarios")
            .patch(MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addPart(imageBody)
                .addPart(updateUser)
                .build())
            .build()

        clienteOkHttp.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val intent = Intent(this@personalizar, MusicianHome::class.java)
                startActivity(intent)
            }

            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("API_ERROR", "Erro na requisição: ${e.message}", e)
                Toast.makeText(baseContext, "Erro na requisição: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun updateUserBody(): MultipartBody.Part {
        val nomeUsuario = binding.etNomeUsuario.text.toString()
        val xSocialMedia =  binding.etX.text.toString()
        val instaMedia = binding.etInstagram.toString()
        val spotifyMedia = binding.etSpotifyPersonalizar.toString()
        val soundCloudMedia = binding.etSoundcloudPersonalizar.toString()
        val generoMusica = binding.etGenero.toString()
        val biografiaUsuario = binding.etBiografia.toString()

        val usuario = JSONObject().apply {
            put("idUsuario", 36)
            put("spotify", spotifyMedia)
            put("soundcloud", soundCloudMedia)
            put("twitter", xSocialMedia)
            put("username", nomeUsuario)
            put("instagram", instaMedia)
            put("genero", generoMusica)
            put("biografia", biografiaUsuario)
        }

        return MultipartBody.Part.createFormData(
            "usuario",
            null,
            usuario.toString().toRequestBody("multipart/form-data".toMediaTypeOrNull())
        )
    }
    private fun handleImageResult(data: Intent?) {
        selectedImageUri = data?.data
        // Handle the selected image URI as needed
        // Update your ImageView or perform any other actions

        if (selectedImageUri != null) {
            // Use Glide to load the image into the ShapeableImageView
            Glide.with(this)
                .load(selectedImageUri)
                .into(binding.ivFotoPerfilPersonalizar)

        }

    }

    private fun getUsuarioPorId(idUsuario: Int) {
        val retrofitClient = Conexao.getRetrofitInstance("/usuario/perfil/${idUsuario}")
        val endpoint = retrofitClient.create(Endpoint::class.java)
        val call = endpoint.getUsuario(idUsuario)
        call.enqueue(object : Callback<Artista> {
            override fun onResponse(call: Call<Artista>, response: Response<Artista>) {
                if (response.isSuccessful) {
                    val usuario: Artista? = response.body()
                    usuario?.let {
                        imagem = it.blob
                        username = it.username
                        x = it.twitter
                        instagram = it.instagram
                        spotify = it.spotify
                        soundcloud = it.soundcloud
                        genero = it.genero
                        biografia = it.biografia
                        tipoUsuario = it.tipoUsuario

                        val imagemUrl = "https://vibratosimages.blob.core.windows.net/imagens/${imagem}"

                        if (!imagemUrl.isNullOrBlank()) {
                            Glide.with(this@personalizar)
                                .load(imagemUrl)
                                .into(binding.ivFotoPerfilPersonalizar)
                        }
                        binding.etNomeUsuario.setHint(username)
                        binding.etGenero.setHint(genero)
                        binding.etBiografia.setHint(biografia)
                    }
                } else {
                    val mensagemErro = "Erro na requisição: ${response.message()}"
                }
            }

            override fun onFailure(call: Call<Artista>, t: Throwable) {
                val mensagemFalha = "Falha na requisição: ${t.message}"
            }
        })

    }

    private fun getArtistEchoes() {
        val retrofitClient = Conexao.getRetrofitInstance("/echo")
        val endpoint = retrofitClient.create(Endpoint::class.java)


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
                            if (echo.artista.idUsuario == idUser){
                                echoesList.add(echo)
                            }
                        }

                        linearArtistEchoes.removeAllViews()
                        for (selectedEcho in echoesList){
                            val childLayout = LinearLayout(this@personalizar)
                            childLayout.layoutParams =
                                LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                )
                            childLayout.orientation = LinearLayout.HORIZONTAL
                            linearArtistEchoes.addView(childLayout)

                            val imageView = ImageView(this@personalizar)
                            val layoutParams = LinearLayout.LayoutParams(
                                150.dpToPx(),
                                100.dpToPx()
                            )
                            layoutParams.leftMargin = 15.dpToPx()
                            imageView.layoutParams = layoutParams
                            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                            childLayout.addView(imageView)

                            val imageUrl =
                                "https://vibratosimages.blob.core.windows.net/imagens/${selectedEcho.blob}"
                            Glide.with(this@personalizar)
                                .load(imageUrl)
                                .centerCrop()
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(imageView)
                        }
                    }

                } else {
                    Toast.makeText(baseContext, "Erro na resposta", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
    private fun Int.dpToPx(): Int {
        val scale = resources.displayMetrics.density
        return (this * scale + 0.5f).toInt()
    }
    private fun openGallery() {
        val pickImg = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        pickImage.launch(pickImg)
    }
}