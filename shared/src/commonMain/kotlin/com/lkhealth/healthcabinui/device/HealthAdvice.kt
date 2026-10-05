package com.lkhealth.healthcabinui.device

/**
 * 指标异常时的健康建议文案，对应旧系统 `PrintModel.ItemSuggest`——
 * 文案逐条取自旧系统 `Core/HealthCabin.PrintPro/ResultMethod/ 下各 Result.cs` 里各 Suggest() 方法，
 * 以及随机型配置下发的 `HealthAdvice.xml`（两处内容一致，段落分隔符 `PrintProMain.SuggestField`
 * 原本是 `\r\n`，这里换成 `\n`）。
 *
 * 旧系统有两处行为在这里做了有意调整：
 *
 * 1. 血压建议原本只挂在收缩压上，且用的是一套独立阈值（`sbp > 140` / `dbp > 90`），
 *    与参考范围的上限（139 / 89）差 1，导致收缩压正好 140 时会"判偏高但不给建议"。
 *    这里统一改成由参考范围的判定结果触发，并且收缩压、舒张压任一偏高都给同一段建议
 *    （渲染时按文案去重，不会重复显示两遍）。
 * 2. 旧系统只有 `BMI` 区分"超重 / 肥胖"两级建议，这里保留该分级。
 */
object HealthAdvice {

    /**
     * 取某个字段在当前判定结果下应显示的建议；判定正常、或该指标旧系统本就没写建议时返回 `null`。
     *
     * @param value 该字段的测量值，仅 BMI 需要（要靠它区分超重与肥胖两级建议）。
     */
    fun of(
        deviceType: DeviceType,
        fieldKey: String,
        prompt: ResultPrompt,
        value: Double? = null,
    ): String? {
        if (prompt == ResultPrompt.NORMAL) return null
        val high = prompt == ResultPrompt.HIGH
        return when (deviceType) {
            DeviceType.HEIGHT_WEIGHT -> when (fieldKey) {
                "bmi" -> if (high) bmiHigh(value) else BMI_LOW
                else -> null
            }

            DeviceType.BLOOD_PRESSURE -> when (fieldKey) {
                "systolic", "diastolic" -> if (high) BP_HIGH else BP_LOW
                else -> null
            }

            DeviceType.BODY_FAT -> when (fieldKey) {
                "bodyFatPercent" -> if (high) FAT_HIGH else FAT_LOW
                "bmr" -> if (high) BMR_HIGH else BMR_LOW
                else -> null
            }

            DeviceType.WAIST_HIP -> when (fieldKey) {
                "waistHipRatio" -> if (high) WHR_HIGH else null
                else -> null
            }

            DeviceType.BLOOD_GLUCOSE -> when (fieldKey) {
                "glucose" -> if (high) GLU_HIGH else GLU_LOW
                else -> null
            }

            DeviceType.URIC_ACID -> when (fieldKey) {
                "uricAcid" -> if (high) UA_HIGH else null
                else -> null
            }

            DeviceType.CHOLESTEROL -> when (fieldKey) {
                "chol" -> if (high) CHOL_HIGH else null
                else -> null
            }

            // 旧系统 BLResult 对血脂四项的异常统一给同一段"血脂异常建议"（只在偏高时给）。
            DeviceType.BLOOD_LIPID -> when (fieldKey) {
                "chol" -> if (high) CHOL_HIGH else null
                "tg", "ldl", "hdl" -> if (high) LIPID_ABNORMAL else null
                else -> null
            }

            DeviceType.HEMOGLOBIN -> when (fieldKey) {
                "hb" -> if (high) HB_HIGH else HB_LOW
                else -> null
            }

            else -> null
        }
    }

    // ---- 以下文案均照搬旧系统，未做改写 ----

    private const val BP_HIGH =
        "高血压健康建议：\n" +
            "请近期密切监测您的血压，减轻精神压力，保证睡眠，避免突发性高血压的发生。\n" +
            "饮食：减少食盐的摄入，每天食盐量不超过3克；增加含钾丰富的食物，如芹菜、冬菇、丝瓜、莴笋等。\n" +
            "运动：肥胖兼高血压的人群，应先控制体重，每周坚持至少运动三次以上。\n" +
            "生活方式：三餐定时定量，戒烟限酒，不熬夜，不发怒，保持心情愉悦。"

    private const val BP_LOW =
        "低血压健康建议：\n" +
            "饮食：荤素搭配，多补充优质蛋白、富含铁、叶酸、维生素的食物，如：猪肝、蛋黄、瘦肉、牛奶、鱼虾、豆制品、新鲜果蔬、红糖等可改善因贫血引起的低血压。\n" +
            "运动：适量开展有氧运动如：快走、慢跑等，增强体质。\n" +
            "生活习惯：保证充足的睡眠，保持心情愉悦。戒烟限酒，控制体重，BMI＜23.9kg/m²，减轻精神压力，保持心理平衡。"

