package game.controller;

import engine.brain.PlayerBot;
import engine.model.Entity;
import game.model.Friend;
import game.model.Pengo;
import game.model.Player;
import oop.graphics.Canvas;
import oop.graphics.VirtualKeyCodes;

public final class PengoController implements Canvas.KeyListener {

	private final Switch controlSwitcher;

	public PengoController(Switch controlSwitcher) {
		assert controlSwitcher != null;
		this.controlSwitcher = controlSwitcher;
	}

	@Override
	public void pressed(Canvas canvas, int keyCode, char keyChar) {
		this.controlSwitcher.updateControlledEntity();
		switch (keyCode) {
		case VirtualKeyCodes.VK_RIGHT:
		case VirtualKeyCodes.VK_D:
			moveOnly(0);
			break;

		case VirtualKeyCodes.VK_DOWN:
		case VirtualKeyCodes.VK_S:
			moveOnly(90);
			break;

		case VirtualKeyCodes.VK_LEFT:
		case VirtualKeyCodes.VK_Q:
			moveOnly(180);
			break;

		case VirtualKeyCodes.VK_UP:
		case VirtualKeyCodes.VK_Z:
			moveOnly(270);
			break;

		case VirtualKeyCodes.VK_SPACE:
			useAction();
			break;

		case 9: // VK_TAB
			this.controlSwitcher.switchControl();
			break;

		default:
			break;
		}
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

		if (controlled instanceof Pengo) {
			((Pengo) controlled).useIceAhead();
		} else if (controlled instanceof Friend) {
			((Friend) controlled).useIceAhead();
		}
	}

	@Override
	public void released(Canvas canvas, int keyCode, char keyChar) {
	}

	@Override
	public void typed(Canvas canvas, char keyChar) {
	}
}