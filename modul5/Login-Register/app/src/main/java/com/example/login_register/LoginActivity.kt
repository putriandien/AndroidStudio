package com.example.login_register

import android.content.Intent
import android.os.Bundle
import android.util.JsonToken
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


class LoginActivity : AppCompatActivity() {
    // View components
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize views
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)

        // Set click listener for register button
        loginButton.setOnClickListener {
            registerUser()
        }
    }

    // Data class for User
    data class User(
        val email: String,
        val password: String,
    )

    // Data class for User Response
    data class UserResponse(
        val message: String,
        val data: userData,
        val accessToken: String,
        val refreshToken: String

    )

    data class userData(
        val full_name: String,
        val role: String
    )

    // API Service Interface
    interface ApiService {
        @POST("login")
        fun registerUser(@Body user: User): Call<UserResponse>
    }

    // Function to register user
    private fun registerUser() {
        try {
            // Get data from EditText
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Validasi Input
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return
            }

            //Buat user object
            val user = User(email, password)

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
                        val full_name = response.body()?.data?.full_name
                        val role = response.body()?.data?.role
                        val accessToken = response.body()?.accessToken
                        val refreshToken = response.body()?.refreshToken

                        editor.putString("full_name", full_name)
                        editor.putString("email", email)
                        editor.putString("role", role)
                        editor.putString("accessToken", accessToken)
                        editor.putString("refreshToken", refreshToken)
                        editor.apply()

                        val serverMessage = response.body()?.message ?: "Login Berhasil"
                        // Registration successful, navigate to MainActivity
                        Toast.makeText(this@LoginActivity, serverMessage, Toast.LENGTH_SHORT).show()

                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish() // Optionally close the RegisterActivity
                    } else {
                        val errorMessage = response.errorBody()?.string() ?: "Login Gagal"
                        val jsonError = JSONObject(errorMessage)
                        val serverMessage = jsonError.getString("message")
                        Toast.makeText(this@LoginActivity, serverMessage, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                    // Show error message if network reques fails
                    Toast.makeText(this@LoginActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
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
