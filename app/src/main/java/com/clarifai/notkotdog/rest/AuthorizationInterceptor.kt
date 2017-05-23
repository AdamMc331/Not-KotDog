package com.clarifai.notkotdog.rest

import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Response


/**
 * Intercepts a response and adds authorization as a header.
 *
 * Created by adam.mcneilly on 5/23/17.
 */
class AuthorizationInterceptor(val apiId: String, val apiSecret: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain?): Response {
        // Get request path.
        val uri = chain?.request()?.url()?.uri()
        val path = uri?.path

        val authValue: String
        if (path == "/v2/token") {
            authValue = Credentials.basic(apiId, apiSecret)
        } else {
            //TODO: Read from SharedPrefs
            authValue = "Bearer " + "TODO:"
        }

        val request = chain?.request()?.newBuilder()?.addHeader("Authorization", authValue)?.build()

        return chain?.proceed(request)!!
    }
}