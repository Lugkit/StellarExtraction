extends PanelContainer

@onready var _symbol : Label       = $Inner/Symbol
@onready var _name   : Label       = $Inner/Info/NameLabel
@onready var _amount : Label       = $Inner/Info/AmountLabel
@onready var _bar    : ProgressBar = $Inner/Info/Bar
@onready var _rate   : Label       = $Inner/Info/RateLabel

var _resource_id := ""
var _color := Color.WHITE

func setup(resource_id: String, data: Dictionary) -> void:
	_resource_id = resource_id
	_color = data.get("color", Color.WHITE)
	_symbol.text = data.get("symbol", "?")
	_symbol.add_theme_color_override("font_color", _color)
	_name.text   = data.get("name", resource_id)

func update_values(amount: float, cap: float, rate: float) -> void:
	_amount.text = "%s / %s" % [
		ResourceManager.format_number(amount),
		ResourceManager.format_number(cap),
	]
	_bar.value = amount / cap if cap > 0.0 else 0.0
	_rate.text  = "+%s/s" % ResourceManager.format_number(rate)

	# Pulse color when near cap
	if amount / cap > 0.95:
		_amount.add_theme_color_override("font_color", Color(1.0, 0.4, 0.3))
	else:
		_amount.add_theme_color_override("font_color", Color(0.8, 0.8, 0.8))
