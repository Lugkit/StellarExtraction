package com.lugkit.stellarextraction.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lugkit.stellarextraction.*

@Composable
fun AscContent(vm: GameViewModel) {
    val state by vm.state.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Header with shard count
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF050505))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SHARD SHOP",
                color = AsteroidsGreen,
                fontFamily = AsteroidsFont,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 3.sp
            )
            Text(
                text = "${state.stellarShards} ◆ SHARDS",
                color = AsteroidsGreen,
                fontFamily = AsteroidsFont,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 1.sp
            )
        }
        HRule()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AscSection("MINING")
            AscRow(state, AscUpgrade.STRIKE_YIELD,          "STRIKE YIELD",          "strike output (both modes)",          vm::buyAscUpgrade)
            AscRow(state, AscUpgrade.DRILL_SPEED,           "DRILL SPEED",           "iron/sec from drill head",            vm::buyAscUpgrade)
            AscRow(state, AscUpgrade.DEEP_SHAFT_SPEED,      "DEEP SHAFT SPEED",      "titanium & iridium/sec",              vm::buyAscUpgrade)
            AscRow(state, AscUpgrade.QUARTZ_RICHNESS,       "QUARTZ RICHNESS",       "quartz/sec from drill",               vm::buyAscUpgrade)
            AscRow(state, AscUpgrade.IRIDIUM_CONCENTRATION, "IRIDIUM CONCENTRATION", "iridium/sec (stacks with shaft)",     vm::buyAscUpgrade)
            AscRow(state, AscUpgrade.TITANIUM_DENSITY,      "TITANIUM DENSITY",      "titanium/sec (stacks with shaft)",    vm::buyAscUpgrade)

            Spacer(Modifier.height(4.dp))
            AscSection("CONSTRUCTION")
            AscRow(state, AscUpgrade.LAUNCH_EFFICIENCY, "LAUNCH EFFICIENCY",     "reduces launch silo cost",            vm::buyAscUpgrade)
            AscRow(state, AscUpgrade.POWER_CORE_COST,   "POWER CORE DISCOUNT",   "reduces power core cost",             vm::buyAscUpgrade)
            AscRow(state, AscUpgrade.REFINERY_COST,     "REFINERY DISCOUNT",     "reduces refinery cost",               vm::buyAscUpgrade)

            Spacer(Modifier.height(4.dp))
            AscSection("ORBITAL")
            AscRow(state, AscUpgrade.SATELLITE_UPLINK,     "SATELLITE UPLINK",      "xenon/sec from asteroid miner",       vm::buyAscUpgrade)
            AscRow(state, AscUpgrade.ORBITAL_LAB_RESEARCH, "ORBITAL LAB RESEARCH",  "iridium/sec (stacks with shaft)",     vm::buyAscUpgrade)
            AscRow(state, AscUpgrade.SOLAR_AMPLIFIER,      "SOLAR AMPLIFIER",       "energy/sec from solar arrays",        vm::buyAscUpgrade)

            Spacer(Modifier.height(4.dp))
            AscSection("META")
            AscRow(state, AscUpgrade.HEAD_START, "HEAD START",  "bonus iron at start of next run",     vm::buyAscUpgrade)
            AscRow(state, AscUpgrade.RESONANCE,  "RESONANCE",   "focused strike multiplier",            vm::buyAscUpgrade)

            Spacer(Modifier.height(8.dp))
            Text(
                text = "Total cost to max one upgrade: 10 ◆",
                color = AsteroidsGreen.copy(alpha = 0.3f),
                fontFamily = AsteroidsFont,
                fontSize = 9.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun AscSection(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.weight(1f).height(1.dp).background(AsteroidsGreen.copy(alpha = 0.18f)))
        Text(
            text = "  $title  ",
            color = AsteroidsGreen.copy(alpha = 0.45f),
            fontFamily = AsteroidsFont,
            fontSize = 9.sp,
            letterSpacing = 2.sp
        )
        Box(Modifier.weight(1f).height(1.dp).background(AsteroidsGreen.copy(alpha = 0.18f)))
    }
}

@Composable
private fun AscRow(
    state: GameState,
    upgrade: AscUpgrade,
    name: String,
    description: String,
    onBuy: (AscUpgrade) -> Unit
) {
    val level    = state.ascLevel(upgrade)
    val maxed    = level >= ASC_MAX_LEVEL
    val nextCost = level + 1
    val canAfford = !maxed && state.stellarShards >= nextCost

    val activeColor = if (maxed) AsteroidsGreen.copy(alpha = 0.55f)
                      else if (canAfford) AsteroidsGreen
                      else AsteroidsGreen.copy(alpha = 0.28f)

    val headStartLabels = listOf("500 iron", "2K iron", "8K iron", "32K iron")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, activeColor.copy(alpha = 0.35f), RoundedCornerShape(2.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: name + description + bonus
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(name, color = activeColor, fontFamily = AsteroidsFont, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 0.5.sp)
            Text(description, color = activeColor.copy(alpha = 0.55f), fontFamily = AsteroidsFont, fontSize = 9.sp)
            val bonusText = if (upgrade == AscUpgrade.HEAD_START) {
                if (level == 0) "—" else headStartLabels[level - 1]
            } else {
                ascBonusPct(level)
            }
            Text(bonusText, color = if (level > 0) AsteroidsGreen.copy(alpha = 0.8f) else AsteroidsGreen.copy(alpha = 0.25f), fontFamily = AsteroidsFont, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.width(12.dp))

        // Right: level dots + buy button
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            // Level dots
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                for (i in 1..ASC_MAX_LEVEL) {
                    Text(
                        text = if (i <= level) "●" else "○",
                        color = if (i <= level) AsteroidsGreen else AsteroidsGreen.copy(alpha = 0.2f),
                        fontFamily = AsteroidsFont,
                        fontSize = 10.sp
                    )
                }
            }
            if (maxed) {
                Text("MAXED", color = AsteroidsGreen.copy(alpha = 0.5f), fontFamily = AsteroidsFont, fontSize = 9.sp, letterSpacing = 1.sp)
            } else {
                val costColor = if (canAfford) AsteroidsGreen else AsteroidsGreen.copy(alpha = 0.28f)
                Text(
                    text = "$nextCost ◆",
                    color = costColor,
                    fontFamily = AsteroidsFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = if (canAfford) Modifier
                        .border(1.dp, costColor.copy(alpha = 0.4f), RoundedCornerShape(2.dp))
                        .clickable { onBuy(upgrade) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                    else Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
