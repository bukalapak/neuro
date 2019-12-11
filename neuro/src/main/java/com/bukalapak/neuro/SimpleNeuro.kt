package com.bukalapak.neuro

import android.content.Context
import android.net.Uri

class SimpleNeuro {

    private val neuro = Neuro()
    private var soma: Soma? = null

    fun setBase(uri: Uri) {
        soma = object : Soma(ID) {
            override val schemes = uri.scheme?.let { listOf(it) } ?: emptyList()
            override val hosts = uri.host?.let { listOf(it) } ?: emptyList()
            override val ports = if (uri.port == -1) emptyList() else listOf(uri.port)
        }
    }

    fun addPath(expression: String, action: SignalAction) {
        val soma =
            soma ?: throw IllegalStateException("You must call SimpleNeuro.setBase(Uri) first.")
        neuro.connect(soma, AxonBranch(expression, action))
    }

    fun proceed(url: String, context: Context? = null) {
        neuro.proceed(url, context)
    }

    fun clearPaths() {
        neuro.clearConnection()
    }

    companion object {
        private const val ID = "simple"
    }
}