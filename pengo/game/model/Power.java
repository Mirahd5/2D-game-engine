package game.model;

import engine.model.Entity;

public class Power extends Entity {

    private PowerType type;

    public Power(String name, PowerType type) {
        super(name);
        this.type = type;
    }

    public PowerType getType() {
        return type;
    }

    public void setType(PowerType type) {
        this.type = type;
    }
    
    public void apply(Pengo pengo) {

        switch(type) {

        case SHIELD:
            pengo.activateShield();
            break;

        case SPEED:
            pengo.activateSpeed();
            break;

        case STAR:
            pengo.activateStar();
            break;

        case EXTRA_LIFE:
            pengo.gainLife();
            break;

        case BOOMFISH:
            pengo.loseLife();
            break;

        default:
            break;
        }
    }
}