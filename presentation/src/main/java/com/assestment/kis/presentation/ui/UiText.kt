package com.assestment.kis.presentation.ui

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

/**
 * Lets the domain/ViewModel describe user-facing text without depending on Android resources:
 * [StringResource] is resolved against the caller's resources at render time, [DynamicString]
 * carries an already-formed value (names, formatted numbers).
 */
sealed interface UiText {
    data class DynamicString(val value: String) : UiText
    data class StringResource(@StringRes val id: Int, val args: List<Any> = emptyList()) : UiText

    @Composable
    fun asString(): String = when (this) {
        is DynamicString -> value
        is StringResource -> stringResource(id, *args.toTypedArray())
    }

    /** For resolving outside composition (e.g. inside an event coroutine showing a snackbar). */
    fun asString(context: Context): String = when (this) {
        is DynamicString -> value
        is StringResource -> context.getString(id, *args.toTypedArray())
    }
}
