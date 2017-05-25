package com.clarifai.notkotdog.models

/**
 * Created by adam.mcneilly on 5/25/17.
 */
data class ClarifaiConcept(
        val id: String? = "",
        val name: String? = "",
        val value: Float? = 0.0f,
        val app_id: String? = ""
)