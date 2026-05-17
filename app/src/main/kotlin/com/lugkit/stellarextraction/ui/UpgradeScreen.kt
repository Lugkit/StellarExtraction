package com.lugkit.stellarextraction.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lugkit.stellarextraction.GameViewModel
import com.lugkit.stellarextraction.data.UPGRADES

private val CATEGORIES = listOf("mining" to "MINERING", "transport" to "TRANSPORT", "station" to "STATION", "research" to "FORSKNING")

@Composable
fun UpgradeScreen(vm: GameViewModel) {
    val state by vm.state.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceBlack)
            .padding(bottom = 80.dp)
    ) {
        Text(
            "OPGRADERINGER",
            color = SpaceAccent,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = SpaceDark,
            contentColor = SpaceAccent,
            edgePadding = 0.dp
        ) {
            CATEGORIES.forEachIndexed { i, (_, label) ->
                Tab(
                    selected = selectedTab == i,
                    onClick = { selectedTab = i },
                    text = {
                        Text(
                            label,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp,
                            fontWeight = if (selectedTab == i) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(12.dp)
        ) {
            val category = CATEGORIES[selectedTab].first
            val upgrades = UPGRADES.filter { it.category == category }

            for (up in upgrades) {
                val purchased = up.id in state.purchasedUpgrades
                val canBuy = vm.canBuyUpgrade(up.id)
                val locked = up.requires != null && up.requires !in state.purchasedUpgrades

                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            purchased -> SpaceAccent.copy(alpha = 0.06f)
                            locked    -> SpacePanel.copy(alpha = 0.5f)
                            else      -> SpacePanel
                        }
                    ),
                    border = if (purchased) BorderStroke(1.dp, SpaceAccent.copy(alpha = 0.3f)) else null
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (purchased) Text("✓ ", color = SpaceAccent, fontSize = 12.sp)
                                if (locked && !purchased) Text("🔒 ", color = SpaceSubtext, fontSize = 11.sp)
                                Text(
                                    up.name,
                                    color = if (purchased) SpaceAccent else if (locked) SpaceSubtext else SpaceText,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            }
                            Text(up.description, color = SpaceSubtext, fontSize = 11.sp)

                            if (!purchased && !locked) {
                                Spacer(Modifier.height(4.dp))
                                CostRow(up.cost, state.stationResources)
                            }
                            if (locked) {
                                val reqName = UPGRADES.find { it.id == up.requires }?.name ?: up.requires
                                Text("Kræver: $reqName", color = SpaceSubtext.copy(alpha = 0.5f), fontSize = 10.sp)
                            }
                        }

                        if (!purchased && !locked) {
                            Spacer(Modifier.width(8.dp))
                            Button(
                                onClick = { vm.buyUpgrade(up.id) },
                                enabled = canBuy,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SpaceAccent,
                                    disabledContainerColor = SpaceDark
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("KØB", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
