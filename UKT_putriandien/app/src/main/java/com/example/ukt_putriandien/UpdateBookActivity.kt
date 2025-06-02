package com.example.ukt_putriandien

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.ukt_putriandien.api.ApiClient
import com.example.ukt_putriandien.databinding.ActivityUpdateBookBinding
import com.example.ukt_putriandien.request.PutBooksRequest
import kotlinx.coroutines.launch

class UpdateBookActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateBookBinding
    private var bookId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateBookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ambil data dari intent
        bookId = intent.getStringExtra("BOOK_ID")
        binding.etIsbn.setText(intent.getStringExtra("BOOK_ISBN"))
        binding.etTitle.setText(intent.getStringExtra("BOOK_TITLE"))
        binding.etAuthor.setText(intent.getStringExtra("BOOK_AUTHOR"))
        binding.etPublisher.setText(intent.getStringExtra("BOOK_PUBLISHER"))
        binding.etPublishedDate.setText(intent.getStringExtra("BOOK_PUBLISHED_DATE"))
        binding.etGenre.setText(intent.getStringExtra("BOOK_GENRE"))
        binding.etLanguage.setText(intent.getStringExtra("BOOK_LANGUAGE"))
        binding.etDescription.setText(intent.getStringExtra("BOOK_DESCRIPTION"))

        binding.btnUpdate.setOnClickListener {
            updateBook()
        }
    }

    private fun updateBook() {
        val isbn = binding.etIsbn.text.toString()
        val title = binding.etTitle.text.toString()
        val author = binding.etAuthor.text.toString()
        val publisher = binding.etPublisher.text.toString()
        val publishedDate = binding.etPublishedDate.text.toString()
        val genre = binding.etGenre.text.toString()
        val language = binding.etLanguage.text.toString()
        val description = binding.etDescription.text.toString()

        if (isbn.isEmpty() || title.isEmpty() || author.isEmpty() || publisher.isEmpty() ||
            publishedDate.isEmpty() || genre.isEmpty() || language.isEmpty() || description.isEmpty()
        ) {
            Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        val request = PutBooksRequest(
            isbn, title, author, publisher,
            publishedDate, genre, language, description
        )

        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("akses", "") ?: ""

        lifecycleScope.launch {
            try {
                val response = ApiClient.authService.updateBookById("Bearer $token", bookId ?: "", request)
                if (response.isSuccessful) {
                    Toast.makeText(this@UpdateBookActivity, "Buku berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    val resultIntent = Intent()
                    resultIntent.putExtra("UPDATED", true)
                    setResult(RESULT_OK, resultIntent)
                    finish() // kembali ke layar sebelumnya
                } else {
                    Toast.makeText(this@UpdateBookActivity, "Gagal memperbarui buku", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@UpdateBookActivity, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
