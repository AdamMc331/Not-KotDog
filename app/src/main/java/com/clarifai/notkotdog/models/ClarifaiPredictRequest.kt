package com.clarifai.notkotdog.models

class ClarifaiConcept

class ClarifaiImage(val base64: String? = "", val crop: FloatArray? = FloatArray(0))

class ClarifaiData(val concepts: List<ClarifaiConcept>? = ArrayList<ClarifaiConcept>(), val image: ClarifaiImage? = null)

class ClarifaiInput(val data: ClarifaiData? = null)

class ClarifaiRequest(val inputs: List<ClarifaiInput>? = ArrayList())