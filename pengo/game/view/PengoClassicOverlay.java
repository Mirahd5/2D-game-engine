package game.view;

import java.util.EnumMap;
import java.util.Map;

import engine.view.Overlay;
import engine.view.View;
import game.model.PengoGameState;
import game.model.PowerType;
import oop.graphics.BufferedImage;
import oop.graphics.Font;
import oop.graphics.Graphics;

public final class PengoClassicOverlay implements Overlay {

	private static final int HEIGHT_PIXEL = 46;
	private static final int ICON_PIXEL = 20;
	private static final int POWER_PIXEL = 20;

	private final PengoGameState state;
	private final Map<PowerType, BufferedImage> powerImages;

	private BufferedImage lifeImage;
	private BufferedImage enemyImage;

	private boolean imagesLoaded;
	private long finishedElapsedMs;

	public PengoClassicOverlay(PengoGameState state) {
		assert state != null;

		this.state = state;
		this.powerImages = new EnumMap<PowerType, BufferedImage>(PowerType.class);

		this.imagesLoaded = false;
		this.finishedElapsedMs = 0L;
	}

	@Override
	public int widthPixel() {
		return 0;
	}

	@Override
	public int heightPixel() {
		return HEIGHT_PIXEL;
	}

	@Override
	public void tick(long elapsed) {
		assert elapsed >= 0L;

		this.state.update();

		if (this.state.finished()) {
			this.finishedElapsedMs += elapsed;
		} else {
			this.finishedElapsedMs = 0L;
		}
	}

	@Override
	public void paint(Graphics graphics, View view, int x, int y) {
		assert graphics != null;
		assert view != null;

		this.state.update();

		loadImages(graphics);

		Font oldFont = graphics.getFont();
		Font hudFont = graphics.getFont("Monospaced", Font.BOLD, 13);

		graphics.setFont(hudFont);

		paintBackground(graphics, view, x, y);
		paintScore(graphics, view, x, y);
		paintLives(graphics, x, y);
		paintEnemies(graphics, view, x, y);
		paintPowers(graphics, view, x, y);
		paintFinishAnimation(graphics, view);

		graphics.setFont(oldFont);
	}

	private void paintBackground(Graphics graphics, View view, int x, int y) {
		graphics.setColor(graphics.getColor(245, 0, 0, 0));
		graphics.fillRect(x, y, view.widthPixel(), HEIGHT_PIXEL);

		graphics.setColor(Graphics.Colors.blue);
		graphics.drawLine(x, y + HEIGHT_PIXEL - 1, x + view.widthPixel(), y + HEIGHT_PIXEL - 1);
	}

	private void paintScore(Graphics graphics, View view, int x, int y) {
		graphics.setColor(Graphics.Colors.yellow);
		graphics.drawString("SCORE", x + 14, y + 16);

		graphics.setColor(Graphics.Colors.white);
		graphics.drawString(String.valueOf(this.state.score()), x + 14, y + 36);
	}

	private void paintLives(Graphics graphics, int x, int y) {
		int startX = x + 115;

		graphics.setColor(Graphics.Colors.cyan);
		graphics.drawString("LIVES", startX, y + 16);

		int iconX = startX + 54;
		int iconY = y + 21;

		for (int i = 0; i < this.state.lives(); i++) {
			graphics.drawImage(this.lifeImage, iconX + i * (ICON_PIXEL + 3), iconY, ICON_PIXEL, ICON_PIXEL);
		}
	}

	private void paintEnemies(Graphics graphics, View view, int x, int y) {
		int startX = Math.max(260, view.widthPixel() / 2 - 75);

		graphics.setColor(Graphics.Colors.green);
		graphics.drawString("ENEMIES", startX, y + 16);

		graphics.drawImage(this.enemyImage, startX + 75, y + 21, ICON_PIXEL, ICON_PIXEL);

		graphics.setColor(Graphics.Colors.white);
		graphics.drawString(this.state.livingEnemies() + "/" + this.state.targetTotalEnemies(), startX + 100, y + 36);
	}

