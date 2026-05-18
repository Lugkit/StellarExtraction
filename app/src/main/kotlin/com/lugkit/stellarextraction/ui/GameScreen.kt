package com.lugkit.stellarextraction.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lugkit.stellarextraction.GameViewModel
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

@Composable
fun GameScreen(vm: GameViewModel) {
    val state by vm.state.collectAsState()
    var showMenu by remember { mutableStateOf(false) }

    if (showMenu) {
        MenuScreen(
            state = state,
            onUpgradeDrill = vm::upgradeDrill,
            onClose = { showMenu = false }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            TopMenu(
                iron = state.iron,
                ironPerSecond = state.ironPerSecond,
                onMenuClick = { showMenu = true }
            )

            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Planet(onClick = vm::mine)
                    Text(
                        text = "[ TAP TO MINE ]",
                        color = AsteroidsGreen.copy(alpha = 0.4f),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        letterSpacing = 3.sp
                    )
                }
            }
        }
    }
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
            .size(260.dp)
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
        val r = size.minDimension / 2 * 0.82f
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

@Composable
fun TopMenu(iron: Double, ironPerSecond: Double, onMenuClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "STELLAR EXTRACTION",
                color = AsteroidsGreen,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 3.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ResourceChip(label = "IRON", amount = iron, rate = ironPerSecond)
            }
        }
        Text(
            text = "[ MENU ]",
            color = AsteroidsGreen.copy(alpha = 0.6f),
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            letterSpacing = 1.sp,
            modifier = Modifier.clickable { onMenuClick() }
        )
    }
}

@Composable
fun ResourceChip(label: String, amount: Double, rate: Double) {
    Row(
        modifier = Modifier
            .border(width = 1.dp, color = AsteroidsGreen.copy(alpha = 0.3f), shape = RoundedCornerShape(2.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = AsteroidsGreen.copy(alpha = 0.5f),
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = formatNumber(amount),
            color = AsteroidsGreen,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        if (rate > 0) {
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "+${formatRate(rate)}/s",
                color = AsteroidsGreen.copy(alpha = 0.55f),
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
            )
        }
    }
}

fun formatRate(n: Double): String = when {
    n >= 1_000_000_000 -> "%.1fB".format(n / 1_000_000_000)
    n >= 1_000_000 -> "%.1fM".format(n / 1_000_000)
    n >= 1_000 -> "%.1fK".format(n / 1_000)
    else -> "%.2f".format(n)
}

fun formatNumber(n: Double): String = when {
    n >= 1_000_000_000 -> "%.1fB".format(n / 1_000_000_000)
    n >= 1_000_000 -> "%.1fM".format(n / 1_000_000)
    n >= 1_000 -> "%.1fK".format(n / 1_000)
    else -> floor(n).toInt().toString()
}
