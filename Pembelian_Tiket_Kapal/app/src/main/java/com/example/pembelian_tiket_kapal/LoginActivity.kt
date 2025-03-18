package com.example.pembelian_tiket_kapal

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val tvRegister = findViewById<TextView>(R.id.tvRegister)
        tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
        val btnLogin = findViewById<TextView>(R.id.btnlogin)
        btnLogin.setOnClickListener {
            val intent = Intent(this, JadwalKapalActivity::class.java)
            startActivity(intent)
        }
    }
}
