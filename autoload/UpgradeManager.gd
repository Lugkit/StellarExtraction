extends Node

# ─────────────────────────────────────────────────────────────────────────────
# All upgrades in the game.
# category: "mining" | "transport" | "station" | "research" | "prestige"
# requires: list of upgrade IDs that must be bought first
# requires_module: space station module that must be built first
# requires_planet_level: minimum planet level
# cost_planet: resource costs from planet storage
# cost_station: resource costs from station storage
# effect: human-readable description of the effect
# ─────────────────────────────────────────────────────────────────────────────
const UPGRADES: Dictionary = {
	# ── Mining upgrades ───────────────────────────────────────────────────────
	"overclock_drills": {
		"name": "Overklok Bore",
		"category": "mining",
		"icon": "⚙",
		"effect": "2× jernudvinding og titanium-rate",
		"description": "Driv borene langt ud over deres sikre grænse. Mere output — kortere levetid.",
		"cost_planet": {"iron": 50.0},
		"requires": [],
		"requires_planet_level": 1,
		"tier": 1,
	},
	"seismic_resonator": {
		"name": "Seismisk Resonator",
		"category": "mining",
		"icon": "〰",
		"effect": "2× jernrate. Rystelser blotlægger nye årer.",
		"description": "Sender lydpulser ned i jordskorpen og sprækker klipper op.",
		"cost_planet": {"iron": 120.0, "titanium": 30.0},
		"requires": ["overclock_drills"],
		"requires_planet_level": 1,
		"tier": 2,
	},
	"nano_bot_swarm": {
		"name": "Nano-Bot Sværm",
		"category": "mining",
		"icon": "🔬",
		"effect": "3× alle minefrekvenser",
		"description": "Millioner af mikroskopiske robotter arbejder utrætteligt.",
		"cost_planet": {"iron": 300.0, "titanium": 80.0, "crystal": 20.0},
		"requires": ["overclock_drills"],
		"requires_planet_level": 1,
		"tier": 2,
	},
	"tectonic_tap": {
		"name": "Tektonisk Aftapning",
		"category": "mining",
		"icon": "🌋",
		"effect": "3× jern, 2× titanium. Adgang til dybe malmårer.",
		"description": "Udnytter planetens egne tektoniske kræfter til at bryde overfladen op.",
		"cost_planet": {"iron": 500.0, "titanium": 150.0},
		"requires": ["seismic_resonator"],
		"requires_planet_level": 1,
		"tier": 3,
	},
	"deep_vein_scanner": {
		"name": "Dybde-Åre Scanner",
		"category": "mining",
		"icon": "📡",
		"effect": "2.5× titaniumrate",
		"description": "Kortlægger skjulte titaniumårer dybt i mantlen.",
		"cost_planet": {"iron": 200.0, "titanium": 60.0, "crystal": 10.0},
		"requires": ["seismic_resonator"],
		"requires_planet_level": 1,
		"tier": 2,
	},
	"plasma_cutter_array": {
		"name": "Plasma-Skærer Array",
		"category": "mining",
		"icon": "⚡",
		"effect": "1.8× alle minefrekvenser",
		"description": "Smelter klipper i mikrosekunder med fokuseret plasmastråle.",
		"cost_planet": {"iron": 400.0, "titanium": 120.0, "crystal": 30.0},
		"requires": ["nano_bot_swarm"],
		"requires_planet_level": 2,
		"tier": 3,
	},
	"ai_foreman": {
		"name": "AI Formand",
		"category": "mining",
		"icon": "🤖",
		"effect": "2.5× alle minefrekvenser",
		"description": "Et neuralt netværk optimerer kontinuerligt mineoperationer i realtid.",
		"cost_planet": {"iron": 600.0, "titanium": 200.0, "crystal": 50.0},
		"requires": ["nano_bot_swarm"],
		"requires_module": "research_lab",
		"requires_planet_level": 2,
		"tier": 3,
	},
	"resonance_tuner": {
		"name": "Resonans-Tuner",
		"category": "mining",
		"icon": "💎",
		"effect": "3× krystalrate",
		"description": "Synkroniserer borehovedet med krystalstrukturens egenfrekvens.",
		"cost_planet": {"titanium": 100.0, "crystal": 40.0},
		"requires": ["plasma_cutter_array"],
		"requires_planet_level": 2,
		"tier": 3,
	},
	"mantle_bore": {
		"name": "Kappe-Bor",
		"category": "mining",
		"icon": "🌡",
		"effect": "3.5× helium-3 rate",
		"description": "En titaniumborekerne der kan tåle 5000°C for at nå He³-lommerne.",
		"cost_planet": {"titanium": 300.0, "crystal": 80.0, "helium3": 10.0},
		"requires": ["tectonic_tap"],
		"requires_planet_level": 2,
		"tier": 4,
	},
	"graviton_lens": {
		"name": "Graviton-Linse",
		"category": "mining",
		"icon": "🔭",
		"effect": "2.2× alle minefrekvenser",
		"description": "Fokuserer gravitationskræfter for at knuse sten uden kontakt.",
		"cost_planet": {"titanium": 500.0, "crystal": 150.0, "helium3": 40.0},
		"requires": ["ai_foreman", "mantle_bore"],
		"requires_planet_level": 3,
		"tier": 4,
	},
	"quantum_tunneling": {
		"name": "Kvantetunnelering",
		"category": "mining",
		"icon": "🌀",
		"effect": "4× alle minefrekvenser. Borehovederne teleporterer igennem klippe.",
		"description": "Udnytter kvantemekanik til at flytte materiale uden fysisk kontakt.",
		"cost_planet": {"crystal": 300.0, "helium3": 100.0, "dark_matter": 10.0},
		"requires": ["graviton_lens"],
		"requires_planet_level": 3,
		"tier": 5,
	},
	"dark_matter_detector": {
		"name": "Mørkt-Stof Detektor",
		"category": "mining",
		"icon": "🔮",
		"effect": "5× mørkt stof rate",
		"description": "Registrerer og lokaliserer mørkt stof med specialiserede sensorer.",
		"cost_planet": {"crystal": 200.0, "helium3": 80.0},
		"requires": ["quantum_tunneling"],
		"requires_module": "quantum_lab",
		"requires_planet_level": 3,
		"tier": 5,
	},
	"quantum_coherence_field": {
		"name": "Kvante-Kohærens Felt",
		"category": "mining",
		"icon": "✨",
		"effect": "4× kvantepartikelrate",
		"description": "Et felt der stabiliserer kvantetilstande og muliggør partikelindsamling.",
		"cost_planet": {"dark_matter": 20.0, "helium3": 200.0},
		"requires": ["dark_matter_detector"],
		"requires_planet_level": 4,
		"tier": 6,
	},
	"planetary_core_siphon": {
		"name": "Planetkerne-Sifon",
		"category": "mining",
		"icon": "🌍",
		"effect": "6× alle minefrekvenser. Aftapper selve planetkernens energi.",
		"description": "Den ultimative mineoperation: en kanal direkte til planetens glødende kerne.",
		"cost_planet": {"dark_matter": 50.0, "quantum_particles": 5.0},
		"requires": ["quantum_tunneling", "mantle_bore"],
		"requires_module": "warp_core",
		"requires_planet_level": 4,
		"tier": 6,
	},

	# ── Transport upgrades ────────────────────────────────────────────────────
	"magnetic_rail_launcher": {
		"name": "Magnetisk Railgun",
		"category": "transport",
		"icon": "🚀",
		"effect": "Hurtigere forsendelse. -20% rejsetid.",
		"description": "Elektromagnetisk katapult skyder lasten afsted med høj hastighed.",
		"cost_planet": {"iron": 150.0, "titanium": 50.0},
		"requires": [],
		"requires_planet_level": 1,
		"tier": 1,
	},
	"gravity_slingshot": {
		"name": "Tyngdekrafts-Slyng",
		"category": "transport",
		"icon": "🌐",
		"effect": "-30% ekstra rejsetid. Bruger planetens tyngdekraft.",
		"description": "Beregner en bane der udnytter planetens tyngdekraft som boost.",
		"cost_planet": {"iron": 200.0, "titanium": 80.0, "crystal": 15.0},
		"requires": ["magnetic_rail_launcher"],
		"requires_planet_level": 1,
		"tier": 2,
	},
	"cargo_compression": {
		"name": "Lastkomprimering",
		"category": "transport",
		"icon": "📦",
		"effect": "+50% lastkapacitet per forsendelse.",
		"description": "Komprimerer ressourcer molekylært for at pakke mere i samme volumen.",
		"cost_planet": {"titanium": 100.0, "crystal": 25.0},
		"requires": ["magnetic_rail_launcher"],
		"requires_planet_level": 1,
		"tier": 2,
	},
	"drone_swarm_escort": {
		"name": "Drone-Sværm Eskorte",
		"category": "transport",
		"icon": "🛸",
		"effect": "Forsendelser kan ikke gå tabt. Droner sikrer leveringen.",
		"description": "En flåde af selvstyrende droner eskorterer hver forsendelse sikkert frem.",
		"cost_planet": {"titanium": 150.0, "crystal": 40.0, "helium3": 10.0},
		"requires": ["cargo_compression"],
		"requires_planet_level": 2,
		"tier": 3,
	},
	"quantum_teleporter_mk1": {
		"name": "Kvanteteleporter Mk.I",
		"category": "transport",
		"icon": "⚛",
		"effect": "Små forsendelser ankommer øjeblikkeligt.",
		"description": "Teleporterer under 10 enheder af en ressource instantant via kvantesammenfiltring.",
		"cost_planet": {"crystal": 200.0, "helium3": 60.0, "dark_matter": 8.0},
		"requires": ["gravity_slingshot"],
		"requires_module": "quantum_lab",
		"requires_planet_level": 3,
		"tier": 4,
	},
	"wormhole_stabilizer": {
		"name": "Ormehuls-Stabilisator",
		"category": "transport",
		"icon": "🕳",
		"effect": "Massive forsendelser. 10× lastkapacitet.",
		"description": "Åbner og stabiliserer et kunstigt ormehul for massetransport.",
		"cost_planet": {"dark_matter": 30.0, "quantum_particles": 3.0},
		"requires": ["quantum_teleporter_mk1"],
		"requires_planet_level": 4,
		"tier": 5,
	},
	"dark_matter_drive": {
		"name": "Mørkt-Stof Drev",
		"category": "transport",
		"icon": "💫",
		"effect": "-80% rejsetid. Kvantespring til stationen.",
		"description": "Udnytter mørkt stofs negative masse til øjeblikkelig acceleration.",
		"cost_planet": {"dark_matter": 60.0, "quantum_particles": 8.0},
		"requires": ["wormhole_stabilizer"],
		"requires_planet_level": 4,
		"tier": 6,
	},

	# ── Station upgrades (forbedrer modulers effektivitet) ───────────────────
	"automated_construction": {
		"name": "Automatiseret Byggeri",
		"category": "station",
		"icon": "🏗",
		"effect": "Moduler bygger 2× hurtigere.",
		"description": "Robotarme arbejder hele døgnet uden pause.",
		"cost_station": {"iron": 100.0, "titanium": 40.0},
		"requires": [],
		"requires_planet_level": 1,
		"tier": 1,
	},
	"solar_array_expansion": {
		"name": "Solarray Udvidelse",
		"category": "station",
		"icon": "☀",
		"effect": "+50% energiproduktion på stationen.",
		"description": "Udbygger stationens solpaneler massivt for mere strøm.",
		"cost_station": {"iron": 200.0, "titanium": 60.0, "crystal": 20.0},
		"requires": ["automated_construction"],
		"requires_module": "power_core",
		"requires_planet_level": 1,
		"tier": 2,
	},
	"zero_g_fabricator": {
		"name": "Nul-G Fabrikator",
		"category": "station",
		"icon": "🔧",
		"effect": "Nyt modul: Fabrikator. Producerer komponenter automatisk.",
		"description": "I mikrogravitation kan enorme strukturer samles med præcision.",
		"cost_station": {"titanium": 150.0, "crystal": 50.0},
		"requires": ["solar_array_expansion"],
		"requires_planet_level": 2,
		"tier": 3,
	},
	"orbital_shield": {
		"name": "Orbital Skjold",
		"category": "station",
		"icon": "🛡",
		"effect": "Beskytter stationen mod meteorer og tab af ressourcer.",
		"description": "Et kraftfelt der reflekterer rumaffald og mikrometeorer.",
		"cost_station": {"titanium": 300.0, "crystal": 100.0, "helium3": 30.0},
		"requires": ["zero_g_fabricator"],
		"requires_planet_level": 2,
		"tier": 3,
	},
	"ai_construction_manager": {
		"name": "AI Byggechef",
		"category": "station",
		"icon": "🧠",
		"effect": "Moduler opgraderes automatisk over tid.",
		"description": "En kunstig intelligens overvåger og optimerer alt byggeri.",
		"cost_station": {"crystal": 200.0, "helium3": 80.0},
		"requires": ["orbital_shield"],
		"requires_module": "research_lab",
		"requires_planet_level": 3,
		"tier": 4,
	},
	"dyson_collector_fragment": {
		"name": "Dyson-Samler Fragment",
		"category": "station",
		"icon": "⭐",
		"effect": "Høster stjerneenergi. +energi over tid uanset sol-afstand.",
		"description": "Det første fragment af en fremtidig Dyson-sfære.",
		"cost_station": {"dark_matter": 25.0, "quantum_particles": 2.0},
		"requires": ["ai_construction_manager"],
		"requires_module": "warp_core",
		"requires_planet_level": 4,
		"tier": 5,
	},
	"quantum_computer_core": {
		"name": "Kvantecomputer Kerne",
		"category": "station",
		"icon": "💻",
		"effect": "Eksponentiel effektivitetsstigning på alle moduler.",
		"description": "En computer der beregner i parallelle kvantetilstande.",
		"cost_station": {"dark_matter": 50.0, "quantum_particles": 10.0},
		"requires": ["dyson_collector_fragment"],
		"requires_planet_level": 4,
		"tier": 6,
	},

	# ── Research upgrades ─────────────────────────────────────────────────────
	"xenogeology": {
		"name": "Xenogeologi",
		"category": "research",
		"icon": "📚",
		"effect": "Låser op: Deep Vein Scanner og Resonance Tuner.",
		"description": "Studie af fremmed planets geologi afslører nye minestrategier.",
		"cost_station": {"iron": 80.0, "crystal": 10.0},
		"requires": [],
		"requires_module": "research_lab",
		"requires_planet_level": 1,
		"tier": 1,
	},
	"quantum_mechanics_theory": {
		"name": "Kvantemekanik Teori",
		"category": "research",
		"icon": "⚛",
		"effect": "Låser op: Quantum Tunneling og Quantum Teleporter.",
		"description": "Grundlæggende teori der muliggør kvanteteknologier.",
		"cost_station": {"crystal": 100.0, "helium3": 30.0},
		"requires": ["xenogeologi"],
		"requires_module": "research_lab",
		"requires_planet_level": 2,
		"tier": 2,
	},
	"dark_matter_physics": {
		"name": "Mørkt-Stof Fysik",
		"category": "research",
		"icon": "🔬",
		"effect": "Låser op: Dark Matter Detector og Dark Matter Drive.",
		"description": "Forståelse af mørkt stofs egenskaber åbner nye teknologier.",
		"cost_station": {"helium3": 80.0, "dark_matter": 5.0},
		"requires": ["quantum_mechanics_theory"],
		"requires_module": "quantum_lab",
		"requires_planet_level": 3,
		"tier": 3,
	},
}

