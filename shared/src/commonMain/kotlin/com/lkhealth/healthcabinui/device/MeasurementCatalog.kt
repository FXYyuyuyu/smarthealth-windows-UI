package com.lkhealth.healthcabinui.device

import androidx.compose.ui.graphics.Color
import com.lkhealth.healthcabinui.ui.theme.HealthCabinColors
import healthcabinui.shared.generated.resources.Res
import healthcabinui.shared.generated.resources.guide_blood_oxygen
import healthcabinui.shared.generated.resources.guide_blood_pressure
import healthcabinui.shared.generated.resources.guide_body_fat
import healthcabinui.shared.generated.resources.guide_ecg
import healthcabinui.shared.generated.resources.guide_height_weight
import healthcabinui.shared.generated.resources.guide_waist_hip
import healthcabinui.shared.generated.resources.icon_blood_glucose
import healthcabinui.shared.generated.resources.icon_blood_oxygen
import healthcabinui.shared.generated.resources.icon_blood_pressure
import healthcabinui.shared.generated.resources.icon_body_fat
import healthcabinui.shared.generated.resources.icon_bone_density
import healthcabinui.shared.generated.resources.icon_breathing
import healthcabinui.shared.generated.resources.icon_ecg
import healthcabinui.shared.generated.resources.icon_height_weight
import healthcabinui.shared.generated.resources.icon_temperature
import healthcabinui.shared.generated.resources.icon_urinalysis
import healthcabinui.shared.generated.resources.icon_vision
import healthcabinui.shared.generated.resources.icon_waist_hip
import org.jetbrains.compose.resources.DrawableResource

/** 报告汇总页的分类分组，对应旧系统 PrintUC 的"基础体征/生化检测/心电结果"等分区。 */
enum class ReportCategory(val label: String) {
    BASIC_VITALS("基础体征"),
    BODY_COMPOSITION("体成分"),
    BIOCHEMICAL("生化检测"),
    CARDIAC("心电结果"),
}

data class ResultField(
    val key: String,
    val label: String,
    val unit: String,
    val decimals: Int = 1,
)

/** 测量进度的阶段性文案，按 percentage 上界排序；用于血压等有明确阶段感知的设备。 */
data class ProgressPhase(
    val untilPercent: Int,
    val label: String,
)

/** 分段测量的一段，例如腰臀比先测腰围再测臀围。 */
data class MeasurementPart(
    val key: String,
    val label: String,
)

data class MeasurementSpec(
    val deviceType: DeviceType,
    val title: String,
    val subtitle: String,
    /** 项目宫格 / 准备页用的图标，直接沿用旧系统同款素材（各设备 Control 项目 img 目录）。 */
    val iconRes: DrawableResource,
    /** 旧系统"XX测量图示"整屏指导图（标题+示意图+文案已经画在图里），未提供时准备页退回纯文字说明。 */
    val guideRes: DrawableResource? = null,
    /**
     * 指导图裁切用的宽高比：原图底部留了一大截给旧系统叠加按钮的空白画布，
     * 各图文字内容多少不一样，空白比例也不一样，所以每张图单独量过"文字最后一行"的像素位置
     * （留了 ~5% 安全余量），换算出只截取有效内容区域、不切到文字的宽高比。为空则整图完整显示。
     */
    val guideAspectRatio: Float? = null,
    val accent: Color,
    val category: ReportCategory,
    val prepareInstruction: String,
    val resultFields: List<ResultField>,
    val progressPhases: List<ProgressPhase> = emptyList(),
    val parts: List<MeasurementPart> = listOf(MeasurementPart("main", "测量")),
) {
    val isMultiPart: Boolean get() = parts.size > 1

    /** 项目宫格上显示的完整标签，对应旧系统"XX测量"的命名习惯（如"血压测量"）。 */
    val gridLabel: String get() = "${title}测量"
}

object MeasurementCatalog {

