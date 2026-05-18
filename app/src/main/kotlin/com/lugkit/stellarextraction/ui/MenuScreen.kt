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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lugkit.stellarextraction.*

@Composable
fun MenuScreen(
    state: GameState,
    onBuyDrillHead: () -> Unit,
    onBuyPowerCore: () -> Unit,
    onBuyDeepShaft: () -> Unit,
    onBuyLaunchSilo: () -> Unit,
    onBuyRelaySatellite: () -> Unit,
    onBuyOrbitalLab: () -> Unit,
    onBuyAsteroidMiner: () -> Unit,
    onBuyCoreTap: () -> Unit,
    onBuyPlanetCore: () -> Unit,
    onAscend: () -> Unit,
    onClose: () -> Unit
) {
    var tab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MENU",
                color = AsteroidsGreen,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                letterSpacing = 3.sp
            )
            Text(
                text = "[ CLOSE ]",
                color = AsteroidsGreen.copy(alpha = 0.6f),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                modifier = Modifier.clickable { onClose() }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(AsteroidsGreen.copy(alpha = 0.2f))
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MenuTabButton(label = "SHOP", selected = tab == 0) { tab = 0 }
            MenuTabButton(label = "TREE", selected = tab == 1) { tab = 1 }
        }

        when (tab) {
            0 -> ShopTab(state, onBuyDrillHead, onBuyPowerCore, onBuyDeepShaft,
                         onBuyLaunchSilo, onBuyRelaySatellite, onBuyOrbitalLab,
                         onBuyAsteroidMiner, onBuyCoreTap, onBuyPlanetCore, onAscend)
            1 -> TreeTab(state)
        }
    }
}

@Composable
fun MenuTabButton(label: String, selected: Boolean, onClick: () -> Unit) {
    val borderColor = if (selected) AsteroidsGreen else AsteroidsGreen.copy(alpha = 0.25f)
    val textColor   = if (selected) AsteroidsGreen else AsteroidsGreen.copy(alpha = 0.4f)
    Text(
        text = label,
        color = textColor,
        fontFamily = FontFamily.Monospace,
        fontSize = 12.sp,
        letterSpacing = 2.sp,
        modifier = Modifier
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(2.dp))
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 8.dp)
    )
}

// ── SHOP ─────────────────────────────────────────────────────────────────────

