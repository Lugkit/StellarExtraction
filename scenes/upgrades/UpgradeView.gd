extends Control

const CATEGORIES := ["mining", "transport", "station", "research"]

@onready var _upgrade_list : VBoxContainer = $Layout/UpgradeScroll/UpgradeList
@onready var _btn_mining   : Button = $Layout/CategoryTabs/BtnMining
@onready var _btn_transport: Button = $Layout/CategoryTabs/BtnTransport
@onready var _btn_station  : Button = $Layout/CategoryTabs/BtnStation
@onready var _btn_research : Button = $Layout/CategoryTabs/BtnResearch

var _active_category := "mining"

func _ready() -> void:
	_btn_mining.pressed.connect(func(): _set_category("mining"))
	_btn_transport.pressed.connect(func(): _set_category("transport"))
	_btn_station.pressed.connect(func(): _set_category("station"))
	_btn_research.pressed.connect(func(): _set_category("research"))

	GameState.resources_changed.connect(_refresh_list)
	GameState.upgrade_purchased.connect(func(_id): _refresh_list())
	GameState.module_built.connect(func(_id): _refresh_list())

	_refresh_list()

func _set_category(cat: String) -> void:
	_active_category = cat
	_refresh_list()
	_highlight_tabs()

func _highlight_tabs() -> void:
	var btns := [_btn_mining, _btn_transport, _btn_station, _btn_research]
	for i in btns.size():
		btns[i].modulate = Color.WHITE if CATEGORIES[i] == _active_category else Color(0.5, 0.5, 0.5)

func _refresh_list() -> void:
	for child in _upgrade_list.get_children():
		child.queue_free()

	for upgrade_id in UpgradeManager.UPGRADES:
		var udata := UpgradeManager.UPGRADES[upgrade_id] as Dictionary
		if udata.get("category", "") != _active_category:
			continue
		if not UpgradeManager.is_upgrade_visible(upgrade_id):
			continue
		_upgrade_list.add_child(_make_upgrade_row(upgrade_id, udata))

func _make_upgrade_row(upgrade_id: String, udata: Dictionary) -> PanelContainer:
	var panel := PanelContainer.new()
	panel.custom_minimum_size = Vector2(0, 140)

	var hbox := HBoxContainer.new()
	hbox.set("theme_override_constants/separation", 12)
	panel.add_child(hbox)

	var purchased := GameState.has_upgrade(upgrade_id)

	# Icon
	var icon_lbl := Label.new()
	icon_lbl.text = udata.get("icon", "?")
	icon_lbl.custom_minimum_size = Vector2(56, 0)
	icon_lbl.add_theme_font_size_override("font_size", 34)
	icon_lbl.vertical_alignment = VERTICAL_ALIGNMENT_CENTER
	if purchased:
		icon_lbl.modulate = Color(0.5, 0.5, 0.5)
	hbox.add_child(icon_lbl)

	# Info
	var info := VBoxContainer.new()
	info.size_flags_horizontal = Control.SIZE_EXPAND_FILL
	hbox.add_child(info)

	var name_lbl := Label.new()
	name_lbl.text = udata.get("name", upgrade_id)
	name_lbl.add_theme_font_size_override("font_size", 24)
	var name_color := Color(0.3, 1.0, 0.6) if not purchased else Color(0.4, 0.6, 0.4)
	name_lbl.add_theme_color_override("font_color", name_color)
	info.add_child(name_lbl)

	var effect_lbl := Label.new()
	effect_lbl.text = udata.get("effect", "")
	effect_lbl.add_theme_font_size_override("font_size", 18)
	effect_lbl.add_theme_color_override("font_color", Color(1.0, 0.85, 0.4))
	effect_lbl.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
	info.add_child(effect_lbl)

	var desc_lbl := Label.new()
	desc_lbl.text = udata.get("description", "")
	desc_lbl.add_theme_font_size_override("font_size", 15)
	desc_lbl.add_theme_color_override("font_color", Color(0.6, 0.6, 0.6))
	desc_lbl.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
	info.add_child(desc_lbl)

	# Right side: cost + button
	var right := VBoxContainer.new()
	right.custom_minimum_size = Vector2(200, 0)
	hbox.add_child(right)

	if purchased:
		var done_lbl := Label.new()
		done_lbl.text = "KØBT ✓"
		done_lbl.add_theme_color_override("font_color", Color(0.3, 1.0, 0.5))
		done_lbl.add_theme_font_size_override("font_size", 22)
		done_lbl.vertical_alignment = VERTICAL_ALIGNMENT_CENTER
		right.add_child(done_lbl)
		return panel

	# Cost display
	var cost_p := udata.get("cost_planet", {}) as Dictionary
	var cost_s := udata.get("cost_station", {}) as Dictionary
	if not cost_p.is_empty():
		var cost_lbl := Label.new()
		cost_lbl.text = _format_cost(cost_p, "Planet")
		cost_lbl.add_theme_font_size_override("font_size", 16)
		cost_lbl.add_theme_color_override("font_color", Color(0.9, 0.8, 0.4))
		cost_lbl.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
		right.add_child(cost_lbl)
	if not cost_s.is_empty():
		var cost_lbl2 := Label.new()
		cost_lbl2.text = _format_cost(cost_s, "Station")
		cost_lbl2.add_theme_font_size_override("font_size", 16)
		cost_lbl2.add_theme_color_override("font_color", Color(0.7, 0.9, 1.0))
		cost_lbl2.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
		right.add_child(cost_lbl2)

	# Requirement note
	var can_buy := UpgradeManager.can_buy_upgrade(upgrade_id)
	if not can_buy and not GameState.has_upgrade(upgrade_id):
		var req_lbl := Label.new()
		req_lbl.text = _get_requirement_text(udata)
		req_lbl.add_theme_font_size_override("font_size", 14)
		req_lbl.add_theme_color_override("font_color", Color(0.8, 0.4, 0.4))
		req_lbl.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
		right.add_child(req_lbl)

	var btn := Button.new()
	btn.text = "Køb"
	btn.custom_minimum_size = Vector2(0, 56)
	btn.disabled = not can_buy
	btn.pressed.connect(func():
		UpgradeManager.buy_upgrade(upgrade_id)
		_refresh_list()
	)
	right.add_child(btn)

	return panel

func _format_cost(cost: Dictionary, label: String) -> String:
	var parts: Array[String] = []
	for res in cost:
		var rdata := ResourceManager.RESOURCE_DATA.get(res, {}) as Dictionary
		parts.append("%s %s" % [ResourceManager.format_number(cost[res]), rdata.get("symbol", res)])
	return "%s: %s" % [label, ", ".join(parts)]

func _get_requirement_text(udata: Dictionary) -> String:
	var reqs: Array = udata.get("requires", [])
	var missing: Array[String] = []
	for r in reqs:
		if not GameState.has_upgrade(r):
			var rdata := UpgradeManager.UPGRADES.get(r, {}) as Dictionary
			missing.append(rdata.get("name", r))
	var req_mod: String = udata.get("requires_module", "")
	if req_mod != "" and not GameState.has_module(req_mod):
		var mdata := UpgradeManager.MODULES.get(req_mod, {}) as Dictionary
		missing.append("Modul: " + mdata.get("name", req_mod))
	if missing.is_empty():
		return "Ikke råd"
	return "Kræver: " + ", ".join(missing)