# ── Station modules ───────────────────────────────────────────────────────────
const MODULES: Dictionary = {
	"docking_bay": {
		"name": "Dokkingbay",
		"icon": "🚉",
		"description": "Tillader forsendelser at ankomme. Nødvendig for alt andet.",
		"max_level": 5,
		"cost_per_level": [
			{"iron": 80.0, "titanium": 20.0},
			{"iron": 200.0, "titanium": 60.0, "crystal": 10.0},
			{"titanium": 150.0, "crystal": 40.0, "helium3": 10.0},
			{"crystal": 100.0, "helium3": 40.0, "dark_matter": 5.0},
			{"helium3": 80.0, "dark_matter": 15.0, "quantum_particles": 1.0},
		],
		"effect_per_level": "+20% modtagekapacitet og hastighed.",
		"requires_module": "",
	},
	"power_core": {
		"name": "Energikerne",
		"icon": "⚡",
		"description": "Forsyner hele stationen med strøm.",
		"max_level": 5,
		"cost_per_level": [
			{"iron": 60.0, "crystal": 15.0},
			{"titanium": 80.0, "crystal": 30.0},
			{"crystal": 80.0, "helium3": 20.0},
			{"helium3": 60.0, "dark_matter": 8.0},
			{"dark_matter": 20.0, "quantum_particles": 2.0},
		],
		"effect_per_level": "+25% energi, booster alle moduler.",
		"requires_module": "docking_bay",
	},
	"research_lab": {
		"name": "Forskningslaboratorium",
		"icon": "🔬",
		"description": "Muliggør forskning og avancerede opgraderinger.",
		"max_level": 5,
		"cost_per_level": [
			{"iron": 100.0, "titanium": 40.0, "crystal": 20.0},
			{"titanium": 120.0, "crystal": 50.0, "helium3": 15.0},
			{"crystal": 150.0, "helium3": 50.0},
			{"helium3": 100.0, "dark_matter": 10.0},
			{"dark_matter": 30.0, "quantum_particles": 3.0},
		],
		"effect_per_level": "+15% alle minefrekvenser pr. niveau.",
		"requires_module": "power_core",
	},
	"habitat": {
		"name": "Beboelsesmodul",
		"icon": "🏠",
		"description": "Kolonister øger stationens driftseffektivitet.",
		"max_level": 5,
		"cost_per_level": [
			{"iron": 150.0, "titanium": 50.0},
			{"titanium": 100.0, "crystal": 30.0},
			{"crystal": 80.0, "helium3": 25.0},
			{"helium3": 80.0, "dark_matter": 8.0},
			{"dark_matter": 25.0, "quantum_particles": 2.0},
		],
		"effect_per_level": "+10% alle ressourceproduktioner.",
		"requires_module": "power_core",
	},
	"fabrication_bay": {
		"name": "Fabrikationshal",
		"icon": "🏭",
		"description": "Producerer avancerede komponenter fra råmaterialer.",
		"max_level": 4,
		"cost_per_level": [
			{"iron": 200.0, "titanium": 100.0, "crystal": 30.0},
			{"titanium": 200.0, "crystal": 80.0, "helium3": 30.0},
			{"crystal": 200.0, "helium3": 80.0, "dark_matter": 10.0},
			{"dark_matter": 40.0, "quantum_particles": 5.0},
		],
		"effect_per_level": "Reducerer byggeomkostninger med 10% pr. niveau.",
		"requires_module": "research_lab",
	},
	"quantum_lab": {
		"name": "Kvantelaboratorium",
		"icon": "🌀",
		"description": "Forskning i kvantemekanik og mørkt stof.",
		"max_level": 3,
		"cost_per_level": [
			{"crystal": 300.0, "helium3": 100.0, "dark_matter": 15.0},
			{"helium3": 200.0, "dark_matter": 40.0, "quantum_particles": 3.0},
			{"dark_matter": 80.0, "quantum_particles": 10.0},
		],
		"effect_per_level": "+20% mørkt stof og kvantepartikelrate.",
		"requires_module": "research_lab",
	},
	"warp_core": {
		"name": "Warp-Kerne",
		"icon": "🌌",
		"description": "Aktiverer kvantespring og avanceret transport.",
		"max_level": 3,
		"cost_per_level": [
			{"helium3": 300.0, "dark_matter": 50.0, "quantum_particles": 5.0},
			{"dark_matter": 100.0, "quantum_particles": 15.0},
			{"quantum_particles": 30.0},
		],
		"effect_per_level": "+50% transportkapacitet og -25% rejsetid.",
		"requires_module": "quantum_lab",
	},
}

