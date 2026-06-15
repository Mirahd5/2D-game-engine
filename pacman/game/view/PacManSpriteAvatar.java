package game.view;

import engine.model.Entity;
import engine.view.AnimatedImageAvatar;
import engine.view.View;

public class PacManSpriteAvatar extends AnimatedImageAvatar {

    private static final String[] PATHS = {
        "assets/pacman/pacman_big_closed.png",
        "assets/pacman/pacman_big_mid_right.png",
        "assets/pacman/pacman_big_open_right.png",
        "assets/pacman/pacman_big_mid_right.png"
    };

    public PacManSpriteAvatar(View view, Entity entity) {
        super(view, entity, PATHS, 120L);
    }

    @Override
    protected double visualScale() {
        return 1.8;
    }
}