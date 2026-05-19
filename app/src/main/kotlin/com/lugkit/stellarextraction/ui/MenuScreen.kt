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

// ── SHOP ─────────────────────────────────────────────────────────────────────

@Composable
fun ShopTab(
    state: GameState,
    onBuyDrillHead: () -> Unit,
    onBuyPowerCore: () -> Unit,
    onBuySolarArray: () -> Unit,
    onBuyDeepShaft: () -> Unit,
    onBuyRefinery: () -> Unit,
    onBuyLaunchSiloA: () -> Unit,
    onBuyLaunchSiloB: () -> Unit,
    onBuyRelaySatellite: () -> Unit,
    onBuyOrbitalLab: () -> Unit,
    onBuyAsteroidMiner: () -> Unit,
    onBuyOrbitalSolarStation: () -> Unit,
    onBuyCoreTap: () -> Unit,
    onBuyPlanetCore: () -> Unit,
    onAscend: () -> Unit,
    onRefineryConvert: (Resource) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // ── MINING ────────────────────────────────────────────────────────────
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

        // ── ENERGY ────────────────────────────────────────────────────────────
        if (state.drillHeadLevel >= 2) {
            SectionLabel("ENERGY")

            if (state.powerCoreLevel < 1) {
                UpgradeCard(
                    name        = "POWER CORE",
                    description = "3 energy/sec  •  upkeep: 1 quartz/sec",
                    levelLabel  = "BUILD",
                    cost        = powerCoreCost,
                    state       = state,
                    onClick     = onBuyPowerCore
                )
            }

            if (state.powerCoreLevel >= 1 && state.solarArrayLevel < 2) {
                val next = state.solarArrayLevel + 1
                val cost = solarArrayCosts[next]!!
                val prod = next
                UpgradeCard(
                    name        = "SOLAR ARRAY",
                    description = "$prod energy/sec  •  no upkeep",
                    levelLabel  = if (state.solarArrayLevel == 0) "BUILD" else "LV1 → LV2",
                    cost        = cost,
                    state       = state,
                    onClick     = onBuySolarArray
                )
            }
        }

        // ── DEEP MINING ───────────────────────────────────────────────────────
        if (state.drillHeadLevel >= 3 && state.deepShaftLevel < 2) {
            SectionLabel("DEEP MINING")
            if (state.deepShaftLevel < 1) {
                UpgradeCard(
                    name        = "DEEP SHAFT",
                    description = "0.5 titanium/sec",
                    levelLabel  = "BUILD",
                    cost        = deepShaftCosts[1]!!,
                    state       = state,
                    onClick     = onBuyDeepShaft
                )
            } else if (state.drillHeadLevel >= 4) {
                UpgradeCard(
                    name        = "DEEP SHAFT LV.2",
                    description = "0.15 iridium/sec",
                    levelLabel  = "UPGRADE",
                    cost        = deepShaftCosts[2]!!,
                    state       = state,
                    onClick     = onBuyDeepShaft
                )
            }
        }

        // ── REFINERY (optional sidegrade) ─────────────────────────────────────
        if (state.drillHeadLevel >= 3 && !state.hasRefinery) {
            SectionLabel("REFINERY")
            UpgradeCard(
                name        = "REFINERY",
                description = "Convert resources downward",
                levelLabel  = "BUILD",
                cost        = refineryCost,
                state       = state,
                onClick     = onBuyRefinery
            )
        }
        if (state.hasRefinery) {
            SectionLabel("REFINERY")
            ConvertCard(
                label    = "SMELT IRIDIUM",
                fromText = "1 IRIDIUM",
                toText   = "3 TITANIUM",
                canAfford= state.iridium >= 1.0,
                onClick  = { onRefineryConvert(Resource.IRIDIUM) }
            )
            ConvertCard(
                label    = "SMELT TITANIUM",
                fromText = "1 TITANIUM",
                toText   = "5 QUARTZ",
                canAfford= state.titanium >= 1.0,
                onClick  = { onRefineryConvert(Resource.TITANIUM) }
            )
            ConvertCard(
                label    = "SMELT QUARTZ",
                fromText = "1 QUARTZ",
                toText   = "5 IRON",
                canAfford= state.quartz >= 1.0,
                onClick  = { onRefineryConvert(Resource.QUARTZ) }
            )
        }

        // ── ORBITAL ───────────────────────────────────────────────────────────
        if (!state.hasLaunchSilo && state.drillHeadLevel >= 3) {
            SectionLabel("ORBITAL")
            if (state.deepShaftLevel >= 1) {
                UpgradeCard(
                    name        = "LAUNCH SILO",
                    description = "Path A — titanium-heavy",
                    levelLabel  = "BUILD",
                    cost        = launchSiloCostA,
                    state       = state,
                    onClick     = onBuyLaunchSiloA
                )
            }
            UpgradeCard(
                name        = "LAUNCH SILO",
                description = "Path B — no titanium required",
                levelLabel  = "BUILD",
                cost        = launchSiloCostB,
                state       = state,
                onClick     = onBuyLaunchSiloB
            )
        }

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

        if (!state.hasAsteroidMiner && state.hasOrbitalLab) {
            SectionLabel("ORBITAL")
            UpgradeCard(
                name        = "ASTEROID MINER",
                description = "0.05 xenon/sec",
                levelLabel  = "DEPLOY",
                cost        = asteroidMinerCost,
                state       = state,
                onClick     = onBuyAsteroidMiner
            )
        }

        if (!state.hasOrbitalSolarStation && state.hasOrbitalLab && state.solarArrayLevel >= 2) {
            SectionLabel("ENERGY")
            UpgradeCard(
                name        = "ORBITAL SOLAR STATION",
                description = "20 energy/sec  •  no upkeep",
                levelLabel  = "BUILD",
                cost        = orbitalSolarStationCost,
                state       = state,
                onClick     = onBuyOrbitalSolarStation
            )
        }

        // ── CORE ──────────────────────────────────────────────────────────────
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

        // ── PRESTIGE ──────────────────────────────────────────────────────────
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
            Text(name, color = borderColor, fontFamily = AsteroidsFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 1.sp)
            Text(levelLabel, color = borderColor.copy(alpha = 0.7f), fontFamily = AsteroidsFont, fontSize = 11.sp)
        }
        Text(description, color = borderColor.copy(alpha = 0.55f), fontFamily = AsteroidsFont, fontSize = 11.sp)
        if (cost != null) {
            CostDisplay(cost = cost, state = state)
        } else {
            Text("COST  free", color = AsteroidsGreen.copy(alpha = 0.85f), fontFamily = AsteroidsFont, fontSize = 12.sp)
        }
    }
}

