package com.financer.feature.home.presentation

import kotlin.math.abs

private const val KOPECKS_IN_RUBLE = 100L
private const val ONE_MILLION_RUBLES_IN_KOPECKS = 100_000_000L
private const val RUBLE_SYMBOL = "\u20BD"

internal fun formatAmount(kopecks: Long): String {
    val absKopecks = abs(kopecks)
    val rubles = absKopecks / KOPECKS_IN_RUBLE
    val kopeckPart = absKopecks % KOPECKS_IN_RUBLE
    val sign = if (kopecks < 0) "-" else ""

    val rublesWithSpaces = formatRublesWithSpaces(rubles)
    val amountText = if (kopeckPart == 0L || absKopecks > ONE_MILLION_RUBLES_IN_KOPECKS) {
        rublesWithSpaces
    } else {
        "$rublesWithSpaces,${kopeckPart.toString().padStart(2, '0')}"
    }

    return "$sign$amountText $RUBLE_SYMBOL"
}

internal fun formatSignedAmount(kopecks: Long): String {
    val prefix = if (kopecks > 0) "+" else ""
    return "$prefix${formatAmount(kopecks)}"
}

private fun formatRublesWithSpaces(value: Long): String {
    val raw = value.toString()
    return buildString {
        raw.forEachIndexed { index, char ->
            append(char)
            val posFromEnd = raw.length - index - 1
            if (posFromEnd > 0 && posFromEnd % 3 == 0) append(' ')
        }
    }
}
