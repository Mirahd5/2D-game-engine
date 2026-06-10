package test_engine;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import engine.model.Entity;
import engine.model.Game;
import engine.model.Grid;
import engine.model.ISU;

/**
 * Tests de la classe Grid.
 *
 * Grid représente le monde discret du jeu, découpé en cellules.
 *
 * Dans ces tests, on vérifie :
 * - que la grille a les bonnes dimensions ;
 * - que les axes sont bien créés ;
 * - que les positions se normalisent correctement sur un tore ;
 * - que cellAt(...) renvoie la bonne cellule ;
 * - que les vecteurs déplacent correctement les positions ;
 * - que les distances utilisent le plus court chemin sur un tore ;
 * - que les dimensions sont valides ;
 * - que les cellules contiennent bien les entités ;
 * - que les conversions Grid -> ISU sont correctes.
 */
public class GridTest {

    /*
     * Jeu concret uniquement pour les tests.
     *
     * Configuration :
     * - largeur = 5 cellules
     * - hauteur = 3 cellules
     * - axe X torique
     * - axe Y torique
     * - 1 cellule = 10 cm
     * - 1 cm = 2 pixels
     *
     * Comme les deux axes sont toriques :
     * - dépasser à droite revient à gauche ;
     * - dépasser en bas revient en haut ;
     * - les coordonnées négatives sont aussi ramenées dans la grille.
     */
    static class TorusGame extends Game {
        TorusGame() {
            super(5, 3, true, true, 10.0, 2);
        }
    }

    /*
     * Jeu concret non torique.
     *
     * Configuration identique au TorusGame, sauf que :
     * - axe X non torique
     * - axe Y non torique
     *
     * Donc les positions hors bornes ne sont pas automatiquement corrigées.
     */
    static class NonTorusGame extends Game {
        NonTorusGame() {
            super(5, 3, false, false, 10.0, 2);
        }
    }

    // =========================================================
    // BASIC GRID
    // =========================================================

