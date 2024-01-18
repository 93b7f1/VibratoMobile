package com.example.vibrato

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.marginRight
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.vibrato.databinding.ActivityCadastroMusicaBinding
import com.example.vibrato.databinding.ActivityExploreBinding
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Explore : AppCompatActivity() {
    private lateinit var linearPrincipal: LinearLayout
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MusicianHome::class.java)
        startActivity(intent)
        finish()
    }
    private val binding by lazy {
        ActivityExploreBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val sharedPreferences2 = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences2.edit()
        editor.putInt("outroId2", 0)
        editor.apply()

        linearPrincipal = findViewById(R.id.linearprincipal)
        val echoId = intent.getIntExtra("echoId", 0)
        linearPrincipal.orientation = LinearLayout.VERTICAL

        buscaInicial(echoId)

        val editText = findViewById<EditText>(R.id.barraPesquisa)

        editText.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (event != null && event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER)
            ) {
                val pesquisa = binding.barraPesquisa.text.toString()
                if (pesquisa.isNullOrBlank()) {
                    buscaInicial(echoId)
                }

                buscaPesquisa(pesquisa)
                return@setOnEditorActionListener true
            }
            false
        }
    }

    private fun buscaPesquisa(pesquisa: String) {
        val retrofitClient = Conexao.getRetrofitInstance("/echo/buscar-por-texto/${pesquisa}")
        val endpoint = retrofitClient.create(Endpoint::class.java)

        endpoint.listarPesquisa(pesquisa).enqueue(object : Callback<List<Map<String, Any>>?> {
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

                        linearPrincipal.removeAllViews()

                        for (selectedEcho in echoesList) {
                            val childLayout = LinearLayout(this@Explore)
                            childLayout.layoutParams =
                                LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                )
                            childLayout.orientation = LinearLayout.HORIZONTAL
                            linearPrincipal.addView(childLayout)

                            val imageView = ImageView(this@Explore)
                            val layoutParams = LinearLayout.LayoutParams(
                                130.dpToPx(),
                                130.dpToPx()
                            )
                            imageView.layoutParams = layoutParams
                            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                            childLayout.addView(imageView)

                            val textLayout = LinearLayout(this@Explore)
                            textLayout.layoutParams =
                                LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                )
                            textLayout.orientation = LinearLayout.VERTICAL
                            childLayout.addView(textLayout)

                            val nomeMusicaTextView = TextView(this@Explore)
                            nomeMusicaTextView.text = selectedEcho.tituloMusica
                            nomeMusicaTextView.setTextColor(Color.WHITE)
                            nomeMusicaTextView.textSize = 25f
                            textLayout.addView(nomeMusicaTextView)

                            val nomeArtistaTextView = TextView(this@Explore)
                            nomeArtistaTextView.text = selectedEcho.artista.username
                            nomeArtistaTextView.setTextColor(Color.WHITE)
                            nomeArtistaTextView.textSize = 20f
                            layoutParams.setMargins(0, 0, 16.dpToPx(), 15.dpToPx())

                            textLayout.addView(nomeArtistaTextView)



                            val imageUrl =
                                "https://vibratosimages.blob.core.windows.net/imagens/${selectedEcho.blob}"
                            Glide.with(this@Explore)
                                .load(imageUrl)
                                .centerCrop()
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(imageView)

                            val finalIdEcho = selectedEcho.idEcho
                            imageView.setOnClickListener {
                                val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                                val editor = sharedPreferences.edit()
                                editor.putInt("idEchoo", finalIdEcho)
                                editor.apply()
                                imageViewHomeClicked(finalIdEcho)
                            }
                        }
                    }
                } else {
                    Toast.makeText(baseContext, "Música ou artista não encontrado", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun buscaInicial(newEchoId: Int) {
        val retrofitClient = Conexao.getRetrofitInstance("/echo/explore")
        val endpoint = retrofitClient.create(Endpoint::class.java)

        endpoint.listarExplore().enqueue(object : Callback<List<Map<String, Any>>?> {
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
                        val echoesList = mutableListOf<DtoExplore>()

                        for (echoMap in responseBody) {
                            val echoJson = gson.toJson(echoMap)
                            val echo = gson.fromJson(echoJson, DtoExplore::class.java)
                            echoesList.add(echo)
                        }

                        linearPrincipal.removeAllViews()

                        for (selectedEcho in echoesList) {
                            val childLayout = LinearLayout(this@Explore)

                            childLayout.layoutParams =
                                LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                )
                            childLayout.orientation = LinearLayout.HORIZONTAL
                            linearPrincipal.addView(childLayout)

                            val imageView = ImageView(this@Explore)
                            val layoutParams = LinearLayout.LayoutParams(
                                130.dpToPx(),
                                130.dpToPx()
                            )
                            imageView.layoutParams = layoutParams
                            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                            childLayout.addView(imageView)

                            val textLayout = LinearLayout(this@Explore)
                            textLayout.layoutParams =
                                LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                )
                            textLayout.orientation = LinearLayout.VERTICAL
                            childLayout.addView(textLayout)

                            val nomeMusicaTextView = TextView(this@Explore)
                            nomeMusicaTextView.text = selectedEcho.tituloMusica
                            nomeMusicaTextView.setTextColor(Color.WHITE)
                            nomeMusicaTextView.textSize = 25f
                            textLayout.addView(nomeMusicaTextView)

                            val nomeArtistaTextView = TextView(this@Explore)
                            nomeArtistaTextView.text = selectedEcho.cantor
                            nomeArtistaTextView.setTextColor(Color.WHITE)
                            nomeArtistaTextView.textSize = 20f
                            layoutParams.setMargins(0, 0, 16.dpToPx(), 15.dpToPx())

                            textLayout.addView(nomeArtistaTextView)


                            val imageUrl =
                                "https://vibratosimages.blob.core.windows.net/imagens/${selectedEcho.blob}"
                            Glide.with(this@Explore)
                                .load(imageUrl)
                                .centerCrop()
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(imageView)
                            val finalIdEcho: Int = selectedEcho.idEcho
                            imageView.setOnClickListener {
                                val sharedPreferences2 = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                                val editor2 = sharedPreferences2.edit()
                                imageViewHomeClicked(finalIdEcho)
                                editor2.putInt("idEchoo", finalIdEcho)
                                editor2.apply()
                            }
                        }
                    }
                } else {
                    Toast.makeText(baseContext, "Erro na resposta", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun imageViewHomeClicked(idEcho: Int) {
        val intent = Intent(this, Echolink2::class.java)
        intent.putExtra("idEcho", idEcho)
        startActivity(intent)
        finish()
    }

    private fun Int.dpToPx(): Int {
        val scale = resources.displayMetrics.density
        return (this * scale + 0.5f).toInt()
    }
}
