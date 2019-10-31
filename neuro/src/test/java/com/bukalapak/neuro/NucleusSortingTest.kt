package com.bukalapak.neuro

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NucleusSortingTest {

    @Test
    fun `nucleus sorting is correct`() {

        val soma1 = object : SomaOnly("soma1") {
            override val schemes = listOf("http", "https")
            override val hosts = listOf("abc.com, def.com")
            override val ports = listOf(8080)
            override fun onSomaProcess(signal: Signal) = Unit
        }

        val soma2 = object : SomaOnly("soma2") {
            override val schemes = listOf("http", "https")
            override val hosts = listOf("abc.com, def.com")
            override val ports = listOf(8080)
            override fun onSomaProcess(signal: Signal) = Unit
        }

        val soma3 = object : SomaOnly("soma3") {
            override val schemes = listOf("http", "https")
            override val hosts = listOf("abc.com, def.com")
            override val ports = listOf(8080, 8000)
            override fun onSomaProcess(signal: Signal) = Unit
        }

        val soma4 = object : SomaOnly("soma4") {
            override val schemes = listOf("http", "https")
            override val hosts = listOf("abc.com, def.com")
            override val ports = listOf(8080)
            override fun onSomaProcess(signal: Signal) = Unit

            override val priority = 99
        }

        assert(soma1 < soma2)
        assert(soma1 > soma3)
        assert(soma1 > soma4)
        assert(soma2 > soma3)
        assert(soma2 > soma4)
        assert(soma3 > soma4)
    }
}