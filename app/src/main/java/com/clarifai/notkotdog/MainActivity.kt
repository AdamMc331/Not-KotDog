package com.clarifai.notkotdog

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import com.clarifai.notkotdog.models.AuthToken
import com.clarifai.notkotdog.models.ClarifaiPredictRequest
import com.clarifai.notkotdog.models.ClarifaiPredictResponse
import com.clarifai.notkotdog.rest.ClarifaiManager
import com.squareup.moshi.Moshi
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.io.File


class MainActivity : AppCompatActivity() {
    var manager: ClarifaiManager? = null

    //TODO: aaa03c23b3724a16a56b629203edc62c - general

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        manager = ClarifaiManager(getString(R.string.api_id), getString(R.string.api_secret), this)

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

    private fun authorizeUser() {
        val call = manager?.authorize(RequestBody.create(MEDIA_TYPE_JSON, GRANT_TYPE_CREDENTIALS))

        call?.enqueue(object : Callback<AuthToken> {
            override fun onFailure(call: Call<AuthToken>?, t: Throwable?) {
                Timber.e(t)
            }

            override fun onResponse(call: Call<AuthToken>?, response: Response<AuthToken>?) {
                Timber.v("Success! Token ${response?.body()?.accessToken}")

                val authString = Moshi.Builder().build().adapter(AuthToken::class.java).toJson(response?.body())
                val prefs = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
                val editor = prefs.edit()
                editor.putString(Constants.AUTH_TOKEN_KEY, authString)
                editor.apply()
            }
        })
    }

    private fun predict(modelId: String, imageBytes: ByteArray) {
        //TODO: Build this
        val request = ClarifaiPredictRequest()

        val call = manager?.predict(modelId, request)

        call?.enqueue(object : Callback<ClarifaiPredictResponse> {
            override fun onResponse(call: Call<ClarifaiPredictResponse>?, response: Response<ClarifaiPredictResponse>?) {
                Timber.v("Success!")
            }

            override fun onFailure(call: Call<ClarifaiPredictResponse>?, t: Throwable?) {
                Timber.e(t)
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            //TODO:
            //uploadImage(data.getData());
        } else if (requestCode == CAMERA_IMAGE_REQUEST && resultCode == RESULT_OK) {
            val photoUri: Uri = FileProvider.getUriForFile(this, applicationContext.packageName + ".provider", getCameraFile());
            //TODO:
            //uploadImage(photoUri);
        }
    }

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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            GALLERY_PERMISSIONS_REQUEST -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) startGalleryChooser()
            CAMERA_PERMISSIONS_REQUEST -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) startCameraChooser()
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    companion object {
        private val MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf8")
        private val GRANT_TYPE_CREDENTIALS = "\"grant_type\":\"client_credentials\""

        private val GALLERY_PERMISSIONS_REQUEST = 0
        private val GALLERY_IMAGE_REQUEST = 1
        private val CAMERA_PERMISSIONS_REQUEST = 2
        private val CAMERA_IMAGE_REQUEST = 3

        private val FILE_NAME = "temp.jpg"
    }
}
