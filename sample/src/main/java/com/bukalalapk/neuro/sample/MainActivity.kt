package com.bukalalapk.neuro.sample

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bukalapak.neuro.SimpleNeuro
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val router = SimpleNeuro()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        registerRoute()

        btnProceed.setOnClickListener {
            router.proceed(etUrl.text.toString(), this)
        }
    }

    fun registerRoute() {
        router.setBase(Uri.parse("https://www.mywebsite.com"))

        // https://www.mywebsite.com/login
        router.addPath("/login") {
            toast(it.context, "Login")
        }

        // https://www.mywebsite.com/messages/1234
        router.addPath("/messages/<message_id>") {
            val messageId = it.variables.optString("message_id")
            toast(it.context, "Message with $messageId")
        }

        // https://www.mywebsite.com/promo?source=banner
        router.addPath("/promo") {
            val source = it.queries.optString("source")
            toast(it.context, "Promo with $source")
        }
    }

    fun toast(context: Context?, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
