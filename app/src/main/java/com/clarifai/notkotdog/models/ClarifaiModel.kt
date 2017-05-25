package com.clarifai.notkotdog.models

import com.squareup.moshi.Json

/**
 * Created by adam.mcneilly on 5/25/17.
 */
data class ClarifaiModel(
        val id: String? = "",
        val name: String? = "",
        @Json(name = "created_at") val createdAt: String? = "",
        val app_id: String? = "",
        @Json(name = "output_info") val outputInfo: ClarifaiOutputInfo? = null,
        @Json(name = "model_version") val modelVersion: ClarifaiModelVersion? = null
)