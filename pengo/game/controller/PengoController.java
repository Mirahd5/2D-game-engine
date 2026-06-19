package game.controller;

import java.util.HashSet;
import java.util.Set;

import engine.brain.PlayerBot;
import engine.model.Entity;
import engine.model.Orientation;
import game.model.Player;
import oop.graphics.Canvas;
import oop.graphics.VirtualKeyCodes;

public final class PengoController implements Canvas.KeyListener {

	private final Switch controlSwitcher;
	private final boolean continuousWalk;

	private final Set<Integer> pressedKeys;
	private Integer continuousDirection;

	public PengoController(Switch controlSwitcher) {
		this(controlSwitcher, false);
	}

	public PengoController(Switch controlSwitcher, boolean continuousWalk) {
		assert controlSwitcher != null;

		this.controlSwitcher = controlSwitcher;
		this.continuousWalk = continuousWalk;
		this.pressedKeys = new HashSet<Integer>();
		this.continuousDirection = null;
	}

	public boolean continuousWalk() {
		return this.continuousWalk;
	}

	@Override
	public void pressed(Canvas canvas, int keyCode, char keyChar) {
		this.controlSwitcher.updateControlledEntity();

		switch (keyCode) {
		case VirtualKeyCodes.VK_RIGHT:
		case VirtualKeyCodes.VK_D:
			directionPressed(keyCode, 0);
			break;

		case VirtualKeyCodes.VK_DOWN:
		case VirtualKeyCodes.VK_S:
			directionPressed(keyCode, 90);
			break;

		case VirtualKeyCodes.VK_LEFT:
		case VirtualKeyCodes.VK_Q:
			directionPressed(keyCode, 180);
			break;

		case VirtualKeyCodes.VK_UP:
		case VirtualKeyCodes.VK_Z:
			directionPressed(keyCode, 270);
			break;

		case VirtualKeyCodes.VK_SPACE:
			if (this.pressedKeys.add(keyCode)) {
				useAction();
			}
			break;

		case 9: // TAB
			if (this.pressedKeys.add(keyCode)) {
				this.controlSwitcher.switchControl();
			}
			break;

		default:
			break;
		}
	}

	private void directionPressed(int keyCode, int direction) {
		boolean firstPress = this.pressedKeys.add(keyCode);

		if (this.continuousWalk) {
			this.continuousDirection = Orientation.normalizeCardinal(direction);
		}

		if (!firstPress && !this.continuousWalk) {
			return;
		}

		moveOnly(direction);
	}

	public void updateContinuousWalk() {
		if (!this.continuousWalk) {
			return;
		}

		if (this.continuousDirection == null) {
			return;
		}

		moveOnly(this.continuousDirection);
	}

	private void moveOnly(int direction) {
		this.controlSwitcher.updateControlledEntity();

		Entity controlled = this.controlSwitcher.controlledEntity();

		if (!(controlled instanceof Player)) {
			return;
		}

		Player player = (Player) controlled;

		if (player.faceIceAhead(direction)) {
			return;
		}

		PlayerBot bot = this.controlSwitcher.playerBot();

		if (bot == null) {
			return;
		}

		bot.wish(direction);
	}

	private void useAction() {
		this.controlSwitcher.updateControlledEntity();

		Entity controlled = this.controlSwitcher.controlledEntity();

		if (controlled instanceof Player) {
			Player player = (Player) controlled;
			player.useIceAhead();
		}
	}

	@Override
	public void released(Canvas canvas, int keyCode, char keyChar) {
		this.pressedKeys.remove(keyCode);

		Integer releasedDirection = directionForKey(keyCode);

		if (releasedDirection == null) {
			return;
		}

		Integer stillPressedDirection = findStillPressedDirection();

		if (this.continuousWalk) {
			if (releasedDirection.equals(this.continuousDirection)) {
				this.continuousDirection = stillPressedDirection;
			}
		}

		if (stillPressedDirection == null) {
			PlayerBot bot = this.controlSwitcher.playerBot();

			if (bot != null) {
				bot.stopWishing();
			}
		}
	}

	private Integer findStillPressedDirection() {
		for (Integer key : this.pressedKeys) {
			Integer direction = directionForKey(key);

			if (direction != null) {
				return direction;
			}
		}

		return null;
	}

	private Integer directionForKey(int keyCode) {
		switch (keyCode) {
		case VirtualKeyCodes.VK_RIGHT:
		case VirtualKeyCodes.VK_D:
			return 0;

		case VirtualKeyCodes.VK_DOWN:
		case VirtualKeyCodes.VK_S:
			return 90;

		case VirtualKeyCodes.VK_LEFT:
		case VirtualKeyCodes.VK_Q:
			return 180;

		case VirtualKeyCodes.VK_UP:
		case VirtualKeyCodes.VK_Z:
			return 270;

		default:
			return null;
		}
	}

	@Override
	public void typed(Canvas canvas, char keyChar) {
		// Non utilisé.
	}
}