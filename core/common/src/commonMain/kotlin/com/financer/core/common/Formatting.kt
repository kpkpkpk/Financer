package com.financer.core.common

import kotlin.math.abs

private val MONTH_NAMES_GENITIVE = arrayOf(
    "января", "февраля", "марта", "апреля", "мая", "июня",
    "июля", "августа", "сентября", "октября", "ноября", "декабря"
)

/**
 * Formats amount in kopecks as "12 344,50 ₽".
 * Example: 1234450L → "12 344,50 ₽"
 * Negative amounts: -267800L → "-2 678,00 ₽"
 */
fun formatAmount(kopecks: Long): String {
    val absKopecks = abs(kopecks)
    val rubles = absKopecks / 100
    val frac = absKopecks % 100

    val rublesStr = formatWithSpaces(rubles)
    val fracStr = frac.toString().padStart(2, '0')

    val sign = if (kopecks < 0) "-" else ""
    return "$sign$rublesStr,$fracStr ₽"
}

/**
 * Formats amount in kopecks with sign prefix: "+40 300,00 ₽" or "-2 678,00 ₽".
 */
fun formatAmountSigned(kopecks: Long): String {
    val prefix = if (kopecks > 0) "+" else ""
    return "$prefix${formatAmount(kopecks)}"
}

private fun formatWithSpaces(value: Long): String {
    val str = value.toString()
    val result = StringBuilder()
    var count = 0
    for (i in str.lastIndex downTo 0) {
        if (count > 0 && count % 3 == 0) {
            result.append(' ')
        }
        result.append(str[i])
        count++
    }
    return result.reverse().toString()
}

/**
 * Formats ISO date string "2024-06-03" as "3 июня".
 * Expects input format: "yyyy-MM-dd" or "yyyy-MM-ddTHH:mm:ss".
 */
fun formatDate(isoDate: String): String {
    val datePart = isoDate.substringBefore('T')
    val parts = datePart.split('-')
    if (parts.size < 3) return isoDate

    val month = parts[1].toIntOrNull() ?: return isoDate
    val day = parts[2].toIntOrNull() ?: return isoDate

    if (month !in 1..12) return isoDate
    return "$day ${MONTH_NAMES_GENITIVE[month - 1]}"
}

/**
 * Formats ISO date string as "3 июня 2024".
 */
fun formatDateWithYear(isoDate: String): String {
    val datePart = isoDate.substringBefore('T')
    val parts = datePart.split('-')
    if (parts.size < 3) return isoDate

    val year = parts[0].toIntOrNull() ?: return isoDate
    val month = parts[1].toIntOrNull() ?: return isoDate
    val day = parts[2].toIntOrNull() ?: return isoDate

    if (month !in 1..12) return isoDate
    return "$day ${MONTH_NAMES_GENITIVE[month - 1]} $year"
}
