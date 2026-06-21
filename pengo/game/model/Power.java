package game.model;

import engine.model.Bounding;
import engine.model.Circle;
import engine.model.Entity;
import engine.model.ISU;
import engine.model.Model;

/**
 * Bonus ramassable par Pengo.
 */
public final class Power extends Entity {

    private PowerType type;

    public Power(Model model, String name, PowerType type) {
        super(model, name);

        assert type != null;
        this.type = type;

        double diameter = model.isu().cmPerCell() * 0.55;
        setSize(model.isu().new Dimension(diameter, diameter));

        setZOrder(2);
        setSOrder(0);

        updateBounding();
    }

    @Override
    protected Bounding createBounding(ISU.Coord center, ISU.Dimension size, int orientationDegree) {
        Bounding result = new Bounding();
        result.add(new Circle(center, Math.min(size.x(), size.y()) / 2.0));
        return result;
    }

    public PowerType getType() {
        return this.type;
    }

    public void setType(PowerType type) {
        assert type != null;
        this.type = type;
    }

    public void apply(Pengo pengo) {
        assert pengo != null;
        assert pengo.model() == model();

        switch (this.type) {
        case SHIELD:
            pengo.activateShield();
            break;

        case SPEED:
            pengo.activateSpeed();
            break;

        case FREEZE:
            pengo.activateFreezePower();
            break;

        case STAR:
            pengo.activateStar();
            break;

        case EXTRA_LIFE:
            pengo.gainLife();
            break;

        case BOOMFISH:
        	pengo.explode();
        	break;

        case FRIEND_ACTIVATOR:
            pengo.activateFriendActivator();
            break;

        default:
            throw new AssertionError("Unhandled power type: " + this.type);
        }
    }
}