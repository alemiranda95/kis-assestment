package com.assestment.kis.presentation.di

import com.assestment.kis.presentation.focus.FocusViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val focusPresentationModule = module {
    viewModelOf(::FocusViewModel)
}
