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
import android.widget.EditText
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.ukt_putriandien.api.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.ukt_putriandien.request.RegisterRequest

class RegisterActivity : AppCompatActivity() {

    // View components
    private lateinit var nameET: EditText
    private lateinit var emailET: EditText
    private lateinit var phoneET: EditText
    private lateinit var passwordET: EditText
    private lateinit var registerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize views
        nameET = findViewById(R.id.nameET)
        emailET = findViewById(R.id.emailET)
        phoneET = findViewById(R.id.phoneET)
        passwordET = findViewById(R.id.passwordET)
        registerButton = findViewById(R.id.registerButton)

        // Set click listener for login button
        registerButton.setOnClickListener {
            val name = nameET.text.toString()
            val email = emailET.text.toString()
            val phone = phoneET.text.toString()
            val password = passwordET.text.toString()

            if (email.isEmpty() || password.isEmpty() || name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            register(name, email, phone,  password)
        }
        val loginTextView: TextView = findViewById(R.id.loginTextView)
        val text = "Already have an account? Login"
        val spannableString = SpannableString(text)

// Buat posisi kata "Register"
        val start = text.indexOf("Login")
        val end = start + "Login".length

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
                val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                startActivity(intent)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true // Tampilkan underline
            }
        }, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        loginTextView.text = spannableString
        loginTextView.movementMethod = LinkMovementMethod.getInstance()
    }


    // Function to register user
    private fun register(name: String, email: String, phone:String, password: String) {
        val request = RegisterRequest(name, email, phone,  password)

        CoroutineScope(Dispatchers.IO).launch {
            val response = ApiClient.authService.register(request)

            runOnUiThread {
                if (response.isSuccessful) {
                    response.body()?.let {
                        val message = it.message

                        // Menyimpan message di SharedPreferences
                        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putString("Pesan", message)
                        editor.putString("name", name)
                        editor.apply()


                        // Menampilkan Toast untuk konfirmasi login sukses
                        Toast.makeText(this@RegisterActivity, "Register sukses", Toast.LENGTH_SHORT).show()

                        // Melanjutkan ke MainActivity
                        val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                        startActivity(intent)
                    }
                } else {
                    Toast.makeText(this@RegisterActivity, "Register gagal", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

