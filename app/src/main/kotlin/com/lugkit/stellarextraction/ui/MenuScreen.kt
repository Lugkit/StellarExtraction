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
import com.lugkit.stellarextraction.GameState

@Composable
fun MenuScreen(
    state: GameState,
    onUpgradeDrill: () -> Unit,
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
            0 -> ShopTab(state = state, onUpgradeDrill = onUpgradeDrill)
            1 -> TreeTab(drillLevel = state.drillLevel)
        }
    }
}

@Composable
fun MenuTabButton(label: String, selected: Boolean, onClick: () -> Unit) {
    val borderColor = if (selected) AsteroidsGreen else AsteroidsGreen.copy(alpha = 0.25f)
    val textColor = if (selected) AsteroidsGreen else AsteroidsGreen.copy(alpha = 0.4f)
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
fun ShopTab(state: GameState, onUpgradeDrill: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionLabel("MINING")
        UpgradeCard(
            name = "DRILL UPGRADE",
            description = "Increases passive iron/s",
            levelLabel = "LVL ${state.drillLevel} → ${state.drillLevel + 1}",
            cost = state.drillCost,
            canAfford = state.iron >= state.drillCost,
            onClick = onUpgradeDrill
        )
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
    cost: Double,
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
            text = "COST:  ${formatNumber(cost)} iron",
            color = lineColor.copy(alpha = 0.85f),
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp
        )
    }
}

// ── PROGRESSION TREE ─────────────────────────────────────────────────────────

private data class TreeNode(
    val label: String,
    val detail: String,
    val requiredLevel: Int
)

private val treeNodes = listOf(
    TreeNode("MANUAL MINING",   "Tap planet to harvest iron",       0),
    TreeNode("AUTO DRILL",      "Autonomous drill activates",        1),
    TreeNode("IMPROVED DRILL",  "Enhanced drill array",              5),
    TreeNode("ADVANCED DRILL",  "Industrial-grade extraction",      10),
    TreeNode("QUANTUM DRILL",   "???",                              20)
)

@Composable
fun TreeTab(drillLevel: Int) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        treeNodes.forEachIndexed { index, node ->
            val unlocked = drillLevel >= node.requiredLevel
            TreeNodeItem(node = node, unlocked = unlocked)
            if (index < treeNodes.lastIndex) {
                val nextUnlocked = drillLevel >= treeNodes[index + 1].requiredLevel
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(36.dp)
                        .background(AsteroidsGreen.copy(alpha = if (nextUnlocked) 0.5f else 0.15f))
                )
            }
        }
    }
}

@Composable
private fun TreeNodeItem(node: TreeNode, unlocked: Boolean) {
    val color = if (unlocked) AsteroidsGreen else AsteroidsGreen.copy(alpha = 0.22f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = color, shape = RoundedCornerShape(2.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = node.label,
                color = color,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = 1.sp
            )
            Text(
                text = node.detail,
                color = color.copy(alpha = 0.65f),
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp
            )
        }
        Text(
            text = if (unlocked) "ACTIVE" else "LVL ${node.requiredLevel}",
            color = color.copy(alpha = 0.75f),
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            letterSpacing = 1.sp
        )
    }
}
