package engine.view;

import java.util.ArrayList;
import java.util.List;

import engine.model.Picture;
import oop.graphics.Canvas;
import oop.graphics.Color;
import oop.graphics.Font;
import oop.graphics.Graphics;

public  class View {

    private static final int HUD_WIDTH = 220;

    private final Canvas canvas;
    private final Picture picture;
    private final List<Avatar> avatars;

    private int score;
    private int lives;
    private String message;
    private long powerRemaining_ms;
    private long invulnerableRemaining_ms;

    public View(Canvas canvas, Picture picture) {
        assert canvas != null : "canvas must not be null";
        assert picture != null : "picture must not be null";

        this.canvas = canvas;
        this.picture = picture;
        this.avatars = new ArrayList<>();
        this.score = 0;
        this.lives = 3;
        this.message = "";
        this.powerRemaining_ms = 0;
        this.invulnerableRemaining_ms = 0;
    }

    public void add(Avatar avatar) {
        assert avatar != null : "avatar must not be null";
        this.avatars.add(avatar);
    }

    public void setHud(int score, int lives, String message) {
        setHud(score, lives, message, 0, 0);
    }

    public void setHud(
            int score,
            int lives,
            String message,
            long powerRemaining_ms,
            long invulnerableRemaining_ms) {

        this.score = score;
        this.lives = lives;
        this.message = message == null ? "" : message;
        this.powerRemaining_ms = Math.max(0, powerRemaining_ms);
        this.invulnerableRemaining_ms = Math.max(0, invulnerableRemaining_ms);
    }

    public void paint(Graphics g) {
        clear(g);

        Object oldTransform = g.getTransform();

        double scale = scale();
        int xOffset = xOffset(scale);
        int yOffset = yOffset(scale);

        g.translate(xOffset, yOffset);
        g.scale(scale, scale);

        drawGrid(g);

        for (Avatar avatar : avatars) {
            avatar.paint(g, picture);
        }

        drawHud(g);

        g.setTransform(oldTransform);
    }

    private void clear(Graphics g) {
        Color black = g.getColor(0, 0, 0, 0);
        g.setColor(black);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private double scale() {
        double sx = canvas.getWidth() / (double) virtualWidth();
        double sy = canvas.getHeight() / (double) virtualHeight();
        return Math.min(sx, sy);
    }

    private int xOffset(double scale) {
        int usedWidth = (int) Math.round(virtualWidth() * scale);
        return Math.max(0, (canvas.getWidth() - usedWidth) / 2);
    }

    private int yOffset(double scale) {
        int usedHeight = (int) Math.round(virtualHeight() * scale);
        return Math.max(0, (canvas.getHeight() - usedHeight) / 2);
    }

    private int virtualWidth() {
        return picture.width() + HUD_WIDTH;
    }

    private int virtualHeight() {
        return picture.height();
    }

    private void drawGrid(Graphics g) {
        Color gray = g.getColor(40, 40, 40, 0);
        g.setColor(gray);

        double cell = picture.game().cmPerCell();

        for (double x = 0; x <= picture.game().width_cm(); x += cell) {
            int px = picture.toPixelX(x);
            g.drawLine(px, 0, px, picture.height());
        }

        for (double y = 0; y <= picture.game().height_cm(); y += cell) {
            int py = picture.toPixelY(y);
            g.drawLine(0, py, picture.width(), py);
        }
    }

    private void drawHud(Graphics g) {
        int panelX = picture.width() + 15;
        int y = 40;

        Color yellow = g.getColor(255, 255, 0, 0);
        g.setColor(yellow);

        Font font = g.getFont("Arial", Font.BOLD, 18);
        g.setFont(font);

        g.drawString("SCORE", panelX, y);
        g.drawString(String.valueOf(score), panelX, y + 25);

        y += 70;
        g.drawString("VIES", panelX, y);
        g.drawString(String.valueOf(lives), panelX, y + 25);

        y += 70;

        if (powerRemaining_ms > 0) {
            g.drawString("POWER", panelX, y);
            g.drawString(formatSeconds(powerRemaining_ms), panelX, y + 25);
            y += 70;
        }

        if (invulnerableRemaining_ms > 0) {
            g.drawString("INVINCIBLE", panelX, y);
            g.drawString(formatSeconds(invulnerableRemaining_ms), panelX, y + 25);
            y += 70;
        }

        if (!message.isEmpty()) {
            g.drawString(message, panelX, y);
        }
    }

    private String formatSeconds(long remaining_ms) {
        double seconds = remaining_ms / 1000.0;
        return String.format("%.1f s", seconds);
    }

    public void removeAvatarOf(engine.model.Entity entity) {
        avatars.removeIf(a -> a.entity() == entity);
    }
}