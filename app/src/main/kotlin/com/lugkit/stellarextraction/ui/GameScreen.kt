package com.lugkit.stellarextraction.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lugkit.stellarextraction.GameState
import com.lugkit.stellarextraction.GameViewModel
import com.lugkit.stellarextraction.Resource
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private enum class Screen { MINE, SHOP, TREE, ASC }

@Composable
fun GameScreen(vm: GameViewModel) {
    val state by vm.state.collectAsState()
    var screen by remember { mutableStateOf(Screen.MINE) }

    BackHandler(enabled = screen != Screen.MINE) { screen = Screen.MINE }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Content area
        Box(modifier = Modifier.weight(1f)) {
            when (screen) {
                Screen.MINE -> MineContent(vm = vm)
                Screen.SHOP -> ShopContent(vm = vm)
                Screen.TREE -> TreeContent(state = state)
                Screen.ASC  -> AscContent(vm = vm)
            }
        }

        // Persistent bottom nav
        HRule()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
        ) {
            if (screen != Screen.MINE) NavLink("MINE") { screen = Screen.MINE }
            if (screen != Screen.SHOP) NavLink("SHOP") { screen = Screen.SHOP }
            if (screen != Screen.TREE) NavLink("TREE") { screen = Screen.TREE }
            if (screen != Screen.ASC)  NavLink("SHARDS") { screen = Screen.ASC  }
        }
    }
}

// ── MINE screen ───────────────────────────────────────────────────────────────

@Composable
private fun MineContent(vm: GameViewModel) {
    val state by vm.state.collectAsState()
    var showResetDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Title bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF050505))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STELLAR EXTRACTION",
                    color = AsteroidsGreen,
                    fontFamily = AsteroidsFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 3.sp
                )
                Text(
                    text = "RESET",
                    color = AsteroidsGreen.copy(alpha = 0.45f),
                    fontFamily = AsteroidsFont,
                    fontSize = 10.sp,
                    letterSpacing = 2.sp,
                    modifier = Modifier.clickable { showResetDialog = true }
                )
            }
            HRule()

            // Planet centered, resources overlaid left
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Planet(onClick = vm::strike, state = state)
                        Text(
                            text = "[ STRIKE ]",
                            color = AsteroidsGreen.copy(alpha = 0.4f),
                            fontFamily = AsteroidsFont,
                            fontSize = 10.sp,
                            letterSpacing = 3.sp
                        )
                    }
                }

                // Resources overlaid on left, split above/below planet
                Column(
                    modifier = Modifier
                        .width(96.dp)
                        .fillMaxHeight()
                        .padding(start = 12.dp, top = 14.dp, bottom = 14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        ResourceItem("IRON",   state.iron,   state.ironPerSec) { vm.focusedStrike(Resource.IRON) }
                        if (state.quartzVisible)
                            ResourceItem("QUARTZ", state.quartz, state.quartzPerSec) { vm.focusedStrike(Resource.QUARTZ) }
                        if (state.energyVisible)
                            ResourceItem("ENERGY", state.energy, state.energyPerSec) { vm.focusedStrike(Resource.ENERGY) }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (state.titaniumVisible)
                            ResourceItem("TITAN",   state.titanium, state.titaniumPerSec) { vm.focusedStrike(Resource.TITANIUM) }
                        if (state.iridiumVisible)
                            ResourceItem("IRIDIUM", state.iridium,  state.iridiumPerSec)  { vm.focusedStrike(Resource.IRIDIUM) }
                        if (state.xenonVisible)
                            ResourceItem("XENON",   state.xenon,    state.xenonPerSec)    { vm.focusedStrike(Resource.XENON) }
                        if (state.stellarShardsVisible)
                            ResourceItem("SHARDS", state.stellarShards.toDouble(), 0.0)
                    }
                }
            }

            // Refinery conversions — shown below planet when refinery is built
            if (state.hasRefinery) {
                HRule()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MineConvertCard("SMELT IRIDIUM",  "1 IRIDIUM",  "3 TITANIUM", state.iridium  >= 1.0) { vm.refineryConvert(Resource.IRIDIUM) }
                    MineConvertCard("SMELT TITANIUM", "1 TITANIUM", "5 QUARTZ",   state.titanium >= 1.0) { vm.refineryConvert(Resource.TITANIUM) }
                    MineConvertCard("SMELT QUARTZ",   "1 QUARTZ",   "5 IRON",     state.quartz   >= 1.0) { vm.refineryConvert(Resource.QUARTZ) }
                }
            }
        }

        if (showResetDialog) {
            ResetDialog(
                onConfirm = { vm.hardReset(); showResetDialog = false },
                onDismiss = { showResetDialog = false }
            )
        }
    }
}

