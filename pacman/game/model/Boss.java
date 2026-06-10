// == BOSS ==
package game.model;

import engine.intersection.Bounding;
import engine.model.Entity;
import engine.model.Game;
import engine.model.ISU;
import engine.intersection.Rect;


public class Boss extends Entity {

    // CONSTRUCTOR

    public Boss(Game game) {
        super("Boss");

        double c = game.cmPerCell();

        // Taille englobante du Boss : 3 cellules x 4 cellules
        setSize(game.isu().new Dimension(3.0 * c, 4.0 * c));
    }

    // === Task COLLISION ===

    @Override
    public void setBounding() {
        super.setBounding();

        assert center() != null : "center must be set before setBounding";

        ISU isu = center().isu();
        double c = isu.cmPerCell();

        // --- contrainte tore ---
        // Les demi-dimensions des Rect doivent être < demi-périmètre
        assert (3.0 * c) / 2.0 < isu.xAxis().halfPerimeter()
            : "Boss horizontal bar halfWidth must be smaller than half perimeter";
        assert (4.0 * c) / 2.0 < isu.yAxis().halfPerimeter()
            : "Boss vertical bar halfHeight must be smaller than half perimeter";

        // --- Barre verticale : 1c x 4c, décalage (0, +0.5c) depuis le centre ---
        ISU.Coord centerVertical = center().mkTranslated(isu.new Vector(0.0, 0.5 * c));
        bounding().add(new Rect(
            centerVertical,
            isu.new Dimension(1.0 * c, 4.0 * c),
            0
        ));

        // --- Barre horizontale : 3c x 1c, décalage (+1c, 0) depuis le centre ---
        ISU.Coord centerHorizontal = center().mkTranslated(isu.new Vector(1.0 * c, 0.0));
        bounding().add(new Rect(
            centerHorizontal,
            isu.new Dimension(3.0 * c, 1.0 * c),
            0
        ));
    }
}