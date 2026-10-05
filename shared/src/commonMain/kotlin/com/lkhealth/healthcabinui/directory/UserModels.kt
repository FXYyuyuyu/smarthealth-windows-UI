package com.lkhealth.healthcabinui.directory

/** 欢迎页可用的入场方式，对应旧系统 WelcomeUC 的图标集合。 */
enum class LoginMethod(val label: String) {
    IdCardSwipe("刷身份证"),
    ManualEntry("手动输入"),
    Face("人脸登录"),
    Scan("微信扫码"),
    Guest("游客体验"),
}

enum class IdentityMode { PHONE, ID_CARD }

data class UserProfile(
    val uid: String,
    val name: String,
    val sex: String,
    val birthday: String,
    val phone: String,
    val age: Int,
    val hasFaceOnFile: Boolean = true,
)

/** 建档表单的可编辑草稿；phone 模式需补姓名+生日，身份证模式需补手机号+姓名。 */
data class NewUserDraft(
    val uid: String,
    val mode: IdentityMode,
    val name: String = "",
    val phone: String = "",
    val sex: String = "",
    val birthday: String = "",
)
