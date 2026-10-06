package com.lkhealth.healthcabinui

import com.lkhealth.healthcabinui.device.DeviceServiceSession
import com.lkhealth.healthcabinui.device.DeviceType
import com.lkhealth.healthcabinui.device.MeasurementCatalog
import com.lkhealth.healthcabinui.device.RangeContext
import com.lkhealth.healthcabinui.device.ReferenceRange
import com.lkhealth.healthcabinui.device.ReferenceRanges
import com.lkhealth.healthcabinui.device.ResultPrompt
import com.lkhealth.healthcabinui.device.evaluate
import com.lkhealth.healthcabinui.directory.UserProfile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** 参考范围判定，重点盯住从旧系统照搬过来的那几条边界约定。 */
class ReferenceRangeTest {

    private fun profile(sex: String, age: Int) =
        UserProfile(uid = "u", name = "测试", sex = sex, birthday = "", phone = "", age = age)

    @Test
    fun `上限为 0 表示不设上限，再大也不判偏高`() {
        // 高密度脂蛋白：旧系统 BLResult.hdlRange 把 highest 留成 0，只可能判"偏低"。
        val range = ReferenceRange(min = 1.04, max = 0.0)
        assertEquals(ResultPrompt.LOW, range.evaluate(0.8))
        assertEquals(ResultPrompt.NORMAL, range.evaluate(1.04))
        assertEquals(ResultPrompt.NORMAL, range.evaluate(99.0))
    }

    @Test
    fun `测量值为 0 视为未测到而不是偏低`() {
        val range = ReferenceRange(min = 90.0, max = 139.0)
        assertEquals(ResultPrompt.NORMAL, range.evaluate(0.0))
        assertEquals(ResultPrompt.LOW, range.evaluate(0.1))
    }

    @Test
    fun `边界值本身算正常`() {
        val range = ReferenceRange(min = 90.0, max = 139.0)
        assertEquals(ResultPrompt.NORMAL, range.evaluate(90.0))
        assertEquals(ResultPrompt.NORMAL, range.evaluate(139.0))
        assertEquals(ResultPrompt.HIGH, range.evaluate(139.1))
    }

    @Test
    fun `体脂率按性别和年龄分档`() {
        // 旧系统 FATResult.fatRange：男 56 岁以上 20-25，女 18-39 岁 21-25。
        assertEquals(ReferenceRange(20.0, 25.0), ReferenceRanges.of(DeviceType.BODY_FAT, "bodyFatPercent", RangeContext("男", 62)))
        assertEquals(ReferenceRange(21.0, 25.0), ReferenceRanges.of(DeviceType.BODY_FAT, "bodyFatPercent", RangeContext("女", 25)))
        // 不满 18 岁旧系统把上下限都置 0，这里统一表达成"没有参考范围"。
        assertNull(ReferenceRanges.of(DeviceType.BODY_FAT, "bodyFatPercent", RangeContext("男", 10)))
    }

    @Test
    fun `体重参考范围依赖同一次测量的身高`() {
        val withHeight = RangeContext("男", 40, values = mapOf("height" to 172.0))
        assertEquals(ReferenceRange((172 - 105) * 0.9, (172 - 105) * 1.1), ReferenceRanges.of(DeviceType.HEIGHT_WEIGHT, "weight", withHeight))
        // 没测到身高就不判定，而不是拿 0 去算出一个荒唐的范围。
        assertNull(ReferenceRanges.of(DeviceType.HEIGHT_WEIGHT, "weight", RangeContext("男", 40)))
    }

    @Test
    fun `旧系统没给范围的项目保持不判定`() {
        listOf(DeviceType.ECG to "heartRate", DeviceType.VISION to "visionLeft", DeviceType.BONE_DENSITY to "tScore", DeviceType.BREATHING to "pef")
            .forEach { (type, key) ->
                assertNull(ReferenceRanges.of(type, key, RangeContext("男", 40)), "$type.$key 不应有参考范围")
            }
    }

    @Test
    fun `游客填了性别年龄后分档项目才有判定`() {
        val spec = MeasurementCatalog.of(DeviceType.BODY_FAT)
        val session = DeviceServiceSession("s", DeviceType.BODY_FAT, mapOf("bodyFatPercent" to 28.4, "bmr" to 1100.0))

        // 跳过填写：只显示数值，不给判定。
        spec.evaluate(session, profile = null).forEach {
            assertNull(it.range, "${it.label} 在没有档案时不应有参考范围")
            assertEquals(ResultPrompt.NORMAL, it.prompt)
        }

        // 填了男 / 56-69 岁（代表年龄 62）：体脂率 28.4 超过上限 25 判偏高，
        // 基础代谢 1100 低于这一档的下限 1215 判偏低（换成 36 岁的话这一档是 1350-1650，结论会不同——
        // 这正是为什么游客也得先填性别年龄）。
        val evaluated = spec.evaluate(session, profile("男", 62)).associateBy { it.definition.key }
        assertEquals(ResultPrompt.HIGH, evaluated.getValue("bodyFatPercent").prompt)
        assertEquals(ResultPrompt.LOW, evaluated.getValue("bmr").prompt)
    }

    @Test
    fun `血压收缩压舒张压共用一段建议且去重`() {
        val spec = MeasurementCatalog.of(DeviceType.BLOOD_PRESSURE)
        val session = DeviceServiceSession("s", DeviceType.BLOOD_PRESSURE, mapOf("systolic" to 152.0, "diastolic" to 95.0, "pulse" to 72.0))
        val evaluated = spec.evaluate(session, profile("男", 36))

        assertEquals(2, evaluated.count { it.isAbnormal })
        assertEquals(1, evaluated.mapNotNull { it.advice }.distinct().size, "收缩压和舒张压的高血压建议应合并成一条")
    }
}
