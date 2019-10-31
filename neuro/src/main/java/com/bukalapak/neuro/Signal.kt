package com.bukalapak.neuro

import android.content.Context
import android.net.Uri
import android.os.Bundle

open class Signal(
    val context: Context?,
    val uri: Uri,
    val url: String,
    val variables: OptWave,
    val queries: OptWaves,
    val fragment: String?,
    val args: Bundle?
)