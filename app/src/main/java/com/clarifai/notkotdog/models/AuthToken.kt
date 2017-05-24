package com.clarifai.notkotdog.models

import com.squareup.moshi.Json

/**
 * Represents an authorization token result returned by the Clarifai API.
 *
 * Created by adam.mcneilly on 5/23/17.
 */
data class AuthToken(
        @Json(name = "access_token") val accessToken: String? = "",
        @Json(name = "expires_in") val expiresIn: Int? = 0
)