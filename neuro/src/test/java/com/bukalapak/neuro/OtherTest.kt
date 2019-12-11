package com.bukalapak.neuro

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OtherTest {

    @Test
    fun `simple neuro is fine`() {
        val router = SimpleNeuro()
        router.setBase(Uri.parse("https://domain.com"))
        router.addPath("/faq") {
            assertThat(it.queries.getString("key"), equalTo("value"))
        }
        router.proceed("https://domain.com/faq?key=value")
    }

    @Test
    fun `nucleus equality comparation`() {
        val nucleusId = "test"
        val soma1 = object : SomaOnly(nucleusId) {
            override val hosts = listOf("abc.com")
            override fun onSomaProcess(signal: Signal) = Unit
        }
        val soma2 = object : SomaOnly(nucleusId) {
            override val hosts = listOf("cde.com")
            override fun onSomaProcess(signal: Signal) = Unit
        }
        val set = setOf(soma1, soma2)
        assertThat(set.size, equalTo(1))
        assertThat(set.first().id, equalTo(nucleusId))
    }

    @Test
    fun `neuro connection clearance`() {
        val router = Neuro()
        router.connect(MySiteSoma, listOf())
        assertThat(router.neurons.size, equalTo(1))
        router.clearConnection()
        assertThat(router.neurons.size, equalTo(0))
    }
}