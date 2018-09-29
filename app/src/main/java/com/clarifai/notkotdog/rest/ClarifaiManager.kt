package com.clarifai.notkotdog.rest

import clarifai2.api.ClarifaiBuilder
import clarifai2.api.ClarifaiClient
import clarifai2.api.request.model.PredictRequest
import clarifai2.dto.input.ClarifaiInput
import clarifai2.dto.prediction.Prediction

/**
 * Manager that handles creating a connection to the API.
 *
 * Created by adam.mcneilly on 5/23/17.
 */
class ClarifaiManager(apiKey: String) {
    private val clarifaiApi: ClarifaiClient = ClarifaiBuilder(apiKey).buildSync()

    fun predict(modelId: String, input: ClarifaiInput): PredictRequest<Prediction> {
        return clarifaiApi.predict(modelId).withInputs(input)
    }
}