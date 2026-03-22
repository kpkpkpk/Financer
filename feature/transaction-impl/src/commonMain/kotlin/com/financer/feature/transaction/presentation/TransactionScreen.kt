package com.financer.feature.transaction.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.Icon
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import com.financer.core.data.model.Category
import com.financer.core.data.model.TransactionType
import financer.core.ui.generated.resources.arrow_narrow_left
import financer.core.ui.generated.resources.check
import financer.core.ui.generated.resources.close_ic
import financer.feature.transaction_impl.generated.resources.Res
import financer.feature.transaction_impl.generated.resources.transaction_amount_label
import financer.feature.transaction_impl.generated.resources.transaction_amount_placeholder
import financer.feature.transaction_impl.generated.resources.transaction_category_all
import financer.feature.transaction_impl.generated.resources.transaction_category_label
import financer.feature.transaction_impl.generated.resources.transaction_category_picker_title
import financer.feature.transaction_impl.generated.resources.transaction_category_placeholder
import financer.feature.transaction_impl.generated.resources.transaction_category_recommended
import financer.feature.transaction_impl.generated.resources.transaction_date_label
import financer.feature.transaction_impl.generated.resources.transaction_date_placeholder
import financer.feature.transaction_impl.generated.resources.transaction_note_label
import financer.feature.transaction_impl.generated.resources.transaction_note_placeholder
import financer.feature.transaction_impl.generated.resources.transaction_save
import financer.feature.transaction_impl.generated.resources.transaction_saving
import financer.feature.transaction_impl.generated.resources.transaction_type_expense
import financer.feature.transaction_impl.generated.resources.transaction_type_income
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import financer.core.ui.generated.resources.Res as CoreRes

private val FieldShape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)
private val SelectorShape = RoundedCornerShape(16.dp)

