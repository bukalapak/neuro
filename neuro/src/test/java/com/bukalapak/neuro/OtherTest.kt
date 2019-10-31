package com.bukalapak.neuro

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.IllegalArgumentException

@RunWith(AndroidJUnit4::class)
class OtherTest {

    @Before
    fun init() {
        Neuro.clearConnection()
    }

    @Test
    fun `simple neuro is fine`() {
        SimpleNeuro.setBase(Uri.parse("https://domain.com"))
        SimpleNeuro.addPath("/faq") {
            assertThat(it.queries.getString("key"), equalTo("value"))
        }
        SimpleNeuro.proceed("https://domain.com/faq?key=value")
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
        Neuro.connect(MySiteSoma, listOf())
        assertThat(Neuro.neurons.size, equalTo(1))
        Neuro.clearConnection()
        assertThat(Neuro.neurons.size, equalTo(0))
    }

    @Test
    fun `is debug`() {
        assertThat(BuildConfig.DEBUG, equalTo(true))
    }
}