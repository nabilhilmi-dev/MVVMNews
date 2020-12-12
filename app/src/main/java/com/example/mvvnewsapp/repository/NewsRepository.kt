package com.example.mvvnewsapp.repository

import com.example.mvvnewsapp.database.ArticleDataBase
import com.example.mvvnewsapp.model.Article
import com.example.mvvnewsapp.network.RetrofitIntance

class NewsRepository(val db: ArticleDataBase) {
    //get semua data untuk di tampilkan difragment breaking news
    suspend fun getBreakingNews(countryCode: String, pageNumber: Int) =
        RetrofitIntance.api.getBreakingNews(countryCode, pageNumber)

    suspend fun searchNews(searchQuery: String,pageNumber: Int)=
        RetrofitIntance.api.searchNews(searchQuery,pageNumber)

    //membuat databse baru di local database
    suspend fun uspert(article: Article) =db.getArticleDao().upsert(article)

    //untuk get semua data yg sudah di bookmark
    fun getSaveNews() = db.getArticleDao().gerAllArticles()

    //delet database local
    suspend fun deleteArticle(article: Article) = db.getArticleDao().deleteArticle(article)
}