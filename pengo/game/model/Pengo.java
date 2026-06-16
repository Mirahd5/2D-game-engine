package game.model;

import engine.model.Bounding;
import engine.model.Circle;
import engine.model.Entity;
import engine.model.Grid;
import engine.model.ISU;
import engine.model.Model;

public final class Pengo extends Entity {

    private int lives;

    private boolean shield;
    private boolean speedBoost;
    private boolean freezePower;
    private boolean star;
    private boolean friendActivator;

    public Pengo(Model model) {
        this(model, "Pengo");
    }

    public Pengo(Model model, String name) {
        super(model, name);

        this.lives = 3;
        this.shield = false;
        this.speedBoost = false;
        this.freezePower = false;
        this.star = false;
        this.friendActivator = false;

        double diameter = model.isu().cmPerCell() * 0.80;
        setSize(model.isu().new Dimension(diameter, diameter));
        setStep(model.isu().new Dimension(model.isu().cmPerCell(), model.isu().cmPerCell()));

        setZOrder(3);
        setSOrder(0);

        updateBounding();
    }

    @Override
    protected Bounding createBounding(ISU.Coord center, ISU.Dimension size, int orientationDegree) {
        Bounding result = new Bounding();
        result.add(new Circle(center, Math.min(size.x(), size.y()) / 2.0));
        return result;
    }

    public void gainLife() {
        this.lives++;
    }

    public void loseLife() {
        if (this.lives > 0) {
            this.lives--;
        }
    }

    public int getLives() {
        return this.lives;
    }

    public boolean isGameOver() {
        return this.lives == 0;
    }

    public void activateShield() {
        this.shield = true;
    }

    public void deactivateShield() {
        this.shield = false;
    }

    public boolean hasShield() {
        return this.shield;
    }

    public void activateSpeed() {
        this.speedBoost = true;
    }

    public void deactivateSpeed() {
        this.speedBoost = false;
    }

    public boolean hasSpeedBoost() {
        return this.speedBoost;
    }

    public void activateFreezePower() {
        this.freezePower = true;
    }

    public void deactivateFreezePower() {
        this.freezePower = false;
    }

    public boolean hasFreezePower() {
        return this.freezePower;
    }

    public void activateStar() {
        this.star = true;
    }

    public void deactivateStar() {
        this.star = false;
    }

    public boolean hasStar() {
        return this.star;
    }

    public void activateFriendActivator() {
        this.friendActivator = true;
    }

    public void deactivateFriendActivator() {
        this.friendActivator = false;
    }

    public boolean hasFriendActivator() {
        return this.friendActivator;
    }


    /**
     * Oriente Pengo vers un glaçon ou un spawner adjacent sans le pousser.
     *
     * Les touches de déplacement ne doivent plus déclencher la glissade du
     * bloc. Elles servent seulement à se déplacer ou, si un bloc poussable est juste
     * devant, à le regarder pour préparer l'action avec Espace.
     */
    public boolean faceIceAhead(int directionDegree) {
        if (!alive() || !installed() || isMoving()) {
            return false;
        }

        int direction = normalizeCardinal(directionDegree);
        Entity block = movableBlockAhead(direction);

        if (block == null) {
            return false;
        }

        setOrientation(direction);
        return true;
    }

    /**
     * Action de la touche Espace sur le bloc poussable devant Pengo.
     *
     * - devant un Glacon libre : il glisse ;
     * - devant un Glacon bloqué : il se casse ;
     * - devant un Spawner libre : il glisse comme un glaçon ;
     * - devant un Spawner bloqué : il reste en place, car un spawner ne se casse pas.
     */
    public boolean useIceAhead() {
        if (!alive() || !installed() || isMoving()) {
            return false;
        }

        Entity block = movableBlockAhead(orientation());

        if (block instanceof Glacon) {
            return ((Glacon) block).push(orientation());
        }

        if (block instanceof Spawner) {
            return ((Spawner) block).push(orientation());
        }

        return false;
    }

    private Entity movableBlockAhead(int direction) {
        Grid.Position target = neighbor(position(), direction);

        if (target == null) {
            return null;
        }

        for (Entity entity : model().grid().cellAt(target).entities()) {
            if ((entity instanceof Glacon || entity instanceof Spawner) && entity.alive()) {
                return entity;
            }
        }

        return null;
    }

    private Grid.Position neighbor(Grid.Position from, int direction) {
        int dx = 0;
        int dy = 0;

        switch (normalizeCardinal(direction)) {
        case 0:
            dx = 1;
            break;
        case 90:
            dy = 1;
            break;
        case 180:
            dx = -1;
            break;
        case 270:
            dy = -1;
            break;
        default:
            throw new AssertionError();
        }

        int x = from.x() + dx;
        int y = from.y() + dy;

        if (!model().game().isTorusOnXaxis() && (x < 0 || x >= model().grid().width())) {
            return null;
        }

        if (!model().game().isTorusOnYaxis() && (y < 0 || y >= model().grid().height())) {
            return null;
        }

        return model().grid().new Position(x, y);
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
    public void collision(Entity other) {
        assert other != null;

        if (other instanceof Power) {
            Power power = (Power) other;
            power.apply(this);
            power.death();
            return;
        }

        super.collision(other);
    }

    @Override
    public void collision(java.util.List<Entity> entities) {
        assert entities != null;

        java.util.List<Entity> physicalContacts = new java.util.ArrayList<Entity>();

        for (Entity entity : entities) {
            if (entity instanceof Power) {
                Power power = (Power) entity;
                power.apply(this);
                power.death();
            } else {
                physicalContacts.add(entity);
            }
        }

        if (!physicalContacts.isEmpty()) {
            super.collision(physicalContacts);
        }
    }
}