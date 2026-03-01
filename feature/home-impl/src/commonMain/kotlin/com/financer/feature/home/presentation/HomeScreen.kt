package com.financer.feature.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.animation.core.animate
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.FloatingActionButtonElevation
import androidx.compose.material3.Icon
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode.Companion.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity

import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import financer.feature.home_impl.generated.resources.Res
import financer.feature.home_impl.generated.resources.home_balance
import financer.feature.home_impl.generated.resources.home_empty_state
import financer.feature.home_impl.generated.resources.home_expense
import financer.feature.home_impl.generated.resources.home_income
import financer.feature.home_impl.generated.resources.home_period_this_month
import financer.feature.home_impl.generated.resources.home_unknown_category
import financer.feature.home_impl.generated.resources.home_add_btn
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

private val ExpandedHeaderHeight: Dp = 222.dp
private val CollapsedHeaderHeight: Dp = 56.dp
private val HorizontalPadding: Dp = 14.dp
private val SummaryCardPadding: Dp = 16.dp
private val SummaryCardBottomPadding: Dp = 18.dp

private const val NavIconId = "navIcon"
private const val ActionsId = "actions"
private const val ExpandedLabelId = "expandedLabel"
private const val ExpandedAmountId = "expandedAmount"
private const val CollapsedTitleId = "collapsedTitle"
private const val PeriodChipId = "periodChip"
private const val SummaryCardId = "summaryCard"

@Stable
private class CollapsingToolbarState(
    val expandedHeightPx: Float,
    val collapsedHeightPx: Float,
    initialHeightOffsetPx: Float = 0f,
    val canExpand: () -> Boolean = { true },
) {
    private val collapseRangePx = expandedHeightPx - collapsedHeightPx

    var heightOffsetPx by mutableFloatStateOf(initialHeightOffsetPx)
        internal set

    val collapsedFraction: Float
        get() = if (collapseRangePx > 0f) -heightOffsetPx / collapseRangePx else 0f

    val currentHeightPx: Float
        get() = expandedHeightPx + heightOffsetPx

    private suspend fun settle() {
        if (collapsedFraction in 0.01f..0.99f) {
            val target = if (collapsedFraction > 0.5f) -collapseRangePx else 0f
            animate(
                initialValue = heightOffsetPx,
                targetValue = target,
            ) { value, _ ->
                heightOffsetPx = value
            }
        }
    }

    val nestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            val delta = available.y
            // Expand (delta > 0) only when list is at the top
            if (delta > 0 && !canExpand()) return Offset.Zero
            val newOffset = (heightOffsetPx + delta).coerceIn(-collapseRangePx, 0f)
            val consumed = newOffset - heightOffsetPx
            heightOffsetPx = newOffset
            return Offset(0f, consumed)
        }

        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
            settle()
            return Velocity.Zero
        }
    }
}

@Composable
private fun rememberCollapsingToolbarState(
    initialHeightOffsetPx: Float = 0f,
    canExpand: () -> Boolean = { true },
): CollapsingToolbarState {
    val density = LocalDensity.current
    return remember {
        CollapsingToolbarState(
            expandedHeightPx = with(density) { ExpandedHeaderHeight.toPx() },
            collapsedHeightPx = with(density) { CollapsedHeaderHeight.toPx() },
            initialHeightOffsetPx = initialHeightOffsetPx,
            canExpand = canExpand,
        )
    }
}

