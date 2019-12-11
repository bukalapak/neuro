package com.bukalapak.neuro

import android.net.Uri
import android.os.Bundle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.natpryce.hamkrest.absent
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ResultParsingTest {

    private val router = Neuro()

    @Before
    fun init() {
        router.clearConnection()
    }

    @Test
    fun `url and uri passing is correct`() {
        connectMySite(router, "/faq") {
            val url = "https://www.mysite.com/faq"
            assertThat(it.url, equalTo(url))
            assertThat(it.uri, equalTo(Uri.parse(url)))
        }
        router.proceed("https://www.mysite.com/faq")
    }

    @Test
    fun `context and args passing is correct`() {
        connectMySite(router, "/yes") {
            assertThat(it.context, absent())
            assertThat(it.args?.getBoolean("passed"), equalTo(true))
        }
        router.proceed("https://www.mysite.com/yes", null, null, Bundle().apply {
            putBoolean("passed", true)
        })
    }

    @Test
    fun `variable parsing is correct`() {
        connectMySite(router, "/product/<product_id>/<>/edit/<slug:[0-9]+>/<label:.+>-<>-<>") {
            assertThat(it.variables.get("product_id"), equalTo(listOf("!@#Asd123")))

            assertThat(it.variables.getString("product_id"), equalTo("!@#Asd123"))
            assertThat(it.variables.optString("product_id"), equalTo("!@#Asd123"))
            assertThat(it.variables.getInt("slug"), equalTo(12345))
            assertThat(it.variables.optInt("slug"), equalTo(12345))
            assertThat(it.variables.getString("label"), equalTo("Fgh\$%^789"))
            assertThat(it.variables.optString("label"), equalTo("Fgh\$%^789"))

            assertThat(it.variables.getString("getString"), absent())
            assertThat(it.variables.optString("optString"), equalTo(""))
            assertThat(it.variables.getBoolean("getBoolean"), absent())
            assertThat(it.variables.optBoolean("optBoolean"), equalTo(false))
            assertThat(it.variables.getDouble("getDouble"), absent())
            assertThat(it.variables.optDouble("optDouble"), equalTo(0.0))
            assertThat(it.variables.getFloat("getFloat"), absent())
            assertThat(it.variables.optFloat("optFloat"), equalTo(0f))
            assertThat(it.variables.getLong("getLong"), absent())
            assertThat(it.variables.optLong("optLong"), equalTo(0L))
        }
        router.proceed("https://www.mysite.com/product/!@#Asd123/#$%Dfg234/edit/12345/Fgh$%^789-Blablabla-123456")
    }

    @Test
    fun `query parsing is correct`() {
        connectMySite(router, "/tnc") {
            assertThat(it.queries.get("key"), equalTo(listOf("value")))

            assertThat(it.queries.getString("key"), equalTo("value"))
            assertThat(it.queries.optString("key"), equalTo("value"))
            assertThat(it.queries.getInt("123"), equalTo(234))
            assertThat(it.queries.optInt("123"), equalTo(234))
            assertThat(it.queries.getBoolean("boolean1"), equalTo(true))
            assertThat(it.queries.optBoolean("boolean1"), equalTo(true))
            assertThat(it.queries.getBoolean("boolean2"), equalTo(false))
            assertThat(it.queries.optBoolean("boolean2"), equalTo(false))
            assertThat(it.queries.getFloat("float"), equalTo(0.5f))
            assertThat(it.queries.optFloat("float"), equalTo(0.5f))
            assertThat(it.queries.getDouble("double"), equalTo(1.9))
            assertThat(it.queries.optDouble("double"), equalTo(1.9))
            assertThat(it.queries.getLong("long"), equalTo(9000L))
            assertThat(it.queries.optLong("long"), equalTo(9000L))
            assertThat(it.queries.getString("url"), equalTo("https://github.com/mrhabibi"))
            assertThat(it.queries.getString("search[key]"), equalTo("celana hitam"))

            assertThat(it.queries.getString("getString"), absent())
            assertThat(it.queries.optString("optString"), equalTo(""))
            assertThat(it.queries.getBoolean("getBoolean"), absent())
            assertThat(it.queries.optBoolean("optBoolean"), equalTo(false))
            assertThat(it.queries.getDouble("getDouble"), absent())
            assertThat(it.queries.optDouble("optDouble"), equalTo(0.0))
            assertThat(it.queries.getFloat("getFloat"), absent())
            assertThat(it.queries.optFloat("optFloat"), equalTo(0f))
            assertThat(it.queries.getLong("getLong"), absent())
            assertThat(it.queries.optLong("optLong"), equalTo(0L))
        }
        val url = "https://www.mysite.com/tnc" +
                "?key=value&123=234&boolean1=1&boolean2=false&float=0.5&double=1.9&long=9000" +
                "&url=https%3A%2F%2Fgithub.com%2Fmrhabibi&search%5Bkey%5D=celana%20hitam"
        router.proceed(url)
    }

    @Test
    fun `multi queries parsing is correct`() {
        connectMySite(router, "/summary") {
            assertThat(it.queries.getStringList("key"), equalTo(listOf("value1", "value2")))
            assertThat(it.queries.optStringList("key"), equalTo(listOf("value1", "value2")))
            assertThat(it.queries.getIntList("123"), equalTo(listOf(2341, 2342)))
            assertThat(it.queries.optIntList("123"), equalTo(listOf(2341, 2342)))
            assertThat(it.queries.getBooleanList("boolean"), equalTo(listOf(true, false)))
            assertThat(it.queries.optBooleanList("boolean"), equalTo(listOf(true, false)))
            assertThat(it.queries.getFloatList("float"), equalTo(listOf(0.5f, 0.8f)))
            assertThat(it.queries.optFloatList("float"), equalTo(listOf(0.5f, 0.8f)))
            assertThat(it.queries.getDoubleList("double"), equalTo(listOf(1.9, 1.1)))
            assertThat(it.queries.optDoubleList("double"), equalTo(listOf(1.9, 1.1)))
            assertThat(it.queries.getLongList("long"), equalTo(listOf(9000L, 9001L)))
            assertThat(it.queries.optLongList("long"), equalTo(listOf(9000L, 9001L)))
            assertThat(
                it.queries.getStringList("search[key]"),
                equalTo(listOf("https://github.com/mrhabibi", "celana hitam"))
            )

            assertThat(it.queries.getStringList("getString"), absent())
            assertThat(it.queries.optStringList("optString"), equalTo(emptyList()))
            assertThat(it.queries.getBooleanList("getBoolean"), absent())
            assertThat(it.queries.optBooleanList("optBoolean"), equalTo(emptyList()))
            assertThat(it.queries.getDoubleList("getDouble"), absent())
            assertThat(it.queries.optDoubleList("optDouble"), equalTo(emptyList()))
            assertThat(it.queries.getFloatList("getFloat"), absent())
            assertThat(it.queries.optFloatList("optFloat"), equalTo(emptyList()))
            assertThat(it.queries.getLongList("getLong"), absent())
            assertThat(it.queries.optLongList("optLong"), equalTo(emptyList()))
        }
        val url = "https://www.mysite.com/summary" +
                "?key=value1&key=value2&123=2341&123=2342&boolean=1&boolean=false&float=0.5" +
                "&float=0.8&double=1.9&double=1.1&long=9000&long=9001" +
                "&search%5Bkey%5D=https%3A%2F%2Fgithub.com%2Fmrhabibi&search%5Bkey%5D=celana%20hitam"
        router.proceed(url)
    }

    @Test
    fun `fragment parsing is correct`() {
        connectMySite(router, "/bantuan") {
            assertThat(it.fragment, equalTo("here"))
        }
        router.proceed("https://www.mysite.com/bantuan#here")
        router.proceed("https://www.mysite.com/bantuan?key=value#here")
    }

    @Test
    fun `incorrect query parsing handling is correct`() {
        connectMySite(router, "/terms") {
            assertThat(it.queries.getLong("str"), equalTo(0L))
            assertThat(it.queries.getInt("str"), equalTo(0))
            assertThat(it.queries.getFloat("str"), equalTo(0.0F))
            assertThat(it.queries.getDouble("str"), equalTo(0.0))
            assertThat(it.queries.optLong("str"), equalTo(0L))
            assertThat(it.queries.optInt("str"), equalTo(0))
            assertThat(it.queries.optFloat("str"), equalTo(0.0F))
            assertThat(it.queries.optDouble("str"), equalTo(0.0))

            assertThat(it.queries.getLongList("strlist"), equalTo(emptyList()))
            assertThat(it.queries.getIntList("strlist"), equalTo(emptyList()))
            assertThat(it.queries.getFloatList("strlist"), equalTo(emptyList()))
            assertThat(it.queries.getDoubleList("strlist"), equalTo(emptyList()))
            assertThat(it.queries.optLongList("strlist"), equalTo(emptyList()))
            assertThat(it.queries.optIntList("strlist"), equalTo(emptyList()))
            assertThat(it.queries.optFloatList("strlist"), equalTo(emptyList()))
            assertThat(it.queries.optDoubleList("strlist"), equalTo(emptyList()))
        }
        router.proceed("https://www.mysite.com/terms?str=value&strlist=value1&strlist=value2")
    }

    @Test
    fun `put data to wave`() {
        val wave = Wave().apply {
            put("key", "value")
        }
        assertThat(wave.getString("key"), equalTo("value"))
    }

    companion object {
        fun connectMySite(router: Neuro, pattern: String, action: SignalAction = {}) {
            router.connect(MySiteSoma, AxonBranch(pattern, action))
        }
    }
}