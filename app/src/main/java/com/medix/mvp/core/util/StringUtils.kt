package com.medix.mvp.core.util

import java.text.Normalizer

fun String.normalizedSpanish(): String {
    val withoutTildes = Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace("\\p{M}+".toRegex(), "")
    return withoutTildes.lowercase().trim()
}
