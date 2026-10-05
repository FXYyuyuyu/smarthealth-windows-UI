package com.lkhealth.healthcabinui.device

/**
 * 检测项目类型。对齐 DEVICE_SERVICE_GUIDE.md 的 DeviceType 对照表；
 * 指南未列出 ECG，但旧系统与本项目 MVP 范围包含心电，故在此补充。
 * [configCode] 对应旧系统 `ItemConfig.xml` 里的项目代码（HW/BP/BO...），
 * 用于 [ItemVisibilityConfig] 读取配置文件时按代码匹配设备，方便直接沿用旧系统的命名习惯。
 */
enum class DeviceType(val configCode: String) {
    HEIGHT_WEIGHT("HW"),
    BLOOD_PRESSURE("BP"),
    BLOOD_OXYGEN("BO"),
    BLOOD_GLUCOSE("GLU"),
    BODY_FAT("FAT"),
    TEMPERATURE("TT"),
    WAIST_HIP("WHR"),
    ECG("ECG"),
    URIC_ACID("UA"),
    CHOLESTEROL("CHOL"),
    BLOOD_LIPID("BL"),
    HEMOGLOBIN("HB"),
    HBA1C("NGSP"),
    VISION("VIS"),
    ARTERIOSCLEROSIS("BPVP"),
    BONE_DENSITY("BD"),
    BREATHING("BRE"),
    URINALYSIS("BC"),
    CAMERA("CAM"),
}

enum class DeviceStatus {
    CONNECTED,
    DISCONNECTED,
    BUSY,
    ERROR,
}

enum class SessionStatus {
    PREPARING,
    CONNECTING,
    IN_PROGRESS,
    RECEIVING,
    COMPLETED,
    FAILED,
    CANCELLED,
    ERROR,
}

/** 发起测量/建档等操作的来源，与指南 SourceType 对齐。 */
enum class SourceType {
    UI,
    MCP,
    MOBILE,
    SYSTEM,
}

data class DeviceDescriptor(
    val driverId: String,
    val displayName: String,
    val deviceType: DeviceType,
    val transportType: String,
    val status: DeviceStatus,
)

data class DeviceError(
    val code: String,
    val message: String,
)

data class SessionProgress(
    val sessionId: String,
    val deviceType: DeviceType,
    val status: SessionStatus,
    val percentage: Int,
    val currentStep: String? = null,
    val error: DeviceError? = null,
    /** 分段测量（如腰臀比先腰围后臀围）时，当前所处的段下标，从 0 开始。 */
    val partIndex: Int = 0,
)

data class DeviceServiceSession(
    val sessionId: String,
    val deviceType: DeviceType,
    val values: Map<String, Double>,
)
