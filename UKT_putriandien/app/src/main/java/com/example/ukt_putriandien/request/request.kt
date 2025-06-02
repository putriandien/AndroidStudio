package com.example.ukt_putriandien.request

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val phone: String,
    val password: String
)

data class RefreshTokenRequest(
    val refreshTokenRequest: String
)

data class BooksRequest(
    val isbn: String,
    val title: String,
    val author: String,
    val publisher: String,
    val published_date: String,
    val genre: String,
    val language: String,
    val description: String
)
data class PutBooksRequest(
    val isbn: String,
    val title: String,
    val author: String,
    val publisher: String,
    val published_date: String,
    val genre: String,
    val language: String,
    val description: String
)
