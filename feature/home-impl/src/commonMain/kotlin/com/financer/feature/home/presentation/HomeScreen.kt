package com.financer.feature.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import financer.feature.home_impl.generated.resources.Res
import financer.feature.home_impl.generated.resources.home_balance
import financer.feature.home_impl.generated.resources.home_empty_state
import financer.feature.home_impl.generated.resources.home_expense
import financer.feature.home_impl.generated.resources.home_income
import financer.feature.home_impl.generated.resources.home_period_this_month
import financer.feature.home_impl.generated.resources.home_unknown_category
import org.jetbrains.compose.resources.stringResource

private val ExpandedHeaderHeight: Dp = 222.dp
private val CollapsedHeaderHeight: Dp = 56.dp
private const val HeaderCollapseSpeedFactor = 1.45f

@Composable
internal fun HomeScreen(component: DefaultHomeComponent, modifier: Modifier = Modifier) {
    val state by component.state.collectAsState()
    val periodTitle = when (state.period.preset) {
        HomeStore.PeriodPreset.ThisMonth -> stringResource(Res.string.home_period_this_month)
        HomeStore.PeriodPreset.Custom -> state.period.customTitle
    }

    val listState = rememberLazyListState()
    val density = LocalDensity.current
    val expandedHeaderHeightPx = with(density) { ExpandedHeaderHeight.toPx() }
    val collapseRangePx = with(density) { (ExpandedHeaderHeight - CollapsedHeaderHeight).toPx() }
    val collapseDistancePx = collapseRangePx * HeaderCollapseSpeedFactor
    val collapseScrollPx by remember(listState, collapseDistancePx) {
        derivedStateOf {
            when {
                collapseDistancePx <= 0f -> 0f
                listState.firstVisibleItemIndex > 0 -> collapseDistancePx
                else -> listState.firstVisibleItemScrollOffset.toFloat().coerceIn(0f, collapseDistancePx)
            }
        }
    }
    val collapseProgress by remember(collapseScrollPx, collapseDistancePx) {
        derivedStateOf {
            if (collapseDistancePx <= 0f) 1f else collapseScrollPx / collapseDistancePx
        }
    }
    val currentHeaderHeight = ExpandedHeaderHeight -
            ((ExpandedHeaderHeight - CollapsedHeaderHeight) * collapseProgress)
    val headerTranslationY = -(expandedHeaderHeightPx * collapseProgress)

    LaunchedEffect(Unit) {
        component.onLoadData()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(top = currentHeaderHeight + 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                items = state.items,
                key = { item -> item.key() },
            ) { item ->
                when (item) {
                    is HomeStore.ListItem.DateHeader -> {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        )
                    }

                    is HomeStore.ListItem.Transaction -> {
                        TransactionRow(
                            transaction = item.item,
                            onClick = { component.onTransactionClicked(item.item.id) },
                        )
                    }

                    HomeStore.ListItem.EmptyState -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(Res.string.home_empty_state),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(currentHeaderHeight)
                .clipToBounds()
                .align(Alignment.TopCenter)
        ) {
            CollapsedBalanceAppBar(
                balance = state.balance,
                onFilterClick = component::onFilterClicked,
                modifier = Modifier.align(Alignment.TopStart),
            )

            HomeHeader(
                balance = state.balance,
                income = state.income,
                expense = state.expense,
                periodTitle = periodTitle,
                onFilterClick = component::onFilterClicked,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .graphicsLayer {
                        translationY = headerTranslationY
                    },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CollapsedBalanceAppBar(
    balance: Long,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = "${stringResource(Res.string.home_balance)}: ${formatAmount(balance)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        },
        navigationIcon = {
            Box(modifier = Modifier.padding(start = 12.dp)) {
                HeaderSquareAction(onClick = onFilterClick)
            }
        },
        actions = {
            Box(modifier = Modifier.padding(end = 12.dp)) {
                HeaderSquareAction(onClick = {})
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
        ),
        windowInsets = WindowInsets(0, 0, 0, 0),
    )
}

private fun HomeStore.ListItem.key(): String =
    when (this) {
        is HomeStore.ListItem.DateHeader -> "header-$date"
        is HomeStore.ListItem.Transaction -> "transaction-${item.id}"
        HomeStore.ListItem.EmptyState -> "empty-state"
    }

@Composable
private fun HomeHeader(
    balance: Long,
    income: Long,
    expense: Long,
    periodTitle: String,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HeaderSquareAction(onClick = onFilterClick)
            HeaderSquareAction(onClick = {})
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.home_balance),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(modifier = Modifier.width(8.dp))

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.45f))
                    .clickable(onClick = onFilterClick)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = periodTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )

                Spacer(modifier = Modifier.width(6.dp))

                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
        }

        Text(
            text = formatAmount(balance),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        SummaryCard(
            income = income,
            expense = expense,
            modifier = Modifier.padding(horizontal = 2.dp),
        )

        Spacer(modifier = Modifier.height(2.dp))
    }
}

@Composable
private fun HeaderSquareAction(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(24.dp)
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .background(
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f),
                shape = RoundedCornerShape(6.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = RoundedCornerShape(2.dp),
                )
        )
    }
}

@Composable
private fun SummaryCard(
    income: Long,
    expense: Long,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(40.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(Res.string.home_income),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatAmount(income),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(30.dp)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.22f))
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(Res.string.home_expense),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatAmount(expense),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun TransactionRow(
    transaction: HomeStore.TransactionItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = transaction.categoryEmoji,
                    style = MaterialTheme.typography.titleLarge
                )
                Column {
                    Text(
                        text = transaction.categoryName ?: stringResource(Res.string.home_unknown_category),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = transaction.time,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = if (transaction.isIncome) {
                    formatSignedAmount(transaction.amount)
                } else {
                    formatAmount(-transaction.amount)
                },
                style = MaterialTheme.typography.titleMedium,
                color = if (transaction.isIncome) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
        }
    }
}
