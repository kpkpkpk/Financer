package com.financer.feature.home.presentation

import com.financer.core.data.model.Category
import com.financer.core.data.model.Transaction
import com.financer.core.data.model.TransactionType
import financer.feature.home_impl.generated.resources.Res
import financer.feature.home_impl.generated.resources.home_date_today
import financer.feature.home_impl.generated.resources.home_date_yesterday
import financer.feature.home_impl.generated.resources.home_month_genitive_1
import financer.feature.home_impl.generated.resources.home_month_genitive_10
import financer.feature.home_impl.generated.resources.home_month_genitive_11
import financer.feature.home_impl.generated.resources.home_month_genitive_12
import financer.feature.home_impl.generated.resources.home_month_genitive_2
import financer.feature.home_impl.generated.resources.home_month_genitive_3
import financer.feature.home_impl.generated.resources.home_month_genitive_4
import financer.feature.home_impl.generated.resources.home_month_genitive_5
import financer.feature.home_impl.generated.resources.home_month_genitive_6
import financer.feature.home_impl.generated.resources.home_month_genitive_7
import financer.feature.home_impl.generated.resources.home_month_genitive_8
import financer.feature.home_impl.generated.resources.home_month_genitive_9
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.StringResource

internal class HomeUiStateMapper {

    fun mapHeader(state: HomeStore.State): HomeHeaderUiState {
        return HomeHeaderUiState(
            formattedBalance = formatAmount(state.balance),
            formattedIncome = formatAmount(state.income),
            formattedExpense = formatAmount(state.expense),
            periodPreset = state.period?.preset ?: HomeStore.PeriodPreset.Custom,
            periodCustomTitle = state.period?.customTitle.orEmpty(),
        )
    }

    fun mapList(state: HomeStore.State): HomeListUiState {
        val categoriesById = state.categories
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        return HomeListUiState(
            items = state.transactions.toListItems(categoriesById, today),
        )
    }

    private fun List<Transaction>.toListItems(
        categoriesById: Map<Long, Category>,
        today: LocalDate,
    ): List<HomeListItem> {
        if (isEmpty()) return listOf(HomeListItem.EmptyState)

        return sortedByDescending { it.date }
            .groupBy { it.date.date }
            .entries
            .sortedByDescending { it.key }
            .flatMap { (date, transactions) ->
                buildList {
                    val totalByDate = transactions.sumOf {
                        if(it.type == TransactionType.INCOME) {
                            it.amount
                        }else{
                            -it.amount
                        }
                    }
                    add(
                        HomeListItem.DateHeader(
                            key = "$date",
                            title = formatDateHeader(date, today),
                            totalSum = TextValue.Raw(
                                text = if (totalByDate > 0) {
                                    formatSignedAmount(totalByDate)
                                } else {
                                    formatAmount(totalByDate)
                                }
                            )
                        )
                    )
                    transactions.forEach { transaction ->
                        val category = categoriesById[transaction.categoryId]
                        val isIncome = transaction.type == TransactionType.INCOME
                        add(
                            HomeListItem.Transaction(
                                key = "${transaction.id}",
                                item = HomeTransactionItem(
                                    id = transaction.id,
                                    categoryEmoji = category?.emoji ?: "",
                                    categoryName = category?.name,
                                    time = transaction.date.formatTime(),
                                    formattedAmount = if (isIncome) {
                                        formatSignedAmount(transaction.amount)
                                    } else {
                                        formatAmount(-transaction.amount)
                                    },
                                    isIncome = isIncome,
                                ),
                            )
                        )
                    }
                }
            } + listOf(HomeListItem.Space(heightDp = 20))
    }

    private fun formatDateHeader(date: LocalDate, today: LocalDate): TextValue {
        val yesterday = LocalDate.fromEpochDays(today.toEpochDays() - 1)

        return when (date) {
            today -> TextValue.Resource(Res.string.home_date_today)
            yesterday -> TextValue.Resource(Res.string.home_date_yesterday)
            else -> buildTextValue {
                addRaw("${date.dayOfMonth} ")
                addResource(monthGenitiveRes(date.monthNumber))
                if (date.year != today.year) {
                    addRaw(" ${date.year}")
                }
            }
        }
    }

    private fun LocalDateTime.formatTime(): String {
        val h = hour.toString().padStart(2, '0')
        val m = minute.toString().padStart(2, '0')
        return "$h:$m"
    }
}

private fun monthGenitiveRes(month: Int): StringResource = when (month) {
    1 -> Res.string.home_month_genitive_1
    2 -> Res.string.home_month_genitive_2
    3 -> Res.string.home_month_genitive_3
    4 -> Res.string.home_month_genitive_4
    5 -> Res.string.home_month_genitive_5
    6 -> Res.string.home_month_genitive_6
    7 -> Res.string.home_month_genitive_7
    8 -> Res.string.home_month_genitive_8
    9 -> Res.string.home_month_genitive_9
    10 -> Res.string.home_month_genitive_10
    11 -> Res.string.home_month_genitive_11
    12 -> Res.string.home_month_genitive_12
    else -> error("Invalid month: $month")
}