    /** 旧系统 HWResult.BMISuggest 的两级：23.9 < BMI < 28 为超重，BMI ≥ 28 为肥胖。 */
    private fun bmiHigh(value: Double?): String =
        if (value != null && value >= 28) BMI_OBESE else BMI_OVERWEIGHT

    private const val BMI_OVERWEIGHT =
        "BMI偏高健康建议：\n" +
            "饮食：控制总热能的摄入，少吃高糖、高脂、冻食品，适量增加蛋白质与粗纤维摄入，如：鱼虾类、鸡肉（去皮）、菌类、豆制品、杂粮等。多补充膳食纤维，促进肠道蠕动帮助排毒。\n" +
            "运动：每周至少运动三次以上，每次坚持30分钟以上。项目为快走、慢跑、游泳等有氧运动。\n" +
            "生活习惯：早餐要保证营养充分、中餐吃饱、晚餐宜少而清淡。"

    private const val BMI_OBESE =
        "BMI偏高健康建议：\n" +
            "饮食：严格控制总能量的摄入，保证能量的消耗大于能量的摄入。不吃高糖（精加工）、高脂、高胆固醇食品，增加粗纤维与维生素的摄入，如：杂粮、番薯、冬瓜、木耳、西柚等。\n" +
            "运动：加强运动，运动可调整身体脂肪分布，减少内脏器官周围脂肪的储藏量。\n" +
            "生活习惯：切勿暴饮暴食，晚上8点后禁止进食（包括水果），主食改吃杂粮饭/粥。多补充膳食纤维，促进肠道蠕动帮助排毒。多补充维生素及矿物质，平衡机体代谢能力。"

    private const val BMI_LOW =
        "BMI偏低健康建议：\n" +
            "长期BMI偏低多有低血压、贫血等症状，并会伴随消瘦、代谢过慢、营养素摄入不足等危害，需要加强营养，多补充优质蛋白及微量元素。\n" +
            "建议：适当增加牛奶、鸡蛋、豆制品、鱼虾贝类等优质蛋白的摄入。食物种类要多样，荤素搭配更有利于全面的营养吸收。运动能够增强心肺功能，增加肌肉量，并提高机体的代谢能力。"

    private const val FAT_HIGH =
        "脂肪含量过高健康建议：\n" +
            "若长期脂肪含量偏高，且脂肪主要堆积在腰腹部，容易增加患心脑血管疾病的风险，如：高血压、糖尿病、冠心病、动脉粥样硬化、心梗、脑梗等。\n" +
            "建议：饮食低糖、少油少盐、多采用凉拌、蒸、煮、炖、焖的烹调方式。增加杂粮、应季新鲜蔬果等富含纤维素的食物摄入。加强锻炼，促进脂肪的燃烧与分解，争取把脂肪含量控制在合理的范围内。"

    private const val FAT_LOW =
        "脂肪含量过低健康建议：\n" +
            "脂肪率低反应了人体能量营养素摄入不充足，应多补充优质蛋白。\n" +
            "建议：适当增加牛奶、鸡蛋、豆制品、肉类、鱼虾贝类等优质蛋白的摄入。少食多餐，避免进餐速度过快，食物种类要多样，荤素搭配更有利于全面的营养吸收。运动能够增强心肺功能，增加肌肉量，并提高机体的代谢能力。"

    private const val BMR_HIGH =
        "基础代谢率偏高建议：\n" +
            "基础代谢偏高除运动员外多见于甲状腺功能亢进者，坚持药物治疗的同时需注意规律作息、规律饮食，增强信心，保持良好的精神状态。"

    private const val BMR_LOW =
        "基础代谢率偏低建议：\n" +
            "基础代谢偏低除手术患者外，常见于营养不良、消瘦、贫血、体脂肪含量多者。要注意加强营养，同时注意适当运动。"

    private const val WHR_HIGH =
        "腰臀比偏高建议：\n" +
            "腰臀比偏高会为心脑血管疾病埋下隐患，腹部肥胖更易增加患冠心病、中风与糖尿病的风险。建议控制总能量摄入，少食多餐，定制运动计划并坚持执行，争取把腰臀比控制在合理范围内。"

    private const val GLU_HIGH =
        "血糖偏高建议：\n" +
            "高血糖是糖尿病的前期，易导致急慢性并发症的发生，如抵抗力下降、肾功能受损、视网膜病变、动脉硬化、肢端坏疽等。\n" +
            "建议：严格控制糖分摄入，选择生糖指数低的食物，如杂豆、荞麦、魔芋、苦瓜、海带、柚子、杨桃等。多补充膳食纤维，膳食纤维可降低血糖升高的速度，避免突然性高血糖。在外就餐时控制饮酒，点菜忌点糖醋类的菜肴，适当补充富含不饱和脂肪酸的食物。每月应监测一次您的血糖。"

