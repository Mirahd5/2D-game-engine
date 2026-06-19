package game.config;

import oop.graphics.BufferedImage;
import oop.graphics.Canvas;
import oop.graphics.Font;
import oop.graphics.Graphics;
import oop.graphics.VirtualKeyCodes;

/**
 * Menu principal de Pengo.
 *
 * Important : la plateforme du professeur n'autorise qu'un seul PaintListener
 * sur le Canvas. Cette classe reste donc le PaintListener pendant le menu ET
 * pendant la partie. Une fois la partie lancée, elle délègue l'affichage au
 * Painter du jeu.
 */
public final class PengoConfigMenu implements Canvas.PaintListener, Canvas.KeyListener, Canvas.MouseListener {

	private static final int LOGO_W = 360;
	private static final int LOGO_H = 150;
	private static final int LOGO_Y = 45;

	private static final int MENU_W = 430;
	private static final int MENU_H = 40;
	private static final int MENU_Y = 245;

	private static final int ARROW_W = 48;

	private static final int LIST_LINE_H = 34;
	private static final int MAX_VISIBLE_MAPS = 6;

	private static final int START_W = 230;
	private static final int START_H = 52;
	private static final int START_Y = 345;

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
		/*
		 * Ne pas arrêter la partie ici. Sur la plateforme donnée, un redimensionnement
		 * peut provoquer une révocation/recréation de surface. Si on stoppe la session
		 * ici, la fenêtre devient grise après resize.
		 */
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

