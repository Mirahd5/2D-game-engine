package test_engine;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import engine.model.Entity;
import engine.model.Game;
import engine.model.Grid;
import engine.model.ISU;

/**
 * Tests de la classe Entity.
 *
 * Une Entity représente un objet du jeu :
 * - Pacman
 * - fantôme
 * - mur
 * - bonus
 * etc.
 *
 * Une Entity possède deux représentations de sa position :
 *
 * 1) Grid.Position :
 *    position discrète dans la grille, en nombre de cellules.
 *
 * 2) ISU.Coord :
 *    position réelle du centre de l'entité, en cm.
 *
 * Le but principal de ces tests est donc de vérifier :
 *
 * - que position et center restent cohérents ;
 * - que l'entité est bien ajoutée dans la bonne cellule ;
 * - que l'entité est retirée de l'ancienne cellule quand elle bouge ;
 * - que les déplacements sur un tore fonctionnent ;
 * - que les getters protègent l'encapsulation en renvoyant des copies.
 */
public class EntityTest {

    /**
     * Petit jeu utilisé uniquement pour les tests.
     *
     * Configuration :
     * - largeur  = 5 cellules
     * - hauteur  = 3 cellules
     * - axe X torique
     * - axe Y torique
     * - 1 cellule = 10 cm
     * - 1 cm = 2 pixels
     *
     * Comme les deux axes sont toriques, si une entité dépasse un bord,
     * elle revient de l'autre côté.
     */
    static class TorusGame extends Game {
        TorusGame() {
            super(5, 3, true, true, 10.0, 2);
        }
    }

    /**
     * Vérifie qu'une Entity créée avec un nom :
     * - conserve bien ce nom ;
     * - commence avec une orientation à 0 degré.
     *
     * Convention :
     * orientation 0° = Est.
     */
    @Test
    void entityStartsWithNameAndOrientationZero() {
        Entity e = new Entity("pacman");

        assertEquals("pacman", e.name());
        assertEquals(0, e.orientation());
    }

    /**
     * Vérifie setPosition(...).
     *
     * On place l'entité à une position de grille.
     *
     * Exemple :
     * position = Grid.Position(2,1)
     *
     * Comme cmPerCell = 10 :
     * le centre ISU attendu est :
     * center = ISU.Coord(20.0, 10.0)
     *
     * On vérifie aussi que l'entité est bien ajoutée
     * dans la cellule correspondant à sa position.
     */
    @Test
    void setPositionInitializesPositionCenterAndCell() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Entity e = new Entity("pacman");
        Grid.Position p = grid.new Position(2, 1);

        e.setPosition(p);

        assertTrue(e.position().equiv(p));

        assertEquals(20.0, e.center().x());
        assertEquals(10.0, e.center().y());

