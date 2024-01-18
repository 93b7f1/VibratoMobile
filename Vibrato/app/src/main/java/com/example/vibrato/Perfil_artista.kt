package com.example.vibrato

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.example.vibrato.databinding.ActivityMusicianHomeBinding
import com.example.vibrato.databinding.ActivityPerfilArtistaBinding
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Perfil_artista : AppCompatActivity() {

    val binding by lazy {
        ActivityPerfilArtistaBinding.inflate(layoutInflater)
    }

    private var id: Int = 0
    private var username: String? = null
    private var tipoUsuario: String? = null
    private var imagem: String? = null
    private var biografia: String? = null
    private lateinit var linearArtistEchoes: LinearLayout
    private fun applyBlur(imageView: ImageView, radius: Float,urlImagem:String) {
        val imageUrl = urlImagem
        Glide.with(this@Perfil_artista)
            .asBitmap()
            .load(imageUrl)
            .into(object : SimpleTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    val rs = RenderScript.create(this@Perfil_artista)
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


        linearArtistEchoes = findViewById(R.id.ultimasMusicas)
        linearArtistEchoes.orientation = LinearLayout.HORIZONTAL

        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        id = sharedPreferences.getInt("id", 0)
        username = sharedPreferences.getString("username", null)
        tipoUsuario = sharedPreferences.getString("tipoUsuario", null)

        binding.nomeUsuario.text = username
        getUsuarioPorId(id)
        getArtistEchoes(id)

        binding.btEditarPerfil.setOnClickListener{
            val intent = Intent(this@Perfil_artista, personalizar::class.java)
            startActivity(intent)
        }


    }

    private fun getUsuarioPorId(idUsuario: Int) {
        val retrofitClient = Conexao.getRetrofitInstance("/usuario/perfil/${id}")
        val endpoint = retrofitClient.create(Endpoint::class.java)
        val call = endpoint.getUsuario(idUsuario)
        call.enqueue(object : Callback<Artista> {
            override fun onResponse(call: Call<Artista>, response: Response<Artista>) {
                if (response.isSuccessful) {
                    val usuario: Artista? = response.body()
                    usuario?.let {
                        imagem = it.blob
                        biografia = it.biografia

                        val imagemUrl = "https://vibratosimages.blob.core.windows.net/imagens/${imagem}"
                        if (!imagemUrl.isNullOrBlank()) {
                            Glide.with(this@Perfil_artista)
                                .load(imagemUrl)
                                .into(binding.imagemPerfil)
                        }
                        if (!imagemUrl.isNullOrBlank()) {
                            val imagee = findViewById<ImageView>(R.id.imagemFundo)
                            applyBlur(imagee, 20f,"https://vibratosimages.blob.core.windows.net/imagens/${imagem}")
                        }
                        if(biografia.isNullOrBlank()){
                            binding.descricao.text = "Sem biografia"
                        }else{
                            binding.descricao.text = biografia
                        }

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
    private fun getArtistEchoes(id : Int) {
        val retrofitClient = Conexao.getRetrofitInstance("/echo/last3/${id}")
        val endpoint = retrofitClient.create(Endpoint::class.java)


        endpoint.listarEchoesArtista(id).enqueue(object : Callback<List<Map<String, Any>>?> {
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
                            if (echo.artista.idUsuario == id){
                                echoesList.add(echo)
                            }
                        }

                        linearArtistEchoes.removeAllViews()
                        for (selectedEcho in echoesList){
                            val childLayout = LinearLayout(this@Perfil_artista)
                            childLayout.layoutParams =
                                LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                )
                            childLayout.orientation = LinearLayout.HORIZONTAL
                            linearArtistEchoes.addView(childLayout)

                            val imageView = ImageView(this@Perfil_artista)
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
                            Glide.with(this@Perfil_artista)
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
}