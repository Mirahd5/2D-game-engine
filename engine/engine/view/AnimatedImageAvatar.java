package engine.view;

import engine.model.Entity;

/**
 * Avatar image avec animation par frames.
 */
public abstract class AnimatedImageAvatar extends ImageAvatar {

	private final long frameDuration;

	private long elapsedSinceFrame;
	private int lastOrientation;

	public AnimatedImageAvatar(View view, Entity entity, String[] paths, long frameDuration) {
		super(view, entity, paths);

		assert frameDuration > 0L : frameDuration;

		this.frameDuration = frameDuration;
		this.elapsedSinceFrame = 0L;
		this.lastOrientation = entity.orientation();
	}

	public final long frameDuration() {
		return this.frameDuration;
	}

	protected boolean shouldAnimate() {
		return this.entity.isMoving();
	}

	protected boolean resetOnOrientationChange() {
		return true;
	}

	protected final void resetAnimation() {
		this.elapsedSinceFrame = 0L;
		resetFrame();
	}

	@Override
	public void tick(long elapsed) {
		assert elapsed >= 0L : elapsed;

		tickSpawnAnimation(elapsed);

		if (!this.entity.alive() || !this.entity.installed()) {
			resetAnimation();
			return;
		}

		updateOrientation();

		if (!shouldAnimate()) {
			resetAnimation();
			return;
		}

		advanceAnimation(elapsed);
	}

	private void updateOrientation() {
		int orientation = this.entity.orientation();

		if (!resetOnOrientationChange()) {
			this.lastOrientation = orientation;
			return;
		}

		if (orientation == this.lastOrientation) {
			return;
		}

		this.lastOrientation = orientation;
		resetAnimation();
	}

	private void advanceAnimation(long elapsed) {
		this.elapsedSinceFrame += elapsed;

		while (this.elapsedSinceFrame >= this.frameDuration) {
			this.elapsedSinceFrame -= this.frameDuration;
			nextFrame();
		}
	}
}