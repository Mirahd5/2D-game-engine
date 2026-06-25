package engine.controller;

import oop.graphics.Canvas;

/**
 * Contrôleur souris qui transforme les boutons gauche/droit en commandes de
 * rotation.
 */
public final class MouseController extends Controller {

	private static final int LEFT_BUTTON = 1;
	private static final int RIGHT_BUTTON = 3;

	public enum State {
		NO_ROT, LEFT_ROT, RIGHT_ROT, LEFT_ROT_RIGHT_PRESSED, RIGHT_ROT_LEFT_PRESSED
	}

	private final RotationCommands commands;

	private State state;

	public MouseController(RotationCommands commands) {
		this.commands = commands;
		this.state = State.NO_ROT;
	}

	public State state() {
		if (this.state == State.LEFT_ROT_RIGHT_PRESSED) {
			return State.LEFT_ROT;
		}

		if (this.state == State.RIGHT_ROT_LEFT_PRESSED) {
			return State.RIGHT_ROT;
		}

		return this.state;
	}

	@Override
	protected void mousePressed(Canvas canvas, int button, int x, int y) {
		switch (this.state) {
		case NO_ROT:
			startRotation(button);
			break;

		case LEFT_ROT:
			if (button == RIGHT_BUTTON) {
				this.state = State.LEFT_ROT_RIGHT_PRESSED;
			}
			break;

		case RIGHT_ROT:
			if (button == LEFT_BUTTON) {
				this.state = State.RIGHT_ROT_LEFT_PRESSED;
			}
			break;

		case LEFT_ROT_RIGHT_PRESSED:
		case RIGHT_ROT_LEFT_PRESSED:
			break;

		default:
			throw new AssertionError("Unknown mouse-controller state");
		}
	}

	@Override
	protected void mouseReleased(Canvas canvas, int x, int y, int button) {
		switch (this.state) {
		case NO_ROT:
			break;

		case LEFT_ROT:
			if (button == LEFT_BUTTON) {
				this.commands.stopTurnLeft();
				this.state = State.NO_ROT;
			}
			break;

		case RIGHT_ROT:
			if (button == RIGHT_BUTTON) {
				this.commands.stopTurnRight();
				this.state = State.NO_ROT;
			}
			break;

		case LEFT_ROT_RIGHT_PRESSED:
			releaseWhenLeftHasPriority(button);
			break;

		case RIGHT_ROT_LEFT_PRESSED:
			releaseWhenRightHasPriority(button);
			break;

		default:
			throw new AssertionError("Unknown mouse-controller state");
		}
	}

	private void startRotation(int button) {
		if (button == LEFT_BUTTON) {
			this.commands.startTurnLeft();
			this.state = State.LEFT_ROT;
			return;
		}

		if (button == RIGHT_BUTTON) {
			this.commands.startTurnRight();
			this.state = State.RIGHT_ROT;
		}
	}

	private void releaseWhenLeftHasPriority(int button) {
		if (button == RIGHT_BUTTON) {
			this.state = State.LEFT_ROT;
			return;
		}

		if (button == LEFT_BUTTON) {
			this.commands.stopTurnLeft();
			this.state = State.NO_ROT;
		}
	}

	private void releaseWhenRightHasPriority(int button) {
		if (button == LEFT_BUTTON) {
			this.state = State.RIGHT_ROT;
			return;
		}

		if (button == RIGHT_BUTTON) {
			this.commands.stopTurnRight();
			this.state = State.NO_ROT;
		}
	}
}