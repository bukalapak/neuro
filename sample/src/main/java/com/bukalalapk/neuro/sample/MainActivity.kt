package com.bukalalapk.neuro.sample

import android.content.Context
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.bukalapak.neuro.SimpleNeuro
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        registerRoute()

        btnProceed.setOnClickListener {
            SimpleNeuro.proceed(etUrl.text.toString(), this)
        }
    }

    fun registerRoute() {
        SimpleNeuro.setBase(Uri.parse("https://www.mywebsite.com"))

        // https://www.mywebsite.com/login
        SimpleNeuro.addPath("/login") {
            toast(it.context, "Login")
        }

        // https://www.mywebsite.com/messages/1234
        SimpleNeuro.addPath("/messages/<message_id>") {
            val messageId = it.variables.optString("message_id")
            toast(it.context, "Message with $messageId")
        }

        // https://www.mywebsite.com/promo?source=banner
        SimpleNeuro.addPath("/promo") {
            val source = it.queries.optString("source")
            toast(it.context, "Promo with $source")
        }
    }

    fun toast(context: Context?, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
