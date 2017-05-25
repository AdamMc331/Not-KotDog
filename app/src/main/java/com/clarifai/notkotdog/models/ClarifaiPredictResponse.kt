package com.clarifai.notkotdog.models

data class ClarifaiPredictResponse(
        val status: ClarifaiStatus? = null,
        val outputs: List<ClarifaiOutput>? = ArrayList()
)