package com.lugkit.stellarextraction.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lugkit.stellarextraction.GameViewModel
import com.lugkit.stellarextraction.data.MODULE_MAP
import com.lugkit.stellarextraction.data.MODULES
import com.lugkit.stellarextraction.data.RESOURCE_MAP
import com.lugkit.stellarextraction.data.RESOURCES

@Composable
fun StationScreen(vm: GameViewModel) {
    val state by vm.state.collectAsState()
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
            "RUMSTATION",
            color = SpaceAccent,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        Spacer(Modifier.height(8.dp))

        // Station resources
        Text("Stationslagre", color = SpaceSubtext, fontSize = 11.sp, modifier = Modifier.padding(bottom = 4.dp))
        for (res in RESOURCES) {
            if (res.id !in state.unlockedResources) continue
            val amount = state.stationResources[res.id] ?: 0.0
            val cap = vm.stationCap(res.id)
            val fill = (amount / cap).toFloat().coerceIn(0f, 1f)

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(res.color.copy(alpha = 0.2f), RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(res.symbol, color = res.color, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(res.name, color = SpaceText, fontSize = 12.sp)
                        Text("${formatNumber(amount)} / ${formatNumber(cap)}", color = SpaceSubtext, fontSize = 11.sp)
                    }
                    Spacer(Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(SpaceDark)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fill)
                                .fillMaxHeight()
                                .background(Brush.horizontalGradient(listOf(res.color.copy(0.5f), res.color)))
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Divider(color = SpaceAccent.copy(alpha = 0.2f))
        Spacer(Modifier.height(16.dp))

        // Modules
        Text(
            "MODULER",
            color = SpaceAccent,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        Spacer(Modifier.height(8.dp))

        for (mod in MODULES) {
            val built = mod.id in state.builtModules
            val canBuild = vm.canBuildModule(mod.id)
            val locked = mod.requires != null && mod.requires !in state.builtModules

            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (built) SpaceAccent.copy(alpha = 0.08f) else SpacePanel
                ),
                border = if (built) BorderStroke(1.dp, SpaceAccent.copy(alpha = 0.4f)) else null
            ) {
                Column(Modifier.padding(12.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    if (built) "✓ " else if (locked) "🔒 " else "",
                                    color = if (built) SpaceAccent else SpaceSubtext,
                                    fontSize = 12.sp
                                )
                                Text(
                                    mod.name,
                                    color = if (built) SpaceAccent else if (locked) SpaceSubtext else SpaceText,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                            }
                            Text(mod.description, color = SpaceSubtext, fontSize = 11.sp)
                        }
                        if (!built && !locked) {
                            Button(
                                onClick = { vm.buildModule(mod.id) },
                                enabled = canBuild,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SpaceAccent2,
                                    disabledContainerColor = SpaceDark
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("BYG", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (!built && !locked) {
                        Spacer(Modifier.height(6.dp))
                        CostRow(mod.cost, state.stationResources)
                    }
                    if (locked) {
                        val reqName = MODULE_MAP[mod.requires]?.name ?: mod.requires
                        Text("Kræver: $reqName", color = SpaceSubtext.copy(alpha = 0.6f), fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CostRow(cost: Map<String, Double>, have: Map<String, Double>) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        for ((k, v) in cost) {
            val res = RESOURCE_MAP[k] ?: continue
            val enough = (have[k] ?: 0.0) >= v
            Text(
                "${res.symbol}: ${formatNumber(v)}",
                color = if (enough) SpaceAccent else SpaceSubtext.copy(alpha = 0.5f),
                fontSize = 10.sp
            )
        }
    }
}
