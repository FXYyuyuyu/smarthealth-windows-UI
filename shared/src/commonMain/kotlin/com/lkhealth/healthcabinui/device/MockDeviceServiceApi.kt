package com.lkhealth.healthcabinui.device

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlin.math.pow
import kotlin.random.Random

/**
 * 设备服务的桌面/联调期 Mock 实现：用协程 + Flow 模拟"连接→测量→出结果"的完整时序，
 * 产出的数值落在合理生理区间内。真实 `:core:device` 模块接入后按 [DeviceServiceApi]
 * 替换 Koin 绑定即可，UI 层无需改动。
 */
class MockDeviceServiceApi : DeviceServiceApi {

    private val cancelledSessions = mutableSetOf<String>()
    private var activeSession: DeviceServiceSession? = null
    private val deviceStatuses = mutableMapOf<String, DeviceStatus>()

    override fun listDevices(): Flow<List<DeviceDescriptor>> = flowOf(
        MeasurementCatalog.specs.map { spec ->
            DeviceDescriptor(
                driverId = spec.deviceType.name,
                displayName = spec.title,
                deviceType = spec.deviceType,
                transportType = "mock",
                status = deviceStatuses[spec.deviceType.name] ?: DeviceStatus.CONNECTED,
            )
        },
    )

    override fun measure(
        deviceType: DeviceType,
        source: SourceType,
        options: Map<String, Any>,
    ): Flow<SessionProgress> = flow {
        val spec = MeasurementCatalog.of(deviceType)
        val sessionId = "mock-${deviceType.name}-${Random.nextInt(10_000, 99_999)}"

        emit(SessionProgress(sessionId, deviceType, SessionStatus.PREPARING, 0, "正在连接设备..."))
        delay(400)
        emit(SessionProgress(sessionId, deviceType, SessionStatus.CONNECTING, 0, "设备已连接，准备采集..."))
        delay(300)

        var percent = 0
        while (percent < 100) {
            if (cancelledSessions.remove(sessionId)) {
                emit(SessionProgress(sessionId, deviceType, SessionStatus.CANCELLED, percent, "已取消测量"))
                return@flow
            }
            delay(120)
            percent = (percent + 4).coerceAtMost(100)
            val partIndex = partIndexFor(spec, percent)
            emit(
                SessionProgress(
                    sessionId = sessionId,
                    deviceType = deviceType,
                    status = SessionStatus.IN_PROGRESS,
                    percentage = percent,
                    currentStep = stepLabel(spec, percent, partIndex),
                    partIndex = partIndex,
                ),
            )
        }

        val values = generateValues(deviceType)
        activeSession = DeviceServiceSession(sessionId, deviceType, values)
        emit(
            SessionProgress(
                sessionId = sessionId,
                deviceType = deviceType,
                status = SessionStatus.COMPLETED,
                percentage = 100,
                currentStep = "测量完成",
                partIndex = spec.parts.lastIndex,
            ),
        )
    }

    override fun cancelMeasurement(sessionId: String, source: SourceType): Boolean {
        cancelledSessions += sessionId
        return true
    }

    override fun getActiveSession(): DeviceServiceSession? = activeSession

    override suspend fun connect(deviceId: String): Result<Unit> {
        delay(200)
        deviceStatuses[deviceId] = DeviceStatus.CONNECTED
        return Result.success(Unit)
    }

    override suspend fun disconnect(deviceId: String): Result<Unit> {
        delay(100)
        deviceStatuses[deviceId] = DeviceStatus.DISCONNECTED
        return Result.success(Unit)
    }

    override fun getDeviceStatus(deviceId: String): DeviceStatus =
        deviceStatuses[deviceId] ?: DeviceStatus.CONNECTED

    override suspend fun injectManualResult(
        deviceType: DeviceType,
        values: Map<String, Double>,
        source: SourceType,
    ): Result<Unit> {
        activeSession = DeviceServiceSession("manual-${deviceType.name}", deviceType, values)
        return Result.success(Unit)
    }

    private fun partIndexFor(spec: MeasurementSpec, percent: Int): Int {
        if (spec.parts.size <= 1) return 0
        val span = 100.0 / spec.parts.size
        return (percent / span).toInt().coerceIn(0, spec.parts.lastIndex)
    }

