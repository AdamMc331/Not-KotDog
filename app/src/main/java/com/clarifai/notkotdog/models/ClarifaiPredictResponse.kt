package com.clarifai.notkotdog.models

/**
 * Created by adam.mcneilly on 5/25/17.
 */
data class ClarifaiPredictResponse(
        val status: ClarifaiStatus? = null,
        val outputs: List<ClarifaiOutput>? = ArrayList()
)