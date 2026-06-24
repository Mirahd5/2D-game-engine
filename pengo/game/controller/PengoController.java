package game.controller;

import java.util.HashSet;
import java.util.Set;

import engine.brain.PlayerBot;
import engine.controller.Controller;
import engine.model.Entity;
import engine.model.Orientation;
import game.model.PengoGameState;
import game.model.Player;
import oop.graphics.Canvas;
import oop.graphics.VirtualKeyCodes;
import engine.view.View;
import engine.view.DebugOptions;

public final class PengoController extends Controller {

	private final Switch controlSwitcher;
	private final boolean continuousWalk;

	private PengoGameState gameState;

	private final View view;

	private final Set<Integer> pressedKeys;
	private Integer continuousDirection;

	// debug
	private final DebugOptions debugOptions;

	private static final int KEY_F1 = 112;
	private static final int KEY_F2 = 113;
	private static final int KEY_F3 = 114;
	private static final int KEY_F4 = 115;
	private static final int KEY_F5 = 116;

	public PengoController(Switch controlSwitcher, View view, boolean continuousWalk) {
		this(controlSwitcher, view, continuousWalk, null);
	}

	public PengoController(Switch controlSwitcher, View view, boolean continuousWalk, DebugOptions debugOptions) {
		assert controlSwitcher != null;
		assert view != null;

		this.controlSwitcher = controlSwitcher;
		this.view = view;
		this.continuousWalk = continuousWalk;
		this.debugOptions = debugOptions;
		this.gameState = null;

		this.pressedKeys = new HashSet<Integer>();
		this.continuousDirection = null;
	}

	public void setGameState(PengoGameState gameState) {
		assert gameState != null;
		this.gameState = gameState;
	}

	public void clearInputs() {
		this.pressedKeys.clear();
		this.continuousDirection = null;

		PlayerBot bot = this.controlSwitcher.playerBot();

		if (bot != null) {
			bot.stopWishing();
		}
	}

	public boolean continuousWalk() {
		return this.continuousWalk;
	}
	
	public boolean gameFinished() {
		return this.gameState != null && this.gameState.finished();
	}

	public boolean gameWon() {
		return this.gameState != null && this.gameState.won();
	}

	public boolean gameLost() {
		return this.gameState != null && this.gameState.lost();
	}

	// =========================
	// Keyboard
	// =========================

	@Override
	public void pressed(Canvas canvas, int keyCode, char keyChar) {

		if (handleDebugKey(keyCode)) {
			return;
		}
		
		if (!enabled()) {
			return;
		}

		if (this.gameState != null && this.gameState.finished()) {
			return;
		}

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
		if (!enabled()) {
			return;
		}

		if (this.gameState != null && this.gameState.finished()) {
			return;
		}

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
		if (!enabled()) {
			return;
		}

		if (this.gameState != null && this.gameState.finished()) {
			return;
		}

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

	// =========================
	// Mouse
	// =========================

	@Override
	public void moved(Canvas canvas, int x, int y) {
		// Non utilisé pour l'instant.
	}

	@Override
	public void pressed(Canvas canvas, int button, int x, int y) {
		if (!enabled()) {
			return;
		}

		if (this.gameState != null && this.gameState.finished()) {
			return;
		}

		if (button == 1) {
			int direction = directionFromMouseClick(x, y);
			faceOnly(direction);
			return;
		}

		if (button == 3) {
			useAction();
		}
	}

	private int directionFromMouseClick(int mouseX, int mouseY) {
		this.controlSwitcher.updateControlledEntity();

		Entity controlled = this.controlSwitcher.controlledEntity();

		if (!(controlled instanceof Player)) {
			return 90;
		}

		/*
		 * Si le clic est dans le HUD à droite, on ignore et on garde l'orientation.
		 */
		if (mouseX > this.view.widthPixel()) {
			return Orientation.normalizeCardinal(controlled.orientation());
		}

		int centerX = this.view.toPixelX(controlled.center().x());
		int centerY = this.view.toPixelY(controlled.center().y());

		int dx = mouseX - centerX;
		int dy = mouseY - centerY;

		if (Math.abs(dx) >= Math.abs(dy)) {
			if (dx >= 0) {
				return 0; // droite
			}

			return 180; // gauche
		}

		if (dy >= 0) {
			return 90; // bas
		}

		return 270; // haut
	}

	private void faceOnly(int direction) {
		this.controlSwitcher.updateControlledEntity();

		Entity controlled = this.controlSwitcher.controlledEntity();

		if (!(controlled instanceof Player)) {
			return;
		}

		int normalized = Orientation.normalizeCardinal(direction);

		controlled.setOrientation(normalized);

		PlayerBot bot = this.controlSwitcher.playerBot();

		if (bot != null) {
			bot.stopWishing();
		}
	}

	@Override
	public void released(Canvas canvas, int x, int y, int button) {
		// Rien : un clic fait une rotation immédiate.
	}

	private boolean handleDebugKey(int keyCode) {
		if (this.debugOptions == null) {
			return false;
		}

		switch (keyCode) {
		case KEY_F1:
			this.debugOptions.toggleEnabled();
			return true;

		case KEY_F2:
			this.debugOptions.toggleBoundingBoxes();
			return true;

		case KEY_F3:
			this.debugOptions.toggleActions();
			return true;

		case KEY_F4:
			this.debugOptions.togglePerformance();
			return true;

		case KEY_F5:
			this.debugOptions.toggleLoadTest();
			return true;

		default:
			return false;
		}
	}
}