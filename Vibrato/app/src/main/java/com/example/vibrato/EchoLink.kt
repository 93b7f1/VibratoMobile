package com.example.vibrato

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.widget.ImageView
class EchoLink : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_echo_link)

        val image: ImageView = findViewById(R.id.image)

        //blur hehe
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.S) {
            image.setRenderEffect(RenderEffect.createBlurEffect(20f, 20f, Shader.TileMode.MIRROR));
        }
    }
}