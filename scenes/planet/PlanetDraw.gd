extends Node2D

var _time := 0.0

func _process(delta: float) -> void:
	_time += delta
	queue_redraw()

func _draw() -> void:
	var center := Vector2(520, 150)
	var radius := 120.0

	# Planet glow
	for i in range(5, 0, -1):
		var glow_r := radius + i * 12.0
		var alpha  := 0.06 - i * 0.01
		draw_circle(center, glow_r, Color(0.2, 0.5, 1.0, alpha))

	# Planet body gradient (fake via circles)
	draw_circle(center, radius, Color(0.15, 0.35, 0.65))
	draw_circle(center - Vector2(20, -20), radius * 0.85, Color(0.18, 0.42, 0.72))

	# Atmosphere ring
	draw_arc(center, radius + 4, 0, TAU, 64, Color(0.4, 0.7, 1.0, 0.4), 6.0)

	# Animated cloud bands
	for band in range(3):
		var y_off := -30.0 + band * 30.0
		var x_off := fmod(_time * (8.0 + band * 3.0), 400.0) - 200.0
		var band_center := center + Vector2(x_off, y_off)
		draw_circle(band_center, 40.0 - band * 8.0, Color(0.5, 0.7, 1.0, 0.12))

	# Mine nodes
	var mine_count := 3 + GameState.planet_level
	for i in mine_count:
		var angle := (TAU / mine_count) * i + _time * 0.3
		var pos := center + Vector2(cos(angle), sin(angle)) * (radius - 15.0)
		_draw_mine_node(pos, i)

	# Rotating scan line
	var scan_angle := _time * 0.8
	var scan_end := center + Vector2(cos(scan_angle), sin(scan_angle)) * radius
	draw_line(center, scan_end, Color(0.2, 1.0, 0.6, 0.3), 2.0)

func _draw_mine_node(pos: Vector2, index: int) -> void:
	var pulse := 0.5 + 0.5 * sin(_time * 2.0 + index * 1.2)
	var col := Color(1.0, 0.7, 0.2, 0.6 + pulse * 0.4)
	draw_circle(pos, 8.0, col)
	draw_circle(pos, 4.0, Color(1.0, 0.9, 0.5))
