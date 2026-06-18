package com.assestment.kis.domain.di

import com.assestment.kis.domain.core.IdGenerator
import com.assestment.kis.domain.core.TimeProvider
import com.assestment.kis.domain.session.detection.DistractionMonitor
import com.assestment.kis.domain.session.detection.FocusConfig
import com.assestment.kis.domain.session.usecase.StartFocusSessionUseCase
import com.assestment.kis.domain.session.usecase.StopFocusSessionUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import java.util.UUID

val focusDomainModule = module {
    single { FocusConfig.Default }
    single<TimeProvider> { TimeProvider { System.currentTimeMillis() } }
    single<IdGenerator> { IdGenerator { UUID.randomUUID().toString() } }

    factoryOf(::DistractionMonitor)
    factoryOf(::StartFocusSessionUseCase)
    factoryOf(::StopFocusSessionUseCase)
}
