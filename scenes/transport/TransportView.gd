extends Control

@onready var _travel_info  : Label         = $Layout/TravelInfo
@onready var _launch_grid  : GridContainer = $Layout/LaunchGrid
@onready var _shipment_list: VBoxContainer = $Layout/ActiveShipments/ShipmentList
@onready var _space_canvas : Control       = $Layout/SpaceCanvas

var _launch_buttons: Dictionary = {}

func _ready() -> void:
	_build_launch_buttons()
	GameState.resources_changed.connect(_refresh)
	TransportManager.shipment_arrived.connect(_on_arrived)
	GameState.upgrade_purchased.connect(func(_id): _refresh())

func _process(_delta: float) -> void:
	_update_shipments()
	_space_canvas.queue_redraw()

func _build_launch_buttons() -> void:
	for child in _launch_grid.get_children():
		child.queue_free()
	_launch_buttons.clear()

	for resource_id in ResourceManager.RESOURCE_DATA:
		var rdata := ResourceManager.RESOURCE_DATA[resource_id] as Dictionary
		if rdata.get("unlock_planet_level", 1) > GameState.planet_level:
			continue

		var container := VBoxContainer.new()
		container.custom_minimum_size = Vector2(480, 120)

		var lbl := Label.new()
		lbl.text = "%s %s" % [rdata.get("symbol", "?"), rdata.get("name", resource_id)]
		lbl.add_theme_font_size_override("font_size", 22)
		lbl.add_theme_color_override("font_color", rdata.get("color", Color.WHITE))
		container.add_child(lbl)

		var avail_lbl := Label.new()
		avail_lbl.add_theme_font_size_override("font_size", 18)
		avail_lbl.add_theme_color_override("font_color", Color(0.7, 0.7, 0.7))
		container.add_child(avail_lbl)

		var btn := Button.new()
		btn.text = "Affyr"
		btn.custom_minimum_size = Vector2(0, 60)
		btn.pressed.connect(func(): TransportManager.launch_all(resource_id))
		container.add_child(btn)

		_launch_grid.add_child(container)
		_launch_buttons[resource_id] = {"container": container, "avail": avail_lbl, "btn": btn}

	_refresh()

func _refresh() -> void:
	_travel_info.text = "Rejsetid: %.0fs | Kapacitet per forsendelse varierer" \
		% TransportManager.get_travel_time()

	if not GameState.has_module("docking_bay"):
		_travel_info.text = "Byg Dokkingbay på stationen for at sende ressourcer!"

	for resource_id in _launch_buttons:
		var widgets := _launch_buttons[resource_id] as Dictionary
		var amount := GameState.planet_resources.get(resource_id, 0.0)
		var cap    := TransportManager.get_launch_capacity(resource_id)
		var avail_lbl: Label = widgets["avail"]
		var btn: Button      = widgets["btn"]
		avail_lbl.text = "På planet: %s | Kapacitet: %s" % [
			ResourceManager.format_number(amount),
			ResourceManager.format_number(cap),
		]
		btn.disabled = amount <= 0.0 or not GameState.has_module("docking_bay")

func _update_shipments() -> void:
	for child in _shipment_list.get_children():
		child.queue_free()

	for shipment in GameState.active_shipments:
		var res_id  : String = shipment["resource_id"]
		var amount  : float  = shipment["amount"]
		var total   : float  = shipment["travel_time_total"]
		var remaining: float = shipment["travel_time_remaining"]

		var rdata := ResourceManager.RESOURCE_DATA.get(res_id, {}) as Dictionary
		var row := HBoxContainer.new()

		var info := Label.new()
		info.text = "%s %s → Station  %.0fs kvar" % [
			ResourceManager.format_number(amount),
			rdata.get("name", res_id),
			remaining,
		]
		info.add_theme_font_size_override("font_size", 20)
		info.size_flags_horizontal = Control.SIZE_EXPAND_FILL
		row.add_child(info)

		var bar := ProgressBar.new()
		bar.max_value = total
		bar.value = total - remaining
		bar.custom_minimum_size = Vector2(200, 24)
		bar.show_percentage = false
		row.add_child(bar)

		_shipment_list.add_child(row)

func _on_arrived(resource_id: String, amount: float) -> void:
	var rdata := ResourceManager.RESOURCE_DATA.get(resource_id, {}) as Dictionary
	# Flash notification could be added here
	print("Ankommet: %s %s" % [ResourceManager.format_number(amount), rdata.get("name", resource_id)])
