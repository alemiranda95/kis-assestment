package com.assestment.kis.domain.di

import com.assestment.kis.domain.util.IdGenerator
import com.assestment.kis.domain.util.TimeProvider
import com.assestment.kis.domain.detection.DistractionMonitor
import com.assestment.kis.domain.detection.FocusConfig
import com.assestment.kis.domain.detection.ObserveDistractionsUseCase
import com.assestment.kis.domain.permission.CheckPermissionUseCase
import com.assestment.kis.domain.session.GetSessionDetailUseCase
import com.assestment.kis.domain.session.GetSessionHistoryUseCase
import com.assestment.kis.domain.session.StartFocusSessionUseCase
import com.assestment.kis.domain.session.StopFocusSessionUseCase
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
    factoryOf(::ObserveDistractionsUseCase)
    factoryOf(::GetSessionHistoryUseCase)
    factoryOf(::GetSessionDetailUseCase)
    factoryOf(::CheckPermissionUseCase)
}
