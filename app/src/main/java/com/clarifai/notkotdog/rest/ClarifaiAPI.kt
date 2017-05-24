package com.clarifai.notkotdog.rest

import com.clarifai.notkotdog.models.AuthToken
import com.clarifai.notkotdog.models.ClarifaiPredictRequest
import com.clarifai.notkotdog.models.ClarifaiPredictResponse
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path


/**
 * Interface for connecting with the API.
 *
 * Created by adam.mcneilly on 5/23/17.
 */
interface ClarifaiAPI {

    @POST("/v2/token")
    fun authorize(@Body requestBody: RequestBody): Call<AuthToken>

    @POST("/v2/models/{model_id}/outputs")
    fun predict(@Path("model_id") modelId: String, @Body requestBody: ClarifaiPredictRequest): Call<ClarifaiPredictResponse>

    //https://api.clarifai.com/v2/models/aaa03c23b3724a16a56b629203edc62c/outputs
}