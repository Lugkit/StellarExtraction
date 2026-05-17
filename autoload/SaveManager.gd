extends Node

const SAVE_PATH := "user://savegame.json"
const AUTOSAVE_INTERVAL := 30.0

var _autosave_timer := 0.0

func _ready() -> void:
	load_game()

func _process(delta: float) -> void:
	_autosave_timer += delta
	if _autosave_timer >= AUTOSAVE_INTERVAL:
		_autosave_timer = 0.0
		save_game()

func save_game() -> void:
	var data := GameState.to_dict()
	data["save_timestamp"] = Time.get_unix_time_from_system()
	var json_string := JSON.stringify(data, "\t")
	var file := FileAccess.open(SAVE_PATH, FileAccess.WRITE)
	if file:
		file.store_string(json_string)
		file.close()

func load_game() -> void:
	if not FileAccess.file_exists(SAVE_PATH):
		return
	var file := FileAccess.open(SAVE_PATH, FileAccess.READ)
	if not file:
		return
	var content := file.get_as_text()
	file.close()

	var json := JSON.new()
	var err := json.parse(content)
	if err != OK:
		push_warning("SaveManager: could not parse save file.")
		return

	var data := json.get_data() as Dictionary
	_apply_offline_progress(data)
	GameState.from_dict(data)
	ResourceManager.recalculate_rates()

func _apply_offline_progress(data: Dictionary) -> void:
	var save_time: float = data.get("save_timestamp", 0.0)
	if save_time <= 0.0:
		return
	var now := Time.get_unix_time_from_system()
	var elapsed := minf(now - save_time, 3600.0 * 8)  # max 8 hours offline
	if elapsed <= 0.0:
		return

	# Simulate resource production offline (uses saved rates via current upgrades)
	# We re-derive rates from the saved upgrade list before GameState is fully loaded.
	var saved_upgrades: Array = data.get("purchased_upgrades", [])
	var saved_planet_level: int = data.get("planet_level", 1)
	var saved_prestige_mult: float = data.get("prestige_multiplier", 1.0)

	var planet_res: Dictionary = data.get("planet_resources", {})
	var planet_caps_ref: Dictionary = GameState.planet_caps

	for resource_id in ResourceManager.RESOURCE_DATA:
		var rdata := ResourceManager.RESOURCE_DATA[resource_id] as Dictionary
		if rdata.get("unlock_planet_level", 1) > saved_planet_level:
			continue
		var rate: float = rdata["base_rate"]
		# Apply rough multiplier (simplified for offline calc)
		var mult := _offline_multiplier(resource_id, saved_upgrades, saved_planet_level)
		rate *= mult * saved_prestige_mult
		var gained := rate * elapsed
		var current := planet_res.get(resource_id, 0.0) as float
		var cap := planet_caps_ref.get(resource_id, 9999.0) as float
		planet_res[resource_id] = minf(current + gained, cap)

	data["planet_resources"] = planet_res

func _offline_multiplier(resource_id: String, ups: Array, _planet_level: int) -> float:
	var mult := 1.0
	if "overclock_drills" in ups:      mult *= 2.0
	if "nano_bot_swarm" in ups:        mult *= 3.0
	if "ai_foreman" in ups:            mult *= 2.5
	if "plasma_cutter_array" in ups:   mult *= 1.8
	if "quantum_tunneling" in ups:     mult *= 4.0
	if "graviton_lens" in ups:         mult *= 2.2
	if "planetary_core_siphon" in ups: mult *= 6.0
	match resource_id:
		"iron":
			if "seismic_resonator" in ups: mult *= 2.0
			if "tectonic_tap" in ups:       mult *= 3.0
		"titanium":
			if "deep_vein_scanner" in ups:  mult *= 2.5
			if "tectonic_tap" in ups:        mult *= 2.0
		"crystal":
			if "resonance_tuner" in ups:    mult *= 3.0
		"helium3":
			if "mantle_bore" in ups:        mult *= 3.5
		"dark_matter":
			if "dark_matter_detector" in ups: mult *= 5.0
		"quantum_particles":
			if "quantum_coherence_field" in ups: mult *= 4.0
	return mult

func delete_save() -> void:
	if FileAccess.file_exists(SAVE_PATH):
		DirAccess.remove_absolute(SAVE_PATH)
