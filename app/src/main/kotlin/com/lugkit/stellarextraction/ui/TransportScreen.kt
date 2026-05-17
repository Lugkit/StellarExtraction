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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lugkit.stellarextraction.GameViewModel
import com.lugkit.stellarextraction.data.RESOURCE_MAP
import com.lugkit.stellarextraction.data.RESOURCES

@Composable
fun TransportScreen(vm: GameViewModel) {
    val state by vm.state.collectAsState()
    val hasDock = state.builtModules.contains("docking_bay")
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceBlack)
            .verticalScroll(scrollState)
            .padding(16.dp)
            .padding(bottom = 80.dp)
    ) {
        Text(
            "TRANSPORT",
            color = SpaceAccent,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        Spacer(Modifier.height(4.dp))

        if (!hasDock) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SpacePanel)
            ) {
                Text(
                    "Byg Dockingbay på stationen for at aktivere transport",
                    color = SpaceSubtext,
                    modifier = Modifier.padding(16.dp)
                )
            }
            return@Column
        }

        val hasMulti = state.purchasedUpgrades.contains("multi_launch")
        if (hasMulti) {
            Button(
                onClick = { vm.launchAll() },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SpaceAccent2)
            ) {
                Text("AFYR ALLE RESSOURCER", letterSpacing = 1.sp)
            }
        }

        Text(
            "Rejsetid: ${"%.0f".format(vm.travelSeconds())}s",
            color = SpaceSubtext,
            fontSize = 11.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        for (res in RESOURCES) {
            if (res.id !in state.unlockedResources) continue
            val amount = state.planetResources[res.id] ?: 0.0
            val capacity = vm.launchCapacity(res.id)
            val canLaunch = amount >= 1.0

            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = SpacePanel),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(res.color.copy(alpha = 0.2f), RoundedCornerShape(4.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(res.symbol, color = res.color, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(res.name, color = SpaceText, fontSize = 14.sp)
                        }
                        Text(
                            "Tilgængeligt: ${formatNumber(amount)}  Kapacitet: ${formatNumber(capacity)}",
                            color = SpaceSubtext,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Button(
                        onClick = { vm.launch(res.id) },
                        enabled = canLaunch,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SpaceAccent,
                            disabledContainerColor = SpaceDark
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("AFYR", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (state.activeShipments.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text(
                "AKTIVE FORSENDELSER",
                color = SpaceAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(4.dp))

            for (ship in state.activeShipments) {
                val res = RESOURCE_MAP[ship.resourceId] ?: continue
                val elapsed = (System.currentTimeMillis() - ship.startTime) / 1000.0
                val progress = (elapsed / ship.travelSeconds).toFloat().coerceIn(0f, 1f)
                val remaining = (ship.travelSeconds - elapsed).coerceAtLeast(0.0)

                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    colors = CardDefaults.cardColors(containerColor = SpacePanel),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(Modifier.padding(10.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("${res.name}: ${formatNumber(ship.amount)}", color = SpaceText, fontSize = 12.sp)
                            Text("${remaining.toInt()}s", color = SpaceSubtext, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }
                        Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(SpaceDark)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progress)
                                    .fillMaxHeight()
                                    .background(Brush.horizontalGradient(listOf(res.color.copy(0.6f), res.color)))
                            )
                        }
                    }
                }
            }
        }
    }
}