    /**
     * Vérifie que la grille reçoit bien les dimensions du Game.
     *
     * TorusGame a :
     * - largeur = 5
     * - hauteur = 3
     */
    @Test
    void gridHasCorrectDimensions() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        assertEquals(5, grid.width());
        assertEquals(3, grid.height());
    }

    /**
     * Vérifie que la grille possède :
     * - un axe X ;
     * - un axe Y ;
     * - un repère ISU associé.
     *
     * Comme TorusGame est torique sur X et Y,
     * les deux axes doivent être toriques.
     */
    @Test
    void gridHasAxesAndISU() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        assertNotNull(grid.xAxis());
        assertNotNull(grid.yAxis());
        assertNotNull(grid.isu());

        assertTrue(grid.xAxis().onTorus());
        assertTrue(grid.yAxis().onTorus());
    }

    // =========================================================
    // CELL AT
    // =========================================================

    /**
     * Vérifie que cellAt(...) renvoie la bonne cellule
     * quand la position est déjà dans les bornes.
     */
    @Test
    void cellAtReturnsCorrectCellInsideBounds() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Grid.Position p = grid.new Position(2, 1);
        Grid.Cell c = grid.cellAt(p);

        assertNotNull(c);
        assertTrue(c.position().equiv(p));
    }

    /**
     * Vérifie que deux positions équivalentes donnent des cellules
     * avec des positions équivalentes.
     */
    @Test
    void cellAtSamePositionReturnsEquivalentCellPosition() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Grid.Position p1 = grid.new Position(3, 2);
        Grid.Position p2 = grid.new Position(3, 2);

        assertTrue(grid.cellAt(p1).position().equiv(grid.cellAt(p2).position()));
    }

    /**
     * Vérifie que cellAt(...) gère les positions positives hors bornes
     * sur un tore.
     *
     * Grille :
     * - largeur = 5
     * - hauteur = 3
     *
     * Position demandée :
     * - x = 6 => 6 mod 5 = 1
     * - y = 4 => 4 mod 3 = 1
     *
     * Donc la cellule attendue est (1,1).
     */
    @Test
    void cellAtWrapsPositivePositionOnTorus() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Grid.Position p = grid.new Position(6, 4);
        Grid.Cell c = grid.cellAt(p);

        assertEquals(1, c.position().x());
        assertEquals(1, c.position().y());
    }

    /**
     * Vérifie que cellAt(...) gère les positions négatives sur un tore.
     *
     * Grille :
     * - largeur = 5
     * - hauteur = 3
     *
     * Position demandée :
     * - x = -1 => 4
     * - y = -1 => 2
     *
     * Donc la cellule attendue est (4,2).
     */
    @Test
    void cellAtWrapsNegativePositionOnTorus() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Grid.Position p = grid.new Position(-1, -1);
        Grid.Cell c = grid.cellAt(p);

        assertEquals(4, c.position().x());
        assertEquals(2, c.position().y());
    }

    /**
     * Vérifie l'invariant de toutes les cellules :
     *
     * Pour chaque position valide (x,y),
     * grid.cellAt(Position(x,y)).position()
     * doit être équivalente à Position(x,y).
     */
    @Test
    void cellAtPositionInvariantIsTrueForAllCells() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        for (int x = 0; x < grid.width(); x++) {
            for (int y = 0; y < grid.height(); y++) {
                Grid.Position p = grid.new Position(x, y);

                assertTrue(grid.cellAt(p).position().equiv(p));
            }
        }
    }

    // =========================================================
    // POSITION
    // =========================================================

    /**
     * Vérifie qu'une position déjà dans les bornes garde ses coordonnées.
     */
    @Test
    void positionInsideBoundsKeepsCoordinates() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Grid.Position p = grid.new Position(2, 1);

        assertEquals(2, p.x());
        assertEquals(1, p.y());
    }

    /**
     * Vérifie que la construction d'une position normalise
     * les coordonnées positives hors bornes sur un tore.
     *
     * Grille :
     * - largeur = 5
     * - hauteur = 3
     *
     * Position :
     * - x = 7 => 7 mod 5 = 2
     * - y = 5 => 5 mod 3 = 2
     */
    @Test
    void positionNormalizesPositiveCoordinatesOnTorus() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Grid.Position p = grid.new Position(7, 5);

        assertEquals(2, p.x());
        assertEquals(2, p.y());
    }

    /**
     * Vérifie que la construction d'une position normalise
     * les coordonnées négatives sur un tore.
     *
     * - x = -1 devient 4
     * - y = -1 devient 2
     */
    @Test
    void positionNormalizesNegativeCoordinatesOnTorus() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Grid.Position p = grid.new Position(-1, -1);

        assertEquals(4, p.x());
        assertEquals(2, p.y());
    }

    /**
     * Vérifie que copy() crée :
     * - une position équivalente ;
     * - mais un objet différent.
     *
     * C'est important pour l'encapsulation.
     */
    @Test
    void positionCopyCreatesEquivalentButDifferentObject() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Grid.Position p = grid.new Position(2, 1);
        Grid.Position copy = p.copy();

        assertNotSame(p, copy);
        assertTrue(p.equiv(copy));
        assertEquals(p, copy);
    }

    /**
     * Vérifie que equals(...) refuse :
     * - null ;
     * - les objets qui ne sont pas des Grid.Position.
     */
    @Test
    void positionEqualsRejectsNullAndOtherTypes() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Grid.Position p = grid.new Position(1, 1);

        assertNotEquals(null, p);
        assertNotEquals("not a position", p);
    }

    // =========================================================
    // VECTOR
    // =========================================================

    /**
     * Vérifie qu'un Grid.Vector peut être négatif.
     *
     * Pourquoi ?
     * Parce qu'un vecteur représente un déplacement.
     *
     * Exemple :
     * Vector(-1,0) signifie une cellule vers l'ouest.
     */
    @Test
    void vectorCanBeNegative() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Grid.Vector v = grid.new Vector(-1, 0);

        assertEquals(-1, v.x());
        assertEquals(0, v.y());
    }

    /**
     * Vérifie l'addition de vecteurs.
     *
     * v1 = (2,-1)
     * v2 = (-3,4)
     *
     * v1 + v2 = (-1,3)
     */
    @Test
    void vectorAddWorksWithPositiveAndNegativeValues() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Grid.Vector v1 = grid.new Vector(2, -1);
        Grid.Vector v2 = grid.new Vector(-3, 4);

        v1.add(v2);

        assertEquals(-1, v1.x());
        assertEquals(3, v1.y());
    }

    // =========================================================
    // TRANSLATION
    // =========================================================

    /**
     * Vérifie une translation simple sans dépassement des bornes.
     *
     * Départ :
     * Position(1,1)
     *
     * Vecteur :
     * Vector(2,1)
     *
     * Résultat :
     * Position(3,2)
     */
    @Test
    void positionTranslateInsideBounds() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Grid.Position p = grid.new Position(1, 1);
        Grid.Vector v = grid.new Vector(2, 1);

        p.translate(v);

        assertEquals(3, p.x());
        assertEquals(2, p.y());
    }

    /**
     * Vérifie une translation qui dépasse les bornes positives.
     *
     * Départ :
     * Position(4,2)
     *
     * Vecteur :
     * Vector(1,1)
     *
     * Sans tore : on obtiendrait (5,3).
     * Avec tore :
     * - x = 5 revient à 0
     * - y = 3 revient à 0
     */
    @Test
    void positionTranslateWrapsOnTorus() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Grid.Position p = grid.new Position(4, 2);
        Grid.Vector v = grid.new Vector(1, 1);

        p.translate(v);

        assertEquals(0, p.x());
        assertEquals(0, p.y());
    }

    /**
     * Vérifie une translation avec un vecteur négatif.
     *
     * Départ :
     * Position(0,0)
     *
     * Vecteur :
     * Vector(-1,-1)
     *
     * Avec tore :
     * - x = -1 devient 4
     * - y = -1 devient 2
     */
    @Test
    void positionTranslateWithNegativeVectorWrapsOnTorus() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Grid.Position p = grid.new Position(0, 0);
        Grid.Vector v = grid.new Vector(-1, -1);

        p.translate(v);

        assertEquals(4, p.x());
        assertEquals(2, p.y());
    }

    /**
     * Vérifie moveNorth.
     *
     * Comme l'axe Y est orienté vers le bas :
     * - aller vers le nord signifie diminuer y.
     *
     * Départ :
     * Position(2,1)
     *
     * moveNorth(1) donne :
     * Position(2,0)
     */
    @Test
    void moveNorthMovesUp() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Grid.Position p = grid.new Position(2, 1);

        p.moveNorth(1);

        assertEquals(2, p.x());
        assertEquals(0, p.y());
    }

    /**
     * Vérifie que moveNorth boucle sur un tore.
     *
     * Départ :
     * Position(2,0)
     *
     * moveNorth(1) donne y = -1.
     *
     * Sur une hauteur 3 :
     * -1 devient 2.
     */
    @Test
    void moveNorthWrapsOnTorus() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Grid.Position p = grid.new Position(2, 0);

        p.moveNorth(1);

        assertEquals(2, p.x());
        assertEquals(2, p.y());
    }

    // =========================================================
    // DISTANCE
    // =========================================================

    /**
     * Vérifie une distance simple sans besoin de passer par le bord.
     *
     * Position(1,1) à Position(3,1) :
     * distance = 2
     */
    @Test
    void distanceWithoutWrappingForClosePositions() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Grid.Position p1 = grid.new Position(1, 1);
        Grid.Position p2 = grid.new Position(3, 1);

        assertEquals(2.0, p1.distanceTo(p2));
    }

    /**
     * Vérifie que la distance utilise le plus court chemin sur l'axe X.
     *
     * Grille largeur = 5.
     *
     * Distance entre x=0 et x=4 :
     * - chemin direct = 4
     * - chemin par le bord = 1
     *
     * Donc distance = 1.
     */
    @Test
    void distanceUsesShortestPathOnTorusX() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Grid.Position p1 = grid.new Position(0, 0);
        Grid.Position p2 = grid.new Position(4, 0);

        assertEquals(1.0, p1.distanceTo(p2));
    }

    /**
     * Vérifie que la distance utilise le plus court chemin sur l'axe Y.
     *
     * Grille hauteur = 3.
     *
     * Distance entre y=0 et y=2 :
     * - chemin direct = 2
     * - chemin par le bord = 1
     *
     * Donc distance = 1.
     */
    @Test
    void distanceUsesShortestPathOnTorusY() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Grid.Position p1 = grid.new Position(0, 0);
        Grid.Position p2 = grid.new Position(0, 2);

        assertEquals(1.0, p1.distanceTo(p2));
    }

    /**
     * Vérifie la distance diagonale.
     *
     * Position(0,0) à Position(1,1) :
     *
     * dx = 1
     * dy = 1
     *
     * distance = sqrt(1² + 1²) = sqrt(2)
     */
    @Test
    void distanceDiagonalUsesEuclideanDistance() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Grid.Position p1 = grid.new Position(0, 0);
        Grid.Position p2 = grid.new Position(1, 1);

        assertEquals(Math.sqrt(2), p1.distanceTo(p2));
    }

    // =========================================================
    // DIMENSION
    // =========================================================

    /**
     * Vérifie qu'une Dimension stocke correctement ses valeurs positives.
     */
    @Test
    void dimensionStoresPositiveSize() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Grid.Dimension d = grid.new Dimension(2, 3);

        assertEquals(2, d.x());
        assertEquals(3, d.y());
    }

    /**
     * Vérifie equals(...) et equiv(...) pour Dimension.
     *
     * d1 = (2,3)
     * d2 = (2,3)
     * d3 = (3,2)
     *
     * d1 doit être égal à d2,
     * mais pas à d3.
     */
    @Test
    void dimensionEqualsEquivalentDimension() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Grid.Dimension d1 = grid.new Dimension(2, 3);
        Grid.Dimension d2 = grid.new Dimension(2, 3);
        Grid.Dimension d3 = grid.new Dimension(3, 2);

        assertEquals(d1, d2);
        assertTrue(d1.equiv(d2));
        assertNotEquals(d1, d3);
    }

    /**
     * Vérifie qu'une Dimension refuse les valeurs nulles.
     *
     * Une dimension représente une taille.
     * Donc 0 cellule de largeur ou de hauteur n'a pas de sens.
     *
     * Nécessite les assertions activées avec -ea.
     */
    @Test
    void dimensionRejectsZeroWhenAssertionsEnabled() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        assertThrows(AssertionError.class, () -> grid.new Dimension(0, 1));
        assertThrows(AssertionError.class, () -> grid.new Dimension(1, 0));
    }

    /**
     * Vérifie qu'une Dimension refuse les valeurs négatives.
     *
     * Une taille négative n'a pas de sens.
     *
     * Nécessite les assertions activées avec -ea.
     */
    @Test
    void dimensionRejectsNegativeWhenAssertionsEnabled() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        assertThrows(AssertionError.class, () -> grid.new Dimension(-1, 1));
        assertThrows(AssertionError.class, () -> grid.new Dimension(1, -1));
    }

    // =========================================================
    // CELL
    // =========================================================

    /**
     * Vérifie qu'une cellule possède :
     * - la bonne position ;
     * - une taille 1 x 1.
     *
     * Dans cette étape, toutes les cellules ont la taille Dimension(1,1).
     */
    @Test
    void cellHasPositionAndSizeOneByOne() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Grid.Position p = grid.new Position(2, 1);
        Grid.Cell c = grid.cellAt(p);

        assertTrue(c.position().equiv(p));
        assertEquals(1, c.size().x());
        assertEquals(1, c.size().y());
    }

    /**
     * Vérifie que Cell.position() renvoie une copie.
     *
     * On modifie la position retournée par c.position().
     * Si la cellule protège bien son état interne,
     * sa vraie position ne doit pas changer.
     */
    @Test
    void cellPositionReturnsCopy() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Grid.Position p = grid.new Position(2, 1);
        Grid.Cell c = grid.cellAt(p);

        Grid.Position copy = c.position();
        copy.translate(grid.new Vector(1, 0));

        assertTrue(c.position().equiv(p));
        assertFalse(c.position().equiv(copy));
    }

    /**
     * Vérifie qu'une cellule peut ajouter puis retirer une entité.
     */
    @Test
    void cellCanAddAndRemoveEntity() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Grid.Cell c = grid.cellAt(grid.new Position(1, 1));
        Entity e = new Entity("test");

        assertFalse(c.contains(e));

        c.add(e);
        assertTrue(c.contains(e));

        c.remove(e);
        assertFalse(c.contains(e));
    }

    /**
     * Vérifie qu'ajouter deux fois la même entité ne casse pas la cellule.
     *
     * Dans ton code, Cell.add(...) évite les doublons.
     */
    @Test
    void cellAddSameEntityTwiceStillContainsIt() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Grid.Cell c = grid.cellAt(grid.new Position(1, 1));
        Entity e = new Entity("test");

        c.add(e);
        c.add(e);

        assertTrue(c.contains(e));
    }

    // =========================================================
    // NON TORUS BEHAVIOR
    // =========================================================

    /**
     * Vérifie qu'en non-torique, une position dans les bornes fonctionne.
     */
    @Test
    void nonTorusPositionInsideBoundsWorks() {
        Game game = new NonTorusGame();
        Grid grid = game.grid();

        Grid.Position p = grid.new Position(2, 1);

        assertEquals(2, p.x());
        assertEquals(1, p.y());
    }

    /**
     * Vérifie qu'en non-torique, une position hors bornes provoque
     * une AssertionError dans cellAt(...).
     *
     * Grille largeur = 5.
     *
     * Les x valides sont :
     * 0, 1, 2, 3, 4
     *
     * Donc x = 5 est hors bornes.
     *
     * Nécessite les assertions activées avec -ea.
     */
    @Test
    void nonTorusCellAtOutOfBoundsFailsWhenAssertionsEnabled() {
        Game game = new NonTorusGame();
        Grid grid = game.grid();

        Grid.Position p = grid.new Position(5, 0);

        assertThrows(AssertionError.class, () -> grid.cellAt(p));
    }

    // =========================================================
    // ROTATION
    // =========================================================

    /**
     * Vérifie une rotation de 90 degrés autour de (0,0).
     *
     * Départ :
     * p = (1,0)
     *
     * Après rotation de 90° :
     * p = (0,1)
     */
    @Test
    void rotateAround90Degrees() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Grid.Position center = grid.new Position(0, 0);
        Grid.Position p = grid.new Position(1, 0);

        p.rotateAround(center, 90);

        assertEquals(0, p.x());
        assertEquals(1, p.y());
    }

    /**
     * Vérifie une rotation de 180 degrés.
     *
     * Départ :
     * p = (1,0)
     *
     * Mathématiquement :
     * après rotation 180° autour de (0,0),
     * p = (-1,0).
     *
     * Sur une grille torique de largeur 5 :
     * -1 devient 4.
     *
     * Donc p = (4,0).
     */
    @Test
    void rotateAround180Degrees() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Grid.Position center = grid.new Position(0, 0);
        Grid.Position p = grid.new Position(1, 0);

        p.rotateAround(center, 180);

        assertEquals(4, p.x());
        assertEquals(0, p.y());
    }

    /**
     * Vérifie une rotation de 270 degrés.
     *
     * Départ :
     * p = (1,0)
     *
     * Après rotation 270° autour de (0,0),
     * p = (0,-1).
     *
     * Sur une grille torique de hauteur 3 :
     * -1 devient 2.
     *
     * Donc p = (0,2).
     */
    @Test
    void rotateAround270Degrees() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Grid.Position center = grid.new Position(0, 0);
        Grid.Position p = grid.new Position(1, 0);

        p.rotateAround(center, 270);

        assertEquals(0, p.x());
        assertEquals(2, p.y());
    }

    /**
     * Vérifie toISUCoord().
     *
     * toISUCoord() donne le coin haut-gauche de la cellule.
     *
     * Rappel :
     * - l'origine ISU (0,0) est le centre de la cellule (0,0).
     *
     * Donc le coin haut-gauche de la cellule (0,0) est :
     *
     * (-0.5 * cmPerCell, -0.5 * cmPerCell)
     *
     * Comme cmPerCell = 10 :
     * attendu = (-5,-5).
     */
    @Test
    void positionToISUCoordReturnsTopLeftCorner() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Grid.Position p = grid.new Position(0, 0);

        ISU.Coord c = p.toISUCoord();

        assertEquals(45.0, c.x(), 0.000001);
        assertEquals(25.0, c.y(), 0.000001);
    }

    /**
     * Vérifie toISUCoord() pour une autre cellule.
     *
     * Position(2,1), cmPerCell = 10.
     *
     * Coin haut-gauche :
     * x = (2 - 0.5) * 10 = 15
     * y = (1 - 0.5) * 10 = 5
     */
    @Test
    void positionToISUCoordReturnsTopLeftCornerForOtherCell() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Grid.Position p = grid.new Position(2, 1);

        ISU.Coord c = p.toISUCoord();

        assertEquals(15.0, c.x(), 0.000001);
        assertEquals(5.0, c.y(), 0.000001);
    }

    /**
     * Vérifie la différence entre :
     * - toISUCoord()
     * - toISUCoordCentered()
     *
     * Pour Position(0,0) :
     *
     * toISUCoord() donne le coin haut-gauche :
     * (-5,-5)
     *
     * toISUCoordCentered() donne le centre :
     * (0,0)
     */
    @Test
    void positionToISUCoordAndCenteredCoordAreDifferent() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Grid.Position p = grid.new Position(0, 0);

        ISU.Coord topLeft = p.toISUCoord();
        ISU.Coord center = p.toISUCoordCentered();

        assertEquals(45.0, topLeft.x(), 0.000001);
        assertEquals(25.0, topLeft.y(), 0.000001);

        assertEquals(0.0, center.x(), 0.000001);
        assertEquals(0.0, center.y(), 0.000001);
    }
    
    @Test
    void gridShowWritesInformation() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        java.io.PrintStream ps = new java.io.PrintStream(out);

        grid.show(ps);

        String result = out.toString();

        assertTrue(result.contains("GRID"));
        assertTrue(result.contains("width_ncell"));
        assertTrue(result.contains("height_ncell"));
        assertTrue(result.contains("cmPerCell"));
        assertTrue(result.contains("5"));
        assertTrue(result.contains("3"));
        assertTrue(result.contains("10.0"));
    }
    
    @Test
    void cellShowWritesInformation() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Grid.Cell cell = grid.cellAt(grid.new Position(2, 1));

        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        java.io.PrintStream ps = new java.io.PrintStream(out);

        cell.show(ps);

        String result = out.toString();

        assertTrue(result.contains("Cell"));
        assertTrue(result.contains("Position"));
        assertTrue(result.contains("entities"));
    }
    
    @Test
    void cellShowWritesEntityCountAfterAddingEntity() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Grid.Cell cell = grid.cellAt(grid.new Position(2, 1));
        Entity entity = new Entity("test");

        cell.add(entity);

        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        java.io.PrintStream ps = new java.io.PrintStream(out);

        cell.show(ps);

        String result = out.toString();

        assertTrue(result.contains("entities = 1"));
    }
    
    @Test
    void vectorShowWritesInformation() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Grid.Vector v = grid.new Vector(-1, 2);

        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        java.io.PrintStream ps = new java.io.PrintStream(out);

        v.show(ps);

        String result = out.toString();

        assertTrue(result.contains("Vector"));
        assertTrue(result.contains("-1"));
        assertTrue(result.contains("2"));
        assertTrue(result.contains("ncell"));
    }
    
    @Test
    void dimensionShowWritesInformation() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Grid.Dimension d = grid.new Dimension(2, 3);

        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        java.io.PrintStream ps = new java.io.PrintStream(out);

        d.show(ps);

        String result = out.toString();

        assertTrue(result.contains("Dimension"));
        assertTrue(result.contains("2"));
        assertTrue(result.contains("3"));
        assertTrue(result.contains("ncell"));
    }
    
    @Test
    void positionShowWritesInformation() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Grid.Position p = grid.new Position(2, 1);

        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        java.io.PrintStream ps = new java.io.PrintStream(out);

        p.show(ps);

        String result = out.toString();

        assertTrue(result.contains("Position"));
        assertTrue(result.contains("2"));
        assertTrue(result.contains("1"));
        assertTrue(result.contains("ncell"));
    }
    
    @Test
    void cellAtRejectsNullPositionWhenAssertionsEnabled() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        assertThrows(AssertionError.class, () -> grid.cellAt(null));
    }

    @Test
    void vectorAddRejectsNullWhenAssertionsEnabled() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Grid.Vector v = grid.new Vector(1, 1);

        assertThrows(AssertionError.class, () -> v.add(null));
    }

    @Test
    void positionTranslateRejectsNullVectorWhenAssertionsEnabled() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Grid.Position p = grid.new Position(1, 1);

        assertThrows(AssertionError.class, () -> p.translate(null));
    }

    @Test
    void positionDistanceRejectsNullPositionWhenAssertionsEnabled() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Grid.Position p = grid.new Position(1, 1);

        assertThrows(AssertionError.class, () -> p.distanceTo(null));
    }

    @Test
    void positionRotateAroundRejectsNullCenterWhenAssertionsEnabled() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Grid.Position p = grid.new Position(1, 1);

        assertThrows(AssertionError.class, () -> p.rotateAround(null, 90));
    }

    @Test
    void cellConstructorRejectsNullPositionWhenAssertionsEnabled() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        assertThrows(AssertionError.class, () -> grid.new Cell(null));
    }

    @Test
    void cellAddRejectsNullEntityWhenAssertionsEnabled() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Grid.Cell cell = grid.cellAt(grid.new Position(0, 0));

        assertThrows(AssertionError.class, () -> cell.add(null));
    }

    @Test
    void cellRemoveRejectsNullEntityWhenAssertionsEnabled() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Grid.Cell cell = grid.cellAt(grid.new Position(0, 0));

        assertThrows(AssertionError.class, () -> cell.remove(null));
    }
    
    @Test
    void dimensionEqualsRejectsNullAndOtherTypes() {
        Game game = new TorusGame();
        Grid grid = game.grid();

        Grid.Dimension d = grid.new Dimension(2, 3);

        assertNotEquals(null, d);
        assertNotEquals("not a dimension", d);
    }
}