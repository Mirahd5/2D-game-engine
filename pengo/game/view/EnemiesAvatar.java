package game.view;

import engine.view.Avatar;
import engine.view.View;
import game.model.Enemies;
import oop.graphics.BufferedImage;
import oop.graphics.Graphics;

/** Avatar directionnel d'un ennemi. */
public final class EnemiesAvatar extends Avatar {

    private final Enemies enemy;

    private BufferedImage normalDown;
    private BufferedImage normalUp;
    private BufferedImage normalLeft;
    private BufferedImage normalRight;

    private BufferedImage frozenDown;
    private BufferedImage frozenUp;
    private BufferedImage frozenLeft;
    private BufferedImage frozenRight;

    public EnemiesAvatar(View view, Enemies enemy) {
        super(view, enemy);
        this.enemy = enemy;
    }

    @Override
    public void paint(Graphics graphics) {
        assert graphics != null;

        loadImages(graphics);

        BufferedImage image = imageForStateAndOrientation();

        int centerX = this.view.toPixelX(this.enemy.center().x());
        int centerY = this.view.toPixelY(this.enemy.center().y());
        int width = this.view.toPixelWidth(this.enemy.size().x());
        int height = this.view.toPixelHeight(this.enemy.size().y());

        graphics.drawImage(image, centerX - width / 2, centerY - height / 2, width, height);
    }

    private void loadImages(Graphics graphics) {
        if (this.normalDown != null) {
            return;
        }

        this.normalDown = graphics.load("pengo_rsrc/normal_down.png");
        this.normalUp = graphics.load("pengo_rsrc/normal_up.png");
        this.normalLeft = graphics.load("pengo_rsrc/normal_left.png");
        this.normalRight = graphics.load("pengo_rsrc/normal_right.png");

        this.frozenDown = graphics.load("pengo_rsrc/peur_down.png");
        this.frozenUp = graphics.load("pengo_rsrc/peur_up.png");
        this.frozenLeft = graphics.load("pengo_rsrc/peur_left.png");
        this.frozenRight = graphics.load("pengo_rsrc/peur_right.png");
    }

    private BufferedImage imageForStateAndOrientation() {
        int orientation = this.enemy.orientation();

        if (this.enemy.isFrozen()) {
            switch (orientation) {
            case 0:
                return this.frozenRight;
            case 90:
                return this.frozenDown;
            case 180:
                return this.frozenLeft;
            case 270:
                return this.frozenUp;
            default:
                return this.frozenDown;
            }
        }

        switch (orientation) {
        case 0:
            return this.normalRight;
        case 90:
            return this.normalDown;
        case 180:
            return this.normalLeft;
        case 270:
            return this.normalUp;
        default:
            return this.normalDown;
        }
    }
}