	private void paintPowers(Graphics graphics, View view, int x, int y) {
		int startX = Math.max(430, view.widthPixel() - 330);

		graphics.setColor(Graphics.Colors.magenta);
		graphics.drawString("POWERS", startX, y + 16);

		int px = startX + 70;
		int py = y + 21;

		boolean hasPower = false;

		for (PowerType type : PowerType.values()) {
			boolean active = this.state.powerActive(type);

			if (!active) {
				continue;
			}

			hasPower = true;

			BufferedImage image = this.powerImages.get(type);

			if (image != null) {
				graphics.drawImage(image, px, py, POWER_PIXEL, POWER_PIXEL);
			}

			long remaining = this.state.powerRemainingMs(type);

			graphics.setColor(Graphics.Colors.white);

			if (remaining > 0L) {
				graphics.drawString(formatSeconds(remaining), px + POWER_PIXEL + 3, y + 36);
			} else {
				graphics.drawString(shortLabel(type), px + POWER_PIXEL + 3, y + 36);
			}

			px += 58;
		}

		if (!hasPower) {
			graphics.setColor(Graphics.Colors.lightGray);
			graphics.drawString("NONE", px, y + 36);
		}
	}

	private void paintFinishAnimation(Graphics graphics, View view) {
		if (!this.state.finished()) {
			return;
		}

		boolean visible = (this.finishedElapsedMs / 280L) % 2L == 0L;

		if (!visible) {
			return;
		}

		String message = this.state.won() ? "YOU WIN" : "GAME OVER";

		int boxWidth = 240;
		int boxHeight = 58;

		int x = view.worldOffsetXPixel() + view.renderedWorldWidthPixel() / 2 - boxWidth / 2;
		int y = view.worldOffsetYPixel() + view.renderedWorldHeightPixel() / 2 - boxHeight / 2;

		graphics.setColor(graphics.getColor(235, 0, 0, 0));
		graphics.fillRect(x, y, boxWidth, boxHeight);

		graphics.setColor(this.state.won() ? Graphics.Colors.green : Graphics.Colors.red);
		graphics.drawRect(x, y, boxWidth, boxHeight);
		graphics.drawRect(x + 3, y + 3, boxWidth - 6, boxHeight - 6);

		Font oldFont = graphics.getFont();
		Font messageFont = graphics.getFont("Monospaced", Font.BOLD, 22);

		graphics.setFont(messageFont);
		graphics.setColor(Graphics.Colors.white);
		graphics.drawString(message, x + 55, y + 37);
		graphics.setFont(oldFont);
	}

	private void loadImages(Graphics graphics) {
		if (this.imagesLoaded) {
			return;
		}

		this.lifeImage = graphics.load("pengo_rsrc/pengo_down1.png");
		this.enemyImage = graphics.load("pengo_rsrc/normal_down.png");

		this.powerImages.put(PowerType.SHIELD, graphics.load("pengo_rsrc/shield.png"));
		this.powerImages.put(PowerType.SPEED, graphics.load("pengo_rsrc/piment.png"));
		this.powerImages.put(PowerType.FREEZE, graphics.load("pengo_rsrc/bonbon.png"));
		this.powerImages.put(PowerType.STAR, graphics.load("pengo_rsrc/etoile.png"));
		this.powerImages.put(PowerType.EXTRA_LIFE, graphics.load("pengo_rsrc/crevette.png"));
		this.powerImages.put(PowerType.BOOMFISH, graphics.load("pengo_rsrc/boom_fish.png"));
		this.powerImages.put(PowerType.FRIEND_ACTIVATOR, graphics.load("pengo_rsrc/friend_activator.png"));

		this.imagesLoaded = true;
	}

	private static String formatSeconds(long remainingMs) {
		long seconds = (remainingMs + 999L) / 1000L;

		return seconds + "s";
	}

	private static String shortLabel(PowerType type) {
		switch (type) {
		case SHIELD:
			return "SH";

		case SPEED:
			return "SP";

		case FREEZE:
			return "FR";

		case STAR:
			return "ST";

		case EXTRA_LIFE:
			return "LIFE";

		case BOOMFISH:
			return "BOOM";

		case FRIEND_ACTIVATOR:
			return "FRND";

		default:
			return "PWR";
		}
	}
}