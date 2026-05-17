extends Control

@onready var _station_resources : HBoxContainer = $Layout/StationResources
@onready var _module_list       : VBoxContainer = $Layout/ModulesScroll/ModuleList
@onready var _station_canvas    : Control       = $Layout/StationCanvas

var _res_labels: Dictionary = {}
var _module_rows: Dictionary = {}
var _canvas_time := 0.0

func _ready() -> void:
	_build_resource_bar()
	_build_module_list()
	GameState.resources_changed.connect(_refresh_resources)
	GameState.module_built.connect(func(_id): _build_module_list())
	GameState.upgrade_purchased.connect(func(_id): _build_module_list())

func _process(delta: float) -> void:
	_canvas_time += delta
	_station_canvas.queue_redraw()

func _build_resource_bar() -> void:
	for child in _station_resources.get_children():
		child.queue_free()
	_res_labels.clear()

	for resource_id in ResourceManager.RESOURCE_DATA:
		var rdata := ResourceManager.RESOURCE_DATA[resource_id] as Dictionary
		var lbl := Label.new()
		lbl.add_theme_font_size_override("font_size", 18)
		lbl.add_theme_color_override("font_color", rdata.get("color", Color.WHITE))
		lbl.text = "%s: 0" % rdata.get("symbol", "?")
		_station_resources.add_child(lbl)
		_res_labels[resource_id] = lbl

	_refresh_resources()

func _refresh_resources() -> void:
	for resource_id in _res_labels:
		var rdata := ResourceManager.RESOURCE_DATA.get(resource_id, {}) as Dictionary
		var amount := GameState.station_resources.get(resource_id, 0.0)
		_res_labels[resource_id].text = "%s: %s" % [
			rdata.get("symbol", "?"),
			ResourceManager.format_number(amount),
		]

func _build_module_list() -> void:
	for child in _module_list.get_children():
		child.queue_free()
	_module_rows.clear()

	for module_id in UpgradeManager.MODULES:
		var mdata := UpgradeManager.MODULES[module_id] as Dictionary
		var req_mod: String = mdata.get("requires_module", "")
		if req_mod != "" and not GameState.has_module(req_mod):
			continue  # not yet visible

		var row := _make_module_row(module_id, mdata)
		_module_list.add_child(row)
		_module_rows[module_id] = row

func _make_module_row(module_id: String, mdata: Dictionary) -> PanelContainer:
	var panel := PanelContainer.new()
	panel.custom_minimum_size = Vector2(0, 130)

	var hbox := HBoxContainer.new()
	hbox.set("theme_override_constants/separation", 16)
	panel.add_child(hbox)

	# Icon + name
	var icon_lbl := Label.new()
	icon_lbl.text  = mdata.get("icon", "▣")
	icon_lbl.custom_minimum_size = Vector2(60, 0)
	icon_lbl.add_theme_font_size_override("font_size", 36)
	icon_lbl.vertical_alignment = VERTICAL_ALIGNMENT_CENTER
	hbox.add_child(icon_lbl)

	var info_col := VBoxContainer.new()
	info_col.size_flags_horizontal = Control.SIZE_EXPAND_FILL
	hbox.add_child(info_col)

	var name_lbl := Label.new()
	name_lbl.text = mdata.get("name", module_id)
	name_lbl.add_theme_font_size_override("font_size", 24)
	name_lbl.add_theme_color_override("font_color", Color(0.8, 0.6, 1.0))
	info_col.add_child(name_lbl)

	var level := GameState.get_module_level(module_id)
	var max_level: int = mdata.get("max_level", 1)
	var level_lbl := Label.new()
	level_lbl.text = "Niveau %d / %d" % [level, max_level]
	level_lbl.add_theme_font_size_override("font_size", 18)
	level_lbl.add_theme_color_override("font_color", Color(0.6, 0.6, 0.6))
	info_col.add_child(level_lbl)

	var effect_lbl := Label.new()
	effect_lbl.text = mdata.get("effect_per_level", "")
	effect_lbl.add_theme_font_size_override("font_size", 16)
	effect_lbl.add_theme_color_override("font_color", Color(0.4, 1.0, 0.7))
	effect_lbl.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
	info_col.add_child(effect_lbl)

	# Cost + build button
	var btn_col := VBoxContainer.new()
	btn_col.custom_minimum_size = Vector2(220, 0)
	hbox.add_child(btn_col)

	if level < max_level:
		var costs: Array = mdata.get("cost_per_level", [])
		if level < costs.size():
			var cost := costs[level] as Dictionary
			var cost_str := ""
			for res in cost:
				var rdata := ResourceManager.RESOURCE_DATA.get(res, {}) as Dictionary
				cost_str += "%s %s  " % [ResourceManager.format_number(cost[res]), rdata.get("symbol", res)]
			var cost_lbl := Label.new()
			cost_lbl.text = cost_str.strip_edges()
			cost_lbl.add_theme_font_size_override("font_size", 16)
			cost_lbl.add_theme_color_override("font_color", Color(0.9, 0.8, 0.4))
			cost_lbl.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
			btn_col.add_child(cost_lbl)

		var btn := Button.new()
		btn.text = "Byg" if level == 0 else "Opgrader"
		btn.custom_minimum_size = Vector2(0, 60)
		btn.disabled = not UpgradeManager.can_build_module(module_id)
		btn.pressed.connect(func():
			UpgradeManager.build_module(module_id)
			_build_module_list()
			_refresh_resources()
		)
		btn_col.add_child(btn)
	else:
		var max_lbl := Label.new()
		max_lbl.text = "MAX NIVEAU"
		max_lbl.add_theme_color_override("font_color", Color(1.0, 0.7, 0.0))
		max_lbl.add_theme_font_size_override("font_size", 20)
		max_lbl.vertical_alignment = VERTICAL_ALIGNMENT_CENTER
		btn_col.add_child(max_lbl)

	return panel