func _ready() -> void:
	pass

func get_upgrade(id: String) -> Dictionary:
	return UPGRADES.get(id, {})

func get_module(id: String) -> Dictionary:
	return MODULES.get(id, {})

func can_buy_upgrade(id: String) -> bool:
	var data := UPGRADES.get(id, {}) as Dictionary
	if data.is_empty():
		return false
	if GameState.has_upgrade(id):
		return false

	# Planet level requirement
	if data.get("requires_planet_level", 1) > GameState.planet_level:
		return false

	# Prerequisite upgrades
	for req in data.get("requires", []):
		if not GameState.has_upgrade(req):
			return false

	# Required module
	var req_mod: String = data.get("requires_module", "")
	if req_mod != "" and not GameState.has_module(req_mod):
		return false

	# Cost check
	var cost_p := data.get("cost_planet", {}) as Dictionary
	var cost_s := data.get("cost_station", {}) as Dictionary
	if not cost_p.is_empty() and not GameState.can_afford_planet(cost_p):
		return false
	if not cost_s.is_empty() and not GameState.can_afford_station(cost_s):
		return false

	return true

func buy_upgrade(id: String) -> bool:
	if not can_buy_upgrade(id):
		return false
	var data := UPGRADES[id] as Dictionary
	var cost_p := data.get("cost_planet", {}) as Dictionary
	var cost_s := data.get("cost_station", {}) as Dictionary
	for res in cost_p:
		GameState.spend_planet_resource(res, cost_p[res])
	for res in cost_s:
		GameState.spend_station_resource(res, cost_s[res])
	GameState.purchased_upgrades.append(id)
	GameState.upgrade_purchased.emit(id)
	return true

