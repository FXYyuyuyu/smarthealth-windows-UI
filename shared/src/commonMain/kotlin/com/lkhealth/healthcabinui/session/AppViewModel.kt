package com.lkhealth.healthcabinui.session

import com.lkhealth.healthcabinui.device.DeviceServiceApi
import com.lkhealth.healthcabinui.device.DeviceServiceSession
import com.lkhealth.healthcabinui.device.DeviceType
import com.lkhealth.healthcabinui.device.MeasurementCatalog
import com.lkhealth.healthcabinui.device.SourceType
import com.lkhealth.healthcabinui.directory.NewUserDraft
import com.lkhealth.healthcabinui.directory.UserDirectoryApi
import com.lkhealth.healthcabinui.directory.UserLookupResult
import com.lkhealth.healthcabinui.directory.UserProfile
import com.lkhealth.healthcabinui.voice.VoiceGuideApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 应用级状态与业务动作的唯一持有者。Composable 只读它暴露的 StateFlow、调用它的方法，
 * 不直接持有导航/业务状态，方便未来把 Mock 服务换成真实的 `:core:device` 实现。
 */
class AppViewModel(
    val deviceService: DeviceServiceApi,
    private val userDirectory: UserDirectoryApi,
) {
    val navigator = Navigator()

    /** 仅用于连接失败/断开这类无需 UI 等待结果的内部异步收尾工作，不承载业务状态。 */
    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val _isGuest = MutableStateFlow(false)
    val isGuest: StateFlow<Boolean> = _isGuest.asStateFlow()

    private val _completedResults = MutableStateFlow<Map<DeviceType, DeviceServiceSession>>(emptyMap())
    val completedResults: StateFlow<Map<DeviceType, DeviceServiceSession>> = _completedResults.asStateFlow()

    /**
     * 项目选择页实际展示、且按此顺序排列的检测项目，由 [com.lkhealth.healthcabinui.config.ItemVisibilityConfig]
     * 在应用启动时读取配置文件解析后写入一次（对应旧系统"改完 ItemConfig.xml 需重启程序生效"的行为，
     * 运行期间不会再变化，因此用普通属性而非 StateFlow 即可）。
     */
    var visibleDeviceTypes: List<DeviceType> = MeasurementCatalog.specs.map { it.deviceType }

    // ---- 系统设置（对应旧系统 SetDeviceUC/SetModeUC 的开关型配置） ----

    private val _printEnabled = MutableStateFlow(true)
    val printEnabled: StateFlow<Boolean> = _printEnabled.asStateFlow()

    private val _faceLoginEnabled = MutableStateFlow(true)
    val faceLoginEnabled: StateFlow<Boolean> = _faceLoginEnabled.asStateFlow()

    private val _guestModeEnabled = MutableStateFlow(true)
    val guestModeEnabled: StateFlow<Boolean> = _guestModeEnabled.asStateFlow()

    private val _voiceEnabled = MutableStateFlow(true)
    val voiceEnabled: StateFlow<Boolean> = _voiceEnabled.asStateFlow()

    fun setPrintEnabled(enabled: Boolean) { _printEnabled.value = enabled }
    fun setFaceLoginEnabled(enabled: Boolean) { _faceLoginEnabled.value = enabled }
    fun setGuestModeEnabled(enabled: Boolean) { _guestModeEnabled.value = enabled }
    fun setVoiceEnabled(enabled: Boolean) {
        _voiceEnabled.value = enabled
        if (!enabled) voiceGuide?.stop()
    }

    // ---- 语音指导（对应旧系统 VoiceHelper） ----

    /** 由 App.kt 在合成根用 rememberVoiceGuide() 拿到平台实现后注入，屏幕本身不关心具体平台实现。 */
    var voiceGuide: VoiceGuideApi? = null

    fun speak(text: String) {
        if (_voiceEnabled.value) voiceGuide?.speak(text)
    }

    // ---- 欢迎页 ----

    fun startManualEntry() = navigator.push(Route.ManualEntry)
    fun startScan() = navigator.push(Route.ScanEntry)
    fun startFaceLogin() = navigator.push(Route.FaceCapture(FaceCaptureMode.LOGIN))

    /** 拿到一个号码后统一进入身份查询过渡态；刷卡、手动输入、扫码、人脸识别成功后都走这里。 */
    fun identifyByUid(uid: String) = navigator.push(Route.IdentityLookup(uid))

    fun startGuest() {
        _isGuest.value = true
        _currentUser.value = null
        _completedResults.value = emptyMap()
        navigator.resetTo(Route.ItemSelection)
    }

    fun goBack() = navigator.pop()

    // ---- 身份查询 / 建档 / 人脸 ----

    suspend fun performLookup(uid: String) {
        when (val result = userDirectory.lookupUser(uid, SourceType.UI)) {
            is UserLookupResult.Found -> enterAsUser(result.profile)
            is UserLookupResult.NeedsFaceEnrollment -> {
                _currentUser.value = result.profile
                navigator.replaceTop(Route.FaceCapture(FaceCaptureMode.ENROLL, result.profile.uid))
            }
            is UserLookupResult.NotFound -> {
                navigator.replaceTop(Route.ProfileOnboarding(result.uid, result.mode))
            }
            is UserLookupResult.Error -> {
                navigator.replaceTop(Route.ErrorScreen(result.message))
            }
        }
    }

    /** 人脸识别登录成功：转入身份查询过渡态（与刷卡/手动输入共用同一条校验路径）。 */
    fun onFaceRecognized(uid: String) = navigator.replaceTop(Route.IdentityLookup(uid))

    suspend fun completeFaceEnrollment() {
        val uid = _currentUser.value?.uid ?: return
        userDirectory.registerFace(uid)
        navigator.resetTo(Route.ItemSelection)
    }

    suspend fun submitOnboarding(draft: NewUserDraft) {
        userDirectory.registerUser(draft)
            .onSuccess { profile -> enterAsUser(profile) }
            .onFailure { navigator.replaceTop(Route.ErrorScreen(it.message ?: "建档失败，请重试")) }
    }

    private fun enterAsUser(profile: UserProfile) {
        _isGuest.value = false
        _currentUser.value = profile
        _completedResults.value = emptyMap()
        navigator.resetTo(Route.ItemSelection)
    }

    // ---- 项目选择 / 设置 ----

    fun selectItem(deviceType: DeviceType) = navigator.push(Route.Prepare(deviceType))
    fun openReport() = navigator.push(Route.ReportSummary)
    fun onAdminUnlocked() = navigator.push(Route.Settings)

    fun backToWelcome() {
        _currentUser.value = null
        _isGuest.value = false
        _completedResults.value = emptyMap()
        connectedDriverIds.keys.toList().forEach(::releaseDevice)
        navigator.resetTo(Route.Welcome)
    }

    /** [deviceType] 非空时表示是从某个检测项目里退出，顺带释放设备连接。 */
    fun backToItemSelection(deviceType: DeviceType? = null) {
        deviceType?.let(::releaseDevice)
        navigator.resetTo(Route.ItemSelection)
    }

    // ---- 设备连接（对应 DEVICE_SERVICE_GUIDE.md 的 listDevices → connect → measure → disconnect） ----

    private val connectedDriverIds = mutableMapOf<DeviceType, String>()

    /** 进入准备页时调用：按 deviceType 找到对应驱动并连接，同一设备在会话内只连接一次。 */
    suspend fun ensureConnected(deviceType: DeviceType): Result<Unit> {
        if (connectedDriverIds.containsKey(deviceType)) return Result.success(Unit)
        val descriptor = deviceService.listDevices().first().firstOrNull { it.deviceType == deviceType }
            ?: return Result.failure(IllegalStateException("未检测到对应设备，请联系工作人员"))
        return deviceService.connect(descriptor.driverId).onSuccess {
            connectedDriverIds[deviceType] = descriptor.driverId
        }
    }

    /** 离开某个检测项目（确认结果/放弃退出）时调用，异步断开、不阻塞导航。 */
    fun releaseDevice(deviceType: DeviceType) {
        val driverId = connectedDriverIds.remove(deviceType) ?: return
        backgroundScope.launch { deviceService.disconnect(driverId) }
    }

    // ---- 测量流程 ----

    fun startMeasuring(deviceType: DeviceType) = navigator.replaceTop(Route.Measuring(deviceType))

    fun onMeasurementCompleted(deviceType: DeviceType, session: DeviceServiceSession) {
        _completedResults.update { it + (deviceType to session) }
        navigator.replaceTop(Route.MeasurementResult(deviceType))
    }

    fun onMeasurementFailed(deviceType: DeviceType, message: String) {
        navigator.replaceTop(Route.MeasurementError(deviceType, message))
    }

    fun retryMeasurement(deviceType: DeviceType) = navigator.replaceTop(Route.Prepare(deviceType))

    /**
     * 确认结果后回项目宫格，由用户自己挑下一项——与旧系统一致。
     * 曾经做过"自动进入下一个未完成项目"，但那不是旧系统的行为：一体机前经常是
     * 只想测血压就走，自动把人推进下一项的准备页反而需要多点一次退出。
     */
    fun confirmResult(deviceType: DeviceType) {
        releaseDevice(deviceType)
        navigator.resetTo(Route.ItemSelection)
    }

    // ---- 报告 / 打印 ----

    fun startPrinting() = navigator.replaceTop(Route.Printing)
    fun onPrintingFinished() = navigator.resetTo(Route.ItemSelection)

    // ---- 空闲超时 ----

    fun idleTimeout() {
        if (navigator.current == Route.Welcome) return
        backToWelcome()
    }
}
