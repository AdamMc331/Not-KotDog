package com.clarifai.notkotdog.models

/**
 * Created by adam.mcneilly on 5/25/17.
 */
data class ClarifaiPredictRequest(
        val inputs: List<ClarifaiInput>? = ArrayList()
)