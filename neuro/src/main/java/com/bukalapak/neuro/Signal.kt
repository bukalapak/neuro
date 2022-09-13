package com.bukalapak.neuro

import android.content.Context
import android.net.Uri

class Signal(
    val context: Context?,
    val uri: Uri,
    val url: String,
    val variables: OptWave,
    val queries: OptWaves,
    val fragment: String?,
    val args: Map<String, Any?>?
)