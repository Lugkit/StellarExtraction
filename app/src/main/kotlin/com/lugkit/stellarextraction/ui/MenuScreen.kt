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
fun ShopScreen(
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        FullScreenHeader(title = "SHOP")
        Box(modifier = Modifier.weight(1f)) {
            ShopTab(state, onBuyDrillHead, onBuyPowerCore, onBuyDeepShaft,
                    onBuyLaunchSilo, onBuyRelaySatellite, onBuyOrbitalLab,
                    onBuyAsteroidMiner, onBuyCoreTap, onBuyPlanetCore, onAscend)
        }
        BottomNav(onClose = onClose)
    }
}

@Composable
fun TreeScreen(state: GameState, onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        FullScreenHeader(title = "TECH TREE")
        Box(modifier = Modifier.weight(1f)) {
            TreeTab(state)
        }
        BottomNav(onClose = onClose)
    }
}

@Composable
private fun FullScreenHeader(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(androidx.compose.ui.graphics.Color(0xFF050505))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            color = AsteroidsGreen,
            fontFamily = AsteroidsFont,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 3.sp
        )
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(AsteroidsGreen.copy(alpha = 0.2f))
    )
}

@Composable
private fun BottomNav(onClose: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(AsteroidsGreen.copy(alpha = 0.2f))
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        NavLink(label = "BACK", onClick = onClose)
    }
}

@Composable
fun MenuTabButton(label: String, selected: Boolean, onClick: () -> Unit) {
    val borderColor = if (selected) AsteroidsGreen else AsteroidsGreen.copy(alpha = 0.25f)
    val textColor   = if (selected) AsteroidsGreen else AsteroidsGreen.copy(alpha = 0.4f)
    Text(
        text = label,
        color = textColor,
        fontFamily = AsteroidsFont,
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
                name        = "DRILL HEAD",
                description = "$prod iron/sec",
                levelLabel  = "LVL ${state.drillHeadLevel} → $next",
                cost        = cost,
                state       = state,
                onClick     = onBuyDrillHead
            )
        }

        // ── Power Core ────────────────────────────────────────────────────────
        if (state.powerCoreLevel < 1 && state.drillHeadLevel >= 2) {
            SectionLabel("ENERGY")
            UpgradeCard(
                name        = "POWER CORE",
                description = "1 energy/sec",
                levelLabel  = "BUILD",
                cost        = powerCoreCost,
                state       = state,
                onClick     = onBuyPowerCore
            )
        }

        // ── Deep Shaft lv1 ────────────────────────────────────────────────────
        if (state.deepShaftLevel < 1 && state.drillHeadLevel >= 3) {
            SectionLabel("DEEP MINING")
            UpgradeCard(
                name        = "DEEP SHAFT",
                description = "0.2 titanium/sec",
                levelLabel  = "BUILD",
                cost        = deepShaftCosts[1]!!,
                state       = state,
                onClick     = onBuyDeepShaft
            )
        }

        // ── Launch Silo ───────────────────────────────────────────────────────
        if (!state.hasLaunchSilo && state.deepShaftLevel >= 1) {
            SectionLabel("ORBITAL")
            UpgradeCard(
                name        = "LAUNCH SILO",
                description = "Enables orbital launches",
                levelLabel  = "BUILD",
                cost        = launchSiloCost,
                state       = state,
                onClick     = onBuyLaunchSilo
            )
        }

        // ── Deep Shaft lv2 ────────────────────────────────────────────────────
        if (state.deepShaftLevel < 2 && state.deepShaftLevel >= 1 && state.drillHeadLevel >= 4) {
            SectionLabel("DEEP MINING")
            UpgradeCard(
                name        = "DEEP SHAFT LV.2",
                description = "0.05 iridium/sec",
                levelLabel  = "UPGRADE",
                cost        = deepShaftCosts[2]!!,
                state       = state,
                onClick     = onBuyDeepShaft
            )
        }

        // ── Relay Satellite ───────────────────────────────────────────────────
        if (!state.hasRelaySatellite && state.hasLaunchSilo) {
            SectionLabel("ORBITAL")
            UpgradeCard(
                name        = "RELAY SATELLITE",
                description = "Orbital communications",
                levelLabel  = "LAUNCH",
                cost        = relaySatelliteCost,
                state       = state,
                onClick     = onBuyRelaySatellite
            )
        }

        // ── Orbital Lab ───────────────────────────────────────────────────────
        if (!state.hasOrbitalLab && state.hasRelaySatellite) {
            SectionLabel("ORBITAL")
            UpgradeCard(
                name        = "ORBITAL LAB",
                description = "Advanced research platform",
                levelLabel  = "BUILD",
                cost        = orbitalLabCost,
                state       = state,
                onClick     = onBuyOrbitalLab
            )
        }

        // ── Asteroid Miner ────────────────────────────────────────────────────
        if (!state.hasAsteroidMiner && state.hasOrbitalLab) {
            SectionLabel("ORBITAL")
            UpgradeCard(
                name        = "ASTEROID MINER",
                description = "0.02 xenon/sec",
                levelLabel  = "DEPLOY",
                cost        = asteroidMinerCost,
                state       = state,
                onClick     = onBuyAsteroidMiner
            )
        }

        // ── Core Tap ─────────────────────────────────────────────────────────
        if (!state.hasCoreTap && state.drillHeadLevel >= 4) {
            SectionLabel("CORE")
            UpgradeCard(
                name        = "CORE TAP",
                description = "Access to planet core",
                levelLabel  = "BUILD",
                cost        = coreTapCost,
                state       = state,
                onClick     = onBuyCoreTap
            )
        }

        // ── Planet Core ───────────────────────────────────────────────────────
        if (!state.hasPlanetCore && state.hasCoreTap) {
            SectionLabel("CORE")
            UpgradeCard(
                name        = "PLANET CORE",
                description = "Breach the planet's core",
                levelLabel  = "BUILD",
                cost        = planetCoreCost,
                state       = state,
                onClick     = onBuyPlanetCore
            )
        }

        // ── Ascend ────────────────────────────────────────────────────────────
        if (state.hasPlanetCore) {
            SectionLabel("PRESTIGE")
            UpgradeCard(
                name        = "ASCEND",
                description = "Reset all — earn 1 Stellar Shard",
                levelLabel  = "PRESTIGE",
                cost        = null,
                state       = state,
                onClick     = onAscend
            )
        }
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(
        text = text,
        color = AsteroidsGreen.copy(alpha = 0.35f),
        fontFamily = AsteroidsFont,
        fontSize = 10.sp,
        letterSpacing = 2.sp
    )
}

