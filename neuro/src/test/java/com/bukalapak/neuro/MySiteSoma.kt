package com.bukalapak.neuro

object MySiteSoma : Soma("mysite") {

    override val hosts = listOf(
        "www.mysite.com",
        "mysite.com"
    )
}