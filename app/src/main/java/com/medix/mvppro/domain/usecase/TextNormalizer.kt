package com.medix.mvppro.domain.usecase

import java.text.Normalizer

fun normalizeText(input: String): String {
    val lowered = input.trim().lowercase()
    return Normalizer.normalize(lowered, Normalizer.Form.NFD)
        .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
}