    private const val GLU_LOW =
        "血糖偏低建议：\n" +
            "低血糖的症状通常表现为出汗、饥饿、心慌、颤抖、面色苍白等，严重者还可出现精神不集中、躁动、易怒甚至昏迷等现象。\n" +
            "建议：随身常备点糖果、果汁、饼干、牛奶等食物，当低血糖症状发生时及时给机体补充能量。运动则以较为柔和的方式为宜，如瑜伽、太极。"

    private const val UA_HIGH =
        "尿酸偏高建议：\n" +
            "机体对嘌呤的代谢发生紊乱，使血液中的尿酸增多。尿酸偏高是痛风的前期，还易诱发急性关节炎、肾结石及尿酸肾病等危害。\n" +
            "建议：低嘌呤饮食，严格控制老火汤、火锅浓汤、海鲜、肉类、豆制品的摄入。选择高钾低钠的食物如赤豆、丝瓜、芹菜、冬菇、竹笋等可促进尿酸盐的溶解和排泄；每天饮水应达到2000毫升以上，有利于尿酸的溶解，预防尿酸性肾结石的发生；适当补充铁剂及多种维生素。合理运动，改善机体自身对嘌呤的代谢功能。每月应监测一次您的尿酸。"

    private const val CHOL_HIGH =
        "胆固醇偏高建议：\n" +
            "胆固醇偏高往往合并肥胖症，会引发高血脂、高血压、糖尿病、胆总管堵塞、冠心病、动脉粥样硬化等，还易诱发心肌缺血、心梗、脑梗甚至猝死的危害。\n" +
            "建议：避免进食动物内脏、蛋黄、鳗鱼、鱿鱼等高胆固醇食物。多食用富含膳食纤维的食物，如魔芋、芹菜、黑木耳、白菜、韭菜、西兰花等。适量的运动可增加脂质转换，降低坏的胆固醇，升高好的胆固醇，还可调整身体脂肪分布，减少内脏器官周围脂肪的储藏量。适量补充维生素（Vc、Ve）及微量元素（钙、镁、锌、镉）有效降低血脂异常的风险；每月应监测一次您的胆固醇。"

    private const val LIPID_ABNORMAL =
        "血脂异常建议：\n" +
            "您的血脂有可能异常，请进一步与医生确认检测结果。血脂异常是导致动脉粥样硬化的重要因素之一，是冠心病和缺血性脑卒中的独立危险因素。"

    private const val HB_HIGH =
        "血红蛋白偏高建议：\n" +
            "可能是环境因素、劳累过度、脱水、肺炎、甲状腺功能亢进症等导致的，也可能是真性红细胞增多症等血液系统疾病引起的。\n" +
            "清淡饮食：避免高脂肪、高热量食物，如肥肉、炸鸡等，以减少血液黏稠度。\n" +
            "多吃新鲜蔬果：如苹果、西红柿、黄瓜、黑木耳、圆葱、西兰花等，这些食物富含维生素和矿物质，有助于降低胆固醇和甘油三酯，预防血栓形成。\n" +
            "适量饮水：保持体内水分充足，有助于稀释血液，降低血红蛋白浓度。\n" +
            "适当运动：选择适合自己的运动方式，如慢跑、游泳、打羽毛球等，可以促进血液循环。但需注意避免剧烈运动导致的血液浓缩。\n" +
            "充足休息：保证充足的睡眠时间，避免熬夜和过度劳累。"

    private const val HB_LOW =
        "血红蛋白偏低建议：\n" +
            "贫血可由多种原因引起，如缺铁性贫血、巨幼细胞贫血、溶血性贫血、再生障碍性贫血等。\n" +
            "多吃富含铁和蛋白质的食物：如红肉（牛肉、羊肉、猪肉）、豆类和豆制品（大豆、黄豆、扁豆、豆腐、豆浆）、深绿色蔬菜（菠菜、甘蓝、刺苋）、柑橘类水果（橙子、柚子、柠檬）以及鱼类（鲭鱼、鲷鱼、沙丁鱼）等。\n" +
            "避免饮用浓茶和咖啡：这些饮品可能影响铁的吸收，不利于血红蛋白的生成。\n" +
            "保持营养均衡：在饮食中注重荤素搭配，多吃富含维生素的蔬菜和水果，以满足机体的营养需要。\n" +
            "适当运动：适当的运动如慢跑、瑜伽、打太极拳等有助于促进血液循环和红细胞生成。但运动只能起到辅助作用，不能代替药物或其他治疗。"
}
