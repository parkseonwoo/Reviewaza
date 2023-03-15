package com.app.service.reviewaza.call

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface SearchService {

    @GET("v1/search/local.json")
    fun getDestinationLocation(
        @Query("query") query: String,
        @Query("display") display: Int,
    ) : Call<SearchResult>
}