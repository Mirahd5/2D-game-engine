package game.config;

import oop.graphics.BufferedImage;
import oop.graphics.Canvas;
import oop.graphics.Font;
import oop.graphics.Graphics;
import oop.graphics.VirtualKeyCodes;

public final class PengoConfigMenu implements Canvas.PaintListener, Canvas.KeyListener, Canvas.MouseListener {

	private static final int BASE_WIDTH_PIXEL = 950;
	private static final int BASE_HEIGHT_PIXEL = 600;

	private static final int LOGO_W = 360;
	private static final int LOGO_H = 150;

	private static final int MENU_W = 430;
	private static final int MENU_H = 40;

	private static final int ARROW_W = 48;

	private static final int LIST_LINE_H = 34;
	private static final int MAX_VISIBLE_MAPS = 6;

	private static final int START_W = 230;
	private static final int START_H = 52;

	private static final int PAUSE_PANEL_W = 430;
	private static final int PAUSE_PANEL_H = 310;
	private static final int PAUSE_BUTTON_W = 260;
	private static final int PAUSE_BUTTON_H = 46;
	private static final int PAUSE_BUTTON_GAP = 18;

	private Canvas canvas;
	private Canvas gameCanvas;

	private final PengoMapChoice[] maps;

	private int selectedMapIndex;
	private int firstVisibleMapIndex;

	private boolean mapListOpen;
	private boolean started;

	private PengoGameSession gameSession;
	private BufferedImage backgroundImage;
	private BufferedImage logoImage;

	public PengoConfigMenu(Canvas canvas) {
		assert canvas != null;

		this.canvas = canvas;
		this.gameCanvas = null;
		this.maps = PengoMaps.all();

		this.selectedMapIndex = 0;
		this.firstVisibleMapIndex = 0;

		this.mapListOpen = false;
		this.started = false;
		this.gameSession = null;
		this.backgroundImage = null;
		this.logoImage = null;
	}

	@Override
	public void visible(Canvas canvas) {
		assert canvas != null;

		this.canvas = canvas;

		if (this.started && this.gameSession != null) {
			this.gameCanvas = canvas;
			this.gameSession.attach(canvas);
			return;
		}

		canvas.repaint();
	}

	@Override
	public void revoked(Canvas canvas) {
		quitApplication();
	}

	@Override
	public void paint(Canvas canvas, Graphics g) {
		assert canvas != null;
		assert g != null;

		this.canvas = canvas;

		if (this.started && this.gameSession != null) {
			if (this.gameCanvas != canvas) {
				this.gameCanvas = canvas;
				this.gameSession.attach(canvas);
			}

			this.gameSession.resizeTo(canvas);
			this.gameSession.painter().paint(canvas, g);

			if (this.gameSession.paused()) {
				drawPauseOverlay(canvas, g);
			}

			return;
		}

		drawBackground(canvas, g);
		drawLogo(canvas, g);
		drawMapSelector(canvas, g);
		drawStartButton(canvas, g);
		drawHelp(canvas, g);

		if (this.mapListOpen) {
			drawMapList(canvas, g);
		}
	}

	private void drawBackground(Canvas canvas, Graphics g) {
		assert canvas != null;
		assert g != null;

		if (this.backgroundImage == null) {
			this.backgroundImage = g.load("pengo_rsrc/menu_background.png");
		}

		g.drawImage(this.backgroundImage, 0, 0, canvas.getWidth(), canvas.getHeight());
	}

	private void drawLogo(Canvas canvas, Graphics g) {
		if (this.logoImage == null) {
			this.logoImage = g.load("pengo_rsrc/pengo_logo.png");
		}

		int logoW = scaled(canvas, LOGO_W);
		int logoH = scaled(canvas, LOGO_H);

		int x = (canvas.getWidth() - logoW) / 2;
		int y = logoY(canvas);

		g.drawImage(this.logoImage, x, y, logoW, logoH);
	}

