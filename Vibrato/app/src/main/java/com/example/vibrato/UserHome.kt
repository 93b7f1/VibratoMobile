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
import com.example.vibrato.databinding.ActivityMusicianHomeBinding
import com.example.vibrato.databinding.ActivityUserHomeBinding
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

class UserHome : AppCompatActivity() {
    val binding by lazy {
        ActivityUserHomeBinding.inflate(layoutInflater)
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
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        val authManager = AuthManager(this)
        setContentView(binding.root)

        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        id = sharedPreferences.getInt("id", 0)
        username = sharedPreferences.getString("username", null)
        tipoUsuario = sharedPreferences.getString("tipoUsuario", null)


//        nomekk = findViewById(R.id.usernome)
//
//        nomekk.text = username
        getTop5 { echoesTop5 ->
            val primeiroEcho: Echo? = echoesTop5.firstOrNull()
            val nomeMusica1: String? = echoesTop5[0].tituloMusica
            val nomeAlbum1: String? = echoesTop5[0].tituloAlbum

            val segundaEcho: String? = echoesTop5[1].blob
            val nomeMusica2: String? = echoesTop5[1].tituloMusica
            val nomeAlbum2: String? = echoesTop5[1].tituloAlbum

            val terceiraEcho: String? = echoesTop5[2].blob
            val nomeMusica3: String? = echoesTop5[2].tituloMusica
            val nomeAlbum3: String? = echoesTop5[2].tituloAlbum

            val quartaEcho: String? = echoesTop5[3].blob
            val nomeMusica4: String? = echoesTop5[3].tituloMusica
            val nomeAlbum4: String? = echoesTop5[3].tituloAlbum

            val quintaEcho: String? = echoesTop5[4].blob
            val nomeMusica5: String? = echoesTop5[4].tituloMusica
            val nomeAlbum5: String? = echoesTop5[4].tituloAlbum


            if (primeiroEcho != null) {
                val imagemUrl =
                    "https://vibratosimages.blob.core.windows.net/imagens/${primeiroEcho.blob}"
                if (!imagemUrl.isNullOrBlank()) {
                    Glide.with(this@UserHome)
                        .load(imagemUrl)
                        .into(binding.topPrimeiraImagem)
                }
                binding.top1Text.text = " Músic: ${nomeMusica1} \n Album: ${nomeAlbum1}"

            }
            if (segundaEcho != null) {
                val imagemUrl =
                    "https://vibratosimages.blob.core.windows.net/imagens/${segundaEcho}"
                if (!imagemUrl.isNullOrBlank()) {
                    Glide.with(this@UserHome)
                        .load(imagemUrl)
                        .into(binding.topSegundaImagem)
                }
                binding.top2Text.text = " Músic: ${nomeMusica2} \n Album: ${nomeAlbum2}"
            }
            if (terceiraEcho != null) {
                val imagemUrl =
                    "https://vibratosimages.blob.core.windows.net/imagens/${terceiraEcho}"
                if (!imagemUrl.isNullOrBlank()) {
                    Glide.with(this@UserHome)
                        .load(imagemUrl)
                        .into(binding.topTerceiraImagem)
                }
                binding.top3Text.text = " Músic: ${nomeMusica3} \n Album: ${nomeAlbum3}"
            }
            if (quartaEcho != null) {
                val imagemUrl = "https://vibratosimages.blob.core.windows.net/imagens/${quartaEcho}"
                if (!imagemUrl.isNullOrBlank()) {
                    Glide.with(this@UserHome)
                        .load(imagemUrl)
                        .into(binding.topQuartaImagem)
                }
                binding.top4Text.text = " Músic: ${nomeMusica4} \n Album: ${nomeAlbum4}"
            }
            if (quintaEcho != null) {
                val imagemUrl = "https://vibratosimages.blob.core.windows.net/imagens/${quintaEcho}"
                if (!imagemUrl.isNullOrBlank()) {
                    Glide.with(this@UserHome)
                        .load(imagemUrl)
                        .into(binding.topQuintaImagem)
                }
                binding.top5Text.text = " Músic: ${nomeMusica5} \n Album: ${nomeAlbum5}"
            }

        }
        startVideo()



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
                    Toast.makeText(applicationContext, "Home clicked", Toast.LENGTH_SHORT).show()
                    drawerLayout.closeDrawer(GravityCompat.START)
                    val intent = Intent(this@UserHome, EchoFlow::class.java)
                    startActivity(intent)
                    return@setNavigationItemSelectedListener true

                    true
                }

                R.id.nav_profile -> {
                    Toast.makeText(applicationContext, "Profile clicked", Toast.LENGTH_SHORT).show()
                    drawerLayout.closeDrawer(GravityCompat.START)
                    val intent = Intent(this@UserHome, Perfil_ouvinte::class.java)
                    startActivity(intent)
                    true
                }

                R.id.nav_settings -> {
                    Toast.makeText(applicationContext, "Settings clicked", Toast.LENGTH_SHORT)
                        .show()
                    drawerLayout.closeDrawer(GravityCompat.START)
                    drawerLayout.closeDrawers()

                    val intent = Intent(this, EchoFlow::class.java)
                    intent.putExtra("id", id.toString().toInt())
                    intent.putExtra("username", username.toString())
                    intent.putExtra("tipoUsuario", tipoUsuario.toString())
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
        // adição de informações do usuário na navbar

        val navHeaderView = navigationView.getHeaderView(0)
        username_nav = navHeaderView.findViewById(R.id.name_account)

        if (username != null) {
            username_nav.text = username
        }

        photo_nav = navHeaderView.findViewById(R.id.circle_image_view)
        buscarUsuarioPorId(id)

        val imagemUrl = "https://vibratosimages.blob.core.windows.net/imagens/${imagem}"
        if (!imagemUrl.isNullOrBlank()) {
            Glide.with(this@UserHome)
                .load(imagemUrl)
                .into(photo_nav)
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
}
