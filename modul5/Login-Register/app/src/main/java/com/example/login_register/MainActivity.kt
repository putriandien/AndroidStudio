package com.example.login_register

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity

import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.core.view.GravityCompat
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var welcomeText: TextView
    private lateinit var logoutButton: Button
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private val handler = Handler(Looper.getMainLooper())

    //Interval pengecekan token dalam milidetik (5 detik)
    private val checkTokenInterval: Long = TimeUnit.SECONDS.toMillis((5))

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        welcomeText = findViewById(R.id.welcomeText)
        logoutButton = findViewById(R.id.logoutbutton)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val menuButton = findViewById<View>(R.id.menuButtonContainer)

        toolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        menuButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Ambil dari SharedPreference
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val full_name = sharedPreferences.getString("full_name", "")
        val role = sharedPreferences.getString("role", "")
        val email = sharedPreferences.getString("email", "")

        // Update header navigasi dengan data pengguna
        val navHeaderView = navigationView.getHeaderView(0)
        val tvUsername = navHeaderView.findViewById<TextView>(R.id.tvUsername)
        val tvEmail = navHeaderView.findViewById<TextView>(R.id.tvEmail)

        tvUsername.text = full_name ?: "Nama Pengguna"
        tvEmail.text = email ?: "Email Pengguna"

        if (role == "admin") {
            toolbar.visibility = View.VISIBLE
            navigationView.visibility = View.VISIBLE
        }

        // Jika username ada, tampilkan pada TextView
        if (!full_name.isNullOrEmpty()) {
            welcomeText.text = "Selamat datang, $full_name!"
            welcomeText.visibility = View.VISIBLE
        }

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> Toast.makeText(this, "Profile ditekan!", Toast.LENGTH_SHORT).show()
                R.id.nav_settings -> Toast.makeText(this, "Setting ditekan!", Toast.LENGTH_SHORT).show()
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // Logout button action
        logoutButton.setOnClickListener {
            // Logout action
            val editor = sharedPreferences.edit()
            editor.remove("full_name")
            editor.remove("accessToken")
            editor.remove("refreshToken")
            editor.apply()

            // Menampilkan pesan dan kembali ke halaman login
            Toast.makeText(this, "Logout Berhasil", Toast.LENGTH_SHORT).show()
            handler.removeCallbacksAndMessages(null)

            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Panggil fungsi untuk mulai pengecekan token periodik
        startTokenChecker()
    }

    // Fungsi untuk mengecek token setiap 5 detik
    private fun isTokenExpired(token: String): Boolean {
        return try {
            val parts = token.split(".")
            if (parts.size == 3) {
                val payload = String(Base64.decode(parts[1], Base64.DEFAULT))
                val jsonObject = JSONObject(payload)
                val expTime = jsonObject.getLong("exp") * 1000
                System.currentTimeMillis() > expTime
            } else true
        } catch (e: Exception) {
            true
        }
    }

    // Fungsi untuk memeriksa tokjen setiap 5 detik
    private fun startTokenChecker() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                checkAndRefreshToken() // Panggil pengecekan token
                handler.postDelayed(this, checkTokenInterval) //Ulangi setiap 5 detik
            }
        }, checkTokenInterval)
    }

    // Fungsi untuk memeriksa apakah token kedaluarsa dan melakukan refresh atau logout
    private fun checkAndRefreshToken() {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val accessToken = sharedPreferences.getString("accessToken", "")
        val refreshToken = sharedPreferences.getString("refreshToken", "")

        if (accessToken != null && isTokenExpired(accessToken)) {
            if (refreshToken != null) {
                // Refresh token masih valid, coba refresh access token
                refreshAccessToken(refreshToken)
            } else {
                // Jika refresh token juga expired, langsung logout
                logoutAndRedirectToLogin()
            }
        }
    }

    // Data class untuk refreshToken
    data class RefreshTokenRequest(val refreshToken: String)

    // Interface API untuk refresh token
    interface ApiService {
        @POST("token-refresh")
        fun refreshToken(@Body request: RefreshTokenRequest): Call<RefreshTokenResponse>
    }

    // Data class untuk response refresh token
    data class RefreshTokenResponse(val accessToken: String)

    // Fungsi untuk memanggil API refresh token
    private fun refreshAccessToken(refreshToken: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.137.81/api/") // Ganti dengan URL base API kamu
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)
        val request = RefreshTokenRequest(refreshToken)
        val call = apiService.refreshToken(request)

        call.enqueue(object : Callback<RefreshTokenResponse> {
            override fun onResponse(
                call: Call<RefreshTokenResponse>,
                response: retrofit2.Response<RefreshTokenResponse>
            ) {
                if (response.isSuccessful) {
                    val newAccessToken = response.body()?.accessToken
                    val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString("accessToken", newAccessToken) // Simpan access token yang baru
                    editor.apply()
                } else {
                    // Jika refresh token gagal, logout dan arahkan ke loginActivity
                    logoutAndRedirectToLogin()
                }
            }

            override fun onFailure(call: Call<RefreshTokenResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Refresh token gagal", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Fungsi untuk logout dan mengarahkan ke halaman login
    private fun logoutAndRedirectToLogin() {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear() // Hapus semua data pengguna
        editor.apply()

        Toast.makeText(this, "Token kedaluarsa. Anda telah logout", Toast.LENGTH_SHORT).show()
        handler.removeCallbacksAndMessages(null)
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish() // Menutup MainActivity agar tidak bisa kembali
    }
}
