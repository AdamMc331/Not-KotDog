package com.clarifai.notkotdog.models

data class ClarifaiStatus(val code: Int? = 0, val description: String? = "")

data class ClarifaiPredictResponse(
        val status: ClarifaiStatus? = null
)