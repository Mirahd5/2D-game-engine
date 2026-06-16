package game.controller;

import engine.brain.PlayerBot;
import game.model.Pengo;
import oop.graphics.Canvas;
import oop.graphics.VirtualKeyCodes;


public final class PengoController implements Canvas.KeyListener {

    private final PlayerBot playerBot;
    private final Pengo pengo;

    public PengoController(PlayerBot playerBot, Pengo pengo) {
        assert playerBot != null;
        assert pengo != null;
        assert playerBot.stunt().entity() == pengo;

        this.playerBot = playerBot;
        this.pengo = pengo;
    }

    @Override
    public void pressed(Canvas canvas, int keyCode, char keyChar) {
        switch (keyCode) {
        case VirtualKeyCodes.VK_RIGHT:
        case VirtualKeyCodes.VK_D:
            moveOnly(0);
            break;

        case VirtualKeyCodes.VK_DOWN:
        case VirtualKeyCodes.VK_S:
            moveOnly(90);
            break;

        case VirtualKeyCodes.VK_LEFT:
        case VirtualKeyCodes.VK_Q:
            moveOnly(180);
            break;

        case VirtualKeyCodes.VK_UP:
        case VirtualKeyCodes.VK_Z:
            moveOnly(270);
            break;

        case VirtualKeyCodes.VK_SPACE:
            this.pengo.useIceAhead();
            break;

        default:
            break;
        }
    }

    private void moveOnly(int direction) {
        if (this.pengo.faceIceAhead(direction)) {
            return;
        }

        this.playerBot.wish(direction);
    }

    @Override
    public void released(Canvas canvas, int keyCode, char keyChar) {
        // Pengo continue dans sa direction après le relâchement de la touche.
    }

    @Override
    public void typed(Canvas canvas, char keyChar) {
        // Non utilisé : pressed(...) gère aussi la touche Espace.
    }
}