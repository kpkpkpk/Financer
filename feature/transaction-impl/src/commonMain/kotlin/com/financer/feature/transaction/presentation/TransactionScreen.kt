package com.financer.feature.transaction.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.financer.core.data.model.Category
import com.financer.core.data.model.TransactionType
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

private val FieldShape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)
private val SelectorShape = RoundedCornerShape(16.dp)

@Composable
internal fun TransactionScreen(
    store: TransactionStore,
    modifier: Modifier = Modifier,
) {
    val state by store.stateFlow.collectAsState()
    val canSave = amountInputToKopecks(state.amountInput) > 0L &&
        state.selectedCategory != null &&
        !state.isSaving &&
        !state.isLoading
    val topCategories = state.topCategories.take(3)
    var isCategoryPickerVisible by rememberSaveable { mutableStateOf(false) }
    var isCalendarVisible by rememberSaveable { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TransactionHeader(
                    selectedType = state.type,
                    onClose = { store.accept(TransactionStore.Intent.Close) },
                    onTypeSelected = { store.accept(TransactionStore.Intent.TypeToggled(it)) },
                )
            },
            bottomBar = {
                Surface(
                    tonalElevation = 2.dp,
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    Button(
                        onClick = { store.accept(TransactionStore.Intent.Confirm) },
                        enabled = canSave,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        shape = RoundedCornerShape(20.dp),
                    ) {
                        Text(
                            text = if (state.isSaving) "Сохраняем..." else "Сохранить",
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
            },
        ) { contentPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                TransactionField(
                    label = "Сумма",
                    value = state.amountInput,
                    onValueChange = { store.accept(TransactionStore.Intent.AmountChanged(it)) },
                    placeholder = "0,00",
                    suffix = "₽",
                    keyboardType = KeyboardType.Decimal,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    valueValidator = ::isValidAmountInput,
                )
                TransactionReadOnlyField(
                    label = "Категория",
                    value = state.selectedCategory?.let { "${it.emoji} ${it.name}" }.orEmpty(),
                    placeholder = "Выбери категорию",
                    onClick = { isCategoryPickerVisible = true },
                )
                SuggestionRow(
                    selectedCategoryId = state.selectedCategory?.id,
                    categories = topCategories,
                    onCategorySelected = { store.accept(TransactionStore.Intent.CategorySelected(it)) },
                    onAllCategoriesClick = { isCategoryPickerVisible = true },
                )
                TransactionReadOnlyField(
                    label = "Дата",
                    value = formatDateForField(state.date),
                    placeholder = "Выбери дату",
                    onClick = { isCalendarVisible = true },
                )
                TransactionField(
                    label = "Комментарий",
                    value = state.note,
                    onValueChange = { store.accept(TransactionStore.Intent.NoteChanged(it)) },
                    placeholder = "Необязательно",
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        if (isCategoryPickerVisible) {
            CategoryPickerModal(
                type = state.type,
                allCategories = state.allCategories,
                selectedCategoryId = state.selectedCategory?.id,
                onDismiss = { isCategoryPickerVisible = false },
                onTypeSelected = { store.accept(TransactionStore.Intent.TypeToggled(it)) },
                onConfirm = { categoryId ->
                    store.accept(TransactionStore.Intent.CategorySelected(categoryId))
                    isCategoryPickerVisible = false
                },
            )
        }

        if (isCalendarVisible) {
            MaterialCalendarDialog(
                initialDate = state.date.date,
                onDismiss = { isCalendarVisible = false },
                onConfirm = { date ->
                    store.accept(
                        TransactionStore.Intent.DateChanged(
                            mergeDateWithCurrentTime(currentDateTime = state.date, selectedDate = date)
                        )
                    )
                    isCalendarVisible = false
                },
            )
        }
    }
}

@Composable
private fun TransactionHeader(
    selectedType: TransactionType,
    onClose: () -> Unit,
    onTypeSelected: (TransactionType) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clickable(onClick = onClose),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = "×",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        TypeSelector(
            selectedType = selectedType,
            onTypeSelected = onTypeSelected,
        )

        Spacer(modifier = Modifier.size(40.dp))
    }
}

@Composable
private fun TypeSelector(
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = SelectorShape,
        color = Color(0xFFEDEDED),
    ) {
        Row(
            modifier = Modifier.padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            TypeSelectorItem(
                title = "Расход",
                selected = selectedType == TransactionType.EXPENSE,
                onClick = { onTypeSelected(TransactionType.EXPENSE) },
            )
            TypeSelectorItem(
                title = "Доход",
                selected = selectedType == TransactionType.INCOME,
                onClick = { onTypeSelected(TransactionType.INCOME) },
            )
        }
    }
}

@Composable
private fun TypeSelectorItem(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(13.dp),
        color = if (selected) Color.White else Color.Transparent,
        onClick = onClick,
        shadowElevation = if (selected) 1.dp else 0.dp,
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun TransactionField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    suffix: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    textStyle: TextStyle = MaterialTheme.typography.titleMedium,
    valueValidator: (String) -> Boolean = { true },
    minLines: Int = 1,
    maxLines: Int = 1,
) {
    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = value,
                selection = TextRange(value.length),
            )
        )
    }

    LaunchedEffect(value) {
        if (value != textFieldValue.text) {
            textFieldValue = TextFieldValue(
                text = value,
                selection = TextRange(value.length),
            )
        }
    }

    TextField(
        value = textFieldValue,
        onValueChange = { updatedValue ->
            if (!valueValidator(updatedValue.text)) return@TextField
            textFieldValue = updatedValue
            onValueChange(updatedValue.text)
        },
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        singleLine = maxLines == 1,
        minLines = minLines,
        maxLines = maxLines,
        suffix = if (suffix == null) null else ({ Text(suffix) }),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        textStyle = textStyle.copy(
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Start,
        ),
        shape = FieldShape,
        colors = transactionFieldColors(),
    )
}