	private void drawMapSelector(Canvas canvas, Graphics g) {
		Font oldFont = g.getFont();
		Font labelFont = g.getFont("Arial", Font.BOLD, scaled(canvas, 18));
		Font valueFont = g.getFont("Arial", Font.BOLD, scaled(canvas, 16));

		int menuX = menuX(canvas);
		int menuY = menuY(canvas);
		int menuW = menuW(canvas);
		int menuH = menuH(canvas);

		g.setFont(labelFont);
		g.setColor(Graphics.Colors.white);
		drawCenteredString(canvas, g, "CHOISIS TA MAP", menuY - scaled(canvas, 18));

		g.setColor(g.getColor(185, 0, 0, 0));
		g.fillRect(menuX, menuY, menuW, menuH);

		g.setColor(Graphics.Colors.white);
		g.drawRect(menuX, menuY, menuW, menuH);

		if (this.maps.length == 0) {
			g.setColor(Graphics.Colors.yellow);
			drawCenteredString(canvas, g, "Aucune map trouvee", menuY + scaled(canvas, 27));
			g.setFont(oldFont);
			return;
		}

		PengoMapChoice selected = selectedMap();

		g.setFont(valueFont);

		String nameText = selected.label();
		String infoText = selected.width() + "x" + selected.height() + " | " + selected.geometryLabel();

		int textY = menuY + scaled(canvas, 27);

		int nameX = menuX + scaled(canvas, 20);

		int arrowX = arrowX(canvas);
		int infoRight = arrowX - scaled(canvas, 12);
		int infoX = infoRight - g.getFont().getWidth(infoText);

		g.setColor(Graphics.Colors.white);
		g.drawString(nameText, nameX, textY);

		g.setColor(Graphics.Colors.lightGray);
		g.drawString(infoText, infoX, textY);

		drawArrow(canvas, g);

		g.setFont(oldFont);
	}
	
	private void drawArrow(Canvas canvas, Graphics g) {
		int arrowX = arrowX(canvas);
		int arrowY = arrowY(canvas);
		int arrowW = arrowW(canvas);
		int menuH = menuH(canvas);

		g.setColor(g.getColor(220, 0, 0, 0));
		g.fillRect(arrowX, arrowY, arrowW, menuH);

		g.setColor(Graphics.Colors.white);
		g.drawRect(arrowX, arrowY, arrowW, menuH);

		g.setColor(Graphics.Colors.cyan);
		g.drawString(
				this.mapListOpen ? "^" : "v",
				arrowX + scaled(canvas, 19),
				arrowY + scaled(canvas, 27)
		);
	}

	private void drawMapList(Canvas canvas, Graphics g) {
		Font oldFont = g.getFont();
		Font listFont = g.getFont("Arial", Font.BOLD, scaled(canvas, 15));
		g.setFont(listFont);

		int visibleCount = Math.min(this.maps.length, MAX_VISIBLE_MAPS);
		int lineH = listLineH(canvas);
		int listHeight = visibleCount * lineH;

		int listX = listX(canvas);
		int listY = listY(canvas);
		int menuW = menuW(canvas);

		g.setColor(g.getColor(235, 0, 0, 0));
		g.fillRect(listX, listY, menuW, listHeight);

		g.setColor(Graphics.Colors.white);
		g.drawRect(listX, listY, menuW, listHeight);

		int end = Math.min(this.firstVisibleMapIndex + visibleCount, this.maps.length);

		for (int i = this.firstVisibleMapIndex; i < end; i++) {
			PengoMapChoice map = this.maps[i];

			int line = i - this.firstVisibleMapIndex;
			int y = listY + line * lineH;

			if (i == this.selectedMapIndex) {
				g.setColor(Graphics.Colors.darkGray);
				g.fillRect(listX + 1, y + 1, menuW - 2, lineH - 2);

				g.setColor(Graphics.Colors.yellow);
				g.drawString(">", listX + scaled(canvas, 8), y + scaled(canvas, 23));
			}

			g.setColor(Graphics.Colors.white);
			g.drawString(map.label(), listX + scaled(canvas, 30), y + scaled(canvas, 23));

			g.setColor(Graphics.Colors.lightGray);
			g.drawString(
					map.width() + "x" + map.height() + " | " + map.geometryLabel(),
					listX + scaled(canvas, 220),
					y + scaled(canvas, 23)
			);
		}

		if (this.firstVisibleMapIndex > 0) {
			g.setColor(Graphics.Colors.yellow);
			g.drawString("^", listX + menuW - scaled(canvas, 22), listY + scaled(canvas, 22));
		}

		if (end < this.maps.length) {
			g.setColor(Graphics.Colors.yellow);
			g.drawString("v", listX + menuW - scaled(canvas, 22), listY + listHeight - scaled(canvas, 8));
		}

		g.setFont(oldFont);
	}

