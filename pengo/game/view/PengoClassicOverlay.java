package game.view;

import java.util.EnumMap;
import java.util.Map;

import engine.view.Overlay;
import engine.view.View;
import game.model.PengoGameState;
import game.model.PowerType;
import oop.graphics.BufferedImage;
import oop.graphics.Color;
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
			graphics.drawImage(
				this.lifeImage,
				iconX + i * (ICON_PIXEL + 3),
				iconY,
				ICON_PIXEL,
				ICON_PIXEL
			);
		}
	}

	private void paintEnemies(Graphics graphics, View view, int x, int y) {
		int startX = Math.max(260, view.widthPixel() / 2 - 75);

		graphics.setColor(Graphics.Colors.green);
		graphics.drawString("ENEMIES", startX, y + 16);

		graphics.drawImage(
			this.enemyImage,
			startX + 75,
			y + 21,
			ICON_PIXEL,
			ICON_PIXEL
		);

		graphics.setColor(Graphics.Colors.white);
		graphics.drawString(
			this.state.livingEnemies() + "/" + this.state.targetTotalEnemies(),
			startX + 100,
			y + 36
		);
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

		boolean visible = (this.finishedElapsedMs / 420L) % 2L == 0L;

		if (!visible) {
			return;
		}

		boolean win = this.state.won();

		String bigMessage = win ? "YOU WIN" : "GAME OVER";
		String smallMessage = win ? "STAGE CLEAR" : "TRY AGAIN";

		int worldX = view.worldOffsetXPixel();
		int worldY = view.worldOffsetYPixel();
		int worldW = view.renderedWorldWidthPixel();
		int worldH = view.renderedWorldHeightPixel();

		int panelWidth = Math.min(520, worldW - 40);
		int panelHeight = 210;

		if (panelWidth < 300) {
			panelWidth = 300;
		}

		if (panelHeight > worldH - 30) {
			panelHeight = worldH - 30;
		}

		int panelX = worldX + worldW / 2 - panelWidth / 2;
		int panelY = worldY + worldH / 2 - panelHeight / 2;

		graphics.setColor(graphics.getColor(220, 0, 0, 0));
		graphics.fillRect(panelX, panelY, panelWidth, panelHeight);

		graphics.setColor(win ? Graphics.Colors.yellow : Graphics.Colors.red);
		graphics.drawRect(panelX, panelY, panelWidth, panelHeight);
		graphics.drawRect(panelX + 4, panelY + 4, panelWidth - 8, panelHeight - 8);

		graphics.setColor(Graphics.Colors.white);
		graphics.drawRect(panelX + 8, panelY + 8, panelWidth - 16, panelHeight - 16);

		if (win) {
			drawPixelTrophy(graphics, panelX + panelWidth / 2 - 39, panelY + 18);
		}

		Font oldFont = graphics.getFont();

		Font bigFont = graphics.getFont("Monospaced", Font.BOLD, 46);
		Font smallFont = graphics.getFont("Monospaced", Font.BOLD, 17);

		graphics.setFont(bigFont);

		int titleY = win ? panelY + 132 : panelY + 108;

		drawArcadeText(
			graphics,
			bigMessage,
			panelX,
			titleY,
			panelWidth,
			win ? Graphics.Colors.yellow : Graphics.Colors.red
		);

		graphics.setFont(smallFont);

		drawArcadeText(
			graphics,
			smallMessage,
			panelX,
			titleY + 42,
			panelWidth,
			Graphics.Colors.white
		);

		graphics.setFont(oldFont);
	}

	private void drawArcadeText(
		Graphics graphics,
		String text,
		int panelX,
		int y,
		int panelWidth,
		Color mainColor
	) {
		assert graphics != null;
		assert text != null;
		assert mainColor != null;

		int estimatedWidth;

		if (text.length() <= 10) {
			estimatedWidth = text.length() * 28;
		} else {
			estimatedWidth = text.length() * 11;
		}

		int x = panelX + panelWidth / 2 - estimatedWidth / 2;

		graphics.setColor(Graphics.Colors.black);
		graphics.drawString(text, x + 4, y + 4);
		graphics.drawString(text, x + 3, y + 4);
		graphics.drawString(text, x + 4, y + 3);

		graphics.setColor(Graphics.Colors.white);
		graphics.drawString(text, x - 1, y);
		graphics.drawString(text, x + 1, y);
		graphics.drawString(text, x, y - 1);
		graphics.drawString(text, x, y + 1);

		graphics.setColor(mainColor);
		graphics.drawString(text, x, y);
	}

	private void drawPixelTrophy(Graphics graphics, int x, int y) {
		assert graphics != null;

		int s = 6;

		graphics.setColor(Graphics.Colors.black);
		drawTrophyBlocks(graphics, x + 4, y + 4, s);

		graphics.setColor(Graphics.Colors.yellow);
		drawTrophyBlocks(graphics, x, y, s);

		graphics.setColor(Graphics.Colors.white);
		graphics.fillRect(x + 4 * s, y + 2 * s, s, 3 * s);
	}

	private void drawTrophyBlocks(Graphics graphics, int x, int y, int s) {
		graphics.fillRect(x + 3 * s, y + 0 * s, 7 * s, 1 * s);
		graphics.fillRect(x + 2 * s, y + 1 * s, 9 * s, 1 * s);
		graphics.fillRect(x + 2 * s, y + 2 * s, 9 * s, 1 * s);
		graphics.fillRect(x + 3 * s, y + 3 * s, 7 * s, 1 * s);
		graphics.fillRect(x + 4 * s, y + 4 * s, 5 * s, 1 * s);
		graphics.fillRect(x + 5 * s, y + 5 * s, 3 * s, 1 * s);

		graphics.fillRect(x + 6 * s, y + 6 * s, 1 * s, 3 * s);
		graphics.fillRect(x + 4 * s, y + 9 * s, 5 * s, 1 * s);
		graphics.fillRect(x + 3 * s, y + 10 * s, 7 * s, 1 * s);

		graphics.fillRect(x + 0 * s, y + 1 * s, 2 * s, 1 * s);
		graphics.fillRect(x + 0 * s, y + 2 * s, 1 * s, 2 * s);
		graphics.fillRect(x + 1 * s, y + 4 * s, 2 * s, 1 * s);

		graphics.fillRect(x + 11 * s, y + 1 * s, 2 * s, 1 * s);
		graphics.fillRect(x + 12 * s, y + 2 * s, 1 * s, 2 * s);
		graphics.fillRect(x + 10 * s, y + 4 * s, 2 * s, 1 * s);
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