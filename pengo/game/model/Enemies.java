package game.model;

import engine.model.Entity;

public class Enemies extends Entity {

    private boolean frozen;
    private boolean alive;

    public Enemies(String name) {
        super(name);

        frozen = false;
        alive = true;
    }

    public boolean isFrozen() {
        return frozen;
    }

    public void freeze() {
        frozen = true;
    }

    public void unfreeze() {
        frozen = false;
    }

    public boolean isAlive() {
        return alive;
    }

    public void kill() {
        alive = false;
    }
}