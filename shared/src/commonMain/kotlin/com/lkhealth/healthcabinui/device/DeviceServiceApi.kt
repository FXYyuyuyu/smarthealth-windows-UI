package com.lkhealth.healthcabinui.device

import kotlinx.coroutines.flow.Flow

/**
 * 设备服务契约，对应底层 DEVICE_SERVICE_GUIDE.md 中的 DeviceServiceApi。
 * 当前由 [MockDeviceServiceApi] 实现；真实的 `:core:device` 模块接入后，
 * 只需替换 Koin 绑定（见 AppModule.kt），UI 与 ViewModel 层无需改动。
 */
interface DeviceServiceApi {
    /** 列出所有设备。 */
    fun listDevices(): Flow<List<DeviceDescriptor>>

    /** 发起测量，返回进度流。options 用于传递超时等参数。 */
    fun measure(
        deviceType: DeviceType,
        source: SourceType,
        options: Map<String, Any> = emptyMap(),
    ): Flow<SessionProgress>

    /** 取消测量。 */
    fun cancelMeasurement(sessionId: String, source: SourceType): Boolean

    /** 获取当前会话（测量已完成后可取结果值）。 */
    fun getActiveSession(): DeviceServiceSession?

    suspend fun connect(deviceId: String): Result<Unit>

    suspend fun disconnect(deviceId: String): Result<Unit>

    fun getDeviceStatus(deviceId: String): DeviceStatus

    /** 手动录入结果（校准或人工输入场景）。 */
    suspend fun injectManualResult(
        deviceType: DeviceType,
        values: Map<String, Double>,
        source: SourceType,
    ): Result<Unit>
}
