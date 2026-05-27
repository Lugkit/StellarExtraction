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
import kotlinx.coroutines.delay
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
    onBuyQuartzVein: () -> Unit,
    onBuyTitaniumShaft: () -> Unit,
    onBuyIridiumDeposit: () -> Unit,
    onBuyXenonExtractor: () -> Unit,
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
    onBuyOrbitalBeacon: () -> Unit,
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
        SectionLabel("MINING")
        MineUpgradeCard(
            name             = "DRILL HEAD",
            resourceLabel    = "iron",
            level            = state.drillHeadLevel,
            productionPerSec = state.ironPerSec,
            nextLevelCost    = drillHeadNextCost(state.drillHeadLevel),
            state            = state,
            onClick          = onBuyDrillHead
        )
        if (state.drillHeadLevel >= 2) {
            MineUpgradeCard(
                name             = "QUARTZ VEIN",
                resourceLabel    = "quartz",
                level            = state.quartzVeinLevel,
                productionPerSec = quartzVeinQuartzRate(state.quartzVeinLevel),
                nextLevelCost    = quartzVeinNextCost(state.quartzVeinLevel),
                state            = state,
                onClick          = onBuyQuartzVein
            )
        }

        // ── ENERGY ────────────────────────────────────────────────────────────
        if (state.drillHeadLevel >= 2) {
            SectionLabel("ENERGY")

            if (state.powerCoreLevel < 1) {
                UpgradeCard(
                    name        = "POWER GENERATOR",
                    description = "3 energy/sec  •  upkeep: 1 quartz/sec",
                    levelLabel  = "BUILD",
                    cost        = state.effectivePowerCoreCost(),
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
        if (state.drillHeadLevel >= 3 || state.deepShaftLevel >= 1) {
            SectionLabel("DEEP MINING")
            if (state.drillHeadLevel >= 3 && state.deepShaftLevel < 1) {
                UpgradeCard(
                    name        = "DEEP MINES",
                    description = "0.5 titanium/sec",
                    levelLabel  = "BUILD",
                    cost        = deepShaftCosts[1]!!,
                    state       = state,
                    onClick     = onBuyDeepShaft
                )
            }
            if (state.deepShaftLevel >= 1 && state.deepShaftLevel < 2 && state.drillHeadLevel >= 4) {
                UpgradeCard(
                    name        = "DEEPER MINES",
                    description = "0.15 iridium/sec",
                    levelLabel  = "UPGRADE",
                    cost        = deepShaftCosts[2]!!,
                    state       = state,
                    onClick     = onBuyDeepShaft
                )
            }
            if (state.deepShaftLevel >= 1) {
                MineUpgradeCard(
                    name             = "TITANIUM SHAFT",
                    resourceLabel    = "titanium",
                    level            = state.titaniumShaftLevel,
                    productionPerSec = titaniumShaftTitaniumRate(state.titaniumShaftLevel),
                    nextLevelCost    = titaniumShaftNextCost(state.titaniumShaftLevel),
                    state            = state,
                    onClick          = onBuyTitaniumShaft
                )
            }
            if (state.deepShaftLevel >= 2) {
                MineUpgradeCard(
                    name             = "IRIDIUM DEPOSIT",
                    resourceLabel    = "iridium",
                    level            = state.iridiumDepositLevel,
                    productionPerSec = iridiumDepositIridiumRate(state.iridiumDepositLevel),
                    nextLevelCost    = iridiumDepositNextCost(state.iridiumDepositLevel),
                    state            = state,
                    onClick          = onBuyIridiumDeposit
                )
            }
        }

        // ── REFINERY (optional sidegrade) ─────────────────────────────────────
        if (state.drillHeadLevel >= 3 && !state.hasRefinery) {
            SectionLabel("REFINERY")
            UpgradeCard(
                name        = "REFINERY",
                description = "Convert resources downward — controls on mine screen",
                levelLabel  = "BUILD",
                cost        = state.effectiveRefineryCost(),
                state       = state,
                onClick     = onBuyRefinery
            )
        }

        // ── ORBITAL ───────────────────────────────────────────────────────────
        val anyOrbital = (!state.hasLaunchSilo && state.drillHeadLevel >= 3) ||
            (!state.hasRelaySatellite && state.hasLaunchSilo) ||
            (!state.hasOrbitalLab && state.hasRelaySatellite) ||
            (!state.hasAsteroidMiner && state.hasOrbitalLab) ||
            state.hasAsteroidMiner
        if (anyOrbital) SectionLabel("ORBITAL")

        if (!state.hasLaunchSilo && state.drillHeadLevel >= 3) {
            if (state.deepShaftLevel >= 1) {
                UpgradeCard(
                    name        = "LAUNCH SILO",
                    description = "Path A — titanium-heavy",
                    levelLabel  = "BUILD",
                    cost        = state.effectiveLaunchSiloCostA(),
                    state       = state,
                    onClick     = onBuyLaunchSiloA
                )
            }
            UpgradeCard(
                name        = "LAUNCH SILO",
                description = "Path B — no titanium required",
                levelLabel  = "BUILD",
                cost        = state.effectiveLaunchSiloCostB(),
                state       = state,
                onClick     = onBuyLaunchSiloB
            )
        }

        if (!state.hasRelaySatellite && state.hasLaunchSilo) {
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
            UpgradeCard(
                name        = "ASTEROID MINER",
                description = "0.05 xenon/sec",
                levelLabel  = "DEPLOY",
                cost        = asteroidMinerCost,
                state       = state,
                onClick     = onBuyAsteroidMiner
            )
        }
        if (state.hasAsteroidMiner) {
            MineUpgradeCard(
                name             = "XENON EXTRACTOR",
                resourceLabel    = "xenon",
                level            = state.xenonExtractorLevel,
                productionPerSec = xenonExtractorXenonRate(state.xenonExtractorLevel),
                nextLevelCost    = xenonExtractorNextCost(state.xenonExtractorLevel),
                state            = state,
                onClick          = onBuyXenonExtractor
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
        if (state.hasPlanetCore && state.hasOrbitalLab) {
            SectionLabel("PRESTIGE")
            if (!state.hasOrbitalBeacon) {
                OrbitalBeaconCard(state = state, onBuy = onBuyOrbitalBeacon)
            } else {
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
private fun MineUpgradeCard(
    name: String,
    resourceLabel: String,
    level: Int,
    productionPerSec: Double,
    nextLevelCost: BuildCost,
    state: GameState,
    onClick: () -> Unit
) {
    val canAfford = state.canAfford(nextLevelCost)
    val borderColor = if (canAfford) AsteroidsGreen else AsteroidsGreen.copy(alpha = 0.2f)
    val progress = (1f - 1f / (1f + level * 0.04f)).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(2.dp))
            .clickable(enabled = canAfford) { onClick() }
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(name, color = borderColor, fontFamily = AsteroidsFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 1.sp)
            Text(if (level == 0) "BUILD" else "LV.$level", color = borderColor.copy(alpha = 0.7f), fontFamily = AsteroidsFont, fontSize = 11.sp)
        }
        val rateStr = if (productionPerSec > 0) formatRate(productionPerSec) else "0"
        Text("$rateStr $resourceLabel/sec", color = borderColor.copy(alpha = 0.55f), fontFamily = AsteroidsFont, fontSize = 11.sp)
        CostDisplay(cost = nextLevelCost, state = state)
        Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(AsteroidsGreen.copy(alpha = 0.08f))) {
            if (progress > 0f) {
                Box(modifier = Modifier.fillMaxWidth(fraction = progress).height(2.dp).background(AsteroidsGreen.copy(alpha = 0.28f)))
            }
        }
    }
}

@Composable
private fun OrbitalBeaconCard(state: GameState, onBuy: () -> Unit) {
    val buildSecs = beaconBuildSeconds(state.stellarShards)
    val isBuilding = state.beaconCompleteAt > 0L && !state.hasOrbitalBeacon
    var now by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(isBuilding) {
        if (isBuilding) {
            while (true) { delay(1000L); now = System.currentTimeMillis() }
        }
    }

    if (!isBuilding) {
        UpgradeCard(
            name        = "ORBITAL BEACON",
            description = "Required for ascension  •  ${formatDuration(buildSecs)} build time",
            levelLabel  = "BUILD",
            cost        = orbitalBeaconCost,
            state       = state,
            onClick     = onBuy
        )
    } else {
        val remainingSecs = ((state.beaconCompleteAt - now) / 1000L).coerceAtLeast(0L)
        val progress = (1f - remainingSecs.toFloat() / buildSecs.toFloat()).coerceIn(0f, 1f)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, AsteroidsGreen, RoundedCornerShape(2.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("ORBITAL BEACON", color = AsteroidsGreen, fontFamily = AsteroidsFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 1.sp)
                Text("BUILDING...", color = AsteroidsGreen.copy(alpha = 0.6f), fontFamily = AsteroidsFont, fontSize = 11.sp)
            }
            Text(
                text = "${formatDuration(remainingSecs)} remaining",
                color = AsteroidsGreen.copy(alpha = 0.75f),
                fontFamily = AsteroidsFont,
                fontSize = 12.sp
            )
            Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(AsteroidsGreen.copy(alpha = 0.12f))) {
                Box(modifier = Modifier.fillMaxWidth(fraction = progress).height(4.dp).background(AsteroidsGreen.copy(alpha = 0.6f)))
            }
        }
    }
}

private fun formatDuration(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return "%d:%02d:%02d".format(h, m, s)
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
        TNode("DRILLING RIG", "1 iron/sec", "BUY IN SHOP", state.drillHeadLevel >= 1)
        TVLine(state.drillHeadLevel >= 1)
        TNode("DRILL HEAD LV.1", "3 iron/sec  +  quartz", "NEED DRILLING RIG", state.drillHeadLevel >= 2)
        TVLine(state.drillHeadLevel >= 2)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TBranchNode("POWER GENERATOR", "3 energy/sec\nupkeep: 1 quartz/sec", "NEED DRILL LV.1", state.powerCoreLevel >= 1, Modifier.weight(1f))
            Text("AND\n/OR", color = AsteroidsGreen.copy(alpha = 0.3f), fontFamily = AsteroidsFont, fontSize = 8.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.align(Alignment.CenterVertically))
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                TBranchNode("SOLAR ARRAY LV.1", "1 energy/sec\nno upkeep", "NEED POWER GEN", state.solarArrayLevel >= 1, Modifier.fillMaxWidth())
                TVLine(state.solarArrayLevel >= 1)
                TBranchNode("SOLAR ARRAY LV.2", "2 energy/sec\nno upkeep", "NEED SOLAR LV.1", state.solarArrayLevel >= 2, Modifier.fillMaxWidth())
            }
        }

        TVLine(state.drillHeadLevel >= 2)
        TNode("DRILL HEAD LV.2", "9 iron/sec", "NEED DRILL LV.1 + 80 energy", state.drillHeadLevel >= 3)
        TVLine(state.drillHeadLevel >= 3)
        TNode("DEEP MINES", "0.5 titanium/sec", "NEED DRILL LV.2", state.deepShaftLevel >= 1)

        Spacer(Modifier.height(6.dp))
        TOptional("REFINERY", "Convert resources downward", "NEED DRILL LV.2", state.hasRefinery)
        Spacer(Modifier.height(6.dp))
        TVLine(state.deepShaftLevel >= 1)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                TBranchNode("SILO", "15K iron\n+ 2K quartz\nno titanium needed", "NEED DRILL LV.2", state.hasLaunchSilo, Modifier.fillMaxWidth())
            }
            Text("OR", color = AsteroidsGreen.copy(alpha = 0.3f), fontFamily = AsteroidsFont, fontSize = 8.sp,
                modifier = Modifier.align(Alignment.CenterVertically))
            Column(modifier = Modifier.weight(1f)) {
                TBranchNode("POSH SILO", "28K iron\n+ 500 titanium\nneeds deep mines", "NEED DEEP MINES", state.hasLaunchSilo, Modifier.fillMaxWidth())
            }
        }

        TVLine(state.hasLaunchSilo)
        TNode("RELAY SATELLITE", "Orbital communications", "NEED LAUNCH SILO", state.hasRelaySatellite)
        TVLine(state.hasRelaySatellite)
        TNode("ORBITAL LAB", "Advanced research", "NEED RELAY + 2K iridium", state.hasOrbitalLab)
        TVLine(state.hasOrbitalLab)
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

        TVLine(state.drillHeadLevel >= 3)
        TNode("DRILL HEAD LV.3", "27 iron/sec", "NEED DRILL LV.2 + 1K titan", state.drillHeadLevel >= 4)
        TVLine(state.drillHeadLevel >= 4)
        TNode("DEEPER MINES", "0.15 iridium/sec", "NEED DRILL LV.3", state.deepShaftLevel >= 2)
        TVLine(state.hasAsteroidMiner)
        TNode("CORE TAP", "500 xenon to build", "NEED DRILL LV.3 + xenon", state.hasCoreTap)
        TVLine(state.hasCoreTap)
        TNode("PLANET CORE", "2K xenon to build", "NEED CORE TAP", state.hasPlanetCore)
        TVLine(state.hasPlanetCore)
        TNode("ASCEND", "Reset  →  +1 Stellar Shard", "NEED PLANET CORE", state.hasPlanetCore)
        TVLine(state.hasPlanetCore)
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
