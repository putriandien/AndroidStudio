package com.example.ukt_putriandien.response


data class LoginResponse(
    val message: String,
    val accessToken: String,
    val refreshToken: String
)

data class RegisterResponse(
    val userId: Int,
    val message: String
)

data class LogoutResponse(
    val message: String
)
data class RefreshTokenResponse(
    val message: String,
    val accessToken: String
)
data class BooksResponseGet(
    val status: String,
    val message: String,
    val data: Books,
    val total: String,
    val page: String,
    val limit: String
)
data class Books(
    val books:List<BooksData>
)
data class BooksResponseGetbyId(
    val status: String,
    val message: String,
    val data: BooksData,
)
data class BooksData(
    val id: String,
    val isbn: String,
    val title: String,
    val author: String,
    val publisher: String,
    val published_date: String,
    val genre: String,
    val language: String,
    val description: String,
    val cover_image: BooksCover,
    val uploaded_by: String
)
data class BooksCover(
    val small: String,
    val medium: String,
    val large: String
)
data class CreateBooksResponse(
    val status: String,
    val message: String,
    val data: BooksDataCreate
)
data class BooksDataCreate(
    val id: String,
    val title: String,
    val author: String
)
data class UpdateBooksResponse(
    val status: String,
    val message: String,
    val data: BooksDataCreate
)