package com.lkhealth.healthcabinui.directory

import com.lkhealth.healthcabinui.device.SourceType
import kotlinx.coroutines.delay

class MockUserDirectoryApi : UserDirectoryApi {

    private val users = mutableMapOf(
        "110101199001011234" to UserProfile(
            uid = "110101199001011234",
            name = "张三",
            sex = "男",
            birthday = "1990-01-01",
            phone = "13800001111",
            age = 36,
            hasFaceOnFile = true,
        ),
        "13900001234" to UserProfile(
            uid = "13900001234",
            name = "李四",
            sex = "女",
            birthday = "1995-05-20",
            phone = "13900001234",
            age = 31,
            hasFaceOnFile = false,
        ),
    )

    override suspend fun lookupUser(uid: String, source: SourceType): UserLookupResult {
        delay(700)
        val mode = if (uid.length == 18) IdentityMode.ID_CARD else IdentityMode.PHONE
        val profile = users[uid] ?: return UserLookupResult.NotFound(uid, mode)
        return if (profile.hasFaceOnFile) {
            UserLookupResult.Found(profile)
        } else {
            UserLookupResult.NeedsFaceEnrollment(profile)
        }
    }

    override suspend fun registerUser(draft: NewUserDraft): Result<UserProfile> {
        delay(600)
        val profile = UserProfile(
            uid = draft.uid,
            name = draft.name,
            sex = draft.sex.ifBlank { "未知" },
            birthday = draft.birthday,
            phone = draft.phone.ifBlank { draft.uid },
            age = estimateAge(draft.birthday),
            hasFaceOnFile = false,
        )
        users[draft.uid] = profile
        return Result.success(profile)
    }

    override suspend fun registerFace(uid: String): Result<Unit> {
        delay(300)
        users[uid]?.let { users[uid] = it.copy(hasFaceOnFile = true) }
        return Result.success(Unit)
    }

    private fun estimateAge(birthday: String): Int {
        val year = birthday.take(4).toIntOrNull() ?: return 0
        return (2026 - year).coerceAtLeast(0)
    }
}
