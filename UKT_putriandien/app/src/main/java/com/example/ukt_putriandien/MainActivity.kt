package com.example.ukt_putriandien

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import com.example.ukt_putriandien.api.ApiClient
import com.example.ukt_putriandien.request.RefreshTokenRequest
import com.example.ukt_putriandien.response.BooksData

class MainActivity : AppCompatActivity() {
    private lateinit var welcome: TextView
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var logoutButton: Button
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var recyclerViewBooks: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private var userList = mutableListOf<BooksData>()
    private lateinit var createButton: Button


    // Interval pengecekan token dalam milidetik (5 detik)
    private val checkTokenInterval: Long = TimeUnit.SECONDS.toMillis(5)

    // Fungsi untuk memeriksa apakah access token sudah kadaluarsa
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

    // Fungsi untuk mengecek token setiap 5 detik
    private fun startTokenChecker() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                checkAndRefreshToken()  // Panggil pengecekan token
                handler.postDelayed(this, checkTokenInterval)  // Ulangi setiap 5 detik
            }
        }, checkTokenInterval)
    }

    // Fungsi untuk memeriksa apakah token kadaluarsa dan melakukan refresh atau logout
    private fun checkAndRefreshToken() {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val accessToken = sharedPreferences.getString("akses", "")
        val refreshToken = sharedPreferences.getString("refresh", "")

        if (accessToken != null && isTokenExpired(accessToken)) {
            if (refreshToken != null) {
                if (isTokenExpired(refreshToken)) {
                    // Jika refresh token juga expired, langsung logout
                    logoutAndRedirectToLogin()
                } else {
                    // Refresh token masih valid, coba refresh access token
                    refreshAccessToken(refreshToken)
                }
            } else {
                logoutAndRedirectToLogin()
            }

        }
    }

    // Fungsi untuk memanggil API refresh token
    private fun refreshAccessToken(refreshToken: String) {
        val request = RefreshTokenRequest(refreshToken)

        CoroutineScope(Dispatchers.IO).launch {
            val response = ApiClient.authService.refreshToken(request)

            runOnUiThread {
                if (response.isSuccessful) {
                    response.body()?.let {
                        val message = it.message
                        val newAccessToken = it.accessToken

                        // Menyimpan message di SharedPreferences
                        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putString("Pesan", message)
                        editor.putString("akses", newAccessToken)
                        editor.apply()
                    }
                } else {
                    logoutAndRedirectToLogin()
                    Toast.makeText(this@MainActivity, "Refresh token gagal", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun logoutAndRedirectToLogin() {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val accessToken = sharedPreferences.getString("akses", null)
        val message = "Logout Berhasil"

        CoroutineScope(Dispatchers.IO).launch {
            var logoutMessage = "Logout gagal"

            if (accessToken != null) {
                try {
                    val response = ApiClient.authService.logout("Bearer $accessToken")
                    logoutMessage = if (response.isSuccessful) {
                        message ?: "Logout berhasil"
                    } else {
                        "Logout gagal di server"
                    }
                } catch (e: Exception) {
                    logoutMessage = "Terjadi kesalahan jaringan saat logout"
                }
            } else {
                logoutMessage = "Token tidak ditemukan, langsung logout"
            }

            // Jalankan proses di UI thread
            withContext(Dispatchers.Main) {
                // Tampilkan toast hasil logout
                Toast.makeText(this@MainActivity, logoutMessage, Toast.LENGTH_SHORT).show()

                // Hapus data lokal
                val editor = sharedPreferences.edit()
                editor.clear()
                editor.apply()

                // Bersihkan handler dan redirect ke login
                handler.removeCallbacksAndMessages(null)
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val accessToken = sharedPreferences.getString("akses", "") ?: ""


        logoutButton = findViewById(R.id.logoutbutton)
        drawerLayout = findViewById(R.id.drawer_layout)

        logoutButton.setOnClickListener {
            logoutAndRedirectToLogin()
        }

        welcome = findViewById(R.id.welcomeText)
        welcome.text = "Selamat datang!!"
        welcome.visibility = View.VISIBLE


        startTokenChecker()

        // Inisialisasi RecyclerView
        recyclerViewBooks = findViewById(R.id.recyclerViewBooks)
        recyclerViewBooks.layoutManager = LinearLayoutManager(this)
        userAdapter = UserAdapter(userList) { book ->
            val intent = Intent(this@MainActivity, DetailActivity::class.java)
            intent.putExtra("BOOK_ID", book.id)
            startActivity(intent)
        }
        recyclerViewBooks.adapter = userAdapter

        createButton = findViewById(R.id.createButton)
        // Panggil fungsi ambil data user dari API
        val fromLogin = intent.getBooleanExtra("fromLogin", false)
        val fromCreate = intent.getBooleanExtra("fromCreate", false)
        if (fromLogin || fromCreate) {
            fetchUsers("Bearer $accessToken")
            createButton.visibility = View.VISIBLE
            createButton.setOnClickListener{
                val intent = Intent(this@MainActivity, BooksActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun fetchUsers(bearerToken: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = ApiClient.authService.getBooks(bearerToken, 1000)
            if (response.isSuccessful) {
                val getUserResponse = response.body()
                getUserResponse?.let {
                    val BooksResponseGet = it.data.books
                    // Update list user di UI thread
                    runOnUiThread {
                        userList.clear()
                        userList.addAll(BooksResponseGet)
                        userAdapter.notifyDataSetChanged()
                    }
                }
            } else {
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "Gagal mengambil data user",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}