@Composable
fun ShopTab(
    state: GameState,
    onBuyDrillHead: () -> Unit,
    onBuyPowerCore: () -> Unit,
    onBuyDeepShaft: () -> Unit,
    onBuyLaunchSilo: () -> Unit,
    onBuyRelaySatellite: () -> Unit,
    onBuyOrbitalLab: () -> Unit,
    onBuyAsteroidMiner: () -> Unit,
    onBuyCoreTap: () -> Unit,
    onBuyPlanetCore: () -> Unit,
    onAscend: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // ── Drill Head ────────────────────────────────────────────────────────
        if (state.drillHeadLevel < 4) {
            val next = state.drillHeadLevel + 1
            val cost = drillHeadCosts[next]!!
            val prod = when (next) { 1 -> "1"; 2 -> "3"; 3 -> "9"; else -> "27" }
            SectionLabel("MINING")
            UpgradeCard(
                name = "DRILL HEAD",
                description = "$prod iron/sec",
                levelLabel = "LVL ${state.drillHeadLevel} → $next",
                costText = formatCost(cost),
                canAfford = state.canAfford(cost),
                onClick = onBuyDrillHead
            )
        }

        // ── Power Core ────────────────────────────────────────────────────────
        if (state.powerCoreLevel < 1 && state.drillHeadLevel >= 2) {
            SectionLabel("ENERGY")
            UpgradeCard(
                name = "POWER CORE",
                description = "1 energy/sec",
                levelLabel = "BUILD",
                costText = formatCost(powerCoreCost),
                canAfford = state.canAfford(powerCoreCost),
                onClick = onBuyPowerCore
            )
        }

        // ── Deep Shaft lv1 ────────────────────────────────────────────────────
        if (state.deepShaftLevel < 1 && state.drillHeadLevel >= 3) {
            SectionLabel("DEEP MINING")
            UpgradeCard(
                name = "DEEP SHAFT",
                description = "0.2 titanium/sec",
                levelLabel = "BUILD",
                costText = formatCost(deepShaftCosts[1]!!),
                canAfford = state.canAfford(deepShaftCosts[1]!!),
                onClick = onBuyDeepShaft
            )
        }

        // ── Launch Silo ───────────────────────────────────────────────────────
        if (!state.hasLaunchSilo && state.deepShaftLevel >= 1) {
            SectionLabel("ORBITAL")
            UpgradeCard(
                name = "LAUNCH SILO",
                description = "Enables orbital launches",
                levelLabel = "BUILD",
                costText = formatCost(launchSiloCost),
                canAfford = state.canAfford(launchSiloCost),
                onClick = onBuyLaunchSilo
            )
        }

        // ── Deep Shaft lv2 ────────────────────────────────────────────────────
        if (state.deepShaftLevel < 2 && state.deepShaftLevel >= 1 && state.drillHeadLevel >= 4) {
            SectionLabel("DEEP MINING")
            UpgradeCard(
                name = "DEEP SHAFT LV.2",
                description = "0.05 iridium/sec",
                levelLabel = "UPGRADE",
                costText = formatCost(deepShaftCosts[2]!!),
                canAfford = state.canAfford(deepShaftCosts[2]!!),
                onClick = onBuyDeepShaft
            )
        }

        // ── Drill Head lv4 extra check: show when lv3 owned ──────────────────
        // (already handled above via drillHeadLevel < 4)

        // ── Relay Satellite ───────────────────────────────────────────────────
        if (!state.hasRelaySatellite && state.hasLaunchSilo) {
            SectionLabel("ORBITAL")
            UpgradeCard(
                name = "RELAY SATELLITE",
                description = "Orbital communications",
                levelLabel = "LAUNCH",
                costText = formatCost(relaySatelliteCost),
                canAfford = state.canAfford(relaySatelliteCost),
                onClick = onBuyRelaySatellite
            )
        }

        // ── Orbital Lab ───────────────────────────────────────────────────────
        if (!state.hasOrbitalLab && state.hasRelaySatellite) {
            SectionLabel("ORBITAL")
            UpgradeCard(
                name = "ORBITAL LAB",
                description = "Advanced research platform",
                levelLabel = "BUILD",
                costText = formatCost(orbitalLabCost),
                canAfford = state.canAfford(orbitalLabCost),
                onClick = onBuyOrbitalLab
            )
        }

        // ── Asteroid Miner ────────────────────────────────────────────────────
        if (!state.hasAsteroidMiner && state.hasOrbitalLab) {
            SectionLabel("ORBITAL")
            UpgradeCard(
                name = "ASTEROID MINER",
                description = "0.02 xenon/sec",
                levelLabel = "DEPLOY",
                costText = formatCost(asteroidMinerCost),
                canAfford = state.canAfford(asteroidMinerCost),
                onClick = onBuyAsteroidMiner
            )
        }

        // ── Core Tap ─────────────────────────────────────────────────────────
        if (!state.hasCoreTap && state.drillHeadLevel >= 4) {
            SectionLabel("CORE")
            UpgradeCard(
                name = "CORE TAP",
                description = "Access to planet core",
                levelLabel = "BUILD",
                costText = formatCost(coreTapCost),
                canAfford = state.canAfford(coreTapCost),
                onClick = onBuyCoreTap
            )
        }

        // ── Planet Core ───────────────────────────────────────────────────────
        if (!state.hasPlanetCore && state.hasCoreTap) {
            SectionLabel("CORE")
            UpgradeCard(
                name = "PLANET CORE",
                description = "Breach the planet's core",
                levelLabel = "BUILD",
                costText = formatCost(planetCoreCost),
                canAfford = state.canAfford(planetCoreCost),
                onClick = onBuyPlanetCore
            )
        }

        // ── Ascend ────────────────────────────────────────────────────────────
        if (state.hasPlanetCore) {
            SectionLabel("PRESTIGE")
            UpgradeCard(
                name = "ASCEND",
                description = "Reset all — earn 1 Stellar Shard",
                levelLabel = "PRESTIGE",
                costText = "free",
                canAfford = true,
                onClick = onAscend
            )
        }
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(
        text = text,
        color = AsteroidsGreen.copy(alpha = 0.35f),
        fontFamily = FontFamily.Monospace,
        fontSize = 10.sp,
        letterSpacing = 2.sp
    )
}

@Composable
fun UpgradeCard(
    name: String,
    description: String,
    levelLabel: String,
    costText: String,
    canAfford: Boolean,
    onClick: () -> Unit
) {
    val lineColor = if (canAfford) AsteroidsGreen else AsteroidsGreen.copy(alpha = 0.2f)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = lineColor, shape = RoundedCornerShape(2.dp))
            .clickable(enabled = canAfford) { onClick() }
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = name,
                color = lineColor,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = 1.sp
            )
            Text(
                text = levelLabel,
                color = lineColor.copy(alpha = 0.7f),
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
            )
        }
        Text(
            text = description,
            color = lineColor.copy(alpha = 0.55f),
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp
        )
        Text(
            text = "COST:  $costText",
            color = lineColor.copy(alpha = 0.85f),
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp
        )
    }
}

