package com.lugkit.stellarextraction.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lugkit.stellarextraction.GameViewModel
import com.lugkit.stellarextraction.data.RESOURCE_MAP
import com.lugkit.stellarextraction.data.RESOURCES
import kotlin.math.*

@Composable
fun PlanetScreen(vm: GameViewModel) {
    val state by vm.state.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceBlack)
            .verticalScroll(scrollState)
            .padding(bottom = 80.dp)
    ) {
        PlanetCanvas()

        Spacer(Modifier.height(12.dp))

        Text(
            "PLANETRESSOURCER",
            color = SpaceAccent,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        for (res in RESOURCES) {
            if (res.id !in state.unlockedResources) continue
            val amount = state.planetResources[res.id] ?: 0.0
            val cap = vm.planetCap(res.id)
            val rate = vm.miningRate(res.id)
            ResourceCard(res.id, amount, cap, rate)
        }
    }
}

@Composable
fun PlanetCanvas() {
    val infiniteTransition = rememberInfiniteTransition(label = "planet")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing)),
        label = "rot"
    )
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        drawPlanet(rotation, pulse)
    }
}

private fun DrawScope.drawPlanet(rotation: Float, pulse: Float) {
    val cx = size.width / 2
    val cy = size.height / 2
    val r = minOf(size.width, size.height) * 0.38f

    // Outer glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0x3300B4D8), Color.Transparent),
            center = Offset(cx, cy),
            radius = r * 1.5f
        ),
        radius = r * 1.5f,
        center = Offset(cx, cy)
    )

    // Planet body
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF1A4A6B), Color(0xFF0A1F30)),
            center = Offset(cx - r * 0.2f, cy - r * 0.2f),
            radius = r
        ),
        radius = r,
        center = Offset(cx, cy)
    )

    // Cloud bands
    val bands = listOf(
        Pair(0.35f, Color(0x401A6B8A)),
        Pair(0.15f, Color(0x301A8A6B)),
        Pair(-0.2f, Color(0x401A6B8A))
    )
    for ((yOff, color) in bands) {
        val bandY = cy + yOff * r
        val halfH = r * 0.06f
        val bandW = sqrt(max(0f, r * r - (yOff * r) * (yOff * r)))
        drawRect(
            color = color,
            topLeft = Offset(cx - bandW, bandY - halfH),
            size = androidx.compose.ui.geometry.Size(bandW * 2, halfH * 2)
        )
    }

    // Mine nodes
    val nodePositions = listOf(
        Pair(0.4f, -0.3f), Pair(-0.5f, 0.1f), Pair(0.1f, 0.5f),
        Pair(-0.3f, -0.4f), Pair(0.6f, 0.2f)
    )
    for ((nx, ny) in nodePositions) {
        val nodeX = cx + nx * r
        val nodeY = cy + ny * r
        val dist = sqrt(nx * nx + ny * ny)
        if (dist > 0.85f) continue
        val nodePulse = pulse * 0.8f + 0.4f
        drawCircle(
            color = Color(0xFFFFEB3B).copy(alpha = 0.2f * nodePulse),
            radius = 10f * nodePulse,
            center = Offset(nodeX, nodeY)
        )
        drawCircle(
            color = Color(0xFFFFEB3B),
            radius = 3f,
            center = Offset(nodeX, nodeY)
        )
    }

    // Scan line
    val scanAngle = Math.toRadians(rotation.toDouble())
    val scanX = cx + (r * cos(scanAngle)).toFloat()
    val scanY = cy + (r * sin(scanAngle)).toFloat()
    drawLine(
        color = Color(0x8000B4D8),
        start = Offset(cx, cy),
        end = Offset(scanX, scanY),
        strokeWidth = 1.5f
    )

    // Planet border
    drawCircle(
        color = Color(0x8000B4D8),
        radius = r,
        center = Offset(cx, cy),
        style = Stroke(width = 1f)
    )
}

@Composable
fun ResourceCard(resourceId: String, amount: Double, cap: Double, rate: Double) {
    val res = RESOURCE_MAP[resourceId] ?: return
    val fill = (amount / cap).toFloat().coerceIn(0f, 1f)
    val nearCap = fill > 0.9f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = SpacePanel)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(res.color.copy(alpha = 0.2f), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(res.symbol, color = res.color, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(res.name, color = SpaceText, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        formatNumber(amount),
                        color = if (nearCap) Color(0xFFFF5252) else SpaceText,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        "+${formatNumber(rate)}/s",
                        color = SpaceAccent,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(SpaceDark)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fill)
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(
                                listOf(res.color.copy(alpha = 0.7f), res.color)
                            )
                        )
                )
            }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
            ) {
                Text("${(fill * 100).toInt()}%", color = SpaceSubtext, fontSize = 9.sp)
                Text("/ ${formatNumber(cap)}", color = SpaceSubtext, fontSize = 9.sp)
            }
        }
    }
}

fun formatNumber(n: Double): String {
    return when {
        n >= 1_000_000_000_000.0 -> "%.1fT".format(n / 1_000_000_000_000.0)
        n >= 1_000_000_000.0     -> "%.1fG".format(n / 1_000_000_000.0)
        n >= 1_000_000.0         -> "%.1fM".format(n / 1_000_000.0)
        n >= 1_000.0             -> "%.1fK".format(n / 1_000.0)
        else                     -> "%.1f".format(n)
    }
}
