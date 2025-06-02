package com.example.ukt_putriandien

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.ukt_putriandien.api.ApiClient
import com.example.ukt_putriandien.databinding.ActivityDetailBinding
import kotlinx.coroutines.launch
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.ukt_putriandien.response.BooksData

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private var currentBook: BooksData? = null
    private lateinit var editBookLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bookId = intent.getStringExtra("BOOK_ID")
        if (bookId != null) {
            getBooksById(bookId)
        }
        binding.btnDelete.setOnClickListener {
            val bookId = intent.getStringExtra("BOOK_ID")
            if (bookId != null) {
                showDeleteConfirmationDialog(bookId)
            }
        }
        binding.btnEdit.setOnClickListener {
            val book = currentBook
            if (book != null) {
                val intent = Intent(this@DetailActivity, UpdateBookActivity::class.java).apply {
                    putExtra("BOOK_ID", book.id)
                    putExtra("BOOK_ISBN", book.isbn)
                    putExtra("BOOK_TITLE", book.title)
                    putExtra("BOOK_AUTHOR", book.author)
                    putExtra("BOOK_PUBLISHER", book.publisher)
                    putExtra("BOOK_PUBLISHED_DATE", book.published_date)
                    putExtra("BOOK_GENRE", book.genre)
                    putExtra("BOOK_LANGUAGE", book.language)
                    putExtra("BOOK_DESCRIPTION", book.description)
                }
                editBookLauncher.launch(intent)
            }
        }
        editBookLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val updated = data?.getBooleanExtra("UPDATED", false) ?: false
                if (updated) {
                    val bookId = intent.getStringExtra("BOOK_ID")
                    if (bookId != null) {
                        getBooksById(bookId) // reload data detail
                    }
                }
            }
        }
    }

    private fun getBooksById(id: String) {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val accessToken = sharedPreferences.getString("akses", "") ?: ""

        lifecycleScope.launch {
            try {
                val response = ApiClient.authService.getBooksById("Bearer $accessToken", id)
                if (response.isSuccessful) {
                    val book = response.body()?.data
                    if (book != null) {
                        currentBook = book

                        binding.tvID.text = "ID: ${book.id}"
                        binding.tvISBN.text = "ISBN: ${book.isbn}"
                        binding.tvTitle.text = "Judul Buku: ${book.title}"
                        binding.tvAuthor.text = "Penulis: ${book.author}"
                        binding.tvPublisher.text = "Penerbit: ${book.publisher}"
                        binding.tvPublishedDate.text = "Tanggal Terbit: ${book.published_date}"
                        binding.tvGenre.text = "Genre: ${book.genre}"
                        binding.tvLanguage.text = "Bahasa: ${book.language}"
                        binding.tvDescription.text = "Deskripsi buku: ${book.description}"
                        binding.tvUploadBy.text = "Diunggah oleh: ${book.uploaded_by}"

                        Glide.with(this@DetailActivity)
                            .load(book.cover_image.large)  // Pastikan ini adalah URL atau path gambar
                            .placeholder(R.drawable.placeholder_image)  // Gambar placeholder
                            .error(R.drawable.error_image)  // Gambar error jika gagal load
                            .into(binding.imageViewCover)
                    }
                } else {
                    Log.e("BookDetail", "Response failed: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("BookDetail", "Error: ${e.message}")
            }
        }
    }
    private fun deleteBookById(id: String) {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val accessToken = sharedPreferences.getString("akses", "") ?: ""

        lifecycleScope.launch {
            try {
                val response = ApiClient.authService.deleteBookById("Bearer $accessToken", id)
                if (response.isSuccessful) {
                    Toast.makeText(this@DetailActivity, "Buku berhasil dihapus", Toast.LENGTH_SHORT).show()
                    finish() // Kembali ke halaman sebelumnya
                } else {
                    Log.e("DeleteBook", "Gagal menghapus: ${response.code()}")
                    Toast.makeText(this@DetailActivity, "Gagal menghapus buku", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("DeleteBook", "Error: ${e.message}")
                Toast.makeText(this@DetailActivity, "Terjadi kesalahan", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun showDeleteConfirmationDialog(bookId: String) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Konfirmasi Hapus")
        builder.setMessage("Apakah kamu yakin ingin menghapus buku ini?")
        builder.setPositiveButton("Ya") { dialog, _ ->
            deleteBookById(bookId)
            dialog.dismiss()
        }
        builder.setNegativeButton("Tidak") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()

    }
}