    private fun stepLabel(spec: MeasurementSpec, percent: Int, partIndex: Int): String {
        spec.progressPhases.firstOrNull { percent <= it.untilPercent }?.let { return it.label }
        if (spec.isMultiPart) return "正在测量${spec.parts[partIndex].label}..."
        return "正在测量${spec.title}..."
    }

    private fun generateValues(deviceType: DeviceType): Map<String, Double> = when (deviceType) {
        DeviceType.HEIGHT_WEIGHT -> {
            val height = Random.nextDouble(155.0, 185.0)
            val weight = Random.nextDouble(50.0, 85.0)
            mapOf(
                "height" to height,
                "weight" to weight,
                "bmi" to weight / (height / 100).pow(2),
            )
        }
        DeviceType.BLOOD_PRESSURE -> mapOf(
            "systolic" to Random.nextDouble(105.0, 135.0),
            "diastolic" to Random.nextDouble(65.0, 88.0),
            "pulse" to Random.nextDouble(60.0, 90.0),
        )
        DeviceType.BLOOD_OXYGEN -> mapOf(
            "spo2" to Random.nextDouble(95.0, 100.0),
            "pulse" to Random.nextDouble(60.0, 95.0),
        )
        DeviceType.BLOOD_GLUCOSE -> mapOf(
            "glucose" to Random.nextDouble(4.2, 7.0),
        )
        DeviceType.BODY_FAT -> mapOf(
            "bodyFatPercent" to Random.nextDouble(14.0, 28.0),
            "bmr" to Random.nextDouble(1250.0, 1750.0),
        )
        DeviceType.TEMPERATURE -> mapOf(
            "temperature" to Random.nextDouble(36.1, 37.2),
        )
        DeviceType.WAIST_HIP -> {
            val waist = Random.nextDouble(65.0, 95.0)
            val hip = Random.nextDouble(85.0, 105.0)
            mapOf("waist" to waist, "hip" to hip, "waistHipRatio" to waist / hip)
        }
        DeviceType.ECG -> mapOf(
            "heartRate" to Random.nextDouble(60.0, 95.0),
            "prInterval" to Random.nextDouble(120.0, 200.0),
            "qrsDuration" to Random.nextDouble(80.0, 110.0),
            "qtInterval" to Random.nextDouble(350.0, 440.0),
        )
        DeviceType.URIC_ACID -> mapOf(
            "uricAcid" to Random.nextDouble(0.15, 0.45),
        )
        DeviceType.CHOLESTEROL -> mapOf(
            "chol" to Random.nextDouble(3.6, 5.8),
        )
        DeviceType.BLOOD_LIPID -> {
            val chol = Random.nextDouble(3.6, 5.8)
            val hdl = Random.nextDouble(1.0, 1.8)
            val ldl = Random.nextDouble(2.0, 3.4)
            mapOf(
                "chol" to chol,
                "tg" to Random.nextDouble(0.6, 1.8),
                "hdl" to hdl,
                "ldl" to ldl,
            )
        }
        DeviceType.HEMOGLOBIN -> mapOf(
            "hb" to Random.nextDouble(12.0, 16.0),
        )
        DeviceType.HBA1C -> mapOf(
            "ngsp" to Random.nextDouble(4.5, 6.0),
        )
        DeviceType.VISION -> mapOf(
            "visionLeft" to Random.nextDouble(4.6, 5.3),
            "visionRight" to Random.nextDouble(4.6, 5.3),
        )
        DeviceType.ARTERIOSCLEROSIS -> mapOf(
            "vpSystolic" to Random.nextDouble(105.0, 135.0),
            "vpDiastolic" to Random.nextDouble(65.0, 88.0),
            "vpPulse" to Random.nextDouble(60.0, 90.0),
            "avi" to Random.nextDouble(14.0, 22.0),
        )
        DeviceType.BONE_DENSITY -> mapOf(
            "tScore" to Random.nextDouble(-2.0, 1.0),
            "boneAge" to Random.nextDouble(25.0, 60.0),
        )
        DeviceType.BREATHING -> mapOf(
            "pef" to Random.nextDouble(5.0, 9.0),
            "fev1" to Random.nextDouble(2.5, 4.2),
            "fvc" to Random.nextDouble(3.0, 5.0),
        )
        DeviceType.URINALYSIS -> mapOf(
            "ph" to Random.nextDouble(5.5, 7.5),
            "sg" to Random.nextDouble(1.010, 1.025),
        )
        DeviceType.CAMERA -> emptyMap()
    }
}