@Composable
internal fun TransactionScreen(
    uiState: StateFlow<TransactionUiState>,
    slot: Value<ChildSlot<*, DefaultTransactionComponent.SlotChild>>,
    onIntent: (TransactionStore.Intent) -> Unit,
    onOpenCategoryPicker: () -> Unit,
    onCloseCategoryPicker: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by uiState.collectAsState()
    val slotState by slot.subscribeAsState()
    val density = LocalDensity.current
    val isKeyboardVisible = WindowInsets.ime.getBottom(density) > 0
    val focusManager = LocalFocusManager.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { focusManager.clearFocus(force = true) },
            ),
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TransactionHeader(
                    selectedType = state.type,
                    onClose = { onIntent(TransactionStore.Intent.Close) },
                    onTypeSelected = { onIntent(TransactionStore.Intent.TypeToggled(it)) },
                )
            },
            bottomBar = {
                if (!isKeyboardVisible) {
                    Surface(
                        tonalElevation = 2.dp,
                        color = MaterialTheme.colorScheme.surface,
                    ) {
                        Button(
                            onClick = { onIntent(TransactionStore.Intent.Confirm) },
                            enabled = state.canSave,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            shape = RoundedCornerShape(20.dp),
                        ) {
                            Text(
                                text = stringResource(if (state.isSaving) Res.string.transaction_saving else Res.string.transaction_save),
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
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
                    label = stringResource(Res.string.transaction_amount_label),
                    value = state.amountInput,
                    onValueChange = { onIntent(TransactionStore.Intent.AmountChanged(it)) },
                    placeholder = stringResource(Res.string.transaction_amount_placeholder),
                    suffix = "₽",
                    keyboardType = KeyboardType.Decimal,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    valueValidator = ::isValidAmountInput,
                )
                TransactionReadOnlyField(
                    label = stringResource(Res.string.transaction_category_label),
                    value = state.selectedCategory?.let { "${it.emoji} ${it.name}" }.orEmpty(),
                    placeholder = stringResource(Res.string.transaction_category_placeholder),
                    onClick = {
                        focusManager.clearFocus()
                        onOpenCategoryPicker()
                    },
                )
                TransactionDateField(
                    label = stringResource(Res.string.transaction_date_label),
                    value = state.dateInput,
                    onValueChange = { onIntent(TransactionStore.Intent.DateInputChanged(it)) },
                    placeholder = stringResource(Res.string.transaction_date_placeholder),
                )
                TransactionField(
                    label = stringResource(Res.string.transaction_note_label),
                    value = state.note,
                    onValueChange = { onIntent(TransactionStore.Intent.NoteChanged(it)) },
                    placeholder = stringResource(Res.string.transaction_note_placeholder),
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        AnimatedContent(
            targetState = slotState.child?.instance,
            transitionSpec = {
                slideInVertically { it } togetherWith slideOutVertically { it }
            },
        ) { child ->
            when (child) {
                is DefaultTransactionComponent.SlotChild.CategoryPicker -> {
                    CategoryPickerModal(
                        topCategories = state.topCategories,
                        allCategories = state.allCategories,
                        selectedCategoryId = state.selectedCategory?.id,
                        onDismiss = onCloseCategoryPicker,
                        onSelect = { categoryId ->
                            onIntent(TransactionStore.Intent.CategorySelected(categoryId))
                            onCloseCategoryPicker()
                        },
                    )
                }

                null -> Unit
            }
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
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClose,
                ),
            contentAlignment = Alignment.CenterStart,
        ) {
            Icon(
                painter = painterResource(CoreRes.drawable.close_ic),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onBackground,
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
            modifier = Modifier.padding(horizontal = 3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            TypeSelectorItem(
                title = stringResource(Res.string.transaction_type_expense),
                selected = selectedType == TransactionType.EXPENSE,
                onClick = { onTypeSelected(TransactionType.EXPENSE) },
            )
            TypeSelectorItem(
                title = stringResource(Res.string.transaction_type_income),
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
private fun TransactionDateField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
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
            val filtered = updatedValue.text.filter { it.isDigit() }.take(8)
            if (filtered != textFieldValue.text) {
                textFieldValue = TextFieldValue(
                    text = filtered,
                    selection = TextRange(filtered.length),
                )
                onValueChange(filtered)
            }
        },
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        visualTransformation = DateMaskVisualTransformation,
        textStyle = MaterialTheme.typography.titleMedium.copy(
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Start,
        ),
        shape = FieldShape,
        colors = transactionFieldColors(),
    )
}

private object DateMaskVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val out = StringBuilder()
        raw.forEachIndexed { index, char ->
            if (index == 2 || index == 4) out.append('.')
            out.append(char)
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return when {
                    offset <= 2 -> offset
                    offset <= 4 -> offset + 1
                    else -> offset + 2
                }
            }

            override fun transformedToOriginal(offset: Int): Int {
                return when {
                    offset <= 2 -> offset
                    offset <= 5 -> offset - 1
                    else -> offset - 2
                }.coerceIn(0, raw.length)
            }
        }

        return TransformedText(AnnotatedString(out.toString()), offsetMapping)
    }
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
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                ),
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
private fun CategoryPickerModal(
    topCategories: List<Category>,
    allCategories: List<Category>,
    selectedCategoryId: Long?,
    onDismiss: () -> Unit,
    onSelect: (Long) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            // Header
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
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onDismiss,
                        ),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Icon(
                        painter = painterResource(CoreRes.drawable.arrow_narrow_left),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
                Text(
                    text = stringResource(Res.string.transaction_category_picker_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.size(40.dp))
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
            ) {
                if (topCategories.isNotEmpty()) {
                    item(key = "header-recommended") {
                        Text(
                            text = stringResource(Res.string.transaction_category_recommended),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 8.dp, bottom = 12.dp),
                        )
                    }
                    items(
                        items = topCategories,
                        key = { "top-${it.id}" },
                    ) { category ->
                        CategoryRow(
                            category = category,
                            selected = selectedCategoryId == category.id,
                            onClick = { onSelect(category.id) },
                        )
                    }
                }

                item(key = "header-all") {
                    Text(
                        text = stringResource(Res.string.transaction_category_all),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(
                            top = if (topCategories.isNotEmpty()) 20.dp else 8.dp,
                            bottom = 12.dp,
                        ),
                    )
                }
                items(
                    items = allCategories,
                    key = { "all-${it.id}" },
                ) { category ->
                    CategoryRow(
                        category = category,
                        selected = selectedCategoryId == category.id,
                        onClick = { onSelect(category.id) },
                    )
                }

                item(key = "bottom-spacer") {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun CategoryRow(
    category: Category,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(
                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                else Color.Transparent,
            )
            .padding(horizontal = 12.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = category.emoji,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            )
        }

        if (selected) {
            Icon(
                painter = painterResource(CoreRes.drawable.check),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