@Composable
fun UpgradeCard(
    name: String,
    description: String,
    levelLabel: String,
    cost: BuildCost?,
    state: GameState,
    onClick: () -> Unit
) {
    val canAfford = cost == null || state.canAfford(cost)
    val borderColor = if (canAfford) AsteroidsGreen else AsteroidsGreen.copy(alpha = 0.2f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(2.dp))
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
                color = borderColor,
                fontFamily = AsteroidsFont,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = 1.sp
            )
            Text(
                text = levelLabel,
                color = borderColor.copy(alpha = 0.7f),
                fontFamily = AsteroidsFont,
                fontSize = 11.sp
            )
        }
        Text(
            text = description,
            color = borderColor.copy(alpha = 0.55f),
            fontFamily = AsteroidsFont,
            fontSize = 11.sp
        )
        if (cost != null) {
            CostDisplay(cost = cost, state = state)
        } else {
            Text(
                text = "COST  free",
                color = AsteroidsGreen.copy(alpha = 0.85f),
                fontFamily = AsteroidsFont,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun CostDisplay(cost: BuildCost, state: GameState) {
    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Text(
            text = "COST",
            color = AsteroidsGreen.copy(alpha = 0.45f),
            fontFamily = AsteroidsFont,
            fontSize = 9.sp,
            letterSpacing = 2.sp
        )
        if (cost.iron > 0)
            CostLine("${formatNumber(cost.iron)} IRON",     state.iron >= cost.iron)
        if (cost.quartz > 0)
            CostLine("${formatNumber(cost.quartz)} QUARTZ",  state.quartz >= cost.quartz)
        if (cost.titanium > 0)
            CostLine("${formatNumber(cost.titanium)} TITANIUM", state.titanium >= cost.titanium)
        if (cost.energy > 0)
            CostLine("${formatNumber(cost.energy)} ENERGY",  state.energy >= cost.energy)
        if (cost.iridium > 0)
            CostLine("${formatNumber(cost.iridium)} IRIDIUM", state.iridium >= cost.iridium)
        if (cost.xenon > 0)
            CostLine("${formatNumber(cost.xenon)} XENON",    state.xenon >= cost.xenon)
    }
}

@Composable
private fun CostLine(text: String, hasEnough: Boolean) {
    Text(
        text = text,
        color = if (hasEnough) AsteroidsGreen.copy(alpha = 0.85f) else Color(0xFFFF4444),
        fontFamily = AsteroidsFont,
        fontSize = 12.sp
    )
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
                fontFamily = AsteroidsFont,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )
            Text(
                text = entry.detail,
                color = color.copy(alpha = 0.6f),
                fontFamily = AsteroidsFont,
                fontSize = 10.sp
            )
        }
        Text(
            text = if (unlocked) "ACTIVE" else entry.lockedHint,
            color = color.copy(alpha = 0.75f),
            fontFamily = AsteroidsFont,
            fontSize = 9.sp,
            letterSpacing = 0.5.sp
        )
    }
}
