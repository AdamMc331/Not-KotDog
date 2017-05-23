package com.clarifai.notkotdog

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.clarifai.notkotdog.models.AuthResponse
import com.clarifai.notkotdog.rest.ClarifaiManager
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        val fab = findViewById(R.id.fab) as FloatingActionButton
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
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
        val manager = ClarifaiManager(getString(R.string.api_id), getString(R.string.api_secret))

        val call = manager.authorize(RequestBody.create(MEDIA_TYPE_JSON, "\"grant_type\":\"client_credentials\""))

        call.enqueue(object : Callback<AuthResponse> {
            override fun onFailure(call: Call<AuthResponse>?, t: Throwable?) {
                Log.e(MainActivity::class.java.simpleName, t?.message, t)
            }

            override fun onResponse(call: Call<AuthResponse>?, response: Response<AuthResponse>?) {
                Log.v(MainActivity::class.java.simpleName, "Success! Token ${response?.body()?.accessToken}")
            }
        })
    }

    companion object {
        private val MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf8")
    }
}
