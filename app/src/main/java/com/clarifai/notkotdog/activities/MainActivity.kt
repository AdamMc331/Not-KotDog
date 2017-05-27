package com.clarifai.notkotdog.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Base64
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.clarifai.notkotdog.App
import com.clarifai.notkotdog.R
import com.clarifai.notkotdog.models.*
import com.clarifai.notkotdog.rest.ClarifaiManager
import com.squareup.moshi.Moshi
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream


class MainActivity : AppCompatActivity() {
    //region Properties
    var manager: ClarifaiManager? = null
    var resultView: TextView? = null
    var imageView: ImageView? = null
    var progressBar: ProgressBar? = null
    //endregion

    //region Lifecycle Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        manager = ClarifaiManager(this, getString(R.string.api_id), getString(R.string.api_secret))

        val toolbar = findViewById(R.id.toolbar) as? Toolbar
        setSupportActionBar(toolbar)

        val fab = findViewById(R.id.fab) as FloatingActionButton
        fab.setOnClickListener {
            AlertDialog.Builder(this)
                    .setMessage("Select An Image")
                    .setPositiveButton("Gallery") { _, _ -> startGalleryChooser() }
                    .setNegativeButton("Camera") { _, _ -> startCameraChooser() }
                    .create()
                    .show()
        }

        resultView = findViewById(R.id.result_view) as TextView
        imageView = findViewById(R.id.image_view) as ImageView
        progressBar = findViewById(R.id.progress_bar) as ProgressBar

        authorizeUser()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            val bytes = getImageBytes(data.data)
            predict(FOOD_MODEL, bytes)
        } else if (requestCode == CAMERA_IMAGE_REQUEST && resultCode == RESULT_OK) {
            val photoUri: Uri = FileProvider.getUriForFile(this, applicationContext.packageName + ".provider", getCameraFile())
            val bytes = getImageBytes(photoUri)
            predict(FOOD_MODEL, bytes)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            GALLERY_PERMISSIONS_REQUEST -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) startGalleryChooser()
            CAMERA_PERMISSIONS_REQUEST -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) startCameraChooser()
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }
    //endregion

    //region Clarifai API Methods
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

    private fun predict(modelId: String, imageBytes: ByteArray?) {
        // If bytes are null just return
        if (imageBytes == null) {
            return
        }

        // Clear out previous and show loading
        resultView?.visibility = View.GONE
        progressBar?.visibility = View.VISIBLE
        imageView?.setImageBitmap(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size))

        // Build out the request
        val image = ClarifaiImage(
                Base64.encodeToString(imageBytes, 0)
        )
        val data = ClarifaiData(image = image)
        val input = ClarifaiInput(data)
        val request = ClarifaiPredictRequest(arrayListOf(input))

        val call = manager?.predict(modelId, request)

        call?.enqueue(object : Callback<ClarifaiPredictResponse> {
            override fun onResponse(call: Call<ClarifaiPredictResponse>?, response: Response<ClarifaiPredictResponse>?) {
                Timber.v("Success!")
                Timber.v("${response?.body()}")

                val matchedConcept = response?.body()?.outputs?.first()?.data?.concepts?.any { it.name == HOTDOG_KEY } ?: false

                val resultTextResource = if (matchedConcept) R.string.hotdog_success else R.string.hotdog_failure
                val resultColorResource = if (matchedConcept) R.color.green else R.color.red

                resultView?.text = getString(resultTextResource)
                resultView?.setBackgroundColor(ContextCompat.getColor(this@MainActivity, resultColorResource))
                resultView?.visibility = View.VISIBLE
                progressBar?.visibility = View.GONE
            }

            override fun onFailure(call: Call<ClarifaiPredictResponse>?, t: Throwable?) {
                Timber.e(t)

                resultView?.text = getString(R.string.hotdog_error)
                resultView?.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.red))
                resultView?.visibility = View.VISIBLE
                progressBar?.visibility = View.GONE
            }
        })
    }
    //endregion

    //region Image Methods
    private fun startGalleryChooser() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select a photo"),
                    GALLERY_IMAGE_REQUEST)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), GALLERY_PERMISSIONS_REQUEST)
        }
    }

    private fun startCameraChooser() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val photoUri = FileProvider.getUriForFile(this, applicationContext.packageName + ".provider", getCameraFile())
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivityForResult(intent, CAMERA_IMAGE_REQUEST)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSIONS_REQUEST)
        }
    }

    private fun getCameraFile(): File {
        val dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File(dir, FILE_NAME)
    }

    private fun getImageBytes(uri: Uri): ByteArray? {
        var inStream: InputStream? = null
        var bitmap: Bitmap? = null

        try {
            inStream = contentResolver.openInputStream(uri)
            bitmap = BitmapFactory.decodeStream(inStream)
            val outStream = ByteArrayOutputStream()
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
            return outStream.toByteArray()
        } catch (e: FileNotFoundException) {
            return null
        } finally {
            inStream?.close()
            bitmap?.recycle()
        }
    }
    //endregion

    companion object {
        private val MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf8")
        private val GRANT_TYPE_CREDENTIALS = "\"grant_type\":\"client_credentials\""

        private val GALLERY_PERMISSIONS_REQUEST = 0
        private val GALLERY_IMAGE_REQUEST = 1
        private val CAMERA_PERMISSIONS_REQUEST = 2
        private val CAMERA_IMAGE_REQUEST = 3

        private val GENERAL_MODEL = "aaa03c23b3724a16a56b629203edc62c"
        private val FOOD_MODEL = "bd367be194cf45149e75f01d59f77ba7"

        private val HOTDOG_KEY = "hot dog"
        private val HOTDOG_URL = "http://i.imgur.com/bKHCL1u.png"

        private val FILE_NAME = "temp.jpg"
    }
}
