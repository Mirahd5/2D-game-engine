package engine.view;

import java.util.List;

import engine.model.Entity;
import engine.model.Hitbox;
import oop.graphics.Font;
import oop.graphics.Graphics;

/**
 * Dessine les informations de debug de la vue.
 */
final class DebugRenderer {

	private static final int LABEL_OFFSET_X = 28;
	private static final int MIN_BOX_SIZE = 1;

	private DebugRenderer() {
	}

	static void paintWorldDebug(Graphics graphics, View view, List<Entity> entities) {
		DebugOptions options = view.debugOptions();

		if (!options.enabled()) {
			return;
		}

		Font oldFont = graphics.getFont();

		graphics.setFont(graphics.getFont("Monospaced", Font.BOLD, 10));

		for (Entity entity : entities) {
			if (!entity.alive() || !entity.installed()) {
				continue;
			}

			if (options.showBoundingBoxes()) {
				paintBoundingBox(graphics, view, entity);
			}

			if (options.showActions()) {
				paintDebugLabel(graphics, view, entity);
			}
		}

		graphics.setFont(oldFont);
	}

	static void paintPerformance(Graphics graphics, View view) {
		DebugOptions options = view.debugOptions();

		if (!options.showPerformance()) {
			return;
		}

		PerformanceMonitor monitor = view.performanceMonitor();

		int x = 12;
		int y = view.overlayHeightPixel() + 12;
		int width = 330;
		int height = options.loadTest() ? 116 : 98;

		paintPerformanceBackground(graphics, x, y, width, height);
		paintPerformanceText(graphics, view, monitor, x, y);
	}

	private static void paintBoundingBox(Graphics graphics, View view, Entity entity) {
		if (entity.bounding().isEmpty()) {
			return;
		}

		Hitbox hitbox = entity.bounding().hitbox();

		int x = view.toPixelX(hitbox.xmin());
		int y = view.toPixelY(hitbox.ymin());

		int width = view.toPixelWidth(hitbox.xmax() - hitbox.xmin());
		int height = view.toPixelHeight(hitbox.ymax() - hitbox.ymin());

		width = Math.max(width, MIN_BOX_SIZE);
		height = Math.max(height, MIN_BOX_SIZE);

		graphics.setColor(Graphics.Colors.green);
		graphics.drawRect(x, y, width, height);
	}

	private static void paintDebugLabel(Graphics graphics, View view, Entity entity) {
		String label = entity.debugLabel();

		if (label.length() == 0) {
			return;
		}

		int x = view.toPixelX(entity.center().x()) - LABEL_OFFSET_X;
		int y = view.toPixelY(entity.center().y()) - view.toPixelHeight(entity.size().y() / 2.0) - 6;

		int minY = view.overlayHeightPixel() + 10;

		if (y < minY) {
			y = minY;
		}

		graphics.setColor(Graphics.Colors.black);
		graphics.drawString(label, x + 1, y + 1);

		graphics.setColor(Graphics.Colors.yellow);
		graphics.drawString(label, x, y);
	}

	private static void paintPerformanceBackground(Graphics graphics, int x, int y, int width, int height) {
		graphics.setColor(graphics.getColor(210, 0, 0, 0));
		graphics.fillRect(x, y, width, height);

		graphics.setColor(Graphics.Colors.cyan);
		graphics.drawRect(x, y, width, height);
	}

	private static void paintPerformanceText(Graphics graphics, View view, PerformanceMonitor monitor, int x, int y) {
		DebugOptions options = view.debugOptions();

		Font oldFont = graphics.getFont();

		graphics.setFont(graphics.getFont("Monospaced", Font.BOLD, 12));

		graphics.setColor(Graphics.Colors.white);
		graphics.drawString("DEBUG  F1:on/off F2:box F3:state F4:perf F5:load", x + 8, y + 18);

		graphics.setColor(Graphics.Colors.lightGray);
		graphics.drawString("TICK  " + formatMs(monitor.tickStats()), x + 8, y + 40);
		graphics.drawString("PAINT " + formatMs(monitor.paintStats()), x + 8, y + 58);
		graphics.drawString("FPS   " + formatFps(monitor.fpsStats()), x + 8, y + 76);
		graphics.drawString("ENTITIES " + view.model().entities().size(), x + 8, y + 94);

		if (options.loadTest()) {
			graphics.setColor(Graphics.Colors.red);
			graphics.drawString("LOAD TEST ACTIVE", x + 150, y + 94);
		}

		graphics.setFont(oldFont);
	}

	private static String formatMs(TimeStats stats) {
		return "min=" + round(stats.min()) + " avg=" + round(stats.average()) + " max=" + round(stats.max()) + " ms";
	}

	private static String formatFps(TimeStats stats) {
		return "min=" + round(stats.min()) + " avg=" + round(stats.average()) + " max=" + round(stats.max());
	}

	private static int round(double value) {
		return (int) Math.round(value);
	}
}