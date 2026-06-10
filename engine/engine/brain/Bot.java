// == BOT ==
package engine.brain;

import java.util.List;

import engine.model.BasicStunt;
import engine.model.Entity;

public abstract class Bot {

    // FIELDS

    protected BasicStunt stunt;

    // CONSTRUCTOR

    protected Bot(BasicStunt stunt) {
        assert stunt != null;
        this.stunt = stunt;
        this.stunt.setBot(this);
    }

    // GETTER

    public BasicStunt stunt() {
        return stunt;
    }

    public Entity entity() {
        return stunt.entity();
    }

    // DECISION MAKING

    public abstract void think();

    public abstract void done();

    public abstract void collision(Entity other);

    public abstract void collision(List<Entity> others);
}