package com.lugkit.stellarextraction.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lugkit.stellarextraction.GameViewModel
import com.lugkit.stellarextraction.Resource
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

private enum class Screen { MAIN, SHOP, TREE }

@Composable
fun GameScreen(vm: GameViewModel) {
    val state by vm.state.collectAsState()
    var screen by remember { mutableStateOf(Screen.MAIN) }

    when (screen) {
        Screen.SHOP -> ShopScreen(
            state               = state,
            onBuyDrillHead      = vm::buyDrillHead,
            onBuyPowerCore      = vm::buyPowerCore,
            onBuyDeepShaft      = vm::buyDeepShaft,
            onBuyLaunchSilo     = vm::buyLaunchSilo,
            onBuyRelaySatellite = vm::buyRelaySatellite,
            onBuyOrbitalLab     = vm::buyOrbitalLab,
            onBuyAsteroidMiner  = vm::buyAsteroidMiner,
            onBuyCoreTap        = vm::buyCoreTap,
            onBuyPlanetCore     = vm::buyPlanetCore,
            onAscend            = vm::ascend,
            onClose             = { screen = Screen.MAIN }
        )
        Screen.TREE -> TreeScreen(
            state   = state,
            onClose = { screen = Screen.MAIN }
        )
        Screen.MAIN -> MainView(
            vm        = vm,
            onShop    = { screen = Screen.SHOP },
            onTree    = { screen = Screen.TREE }
        )
    }
}

@Composable
private fun MainView(vm: GameViewModel, onShop: () -> Unit, onTree: () -> Unit) {
    val state by vm.state.collectAsState()
    var showResetDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Title bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF050505))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "STELLAR EXTRACTION",
                color = AsteroidsGreen,
                fontFamily = AsteroidsFont,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 3.sp
            )
            Text(
                text = "RESET",
                color = AsteroidsGreen.copy(alpha = 0.45f),
                fontFamily = AsteroidsFont,
                fontSize = 10.sp,
                letterSpacing = 2.sp,
                modifier = Modifier.clickable { showResetDialog = true }
            )
        }
        HRule()

        // Resources | Planet
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalAlignment = Alignment.Top
        ) {
            // Left: resources
            Column(
                modifier = Modifier
                    .width(100.dp)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 12.dp, top = 14.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ResourceItem("IRON",   state.iron,   state.ironPerSec)     { vm.focusedStrike(Resource.IRON) }
                if (state.quartzVisible)
                    ResourceItem("QUARTZ", state.quartz, state.quartzPerSec)   { vm.focusedStrike(Resource.QUARTZ) }
                if (state.energyVisible)
                    ResourceItem("ENERGY", state.energy, state.energyPerSec)   { vm.focusedStrike(Resource.ENERGY) }
                if (state.titaniumVisible)
                    ResourceItem("TITAN",  state.titanium, state.titaniumPerSec) { vm.focusedStrike(Resource.TITANIUM) }
                if (state.iridiumVisible)
                    ResourceItem("IRIDIUM", state.iridium, state.iridiumPerSec) { vm.focusedStrike(Resource.IRIDIUM) }
                if (state.xenonVisible)
                    ResourceItem("XENON",  state.xenon, state.xenonPerSec)     { vm.focusedStrike(Resource.XENON) }
                if (state.stellarShardsVisible)
                    ResourceItem("SHARDS", state.stellarShards.toDouble(), 0.0)
            }

            // Center: planet
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Planet(onClick = vm::strike)
                    Text(
                        text = "[ STRIKE ]",
                        color = AsteroidsGreen.copy(alpha = 0.4f),
                        fontFamily = AsteroidsFont,
                        fontSize = 10.sp,
                        letterSpacing = 3.sp
                    )
                }
            }
        }

        // Bottom nav
        HRule()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
        ) {
            NavLink("SHOP", onClick = onShop)
            NavLink("TREE", onClick = onTree)
        }
    } // end inner Column

    if (showResetDialog) {
        ResetDialog(
            onConfirm = { vm.hardReset(); showResetDialog = false },
            onDismiss = { showResetDialog = false }
        )
    }
    } // end Box
}

