package com.lkhealth.healthcabinui.session

import com.lkhealth.healthcabinui.device.DeviceType
import com.lkhealth.healthcabinui.directory.IdentityMode

enum class FaceCaptureMode { LOGIN, ENROLL }

/** 应用内可导航的全部页面。旧系统没有系统级返回栈，各屏幕显式导航——这里的 [Navigator] 沿用同一思路。 */
sealed class Route {
    data object Welcome : Route()
    data object ManualEntry : Route()
    data object ScanEntry : Route()

    /**
     * 游客模式下补填性别/年龄段。体脂率、基础代谢、尿酸、血红蛋白、腰臀比的参考范围都分性别/年龄档，
     * 没有这两项就只能显示数值、给不出"偏高/偏低"。可以跳过，跳过后这些项目不作判定。
     */
    data object GuestProfile : Route()
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
