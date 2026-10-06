package com.lkhealth.healthcabinui.ui.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lkhealth.healthcabinui.device.MeasurementCatalog
import com.lkhealth.healthcabinui.session.AppViewModel
import com.lkhealth.healthcabinui.ui.components.GridTile
import com.lkhealth.healthcabinui.ui.theme.Spacing
import healthcabinui.shared.generated.resources.Res
import healthcabinui.shared.generated.resources.icon_exit
import healthcabinui.shared.generated.resources.icon_view_result
import org.jetbrains.compose.resources.DrawableResource

private data class TileEntry(
    val iconRes: DrawableResource,
    val label: String,
    val enabled: Boolean = true,
    val completed: Boolean = false,
    val onClick: () -> Unit,
)

/**
 * 对应旧系统 SelectControl_TestCanal：检测项目宫格。显示哪些项目、按什么顺序由配置文件决定（见 ItemVisibilityConfig），
 * 末尾固定跟"查看结果""退出"两个功能方块（对应旧系统 ItemConfig 里的 Print/OUT）。
 * 与旧系统一样力求一屏放下：按可用区域自动选列数和方块尺寸，只有项目多到放不下时才允许滚动。
 */
@Composable
fun ItemSelectionScreen(viewModel: AppViewModel) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isGuest by viewModel.isGuest.collectAsStateWithLifecycle()
    val completedResults by viewModel.completedResults.collectAsStateWithLifecycle()
    val visibleSpecs = remember(viewModel.visibleDeviceTypes) {
        viewModel.visibleDeviceTypes.map(MeasurementCatalog::of)
    }

    LaunchedEffect(Unit) {
        val greetName = if (isGuest) "您" else currentUser?.name ?: "您"
        viewModel.speak("欢迎$greetName，请选择需要进行的检测项目")
    }

    val tiles = visibleSpecs.map { spec ->
        TileEntry(
            iconRes = spec.iconRes,
            label = spec.gridLabel,
            completed = spec.deviceType in completedResults.keys,
            onClick = { viewModel.selectItem(spec.deviceType) },
        )
    } + listOf(
        TileEntry(Res.drawable.icon_view_result, "查看结果", enabled = completedResults.isNotEmpty(), onClick = viewModel::openReport),
        TileEntry(Res.drawable.icon_exit, "退出", onClick = viewModel::backToWelcome),
    )

    BoxWithConstraints(Modifier.fillMaxSize().padding(Spacing.l)) {
        val layout = remember(maxWidth, maxHeight, tiles.size) { computeGridLayout(tiles.size, maxWidth, maxHeight) }
        val labelSize = with(LocalDensity.current) {
            // 最长项目名约 8 个字（如"糖化血红蛋白测量"），按方块宽度反推字号，保证单行完整显示。
            val fitted = ((layout.tileWidth - Spacing.s) / 9).toSp()
            if (fitted.value < 26f) fitted else 26.sp
        }
        val gridModifier = if (layout.fits) Modifier.fillMaxSize() else Modifier.fillMaxSize().verticalScroll(rememberScrollState())

        Box(gridModifier, contentAlignment = Alignment.Center) {
            Column(verticalArrangement = Arrangement.spacedBy(GridGap), horizontalAlignment = Alignment.CenterHorizontally) {
                tiles.chunked(layout.columns).forEach { rowTiles ->
                    Row(horizontalArrangement = Arrangement.spacedBy(GridGap)) {
                        rowTiles.forEach { tile ->
                            GridTile(
                                iconRes = tile.iconRes,
                                label = tile.label,
                                iconSize = layout.iconSize,
                                labelSize = labelSize,
                                enabled = tile.enabled,
                                completed = tile.completed,
                                onClick = tile.onClick,
                                modifier = Modifier.size(layout.tileWidth, layout.tileHeight),
                            )
                        }
                    }
                }
            }
        }
    }
}

private val GridGap = 24.dp
private val MinTileSide = 120.dp

/**
 * 方块尺寸上限。项目少的时候方块会自动撑到这个上限为止——旧系统只配 9 个项目时图标明显更大，
 * 就是这个效果。上限不设成无穷大是因为：图标素材本身只有几百像素，拉过头会糊，
 * 而且一行只剩三四个巨大方块时，视线要横跨整屏才能扫完，反而比适中尺寸更难找。
 */
private val MaxTileWidth = 300.dp
private val MaxTileHeight = 300.dp

/**
 * 图标上限卡在素材自身的分辨率上：旧系统这套图标 PNG 只有 175×167 像素，
 * 再往上拉就是放大糊（安卓一体机 density ≥ 2 时更明显）。想要更大的图标，
 * 得先从原始设计稿重新导一套 2x/3x 的素材，不是改这里的数字能解决的。
 */
private val MaxIconSide = 176.dp

/** 方块内图标以外的固定占用：图标与文字间距 + 一行文字。去掉白卡容器后不再有卡片内边距。 */
private val TileChrome = 44.dp

private data class GridLayout(
    val columns: Int,
    val tileWidth: Dp,
    val tileHeight: Dp,
    val iconSize: Dp,
    val fits: Boolean,
)

/** 在 3..10 列里选出能一屏放下、且图标最大的列数；都放不下时退回按最小尺寸排布并允许滚动。 */
private fun computeGridLayout(count: Int, width: Dp, height: Dp): GridLayout {
    var best: GridLayout? = null
    for (columns in 3..10) {
        val rows = (count + columns - 1) / columns
        val w = (width - GridGap * (columns - 1)) / columns
        val h = (height - GridGap * (rows - 1)) / rows
        if (w < MinTileSide || h < MinTileSide) continue
        val tileWidth = minOf(w, MaxTileWidth)
        val tileHeight = minOf(h, MaxTileHeight, tileWidth * 1.15f)
        // 去掉白卡容器后图标可以占到方块宽度的 0.85（原来留给卡片内边距的部分省出来了）。
        val icon = minOf(tileWidth * 0.85f, tileHeight - TileChrome).coerceIn(48.dp, MaxIconSide)
        if (best == null || icon > best.iconSize) best = GridLayout(columns, tileWidth, tileHeight, icon, fits = true)
    }
    best?.let { return it }

    val columns = ((width + GridGap) / (MinTileSide + GridGap)).toInt().coerceAtLeast(1)
    val tileWidth = minOf((width - GridGap * (columns - 1)) / columns, MaxTileWidth)
    val tileHeight = minOf(tileWidth * 1.15f, MaxTileHeight)
    return GridLayout(columns, tileWidth, tileHeight, (tileHeight - TileChrome).coerceIn(48.dp, MaxIconSide), fits = false)
}
