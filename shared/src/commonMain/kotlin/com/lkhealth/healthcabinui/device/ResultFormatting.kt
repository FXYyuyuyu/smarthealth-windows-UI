package com.lkhealth.healthcabinui.device

import kotlin.math.roundToInt

/** 按字段配置的小数位数格式化数值；未取到值时显示占位符。 */
fun ResultField.format(session: DeviceServiceSession?): String {
    val raw = session?.values?.get(key) ?: return "--"
    return if (decimals <= 0) raw.roundToInt().toString() else "%.${decimals}f".format(raw)
}
