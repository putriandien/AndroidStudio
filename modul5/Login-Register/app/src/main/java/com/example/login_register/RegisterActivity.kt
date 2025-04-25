package com.example.login_register

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import kotlin.math.log

class RegisterActivity : AppCompatActivity() {

    // View components
    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize views
        usernameEditText = findViewById(R.id.usernameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        registerButton = findViewById(R.id.registerButton)
        loginButton = findViewById(R.id.loginButton)

        // Set click listener for register button
        registerButton.setOnClickListener {
            registerUser()
        }

        loginButton.setOnClickListener{
            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // Data class for User
    data class User(
        val full_name: String,
        val email: String,
        val password: String
    )

    // Data class for User Response
    data class UserResponse(
        val message: String
    )

    // API Service Interface
    interface ApiService {
        @POST("register")
        fun registerUser(@Body user: User): Call<UserResponse>
    }

    // Function to register user
    private fun registerUser() {
        try {
            // Get data from EditText
            val full_name = usernameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Validasi Input
            if (full_name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return
            }

            val user = User(full_name, email, password)

            // Create Retrofit Instance
            val retrofit = Retrofit.Builder()
                .baseUrl("http://192.168.137.81:5000/api/") // Ganti sesuai dengan IP kalian
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val apiService = retrofit.create(ApiService::class.java)

            val call = apiService.registerUser(user)
            call.enqueue(object : Callback<UserResponse> {
                override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                    if (response.isSuccessful) {
                        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putString("full_name", full_name)
                        editor.apply()

                        val serverMessage = response.body()?.message ?: "Registrasi Berhasil"
                        // Registration successful, navigate to MainActivity
                        Toast.makeText(this@RegisterActivity, serverMessage, Toast.LENGTH_SHORT).show()

                        val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish() // Optionally close the RegisterActivity
                    } else {
                        val errorMessage = response.errorBody()?.string() ?: "Registrasi Gagal"
                        val jsonError = JSONObject(errorMessage)
                        val serverMessage = jsonError.getString("message")
                        Toast.makeText(this@RegisterActivity, serverMessage, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                    // Show error message if network reques fails
                    Toast.makeText(this@RegisterActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                    Log.e("RegisterActivity", "Error: ${t.message}", t) // Log Error
                }
            })
        } catch (e: Exception) {
            // Handle unxpeced exceptions
            Toast.makeText(this, "An error occurred: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("RegisterActivity", "Exception: ${e.message}")
        }
    }
}
