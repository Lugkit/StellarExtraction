extends Node

signal resources_changed
signal upgrade_purchased(upgrade_id: String)
signal shipment_launched(resource_id: String, amount: float)
signal shipment_arrived(resource_id: String, amount: float)
signal module_built(module_id: String)
signal prestige_reset

# ── Resources on planet ──────────────────────────────────────────────────────
var planet_resources: Dictionary = {
	"iron":     0.0,
	"titanium": 0.0,
	"crystal":  0.0,
	"helium3":  0.0,
	"dark_matter":       0.0,
	"quantum_particles": 0.0,
}

var planet_caps: Dictionary = {
	"iron":     500.0,
	"titanium": 200.0,
	"crystal":  100.0,
	"helium3":  80.0,
	"dark_matter":       30.0,
	"quantum_particles": 10.0,
}

# ── Resources on station ─────────────────────────────────────────────────────
var station_resources: Dictionary = {
	"iron":     0.0,
	"titanium": 0.0,
	"crystal":  0.0,
	"helium3":  0.0,
	"dark_matter":       0.0,
	"quantum_particles": 0.0,
}

# ── Upgrades purchased ───────────────────────────────────────────────────────
var purchased_upgrades: Array[String] = []

# ── Space station modules ─────────────────────────────────────────────────────
var built_modules: Array[String] = []
var module_levels: Dictionary = {}   # module_id -> level

# ── Transport ────────────────────────────────────────────────────────────────
var active_shipments: Array[Dictionary] = []
# Each shipment: { resource_id, amount, travel_time_total, travel_time_remaining }

# ── Stats ────────────────────────────────────────────────────────────────────
var total_resources_mined: Dictionary = {}
var total_resources_launched: Dictionary = {}
var total_playtime_seconds: float = 0.0
var prestige_count: int = 0
var prestige_multiplier: float = 1.0

# ── Progression flags ────────────────────────────────────────────────────────
var planet_level: int = 1        # unlocks new resource types
var research_points: float = 0.0

func _ready() -> void:
	for key in planet_resources:
		total_resources_mined[key] = 0.0
		total_resources_launched[key] = 0.0

func _process(delta: float) -> void:
	total_playtime_seconds += delta

func add_planet_resource(resource_id: String, amount: float) -> void:
	if resource_id not in planet_resources:
		return
	var cap = planet_caps[resource_id]
	planet_resources[resource_id] = minf(planet_resources[resource_id] + amount, cap)
	total_resources_mined[resource_id] = total_resources_mined.get(resource_id, 0.0) + amount
	resources_changed.emit()

func spend_planet_resource(resource_id: String, amount: float) -> bool:
	if planet_resources.get(resource_id, 0.0) < amount:
		return false
	planet_resources[resource_id] -= amount
	resources_changed.emit()
	return true

func add_station_resource(resource_id: String, amount: float) -> void:
	station_resources[resource_id] = station_resources.get(resource_id, 0.0) + amount
	resources_changed.emit()

func spend_station_resource(resource_id: String, amount: float) -> bool:
	if station_resources.get(resource_id, 0.0) < amount:
		return false
	station_resources[resource_id] -= amount
	resources_changed.emit()
	return true

func can_afford_planet(costs: Dictionary) -> bool:
	for res in costs:
		if planet_resources.get(res, 0.0) < costs[res]:
			return false
	return true

func can_afford_station(costs: Dictionary) -> bool:
	for res in costs:
		if station_resources.get(res, 0.0) < costs[res]:
			return false
	return true

func has_upgrade(id: String) -> bool:
	return id in purchased_upgrades

func has_module(id: String) -> bool:
	return id in built_modules

func get_module_level(id: String) -> int:
	return module_levels.get(id, 0)

func unlock_planet_level(level: int) -> void:
	planet_level = maxi(planet_level, level)

func to_dict() -> Dictionary:
	return {
		"planet_resources": planet_resources.duplicate(),
		"station_resources": station_resources.duplicate(),
		"purchased_upgrades": purchased_upgrades.duplicate(),
		"built_modules": built_modules.duplicate(),
		"module_levels": module_levels.duplicate(),
		"active_shipments": active_shipments.duplicate(true),
		"total_resources_mined": total_resources_mined.duplicate(),
		"total_resources_launched": total_resources_launched.duplicate(),
		"total_playtime_seconds": total_playtime_seconds,
		"prestige_count": prestige_count,
		"prestige_multiplier": prestige_multiplier,
		"planet_level": planet_level,
		"research_points": research_points,
	}

func from_dict(data: Dictionary) -> void:
	planet_resources = data.get("planet_resources", planet_resources)
	station_resources = data.get("station_resources", station_resources)
	purchased_upgrades = data.get("purchased_upgrades", [])
	built_modules = data.get("built_modules", [])
	module_levels = data.get("module_levels", {})
	active_shipments = data.get("active_shipments", [])
	total_resources_mined = data.get("total_resources_mined", {})
	total_resources_launched = data.get("total_resources_launched", {})
	total_playtime_seconds = data.get("total_playtime_seconds", 0.0)
	prestige_count = data.get("prestige_count", 0)
	prestige_multiplier = data.get("prestige_multiplier", 1.0)
	planet_level = data.get("planet_level", 1)
	research_points = data.get("research_points", 0.0)