// ── SHOP screen ───────────────────────────────────────────────────────────────

@Composable
private fun ShopContent(vm: GameViewModel) {
    val state by vm.state.collectAsState()
    Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        ScreenHeader("SHOP")
        Box(modifier = Modifier.weight(1f)) {
            ShopTab(
                state                    = state,
                onBuyDrillHead           = vm::buyDrillHead,
                onBuyPowerCore           = vm::buyPowerCore,
                onBuySolarArray          = vm::buySolarArray,
                onBuyDeepShaft           = vm::buyDeepShaft,
                onBuyRefinery            = vm::buyRefinery,
                onBuyLaunchSiloA         = vm::buyLaunchSiloA,
                onBuyLaunchSiloB         = vm::buyLaunchSiloB,
                onBuyRelaySatellite      = vm::buyRelaySatellite,
                onBuyOrbitalLab          = vm::buyOrbitalLab,
                onBuyAsteroidMiner       = vm::buyAsteroidMiner,
                onBuyOrbitalSolarStation = vm::buyOrbitalSolarStation,
                onBuyCoreTap             = vm::buyCoreTap,
                onBuyPlanetCore          = vm::buyPlanetCore,
                onAscend                 = vm::ascend,
                onRefineryConvert        = vm::refineryConvert
            )
        }
    }
}

// ── TREE screen ───────────────────────────────────────────────────────────────

@Composable
private fun TreeContent(state: GameState) {
    Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        ScreenHeader("TECH TREE")
        Box(modifier = Modifier.weight(1f)) {
            TreeTab(state)
        }
    }
}

// ── Shared components ─────────────────────────────────────────────────────────

@Composable
private fun ScreenHeader(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF050505))
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
    HRule()
}

@Composable
private fun ResetDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.88f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.78f)
                .border(width = 1.dp, color = AsteroidsGreen.copy(alpha = 0.6f), shape = RoundedCornerShape(2.dp))
                .background(Color.Black)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("RESET", color = AsteroidsGreen, fontFamily = AsteroidsFont, fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 4.sp)
            Text(
                text = "All progress will be lost.\nThis cannot be undone.",
                color = AsteroidsGreen.copy(alpha = 0.55f),
                fontFamily = AsteroidsFont,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                NavLink(label = "CONFIRM", onClick = onConfirm)
                NavLink(label = "CANCEL",  onClick = onDismiss)
            }
        }
    }
}

@Composable
private fun MineConvertCard(label: String, fromText: String, toText: String, canAfford: Boolean, onClick: () -> Unit) {
    val borderColor = if (canAfford) AsteroidsGreen.copy(alpha = 0.5f) else AsteroidsGreen.copy(alpha = 0.15f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, androidx.compose.foundation.shape.RoundedCornerShape(2.dp))
            .clickable(enabled = canAfford) { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = borderColor, fontFamily = AsteroidsFont, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text("$fromText  →  $toText", color = borderColor.copy(alpha = 0.8f), fontFamily = AsteroidsFont, fontSize = 10.sp)
    }
}

@Composable
fun NavLink(label: String, active: Boolean = false, onClick: () -> Unit) {
    val borderAlpha = if (active) 0.85f else 0.3f
    val textAlpha   = if (active) 1.00f else 0.7f
    Text(
        text = label,
        color = AsteroidsGreen.copy(alpha = textAlpha),
        fontFamily = AsteroidsFont,
        fontSize = 12.sp,
        letterSpacing = 2.sp,
        modifier = Modifier
            .border(width = 1.dp, color = AsteroidsGreen.copy(alpha = borderAlpha), shape = RoundedCornerShape(2.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp)
    )
}

@Composable
fun ResourceItem(label: String, amount: Double, rate: Double, onTap: (() -> Unit)? = null) {
    var tapped by remember { mutableStateOf(false) }
    val labelAlpha by animateFloatAsState(
        targetValue = if (tapped) 1f else 0.45f,
        animationSpec = tween(durationMillis = if (tapped) 50 else 400),
        finishedListener = { if (tapped) tapped = false },
        label = "resourceFlash"
    )

    Column(
        modifier = if (onTap != null) Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) { tapped = true; onTap() } else Modifier,
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        Text(text = label, color = AsteroidsGreen.copy(alpha = labelAlpha), fontFamily = AsteroidsFont, fontSize = 11.sp, letterSpacing = 1.sp)
        Text(text = formatNumber(amount), color = AsteroidsGreen, fontFamily = AsteroidsFont, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        if (rate > 0) {
            Text(text = "+${formatRate(rate)}/s", color = AsteroidsGreen.copy(alpha = 0.5f), fontFamily = AsteroidsFont, fontSize = 8.sp)
        }
    }
}

@Composable
fun HRule() {
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(AsteroidsGreen.copy(alpha = 0.2f)))
}

