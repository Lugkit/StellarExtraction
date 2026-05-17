extends Node

# Resource definitions: display data + base production per second on the planet
const RESOURCE_DATA: Dictionary = {
	"iron": {
		"name": "Jern",
		"symbol": "Fe",
		"color": Color(0.78, 0.78, 0.78),
		"base_rate": 1.0,
		"unlock_planet_level": 1,
		"description": "Grundlæggende byggemateriale.",
	},
	"titanium": {
		"name": "Titanium",
		"symbol": "Ti",
		"color": Color(0.4, 0.7, 1.0),
		"base_rate": 0.4,
		"unlock_planet_level": 1,
		"description": "Let og stærkt strukturmateriale.",
	},
	"crystal": {
		"name": "Energikrystal",
		"symbol": "Cr",
		"color": Color(0.3, 1.0, 0.8),
		"base_rate": 0.15,
		"unlock_planet_level": 2,
		"description": "Lagrer enorme mængder energi.",
	},
	"helium3": {
		"name": "Helium-3",
		"symbol": "He³",
		"color": Color(1.0, 0.85, 0.2),
		"base_rate": 0.08,
		"unlock_planet_level": 2,
		"description": "Fusionsbrændstof fra planetens kappe.",
	},
	"dark_matter": {
		"name": "Mørkt Stof",
		"symbol": "DM",
		"color": Color(0.6, 0.1, 0.9),
		"base_rate": 0.02,
		"unlock_planet_level": 3,
		"description": "Usynlig men utrolig kraftfuld substans.",
	},
	"quantum_particles": {
		"name": "Kvantepartikler",
		"symbol": "QP",
		"color": Color(1.0, 0.4, 0.6),
		"base_rate": 0.004,
		"unlock_planet_level": 4,
		"description": "Eksisterer i flere tilstande simultant.",
	},
}

# Computed each tick from base_rate × all active multipliers
var _production_rates: Dictionary = {}

func _ready() -> void:
	recalculate_rates()
	GameState.upgrade_purchased.connect(_on_upgrade_purchased)

func _process(delta: float) -> void:
	_tick_production(delta)

func _tick_production(delta: float) -> void:
	var changed := false
	for resource_id in _production_rates:
		var rate: float = _production_rates[resource_id]
		if rate <= 0.0:
			continue
		var before := GameState.planet_resources[resource_id]
		GameState.add_planet_resource(resource_id, rate * delta)
		if GameState.planet_resources[resource_id] != before:
			changed = true
	# signal is already emitted inside add_planet_resource

func recalculate_rates() -> void:
	for resource_id in RESOURCE_DATA:
		var data := RESOURCE_DATA[resource_id] as Dictionary
		if data["unlock_planet_level"] > GameState.planet_level:
			_production_rates[resource_id] = 0.0
			continue
		var rate: float = data["base_rate"]
		rate *= _get_mining_multiplier(resource_id)
		rate *= GameState.prestige_multiplier
		_production_rates[resource_id] = rate

func get_rate(resource_id: String) -> float:
	return _production_rates.get(resource_id, 0.0)

func get_all_rates() -> Dictionary:
	return _production_rates.duplicate()

func _get_mining_multiplier(resource_id: String) -> float:
	var mult := 1.0
	var ups := GameState.purchased_upgrades

	# Global mining upgrades
	if "overclock_drills" in ups:       mult *= 2.0
	if "nano_bot_swarm" in ups:         mult *= 3.0
	if "ai_foreman" in ups:             mult *= 2.5
	if "plasma_cutter_array" in ups:    mult *= 1.8
	if "quantum_tunneling" in ups:      mult *= 4.0
	if "graviton_lens" in ups:          mult *= 2.2
	if "planetary_core_siphon" in ups:  mult *= 6.0

	# Resource-specific multipliers
	match resource_id:
		"iron":
			if "seismic_resonator" in ups:      mult *= 2.0
			if "tectonic_tap" in ups:            mult *= 3.0
		"titanium":
			if "deep_vein_scanner" in ups:       mult *= 2.5
			if "tectonic_tap" in ups:            mult *= 2.0
		"crystal":
			if "resonance_tuner" in ups:         mult *= 3.0
		"helium3":
			if "mantle_bore" in ups:             mult *= 3.5
		"dark_matter":
			if "dark_matter_detector" in ups:    mult *= 5.0
		"quantum_particles":
			if "quantum_coherence_field" in ups: mult *= 4.0

	# Station module bonuses
	var research_level := GameState.get_module_level("research_lab")
	if research_level > 0:
		mult *= 1.0 + research_level * 0.15

	return mult

func _on_upgrade_purchased(_id: String) -> void:
	recalculate_rates()

func format_number(value: float) -> String:
	if value >= 1_000_000_000_000.0:
		return "%.2fT" % (value / 1_000_000_000_000.0)
	elif value >= 1_000_000_000.0:
		return "%.2fG" % (value / 1_000_000_000.0)
	elif value >= 1_000_000.0:
		return "%.2fM" % (value / 1_000_000.0)
	elif value >= 1_000.0:
		return "%.2fK" % (value / 1_000.0)
	elif value >= 10.0:
		return "%.1f" % value
	else:
		return "%.2f" % value
