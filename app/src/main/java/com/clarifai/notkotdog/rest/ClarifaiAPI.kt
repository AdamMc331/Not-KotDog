package com.clarifai.notkotdog.rest

import com.clarifai.notkotdog.models.AuthToken
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST


/**
 * Interface for connecting with the API.
 *
 * Created by adam.mcneilly on 5/23/17.
 */
interface ClarifaiAPI {

    @POST("/v2/token")
    fun authorize(@Body requestBody: RequestBody): Call<AuthToken>
}