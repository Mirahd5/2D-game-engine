package engine.view;

import engine.model.Entity;
import engine.model.Picture;
import oop.graphics.Graphics;


public abstract class Avatar {

    protected final Entity entity;

    public Avatar(Entity entity) {
        this.entity = entity;
    }

    public Entity entity() {
        return this.entity;
    }

    public abstract void paint(Graphics g, Picture picture);
}