package com.example.ukt_putriandien

import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.ukt_putriandien.api.ApiClient
import com.example.ukt_putriandien.request.LoginRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var emailET: EditText
    private lateinit var passwordET: EditText
    private lateinit var loginButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailET = findViewById(R.id.emailET)
        passwordET =  findViewById(R.id.passwordET)
        loginButton = findViewById(R.id.loginButton)


        loginButton.setOnClickListener {
            val email = emailET.text.toString()
            val password = passwordET.text.toString()

            if ( email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            login(email, password)
        }

        val registerTextView: TextView = findViewById(R.id.registerTextView)
        val text = "Don't have an account yet? Register"
        val spannableString = SpannableString(text)

// Buat posisi kata "Register"
        val start = text.indexOf("Register")
        val end = start + "Register".length

// Warna teks "Register"
        spannableString.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(this, R.color.pink)), // atau Color.parseColor("#EC5C9B")
            start,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

// Tambahkan aksi klik hanya pada "Register"
        spannableString.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                startActivity(intent)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true // Tampilkan underline
            }
        }, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        registerTextView.text = spannableString
        registerTextView.movementMethod = LinkMovementMethod.getInstance()
    }


    // Function to login user
    private fun login(email: String, password: String) {
        val request = LoginRequest(email, password)

        CoroutineScope(Dispatchers.IO).launch {
            val response = ApiClient.authService.login(request)

            runOnUiThread {
                if (response.isSuccessful) {
                    response.body()?.let {
                        val message = it.message
                        val accessToken = it.accessToken
                        val refreshToken = it.refreshToken

                        // Menyimpan message di SharedPreferences
                        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putString("akses", accessToken)
                        editor.putString("refresh", refreshToken)
                        editor.putString("Pesan", message)
                        editor.apply()


                        // Menampilkan Toast untuk konfirmasi login sukses
                        Toast.makeText(this@LoginActivity, "Login sukses", Toast.LENGTH_SHORT).show()

                        // Melanjutkan ke MainActivity
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.putExtra("fromLogin", true)
                        startActivity(intent)
                        finish()
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "Login gagal", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}