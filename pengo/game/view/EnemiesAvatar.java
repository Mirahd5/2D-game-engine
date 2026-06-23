package game.view;

import java.util.HashMap;
import java.util.Map;

import engine.model.Entity;
import engine.model.ISU;
import engine.view.Avatar;
import engine.view.SpawnAnimation;
import engine.view.View;
import game.model.Enemies;
import game.model.Glacon;
import game.model.Mur;
import game.model.Spawner;
import oop.graphics.BufferedImage;
import oop.graphics.Graphics;

public final class EnemiesAvatar extends Avatar {

	private final Enemies enemy;
	private final Map<String, BufferedImage> images;
	private final SpawnAnimation spawnAnimation;
	private long crushedElapsed_ms;

	public EnemiesAvatar(View view, Enemies enemy) {
		super(view, enemy);

		this.enemy = enemy;
		this.images = new HashMap<String, BufferedImage>();
		this.spawnAnimation = new SpawnAnimation();
		this.crushedElapsed_ms = 0L;
	}

	@Override
	public void tick(long elapsed) {
		assert elapsed >= 0L;

		this.spawnAnimation.tick(elapsed);

		if (this.enemy.isCrushed()) {
			this.crushedElapsed_ms += elapsed;

			if (this.crushedElapsed_ms >= Enemies.CRUSH_DISPLAY_MS) {
				this.enemy.finishCrushDeath();
			}
		} else {
			this.crushedElapsed_ms = 0L;
		}
	}

	@Override
	public void paint(Graphics graphics) {
		assert graphics != null;

		BufferedImage image = imageForStateAndOrientation(graphics);

		double revealScale = this.view.revealScale(this.enemy);

		if (revealScale <= 0.0) {
			return;
		}

		if (this.enemy.isCrushed()) {
			paintCrushed(graphics, image, revealScale);
			return;
		}

		int centerX = this.view.toPixelX(this.enemy.center().x());
		int centerY = this.view.toPixelY(this.enemy.center().y());

		int width = this.view.toPixelWidth(this.enemy.size().x() * revealScale);
		int height = this.view.toPixelHeight(this.enemy.size().y() * revealScale);

		graphics.drawImage(image, centerX - width / 2, centerY - height / 2, width, height);
	}

	private void paintCrushed(Graphics graphics, BufferedImage image, double revealScale) {
		CrushBox box = dynamicCrushBox();

		double centerX_cm;
		double centerY_cm;
		double width_cm;
		double height_cm;

		if (box != null) {
			centerX_cm = box.centerX_cm;
			centerY_cm = box.centerY_cm;
			width_cm = box.width_cm;
			height_cm = box.height_cm;
		} else {
			centerX_cm = this.enemy.center().x();
			centerY_cm = this.enemy.center().y();
			width_cm = this.enemy.crushedDisplayWidthCm();
			height_cm = this.enemy.crushedDisplayHeightCm();
		}

		int centerX = this.view.toPixelX(centerX_cm);
		int centerY = this.view.toPixelY(centerY_cm);
		int width = Math.max(1, this.view.toPixelWidth(width_cm * revealScale));
		int height = Math.max(1, this.view.toPixelHeight(height_cm * revealScale));

		graphics.drawImage(image, centerX - width / 2, centerY - height / 2, width, height);
	}

	/**
	 * Calcule la zone exacte de dessin de l'ennemi ecrase en ISU.
	 *
	 * Important : on n'utilise pas la cellule de l'ennemi, ni une distance
	 * centre-a-centre. On utilise l'intervalle bord-a-bord entre le pousseur et
	 * le recepteur, dans la direction du glissement. Cela evite que l'image soit
	 * dessinee sur le glacon pousseur.
	 */
	private CrushBox dynamicCrushBox() {
		Entity pusher = this.enemy.crushPusher();
		Entity obstacle = this.enemy.crushObstacle();

		if (pusher == null || obstacle == null || !pusher.alive() || !obstacle.alive()) {
			return null;
		}

		int direction = normalizeCardinal(this.enemy.crushDirection());
		double cell = this.enemy.model().isu().cmPerCell();
		double minThickness = cell * 0.025;
		double crossSize = cell * 1.03;

		ISU.Vector towardObstacle = pusher.center().mkVectorToward(obstacle.center());
		double centerDistance = forwardProjection(towardObstacle, direction);

		if (centerDistance <= 0.0) {
			return null;
		}

		double pusherHalf = solidHalfSizeOnAxis(pusher, direction, cell);
		double obstacleHalf = solidHalfSizeOnAxis(obstacle, direction, cell);
		double realGap = centerDistance - pusherHalf - obstacleHalf;
		double visibleGap = Math.max(minThickness, realGap);

		double distanceFromPusherCenter = pusherHalf + visibleGap / 2.0;
		ISU.Coord center = pusher.center().mkTranslated(offset(direction, distanceFromPusherCenter));

		if (direction == 0 || direction == 180) {
			return new CrushBox(center.x(), center.y(), visibleGap, crossSize);
		}

		return new CrushBox(center.x(), center.y(), crossSize, visibleGap);
	}

	private double solidHalfSizeOnAxis(Entity entity, int direction, double cell) {
		
		if (entity instanceof Glacon || entity instanceof Mur || entity instanceof Spawner) {
			return cell / 2.0;
		}

		if (direction == 0 || direction == 180) {
			return entity.size().x() / 2.0;
		}

		return entity.size().y() / 2.0;
	}

	private ISU.Vector offset(int direction, double distance_cm) {
		switch (direction) {
		case 0:
			return this.enemy.model().isu().new Vector(distance_cm, 0.0);

		case 90:
			return this.enemy.model().isu().new Vector(0.0, distance_cm);

		case 180:
			return this.enemy.model().isu().new Vector(-distance_cm, 0.0);

		case 270:
			return this.enemy.model().isu().new Vector(0.0, -distance_cm);

		default:
			throw new AssertionError("Not a cardinal direction: " + direction);
		}
	}

	private static double forwardProjection(ISU.Vector vector, int direction) {
		switch (direction) {
		case 0:
			return vector.x();

		case 90:
			return vector.y();

		case 180:
			return -vector.x();

		case 270:
			return -vector.y();

		default:
			throw new AssertionError("Not a cardinal direction: " + direction);
		}
	}

	private static final class CrushBox {
		final double centerX_cm;
		final double centerY_cm;
		final double width_cm;
		final double height_cm;

		CrushBox(double centerX_cm, double centerY_cm, double width_cm, double height_cm) {
			this.centerX_cm = centerX_cm;
			this.centerY_cm = centerY_cm;
			this.width_cm = width_cm;
			this.height_cm = height_cm;
		}
	}

	private BufferedImage imageForStateAndOrientation(Graphics graphics) {
		if (this.enemy.isCrushed()) {
			String path = this.enemy.crushedHorizontal() ? "pengo_rsrc/ecrasee_horizontal.png"
					: "pengo_rsrc/ecrasee_vertical.png";
			return imageOrNull(graphics, path);
		}

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
		return imageOrNull(graphics, path);
	}

	private BufferedImage imageOrNull(Graphics graphics, String path) {

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