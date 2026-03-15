package com.financer.feature.transaction.presentation

internal fun isValidAmountInput(value: String): Boolean {
    if (value.isBlank()) return true

    var separatorIndex = -1
    value.forEachIndexed { index, char ->
        when {
            char.isDigit() -> Unit
            (char == ',' || char == '.') && separatorIndex == -1 -> separatorIndex = index
            else -> return false
        }
    }

    return separatorIndex == -1 || value.length - separatorIndex - 1 <= 2
}

internal fun amountInputToKopecks(value: String): Long {
    if (value.isBlank()) return 0L

    val parts = value.replace('.', ',').split(',', limit = 2)
    val rubles = parts.firstOrNull().orEmpty()
        .ifBlank { "0" }
        .toLongOrNull() ?: return 0L
    val fraction = parts.getOrNull(1).orEmpty()
        .padEnd(2, '0')
        .take(2)
        .ifBlank { "00" }
        .toLongOrNull() ?: return 0L

    return rubles * 100 + fraction
}

internal fun formatAmountInput(kopecks: Long): String {
    val rubles = kopecks / 100
    val fraction = (kopecks % 100).toString().padStart(2, '0')
    return "$rubles,$fraction"
}
