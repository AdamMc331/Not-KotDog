package com.clarifai.notkotdog.models

/**
 * Created by adam.mcneilly on 5/25/17.
 */
data class ClarifaiImage(
        val base64: String? = "",
        val crop: List<Float>? = ArrayList(),
        val url: String? = ""
)