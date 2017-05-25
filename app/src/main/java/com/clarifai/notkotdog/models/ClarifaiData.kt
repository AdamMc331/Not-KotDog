package com.clarifai.notkotdog.models

/**
 * Created by adam.mcneilly on 5/25/17.
 */
data class ClarifaiData(
        val concepts: List<ClarifaiConcept>? = ArrayList<ClarifaiConcept>(),
        val image: ClarifaiImage? = null
)