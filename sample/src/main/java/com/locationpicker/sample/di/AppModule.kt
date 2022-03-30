package com.locationpicker.sample.di

import android.content.Context
import com.locationpicker.sample.data.repositories.DeviceInfoRepostory
import com.locationpicker.sample.data.repositories.LocaleRepository
import com.locationpicker.sample.data.repositories.StringsRepository
import com.locationpicker.sample.data.sources.local.AppPreference
import com.locationpicker.sample.data.sources.local.AppPreferenceConstants.DEFAULT_LOCALE
import com.locationpicker.sample.data.sources.local.AppPreferenceConstants.PREFERENCE_FILE_NAME
import com.locationpicker.sample.di.DIConstants.KEY_CURRENT_LANGUAGE
import com.locationpicker.sample.di.DIConstants.KEY_DEVICE_ID
import com.locationpicker.sample.data.sources.resources.AppResources
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module.module


val applicationModule = module {

    single { AppResources(get()) }

    single { StringsRepository(get()) }

    single(PREFERENCE_FILE_NAME) { androidApplication().getSharedPreferences(PREFERENCE_FILE_NAME, Context.MODE_PRIVATE) }

    single { AppPreference(get(PREFERENCE_FILE_NAME)) }


    single { DeviceInfoRepostory(get(),get()) }

    factory(KEY_DEVICE_ID) { get<DeviceInfoRepostory>().getDeviceID()!! }


    single { LocaleRepository(get()) }

    single(DEFAULT_LOCALE) { get<LocaleRepository>().getLocale() }

    factory(KEY_CURRENT_LANGUAGE) { get<LocaleRepository>().getLanguage()!! }


}