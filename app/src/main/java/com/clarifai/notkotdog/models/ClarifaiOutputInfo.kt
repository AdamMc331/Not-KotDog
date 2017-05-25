package com.clarifai.notkotdog.models

import com.squareup.moshi.Json

/**
 * Created by adam.mcneilly on 5/25/17.
 */
data class ClarifaiOutputInfo(
        val message: String? = "",
        val type: String? = "",
        @Json(name = "type_ext") val typeExt : String? = ""
)