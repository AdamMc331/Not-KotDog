# Not KotDog - Using Clarifai To Detect Hot Dogs In Kotlin

This post is going to show you how [Kotlin](https://kotlinlang.org/), a programming language from JetBrains and now officially supported for Android development, can make it very easy to implement a RESTful API like Clarifai's!

We will be assuming some basic knowledge of Android to get off the ground, but if you are unfamiliar you can learn about building your first app [here](https://developer.android.com/training/basics/firstapp/index.html). If you have a version of Android Studio prior to 3.0, you can refer to thes guides on [starting a Kotlin project](https://kotlinlang.org/docs/tutorials/kotlin-android.html).
 
## Defining Our Models

Let's start by discussing our model objects. If you take a look at the [Clarifai Predict Documentation](https://developer.clarifai.com/guide/predict#predict) you'll see the body of the cUrl request looks like this:

```json
{
    "inputs": [
        {
            "data": {
                "image": {
                  "base64": "'"$(base64 /home/user/image.jpeg)"'"
                }
            }
        }
    ]
}
```

What we have here is an image object, within a data object, within an input object, that is part of an array. So ultimately we will need four classes here. I will call each of them `ClarifaiImage`, `ClarifaiData`, `ClarifaiInput`, `ClarifaiPredictRequest`, respectfully. Here is how they will be defined in Kotlin:

```kotlin
data class ClarifaiImage(val base64: String? = "")
 
data class ClarifaiData(val image: ClarifaiImage? = null)
 
data class ClarifaiInput(val data: ClarifaiData? = null)
 
data class ClarifaiPredictRequest(val inputs: List<ClarifaiInput>? = ArrayList())
```

Yes, it is true, each of these classes only needs a single line! Kotlin provides us with [data classes](https://kotlinlang.org/docs/reference/data-classes.html) which provide default implementations for common methods such as `toString()`, `equals()`, and `copy()`. Kotlin classes already provide us with getter and setter methods. Another benefit of Kotlin as opposed to some other languages is the use of default parameters in the constructor. If we look at `ClarifaiImage`, for example - the constructor takes in an argument for the base64 value, but if it is not passed in it will be assigned to an empty string. You can learn more about those [here](https://kotlinlang.org/docs/reference/functions.html#default-arguments).

In addition to all of those, we need to make an `AuthToken` class that will come back from our authorization call, discussed next:

```kotlin
data class AuthToken(
        @Json(name = "access_token") val accessToken: String? = "",
        @Json(name = "expires_in") val expiresIn: Int? = 0
)
```

Notice that in this class, we use the `@Json(name = "")` annotation to specify what the JSON key is for a field. If you don't specify this, Retrofit will just use the variable name. In this case, though, the JSON convention conflicts with Kotlin variable name convention, so we're using the annotation to override that.

If you would like to see all of the model classes for this project, including the ones used for a `ClarifaiPredictResponse`, you can view them [here](https://github.com/AdamMc331/Not-KotDog/blob/master/app/src/main/java/com/clarifai/notkotdog/models/ClarifaiModels.kt).

## Retrofit & Authorization

Now that we've defined the necessary models used to make our calls,  we need to implement in our app is an authorization call. We will do so using [Retrofit](http://square.github.io/retrofit/), an HTTP Client for Android that was built by Square. This is an industry standard library used for making network requests. Let's start by adding the necessary dependencies into our `build.gradle` file:

```groovy
compile 'com.squareup.retrofit2:retrofit:2.1.0'
compile 'com.squareup.retrofit2:converter-moshi:2.1.0'
compile 'com.squareup.okhttp3:logging-interceptor:3.3.1'
compile 'com.jakewharton.timber:timber:4.5.1'
```

While only the first two are required, I've included a logging interceptor for the HTTP calls for debug purposes, as well as a common logging library by Jake Wharton called [Timber](https://github.com/JakeWharton/timber).

To implement Retrofit, we start by creating an interface that defines any calls we want to make. So far, we'll need one for `authorize()` that takes in a `RequestBody` object, and will return an `AuthToken` result. Here is what the interface code looks like:

```kotlin
interface ClarifaiAPI {
    @POST("/v2/token")
    fun authorize(@Body requestBody: RequestBody): Call<AuthToken>
}
```

The annotation is what tells retrofit that this is a POST request, and provides any extension onto the base URL. Where is the base URL coming from? I'm glad you asked! Let's build our ClarifaiManager class!

```kotlin
class ClarifaiManager(context: Context, apiId: String, apiSecret: String) {
    private val clarifaiApi: ClarifaiAPI

    init {
        val authInterceptor = AuthorizationInterceptor(apiId, apiSecret, context)
        val loggingInterceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient.Builder().addInterceptor(authInterceptor).addInterceptor(loggingInterceptor).build()

        val retrofit = Retrofit.Builder()
                .baseUrl("https://api.clarifai.com/")
                .addConverterFactory(MoshiConverterFactory.create())
                .client(client)
                .build()

        clarifaiApi = retrofit.create(ClarifaiAPI::class.java)
    }

    fun authorize(requestBody: RequestBody): Call<AuthToken> {
        return clarifaiApi.authorize(requestBody)
    }
}
```

The `ClarifaiManager.kt` class maintains a reference to our `ClarifaiApi` interface. This class defines the OkHttp client we want to use, and any initializations. Here we define our logging intercepter, an authorization interceptor (explained next), and our client which has a base url of "https://api.clarifai.com/" and uses [Moshi](https://github.com/square/moshi) to convert the JSON response to our Kotlin objects.

The `AuthorizationInterceptor.kt` file is an interceptor class that will intercept all outgoing Retrofit calls, and preform any necessary actions. In this case, we know that we need to include an Authorization header on every call, so defining this in an interceptor is easier than applying it to every call in the `ClarifaiApi.kt` interface. Here is the code for the interceptor:

```kotlin
class AuthorizationInterceptor(val apiId: String, val apiSecret: String, val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain?): Response {
        // Get request path.
        val uri = chain?.request()?.url()?.uri()
        val path = uri?.path

        val authValue: String
        if (path == "/v2/token") {
            authValue = Credentials.basic(apiId, apiSecret)
        } else {
            val prefs = context.getSharedPreferences(App.PREFS_NAME, Context.MODE_PRIVATE)
            val authString = prefs.getString(App.AUTH_TOKEN_KEY, "")
            val authResponse = Moshi.Builder().build().adapter(AuthToken::class.java).fromJson(authString)
            authValue = "Bearer ${authResponse?.accessToken}"
        }

        val request = chain?.request()?.newBuilder()?.addHeader("Authorization", authValue)?.build()

        return chain?.proceed(request)!!
    }
}
```

The class accepts two strings, which are your API ID and API Secret, as well as a context which is used for shared preferences. Our interceptor does one of two things:
1. If we are trying to hit the token endpoint, we use basic authorization credentials.
2. If we are trying to access any other endpoint, we use the authorization token that's been stored in shared preferences. We read back the `AuthToken.kt` object as a string and use Moshi to convert it back to an object. We'll discuss how to save that next.

Now that we have our Retrofit service defined, it's time to implement it. We'll do this in our `MainActivity.kt` file inside the `onCreate()` method. Here is a snippet of our activity file that is relevent up to this point:

```kotlin
class MainActivity : AppCompatActivity() {
    var manager: ClarifaiManager? = null
 
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
 
        manager = ClarifaiManager(this, getString(R.string.api_id), getString(R.string.api_secret))
 
        authorizeUser()
    }
 
    private fun authorizeUser() {
        val call = manager?.authorize(RequestBody.create(MEDIA_TYPE_JSON, GRANT_TYPE_CREDENTIALS))
 
        call?.enqueue(object : Callback<AuthToken> {
            override fun onFailure(call: Call<AuthToken>?, t: Throwable?) {
                Timber.e(t)
            }
 
            override fun onResponse(call: Call<AuthToken>?, response: Response<AuthToken>?) {
                Timber.v("Success! Token ${response?.body()?.accessToken}")
 
                val authString = Moshi.Builder().build().adapter(AuthToken::class.java).toJson(response?.body())
                val prefs = getSharedPreferences(App.PREFS_NAME, Context.MODE_PRIVATE)
                val editor = prefs.edit()
                editor.putString(App.AUTH_TOKEN_KEY, authString)
                editor.apply()
            }
        })
    }
 
    companion object {
        private val MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf8")
        private val GRANT_TYPE_CREDENTIALS = "\"grant_type\":\"client_credentials\""
    }
}
```

## Break Point

This would be a good point to pause from the tutorial and test that your application works. Before you run it, here are some additional steps that didn't get covered:
1. Include the internet permission in your `AndroidManifest.xml` file by adding `<uses-permission android:name="android.permission.INTERNET" />` outside of the `<application>` tag.
2. Add an `App.kt` file which defines your application and has some constants and the Timber setup. You can copy the source [here](https://github.com/AdamMc331/Not-KotDog/blob/master/app/src/main/java/com/clarifai/notkotdog/App.kt).
3. Following the tutorial, you should now be able to run your app. When the activity starts, you should see something similar to this in your logcat:
    1. > 05-25 13:54:34.619 24830-24830/com.clarifai.notkotdog V/MainActivity$authorizeU: Success! Token jU85Sdyz2moNlGOK6Pl4MVHEu2ZJJj
    
If you experienced any errors, please double check the source code from GitHub and let us know in the comments so we can update the tutorial accordingly.