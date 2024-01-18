package com.example.vibrato

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.vibrato.databinding.ActivityCadastroMusicaBinding
import com.google.android.material.imageview.ShapeableImageView


class CadastroMusica : AppCompatActivity() {

    private val binding by lazy {
        ActivityCadastroMusicaBinding.inflate(layoutInflater)
    }

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                handleImageResult(data)
            }
        }

    private var selectedImageUri: Uri? = null
    private var id: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        id = sharedPreferences.getInt("id", 0)
        val cadastroMusica2 = Intent(this, cadastroMusica2::class.java)

        binding.btnCadastroImagem.setOnClickListener {
            openGallery()
        }

        binding.btnProximaEtapa.setOnClickListener {
            if (binding.etLinkPerfil.text.toString().isEmpty()) {
                binding.etLinkPerfil.error = "Por favor insira o link do seu perfil"
                return@setOnClickListener
            }

            if (binding.etLinkMusica.text.toString().isEmpty()) {
                binding.etLinkMusica.error = "Por favor insira o Link de sua música"
                return@setOnClickListener
            }

            val image: Uri? = selectedImageUri
            val editor = sharedPreferences.edit()
            editor.putString("imagem", image.toString())
            editor.putString("spotify", binding.etSpotify.text.toString())
            editor.putString("youtube", binding.etYoutube.text.toString())
            editor.putString("soundCloud", binding.etSoundcloud.text.toString())
            editor.putString("deezer", binding.etDeezer.text.toString())
            editor.putString("amazonMusic", binding.etAmazonMusic.text.toString())
            editor.putString("appleMusic", binding.etAppleMusic.text.toString())
            editor.putString("linkPerfil", binding.etLinkPerfil.text.toString())
            editor.putString("linkMusica", binding.etLinkMusica.text.toString())
            editor.apply()

            startActivity(cadastroMusica2)


        }
    }
    private fun handleImageResult(data: Intent?) {
        selectedImageUri = data?.data
        if (selectedImageUri != null) {
            Glide.with(this)
                .load(selectedImageUri)
                .into(binding.imageViewPerfil as ShapeableImageView)

        }
    }

    private fun openGallery() {
        val pickImg = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        pickImage.launch(pickImg)
    }
}



