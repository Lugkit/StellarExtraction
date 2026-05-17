extends Node

signal shipment_launched(resource_id: String, amount: float)
signal shipment_arrived(resource_id: String, amount: float)

const BASE_TRAVEL_TIME := 30.0  # seconds

func _ready() -> void:
	pass

func _process(delta: float) -> void:
	_tick_shipments(delta)

func _tick_shipments(delta: float) -> void:
	var arrived: Array[Dictionary] = []
	for shipment in GameState.active_shipments:
		shipment["travel_time_remaining"] -= delta
		if shipment["travel_time_remaining"] <= 0.0:
			arrived.append(shipment)

	for shipment in arrived:
		GameState.active_shipments.erase(shipment)
		var res_id: String = shipment["resource_id"]
		var amount: float = shipment["amount"]
		GameState.add_station_resource(res_id, amount)
		shipment_arrived.emit(res_id, amount)
		GameState.shipment_arrived.emit(res_id, amount)

func get_travel_time() -> float:
	var t := BASE_TRAVEL_TIME
	var ups := GameState.purchased_upgrades
	if "magnetic_rail_launcher" in ups: t *= 0.80
	if "gravity_slingshot" in ups:      t *= 0.70
	if "dark_matter_drive" in ups:      t *= 0.20
	var warp_level := GameState.get_module_level("warp_core")
	t *= pow(0.75, warp_level)
	return maxf(t, 1.0)

func get_launch_capacity(resource_id: String) -> float:
	var base := _base_capacity(resource_id)
	var ups := GameState.purchased_upgrades
	if "cargo_compression" in ups:    base *= 1.5
	if "wormhole_stabilizer" in ups:  base *= 10.0
	var warp_level := GameState.get_module_level("warp_core")
	base *= 1.0 + warp_level * 0.5
	return base

func _base_capacity(resource_id: String) -> float:
	match resource_id:
		"iron":             return 50.0
		"titanium":         return 30.0
		"crystal":          return 20.0
		"helium3":          return 15.0
		"dark_matter":      return 5.0
		"quantum_particles": return 2.0
	return 10.0

func launch(resource_id: String, amount: float) -> bool:
	if not GameState.has_module("docking_bay"):
		return false
	var available := GameState.planet_resources.get(resource_id, 0.0)
	var cap := get_launch_capacity(resource_id)
	amount = minf(amount, minf(available, cap))
	if amount <= 0.0:
		return false

	# Quantum teleporter: instant small shipments
	var ups := GameState.purchased_upgrades
	if "quantum_teleporter_mk1" in ups and amount <= 10.0:
		GameState.spend_planet_resource(resource_id, amount)
		GameState.add_station_resource(resource_id, amount)
		GameState.total_resources_launched[resource_id] = \
			GameState.total_resources_launched.get(resource_id, 0.0) + amount
		shipment_arrived.emit(resource_id, amount)
		GameState.shipment_arrived.emit(resource_id, amount)
		return true

	GameState.spend_planet_resource(resource_id, amount)
	GameState.total_resources_launched[resource_id] = \
		GameState.total_resources_launched.get(resource_id, 0.0) + amount

	var travel := get_travel_time()
	GameState.active_shipments.append({
		"resource_id": resource_id,
		"amount": amount,
		"travel_time_total": travel,
		"travel_time_remaining": travel,
	})
	shipment_launched.emit(resource_id, amount)
	GameState.shipment_launched.emit(resource_id, amount)
	return true

func launch_all(resource_id: String) -> bool:
	var amount := GameState.planet_resources.get(resource_id, 0.0)
	return launch(resource_id, amount)
