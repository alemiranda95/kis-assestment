package com.assestment.kis

import android.app.Application
import com.assestment.kis.data.di.focusDataModule
import com.assestment.kis.di.appModule
import com.assestment.kis.domain.di.focusDomainModule
import com.assestment.kis.platform.di.focusPlatformModule
import com.assestment.kis.presentation.di.focusPresentationModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class FocusApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@FocusApp)
            modules(
                focusDomainModule,
                focusDataModule,
                focusPlatformModule,
                focusPresentationModule,
                appModule,
            )
        }
    }
}