@Composable
private fun ConvertCard(
    label: String,
    fromText: String,
    toText: String,
    canAfford: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (canAfford) AsteroidsGreen.copy(alpha = 0.5f) else AsteroidsGreen.copy(alpha = 0.15f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(2.dp))
            .clickable(enabled = canAfford) { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = borderColor, fontFamily = AsteroidsFont, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text(
            text = "$fromText  →  $toText",
            color = borderColor.copy(alpha = 0.8f),
            fontFamily = AsteroidsFont,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun CostDisplay(cost: BuildCost, state: GameState) {
    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Text("COST", color = AsteroidsGreen.copy(alpha = 0.45f), fontFamily = AsteroidsFont, fontSize = 9.sp, letterSpacing = 2.sp)
        if (cost.iron > 0)     CostLine("${formatNumber(cost.iron)} IRON",     state.iron >= cost.iron)
        if (cost.quartz > 0)   CostLine("${formatNumber(cost.quartz)} QUARTZ",  state.quartz >= cost.quartz)
        if (cost.titanium > 0) CostLine("${formatNumber(cost.titanium)} TITANIUM", state.titanium >= cost.titanium)
        if (cost.energy > 0)   CostLine("${formatNumber(cost.energy)} ENERGY",  state.energy >= cost.energy)
        if (cost.iridium > 0)  CostLine("${formatNumber(cost.iridium)} IRIDIUM", state.iridium >= cost.iridium)
        if (cost.xenon > 0)    CostLine("${formatNumber(cost.xenon)} XENON",    state.xenon >= cost.xenon)
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

@Composable
fun TreeTab(state: GameState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── BOOT ──────────────────────────────────────────────────────────────
        TNode("PICKAXE", "Manual strike mining", "ACTIVE", true)
        TVLine(true)
        TNode("DRILL HEAD LV.1", "1 iron/sec", "BUY IN SHOP", state.drillHeadLevel >= 1)
        TVLine(state.drillHeadLevel >= 1)
        TNode("DRILL HEAD LV.2", "3 iron/sec  +  quartz", "NEED DRILL LV.1", state.drillHeadLevel >= 2)

        // ── ENERGY BRANCH (build either or both) ──────────────────────────────
        TSectionHeader("ENERGY GRID  —  build any combination")
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TBranchNode("POWER CORE", "3 energy/sec\nupkeep: 1 quartz/sec", "NEED DRILL LV.2", state.powerCoreLevel >= 1, Modifier.weight(1f))
            Text("AND\n/OR", color = AsteroidsGreen.copy(alpha = 0.3f), fontFamily = AsteroidsFont, fontSize = 8.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.align(Alignment.CenterVertically))
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                TBranchNode("SOLAR ARRAY LV.1", "1 energy/sec\nno upkeep", "NEED POWER CORE", state.solarArrayLevel >= 1, Modifier.fillMaxWidth())
                TVLine(state.solarArrayLevel >= 1)
                TBranchNode("SOLAR ARRAY LV.2", "2 energy/sec\nno upkeep", "NEED SOLAR LV.1", state.solarArrayLevel >= 2, Modifier.fillMaxWidth())
            }
        }

        TVLine(state.drillHeadLevel >= 2)
        TNode("DRILL HEAD LV.3", "9 iron/sec", "NEED DRILL LV.2 + 80 energy", state.drillHeadLevel >= 3)
        TVLine(state.drillHeadLevel >= 3)
        TNode("DEEP SHAFT", "0.5 titanium/sec", "NEED DRILL LV.3", state.deepShaftLevel >= 1)

        // ── OPTIONAL SIDEGRADE ────────────────────────────────────────────────
        Spacer(Modifier.height(6.dp))
        TOptional("REFINERY", "Convert resources downward", "NEED DRILL LV.3", state.hasRefinery)
        Spacer(Modifier.height(6.dp))

        // ── LAUNCH SILO — choose one path ─────────────────────────────────────
        TSectionHeader("LAUNCH SILO  —  choose one path")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                TBranchNode("PATH A", "28K iron\n+ 500 titanium\nfaster, needs deep shaft", "NEED DEEP SHAFT", state.hasLaunchSilo, Modifier.fillMaxWidth())
            }
            Text("OR", color = AsteroidsGreen.copy(alpha = 0.3f), fontFamily = AsteroidsFont, fontSize = 8.sp,
                modifier = Modifier.align(Alignment.CenterVertically))
            Column(modifier = Modifier.weight(1f)) {
                TBranchNode("PATH B", "15K iron\n+ 2K quartz\nno titanium needed", "NEED DRILL LV.3", state.hasLaunchSilo, Modifier.fillMaxWidth())
            }
        }

        TVLine(state.hasLaunchSilo)
        TNode("RELAY SATELLITE", "Orbital communications", "NEED LAUNCH SILO", state.hasRelaySatellite)
        TVLine(state.hasRelaySatellite)
        TNode("ORBITAL LAB", "Advanced research", "NEED RELAY + 2K iridium", state.hasOrbitalLab)

        // ── ORBITAL BRANCH (build either or both) ─────────────────────────────
        TSectionHeader("ORBITAL  —  build either or both")
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TBranchNode("ASTEROID MINER", "0.05 xenon/sec\nproduces xenon for core path", "NEED ORBITAL LAB", state.hasAsteroidMiner, Modifier.weight(1f))
            Text("AND\n/OR", color = AsteroidsGreen.copy(alpha = 0.3f), fontFamily = AsteroidsFont, fontSize = 8.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.align(Alignment.CenterVertically))
            TBranchNode("ORBITAL SOLAR", "20 energy/sec\nno upkeep\nneeds Solar LV.2", "NEED LAB\n+ SOLAR LV.2", state.hasOrbitalSolarStation, Modifier.weight(1f))
        }

        // ── CORE PATH ─────────────────────────────────────────────────────────
        TSectionHeader("CORE PATH  —  requires Drill LV.4 + xenon")
        TNode("DRILL HEAD LV.4", "27 iron/sec", "NEED DRILL LV.3 + 1K titan", state.drillHeadLevel >= 4)
        TVLine(state.drillHeadLevel >= 4)
        TNode("DEEP SHAFT LV.2", "0.15 iridium/sec", "NEED DRILL LV.4", state.deepShaftLevel >= 2)
        TVLine(state.hasAsteroidMiner)
        TNode("CORE TAP", "500 xenon to build", "NEED DRILL LV.4 + xenon", state.hasCoreTap)
        TVLine(state.hasCoreTap)
        TNode("PLANET CORE", "2K xenon to build", "NEED CORE TAP", state.hasPlanetCore)
        TVLine(state.hasPlanetCore)
        TNode("ASCEND", "Reset  →  +1 Stellar Shard", "NEED PLANET CORE", state.stellarShards > 0)
        TVLine(state.stellarShards > 0)
        TNode("STELLAR SHARDS", "Permanent prestige currency", "ASCEND FIRST", state.stellarShards > 0)
    }
}

