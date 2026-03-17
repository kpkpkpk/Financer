package com.financer.feature.transaction.presentation

import androidx.compose.runtime.Immutable
import com.financer.core.data.model.Category
import com.financer.core.data.model.TransactionType

@Immutable
internal data class TransactionUiState(
    val amountInput: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val selectedCategory: Category? = null,
    val formattedDate: String = "",
    val dateInput: String = "",
    val note: String = "",
    val topCategories: List<Category> = emptyList(),
    val allCategories: List<Category> = emptyList(),
    val canSave: Boolean = false,
    val isSaving: Boolean = false,
    val isLoading: Boolean = true,
    val isEditMode: Boolean = false,
)

internal class TransactionUiStateMapper {

    fun map(state: TransactionStore.State): TransactionUiState {
        return TransactionUiState(
            amountInput = state.amountInput,
            type = state.type,
            selectedCategory = state.selectedCategory,
            formattedDate = formatDateInput(state.dateInput),
            dateInput = state.dateInput,
            note = state.note,
            topCategories = state.topCategories.take(5),
            allCategories = state.allCategories,
            canSave = amountInputToKopecks(state.amountInput) > 0L &&
                state.selectedCategory != null &&
                !state.isSaving &&
                !state.isLoading &&
                isValidDateInput(state.dateInput),
            isSaving = state.isSaving,
            isLoading = state.isLoading,
            isEditMode = state.transactionId != null,
        )
    }
}

private fun formatDateInput(rawDigits: String): String {
    if (rawDigits.isEmpty()) return ""
    val sb = StringBuilder()
    rawDigits.forEachIndexed { index, char ->
        if (index == 2 || index == 4) sb.append('.')
        sb.append(char)
    }
    return sb.toString()
}

internal fun isValidDateInput(rawDigits: String): Boolean {
    if (rawDigits.length != 8) return false
    val day = rawDigits.substring(0, 2).toIntOrNull() ?: return false
    val month = rawDigits.substring(2, 4).toIntOrNull() ?: return false
    val year = rawDigits.substring(4, 8).toIntOrNull() ?: return false
    return day in 1..31 && month in 1..12 && year in 2000..2100
}
