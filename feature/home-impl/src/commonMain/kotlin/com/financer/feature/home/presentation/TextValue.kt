package com.financer.feature.home.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Immutable
internal sealed interface TextValue {

    @Immutable
    data class Raw(val text: String) : TextValue

    @Immutable
    data class Resource(val res: StringResource, val args: List<Any> = emptyList()) : TextValue

    @Immutable
    data class Composite(val parts: List<TextValue>) : TextValue
}

internal class TextValueBuilder {
    private val parts = mutableListOf<TextValue>()

    fun addRaw(text: String) = apply { parts.add(TextValue.Raw(text)) }

    fun addResource(res: StringResource, vararg args: Any) = apply {
        parts.add(TextValue.Resource(res, args.toList()))
    }

    fun build(): TextValue = when (parts.size) {
        0 -> TextValue.Raw("")
        1 -> parts.first()
        else -> TextValue.Composite(parts.toList())
    }
}

internal fun buildTextValue(block: TextValueBuilder.() -> Unit): TextValue =
    TextValueBuilder().apply(block).build()

@Composable
internal fun TextValue.resolve(): String = when (this) {
    is TextValue.Raw -> text
    is TextValue.Resource -> {
        if (args.isEmpty()) stringResource(res)
        else stringResource(res, *args.toTypedArray())
    }
    is TextValue.Composite -> {
        val sb = StringBuilder()
        for (part in parts) {
            sb.append(part.resolve())
        }
        sb.toString()
    }
}
