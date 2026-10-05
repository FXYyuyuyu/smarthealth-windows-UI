package com.lkhealth.healthcabinui

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform