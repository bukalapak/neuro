package com.bukalapak.neuro

import android.util.Log

interface Logger {

    fun onRoutingUrl(url: String) {
        Log.i(Neuro.TAG, "Routing url $url")
    }

    fun onUrlHasResult(url: String, nucleus: Nucleus, branch: AxonBranch?) {
        Log.i(Neuro.TAG, "Routing via $nucleus and $branch")
    }

    fun onUrlHasNoResult(url: String) {
        Log.e(Neuro.TAG, "Url $url has no result")
    }

    fun onNucleusReturnedFalse(url: String) {
        Log.i(Neuro.TAG, "Nucleus transporter returned false")
    }

    fun onFindRouteStarted(url: String) = Unit

    fun onFindRouteFinished(url: String) = Unit

    companion object {
        val DEFAULT: Logger = object : Logger {}
    }
}