	private void drawStartButton(Canvas canvas, Graphics g) {
		if (this.maps.length == 0) {
			return;
		}

		Font oldFont = g.getFont();
		Font bold = g.getFont("Arial", Font.BOLD, scaled(canvas, 17));
		g.setFont(bold);

		int startX = startX(canvas);
		int startY = startY(canvas);
		int startW = startW(canvas);
		int startH = startH(canvas);

		g.setColor(g.getColor(185, 0, 0, 0));
		g.fillRect(startX, startY, startW, startH);

		g.setColor(Graphics.Colors.white);
		g.drawRect(startX, startY, startW, startH);

		g.setColor(Graphics.Colors.cyan);
		drawCenteredString(canvas, g, "START GAME", startY + scaled(canvas, 33));

		g.setFont(oldFont);
	}

	private void drawHelp(Canvas canvas, Graphics g) {
		Font oldFont = g.getFont();
		Font bold = g.getFont("Arial", Font.BOLD, scaled(canvas, 14));
		g.setFont(bold);

		int bottom = canvas.getHeight();

		int firstLineY = Math.max(
				startY(canvas) + startH(canvas) + scaled(canvas, 45),
				bottom - scaled(canvas, 70)
		);

		int secondLineY = firstLineY + scaled(canvas, 25);

		g.setColor(Graphics.Colors.white);
		drawCenteredString(canvas, g, "SOURIS : clique sur la fleche pour ouvrir / fermer les maps", firstLineY);
		drawCenteredString(canvas, g, "CLAVIER : UP / DOWN choisir map  |  ENTER lancer  |  ESC/Q quitter", secondLineY);

		g.setFont(oldFont);
	}

	@Override
	public void pressed(Canvas canvas, int keyCode, char keyChar) {
		this.canvas = canvas;

		if (this.started && this.gameSession != null) {
			handleGameKey(canvas, keyCode, keyChar);
			return;
		}

		switch (keyCode) {
		case VirtualKeyCodes.VK_UP:
			moveMapSelection(-1);
			break;

		case VirtualKeyCodes.VK_DOWN:
			moveMapSelection(1);
			break;

		case VirtualKeyCodes.VK_ENTER:
			startGame();
			break;

		case VirtualKeyCodes.VK_ESCAPE:
		case VirtualKeyCodes.VK_Q:
			quitApplication();
			break;

		default:
			break;
		}
	}

	private void handleGameKey(Canvas canvas, int keyCode, char keyChar) {
		assert canvas != null;

		if (keyCode == VirtualKeyCodes.VK_ESCAPE) {
			this.gameSession.togglePause();
			canvas.repaint();
			return;
		}

		if (this.gameSession.paused()) {
			switch (keyCode) {
			case VirtualKeyCodes.VK_ENTER:
				this.gameSession.resume();
				canvas.repaint();
				break;

			case VirtualKeyCodes.VK_M:
				returnToMainMenu();
				break;

			case VirtualKeyCodes.VK_Q:
				quitApplication();
				break;

			default:
				break;
			}

			return;
		}

		this.gameSession.controller().pressed(canvas, keyCode, keyChar);
	}

