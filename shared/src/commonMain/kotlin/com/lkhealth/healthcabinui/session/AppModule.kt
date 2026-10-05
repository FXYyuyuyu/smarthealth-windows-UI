package com.lkhealth.healthcabinui.session

import com.lkhealth.healthcabinui.device.DeviceServiceApi
import com.lkhealth.healthcabinui.device.MockDeviceServiceApi
import com.lkhealth.healthcabinui.directory.MockUserDirectoryApi
import com.lkhealth.healthcabinui.directory.UserDirectoryApi
import org.koin.dsl.module

/**
 * 唯一的 DI 换装点：真实的 `:core:device` 模块接入后，把 `MockDeviceServiceApi()`
 * 换成后端提供的实现即可，[AppViewModel] 及其上的所有 UI 代码都不需要改动。
 */
val appModule = module {
    single<DeviceServiceApi> { MockDeviceServiceApi() }
    single<UserDirectoryApi> { MockUserDirectoryApi() }
    single { AppViewModel(get(), get()) }
}
