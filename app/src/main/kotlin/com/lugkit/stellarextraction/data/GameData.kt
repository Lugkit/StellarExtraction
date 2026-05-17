package com.lugkit.stellarextraction.data

import androidx.compose.ui.graphics.Color

data class ResourceInfo(
    val id: String,
    val name: String,
    val symbol: String,
    val color: Color,
    val planetCap: Double,
    val stationCap: Double,
    val baseRate: Double,
    val unlockCost: Map<String, Double> = emptyMap()
)

val RESOURCES = listOf(
    ResourceInfo("iron",             "Jern",            "Fe",  Color(0xFF9E9E9E), 1000.0,  5000.0,  1.0),
    ResourceInfo("titanium",         "Titanium",        "Ti",  Color(0xFF607D8B), 500.0,   2500.0,  0.4,  mapOf("iron" to 200.0)),
    ResourceInfo("crystal",          "Krystal",         "Cr",  Color(0xFF00BCD4), 250.0,   1250.0,  0.15, mapOf("iron" to 500.0, "titanium" to 100.0)),
    ResourceInfo("helium3",          "Helium-3",        "He3", Color(0xFFFFEB3B), 100.0,   500.0,   0.05, mapOf("titanium" to 300.0, "crystal" to 50.0)),
    ResourceInfo("dark_matter",      "Mørkt Stof",      "DM",  Color(0xFF9C27B0), 50.0,    250.0,   0.01, mapOf("crystal" to 200.0, "helium3" to 30.0)),
    ResourceInfo("quantum_particles","Kvantepartikler", "QP",  Color(0xFFE91E63), 20.0,    100.0,   0.002,mapOf("dark_matter" to 10.0))
)

val RESOURCE_MAP = RESOURCES.associateBy { it.id }

data class UpgradeInfo(
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val cost: Map<String, Double>,
    val requires: String? = null,
    val multiplier: Double = 1.0,
    val targetResource: String? = null
)

