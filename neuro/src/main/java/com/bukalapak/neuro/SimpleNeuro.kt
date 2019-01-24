package com.bukalapak.neuro

import android.content.Context
import android.net.Uri

object SimpleNeuro {

    private const val ID = "simple"

    private var soma: Soma? = null

    fun setBase(uri: Uri) {
        soma = object : Soma(ID) {
            override val schemes = listOf(uri.scheme)
            override val hosts = listOf(uri.host)
            override val ports = if (uri.port == -1) emptyList() else listOf(uri.port)
        }
    }

    fun addPath(expression: String, action: SignalAction) {
        val soma = soma ?: throw IllegalStateException("You must call SimpleNeuro.setBase(Uri) first.")
        Neuro.connect(soma, AxonBranch(expression, action))
    }

    fun proceed(url: String, context: Context? = null) {
        Neuro.proceed(url, context)
    }

}