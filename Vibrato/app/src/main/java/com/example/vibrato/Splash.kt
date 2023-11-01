package com.example.vibrato

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class Splash : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val authManager = AuthManager(this)

        if (authManager.getAuthToken() != null) {
            val intent = Intent(this@Splash, EchoFlow::class.java)
            startActivity(intent)
        } else {
            val intent = Intent(this@Splash, MainActivity::class.java)
            startActivity(intent)
        }
        }
    }
