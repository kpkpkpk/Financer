package com.financer.feature.home.presentation

import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.math.min

internal fun HomeStore.Period.Companion.currentMonth(): HomeStore.Period {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val periodStart =
        LocalDateTime(year = now.year, month = now.month, day = 1, hour = 0, minute = 0)

    return HomeStore.Period(
        start = periodStart,
        end = now,
        preset = HomeStore.PeriodPreset.ThisMonth
    )
}

internal fun LocalDate.toHeaderLabel(): String {
    val day = dayOfMonth.toString().padStart(2, '0')
    val month = monthNumber.toString().padStart(2, '0')
    return "$day.$month.$year"
}

internal fun LocalDateTime.toTimeLabel(): String {
    val hours = hour.toString().padStart(2, '0')
    val minutes = minute.toString().padStart(2, '0')
    return "$hours:$minutes"
}