	@Override
	public void released(Canvas canvas, int keyCode, char keyChar) {
		if (this.started && this.gameSession != null && !this.gameSession.paused()) {
			this.gameSession.controller().released(canvas, keyCode, keyChar);
		}
	}

	@Override
	public void typed(Canvas canvas, char keyChar) {
		if (this.started && this.gameSession != null && !this.gameSession.paused()) {
			this.gameSession.controller().typed(canvas, keyChar);
		}
	}

	@Override
	public void moved(Canvas canvas, int x, int y) {
		// Pas besoin de hover pour l'instant.
	}

	@Override
	public void pressed(Canvas canvas, int button, int x, int y) {
		if (this.started && this.gameSession != null) {
			this.canvas = canvas;

			if (this.gameSession.paused()) {
				handlePauseMousePressed(canvas, x, y);
			} else {
				this.gameSession.controller().pressed(canvas, button, x, y);
			}

			return;
		}

		this.canvas = canvas;

		if (inside(x, y, arrowX(canvas), arrowY(canvas), arrowW(canvas), menuH(canvas))
				|| inside(x, y, menuX(canvas), menuY(canvas), menuW(canvas), menuH(canvas))) {
			this.mapListOpen = !this.mapListOpen;
			canvas.repaint();
			return;
		}

		if (this.mapListOpen && clickOnMapList(canvas, x, y)) {
			this.mapListOpen = false;
			canvas.repaint();
			return;
		}

		if (inside(x, y, startX(canvas), startY(canvas), startW(canvas), startH(canvas))) {
			startGame();
			return;
		}

		this.mapListOpen = false;
		canvas.repaint();
	}

	@Override
	public void released(Canvas canvas, int x, int y, int button) {
		// Rien.
	}


	private void drawPauseOverlay(Canvas canvas, Graphics g) {
		Font oldFont = g.getFont();

		g.setColor(g.getColor(175, 0, 0, 0));
		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

		int panelX = pausePanelX(canvas);
		int panelY = pausePanelY(canvas);
		int panelW = pausePanelW(canvas);
		int panelH = pausePanelH(canvas);

		g.setColor(g.getColor(225, 5, 8, 22));
		g.fillRect(panelX, panelY, panelW, panelH);

		g.setColor(Graphics.Colors.white);
		g.drawRect(panelX, panelY, panelW, panelH);

		Font titleFont = g.getFont("Arial", Font.BOLD, scaled(canvas, 34));
		Font textFont = g.getFont("Arial", Font.BOLD, scaled(canvas, 15));
		g.setFont(titleFont);
		g.setColor(Graphics.Colors.yellow);
		drawCenteredString(canvas, g, "PAUSE", panelY + scaled(canvas, 58));

		drawPauseButton(canvas, g, 0, "REPRENDRE");
		drawPauseButton(canvas, g, 1, "MENU INITIAL");
		drawPauseButton(canvas, g, 2, "QUITTER");

		g.setFont(textFont);
		g.setColor(Graphics.Colors.lightGray);
		drawCenteredString(canvas, g, "ESC/ENTER reprendre  |  M menu  |  Q quitter", panelY + panelH - scaled(canvas, 28));

		g.setFont(oldFont);
	}

	private void drawPauseButton(Canvas canvas, Graphics g, int index, String text) {
		int x = pauseButtonX(canvas);
		int y = pauseButtonY(canvas, index);
		int w = pauseButtonW(canvas);
		int h = pauseButtonH(canvas);

		g.setColor(g.getColor(210, 0, 0, 0));
		g.fillRect(x, y, w, h);

		g.setColor(Graphics.Colors.cyan);
		g.drawRect(x, y, w, h);

		Font oldFont = g.getFont();
		g.setFont(g.getFont("Arial", Font.BOLD, scaled(canvas, 17)));
		g.setColor(Graphics.Colors.white);
		drawCenteredString(canvas, g, text, y + scaled(canvas, 30));
		g.setFont(oldFont);
	}