@Composable
private fun TNode(label: String, detail: String, hint: String, unlocked: Boolean) {
    val color = if (unlocked) AsteroidsGreen else AsteroidsGreen.copy(alpha = 0.22f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, color, RoundedCornerShape(2.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(label, color = color, fontFamily = AsteroidsFont, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
            Text(detail, color = color.copy(alpha = 0.6f), fontFamily = AsteroidsFont, fontSize = 9.sp)
        }
        Text(if (unlocked) "ACTIVE" else hint, color = color.copy(alpha = 0.75f), fontFamily = AsteroidsFont, fontSize = 8.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.End)
    }
}

@Composable
private fun TBranchNode(label: String, detail: String, hint: String, unlocked: Boolean, modifier: Modifier = Modifier) {
    val color = if (unlocked) AsteroidsGreen else AsteroidsGreen.copy(alpha = 0.22f)
    Column(
        modifier = modifier
            .border(1.dp, color, RoundedCornerShape(2.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(label, color = color, fontFamily = AsteroidsFont, fontWeight = FontWeight.Bold, fontSize = 10.sp)
        Text(detail, color = color.copy(alpha = 0.6f), fontFamily = AsteroidsFont, fontSize = 8.sp, lineHeight = 12.sp)
        Text(if (unlocked) "ACTIVE" else hint, color = color.copy(alpha = 0.7f), fontFamily = AsteroidsFont, fontSize = 7.sp, lineHeight = 10.sp)
    }
}

@Composable
private fun TOptional(label: String, detail: String, hint: String, unlocked: Boolean) {
    val color = if (unlocked) AsteroidsGreen.copy(alpha = 0.75f) else AsteroidsGreen.copy(alpha = 0.18f)
    Row(
        modifier = Modifier
            .fillMaxWidth(0.82f)
            .border(1.dp, color, RoundedCornerShape(2.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("OPTIONAL", color = color.copy(alpha = 0.55f), fontFamily = AsteroidsFont, fontSize = 7.sp, letterSpacing = 1.sp)
                Text(label, color = color, fontFamily = AsteroidsFont, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
            Text(detail, color = color.copy(alpha = 0.6f), fontFamily = AsteroidsFont, fontSize = 9.sp)
        }
        Text(if (unlocked) "ACTIVE" else hint, color = color.copy(alpha = 0.7f), fontFamily = AsteroidsFont, fontSize = 8.sp)
    }
}

@Composable
private fun TVLine(lit: Boolean) {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(20.dp)
            .background(AsteroidsGreen.copy(alpha = if (lit) 0.5f else 0.12f))
    )
}

@Composable
private fun TSectionHeader(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.weight(1f).height(1.dp).background(AsteroidsGreen.copy(alpha = 0.18f)))
        Text(
            text = "  $text  ",
            color = AsteroidsGreen.copy(alpha = 0.45f),
            fontFamily = AsteroidsFont,
            fontSize = 8.sp,
            letterSpacing = 1.sp
        )
        Box(Modifier.weight(1f).height(1.dp).background(AsteroidsGreen.copy(alpha = 0.18f)))
    }
}
