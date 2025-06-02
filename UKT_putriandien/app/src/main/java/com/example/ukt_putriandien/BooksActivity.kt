package com.example.ukt_putriandien

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ukt_putriandien.api.ApiClient
import com.example.ukt_putriandien.request.BooksRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BooksActivity : AppCompatActivity() {
    private lateinit var isbnET: EditText
    private lateinit var titleET: EditText
    private lateinit var authorET: EditText
    private lateinit var publisherET: EditText
    private lateinit var published_dateET: EditText
    private lateinit var genreET: EditText
    private lateinit var languageET: EditText
    private lateinit var descriptionET: EditText
    private lateinit var createButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_books)
        isbnET = findViewById(R.id.isbnET)
        titleET = findViewById(R.id.titleET)
        authorET = findViewById(R.id.authorET)
        publisherET = findViewById(R.id.publisherET)
        published_dateET = findViewById(R.id.published_dateET)
        genreET = findViewById(R.id.genreET)
        languageET = findViewById(R.id.languageET)
        descriptionET = findViewById(R.id.descriptionET)
        createButton = findViewById(R.id.createButton)

        // Set click listener for login button
        createButton.setOnClickListener {
            val isbn = isbnET.text.toString()
            val title = titleET.text.toString()
            val author = authorET.text.toString()
            val publisher = publisherET.text.toString()
            val published_date = published_dateET.text.toString()
            val genre = genreET.text.toString()
            val language = languageET.text.toString()
            val description = descriptionET.text.toString()

            if (isbn.isEmpty() || title.isEmpty() || author.isEmpty() || publisher.isEmpty() || published_date.isEmpty() || genre.isEmpty() || language.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            createBooks(isbn, title, author, publisher, published_date, genre, language, description
            )
        }
    }

        private fun createBooks(isbn:String, title:String, author:String, publisher:String, published_date:String, genre:String, language: String, description:String) {
            val request = BooksRequest(isbn, title, author, publisher, published_date, genre, language, description)

            val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            val accessToken = sharedPreferences.getString("akses", null) ?: ""

            CoroutineScope(Dispatchers.IO).launch {
                val response = ApiClient.authService.books("Bearer $accessToken", request)

                runOnUiThread {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            val message = it.message

                            // Menyimpan message di SharedPreferences
                            val editor = sharedPreferences.edit()
                            editor.putString("Pesan", message)
                            editor.apply()


                            // Menampilkan Toast untuk konfirmasi login sukses
                            Toast.makeText(this@BooksActivity, "Create Books Sukses", Toast.LENGTH_SHORT).show()

                            // Melanjutkan ke MainActivity
                            val intent = Intent(this@BooksActivity, MainActivity::class.java)
                            intent.putExtra("fromCreate", true)
                            startActivity(intent)
                        }
                    } else {
                        Toast.makeText(this@BooksActivity, "Create Books Gagal", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

