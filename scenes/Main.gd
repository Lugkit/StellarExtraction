extends Control

const PlanetScene    := preload("res://scenes/planet/PlanetView.tscn")
const TransportScene := preload("res://scenes/transport/TransportView.tscn")
const StationScene   := preload("res://scenes/space/SpaceView.tscn")
const UpgradeScene   := preload("res://scenes/upgrades/UpgradeView.tscn")

var _views: Array[Control] = []
var _active_index := 0

@onready var _view_container : Control = $ViewContainer
@onready var _btn_planet    : Button = $BottomNav/NavRow/BtnPlanet
@onready var _btn_transport : Button = $BottomNav/NavRow/BtnTransport
@onready var _btn_station   : Button = $BottomNav/NavRow/BtnStation
@onready var _btn_upgrades  : Button = $BottomNav/NavRow/BtnUpgrades

func _ready() -> void:
	_build_views()
	_connect_nav()
	_show_view(0)

func _build_views() -> void:
	var scenes := [PlanetScene, TransportScene, StationScene, UpgradeScene]
	for s in scenes:
		var v := s.instantiate() as Control
		v.layout_mode = 1
		v.anchors_preset = 15
		v.anchor_right = 1.0
		v.anchor_bottom = 1.0
		v.visible = false
		_view_container.add_child(v)
		_views.append(v)

func _connect_nav() -> void:
	_btn_planet.pressed.connect(func(): _show_view(0))
	_btn_transport.pressed.connect(func(): _show_view(1))
	_btn_station.pressed.connect(func(): _show_view(2))
	_btn_upgrades.pressed.connect(func(): _show_view(3))

func _show_view(index: int) -> void:
	for i in _views.size():
		_views[i].visible = (i == index)
	_active_index = index
	_highlight_nav(index)

func _highlight_nav(index: int) -> void:
	var btns := [_btn_planet, _btn_transport, _btn_station, _btn_upgrades]
	for i in btns.size():
		btns[i].modulate = Color.WHITE if i == index else Color(0.6, 0.6, 0.6)
