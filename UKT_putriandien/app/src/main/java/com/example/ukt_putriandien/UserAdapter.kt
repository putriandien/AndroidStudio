package com.example.ukt_putriandien

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ukt_putriandien.response.BooksData

class UserAdapter(private val userList: List<BooksData>,
                  private val onItemClick: (BooksData) -> Unit) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewCover: ImageView = itemView.findViewById(R.id.imageViewCover)
        val tvId: TextView = itemView.findViewById(R.id.tvID)
        val tvIsbn: TextView = itemView.findViewById(R.id.tvISBN)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvAuthor: TextView = itemView.findViewById(R.id.tvAuthor)
        val tvPublisher: TextView = itemView.findViewById(R.id.tvPublisher)
        val tvPublished_date: TextView = itemView.findViewById(R.id.tvPublished_date)
        val tvGenre: TextView = itemView.findViewById(R.id.tvGenre)
        val tvLanguage: TextView = itemView.findViewById(R.id.tvLanguage)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val tvUpload_by: TextView = itemView.findViewById(R.id.tvUpload_by)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val book = userList[position]

        // Set teks
        holder.tvId.text = book.id
        holder.tvIsbn.text = book.isbn
        holder.tvTitle.text = book.title
        holder.tvAuthor.text = book.author
        holder.tvPublisher.text = book.publisher
        holder.tvPublished_date.text = book.published_date
        holder.tvGenre.text = book.genre
        holder.tvLanguage.text = book.language
        holder.tvDescription.text = book.description
        holder.tvUpload_by.text = book.uploaded_by

        // Load gambar ke imageViewCover dengan Glide
        Glide.with(holder.itemView.context)
            .load(book.cover_image.large)  // Pastikan ini adalah URL atau path gambar
            .placeholder(R.drawable.placeholder_image)  // Gambar placeholder
            .error(R.drawable.error_image)  // Gambar error jika gagal load
            .into(holder.imageViewCover)

        holder.itemView.setOnClickListener {
            onItemClick(book)
        }
    }

    override fun getItemCount() = userList.size
}