@Composable
fun Planet(onClick: () -> Unit, state: GameState) {
    var tapped by remember { mutableStateOf(false) }
    val glow by animateFloatAsState(
        targetValue = if (tapped) 1f else 0.4f,
        animationSpec = tween(durationMillis = if (tapped) 60 else 500),
        finishedListener = { if (tapped) tapped = false },
        label = "planetGlow"
    )

    Canvas(
        modifier = Modifier
            .size(200.dp)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                tapped = true; onClick()
            }
    ) {
        val cx = size.width / 2
        val cy = size.height / 2
        val r  = size.minDimension / 2 * 0.82f

        drawCircle(color = AsteroidsGreen.copy(alpha = glow), radius = r, center = Offset(cx, cy), style = Stroke(width = 2.5f))

        for (lat in listOf(-0.5f, 0f, 0.5f)) {
            val latY = cy + lat * r
            val latR = sqrt(max(0f, r * r - (lat * r).pow(2)))
            val h = latR * 0.28f
            drawOval(color = AsteroidsGreen.copy(alpha = glow * 0.4f), topLeft = Offset(cx - latR, latY - h), size = Size(latR * 2, h * 2), style = Stroke(width = 1.5f))
        }

        for (angle in listOf(0f, 40f, -40f)) {
            rotate(degrees = angle) {
                drawOval(color = AsteroidsGreen.copy(alpha = glow * 0.4f), topLeft = Offset(cx - r * 0.18f, cy - r), size = Size(r * 0.36f, r * 2), style = Stroke(width = 1.5f))
            }
        }

        drawBuildings(state, cx, cy, r)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBuildings(
    state: GameState, cx: Float, cy: Float, r: Float
) {
    val col = AsteroidsGreen.copy(alpha = 0.75f)
    val sw  = 1.5f
    val s   = r * 0.12f

    // Shuffle pre-defined slots so buildings land in different spots each run.
    // Slots are spread far enough apart that buildings never overlap.
    val rng1 = kotlin.random.Random(state.buildingSeed)
    val rng2 = kotlin.random.Random(state.buildingSeed + 1L)
    val rng3 = kotlin.random.Random(state.buildingSeed + 2L)
    // 8 surface slots ≥ 40° apart, 4 orbital angle slots 90° apart
    val sa = listOf(15f, 60f, 105f, 155f, 200f, 250f, 295f, 340f).shuffled(rng1)
    val oa = listOf(30f, 120f, 210f, 300f).shuffled(rng2)
    val od = listOf(1.42f, 1.52f, 1.62f, 1.48f).shuffled(rng3)

    fun p(deg: Float, dist: Float = 1f): Offset {
        val rad = Math.toRadians(deg.toDouble())
        return Offset(cx + cos(rad).toFloat() * r * dist, cy + sin(rad).toFloat() * r * dist)
    }
    fun n(deg: Float) = Offset(cos(Math.toRadians(deg.toDouble())).toFloat(), sin(Math.toRadians(deg.toDouble())).toFloat())
    fun t(deg: Float) = Offset(-sin(Math.toRadians(deg.toDouble())).toFloat(), cos(Math.toRadians(deg.toDouble())).toFloat())

    // ── Drill Head → sa[0] ────────────────────────────────────────────────────
    if (state.drillHeadLevel >= 1) {
        val bp = p(sa[0]); val nv = n(sa[0]); val tv = t(sa[0])
        val tip = bp - nv * s
        val bl  = bp + tv*(s*0.5f) + nv*(s*0.3f)
        val br  = bp - tv*(s*0.5f) + nv*(s*0.3f)
        drawLine(col, tip, bl, sw); drawLine(col, bl, br, sw); drawLine(col, br, tip, sw)
        if (state.drillHeadLevel >= 2) drawLine(col, bp + tv*(s*0.4f), bp - tv*(s*0.4f), sw)
        if (state.drillHeadLevel >= 3) drawLine(col, bp + nv*(s*0.5f) + tv*(s*0.35f), bp + nv*(s*0.5f) - tv*(s*0.35f), sw)
        if (state.drillHeadLevel >= 4) { val t2 = bp + nv*(s*0.8f); drawLine(col, t2 + tv*(s*0.25f), t2 - tv*(s*0.25f), sw) }
    }

    // ── Power Core → sa[1] ────────────────────────────────────────────────────
    if (state.powerCoreLevel >= 1) {
        val center = p(sa[1]) + n(sa[1]) * (s*0.3f)
        drawCircle(col, radius = s*0.45f, center = center, style = Stroke(sw))
        drawLine(col, center - n(sa[1])*(s*0.35f), center + n(sa[1])*(s*0.35f), sw)
        drawLine(col, center - t(sa[1])*(s*0.35f), center + t(sa[1])*(s*0.35f), sw)
    }

    // ── Solar Array → sa[2] ───────────────────────────────────────────────────
    if (state.solarArrayLevel >= 1) {
        val bp = p(sa[2]); val nv = n(sa[2]); val tv = t(sa[2])
        val tl = bp - tv*(s*0.5f) + nv*(s*0.1f); val tr = bp + tv*(s*0.5f) + nv*(s*0.1f)
        val bl = tl + nv*(s*0.7f); val br = tr + nv*(s*0.7f)
        drawLine(col, tl, tr, sw); drawLine(col, tr, br, sw); drawLine(col, br, bl, sw); drawLine(col, bl, tl, sw)
        drawLine(col, bp + nv*(s*0.45f) - tv*(s*0.5f), bp + nv*(s*0.45f) + tv*(s*0.5f), sw*0.8f)
        if (state.solarArrayLevel >= 2) drawLine(col, bp + nv*(s*0.22f) - tv*(s*0.5f), bp + nv*(s*0.22f) + tv*(s*0.5f), sw*0.8f)
    }

    // ── Deep Shaft → sa[3] ────────────────────────────────────────────────────
    if (state.deepShaftLevel >= 1) {
        val bp = p(sa[3]); val nv = n(sa[3]); val tv = t(sa[3])
        val depth = if (state.deepShaftLevel >= 2) s*0.9f else s*0.55f
        val tl = bp - tv*(s*0.2f); val tr = bp + tv*(s*0.2f)
        val bl = bp - nv*depth - tv*(s*0.2f); val br = bp - nv*depth + tv*(s*0.2f)
        drawLine(col, tl, tr, sw); drawLine(col, tr, br, sw); drawLine(col, br, bl, sw); drawLine(col, bl, tl, sw)
        if (state.deepShaftLevel >= 2) drawLine(col, bl + tv*(s*0.2f), br - tv*(s*0.2f), sw*0.7f)
    }

    // ── Launch Silo → sa[4] ───────────────────────────────────────────────────
    if (state.hasLaunchSilo) {
        val bp = p(sa[4]); val nv = n(sa[4]); val tv = t(sa[4])
        val w = s*0.3f; val h = s*0.8f
        val tl = bp + tv*w + nv*(s*0.1f); val tr = bp - tv*w + nv*(s*0.1f)
        val bl = bp + tv*w + nv*(h+s*0.1f); val br = bp - tv*w + nv*(h+s*0.1f)
        drawLine(col, tl, bl, sw); drawLine(col, tr, br, sw); drawLine(col, bl, br, sw)
        val tip2 = bp + nv*(h+s*0.55f); drawLine(col, tl, tip2, sw); drawLine(col, tr, tip2, sw)
    }

    // ── Core Tap → sa[5] ──────────────────────────────────────────────────────
    if (state.hasCoreTap) {
        val bp = p(sa[5]); val nv = n(sa[5]); val tv = t(sa[5])
        val hs = s*0.4f
        drawLine(col, bp + tv*hs + nv*hs, bp - tv*hs - nv*hs, sw)
        drawLine(col, bp - tv*hs + nv*hs, bp + tv*hs - nv*hs, sw)
    }

    // ── Planet Core → sa[6] (slightly inside surface) ─────────────────────────
    if (state.hasPlanetCore) {
        val center = p(sa[6], 0.9f)
        drawCircle(col, radius = s*0.38f, center = center, style = Stroke(sw))
        drawCircle(col.copy(alpha = 0.35f), radius = s*0.18f, center = center, style = Stroke(sw))
    }

    // ── Relay Satellite → oa[0], od[0] ───────────────────────────────────────
    if (state.hasRelaySatellite) {
        val center = p(oa[0], od[0]); val nv = n(oa[0]); val tv = t(oa[0])
        val arm = s*0.5f
        drawLine(col, center - nv*arm, center + nv*arm, sw)
        drawLine(col, center - tv*arm, center + tv*arm, sw)
        val sq = s*0.13f
        for (end in listOf(center+nv*arm, center-nv*arm, center+tv*arm, center-tv*arm))
            drawRect(col, topLeft = Offset(end.x-sq, end.y-sq), size = Size(sq*2, sq*2), style = Stroke(sw))
    }

    // ── Orbital Lab → oa[1], od[1] ────────────────────────────────────────────
    if (state.hasOrbitalLab) {
        val center = p(oa[1], od[1]); val nv = n(oa[1]); val tv = t(oa[1])
        val hw = s*0.55f; val hh = s*0.3f
        val tl = center - tv*hw - nv*hh; val tr = center + tv*hw - nv*hh
        val bl = center - tv*hw + nv*hh; val br = center + tv*hw + nv*hh
        drawLine(col, tl, tr, sw); drawLine(col, tr, br, sw); drawLine(col, br, bl, sw); drawLine(col, bl, tl, sw)
        drawLine(col, center - tv*hw, center + tv*hw, sw*0.7f)
    }

    // ── Asteroid Miner → oa[2], od[2] ────────────────────────────────────────
    if (state.hasAsteroidMiner) {
        val center = p(oa[2], od[2]); val nv = n(oa[2]); val tv = t(oa[2])
        val pts = listOf(center+nv*(s*0.5f), center+tv*(s*0.45f)+nv*(s*0.1f),
            center+tv*(s*0.25f)-nv*(s*0.4f), center-nv*(s*0.5f), center-tv*(s*0.4f)-nv*(s*0.1f))
        for (i in pts.indices) drawLine(col, pts[i], pts[(i+1) % pts.size], sw)
    }

    // ── Orbital Solar Station → oa[3], od[3] ─────────────────────────────────
    if (state.hasOrbitalSolarStation) {
        val center = p(oa[3], od[3]); val nv = n(oa[3]); val tv = t(oa[3])
        val wingW = s*0.9f; val wingH = s*0.22f
        val tl = center-tv*wingW-nv*wingH; val tr = center+tv*wingW-nv*wingH
        val bl = center-tv*wingW+nv*wingH; val br = center+tv*wingW+nv*wingH
        drawLine(col,tl,tr,sw); drawLine(col,tr,br,sw); drawLine(col,br,bl,sw); drawLine(col,bl,tl,sw)
        val cw = s*0.22f; val ch = s*0.38f
        val ctl = center-tv*cw-nv*ch; val ctr = center+tv*cw-nv*ch
        val cbl = center-tv*cw+nv*ch; val cbr = center+tv*cw+nv*ch
        drawLine(col,ctl,ctr,sw); drawLine(col,ctr,cbr,sw); drawLine(col,cbr,cbl,sw); drawLine(col,cbl,ctl,sw)
        drawLine(col, center-tv*wingW, center-tv*cw, sw*0.6f)
        drawLine(col, center+tv*cw, center+tv*wingW, sw*0.6f)
    }
}

fun formatRate(n: Double): String = when {
    n >= 1_000_000_000 -> "%.1fB".format(n / 1_000_000_000)
    n >= 1_000_000     -> "%.1fM".format(n / 1_000_000)
    n >= 1_000         -> "%.1fK".format(n / 1_000)
    else               -> "%.2f".format(n)
}

fun formatNumber(n: Double): String = when {
    n >= 1_000_000_000 -> "%.1fB".format(n / 1_000_000_000)
    n >= 1_000_000     -> "%.1fM".format(n / 1_000_000)
    n >= 1_000         -> "%.1fK".format(n / 1_000)
    else               -> floor(n).toInt().toString()
}