func can_build_module(id: String) -> bool:
	var data := MODULES.get(id, {}) as Dictionary
	if data.is_empty():
		return false
	var current_level := GameState.get_module_level(id)
	if current_level >= data.get("max_level", 1):
		return false

	var req_mod: String = data.get("requires_module", "")
	if req_mod != "" and not GameState.has_module(req_mod):
		return false

	var costs: Array = data.get("cost_per_level", [])
	if current_level >= costs.size():
		return false
	var cost := costs[current_level] as Dictionary
	return GameState.can_afford_station(cost)

func build_module(id: String) -> bool:
	if not can_build_module(id):
		return false
	var data := MODULES[id] as Dictionary
	var current_level := GameState.get_module_level(id)
	var costs: Array = data.get("cost_per_level", [])
	var cost := costs[current_level] as Dictionary
	for res in cost:
		GameState.spend_station_resource(res, cost[res])
	GameState.module_levels[id] = current_level + 1
	if id not in GameState.built_modules:
		GameState.built_modules.append(id)
	GameState.module_built.emit(id)
	ResourceManager.recalculate_rates()
	return true

func get_upgrades_by_category(category: String) -> Array[String]:
	var result: Array[String] = []
	for id in UPGRADES:
		if UPGRADES[id].get("category", "") == category:
			result.append(id)
	return result

func is_upgrade_visible(id: String) -> bool:
	var data := UPGRADES.get(id, {}) as Dictionary
	if data.is_empty():
		return false
	if GameState.has_upgrade(id):
		return true
	if data.get("requires_planet_level", 1) > GameState.planet_level + 1:
		return false
	return true