val UPGRADES = listOf(
    // Mining
    UpgradeInfo("drill_mk1",      "Boremaskine Mk1",       "+50% jernminering",          "mining",    mapOf("iron" to 50.0),             null,        1.5, "iron"),
    UpgradeInfo("drill_mk2",      "Boremaskine Mk2",       "+100% jernminering",         "mining",    mapOf("iron" to 200.0),            "drill_mk1", 2.0, "iron"),
    UpgradeInfo("drill_mk3",      "Boremaskine Mk3",       "+200% jernminering",         "mining",    mapOf("iron" to 1000.0),           "drill_mk2", 3.0, "iron"),
    UpgradeInfo("titanium_drill", "Titaniumbor",           "+100% titaniumminering",     "mining",    mapOf("iron" to 300.0, "titanium" to 50.0), null, 2.0, "titanium"),
    UpgradeInfo("crystal_laser",  "Krystallaser",          "+100% krystalminering",      "mining",    mapOf("titanium" to 200.0),        null,        2.0, "crystal"),
    UpgradeInfo("he3_extractor",  "Helium-3 Ekstraktor",   "+100% He3-minering",         "mining",    mapOf("crystal" to 100.0),         null,        2.0, "helium3"),
    UpgradeInfo("dark_scanner",   "Mørkt-stof-scanner",    "+100% mørkt-stof-minering",  "mining",    mapOf("helium3" to 50.0),          null,        2.0, "dark_matter"),
    UpgradeInfo("quantum_tap",    "Kvantetap",             "+100% kvanteminering",        "mining",    mapOf("dark_matter" to 10.0),      null,        2.0, "quantum_particles"),
    UpgradeInfo("auto_miner",     "Autominer",             "+25% alle ressourcer",        "mining",    mapOf("iron" to 500.0, "titanium" to 100.0), null, 1.25, null),
    UpgradeInfo("deep_scan",      "Dybdescanning",         "+50% alle ressourcer",        "mining",    mapOf("crystal" to 150.0),        "auto_miner",1.5,  null),
    UpgradeInfo("nano_drills",    "Nanoborekroner",        "+75% alle ressourcer",        "mining",    mapOf("helium3" to 30.0),         "deep_scan", 1.75, null),
    UpgradeInfo("quantum_mining", "Kvantemining",          "×3 alle ressourcer",          "mining",    mapOf("quantum_particles" to 5.0),"nano_drills",3.0, null),
    // Transport
    UpgradeInfo("cargo_pod",      "Lastpod",               "+50% transportkapacitet",    "transport", mapOf("iron" to 150.0),            null,        1.5, null),
    UpgradeInfo("cargo_mk2",      "Lastpod Mk2",           "+100% transportkapacitet",   "transport", mapOf("iron" to 400.0, "titanium" to 80.0), "cargo_pod", 2.0, null),
    UpgradeInfo("ion_drive",      "Ion-fremdrift",         "-30% rejsetid",              "transport", mapOf("titanium" to 150.0),        null,        1.0, null),
    UpgradeInfo("warp_hop",       "Warp-hop",              "-50% rejsetid",              "transport", mapOf("crystal" to 100.0),        "ion_drive", 1.0, null),
    UpgradeInfo("multi_launch",   "Multi-affyring",        "Send alle ressourcer på én gang","transport",mapOf("titanium" to 300.0),    null,        1.0, null),
    UpgradeInfo("auto_transport", "Auto-transport",        "Automatisk afsendelse hvert 60s","transport",mapOf("crystal" to 200.0, "helium3" to 20.0),null,1.0,null),
    UpgradeInfo("quantum_tunnel", "Kvantetunnel",          "Øjeblikkelig transport",     "transport", mapOf("quantum_particles" to 3.0), "warp_hop",  1.0, null),
    // Station
    UpgradeInfo("storage_bay",    "Lagringsrum",           "×2 stationskapacitet",       "station",   mapOf("iron" to 300.0, "titanium" to 60.0), null, 1.0, null),
    UpgradeInfo("storage_mk2",    "Lagringsrum Mk2",       "×3 stationskapacitet",       "station",   mapOf("titanium" to 200.0, "crystal" to 40.0),"storage_bay",1.0,null),
    UpgradeInfo("refinery",       "Raffinaderi",           "+20% alle stationsressourcer","station",  mapOf("iron" to 400.0),            null,        1.2, null),
    UpgradeInfo("solar_array",    "Solcellearray",         "Frigør energi til moduler",  "station",   mapOf("titanium" to 250.0),        null,        1.0, null),
    UpgradeInfo("shield_gen",     "Skærmgenerator",        "Beskytter stationen",        "station",   mapOf("crystal" to 150.0),         null,        1.0, null),
    // Research
    UpgradeInfo("research_proto", "Forskningsprotokol",    "+10% alle rater",            "research",  mapOf("iron" to 100.0, "titanium" to 20.0), null, 1.1, null),
    UpgradeInfo("ai_optimization","AI-optimering",         "+20% alle rater",            "research",  mapOf("crystal" to 80.0),         "research_proto",1.2,null),
    UpgradeInfo("dark_theory",    "Mørk-stof-teori",       "+50% alle rater",            "research",  mapOf("helium3" to 40.0),         "ai_optimization",1.5,null),
    UpgradeInfo("quantum_theory", "Kvanteteori",           "×2 alle rater",              "research",  mapOf("dark_matter" to 8.0),      "dark_theory", 2.0, null),
    UpgradeInfo("singularity",    "Singularitet",          "×5 alle rater",              "research",  mapOf("quantum_particles" to 10.0),"quantum_theory",5.0,null)
)

data class ModuleInfo(
    val id: String,
    val name: String,
    val description: String,
    val cost: Map<String, Double>,
    val requires: String? = null
)

val MODULES = listOf(
    ModuleInfo("docking_bay",   "Dockingbay",       "Muliggør ressourcemodtagelse",    mapOf("iron" to 200.0)),
    ModuleInfo("power_core",    "Energikerne",       "Forsyner alle moduler",           mapOf("iron" to 400.0, "titanium" to 80.0),   "docking_bay"),
    ModuleInfo("research_lab",  "Forskningslaboratorium","Låser op for forsknings-opgraderinger",mapOf("titanium" to 300.0, "crystal" to 60.0),"power_core"),
    ModuleInfo("habitat",       "Beboelsesmodul",    "Øger effektivitet med 10%",       mapOf("titanium" to 200.0, "crystal" to 40.0),"power_core"),
    ModuleInfo("fabrication",   "Fabrikationsmodul", "Producerer udstyr automatisk",   mapOf("crystal" to 200.0, "helium3" to 25.0), "research_lab"),
    ModuleInfo("quantum_lab",   "Kvantelaboratorium","Låser op for kvanteforskning",    mapOf("helium3" to 50.0, "dark_matter" to 8.0),"fabrication"),
    ModuleInfo("warp_core",     "Warp-kerne",        "Muliggør øjeblikkelig transport", mapOf("dark_matter" to 15.0, "quantum_particles" to 5.0),"quantum_lab")
)

val MODULE_MAP = MODULES.associateBy { it.id }

data class Shipment(
    val id: Long,
    val resourceId: String,
    val amount: Double,
    val travelSeconds: Double,
    val startTime: Long
)
