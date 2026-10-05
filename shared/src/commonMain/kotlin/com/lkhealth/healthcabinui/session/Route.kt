package com.lkhealth.healthcabinui.session

import com.lkhealth.healthcabinui.device.DeviceType
import com.lkhealth.healthcabinui.directory.IdentityMode

enum class FaceCaptureMode { LOGIN, ENROLL }

/** 应用内可导航的全部页面。旧系统没有系统级返回栈，各屏幕显式导航——这里的 [Navigator] 沿用同一思路。 */
sealed class Route {
    data object Welcome : Route()
    data object ManualEntry : Route()
    data object ScanEntry : Route()
    data class FaceCapture(val mode: FaceCaptureMode, val uid: String? = null) : Route()
    data class IdentityLookup(val uid: String) : Route()
    data class ProfileOnboarding(val uid: String, val mode: IdentityMode) : Route()
    data object ItemSelection : Route()
    data class Prepare(val deviceType: DeviceType) : Route()
    data class Measuring(val deviceType: DeviceType) : Route()
    data class MeasurementResult(val deviceType: DeviceType) : Route()
    data class MeasurementError(val deviceType: DeviceType, val message: String) : Route()
    data object ReportSummary : Route()
    data object Printing : Route()
    data object Settings : Route()
    data class ErrorScreen(val message: String) : Route()
}