	private void handlePauseMousePressed(Canvas canvas, int x, int y) {
		if (inside(x, y, pauseButtonX(canvas), pauseButtonY(canvas, 0), pauseButtonW(canvas), pauseButtonH(canvas))) {
			this.gameSession.resume();
			canvas.repaint();
			return;
		}

		if (inside(x, y, pauseButtonX(canvas), pauseButtonY(canvas, 1), pauseButtonW(canvas), pauseButtonH(canvas))) {
			returnToMainMenu();
			return;
		}

		if (inside(x, y, pauseButtonX(canvas), pauseButtonY(canvas, 2), pauseButtonW(canvas), pauseButtonH(canvas))) {
			quitApplication();
		}
	}

	private void returnToMainMenu() {
		if (this.gameSession != null) {
			this.gameSession.stop();
		}

		this.gameSession = null;
		this.gameCanvas = null;
		this.started = false;
		this.mapListOpen = false;

		if (this.canvas != null) {
			this.canvas.repaint();
		}
	}

	private void quitApplication() {
		if (this.gameSession != null) {
			this.gameSession.stop();
			this.gameSession = null;
		}

		oop.tasks.Runtime.shutdown();
		System.exit(0);
	}

	private int pausePanelW(Canvas canvas) {
		return scaled(canvas, PAUSE_PANEL_W);
	}

	private int pausePanelH(Canvas canvas) {
		return scaled(canvas, PAUSE_PANEL_H);
	}

	private int pausePanelX(Canvas canvas) {
		return (canvas.getWidth() - pausePanelW(canvas)) / 2;
	}

	private int pausePanelY(Canvas canvas) {
		return (canvas.getHeight() - pausePanelH(canvas)) / 2;
	}

	private int pauseButtonW(Canvas canvas) {
		return scaled(canvas, PAUSE_BUTTON_W);
	}

	private int pauseButtonH(Canvas canvas) {
		return scaled(canvas, PAUSE_BUTTON_H);
	}

	private int pauseButtonX(Canvas canvas) {
		return (canvas.getWidth() - pauseButtonW(canvas)) / 2;
	}

	private int pauseButtonY(Canvas canvas, int index) {
		int start = pausePanelY(canvas) + scaled(canvas, 92);
		return start + index * (pauseButtonH(canvas) + scaled(canvas, PAUSE_BUTTON_GAP));
	}

	private boolean clickOnMapList(Canvas canvas, int x, int y) {
		int visibleCount = Math.min(this.maps.length, MAX_VISIBLE_MAPS);
		int lineH = listLineH(canvas);

		if (!inside(x, y, listX(canvas), listY(canvas), menuW(canvas), visibleCount * lineH)) {
			return false;
		}

		int line = (y - listY(canvas)) / lineH;
		int mapIndex = this.firstVisibleMapIndex + line;

		if (mapIndex < 0 || mapIndex >= this.maps.length) {
			return false;
		}

		this.selectedMapIndex = mapIndex;
		updateVisibleWindow();

		return true;
	}

	private void moveMapSelection(int delta) {
		if (this.maps.length == 0) {
			return;
		}

		this.selectedMapIndex += delta;

		if (this.selectedMapIndex < 0) {
			this.selectedMapIndex = this.maps.length - 1;
		} else if (this.selectedMapIndex >= this.maps.length) {
			this.selectedMapIndex = 0;
		}

		updateVisibleWindow();

		if (this.canvas != null) {
			this.canvas.repaint();
		}
	}

	private void updateVisibleWindow() {
		if (this.selectedMapIndex < this.firstVisibleMapIndex) {
			this.firstVisibleMapIndex = this.selectedMapIndex;
		}

		if (this.selectedMapIndex >= this.firstVisibleMapIndex + MAX_VISIBLE_MAPS) {
			this.firstVisibleMapIndex = this.selectedMapIndex - MAX_VISIBLE_MAPS + 1;
		}

		if (this.firstVisibleMapIndex < 0) {
			this.firstVisibleMapIndex = 0;
		}
	}

