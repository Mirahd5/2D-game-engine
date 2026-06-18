package game.controller;

import java.util.HashSet;
import java.util.Set;

import engine.model.BasicStunt;
import game.model.Pengo;
import oop.graphics.Canvas;
import oop.graphics.VirtualKeyCodes;

public final class PengoController implements Canvas.KeyListener {

    private final BasicStunt pengoStunt;
    private final Pengo pengo;
    private final boolean continuousWalk;

    private final Set<Integer> pressedKeys;

    private Integer continuousDirection;

    public PengoController(BasicStunt pengoStunt, Pengo pengo) {
        this(pengoStunt, pengo, false);
    }

    public PengoController(BasicStunt pengoStunt, Pengo pengo, boolean continuousWalk) {
        assert pengoStunt != null;
        assert pengo != null;
        assert pengoStunt.entity() == pengo;

        this.pengoStunt = pengoStunt;
        this.pengo = pengo;
        this.continuousWalk = continuousWalk;
        this.pressedKeys = new HashSet<>();
        this.continuousDirection = null;
    }

    public boolean continuousWalk() {
        return this.continuousWalk;
    }
    
    public BasicStunt stunt() {
        return this.pengoStunt;
    }

    @Override
    public void pressed(Canvas canvas, int keyCode, char keyChar) {
        switch (keyCode) {
        case VirtualKeyCodes.VK_RIGHT:
        case VirtualKeyCodes.VK_D:
            directionPressed(keyCode, 0);
            break;

        case VirtualKeyCodes.VK_DOWN:
        case VirtualKeyCodes.VK_S:
            directionPressed(keyCode, 90);
            break;

        case VirtualKeyCodes.VK_LEFT:
        case VirtualKeyCodes.VK_Q:
            directionPressed(keyCode, 180);
            break;

        case VirtualKeyCodes.VK_UP:
        case VirtualKeyCodes.VK_Z:
            directionPressed(keyCode, 270);
            break;

        case VirtualKeyCodes.VK_SPACE:
            if (this.pressedKeys.add(keyCode)) {
                this.pengo.useIceAhead();
            }
            break;

        default:
            break;
        }
    }

    private void directionPressed(int keyCode, int direction) {

        if (!this.pressedKeys.add(keyCode)) {
            return;
        }

        if (this.continuousWalk) {
            this.continuousDirection = normalizeCardinal(direction);
        }

        tryStartStep(direction);
    }

    public void updateContinuousWalk() {
        if (!this.continuousWalk) {
            return;
        }

        if (this.continuousDirection == null) {
            return;
        }

        tryStartStep(this.continuousDirection);
    }

    private boolean tryStartStep(int direction) {

        if (this.pengo.isMoving()) {
            return false;
        }

        if (this.pengo.faceIceAhead(direction)) {
            return false;
        }

        return this.pengoStunt.walk(direction);
    }

    @Override
    public void released(Canvas canvas, int keyCode, char keyChar) {
        this.pressedKeys.remove(keyCode);

        if (!this.continuousWalk) {
            return;
        }

        Integer releasedDirection = directionForKey(keyCode);

        if (releasedDirection == null) {
            return;
        }

        if (releasedDirection.equals(this.continuousDirection)) {
            this.continuousDirection = findStillPressedDirection();
        }
    }

    private Integer findStillPressedDirection() {
        for (Integer key : this.pressedKeys) {
            Integer direction = directionForKey(key);

            if (direction != null) {
                return direction;
            }
        }

        return null;
    }

    private Integer directionForKey(int keyCode) {
        switch (keyCode) {
        case VirtualKeyCodes.VK_RIGHT:
        case VirtualKeyCodes.VK_D:
            return 0;

        case VirtualKeyCodes.VK_DOWN:
        case VirtualKeyCodes.VK_S:
            return 90;

        case VirtualKeyCodes.VK_LEFT:
        case VirtualKeyCodes.VK_Q:
            return 180;

        case VirtualKeyCodes.VK_UP:
        case VirtualKeyCodes.VK_Z:
            return 270;

        default:
            return null;
        }
    }

    private int normalizeCardinal(int degree) {
        int d = ((degree % 360) + 360) % 360;

        if (d < 45 || d >= 315) {
            return 0;
        }
        if (d < 135) {
            return 90;
        }
        if (d < 225) {
            return 180;
        }
        return 270;
    }

    @Override
    public void typed(Canvas canvas, char keyChar) {
        // Non utilisé : pressed(...) gère aussi la touche Espace.
    }
}