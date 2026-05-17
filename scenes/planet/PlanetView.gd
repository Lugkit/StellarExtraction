extends Control

const ResourceCardScene := preload("res://scenes/components/ResourceCard.tscn")

@onready var _resource_list : GridContainer = $Layout/ResourceList
@onready var _rate_label    : Label         = $Layout/RateLabel
@onready var _planet_draw   : Node2D        = $Layout/PlanetCanvas/SubViewport/PlanetDraw

var _cards: Dictionary = {}   # resource_id -> ResourceCard
var _time := 0.0

func _ready() -> void:
	_build_cards()
	GameState.resources_changed.connect(_refresh_all)
	GameState.upgrade_purchased.connect(func(_id): _build_cards())

func _process(delta: float) -> void:
	_time += delta
	queue_redraw()   # animate planet
	_update_rates()

func _build_cards() -> void:
	for child in _resource_list.get_children():
		child.queue_free()
	_cards.clear()

	for resource_id in ResourceManager.RESOURCE_DATA:
		var rdata := ResourceManager.RESOURCE_DATA[resource_id] as Dictionary
		if rdata.get("unlock_planet_level", 1) > GameState.planet_level:
			continue
		var card := ResourceCardScene.instantiate()
		_resource_list.add_child(card)
		_cards[resource_id] = card
		card.setup(resource_id, rdata)

	_refresh_all()

func _refresh_all() -> void:
	for resource_id in _cards:
		var card = _cards[resource_id]
		var amount := GameState.planet_resources.get(resource_id, 0.0)
		var cap    := GameState.planet_caps.get(resource_id, 1.0)
		var rate   := ResourceManager.get_rate(resource_id)
		card.update_values(amount, cap, rate)

func _update_rates() -> void:
	var total := 0.0
	for id in ResourceManager.get_all_rates().values():
		total += id
	_rate_label.text = "Total: %s/s" % ResourceManager.format_number(total)

func _draw() -> void:
	_draw_planet()

func _draw_planet() -> void:
	# Drawn on the Node2D in SubViewport — override draw there instead
	pass
