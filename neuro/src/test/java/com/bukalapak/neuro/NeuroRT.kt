package com.bukalapak.neuro

import android.net.Uri
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NeuroRT {

    @Test
    fun all_test() {
        val url = "https://www.bukalapak.com/bbbb/bbbb/aaa?key=value#section1"

        val soma1 = object : Soma("dfg") {
            override val hosts = listOf("<subdomain>.bukalapak.com")

            override fun onSomaProcess(signal: Signal): Boolean {
                printout("subdomain", signal.variables.optString("subdomain"))
                return false
            }
        }
        val soma2 = object : SomaOnly("asd") {
            override val hosts = listOf("<subdomain>.hehehe.com")
            override val ports = listOf(8089, 7823)
        }
        val soma3 = object : Soma("sdf") {
            override val hosts = listOf("<subdomain>.staging.com")
        }
        Neuro.connect(soma2)
        Neuro.connect(soma1, AxonBranch("/bbbb/*") {

        })
        Neuro.connect(soma1, AxonBranch("/bbbb/bbbb/4") {

        })
        Neuro.connect(soma1, AxonBranch("/bbbb/2222222") {

        })
        Neuro.connect(soma1, AxonBranch("/cccc/cccc/111111111111/123") {

        })
        Neuro.connect(soma1, AxonBranch("/cccc/cccc/34") {

        })
        Neuro.connect(soma1, AxonBranch("/*/cc/ccccc") {

        })
        Neuro.connect(soma3, AxonBranch("/dddd/dddd/333") {

        })

        Neuro.neurons[soma1]?.forEach {
            printout("ooo", it.key)
        }

        Neuro.connect(soma1, AxonBranch("/bbbb/bbbb/<product_id:[a-z_]+>") {
            printout("product_id", it.variables.optString("product_id"))
            printout("key", it.queries.optString("key"))
        })

        Neuro.proceed(url)
    }

    @Test
    fun uri_parts_test() {
        val url = "https://www.bukalapak.com/hahaha?key=value"
        val uri = Uri.parse(url)
        printout("scheme", uri.scheme)
        printout("host", uri.host)
        printout("port", uri.port)
        printout("path", uri.path)

        // anatomy:
        // <(scheme)://><(host)><:(port)><(/path)>
    }

    @Test
    fun utils_test() {
        val pattern = "<subdomain:\\w+>.bukalapak.com"
        val regex = pattern.toPattern()
        printout("regex", regex)
    }

    fun printout(label: String, content: Any?) {
        System.out.println("*** ${label.toUpperCase()} : $content")
    }

}