private fun formatCost(cost: BuildCost): String {
    val parts = mutableListOf<String>()
    if (cost.iron > 0)     parts.add("${formatNumber(cost.iron)} iron")
    if (cost.quartz > 0)   parts.add("${formatNumber(cost.quartz)} quartz")
    if (cost.titanium > 0) parts.add("${formatNumber(cost.titanium)} titanium")
    if (cost.energy > 0)   parts.add("${formatNumber(cost.energy)} energy")
    if (cost.iridium > 0)  parts.add("${formatNumber(cost.iridium)} iridium")
    if (cost.xenon > 0)    parts.add("${formatNumber(cost.xenon)} xenon")
    return parts.joinToString(" + ")
}

// ── PROGRESSION TREE ─────────────────────────────────────────────────────────

private class TreeEntry(
    val label: String,
    val detail: String,
    val lockedHint: String,
    val isUnlocked: (GameState) -> Boolean
)

private val treeEntries = listOf(
    TreeEntry("PICKAXE",           "Manual strike mining",          "ACTIVE")          { true },
    TreeEntry("DRILL HEAD LV.1",   "1 iron/sec",                   "BUY IN SHOP")      { it.drillHeadLevel >= 1 },
    TreeEntry("DRILL HEAD LV.2",   "3 iron/sec  +  quartz",        "NEED DRILL LV.1")  { it.drillHeadLevel >= 2 },
    TreeEntry("POWER CORE",        "1 energy/sec",                  "NEED DRILL LV.2")  { it.powerCoreLevel >= 1 },
    TreeEntry("DRILL HEAD LV.3",   "9 iron/sec",                   "NEED DRILL LV.2")  { it.drillHeadLevel >= 3 },
    TreeEntry("DEEP SHAFT",        "0.2 titanium/sec",              "NEED DRILL LV.3")  { it.deepShaftLevel >= 1 },
    TreeEntry("LAUNCH SILO",       "Orbital launch capability",     "NEED DEEP SHAFT")  { it.hasLaunchSilo },
    TreeEntry("DRILL HEAD LV.4",   "27 iron/sec",                  "NEED DRILL LV.3")  { it.drillHeadLevel >= 4 },
    TreeEntry("DEEP SHAFT LV.2",   "0.05 iridium/sec",             "NEED DRILL LV.4")  { it.deepShaftLevel >= 2 },
    TreeEntry("RELAY SATELLITE",   "Orbital communications",        "NEED SILO")        { it.hasRelaySatellite },
    TreeEntry("ORBITAL LAB",       "Advanced research",             "NEED RELAY")       { it.hasOrbitalLab },
    TreeEntry("ASTEROID MINER",    "0.02 xenon/sec",               "NEED LAB")         { it.hasAsteroidMiner },
    TreeEntry("CORE TAP",          "Planet core access",            "NEED DRILL LV.4")  { it.hasCoreTap },
    TreeEntry("PLANET CORE",       "Pre-ascension",                 "NEED CORE TAP")    { it.hasPlanetCore },
    TreeEntry("ASCEND",            "Reset  →  +1 Stellar Shard",   "NEED PLANET CORE") { it.stellarShards > 0 },
    TreeEntry("STELLAR SHARDS",    "Permanent prestige currency",   "ASCEND FIRST")     { it.stellarShards > 0 }
)

@Composable
fun TreeTab(state: GameState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        treeEntries.forEachIndexed { index, entry ->
            val unlocked = entry.isUnlocked(state)
            TreeNodeItem(entry = entry, unlocked = unlocked)
            if (index < treeEntries.lastIndex) {
                val nextUnlocked = treeEntries[index + 1].isUnlocked(state)
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(28.dp)
                        .background(AsteroidsGreen.copy(alpha = if (unlocked && nextUnlocked) 0.5f else 0.12f))
                )
            }
        }
    }
}

@Composable
private fun TreeNodeItem(entry: TreeEntry, unlocked: Boolean) {
    val color = if (unlocked) AsteroidsGreen else AsteroidsGreen.copy(alpha = 0.22f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = color, shape = RoundedCornerShape(2.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = entry.label,
                color = color,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )
            Text(
                text = entry.detail,
                color = color.copy(alpha = 0.6f),
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp
            )
        }
        Text(
            text = if (unlocked) "ACTIVE" else entry.lockedHint,
            color = color.copy(alpha = 0.75f),
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            letterSpacing = 0.5.sp
        )
    }
}
