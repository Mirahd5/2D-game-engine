package game.view;

import engine.view.ImageAvatar;
import engine.view.View;
import game.model.Glacon;

/** Avatar d'un bloc de glace. */
public final class GlaconAvatar extends ImageAvatar {

    public GlaconAvatar(View view, Glacon glacon) {
        super(view, glacon, new String[] { "pengo_rsrc/glacon.png" });
    }

    @Override
    protected double visualScale() {
        return 1.0 / Glacon.collisionSizeFactor();
    }

    @Override
    protected boolean rotateWithEntity() {
        return false;
    }
}