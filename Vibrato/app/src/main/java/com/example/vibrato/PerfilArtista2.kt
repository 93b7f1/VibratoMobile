package com.example.vibrato

import android.content.Context
import android.content.Intent
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.renderscript.RenderScript
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.vibrato.databinding.ActivityPerfilArtista2Binding
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.ScriptIntrinsicBlur
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
class PerfilArtista2 : AppCompatActivity() {
    val binding by lazy {
        ActivityPerfilArtista2Binding.inflate(layoutInflater)
    }

    private var id: Int = 0
    private var idEcho: Int = 0
    private var username: String? = null
    private var tipoUsuario: String? = null
    private var imagem: String? = null
    private var usernameArtist: String?=null
    private var echoAccess:  String?=null
    private var echoAccess2: String?=null
    private var echoAccess3: String?=null
    private var echoAccess4: String?=null


    private var outroId: Int = 0

    private var biografia: String? = null
    private lateinit var linearArtistEchoes: LinearLayout
    private fun applyBlur(imageView: ImageView, radius: Float,urlImagem:String) {
        val imageUrl = urlImagem
        Glide.with(this@PerfilArtista2)
            .asBitmap()
            .load(imageUrl)
            .into(object : SimpleTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    val rs = RenderScript.create(this@PerfilArtista2)
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

        val imagee = findViewById<ImageView>(R.id.imagemFundo)
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.S) {
            imagee.setRenderEffect(RenderEffect.createBlurEffect(20f, 20f, Shader.TileMode.MIRROR));
        }

        linearArtistEchoes = findViewById(R.id.ultimasMusicas)
        linearArtistEchoes.orientation = LinearLayout.HORIZONTAL

        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        id = sharedPreferences.getInt("id", 0)
        username = sharedPreferences.getString("username", null)
        tipoUsuario = sharedPreferences.getString("tipoUsuario", null)
        idEcho = sharedPreferences.getInt("idEchoChosen", 0)
        usernameArtist = sharedPreferences.getString("usernameArtist", null)
        echoAccess = sharedPreferences.getString("echoAccess", null)
        echoAccess2 = sharedPreferences.getString("echoAccess2", null)
        echoAccess3 = sharedPreferences.getString("echoAccess3", null)
        echoAccess4 = sharedPreferences.getString("echoAccess4", null)

        outroId = sharedPreferences.getInt("outroId", 0)


        getUsuarioPorId(idEcho)
        usernameArtist?.let { getArtistEchoes(it) }



    }
    override fun onBackPressed() {
        super.onBackPressed()
        if(echoAccess=="echoAccess"){
            val sharedPreferences2 = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences2.edit()
            editor.remove("echoAccess")
            editor.apply()
            val intent = Intent(this, EchoLink::class.java)
            startActivity(intent)
            finish()
        }
        else if(echoAccess2=="echoAccess2"){
            val sharedPreferences2 = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences2.edit()
            editor.remove("echoAccess2")
            editor.apply()
            val intent = Intent(this, EchoLink::class.java)
            startActivity(intent)
            finish()
        }
        else if(echoAccess3=="echoAccess3"){
            val sharedPreferences2 = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences2.edit()
            editor.remove("echoAccess3")
            editor.apply()
            val intent = Intent(this, Echolink2::class.java)
            startActivity(intent)
            finish()
        }
        else if(echoAccess4=="echoAccess4"){
            val sharedPreferences2 = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences2.edit()
            editor.remove("echoAccess4")
            editor.apply()
            val intent = Intent(this, Echolink2::class.java)
            startActivity(intent)
            finish()
        }
        else{
            val intent = Intent(this, EchoFlow::class.java)
            startActivity(intent)
            finish()
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
                        biografia = it.biografia
                        usernameArtist = it.username
                        val imagemUrl = "https://vibratosimages.blob.core.windows.net/imagens/${imagem}"
                        if (!imagemUrl.isNullOrBlank()) {
                            Glide.with(this@PerfilArtista2)
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

                        binding.nomeUsuario.text = usernameArtist


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
    private fun getArtistEchoes(username : String) {
        val retrofitClient = Conexao.getRetrofitInstance("/echo/visuu/${username}")
        val endpoint = retrofitClient.create(Endpoint::class.java)


        endpoint.listarEchoesMaisView(username).enqueue(object : Callback<List<Map<String, Any>>?> {
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
                            if (echo.artista.idUsuario == idEcho){
                                echoesList.add(echo)
                            }
                        }

                        linearArtistEchoes.removeAllViews()
                        for (selectedEcho in echoesList){
                            val childLayout = LinearLayout(this@PerfilArtista2)
                            childLayout.layoutParams =
                                LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                )
                            childLayout.orientation = LinearLayout.HORIZONTAL
                            linearArtistEchoes.addView(childLayout)

                            val imageView = ImageView(this@PerfilArtista2)
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
                            Glide.with(this@PerfilArtista2)
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