@Composable
internal fun HomeScreen(
    headerComponent: HomeHeaderComponent,
    listComponent: HomeListComponent,
    addTransactionButtonComponent: HomeAddTransactionButtonComponent,
    modifier: Modifier = Modifier,
) {
    val headerUiState by headerComponent.uiState.collectAsState()
    val listUiState by listComponent.uiState.collectAsState()

    val periodTitle = when (headerUiState.periodPreset) {
        HomeStore.PeriodPreset.ThisMonth -> stringResource(Res.string.home_period_this_month)
        HomeStore.PeriodPreset.Custom -> headerUiState.periodCustomTitle
    }

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = listComponent.savedFirstVisibleItemIndex,
        initialFirstVisibleItemScrollOffset = listComponent.savedFirstVisibleItemScrollOffset,
    )

    val toolbarState = rememberCollapsingToolbarState(
        initialHeightOffsetPx = headerComponent.savedToolbarHeightOffsetPx,
        canExpand = { listState.firstVisibleItemIndex <= 2 },
    )
    val currentHeaderHeight = with(LocalDensity.current) { toolbarState.currentHeightPx.toDp() }

    DisposableEffect(listState, toolbarState) {
        onDispose {
            listComponent.savedFirstVisibleItemIndex = listState.firstVisibleItemIndex
            listComponent.savedFirstVisibleItemScrollOffset = listState.firstVisibleItemScrollOffset
            headerComponent.savedToolbarHeightOffsetPx = toolbarState.heightOffsetPx
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .nestedScroll(toolbarState.nestedScrollConnection),
    ) {
        LazyColumn(
            modifier = Modifier.padding(top = 16.dp).fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(top = currentHeaderHeight, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                items = listUiState.items,
                key = { item -> item.key },
            ) { item ->
                when (item) {
                    is HomeListItem.DateHeader -> {
                        Text(
                            text = item.title.resolve(),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        )
                    }

                    is HomeListItem.Transaction -> {
                        TransactionRow(
                            transaction = item.item,
                            onClick = { listComponent.onTransactionClicked(item.item.id) },
                        )
                    }

                    HomeListItem.EmptyState -> {
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

                    is HomeListItem.Space -> {
                        Spacer(Modifier.height(item.heightDp.dp))
                    }
                }
            }
        }

        CollapsingHeader(
            toolbarState = toolbarState,
            balance = headerUiState.formattedBalance,
            income = headerUiState.formattedIncome,
            expense = headerUiState.formattedExpense,
            periodTitle = periodTitle,
            onFilterClick = headerComponent::onFilterClicked,
            modifier = Modifier
                .fillMaxWidth()
                .clipToBounds()
                .align(Alignment.TopCenter),
        )

        ExtendedFloatingActionButton(
            modifier = Modifier.padding(bottom = 4.dp).align(Alignment.BottomCenter),
            onClick = { addTransactionButtonComponent.onAddTransactionClicked() },
            elevation = FloatingActionButtonDefaults.elevation(1.dp),
            text = { Text(stringResource(Res.string.home_add_btn)) },
            icon = { Box(Modifier.size(24.dp)) }
        )
    }
}

@Composable
private fun CollapsingHeader(
    toolbarState: CollapsingToolbarState,
    balance: String,
    income: String,
    expense: String,
    periodTitle: String,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val collapsedFraction = toolbarState.collapsedFraction
    val cornerRadius = lerp(40f, 0f, collapsedFraction)
    val secondaryAlpha = ((1f - collapsedFraction) / 0.1f).coerceIn(0f, 1f)
    val balanceLabel = stringResource(Res.string.home_balance)

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(bottomStart = cornerRadius.dp, bottomEnd = cornerRadius.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Layout(
            content = {
                HeaderSquareAction(
                    onClick = onFilterClick,
                    modifier = Modifier.layoutId(NavIconId),
                )

                HeaderSquareAction(
                    onClick = {},
                    modifier = Modifier.layoutId(ActionsId),
                )

                Text(
                    text = balanceLabel,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.layoutId(ExpandedLabelId),
                )

                Text(
                    text = balance,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.layoutId(ExpandedAmountId),
                )

                Text(
                    text = "$balanceLabel: $balance",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.layoutId(CollapsedTitleId),
                )

                PeriodChip(
                    text = periodTitle,
                    onClick = onFilterClick,
                    modifier = Modifier.layoutId(PeriodChipId),
                )

                SummaryCard(
                    income = income,
                    expense = expense,
                    modifier = Modifier.layoutId(SummaryCardId),
                )
            },
        ) { measurables, constraints ->
            val horizontalPaddingPx = HorizontalPadding.toPx()
            val expandedHeightPx = toolbarState.expandedHeightPx
            val collapsedHeightPx = toolbarState.collapsedHeightPx
            val currentHeightPx = toolbarState.currentHeightPx
            val cf = collapsedFraction

            // Measure navigation icon and actions
            val navIconPlaceable = measurables.first { it.layoutId == NavIconId }
                .measure(constraints.copy(minWidth = 0, minHeight = 0))
            val actionsPlaceable = measurables.first { it.layoutId == ActionsId }
                .measure(constraints.copy(minWidth = 0, minHeight = 0))

            val navIconOffset = navIconPlaceable.width + horizontalPaddingPx * 2
            val actionsOffset = actionsPlaceable.width + horizontalPaddingPx * 2

            // Measure expanded title elements
            val expandedLabelPlaceable = measurables.first { it.layoutId == ExpandedLabelId }
                .measure(constraints.copy(minWidth = 0, minHeight = 0))
            val expandedAmountPlaceable = measurables.first { it.layoutId == ExpandedAmountId }
                .measure(constraints.copy(minWidth = 0, minHeight = 0))

            // Measure collapsed title with available width between icons
            val collapsedTitleMaxWidth = (constraints.maxWidth - navIconOffset - actionsOffset)
                .roundToInt().coerceAtLeast(0)
            val collapsedTitlePlaceable = measurables.first { it.layoutId == CollapsedTitleId }
                .measure(
                    constraints.copy(
                        minWidth = 0,
                        maxWidth = collapsedTitleMaxWidth,
                        minHeight = 0
                    )
                )

            // Measure period chip and summary card
            val periodChipPlaceable = measurables.first { it.layoutId == PeriodChipId }
                .measure(constraints.copy(minWidth = 0, minHeight = 0))

            val summaryCardMaxWidth =
                (constraints.maxWidth - 2 * SummaryCardPadding.toPx()).roundToInt()
            val summaryCardPlaceable = measurables.first { it.layoutId == SummaryCardId }
                .measure(
                    constraints.copy(
                        minWidth = 0,
                        maxWidth = summaryCardMaxWidth,
                        minHeight = 0
                    )
                )

            layout(constraints.maxWidth, currentHeightPx.roundToInt()) {
                // Expanded fades out in 0..30%, collapsed fades in at 70..100%
                val expandedAlpha = (1f - cf / 0.3f).coerceIn(0f, 1f)
                val collapsedAlpha = ((cf - 0.7f) / 0.3f).coerceIn(0f, 1f)
                // Navigation icon: top-left in expanded -> vertically centered in collapsed
                val navExpandedY = 10.dp.toPx()
                val navCollapsedY = (collapsedHeightPx - navIconPlaceable.height) / 2
                navIconPlaceable.placeRelative(
                    x = horizontalPaddingPx.roundToInt(),
                    y = lerp(navExpandedY, navCollapsedY, cf).roundToInt(),
                )

                // Actions: top-right in expanded -> vertically centered in collapsed
                val actExpandedY = 10.dp.toPx()
                val actCollapsedY = (collapsedHeightPx - actionsPlaceable.height) / 2
                actionsPlaceable.placeRelative(
                    x = (constraints.maxWidth - actionsPlaceable.width - horizontalPaddingPx).roundToInt(),
                    y = lerp(actExpandedY, actCollapsedY, cf).roundToInt(),
                )

                // Label + chip group: centered together at 24% of expanded height
                val chipGap = 6.dp.toPx()
                val groupWidth = expandedLabelPlaceable.width + chipGap + periodChipPlaceable.width
                val groupStartX = (constraints.maxWidth - groupWidth) / 2
                val labelY =
                    (expandedHeightPx * 0.24f - expandedLabelPlaceable.height / 2).roundToInt()

                expandedLabelPlaceable.placeRelativeWithLayer(
                    x = groupStartX.roundToInt(),
                    y = labelY,
                    layerBlock = { alpha = expandedAlpha }
                )

                val chipX = groupStartX + expandedLabelPlaceable.width + chipGap
                val chipY = expandedHeightPx * 0.24f - periodChipPlaceable.height / 2
                periodChipPlaceable.placeRelativeWithLayer(
                    x = chipX.roundToInt(),
                    y = chipY.roundToInt(),
                    layerBlock = { alpha = expandedAlpha }
                )

                // Expanded amount: centered at 42% of expanded height, fades out
                expandedAmountPlaceable.placeRelativeWithLayer(
                    x = (constraints.maxWidth - expandedAmountPlaceable.width) / 2,
                    y = (expandedHeightPx * 0.42f - expandedAmountPlaceable.height / 2).roundToInt(),
                    layerBlock = { alpha = expandedAlpha }
                )

                // Collapsed title: after nav icon, vertically centered in collapsed bar, fades in
                collapsedTitlePlaceable.placeRelativeWithLayer(
                    x = navIconOffset.roundToInt(),
                    y = ((collapsedHeightPx - collapsedTitlePlaceable.height) / 2).roundToInt(),
                    layerBlock = { alpha = collapsedAlpha }
                )

                // Summary card: stays at expanded position, clipped by clipToBounds
                if (secondaryAlpha > 0f) {
                    val summaryY =
                        expandedHeightPx - summaryCardPlaceable.height - SummaryCardBottomPadding.toPx()
                    summaryCardPlaceable.placeRelativeWithLayer(
                        x = (constraints.maxWidth - summaryCardPlaceable.width) / 2,
                        y = summaryY.roundToInt(),
                        layerBlock = { alpha = secondaryAlpha }
                    )
                }
            }
        }
    }
}

@Composable
private fun PeriodChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.45f))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Spacer(modifier = Modifier.width(6.dp))
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
    income: String,
    expense: String,
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
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(Res.string.home_income),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = income,
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
                    text = expense,
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
    transaction: HomeTransactionItem,
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
                        text = transaction.categoryName
                            ?: stringResource(Res.string.home_unknown_category),
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
                text = transaction.formattedAmount,
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