@Composable
fun NavLink(label: String, onClick: () -> Unit) {
    Text(
        text = label,
        color = AsteroidsGreen.copy(alpha = 0.7f),
        fontFamily = AsteroidsFont,
        fontSize = 12.sp,
        letterSpacing = 2.sp,
        modifier = Modifier
            .border(width = 1.dp, color = AsteroidsGreen.copy(alpha = 0.3f), shape = RoundedCornerShape(2.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp)
    )
}

@Composable
fun ResourceItem(label: String, amount: Double, rate: Double, onTap: (() -> Unit)? = null) {
    var tapped by remember { mutableStateOf(false) }
    val labelAlpha by animateFloatAsState(
        targetValue = if (tapped) 1f else 0.45f,
        animationSpec = tween(durationMillis = if (tapped) 50 else 400),
        finishedListener = { if (tapped) tapped = false },
        label = "resourceFlash"
    )

    Column(
        modifier = if (onTap != null) Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) { tapped = true; onTap() } else Modifier,
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        Text(
            text = label,
            color = AsteroidsGreen.copy(alpha = labelAlpha),
            fontFamily = AsteroidsFont,
            fontSize = 11.sp,
            letterSpacing = 1.sp
        )
        Text(
            text = formatNumber(amount),
            color = AsteroidsGreen,
            fontFamily = AsteroidsFont,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        if (rate > 0) {
            Text(
                text = "+${formatRate(rate)}/s",
                color = AsteroidsGreen.copy(alpha = 0.5f),
                fontFamily = AsteroidsFont,
                fontSize = 8.sp
            )
        }
    }
}

@Composable
private fun ResetDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.88f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.78f)
                .border(width = 1.dp, color = AsteroidsGreen.copy(alpha = 0.6f), shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp))
                .background(Color.Black)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "RESET",
                color = AsteroidsGreen,
                fontFamily = AsteroidsFont,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                letterSpacing = 4.sp
            )
            Text(
                text = "All progress will be lost.\nThis cannot be undone.",
                color = AsteroidsGreen.copy(alpha = 0.55f),
                fontFamily = AsteroidsFont,
                fontSize = 11.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 18.sp
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                NavLink(label = "CONFIRM", onClick = onConfirm)
                NavLink(label = "CANCEL",  onClick = onDismiss)
            }
        }
    }
}

@Composable
fun HRule() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(AsteroidsGreen.copy(alpha = 0.2f))
    )
}

@Composable
fun Planet(onClick: () -> Unit) {
    var tapped by remember { mutableStateOf(false) }
    val glow by animateFloatAsState(
        targetValue = if (tapped) 1f else 0.4f,
        animationSpec = tween(durationMillis = if (tapped) 60 else 500),
        finishedListener = { if (tapped) tapped = false },
        label = "planetGlow"
    )

    Canvas(
        modifier = Modifier
            .size(200.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                tapped = true
                onClick()
            }
    ) {
        val cx = size.width / 2
        val cy = size.height / 2
        val r  = size.minDimension / 2 * 0.82f
        val green = AsteroidsGreen

        drawCircle(
            color = green.copy(alpha = glow),
            radius = r,
            center = Offset(cx, cy),
            style = Stroke(width = 2.5f)
        )

        for (t in listOf(-0.5f, 0f, 0.5f)) {
            val latY = cy + t * r
            val latR = sqrt(max(0f, r * r - (t * r).pow(2)))
            val h = latR * 0.28f
            drawOval(
                color = green.copy(alpha = glow * 0.4f),
                topLeft = Offset(cx - latR, latY - h),
                size = Size(latR * 2, h * 2),
                style = Stroke(width = 1.5f)
            )
        }

        for (angle in listOf(0f, 40f, -40f)) {
            rotate(degrees = angle) {
                drawOval(
                    color = green.copy(alpha = glow * 0.4f),
                    topLeft = Offset(cx - r * 0.18f, cy - r),
                    size = Size(r * 0.36f, r * 2),
                    style = Stroke(width = 1.5f)
                )
            }
        }
    }
}

fun formatRate(n: Double): String = when {
    n >= 1_000_000_000 -> "%.1fB".format(n / 1_000_000_000)
    n >= 1_000_000     -> "%.1fM".format(n / 1_000_000)
    n >= 1_000         -> "%.1fK".format(n / 1_000)
    else               -> "%.2f".format(n)
}

fun formatNumber(n: Double): String = when {
    n >= 1_000_000_000 -> "%.1fB".format(n / 1_000_000_000)
    n >= 1_000_000     -> "%.1fM".format(n / 1_000_000)
    n >= 1_000         -> "%.1fK".format(n / 1_000)
    else               -> floor(n).toInt().toString()
}
