package com.app.service.reviewaza.call

import com.app.service.reviewaza.R
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object SearchRepository {

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(AppIntercepter())
        .build()

    // 코틀린과 Json을 매핑해주는 어댑터 생성
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://openapi.naver.com/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .client(okHttpClient)  // 로그캣에서 패킷 내용을 모니터링 할 수 있음
        .build()

    private val service = retrofit.create(SearchService::class.java)

    fun getDestinationLocation(query: String): Call<SearchResult> {
        return service.getDestinationLocation(query = "$query", display = 5)
    }

    class AppIntercepter: Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val clientId = MyApplication.applicationContext.getString(R.string.NAVER_SEARCH_CLIENT_ID)
            val clientSecret = MyApplication.applicationContext.getString(R.string.NAVER_SEARCH_CLIENT_SECRET)
            val newRequest = chain.request().newBuilder()
                .addHeader("X-Naver-Client-Id", clientId)
                .addHeader("X-Naver-Client-Secret", clientSecret)
                .build()
            return chain.proceed(newRequest)
        }
    }

}