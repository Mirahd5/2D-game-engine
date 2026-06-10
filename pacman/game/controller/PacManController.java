package game.controller;

import engine.controller.Controller;
import engine.model.BasicStunt;
import oop.graphics.Canvas;
import oop.graphics.VirtualKeyCodes;

public class PacManController extends Controller {

	private final BasicStunt stunt;
	private int currentKeyCode;

	public PacManController(Canvas canvas, BasicStunt stunt) {
		super(canvas);

		assert stunt != null : "stunt must not be null";
		this.stunt = stunt;
		this.currentKeyCode = -1;
	}

	@Override
	public void pressed(Canvas canvas, int keyCode, char keyChar) {
		Integer direction = directionOf(keyCode);

		if (direction == null) {
			return;
		}

		this.currentKeyCode = keyCode;
		this.stunt.walk(direction);
	}

	@Override
	public void released(Canvas canvas, int keyCode, char keyChar) {
		if (keyCode == this.currentKeyCode) {
			this.currentKeyCode = -1;

		}
	}

	private Integer directionOf(int keyCode) {
		switch (keyCode) {
		case VirtualKeyCodes.VK_RIGHT:
			return BasicStunt.EAST;

		case VirtualKeyCodes.VK_LEFT:
			return BasicStunt.WEST;

		case VirtualKeyCodes.VK_UP:
			return BasicStunt.NORTH;

		case VirtualKeyCodes.VK_DOWN:
			return BasicStunt.SOUTH;

		default:
			return null;
		}
	}
}