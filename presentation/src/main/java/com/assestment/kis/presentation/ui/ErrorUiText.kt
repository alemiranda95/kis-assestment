package com.assestment.kis.presentation.ui

import com.assestment.kis.domain.util.DataError
import com.assestment.kis.presentation.R

fun DataError.toUiText(): UiText = UiText.StringResource(
    when (this) {
        DataError.Network.NO_INTERNET -> R.string.error_no_internet
        DataError.Network.REQUEST_TIMEOUT -> R.string.error_timeout
        DataError.Network.SERVER_ERROR -> R.string.error_server
        DataError.Network.SERIALIZATION -> R.string.error_unexpected
        DataError.Local.DISK_FULL -> R.string.error_disk_full
        else -> R.string.error_unexpected
    },
)
