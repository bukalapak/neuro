package com.bukalapak.neuro

import com.bukalapak.neuro.Soma

object MySiteSoma : Soma("mysite") {

    override val hosts = listOf(
        "www.mysite.com",
        "mysite.com"
    )

}