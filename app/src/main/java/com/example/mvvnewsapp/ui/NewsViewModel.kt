package com.example.mvvnewsapp.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.TYPE_MOBILE
import android.net.ConnectivityManager.TYPE_WIFI
import android.net.NetworkCapabilities.*
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mvvnewsapp.NewsApplication
import com.example.mvvnewsapp.model.Article
import com.example.mvvnewsapp.model.NewsResponse
import com.example.mvvnewsapp.repository.NewsRepository
import com.example.mvvnewsapp.util.Resource
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class NewsViewModel(
    app: Application,
    val newsRepository: NewsRepository)
    :AndroidViewModel(app) {

    val breakingNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var breakingNewsPage = 1
    var breakingNewsResponse: NewsResponse? = null

    val searchNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var searchNewsPage = 1
    var searchNewsResponse: NewsResponse? = null


    init {
        getBreakingNews("id")
    }

    fun saveArticle(article: Article) = viewModelScope.launch {
        newsRepository.uspert(article)
    }
    fun getsaveNews() = newsRepository.getSaveNews()

    fun deleteArticle(article: Article) = viewModelScope.launch {
        newsRepository.deleteArticle(article)
    }

    fun getBreakingNews(countryCode: String) = viewModelScope.launch {
      safeBreakingNewsCall(countryCode)
    }
    fun searchNews(searchQuery: String) = viewModelScope.launch {
        safeSearchNewsCall(searchQuery)

    }
    private fun handleBreakingNewsResponse(response : Response<NewsResponse>): Resource<NewsResponse>{
        if (response.isSuccessful){
            response.body()?.let { resultResponse ->
                breakingNewsPage++
                if (breakingNewsResponse == null){
                    breakingNewsResponse = resultResponse
                }else{
                    val oldArticle = breakingNewsResponse?.articles
                    val newArticle = resultResponse.articles
                    oldArticle?.addAll(newArticle)
                }

                return  Resource.Success(breakingNewsResponse ?: resultResponse)
            }
        }

        return Resource.Error(response.message())
    }
    private fun handleSearchNewsResponse(response : Response<NewsResponse>): Resource<NewsResponse>{
        if (response.isSuccessful){
            response.body()?.let { resultResponse ->
                searchNewsPage++
                if (searchNewsResponse == null){
                    searchNewsResponse = resultResponse
                }else{
                    val oldArticle = searchNewsResponse?.articles
                    val newArticle = resultResponse.articles
                    oldArticle?.addAll(newArticle)
                }

                return  Resource.Success(searchNewsResponse ?: resultResponse)
            }
        }

        return Resource.Error(response.message())
    }

    private fun hasInternetConnection(): Boolean{
        val connectivityManager = getApplication<NewsApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        )as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            val activityNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(activityNetwork) ?: return false
            return when{
                capabilities.hasTransport(TRANSPORT_WIFI) -> true
                capabilities.hasTransport(TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(TRANSPORT_ETHERNET) -> true
                else -> false

            }
        }else{
            connectivityManager.activeNetworkInfo?.run {
               return when(type){
                   TYPE_WIFI -> true
                   TYPE_MOBILE -> true
                   TRANSPORT_ETHERNET -> true
                   else -> false

               }
            }
        }

        return false
    }

    private suspend fun safeBreakingNewsCall(countryCode: String){
        breakingNews.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()){
                val respone = newsRepository.getBreakingNews(countryCode, breakingNewsPage)
                breakingNews.postValue(handleBreakingNewsResponse(respone))
            }else{
                breakingNews.postValue(Resource.Error("No Internet Connection"))
            }
        }catch (t:Throwable){
            when(t){
                is IOException ->breakingNews.postValue(Resource.Error("Network Failure"))
                else -> breakingNews.postValue(Resource.Error("Conversion Error"))
            }
        }
    }

    private suspend fun safeSearchNewsCall(countryCode: String){
        searchNews.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()){
                val respone = newsRepository.searchNews(countryCode, searchNewsPage)
                searchNews.postValue(handleSearchNewsResponse(respone))
            }else{
                searchNews.postValue(Resource.Error("No Internet Connection"))
            }
        }catch (t:Throwable){
            when(t){
                is IOException ->searchNews.postValue(Resource.Error("Network Failure"))
                else -> searchNews.postValue(Resource.Error("Conversion Error"))
            }
        }
    }

}