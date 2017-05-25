package com.clarifai.notkotdog.models

import com.squareup.moshi.Json

/**
 * Created by adam.mcneilly on 5/25/17.
 */
data class ClarifaiOutput(
        val id: String? = "",
        val status: ClarifaiStatus? = null,
        @Json(name = "created_at") val createdAt: String? = "",
        val model: ClarifaiModel? = null,
        val input: ClarifaiInput? = null,
        val data: ClarifaiData? = null
)