	private void startGame() {
		if (this.started || this.maps.length == 0) {
			return;
		}

		PengoConfig defaultConfig = new PengoConfig(selectedMap(), true);

		int windowWidth = this.canvas.getWidth();
		int windowHeight = this.canvas.getHeight();

		PengoConfig config = new PengoConfig(
				selectedMap(),
				true,
				defaultConfig.cmPerCell(),
				defaultConfig.pixelPerCm(),
				windowWidth,
				windowHeight,
				defaultConfig.viewportWidthCells(),
				defaultConfig.viewportHeightCells(),
				defaultConfig.tickDelayMs(),
				defaultConfig.paintDelayMs(),
				defaultConfig.spawnDelayMs(),
				defaultConfig.maxLivingEnemies(),
				defaultConfig.maxTotalSpawnedEnemies()
		);

		this.gameSession = PengoGameRunner.start(this.canvas, config);
		this.gameSession.attach(this.canvas);
		this.gameCanvas = this.canvas;
		this.started = true;

		this.canvas.repaint();
	}

	private PengoMapChoice selectedMap() {
		assert this.maps.length > 0;
		assert this.selectedMapIndex >= 0;
		assert this.selectedMapIndex < this.maps.length;

		return this.maps[this.selectedMapIndex];
	}

	private void drawCenteredString(Canvas canvas, Graphics g, String text, int y) {
		int x = (canvas.getWidth() - g.getFont().getWidth(text)) / 2;
		g.drawString(text, x, y);
	}

	private double uiScale(Canvas canvas) {
		double scaleX = canvas.getWidth() / (double) BASE_WIDTH_PIXEL;
		double scaleY = canvas.getHeight() / (double) BASE_HEIGHT_PIXEL;

		double scale = Math.min(scaleX, scaleY);

		if (scale < 0.75) {
			return 0.75;
		}

		if (scale > 1.8) {
			return 1.8;
		}

		return scale;
	}

	private int scaled(Canvas canvas, int value) {
		return (int) Math.round(value * uiScale(canvas));
	}

	private int menuW(Canvas canvas) {
		return scaled(canvas, MENU_W);
	}

	private int menuH(Canvas canvas) {
		return scaled(canvas, MENU_H);
	}

	private int menuX(Canvas canvas) {
		return (canvas.getWidth() - menuW(canvas)) / 2;
	}

	private int menuY(Canvas canvas) {
		return canvas.getHeight() / 2 - scaled(canvas, 60);
	}

	private int arrowW(Canvas canvas) {
		return scaled(canvas, ARROW_W);
	}

	private int arrowX(Canvas canvas) {
		return menuX(canvas) + menuW(canvas) - arrowW(canvas);
	}

	private int arrowY(Canvas canvas) {
		return menuY(canvas);
	}

	private int listX(Canvas canvas) {
		return menuX(canvas);
	}

	private int listY(Canvas canvas) {
		return menuY(canvas) + menuH(canvas);
	}

	private int listLineH(Canvas canvas) {
		return scaled(canvas, LIST_LINE_H);
	}

	private int startW(Canvas canvas) {
		return scaled(canvas, START_W);
	}

	private int startH(Canvas canvas) {
		return scaled(canvas, START_H);
	}

	private int startX(Canvas canvas) {
		return (canvas.getWidth() - startW(canvas)) / 2;
	}

	private int startY(Canvas canvas) {
		return menuY(canvas) + scaled(canvas, 100);
	}

	private int logoY(Canvas canvas) {
		return Math.max(scaled(canvas, 20), menuY(canvas) - scaled(canvas, 190));
	}

	private boolean inside(int px, int py, int x, int y, int w, int h) {
		return px >= x && px <= x + w && py >= y && py <= y + h;
	}

	public boolean repaintIfPossible() {
		if (this.canvas == null) {
			return false;
		}

		if (this.started) {
			return false;
		}

		this.canvas.repaint();
		return true;
	}
}