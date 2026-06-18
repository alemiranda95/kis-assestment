package com.assestment.kis.di

import com.assestment.kis.domain.notification.DistractionNotifier
import com.assestment.kis.domain.permission.PermissionChecker
import com.assestment.kis.domain.session.FocusSessionRepository
import com.assestment.kis.domain.detection.MotionSource
import com.assestment.kis.domain.detection.NoiseSource
import com.assestment.kis.fake.DemoMotionSource
import com.assestment.kis.fake.DemoNoiseSource
import com.assestment.kis.fake.GrantAllPermissionChecker
import com.assestment.kis.fake.InMemoryFocusSessionRepository
import com.assestment.kis.fake.NoOpDistractionNotifier
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

// Phase 3 wiring: fake sources/notifier/permission/repository so the app runs end-to-end.
// Replaced by the real :platform and :data bindings in Phases 4–5.
val appModule = module {
    singleOf(::DemoNoiseSource) bind NoiseSource::class
    singleOf(::DemoMotionSource) bind MotionSource::class
    singleOf(::NoOpDistractionNotifier) bind DistractionNotifier::class
    singleOf(::GrantAllPermissionChecker) bind PermissionChecker::class
    singleOf(::InMemoryFocusSessionRepository) bind FocusSessionRepository::class
}