		int x = (canvas.getWidth() - LOGO_W) / 2;
		g.drawImage(this.logoImage, x, LOGO_Y, LOGO_W, LOGO_H);
	}

	private void drawMapSelector(Canvas canvas, Graphics g) {
		Font oldFont = g.getFont();
		Font labelFont = g.getFont("Arial", Font.BOLD, 18);
		Font valueFont = g.getFont("Arial", Font.BOLD, 16);

		int menuX = menuX(canvas);
		int menuY = menuY();

		g.setFont(labelFont);
		g.setColor(Graphics.Colors.white);
		drawCenteredString(canvas, g, "CHOISIS TA MAP", menuY - 18);

		g.setColor(g.getColor(185, 0, 0, 0));
		g.fillRect(menuX, menuY, MENU_W, MENU_H);

		g.setColor(Graphics.Colors.white);
		g.drawRect(menuX, menuY, MENU_W, MENU_H);

		if (this.maps.length == 0) {
			g.setColor(Graphics.Colors.yellow);
			drawCenteredString(canvas, g, "Aucune map trouvee", menuY + 27);
			g.setFont(oldFont);
			return;
		}

		PengoMapChoice selected = selectedMap();

		g.setFont(valueFont);
		g.setColor(Graphics.Colors.white);
		g.drawString(selected.label(), menuX + 20, menuY + 27);

		g.setColor(Graphics.Colors.lightGray);
		g.drawString(selected.width() + "x" + selected.height() + " | " + selected.geometryLabel(), menuX + 205,
				menuY + 27);

		drawArrow(canvas, g);

		g.setFont(oldFont);
	}

	private void drawArrow(Canvas canvas, Graphics g) {
		int arrowX = arrowX(canvas);
		int arrowY = arrowY();

		g.setColor(g.getColor(220, 0, 0, 0));
		g.fillRect(arrowX, arrowY, ARROW_W, MENU_H);

		g.setColor(Graphics.Colors.white);
		g.drawRect(arrowX, arrowY, ARROW_W, MENU_H);

		g.setColor(Graphics.Colors.cyan);
		g.drawString(this.mapListOpen ? "^" : "v", arrowX + 19, arrowY + 27);
	}

	private void drawMapList(Canvas canvas, Graphics g) {
		Font oldFont = g.getFont();
		Font listFont = g.getFont("Arial", Font.BOLD, 15);
		g.setFont(listFont);

		int visibleCount = Math.min(this.maps.length, MAX_VISIBLE_MAPS);
		int listHeight = visibleCount * LIST_LINE_H;
		int listX = listX(canvas);
		int listY = listY();

		g.setColor(g.getColor(235, 0, 0, 0));
		g.fillRect(listX, listY, MENU_W, listHeight);

		g.setColor(Graphics.Colors.white);
		g.drawRect(listX, listY, MENU_W, listHeight);

		int end = Math.min(this.firstVisibleMapIndex + visibleCount, this.maps.length);

		for (int i = this.firstVisibleMapIndex; i < end; i++) {
			PengoMapChoice map = this.maps[i];

			int line = i - this.firstVisibleMapIndex;
			int y = listY + line * LIST_LINE_H;

			if (i == this.selectedMapIndex) {
				g.setColor(Graphics.Colors.darkGray);
				g.fillRect(listX + 1, y + 1, MENU_W - 2, LIST_LINE_H - 2);

				g.setColor(Graphics.Colors.yellow);
				g.drawString(">", listX + 8, y + 23);
			}

			g.setColor(Graphics.Colors.white);
			g.drawString(map.label(), listX + 30, y + 23);

			g.setColor(Graphics.Colors.lightGray);
			g.drawString(map.width() + "x" + map.height() + " | " + map.geometryLabel(), listX + 220, y + 23);
		}

		if (this.firstVisibleMapIndex > 0) {
			g.setColor(Graphics.Colors.yellow);
			g.drawString("^", listX + MENU_W - 22, listY + 22);
		}

		if (end < this.maps.length) {
			g.setColor(Graphics.Colors.yellow);
			g.drawString("v", listX + MENU_W - 22, listY + listHeight - 8);
		}

		g.setFont(oldFont);
	}

	private void drawStartButton(Canvas canvas, Graphics g) {
		if (this.maps.length == 0) {
			return;
		}

		Font oldFont = g.getFont();
		Font bold = g.getFont("Arial", Font.BOLD, 17);
		g.setFont(bold);

		int startX = startX(canvas);

		g.setColor(g.getColor(185, 0, 0, 0));
		g.fillRect(startX, START_Y, START_W, START_H);

		g.setColor(Graphics.Colors.white);
		g.drawRect(startX, START_Y, START_W, START_H);

		g.setColor(Graphics.Colors.cyan);
		drawCenteredString(canvas, g, "START GAME", START_Y + 33);

		g.setFont(oldFont);
	}

	private void drawHelp(Canvas canvas, Graphics g) {
		Font oldFont = g.getFont();
		Font bold = g.getFont("Arial", Font.BOLD, 14);
		g.setFont(bold);

		int bottom = canvas.getHeight();
		int firstLineY = Math.max(START_Y + START_H + 45, bottom - 70);
		int secondLineY = firstLineY + 25;

		g.setColor(Graphics.Colors.white);
		drawCenteredString(canvas, g, "SOURIS : clique sur la fleche pour ouvrir / fermer les maps", firstLineY);
		drawCenteredString(canvas, g, "CLAVIER : UP / DOWN choisir map  |  ENTER lancer", secondLineY);

		g.setFont(oldFont);
	}

	@Override
	public void pressed(Canvas canvas, int keyCode, char keyChar) {
		if (this.started && this.gameSession != null) {
			this.gameSession.controller().pressed(canvas, keyCode, keyChar);
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

		default:
			break;
		}
	}

	@Override
	public void released(Canvas canvas, int keyCode, char keyChar) {
		if (this.started && this.gameSession != null) {
			this.gameSession.controller().released(canvas, keyCode, keyChar);
		}
	}

	@Override
	public void typed(Canvas canvas, char keyChar) {
		if (this.started && this.gameSession != null) {
			this.gameSession.controller().typed(canvas, keyChar);
		}
	}

	@Override
	public void moved(Canvas canvas, int x, int y) {
		// Pas besoin de hover pour l'instant.
	}

	@Override
	public void pressed(Canvas canvas, int button, int x, int y) {
		if (this.started) {
			return;
		}

		this.canvas = canvas;

		if (inside(x, y, arrowX(canvas), arrowY(), ARROW_W, MENU_H)
				|| inside(x, y, menuX(canvas), menuY(), MENU_W, MENU_H)) {
			this.mapListOpen = !this.mapListOpen;
			canvas.repaint();
			return;
		}

		if (this.mapListOpen && clickOnMapList(canvas, x, y)) {
			this.mapListOpen = false;
			canvas.repaint();
			return;
		}

		if (inside(x, y, startX(canvas), START_Y, START_W, START_H)) {
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

	private boolean clickOnMapList(Canvas canvas, int x, int y) {
		int visibleCount = Math.min(this.maps.length, MAX_VISIBLE_MAPS);

		if (!inside(x, y, listX(canvas), listY(), MENU_W, visibleCount * LIST_LINE_H)) {
			return false;
		}

		int line = (y - listY()) / LIST_LINE_H;
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
		this.canvas.repaint();
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

		PengoConfig config = new PengoConfig(selectedMap(), true, defaultConfig.cmPerCell(), defaultConfig.pixelPerCm(),
				windowWidth, windowHeight, defaultConfig.viewportWidthCells(), defaultConfig.viewportHeightCells(),
				defaultConfig.tickDelayMs(), defaultConfig.paintDelayMs(), defaultConfig.spawnDelayMs(),
				defaultConfig.maxLivingEnemies(), defaultConfig.maxTotalSpawnedEnemies());

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

	private int menuX(Canvas canvas) {
		return (canvas.getWidth() - MENU_W) / 2;
	}

	private int menuY() {
		return MENU_Y;
	}

	private int arrowX(Canvas canvas) {
		return menuX(canvas) + MENU_W - ARROW_W;
	}

	private int arrowY() {
		return MENU_Y;
	}

	private int listX(Canvas canvas) {
		return menuX(canvas);
	}

	private int listY() {
		return MENU_Y + MENU_H;
	}

	private int startX(Canvas canvas) {
		return (canvas.getWidth() - START_W) / 2;
	}

	private boolean inside(int px, int py, int x, int y, int w, int h) {
		return px >= x && px <= x + w && py >= y && py <= y + h;
	}
}