package com.example.vibrato
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.vibrato.databinding.ActivityMusicianHomeBinding
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import de.hdodenhof.circleimageview.CircleImageView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class MusicianHome : AppCompatActivity() {
    val binding by lazy {
        ActivityMusicianHomeBinding.inflate(layoutInflater)
    }
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var player: SimpleExoPlayer
    private lateinit var playerView: PlayerView
    private lateinit var nomekk: TextView
    private lateinit var username_nav: TextView
    private lateinit var photo_nav: CircleImageView

    private var id: Int = 0
    private var username: String? = null
    private var tipoUsuario: String? = null
    private var imagem: String? =null

    private var idArtista: Int = 0
    private var artistaUsername: String? = null
    private var artistaUsername1: String? = null
    private var artistaUsername2: String? = null
    private var artistaUsername3: String? = null
    private var artistaUsername4: String? = null
    private var artistaUsername5: String? = null
    private var artistaId1: Int? = 0
    private var artistaId2: Int? = 0
    private var artistaId3: Int? = 0
    private var artistaId4: Int? = 0
    private var artistaId5: Int? = 0
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, EchoFlow::class.java)
        startActivity(intent)
        finish()
    }
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        val authManager = AuthManager(this)
        setContentView(binding.root)

        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        id = sharedPreferences.getInt("id", 0)
        username = sharedPreferences.getString("username", null)
        tipoUsuario = sharedPreferences.getString("tipoUsuario", null)
        val imageView: ImageView = findViewById(R.id.top_primeira_imagem)
        val imageView2: ImageView = findViewById(R.id.top_segunda_imagem)

        Glide.with(this)
            .asGif()
            .load(R.drawable.loading)
            .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.DATA))
            .into(imageView)
        Glide.with(this)
            .asGif()
            .load(R.drawable.loading)
            .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.DATA))
            .into(imageView2)


