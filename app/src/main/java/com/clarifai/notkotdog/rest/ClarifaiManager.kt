package com.clarifai.notkotdog.rest

import android.content.Context
import com.clarifai.notkotdog.models.AuthToken
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Manager that handles creating a connection to the API.
 *
 * Created by adam.mcneilly on 5/23/17.
 */
class ClarifaiManager(apiId: String, apiSecret: String, context: Context) {
    private val clarifaiApi: ClarifaiAPI

    init {
        val authInterceptor = AuthorizationInterceptor(apiId, apiSecret, context)
        val loggingInterceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient.Builder().addInterceptor(authInterceptor).addInterceptor(loggingInterceptor).build()

        val retrofit = Retrofit.Builder()
                .baseUrl("https://api.clarifai.com/")
                .addConverterFactory(MoshiConverterFactory.create())
                .client(client)
                .build()

        clarifaiApi = retrofit.create(ClarifaiAPI::class.java)
    }

    fun authorize(requestBody: RequestBody): Call<AuthToken> {
        return clarifaiApi.authorize(requestBody)
    }
}