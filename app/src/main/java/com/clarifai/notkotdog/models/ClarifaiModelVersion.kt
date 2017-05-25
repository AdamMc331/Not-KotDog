package com.clarifai.notkotdog.models

import com.squareup.moshi.Json

/**
 * Created by adam.mcneilly on 5/25/17.
 */
data class ClarifaiModelVersion(
        val id: String? = "",
        @Json(name = "created_at") val createdAt: String? = "",
        val status: ClarifaiStatus? = null
)