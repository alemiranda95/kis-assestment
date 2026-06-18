package com.assestment.kis.data.di

import androidx.room.Room
import com.assestment.kis.data.session.local.AppDatabase
import com.assestment.kis.data.session.local.RoomSessionLocalDataSource
import com.assestment.kis.data.session.local.SessionLocalDataSource
import com.assestment.kis.data.session.remote.FakeRemoteSessionDataSource
import com.assestment.kis.data.session.remote.RemoteSessionDataSource
import com.assestment.kis.data.session.repository.OfflineFirstFocusSessionRepository
import com.assestment.kis.domain.session.FocusSessionRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val focusDataModule = module {
    single { Room.databaseBuilder(androidContext(), AppDatabase::class.java, "focus.db").build() }
    single { get<AppDatabase>().sessionDao() }

    singleOf(::RoomSessionLocalDataSource) bind SessionLocalDataSource::class
    // Option B: the fake remote is the runtime default; RetrofitRemoteSessionDataSource is the tested swap.
    single<RemoteSessionDataSource> { FakeRemoteSessionDataSource() }
    singleOf(::OfflineFirstFocusSessionRepository) bind FocusSessionRepository::class
}
