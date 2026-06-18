package com.assestment.kis.di

import com.assestment.kis.domain.detection.MotionSource
import com.assestment.kis.domain.detection.NoiseSource
import com.assestment.kis.domain.notification.DistractionNotifier
import com.assestment.kis.domain.permission.PermissionChecker
import com.assestment.kis.fake.DemoMotionSource
import com.assestment.kis.fake.DemoNoiseSource
import com.assestment.kis.fake.GrantAllPermissionChecker
import com.assestment.kis.fake.NoOpDistractionNotifier
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

// Fake sensors/notifier/permission so the app runs before the real :platform bindings (Phase 5).
// The repository now comes from :data (focusDataModule).
val appModule = module {
    singleOf(::DemoNoiseSource) bind NoiseSource::class
    singleOf(::DemoMotionSource) bind MotionSource::class
    singleOf(::NoOpDistractionNotifier) bind DistractionNotifier::class
    singleOf(::GrantAllPermissionChecker) bind PermissionChecker::class
}
