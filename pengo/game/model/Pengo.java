package game.model;

import engine.model.Entity;

public class Pengo extends Entity {

    private int lives;

    private boolean shield;
    private boolean speedBoost;
    private boolean star;

    public Pengo() {
        super("Pengo");

        lives = 3;

        shield = false;
        speedBoost = false;
        star = false;
    }

    public void gainLife() {
        lives++;
    }

    public void loseLife() {
        if(lives > 0)
            lives--;
    }

    public int getLives() {
        return lives;
    }

    public void activateShield() {
        shield = true;
    }

    public boolean hasShield() {
        return shield;
    }

    public void activateSpeed() {
        speedBoost = true;
    }

    public boolean hasSpeedBoost() {
        return speedBoost;
    }

    public void activateStar() {
        star = true;
    }

    public boolean hasStar() {
        return star;
    }
}