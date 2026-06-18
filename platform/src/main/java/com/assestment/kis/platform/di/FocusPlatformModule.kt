package com.assestment.kis.platform.di

import com.assestment.kis.domain.detection.MotionSource
import com.assestment.kis.domain.detection.NoiseSource
import com.assestment.kis.domain.notification.DistractionNotifier
import com.assestment.kis.domain.permission.PermissionChecker
import com.assestment.kis.platform.notification.AndroidDistractionNotifier
import com.assestment.kis.platform.permission.AndroidPermissionChecker
import com.assestment.kis.platform.sensor.AudioRecordNoiseSource
import com.assestment.kis.platform.sensor.SensorManagerMotionSource
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val focusPlatformModule = module {
    single<NoiseSource> { AudioRecordNoiseSource(androidContext()) }
    single<MotionSource> { SensorManagerMotionSource(androidContext()) }
    single<DistractionNotifier> { AndroidDistractionNotifier(androidContext()) }
    single<PermissionChecker> { AndroidPermissionChecker(androidContext()) }
}
