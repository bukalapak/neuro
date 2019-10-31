package com.bukalapak.neuro

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.natpryce.hamkrest.absent
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.or
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NeuroRoutingTest {

    @Before
    fun init() {
        Neuro.clearConnection()
        Neuro.preprocessor = null
    }

    @Test
    fun `literal routes are correct`() {
        connectMySite("/about") {}
        connectMySite("/transaction/view") {}
        connectMySite("/booking/view/as_pdf") {}

        assertRouteMySite(
            Neuro.findRoute("https://www.mysite.com/about"),
            "/about"
        )
        assertRouteMySite(
            Neuro.findRoute("https://www.mysite.com/transaction/view"),
            "/transaction/view"
        )
        assertRouteMySite(
            Neuro.findRoute("https://www.mysite.com/booking/view/as_pdf"),
            "/booking/view/as_pdf"
        )
    }

    @Test
    fun `anonymous routes are correct`() {
        connectMySite("/product/<>") {}
        connectMySite("/<>/detail") {}
        connectMySite("/product/<>/edit") {}

        assertRouteMySite(
            Neuro.findRoute("https://www.mysite.com/product/8w7yf7-this-is-a-product"),
            "/product/<>"
        )
        assertRouteMySite(
            Neuro.findRoute("https://www.mysite.com/8732427348927394/detail"),
            "/<>/detail"
        )
        assertRouteMySite(
            Neuro.findRoute("https://www.mysite.com/product/98ur89_nice_product/edit"),
            "/product/<>/edit"
        )
    }

    @Test
    fun `unpatterned routes are correct`() {
        connectMySite("/labels/<label_id>") {}
        connectMySite("/<booking_id>/view") {}
        connectMySite("/product/<id>/view") {}

        assertRouteMySite(
            Neuro.findRoute("https://www.mysite.com/labels/newset-hardware-ever-1234"),
            "/labels/<label_id>"
        )
        assertRouteMySite(
            Neuro.findRoute("https://www.mysite.com/aaa_bbb/view"),
            "/<booking_id>/view"
        )
        assertRouteMySite(
            Neuro.findRoute("https://www.mysite.com/product/this.is.dot/view"),
            "/product/<id>/view"
        )
    }

    @Test
    fun `case sensitive routes are correct`() {
        connectMySite("/tHiSiSAlaY") {}

        assertRouteMySite(
            Neuro.findRoute("https://www.mysite.com/tHiSiSAlaY"),
            "/tHiSiSAlaY"
        )
        assertRouteMySite(
            Neuro.findRoute("https://www.MYSITE.com/tHiSiSAlaY"),
            "/tHiSiSAlaY"
        )
        assertRouteMySite(
            Neuro.findRoute("hTTps://wWw.MYSiTE.cOm/tHiSiSAlaY"),
            "/tHiSiSAlaY"
        )
        assertRouteMySite(
            Neuro.findRoute("https://www.mysite.com/thisisalay"),
            Soma.EXPRESSION_OTHER_BRANCH // other
        )
    }

    @Test
    fun `slashed routes are correct`() {
        connectMySite("/detail/") {}
        connectMySite("/<>/blabla") {}

        assertRouteMySite(
            Neuro.findRoute("https://www.mysite.com/"),
            Soma.EXPRESSION_NO_BRANCH_WITH_SLASH
        )
        assertRouteMySite(
            Neuro.findRoute("https://www.mysite.com/detail/"),
            "/detail/"
        )
        assertRouteMySite(
            Neuro.findRoute("https://www.mysite.com/detail/blabla"),
            "/<>/blabla"
        )
        assertRouteMySite(
            Neuro.findRoute("https://www.mysite.com/detail"),
            Soma.EXPRESSION_OTHER_BRANCH
        )
        assertRouteMySite(
            Neuro.findRoute("https://www.mysite.com/detail/blabla/"),
            Soma.EXPRESSION_OTHER_BRANCH
        )
        assertRouteMySite(
            Neuro.findRoute("https://www.mysite.com"),
            Soma.EXPRESSION_NO_BRANCH
        )
    }

    @Test
    fun `Soma processor no branch is functional`() {
        val urlNoSlash = "http://sites.com"
        val urlWithSlash = "http://sites.com/"

        Neuro.connect(object : Soma("sites") {

            override val hosts = listOf(
                "sites.com"
            )

            override fun onProcessNoBranch(signal: Signal) {
                assertThat(signal.url, equalTo(urlNoSlash).or(equalTo(urlWithSlash)))
            }
        }, listOf())

        Neuro.proceed(urlNoSlash)
        Neuro.proceed(urlWithSlash)
    }

    @Test
    fun `Soma processor is functional`() {
        val urlNoBranch = "http://myprecioussite.com"
        val urlOtherBranch = "http://myprecioussite.com/other"
        val urlDenied = "http://myprecioussite.com/denyme"

        Neuro.connect(object : Soma("myprecioussite") {

            override val hosts = listOf(
                "www.myprecioussite.com",
                "myprecioussite.com"
            )

            override fun onSomaProcess(signal: Signal): Boolean {
                return signal.uri.path == "/denyme"
            }

            override fun onProcessNoBranch(signal: Signal) {
                super.onProcessNoBranch(signal)
                assertThat(signal.url, equalTo(urlNoBranch))
            }

            override fun onProcessOtherBranch(signal: Signal) {
                super.onProcessOtherBranch(signal)
                assertThat(signal.url, equalTo(urlOtherBranch))
            }
        }, listOf())

        Neuro.proceed(urlNoBranch)
        Neuro.proceed(urlOtherBranch)
        Neuro.proceed(urlDenied)
    }

    @Test
    fun `SomaOnly is functional`() {
        val url = "https://myothersite.com:8089/profile/"
        Neuro.connect(object : SomaOnly("myothersite") {

            override val ports = listOf(8089)

            override val hosts = listOf(
                "www.myothersite.com",
                "myothersite.com"
            )

            override fun onSomaProcess(signal: Signal) {
                assertThat(signal.url, equalTo(url))
            }
        })
        Neuro.proceed(url)
    }

    @Test
    fun `SomaFallback can catch fallback`() {
        val url = "https://www.google.com"
        Neuro.connect(object : SomaFallback() {
            override fun onSomaProcess(signal: Signal) {
                assertThat(signal.url, equalTo(url))
            }
        })
        Neuro.proceed(url)
    }

    @Test
    fun `no route found`() {
        val url = "http://www.noroute.com"
        val decision = Neuro.findRoute(url)
        assertThat(decision?.first, absent())
        Neuro.proceed(url, decision)
    }

    @Test
    fun `axon preprocessor usage`() {
        val url = "https://watashi.com/profile"
        val newUrl = "https://www.intercepted.com"
        val newUri = Uri.parse(newUrl)
        connectMySite("/profile") {
            assertThat(it.url, equalTo(newUrl))
        }
        Neuro.preprocessor = { processor, action, signal ->
            val newSignal = Signal(
                signal.context,
                newUri,
                newUrl,
                signal.variables,
                signal.queries,
                signal.fragment,
                signal.args
            )
            processor.invoke(action, newSignal)
        }
        Neuro.proceed(url)
    }

    @Test
    fun `axon processor usage`() {
        val url = "https://watashi.com/profile"
        val newUrl = "https://www.hacked.com"
        val newUri = Uri.parse(newUrl)
        val context = InstrumentationRegistry.getInstrumentation().context

        connectMySite("/profile") {
            assertThat(it.url, equalTo(newUrl))
        }
        val processor: AxonProcessor = { action, signal ->
            val newSignal = Signal(
                signal.context,
                newUri,
                newUrl,
                signal.variables,
                signal.queries,
                signal.fragment,
                signal.args
            )
            action.invoke(newSignal)
        }
        Neuro.proceed(url, context, processor)

        val decision = Neuro.findRoute(url)
        Neuro.proceed(url, decision, context, processor)
    }

    @Test
    fun `opaque form parsed succesfully`() {
        val url = "mailto:habibi@bukalapak.com?Subject=Test"
        Neuro.connect(object : SomaFallback() {
            override fun onSomaProcess(signal: Signal) {
                assertThat(signal.url, equalTo(url))
            }
        })
        Neuro.proceed(url)
    }

    companion object {
        internal fun assertRouteMySite(decision: RouteDecision?, expression: String) {
            assertThat(decision?.first?.nucleus?.id, equalTo(MySiteSoma.id))
            assertThat(decision?.second?.expression, equalTo(expression))
        }

        fun connectMySite(pattern: String, action: SignalAction = {}) {
            Neuro.connect(MySiteSoma, AxonBranch(pattern, action))
        }
    }
}