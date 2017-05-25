package com.clarifai.notkotdog.models

import com.squareup.moshi.Json

/**
 * All of the models used for the Clarifai Application.
 *
 * Created by adam.mcneilly on 5/25/17.
 */

data class AuthToken(
        @Json(name = "access_token") val accessToken: String? = "",
        @Json(name = "expires_in") val expiresIn: Int? = 0
)

data class ClarifaiConcept(
        val id: String? = "",
        val name: String? = "",
        val value: Float? = 0.0f,
        val app_id: String? = ""
)

data class ClarifaiImage(
        val base64: String? = "",
        val crop: List<Float>? = ArrayList(),
        val url: String? = ""
)

data class ClarifaiData(
        val concepts: List<ClarifaiConcept>? = ArrayList<ClarifaiConcept>(),
        val image: ClarifaiImage? = null
)

data class ClarifaiInput(
        val data: ClarifaiData? = null,
        val id: String? = ""
)

data class ClarifaiStatus(
        val code: Int? = 0,
        val description: String? = ""
)

data class ClarifaiModelVersion(
        val id: String? = "",
        @Json(name = "created_at") val createdAt: String? = "",
        val status: ClarifaiStatus? = null
)

data class ClarifaiOutputInfo(
        val message: String? = "",
        val type: String? = "",
        @Json(name = "type_ext") val typeExt : String? = ""
)

data class ClarifaiModel(
        val id: String? = "",
        val name: String? = "",
        @Json(name = "created_at") val createdAt: String? = "",
        val app_id: String? = "",
        @Json(name = "output_info") val outputInfo: ClarifaiOutputInfo? = null,
        @Json(name = "model_version") val modelVersion: ClarifaiModelVersion? = null
)

data class ClarifaiOutput(
        val id: String? = "",
        val status: ClarifaiStatus? = null,
        @Json(name = "created_at") val createdAt: String? = "",
        val model: ClarifaiModel? = null,
        val input: ClarifaiInput? = null,
        val data: ClarifaiData? = null
)

data class ClarifaiPredictRequest(
        val inputs: List<ClarifaiInput>? = ArrayList()
)

data class ClarifaiPredictResponse(
        val status: ClarifaiStatus? = null,
        val outputs: List<ClarifaiOutput>? = ArrayList()
)