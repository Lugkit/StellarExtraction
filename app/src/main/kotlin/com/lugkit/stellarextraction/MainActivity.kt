package com.lugkit.stellarextraction

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lugkit.stellarextraction.ui.*

private data class NavItem(val label: String, val icon: ImageVector)
private val NAV_ITEMS = listOf(
    NavItem("Planet",    Icons.Default.Star),
    NavItem("Transport", Icons.Default.Send),
    NavItem("Station",   Icons.Default.Home),
    NavItem("Opgrader", Icons.Default.Build)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StellarTheme {
                StellarApp()
            }
        }
    }
}

@Composable
fun StellarApp() {
    val vm: GameViewModel = viewModel()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = SpacePanel) {
                NAV_ITEMS.forEachIndexed { i, item ->
                    NavigationBarItem(
                        selected = selectedTab == i,
                        onClick = { selectedTab = i },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SpaceAccent,
                            selectedTextColor = SpaceAccent,
                            indicatorColor = SpaceAccent.copy(alpha = 0.15f),
                            unselectedIconColor = SpaceSubtext,
                            unselectedTextColor = SpaceSubtext
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (selectedTab) {
                0 -> PlanetScreen(vm)
                1 -> TransportScreen(vm)
                2 -> StationScreen(vm)
                3 -> UpgradeScreen(vm)
            }
        }
    }
}