//        nomekk = findViewById(R.id.usernome)
//        nomekk.text = username
        getTop5 { echoesTop5 ->
            val primeiroEcho: Echo? = echoesTop5.firstOrNull()
            val nomeMusica1: String? = echoesTop5[0].tituloMusica
            val nomeAlbum1: String? = echoesTop5[0].tituloAlbum
            artistaUsername1 = echoesTop5[0].artista.username
            artistaId1 = echoesTop5[0].artista.idUsuario

            val segundaEcho: String? = echoesTop5[1].blob
            val nomeMusica2: String? = echoesTop5[1].tituloMusica
            val nomeAlbum2: String? = echoesTop5[1].tituloAlbum
            artistaUsername2 = echoesTop5[1].artista.username
            artistaId2 = echoesTop5[1].artista.idUsuario

            val terceiraEcho: String? = echoesTop5[2].blob
            val nomeMusica3: String? = echoesTop5[2].tituloMusica
            val nomeAlbum3: String? = echoesTop5[2].tituloAlbum
            artistaUsername3 = echoesTop5[2].artista.username
            artistaId3 = echoesTop5[2].artista.idUsuario

            val quartaEcho: String? = echoesTop5[3].blob
            val nomeMusica4: String? = echoesTop5[3].tituloMusica
            val nomeAlbum4: String? = echoesTop5[3].tituloAlbum
            artistaUsername4 = echoesTop5[3].artista.username
            artistaId4 = echoesTop5[3].artista.idUsuario

            val quintaEcho: String? = echoesTop5[4].blob
            val nomeMusica5: String? = echoesTop5[4].tituloMusica
            val nomeAlbum5: String? = echoesTop5[4].tituloAlbum
            artistaUsername5 = echoesTop5[4].artista.username
            artistaId5 = echoesTop5[4].artista.idUsuario


            if (primeiroEcho != null) {
                val imagemUrl =
                    "https://vibratosimages.blob.core.windows.net/imagens/${primeiroEcho.blob}"
                if (!imagemUrl.isNullOrBlank()) {
                    Glide.with(this@MusicianHome)
                        .load(imagemUrl)
                        .into(binding.topPrimeiraImagem)
                }
                binding.top1Text.text = " Músic: ${nomeMusica1} \n Album: ${nomeAlbum1}"

            }

            if (segundaEcho != null) {
                val imagemUrl =
                    "https://vibratosimages.blob.core.windows.net/imagens/${segundaEcho}"
                if (!imagemUrl.isNullOrBlank()) {
                    Glide.with(this@MusicianHome)
                        .load(imagemUrl)
                        .into(binding.topSegundaImagem)
                }
                binding.top2Text.text = " Músic: ${nomeMusica2} \n Album: ${nomeAlbum2}"
            }
            if (terceiraEcho != null) {
                val imagemUrl =
                    "https://vibratosimages.blob.core.windows.net/imagens/${terceiraEcho}"
                if (!imagemUrl.isNullOrBlank()) {
                    Glide.with(this@MusicianHome)
                        .load(imagemUrl)
                        .into(binding.topTerceiraImagem)
                }
                binding.top3Text.text = " Músic: ${nomeMusica3} \n Album: ${nomeAlbum3}"
            }
            if (quartaEcho != null) {
                val imagemUrl = "https://vibratosimages.blob.core.windows.net/imagens/${quartaEcho}"
                if (!imagemUrl.isNullOrBlank()) {
                    Glide.with(this@MusicianHome)
                        .load(imagemUrl)
                        .into(binding.topQuartaImagem)
                }
                binding.top4Text.text = " Músic: ${nomeMusica4} \n Album: ${nomeAlbum4}"
            }
            if (quintaEcho != null) {
                val imagemUrl = "https://vibratosimages.blob.core.windows.net/imagens/${quintaEcho}"
                if (!imagemUrl.isNullOrBlank()) {
                    Glide.with(this@MusicianHome)
                        .load(imagemUrl)
                        .into(binding.topQuintaImagem)
                }
                binding.top5Text.text = " Músic: ${nomeMusica5} \n Album: ${nomeAlbum5}"
            }

        }
        startVideo()

        binding.topPrimeiraImagem.setOnClickListener {
            val intent = Intent(this, PerfilArtista2::class.java)
            val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putInt("idEchoChosen",artistaId1.toString().toInt())
            editor.putString("usernameArtist", artistaUsername1.toString())
            editor.apply()
            startActivity(intent)
        }
        binding.topSegundaImagem.setOnClickListener {
            val intent = Intent(this, PerfilArtista2::class.java)
            val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putInt("idEchoChosen",artistaId2.toString().toInt())
            editor.putString("usernameArtist", artistaUsername2.toString())
            editor.apply()
            startActivity(intent)
        }
        binding.topTerceiraImagem.setOnClickListener {
            val intent = Intent(this, PerfilArtista2::class.java)
            val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putInt("idEchoChosen",artistaId3.toString().toInt())
            editor.putString("usernameArtist", artistaUsername3.toString())
            editor.apply()
            startActivity(intent)
        }
        binding.topQuartaImagem.setOnClickListener {
            val intent = Intent(this, PerfilArtista2::class.java)
            val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putInt("idEchoChosen",artistaId4.toString().toInt())
            editor.putString("usernameArtist", artistaUsername4.toString())
            editor.apply()
            startActivity(intent)
        }
        binding.topQuintaImagem.setOnClickListener {
            val intent = Intent(this, PerfilArtista2::class.java)
            val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putInt("idEchoChosen",artistaId5.toString().toInt())
            editor.putString("usernameArtist", artistaUsername5.toString())
            editor.apply()
            startActivity(intent)
        }
        drawerLayout = findViewById(R.id.drawer_layout)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        navigationView = findViewById(R.id.nav_view)
        toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            findViewById(R.id.toolbar),
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    val intent = Intent(this@MusicianHome, EchoFlow::class.java)
                    startActivity(intent)
                    return@setNavigationItemSelectedListener true

                    true
                }

                R.id.nav_profile -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    val intent = Intent(this@MusicianHome, Perfil_artista::class.java)
                    startActivity(intent)
                    true
                }

                R.id.nav_settings -> {

                    drawerLayout.closeDrawer(GravityCompat.START)
                    drawerLayout.closeDrawers()

                    val intent = Intent(this, Configuracoes::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.analise -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    drawerLayout.closeDrawers()
                    val intent = Intent(this, Dashboard::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.explore -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    drawerLayout.closeDrawers()
                    val intent = Intent(this, Explore::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_logout -> {
                    Toast.makeText(applicationContext, "Logout clicked", Toast.LENGTH_SHORT).show()
                    drawerLayout.closeDrawer(GravityCompat.START)
                    authManager.logout()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }

                else -> false
            }
            true
        }
        val navHeaderView = navigationView.getHeaderView(0)
        username_nav = navHeaderView.findViewById(R.id.name_account)

        if (username != null) {
            username_nav.text = username
        }

        photo_nav = navHeaderView.findViewById(R.id.circle_image_view)
        buscarUsuarioPorId(id)
        getUsuarioPorId(id)

        binding.btnPublicarMusica.setOnClickListener {
            val intent = Intent(this@MusicianHome, CadastroMusica::class.java)
            startActivity(intent)
        }

        val amazon: Button = binding.imageViewAmazon
        amazon.setOnClickListener {view ->
            val url = "https://music.amazon.com.br"
            openExternalLink(url)
        }
        val youtube: ImageView = binding.imageViewYoutube
        youtube.setOnClickListener {view ->
            val url = "https://www.youtube.com/"
            openExternalLink(url)
        }

        val spotify: ImageView = binding.imageViewSpotify
        spotify.setOnClickListener {view ->
            val url = "https://open.spotify.com/intl-pt"
            openExternalLink(url)
        }

        val apple: ImageView = binding.imageViewApple
        apple.setOnClickListener {view ->
            val url = "https://music.apple.com/us/browse"
            openExternalLink(url)
        }

        val deezer: ImageView = binding.imageViewDeezer
        deezer.setOnClickListener {view ->
            val url = "https://www.deezer.com/en/"
            openExternalLink(url)
        }

        val soundcloud: ImageView = binding.imageViewSoundcloud
        soundcloud.setOnClickListener {view ->
            val url = "https://soundcloud.com/"
            openExternalLink(url)
        }

    }

    private fun startVideo() {
        playerView = findViewById(R.id.home_video)
        player = SimpleExoPlayer.Builder(this).build()

        playerView.player = player

        val mediaItem = MediaItem.fromUri(Uri.parse("https://vibratosimages.blob.core.windows.net/videos/bannersVideo.mp4"))
        player.setMediaItem(mediaItem)
        player.repeatMode = SimpleExoPlayer.REPEAT_MODE_ALL

        player.prepare()
        player.play()
        playerView.useController = false

    }
    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }

    private fun releasePlayer() {
        player.release()
    }

    private fun getTop5(callback:(List<Echo>) -> Unit ){
        val retrofitClient = Conexao.getRetrofitInstance("/echo/top5")
        val endpoint = retrofitClient.create(Endpoint::class.java)
        endpoint.listarTop5().enqueue(object : retrofit2.Callback<List<Map<String, Any>>?> {
            override fun onFailure(call: retrofit2.Call<List<Map<String, Any>>?>, t: Throwable) {
                Toast.makeText(baseContext, t.message, Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(
                call: retrofit2.Call<List<Map<String, Any>>?>,
                response: retrofit2.Response<List<Map<String, Any>>?>
            ) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        val gson = Gson()
                        val echoestop5 = mutableListOf<Echo>()

                        for (echoMap in responseBody) {
                            val echoJson = gson.toJson(echoMap)
                            val echo = gson.fromJson(echoJson, Echo::class.java)
                            echoestop5.add(echo)
                        }
                        callback(echoestop5)
                    }

                } else {
                    Toast.makeText(baseContext, "Erro na resposta", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun buscarUsuarioPorId(idUsuario: Int) {
        val retrofitClient = Conexao.getRetrofitInstance("/usuario/perfil/${idUsuario}")
        val endpoint = retrofitClient.create(Endpoint::class.java)
        val call = endpoint.getUsuario(idUsuario)
        call.enqueue(object : Callback<Artista> {
            override fun onResponse(call: Call<Artista>, response: Response<Artista>) {
                if (response.isSuccessful) {
                    val usuario: Artista? = response.body()
                    usuario?.let {
                        imagem = it.blob
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

    private fun openExternalLink(url: String) {
        val openURL = Intent(Intent.ACTION_VIEW)
        openURL.data = Uri.parse(url)
        startActivity(openURL)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        toggle.syncState()

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        toggle.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                drawerLayout.openDrawer(GravityCompat.START)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun getUsuarioPorId(idUsuario: Int) {
        val navHeaderView = navigationView.getHeaderView(0)
        photo_nav = navHeaderView.findViewById(R.id.circle_image_view)
        val retrofitClient = Conexao.getRetrofitInstance("/usuario/perfil/${idUsuario}")
        val endpoint = retrofitClient.create(Endpoint::class.java)
        val call = endpoint.getUsuario(idUsuario)
        call.enqueue(object : Callback<Artista> {
            override fun onResponse(call: Call<Artista>, response: Response<Artista>) {
                if (response.isSuccessful) {
                    val usuario: Artista? = response.body()
                    usuario?.let {
                        imagem = it.blob

                        val imagemUrl = "https://vibratosimages.blob.core.windows.net/imagens/${imagem}"
                        if (!imagemUrl.isNullOrBlank()) {
                            Glide.with(this@MusicianHome)
                                .load(imagemUrl)
                                .into(photo_nav)
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
}

