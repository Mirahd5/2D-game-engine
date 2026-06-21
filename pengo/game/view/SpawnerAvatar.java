package game.view;

import engine.view.ImageAvatar;
import engine.view.View;
import game.model.Spawner;

/** Avatar du point d'apparition présent sur la carte. */
public final class SpawnerAvatar extends ImageAvatar {

    public SpawnerAvatar(View view, Spawner spawner) {
        super(view, spawner, new String[] { "pengo_rsrc/spawner_bleu.png" });
    }

    @Override
    protected boolean rotateWithEntity() {
        return false;
    }
}