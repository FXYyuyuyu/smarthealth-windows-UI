package com.lkhealth.healthcabinui.directory

import com.lkhealth.healthcabinui.device.SourceType

/** 复刻《自动建档流程设计文档》中 getPersonInfo 的四种返回分支。 */
sealed interface UserLookupResult {
    data class Found(val profile: UserProfile) : UserLookupResult
    data class NeedsFaceEnrollment(val profile: UserProfile) : UserLookupResult
    data class NotFound(val uid: String, val mode: IdentityMode) : UserLookupResult
    data class Error(val message: String) : UserLookupResult
}

/**
 * 用户身份查询/建档服务契约。指南未覆盖此部分（用户数据由底层另一套服务提供），
 * 这里按旧系统《自动建档流程设计文档》的语义定义接口，先用 [MockUserDirectoryApi] 顶替。
 */
interface UserDirectoryApi {
    suspend fun lookupUser(uid: String, source: SourceType): UserLookupResult
    suspend fun registerUser(draft: NewUserDraft): Result<UserProfile>
    suspend fun registerFace(uid: String): Result<Unit>
}
