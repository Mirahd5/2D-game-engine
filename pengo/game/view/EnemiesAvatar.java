package game.view;

import java.util.HashMap;
import java.util.Map;

import engine.view.Avatar;
import engine.view.SpawnAnimation;
import engine.view.View;
import game.model.Enemies;
import oop.graphics.BufferedImage;
import oop.graphics.Graphics;

public final class EnemiesAvatar extends Avatar {

	private final Enemies enemy;
	private final Map<String, BufferedImage> images;
	private final SpawnAnimation spawnAnimation;

	public EnemiesAvatar(View view, Enemies enemy) {
		super(view, enemy);

		this.enemy = enemy;
		this.images = new HashMap<String, BufferedImage>();
		this.spawnAnimation = new SpawnAnimation();
	}

	@Override
	public void tick(long elapsed) {
		assert elapsed >= 0L;

		this.spawnAnimation.tick(elapsed);
	}

	@Override
	public void paint(Graphics graphics) {
		assert graphics != null;

		BufferedImage image = imageForStateAndOrientation(graphics);

		int centerX = this.view.toPixelX(this.enemy.center().x());
		int centerY = this.view.toPixelY(this.enemy.center().y());

		double revealScale = this.view.revealScale(this.enemy);

		if (revealScale <= 0.0) {
			return;
		}

		int width = this.view.toPixelWidth(this.enemy.size().x() * revealScale);
		int height = this.view.toPixelHeight(this.enemy.size().y() * revealScale);

		graphics.drawImage(image, centerX - width / 2, centerY - height / 2, width, height);
	}

	private BufferedImage imageForStateAndOrientation(Graphics graphics) {
		String direction = directionName(this.enemy.orientation());

		if (this.enemy.isFrozen()) {
			BufferedImage frozen = imageOrNull(graphics, "peur", direction);

			if (frozen != null) {
				return frozen;
			}
		}

		String state = normalizeStateName(this.enemy.galState());

		BufferedImage stateImage = imageOrNull(graphics, state, direction);

		if (stateImage != null) {
			return stateImage;
		}

		BufferedImage fallback = imageOrNull(graphics, "normal", direction);

		if (fallback != null) {
			return fallback;
		}

		throw new IllegalStateException("Missing enemy fallback image normal_" + direction + ".png");
	}

	private BufferedImage imageOrNull(Graphics graphics, String state, String direction) {
		String path = "pengo_rsrc/" + state + "_" + direction + ".png";

		if (this.images.containsKey(path)) {
			return this.images.get(path);
		}

		try {
			BufferedImage image = graphics.load(path);
			this.images.put(path, image);
			return image;
		} catch (RuntimeException exception) {
			this.images.put(path, null);
			return null;
		} catch (AssertionError error) {
			this.images.put(path, null);
			return null;
		}
	}

	private static String normalizeStateName(String state) {
		if (state == null || state.trim().length() == 0) {
			return "normal";
		}

		return state.trim().toLowerCase();
	}

	private static String directionName(int orientation) {
		int direction = normalizeCardinal(orientation);

		switch (direction) {
		case 0:
			return "right";

		case 90:
			return "down";

		case 180:
			return "left";

		case 270:
			return "up";

		default:
			return "down";
		}
	}

	private static int normalizeCardinal(int degree) {
		int d = ((degree % 360) + 360) % 360;

		if (d < 45 || d >= 315) {
			return 0;
		}

		if (d < 135) {
			return 90;
		}

		if (d < 225) {
			return 180;
		}

		return 270;
	}
}