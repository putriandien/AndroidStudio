package com.example.ukt_putriandien.api

import com.example.ukt_putriandien.request.BooksRequest
import com.example.ukt_putriandien.request.LoginRequest
import com.example.ukt_putriandien.request.PutBooksRequest
import com.example.ukt_putriandien.request.RefreshTokenRequest
import com.example.ukt_putriandien.request.RegisterRequest
import com.example.ukt_putriandien.response.BooksResponseGet
import com.example.ukt_putriandien.response.BooksResponseGetbyId
import com.example.ukt_putriandien.response.CreateBooksResponse
import com.example.ukt_putriandien.response.LoginResponse
import com.example.ukt_putriandien.response.LogoutResponse
import com.example.ukt_putriandien.response.RefreshTokenResponse
import com.example.ukt_putriandien.response.RegisterResponse
import com.example.ukt_putriandien.response.UpdateBooksResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface AuthService {
    @POST("login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>
    @POST("register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<RegisterResponse>
    @POST("token-refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): Response<RefreshTokenResponse>
    @GET("books")
    suspend fun getBooks(
        @Header ("Authorization") token: String,
        @Query("limit") limit: Int = 1000
    ): Response<BooksResponseGet>
    @POST("books")
    suspend fun books(
        @Header("Authorization") token: String,
        @Body request: BooksRequest
    ): Response<CreateBooksResponse>
    @GET("books/{id}")
    suspend fun getBooksById(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<BooksResponseGetbyId>
    @DELETE("books/{id}")
    suspend fun deleteBookById(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Void>
    @PUT("books/{id}")
    suspend fun updateBookById(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: PutBooksRequest
    ): Response<UpdateBooksResponse>
    @POST("logout")
    suspend fun logout(@Header("Authorization") token: String): Response<LogoutResponse>
}