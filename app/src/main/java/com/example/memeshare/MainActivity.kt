package com.example.memeshare

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.LruCache
import android.view.View
import android.view.inputmethod.InputBinding
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.ImageLoader
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.JsonRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.example.memeshare.databinding.ActivityMainBinding
import org.chromium.net.CronetEngine
import org.chromium.net.UrlRequest
import org.chromium.net.UrlResponseInfo
import java.nio.ByteBuffer
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var currentImgUrl: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadmeme()
    }

      fun loadmeme(){
        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(this)
        val url = "https://meme-api.com/gimme"

        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                this.currentImgUrl = response.getString("url")
                Glide.with(this).load(currentImgUrl).into(binding.imageView)
            },
            { error ->
                // TODO: Handle error
            }
        )

        // Access the RequestQueue through your singleton class.
        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest)
    }
    class MySingleton constructor(context: Context) {
        companion object {
            @Volatile
            private var INSTANCE: MySingleton? = null
            fun getInstance(context: Context) =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: MySingleton(context).also {
                        INSTANCE = it
                    }
                }
        }
        val imageLoader: ImageLoader by lazy {
            ImageLoader(requestQueue,
                object : ImageLoader.ImageCache {
                    private val cache = LruCache<String, Bitmap>(20)
                    override fun getBitmap(url: String): Bitmap? {
                        return cache.get(url)
                    }
                    override fun putBitmap(url: String, bitmap: Bitmap) {
                        cache.put(url, bitmap)
                    }
                })
        }
        val requestQueue: RequestQueue by lazy {
            // applicationContext is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            Volley.newRequestQueue(context.applicationContext)
        }
        fun <T> addToRequestQueue(req: Request<T>) {
            requestQueue.add(req)
        }

    }
    fun Shareclick(view: View) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type="text/plain"
        intent.putExtra(Intent.EXTRA_TEXT,"HEY, checkout this funny meme! $currentImgUrl")
        val chooser = Intent.createChooser(intent,"share this using...")
        startActivity(chooser)

//        val shareIntent = Intent(Intent.ACTION_SEND)
//        shareIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//        shareIntent.putExtra(Intent.EXTRA_STREAM, currentImgUrl)
//        shareIntent.type = "text/plain"
//        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//        shareIntent.putExtra(Intent.EXTRA_TEXT,"checkout this funny meme! $currentImgUrl")
////        shareIntent.putExtra(Intent.EXTRA_TEXT, currentImgUrl)
//        // Open the chooser dialog box
//        startActivity(Intent.createChooser(shareIntent, "Share with"))
    }
    fun Nextclick(view: View) {
        loadmeme()
    }
}