        assertTrue(grid.cellAt(p).contains(e));
    }

    /**
     * Vérifie setCoord(...).
     *
     * Cette fois, on place l'entité à partir d'une coordonnée réelle en cm.
     *
     * Exemple :
     * center = ISU.Coord(20.0, 10.0)
     *
     * Comme cmPerCell = 10 :
     * la position de grille attendue est :
     * position = Grid.Position(2,1)
     *
     * On vérifie aussi que la cellule (2,1) contient bien l'entité.
     */
    @Test
    void setCoordInitializesCenterPositionAndCell() {
        Game game = new TorusGame();
        ISU isu = game.isu();
        Grid grid = game.grid();

        Entity e = new Entity("ghost");
        ISU.Coord c = isu.new Coord(20.0, 10.0);

        e.setCoord(c);

        assertEquals(20.0, e.center().x());
        assertEquals(10.0, e.center().y());

        Grid.Position expected = grid.new Position(2, 1);

        assertTrue(e.position().equiv(expected));
        assertTrue(grid.cellAt(expected).contains(e));
    }

    /**
     * Vérifie un déplacement avec un Grid.Vector.
     *
     * Ici, on déplace l'entité en nombre de cellules.
     *
     * Départ :
     * position = (0,0)
     * center   = (0,0)
     *
     * Déplacement :
     * Vector(1,0)
     *
     * Résultat attendu :
     * position = (1,0)
     * center   = (10,0)
     *
     * On vérifie aussi :
     * - l'entité n'est plus dans l'ancienne cellule ;
     * - l'entité est dans la nouvelle cellule.
     */
    @Test
    void translateWithGridVectorUpdatesPositionCenterAndCells() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Entity e = new Entity("pacman");

        Grid.Position start = grid.new Position(0, 0);
        Grid.Position next = grid.new Position(1, 0);

        e.setPosition(start);

        assertTrue(grid.cellAt(start).contains(e));

        e.translate(grid.new Vector(1, 0));

        assertFalse(grid.cellAt(start).contains(e));
        assertTrue(grid.cellAt(next).contains(e));

        assertTrue(e.position().equiv(next));
        assertEquals(10.0, e.center().x());
        assertEquals(0.0, e.center().y());
    }

    /**
     * Vérifie un déplacement avec un ISU.Vector.
     *
     * Ici, on déplace l'entité en cm.
     *
     * Départ :
     * position = (0,0)
     * center   = (0,0)
     *
     * Déplacement :
     * Vector(10.0, 0.0)
     *
     * Comme 1 cellule = 10 cm :
     *
     * Résultat attendu :
     * position = (1,0)
     * center   = (10,0)
     *
     * On vérifie aussi que le changement de cellule est bien fait.
     */
    @Test
    void translateWithISUVectorUpdatesCenterPositionAndCells() {
        Game game = new TorusGame();
        Grid grid = game.grid();
        ISU isu = game.isu();

        Entity e = new Entity("pacman");

        Grid.Position start = grid.new Position(0, 0);
        Grid.Position next = grid.new Position(1, 0);

        e.setPosition(start);

        e.translate(isu.new Vector(10.0, 0.0));

        assertFalse(grid.cellAt(start).contains(e));
        assertTrue(grid.cellAt(next).contains(e));

        assertTrue(e.position().equiv(next));
        assertEquals(10.0, e.center().x());
        assertEquals(0.0, e.center().y());
    }

    /**
     * Vérifie un tour complet vers l'Est sur un tore.
     *
     * Grille :
     * largeur = 5
     *
     * Départ :
     * position = (0,1)
     *
     * Chaque moveEast(game.cmPerCell()) avance de 10 cm,
     * donc d'une cellule.
     *
     * Après 5 déplacements vers l'Est :
     * (0,1) -> (1,1) -> (2,1) -> (3,1) -> (4,1) -> (0,1)
     *
     * On doit donc revenir à la position de départ.
     */
    @Test
    void moveEastFullTorusComesBackToStart() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Entity e = new Entity("pacman");
        Grid.Position start = grid.new Position(0, 1);

        e.setPosition(start);

        for (int i = 0; i < grid.width(); i++) {
            e.moveEast(game.cmPerCell());
        }

        assertTrue(e.position().equiv(start));
        assertTrue(grid.cellAt(start).contains(e));
    }

    /**
     * Vérifie un tour complet vers l'Ouest sur un tore.
     *
     * Grille :
     * largeur = 5
     *
     * Départ :
     * position = (0,1)
     *
     * Après un déplacement vers l'Ouest :
     * (0,1) -> (4,1)
     *
     * Après 5 déplacements :
     * on revient à (0,1).
     */
    @Test
    void moveWestFullTorusComesBackToStart() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Entity e = new Entity("pacman");
        Grid.Position start = grid.new Position(0, 1);

        e.setPosition(start);

        for (int i = 0; i < grid.width(); i++) {
            e.moveWest(game.cmPerCell());
        }

        assertTrue(e.position().equiv(start));
        assertTrue(grid.cellAt(start).contains(e));
    }

    /**
     * Vérifie un tour complet vers le Nord sur un tore.
     *
     * Grille :
     * hauteur = 3
     *
     * Axe Y orienté vers le bas :
     * - Nord = y diminue
     *
     * Départ :
     * position = (2,0)
     *
     * Après un déplacement vers le Nord :
     * (2,0) -> (2,2)
     *
     * Après 3 déplacements :
     * on revient à (2,0).
     */
    @Test
    void moveNorthFullTorusComesBackToStart() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Entity e = new Entity("pacman");
        Grid.Position start = grid.new Position(2, 0);

        e.setPosition(start);

        for (int i = 0; i < grid.height(); i++) {
            e.moveNorth(1);
        }

        assertTrue(e.position().equiv(start));
        assertTrue(grid.cellAt(start).contains(e));
    }

    /**
     * Vérifie un tour complet vers le Sud sur un tore.
     *
     * Grille :
     * hauteur = 3
     *
     * Axe Y orienté vers le bas :
     * - Sud = y augmente
     *
     * Départ :
     * position = (2,0)
     *
     * Après 3 déplacements vers le Sud :
     * (2,0) -> (2,1) -> (2,2) -> (2,0)
     */
    @Test
    void moveSouthFullTorusComesBackToStart() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Entity e = new Entity("pacman");
        Grid.Position start = grid.new Position(2, 0);

        e.setPosition(start);

        for (int i = 0; i < grid.height(); i++) {
            e.moveSouth(1);
        }

        assertTrue(e.position().equiv(start));
        assertTrue(grid.cellAt(start).contains(e));
    }

    /**
     * Vérifie setSize avec une Grid.Dimension.
     *
     * Grid.Dimension est en nombre de cellules.
     *
     * Exemple :
     * Grid.Dimension(2,3)
     *
     * Comme cmPerCell = 10 :
     * taille attendue en ISU :
     * ISU.Dimension(20.0, 30.0)
     */
    @Test
    void setSizeWithGridDimensionWorks() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Entity e = new Entity("pacman");

        e.setSize(grid.new Dimension(2, 3));

        assertEquals(20.0, e.size().x());
        assertEquals(30.0, e.size().y());
    }

    /**
     * Vérifie setSize avec une ISU.Dimension.
     *
     * Ici, la dimension est déjà donnée directement en cm.
     *
     * Donc :
     * ISU.Dimension(5.0,6.0)
     *
     * doit rester :
     * size = (5.0,6.0)
     */
    @Test
    void setSizeWithISUDimensionWorks() {
        Game game = new TorusGame();
        ISU isu = game.isu();

        Entity e = new Entity("pacman");

        e.setSize(isu.new Dimension(5.0, 6.0));

        assertEquals(5.0, e.size().x());
        assertEquals(6.0, e.size().y());
    }

    /**
     * Vérifie que center() renvoie une copie.
     *
     * Pourquoi ?
     *
     * Si center() renvoyait directement this.center,
     * un code extérieur pourrait faire :
     *
     * e.center().translate(...)
     *
     * et modifier l'état interne de l'entité sans passer par translate(...).
     *
     * Cela casserait la cohérence position / center.
     *
     * Ici on récupère une copie, on la modifie,
     * puis on vérifie que le vrai centre de l'entité n'a pas changé.
     */
    @Test
    void centerGetterReturnsCopy() {
        Game game = new TorusGame();
        ISU isu = game.isu();

        Entity e = new Entity("pacman");
        e.setPosition(game.grid().new Position(0, 0));

        ISU.Coord copy = e.center();
        copy.translate(isu.new Vector(10.0, 0.0));

        assertEquals(0.0, e.center().x());
        assertEquals(0.0, e.center().y());
    }

    /**
     * Vérifie que position() renvoie une copie.
     *
     * Même logique que center().
     *
     * On récupère une position, on modifie cette copie,
     * puis on vérifie que la vraie position de l'entité n'a pas changé.
     */
    @Test
    void positionGetterReturnsCopy() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Entity e = new Entity("pacman");
        e.setPosition(grid.new Position(0, 0));

        Grid.Position copy = e.position();
        copy.translate(grid.new Vector(1, 0));

        assertTrue(e.position().equiv(grid.new Position(0, 0)));
        assertFalse(e.position().equiv(copy));
    }

    /**
     * Vérifie que les rotations normalisent bien l'angle.
     *
     * L'orientation doit toujours rester dans [0, 360[.
     *
     * Étapes :
     * - départ : 0
     * - turn(90)  -> 90
     * - turn(300) -> 390, normalisé en 30
     * - turn(-60) -> -30, normalisé en 330
     * - turn(720) -> 330, car 720 est un tour complet
     */
    @Test
    void turnNormalizesAngle() {
        Entity e = new Entity("pacman");

        e.turn(90);
        assertEquals(90, e.orientation());

        e.turn(300);
        assertEquals(30, e.orientation());

        e.turn(-60);
        assertEquals(330, e.orientation());

        e.turn(720);
        assertEquals(330, e.orientation());
    }
    
    @Test
    void stepIsInitializedWhenEntityIsPositioned() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Entity e = new Entity("pacman");

        e.setPosition(grid.new Position(0, 0));

        assertNotNull(e.step());
        assertEquals(10.0, e.step().x(), 0.000001);
        assertEquals(10.0, e.step().y(), 0.000001);
    }
    
    @Test
    void showWorksWhenEntityIsNotPlacedYet() {
        Entity e = new Entity("pacman");

        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        java.io.PrintStream ps = new java.io.PrintStream(out);

        e.show(ps);

        String result = out.toString();

        assertTrue(result.contains("ENTITY"));
        assertTrue(result.contains("pacman"));
        assertTrue(result.contains("orientation = 0"));
        assertTrue(result.contains("position = null"));
        assertTrue(result.contains("center = null"));
        assertTrue(result.contains("size = null"));
        assertTrue(result.contains("step = null"));
    }
    
    @Test
    void showWorksWhenEntityIsFullyInitialized() {
        Game game = new TorusGame();
        Grid grid = game.grid();
        ISU isu = game.isu();

        Entity e = new Entity("pacman");

        e.setPosition(grid.new Position(2, 1));
        e.setSize(isu.new Dimension(5.0, 6.0));

        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        java.io.PrintStream ps = new java.io.PrintStream(out);

        e.show(ps);

        String result = out.toString();

        assertTrue(result.contains("ENTITY"));
        assertTrue(result.contains("pacman"));
        assertTrue(result.contains("orientation"));
        assertTrue(result.contains("Position"));
        assertTrue(result.contains("ISU.Coord"));
        assertTrue(result.contains("ISU.Dimension"));
    }
    
    @Test
    void constructorRejectsNullNameWhenAssertionsEnabled() {
        assertThrows(AssertionError.class, () -> {
            new Entity(null);
        });
    }
    
    @Test
    void centerGetterFailsWhenEntityIsNotPlaced() {
        Entity e = new Entity("pacman");

        assertThrows(AssertionError.class, () -> {
            e.center();
        });
    }

    @Test
    void positionGetterFailsWhenEntityIsNotPlaced() {
        Entity e = new Entity("pacman");

        assertThrows(AssertionError.class, () -> {
            e.position();
        });
    }
    
    @Test
    void moveRejectsNegativeValuesWhenAssertionsEnabled() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Entity e = new Entity("pacman");
        e.setPosition(grid.new Position(0, 0));

        assertThrows(AssertionError.class, () -> e.moveNorth(-1));
        assertThrows(AssertionError.class, () -> e.moveSouth(-1));
        assertThrows(AssertionError.class, () -> e.moveEast(-1.0));
        assertThrows(AssertionError.class, () -> e.moveWest(-1.0));
    }
    
    @Test
    void settersRejectNullArgumentsWhenAssertionsEnabled() {
        Entity e = new Entity("pacman");

        assertThrows(AssertionError.class, () -> e.setPosition(null));
        assertThrows(AssertionError.class, () -> e.setCoord(null));
        assertThrows(AssertionError.class, () -> e.setSize((Grid.Dimension) null));
        assertThrows(AssertionError.class, () -> e.setSize((ISU.Dimension) null));
    }
    
    @Test
    void translateRejectsNullVectorsWhenAssertionsEnabled() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Entity e = new Entity("pacman");
        e.setPosition(grid.new Position(0, 0));

        assertThrows(AssertionError.class, () -> e.translate((Grid.Vector) null));
        assertThrows(AssertionError.class, () -> e.translate((ISU.Vector) null));
    }
    
    @Test
    void setPositionKeepsExistingStepWhenCalledTwice() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Entity e = new Entity("pacman");

        e.setPosition(grid.new Position(0, 0));
        ISU.Dimension firstStep = e.step();

        e.setPosition(grid.new Position(1, 0));
        ISU.Dimension secondStep = e.step();

        assertSame(firstStep, secondStep);
        assertEquals(10.0, secondStep.x(), 0.000001);
        assertEquals(10.0, secondStep.y(), 0.000001);
    }
    
    @Test
    void entityCanMoveSeveralTimesAndRemainInOnlyCurrentCell() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Entity e = new Entity("pacman");

        Grid.Position p0 = grid.new Position(0, 0);
        Grid.Position p1 = grid.new Position(1, 0);
        Grid.Position p2 = grid.new Position(2, 0);

        e.setPosition(p0);

        e.translate(grid.new Vector(1, 0));
        assertFalse(grid.cellAt(p0).contains(e));
        assertTrue(grid.cellAt(p1).contains(e));

        e.translate(grid.new Vector(1, 0));
        assertFalse(grid.cellAt(p0).contains(e));
        assertFalse(grid.cellAt(p1).contains(e));
        assertTrue(grid.cellAt(p2).contains(e));

        assertTrue(e.position().equiv(p2));
        assertEquals(20.0, e.center().x(), 0.000001);
        assertEquals(0.0, e.center().y(), 0.000001);
    }
    
    
}