    val specs: List<MeasurementSpec> = listOf(
        MeasurementSpec(
            deviceType = DeviceType.HEIGHT_WEIGHT,
            title = "身高体重",
            subtitle = "身高、体重、BMI",
            iconRes = Res.drawable.icon_height_weight,
            guideRes = Res.drawable.guide_height_weight,
            guideAspectRatio = 1.99f,
            accent = HealthCabinColors.ItemAccent.HeightWeight,
            category = ReportCategory.BASIC_VITALS,
            prepareInstruction = "请脱鞋，并挺胸站直，点击\"开始测量\"按钮进行测量！",
            resultFields = listOf(
                ResultField("height", "身高", "cm", 1),
                ResultField("weight", "体重", "kg", 1),
                ResultField("bmi", "BMI", "", 1),
            ),
        ),
        MeasurementSpec(
            deviceType = DeviceType.BLOOD_PRESSURE,
            title = "血压",
            subtitle = "收缩压、舒张压、脉搏",
            iconRes = Res.drawable.icon_blood_pressure,
            guideRes = Res.drawable.guide_blood_pressure,
            guideAspectRatio = 1.94f,
            accent = HealthCabinColors.ItemAccent.BloodPressure,
            category = ReportCategory.BASIC_VITALS,
            prepareInstruction = "点击操作界面上\"开始测量\"按钮开始测量，测量过程请保持自然姿势，身体不要移动。",
            resultFields = listOf(
                ResultField("systolic", "收缩压", "mmHg", 0),
                ResultField("diastolic", "舒张压", "mmHg", 0),
                ResultField("pulse", "脉搏", "bpm", 0),
            ),
            progressPhases = listOf(
                ProgressPhase(20, "正在充气加压..."),
                ProgressPhase(60, "正在测量中，请保持安静..."),
                ProgressPhase(90, "正在放气..."),
                ProgressPhase(100, "测量完成"),
            ),
        ),
        MeasurementSpec(
            deviceType = DeviceType.BLOOD_OXYGEN,
            title = "血氧",
            subtitle = "血氧饱和度、脉率",
            iconRes = Res.drawable.icon_blood_oxygen,
            guideRes = Res.drawable.guide_blood_oxygen,
            guideAspectRatio = 2.01f,
            accent = HealthCabinColors.ItemAccent.BloodOxygen,
            category = ReportCategory.BASIC_VITALS,
            prepareInstruction = "将血氧仪夹住右手食指，姿势保持不动，点击\"开始测量\"按钮进行测量!",
            resultFields = listOf(
                ResultField("spo2", "血氧饱和度", "%", 0),
                ResultField("pulse", "脉率", "bpm", 0),
            ),
        ),
        MeasurementSpec(
            deviceType = DeviceType.BLOOD_GLUCOSE,
            title = "血糖",
            subtitle = "空腹或餐后血糖",
            iconRes = Res.drawable.icon_blood_glucose,
            accent = HealthCabinColors.ItemAccent.BloodGlucose,
            category = ReportCategory.BIOCHEMICAL,
            prepareInstruction = "请将试纸插入采血针取血后，按提示放入检测仪。",
            resultFields = listOf(
                ResultField("glucose", "血糖", "mmol/L", 1),
            ),
        ),
        MeasurementSpec(
            deviceType = DeviceType.BODY_FAT,
            title = "人体成分",
            subtitle = "体脂率、基础代谢",
            iconRes = Res.drawable.icon_body_fat,
            guideRes = Res.drawable.guide_body_fat,
            guideAspectRatio = 1.89f,
            accent = HealthCabinColors.ItemAccent.BodyFat,
            category = ReportCategory.BODY_COMPOSITION,
            prepareInstruction = "请双手紧握住脂肪仪的金属部分，背部挺直，手臂伸直与身体保持90°，点击\"开始测量\"按钮进行测量！",
            resultFields = listOf(
                ResultField("bodyFatPercent", "体脂率", "%", 1),
                ResultField("bmr", "基础代谢", "kcal", 0),
            ),
        ),
        MeasurementSpec(
            deviceType = DeviceType.TEMPERATURE,
            title = "体温",
            subtitle = "体温测量",
            iconRes = Res.drawable.icon_temperature,
            accent = HealthCabinColors.ItemAccent.Temperature,
            category = ReportCategory.BASIC_VITALS,
            prepareInstruction = "请将额温枪对准前额中央，距离约 3-5 厘米。",
            resultFields = listOf(
                ResultField("temperature", "体温", "°C", 1),
            ),
        ),
        MeasurementSpec(
            deviceType = DeviceType.WAIST_HIP,
            title = "腰臀比",
            subtitle = "腰围、臀围、比值",
            iconRes = Res.drawable.icon_waist_hip,
            guideRes = Res.drawable.guide_waist_hip,
            guideAspectRatio = 1.53f,
            accent = HealthCabinColors.ItemAccent.WaistHip,
            category = ReportCategory.BODY_COMPOSITION,
            prepareInstruction = "请将卷尺环绕对应部位，保持水平贴合皮肤。",
            resultFields = listOf(
                ResultField("waist", "腰围", "cm", 1),
                ResultField("hip", "臀围", "cm", 1),
                ResultField("waistHipRatio", "腰臀比", "", 2),
            ),
            parts = listOf(
                MeasurementPart("waist", "腰围"),
                MeasurementPart("hip", "臀围"),
            ),
        ),
        MeasurementSpec(
            deviceType = DeviceType.ECG,
            title = "心电",
            subtitle = "实时波形与报告",
            iconRes = Res.drawable.icon_ecg,
            guideRes = Res.drawable.guide_ecg,
            guideAspectRatio = 1.80f,
            accent = HealthCabinColors.ItemAccent.Ecg,
            category = ReportCategory.CARDIAC,
            prepareInstruction = "请平躺放松，按提示依次贴好导联电极片。",
            resultFields = listOf(
                ResultField("heartRate", "心率", "bpm", 0),
                ResultField("prInterval", "PR间期", "ms", 0),
                ResultField("qrsDuration", "QRS时限", "ms", 0),
                ResultField("qtInterval", "QT间期", "ms", 0),
            ),
        ),
        // 以下为旧系统 ItemConfig.xml 里配置过、但当前 8 类之外的检测项目；
        // 图标直接复用各自 LogoUC 指向的旧系统素材（血糖类几项在旧系统里本就共用同一个 GLUBtn 图标，
        // 并非本项目简化——原样保留这份"共用图标、靠文字区分"的设计）。
        MeasurementSpec(
            deviceType = DeviceType.URIC_ACID,
            title = "尿酸",
            subtitle = "尿酸浓度",
            iconRes = Res.drawable.icon_blood_glucose,
            accent = HealthCabinColors.ItemAccent.UricAcid,
            category = ReportCategory.BIOCHEMICAL,
            prepareInstruction = "请将试纸插入采血针取血后，按提示放入检测仪。",
            resultFields = listOf(
                ResultField("uricAcid", "尿酸", "mmol/L", 2),
            ),
        ),
        MeasurementSpec(
            deviceType = DeviceType.CHOLESTEROL,
            title = "胆固醇",
            subtitle = "总胆固醇",
            iconRes = Res.drawable.icon_blood_glucose,
            accent = HealthCabinColors.ItemAccent.Cholesterol,
            category = ReportCategory.BIOCHEMICAL,
            prepareInstruction = "请将试纸插入检测仪，等待胆固醇检测结果。",
            resultFields = listOf(
                ResultField("chol", "总胆固醇", "mmol/L", 1),
            ),
        ),
        MeasurementSpec(
            deviceType = DeviceType.BLOOD_LIPID,
            title = "血脂",
            subtitle = "总胆固醇、甘油三酯、高低密度脂蛋白",
            iconRes = Res.drawable.icon_blood_glucose,
            accent = HealthCabinColors.ItemAccent.BloodLipid,
            category = ReportCategory.BIOCHEMICAL,
            prepareInstruction = "请将血样放入血脂检测仪，等待读取结果。",
            resultFields = listOf(
                ResultField("chol", "总胆固醇", "mmol/L", 1),
                ResultField("tg", "甘油三酯", "mmol/L", 1),
                ResultField("hdl", "高密度脂蛋白", "mmol/L", 1),
                ResultField("ldl", "低密度脂蛋白", "mmol/L", 1),
            ),
        ),
        MeasurementSpec(
            deviceType = DeviceType.HEMOGLOBIN,
            title = "血红蛋白",
            subtitle = "血红蛋白浓度",
            iconRes = Res.drawable.icon_blood_glucose,
            accent = HealthCabinColors.ItemAccent.Hemoglobin,
            category = ReportCategory.BIOCHEMICAL,
            prepareInstruction = "请将试纸插入采血针取血后，按提示放入检测仪。",
            resultFields = listOf(
                ResultField("hb", "血红蛋白", "g/dL", 1),
            ),
        ),
        MeasurementSpec(
            deviceType = DeviceType.HBA1C,
            title = "糖化血红蛋白",
            subtitle = "近三个月平均血糖水平",
            iconRes = Res.drawable.icon_blood_glucose,
            accent = HealthCabinColors.ItemAccent.Hba1c,
            category = ReportCategory.BIOCHEMICAL,
            prepareInstruction = "请将血样放入糖化血红蛋白检测仪，等待读取结果。",
            resultFields = listOf(
                ResultField("ngsp", "糖化血红蛋白", "%", 1),
            ),
        ),
        MeasurementSpec(
            deviceType = DeviceType.VISION,
            title = "视力",
            subtitle = "左眼、右眼视力",
            iconRes = Res.drawable.icon_vision,
            accent = HealthCabinColors.ItemAccent.Vision,
            category = ReportCategory.BASIC_VITALS,
            prepareInstruction = "请按提示依次遮挡单眼，注视视力表进行检测。",
            resultFields = listOf(
                ResultField("visionLeft", "左眼视力", "", 1),
                ResultField("visionRight", "右眼视力", "", 1),
            ),
        ),
        MeasurementSpec(
            deviceType = DeviceType.ARTERIOSCLEROSIS,
            title = "动脉硬化",
            subtitle = "血压、脉搏、动脉硬化指数",
            iconRes = Res.drawable.icon_blood_pressure,
            accent = HealthCabinColors.ItemAccent.Arteriosclerosis,
            category = ReportCategory.BASIC_VITALS,
            prepareInstruction = "请保持自然坐姿，将臂带绑好，点击\"开始测量\"按钮进行测量，测量中请勿移动。",
            resultFields = listOf(
                ResultField("vpSystolic", "收缩压", "mmHg", 0),
                ResultField("vpDiastolic", "舒张压", "mmHg", 0),
                ResultField("vpPulse", "脉搏", "bpm", 0),
                ResultField("avi", "动脉速度指数", "", 1),
            ),
        ),
        MeasurementSpec(
            deviceType = DeviceType.BONE_DENSITY,
            title = "骨密度",
            subtitle = "骨密度T值、骨龄",
            iconRes = Res.drawable.icon_bone_density,
            accent = HealthCabinColors.ItemAccent.BoneDensity,
            category = ReportCategory.BODY_COMPOSITION,
            prepareInstruction = "请将脚跟置于骨密度检测仪测量区域，保持不动。",
            resultFields = listOf(
                ResultField("tScore", "T值", "", 1),
                ResultField("boneAge", "骨龄", "岁", 0),
            ),
        ),
        MeasurementSpec(
            deviceType = DeviceType.BREATHING,
            title = "肺功能",
            subtitle = "峰值呼气流量、用力肺活量",
            iconRes = Res.drawable.icon_breathing,
            accent = HealthCabinColors.ItemAccent.Breathing,
            category = ReportCategory.BASIC_VITALS,
            prepareInstruction = "请深吸一口气，对准吹嘴用力快速呼出，直至气体呼尽。",
            resultFields = listOf(
                ResultField("pef", "峰值呼气流量", "L/s", 1),
                ResultField("fev1", "用力呼气容积", "L", 1),
                ResultField("fvc", "用力肺活量", "L", 1),
            ),
        ),
        MeasurementSpec(
            deviceType = DeviceType.URINALYSIS,
            title = "尿常规",
            subtitle = "pH值、尿比重等指标",
            iconRes = Res.drawable.icon_urinalysis,
            accent = HealthCabinColors.ItemAccent.Urinalysis,
            category = ReportCategory.BIOCHEMICAL,
            prepareInstruction = "请将尿液试纸插入检测仪进行检测。",
            resultFields = listOf(
                ResultField("ph", "pH值", "", 1),
                ResultField("sg", "尿比重", "", 3),
            ),
        ),
    )

    private val byType = specs.associateBy { it.deviceType }

    fun of(deviceType: DeviceType): MeasurementSpec =
        byType[deviceType] ?: error("No MeasurementSpec registered for $deviceType")
}
