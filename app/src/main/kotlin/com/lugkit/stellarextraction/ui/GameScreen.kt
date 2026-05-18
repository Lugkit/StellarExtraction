package com.lugkit.stellarextraction.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lugkit.stellarextraction.GameViewModel
import kotlin.math.floor

@Composable
fun GameScreen(vm: GameViewModel) {
    val state by vm.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        TopMenu(
            iron = state.iron,
            ironPerSecond = state.ironPerSecond
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(onClick = vm::mine) {
                    Text(
                        text = "MINE  +${formatNumber(state.ironPerClick)} iron",
                        fontFamily = FontFamily.Monospace
                    )
                }
                Button(
                    onClick = vm::upgradeDrill,
                    enabled = state.iron >= state.drillCost,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1F2937),
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Upgrade Drill  [${formatNumber(state.drillCost)} iron]",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                }
                if (state.drillLevel > 0) {
                    Text(
                        text = "Drill level ${state.drillLevel}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun TopMenu(iron: Double, ironPerSecond: Double) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = "STELLAR EXTRACTION",
            color = MaterialTheme.colorScheme.primary,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 3.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ResourceChip(
                label = "IRON",
                amount = iron,
                rate = ironPerSecond,
                color = Color(0xFFB0BEC5)
            )
        }
    }
}

@Composable
fun ResourceChip(label: String, amount: Double, rate: Double, color: Color) {
    Row(
        modifier = Modifier
            .background(color = color.copy(alpha = 0.08f), shape = RoundedCornerShape(6.dp))
            .border(width = 1.dp, color = color.copy(alpha = 0.25f), shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = color.copy(alpha = 0.6f),
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = formatNumber(amount),
            color = color,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        if (rate > 0) {
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "+${formatRate(rate)}/s",
                color = color.copy(alpha = 0.55f),
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
    else -> "%.1f".format(n)
}

fun formatNumber(n: Double): String = when {
    n >= 1_000_000_000 -> "%.1fB".format(n / 1_000_000_000)
    n >= 1_000_000 -> "%.1fM".format(n / 1_000_000)
    n >= 1_000 -> "%.1fK".format(n / 1_000)
    else -> floor(n).toInt().toString()
}