@Composable
private fun TransactionReadOnlyField(
    label: String,
    value: String,
    placeholder: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
    ) {
        TextField(
            value = value,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            readOnly = true,
            textStyle = MaterialTheme.typography.titleMedium.copy(
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Start,
            ),
            shape = FieldShape,
            colors = transactionFieldColors(),
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(FieldShape)
                .clickable(onClick = onClick),
        )
    }
}

@Composable
private fun transactionFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    disabledContainerColor = Color.Transparent,
    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
    disabledIndicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    disabledTextColor = MaterialTheme.colorScheme.onSurface,
    focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
    disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
)

@Composable
private fun SuggestionRow(
    selectedCategoryId: Long?,
    categories: List<Category>,
    onCategorySelected: (Long) -> Unit,
    onAllCategoriesClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        categories.forEach { category ->
            SuggestionChip(
                title = "${category.emoji} ${category.name}",
                selected = selectedCategoryId == category.id,
                onClick = { onCategorySelected(category.id) },
            )
        }
        SuggestionChip(
            title = "Все категории",
            selected = false,
            onClick = onAllCategoriesClick,
        )
    }
}

@Composable
private fun SuggestionChip(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        ),
        onClick = onClick,
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun CategoryPickerModal(
    type: TransactionType,
    allCategories: List<Category>,
    selectedCategoryId: Long?,
    onDismiss: () -> Unit,
    onTypeSelected: (TransactionType) -> Unit,
    onConfirm: (Long) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "←",
                    modifier = Modifier
                        .size(40.dp)
                        .clickable(onClick = onDismiss)
                        .padding(top = 4.dp),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "Выбор категории",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.size(40.dp))
            }

            CategoryTypeTabs(
                selectedType = type,
                onTypeSelected = onTypeSelected,
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                allCategories.forEach { category ->
                    CategoryPickerItem(
                        category = category,
                        selected = selectedCategoryId == category.id,
                        onClick = { onConfirm(category.id) },
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun CategoryPickerItem(
    category: Category,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else Color(0xFFE6E6E9),
        ),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = CircleShape,
                    color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.22f) else Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE6E6E9)),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = category.emoji,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                )
            }

            if (selected) {
                Text(
                    text = "✓",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun CategoryTypeTabs(
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        CategoryTypeTab(
            title = "Расход",
            selected = selectedType == TransactionType.EXPENSE,
            onClick = { onTypeSelected(TransactionType.EXPENSE) },
            modifier = Modifier.weight(1f),
        )
        CategoryTypeTab(
            title = "Доход",
            selected = selectedType == TransactionType.INCOME,
            onClick = { onTypeSelected(TransactionType.INCOME) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun CategoryTypeTab(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(
                    if (selected) MaterialTheme.colorScheme.onBackground
                    else Color(0xFFE7E7EA)
                ),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MaterialCalendarDialog(
    initialDate: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.toPickerSelectionMillis(),
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val selectedDateMillis = datePickerState.selectedDateMillis ?: return@Button
                    onConfirm(selectedDateMillis.toLocalDate())
                },
                shape = RoundedCornerShape(16.dp),
            ) {
                Text("Выбрать")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        },
    ) {
        DatePicker(
            state = datePickerState,
            showModeToggle = false,
        )
    }
}

private fun mergeDateWithCurrentTime(
    currentDateTime: LocalDateTime,
    selectedDate: LocalDate,
): LocalDateTime {
    return LocalDateTime(
        year = selectedDate.year,
        month = selectedDate.month,
        day = selectedDate.dayOfMonth,
        hour = currentDateTime.hour,
        minute = currentDateTime.minute,
    )
}

private fun formatDateForField(date: LocalDateTime): String {
    return formatDateForField(date.date)
}

private fun formatDateForField(date: LocalDate): String {
    val day = date.dayOfMonth.toString().padStart(2, '0')
    val month = date.month.number.toString().padStart(2, '0')
    val year = date.year.toString().padStart(4, '0')
    return "$day.$month.$year"
}

private fun LocalDate.toPickerSelectionMillis(): Long {
    return LocalDateTime(
        year = year,
        month = month,
        day = dayOfMonth,
        hour = 0,
        minute = 0,
    ).toInstant(TimeZone.UTC).toEpochMilliseconds()
}

private fun Long.toLocalDate(): LocalDate {
    return Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.UTC).date
}
