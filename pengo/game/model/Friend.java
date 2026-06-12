package game.model;

import engine.model.Entity;

public class Friend extends Entity {

    private boolean controlled;

    public Friend() {
        super("Friend");
        this.controlled = false;
    }

    public boolean isControlled() {
        return controlled;
    }

    public void setControlled(boolean controlled) {
        this.controlled = controlled;
    }
}