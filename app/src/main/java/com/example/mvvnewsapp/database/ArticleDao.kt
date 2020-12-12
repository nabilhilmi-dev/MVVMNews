package com.example.mvvnewsapp.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.mvvnewsapp.model.Article

@Dao
interface ArticleDao {
    @Insert (onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(article: Article): Long

    @Query("SELECT * FROM articles")
    fun gerAllArticles(): LiveData<List<Article>>

    @Delete
    suspend fun deleteArticle(article: Article)
}