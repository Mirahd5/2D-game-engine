package test_engine;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import engine.model.Game;
import engine.model.Grid;
import engine.model.ISU;

/**
 * Tests de la classe Game.
 *
 * Game représente la configuration générale du monde de jeu.
 *
 * Elle contient notamment :
 * - la largeur et la hauteur en nombre de cellules ;
 * - la largeur et la hauteur en cm ;
 * - le facteur cmPerCell ;
 * - le facteur pixelPerCm ;
 * - les informations de tore sur X et Y ;
 * - la Grid ;
 * - l'ISU.
 *
 * L'objectif de ces tests est de vérifier :
 * - que les deux constructeurs fonctionnent ;
 * - que les dimensions sont cohérentes ;
 * - que Grid et ISU sont bien créés ;
 * - que Grid et ISU sont bien liés entre eux ;
 * - que les paramètres invalides sont refusés par assertions.
 */
public class GameTest {

    /**
     * Jeu concret minimal utilisé pour les tests.
     *
     * Comme Game est faite pour être spécialisée par un jeu concret,
     * on crée ici une petite sous-classe uniquement pour tester.
     *
     * Configuration :
     * - largeur  = 10 cellules
     * - hauteur  = 5 cellules
     * - axe X torique
     * - axe Y non torique
     * - une cellule = 3.7 cm
     * - un cm = 2 pixels
     */
    static class TestGame extends Game {
        TestGame() {
            super(
                10,
                5,
                true,
                false,
                3.7,
                2
            );
        }
    }

    /**
     * Même jeu de test, mais construit à partir des dimensions en cm.
     *
     * Ici :
     * - width_cm = 37.0
     * - height_cm = 18.5
     * - cmPerCell = 3.7
     *
     * Donc :
     * width_ncell = 37.0 / 3.7 = 10
     * height_ncell = 18.5 / 3.7 = 5
     */
    static class TestGameFromCm extends Game {
        TestGameFromCm() {
            super(
                37.0,
                18.5,
                true,
                false,
                3.7,
                2
            );
        }
    }

    // =========================================================
    // CONSTRUCTOR WITH NCELL DIMENSIONS
    // =========================================================

    /**
     * Vérifie le constructeur qui reçoit les dimensions en cellules.
     *
     * On donne :
     * - 10 cellules de largeur ;
     * - 5 cellules de hauteur ;
     * - cmPerCell = 3.7.
     *
     * Donc les dimensions en cm attendues sont :
     * - width_cm = 10 * 3.7 = 37.0
     * - height_cm = 5 * 3.7 = 18.5
     */
    @Test
    void gameInitializesDimensionsFromNCellConstructor() {
        Game game = new TestGame();

        assertEquals(10, game.width_ncell());
        assertEquals(5, game.height_ncell());

        assertEquals(37.0, game.width_cm(), 0.000001);
        assertEquals(18.5, game.height_cm(), 0.000001);

        assertEquals(3.7, game.cmPerCell(), 0.000001);
        assertEquals(2, game.pixelPerCm());

        assertTrue(game.torusOnXaxis());
        assertFalse(game.torusOnYaxis());
    }

    /**
     * Vérifie la cohérence mathématique entre :
     * - dimensions en cellules ;
     * - dimensions en cm.
     *
     * Invariant attendu :
     * width_cm  = width_ncell  * cmPerCell
     * height_cm = height_ncell * cmPerCell
     */
    @Test
    void gameDimensionsAreCoherentFromNCellConstructor() {
        Game game = new TestGame();

        assertEquals(
            game.width_ncell() * game.cmPerCell(),
            game.width_cm(),
            0.000001
        );

        assertEquals(
            game.height_ncell() * game.cmPerCell(),
            game.height_cm(),
            0.000001
        );
    }

    // =========================================================
    // CONSTRUCTOR WITH CM DIMENSIONS
    // =========================================================

    /**
     * Vérifie le constructeur qui reçoit les dimensions en cm.
     *
     * On donne :
     * - width_cm = 37.0
     * - height_cm = 18.5
     * - cmPerCell = 3.7
     *
     * Donc le constructeur doit retrouver :
     * - width_ncell = 10
     * - height_ncell = 5
     */
    @Test
    void gameInitializesDimensionsFromCmConstructor() {
        Game game = new TestGameFromCm();

        assertEquals(10, game.width_ncell());
        assertEquals(5, game.height_ncell());

        assertEquals(37.0, game.width_cm(), 0.000001);
        assertEquals(18.5, game.height_cm(), 0.000001);

        assertEquals(3.7, game.cmPerCell(), 0.000001);
        assertEquals(2, game.pixelPerCm());

        assertTrue(game.torusOnXaxis());
        assertFalse(game.torusOnYaxis());
    }

    /**
     * Vérifie aussi l'invariant de cohérence pour le constructeur en cm.
     *
     * Même si on construit avec des cm, à la fin on doit avoir :
     *
     * width_cm  = width_ncell  * cmPerCell
     * height_cm = height_ncell * cmPerCell
     */
    @Test
    void gameDimensionsAreCoherentFromCmConstructor() {
        Game game = new TestGameFromCm();

        assertEquals(
            game.width_ncell() * game.cmPerCell(),
            game.width_cm(),
            0.000001
        );

        assertEquals(
            game.height_ncell() * game.cmPerCell(),
            game.height_cm(),
            0.000001
        );
    }

    // =========================================================
    // GRID / ISU LINKS
    // =========================================================

    /**
     * Vérifie que Game crée bien les deux objets fondamentaux :
     *
     * - Grid : monde discret en cellules ;
     * - ISU  : monde réel en cm.
     */
    @Test
    void gameCreatesGridAndISU() {
        Game game = new TestGame();

        assertNotNull(game.grid());
        assertNotNull(game.isu());
    }

    /**
     * Vérifie que Grid et ISU sont bien liés ensemble.
     *
     * On veut :
     * - grid.isu() == game.isu()
     * - isu.grid() == game.grid()
     *
     * Pourquoi ?
     *
     * Parce que les conversions doivent fonctionner dans les deux sens :
     * - Grid.Position -> ISU.Coord
     * - ISU.Coord -> Grid.Position
     */
    @Test
    void gameGridAndISUAreCorrectlyLinkedTogether() {
        Game game = new TestGame();

        Grid grid = game.grid();
        ISU isu = game.isu();

        assertSame(isu, grid.isu());
        assertSame(grid, isu.grid());
    }

    /**
     * Vérifie que Grid a bien reçu les paramètres de Game.
     *
     * Grid travaille en nombre de cellules.
     *
     * Donc :
     * - grid.width() doit valoir game.width_ncell()
     * - grid.height() doit valoir game.height_ncell()
     *
     * Les axes de Grid sont aussi en nombre de cellules :
     * - grid.xAxis().perimeter() = width_ncell
     * - grid.yAxis().perimeter() = height_ncell
     */
    @Test
    void gridReceivesGameConfiguration() {
        Game game = new TestGame();
        Grid grid = game.grid();

        assertEquals(game.width_ncell(), grid.width());
        assertEquals(game.height_ncell(), grid.height());

        assertTrue(grid.xAxis().onTorus());
        assertFalse(grid.yAxis().onTorus());

        assertEquals(game.width_ncell(), grid.xAxis().perimeter(), 0.000001);
        assertEquals(game.height_ncell(), grid.yAxis().perimeter(), 0.000001);
    }

    /**
     * Vérifie que ISU a bien reçu les paramètres de Game.
     *
     * ISU travaille en cm.
     *
     * Donc :
     * - isu.cmPerCell() doit valoir game.cmPerCell()
     *
     * Les axes de ISU sont en cm :
     * - isu.xAxis().perimeter() = width_cm
     * - isu.yAxis().perimeter() = height_cm
     */
    @Test
    void isuReceivesGameConfiguration() {
        Game game = new TestGame();
        ISU isu = game.isu();

        assertEquals(game.cmPerCell(), isu.cmPerCell(), 0.000001);

        assertTrue(isu.xAxis().onTorus());
        assertFalse(isu.yAxis().onTorus());

        assertEquals(game.width_cm(), isu.xAxis().perimeter(), 0.000001);
        assertEquals(game.height_cm(), isu.yAxis().perimeter(), 0.000001);
    }

    // =========================================================
    // GEOMETRY CONSISTENCY
    // =========================================================

    /**
     * Vérifie la conversion :
     *
     * Grid.Position -> ISU.Coord
     *
     * Ici on teste toISUCoordCentered(), donc le centre de la cellule.
     *
     * Exemple :
     * position = (2,3)
     * cmPerCell = 3.7
     *
     * centre attendu :
     * x = 2 * 3.7
     * y = 3 * 3.7
     */
    @Test
    void gridPositionCanBeConvertedToISUCoordUsingGameScale() {
        Game game = new TestGame();
        Grid grid = game.grid();

        Grid.Position p = grid.new Position(2, 3);
        ISU.Coord c = p.toISUCoordCentered();

        assertEquals(2 * game.cmPerCell(), c.x(), 0.000001);
        assertEquals(3 * game.cmPerCell(), c.y(), 0.000001);
    }

    /**
     * Vérifie la conversion inverse :
     *
     * ISU.Coord -> Grid.Position
     *
     * Exemple :
     * Coord(2 * cmPerCell, 3 * cmPerCell)
     * doit donner Position(2,3).
     */
    @Test
    void isuCoordCanBeConvertedBackToGridPosition() {
        Game game = new TestGame();
        ISU isu = game.isu();

        ISU.Coord c = isu.new Coord(
            2 * game.cmPerCell(),
            3 * game.cmPerCell()
        );

        Grid.Position p = c.toGridPosition();

        assertEquals(2, p.x());
        assertEquals(3, p.y());
    }

    /**
     * Vérifie que la conversion aller-retour conserve la position.
     *
     * On fait :
     *
     * Grid.Position -> ISU.Coord centered -> Grid.Position
     *
     * Si la conversion est correcte, on doit retrouver une position équivalente.
     */
    @Test
    void gridPositionAndISUCoordConversionIsReversible() {
        Game game = new TestGame();
        Grid grid = game.grid();

        Grid.Position p1 = grid.new Position(4, 2);
        ISU.Coord c = p1.toISUCoordCentered();
        Grid.Position p2 = c.toGridPosition();

        assertTrue(p1.equiv(p2));
    }

    // =========================================================
    // GAME METHOD
    // =========================================================

    /**
     * Vérifie que game.game() retourne l'objet lui-même.
     *
     * Cette méthode peut être utile pour avoir une API uniforme :
     * un objet Game peut renvoyer son propre contexte.
     */
    @Test
    void gameMethodReturnsItself() {
        Game game = new TestGame();

        assertSame(game, game.game());
    }

    // =========================================================
    // INVALID PARAMETERS
    // =========================================================

    /**
     * Vérifie que le constructeur refuse les dimensions en cellules invalides.
     *
     * Les dimensions doivent être strictement positives :
     * - width_ncell > 0
     * - height_ncell > 0
     *
     * Ces tests nécessitent que les assertions soient activées avec -ea.
     */
    @Test
    void constructorRejectsInvalidNCellDimensionsWhenAssertionsEnabled() {
        assertThrows(AssertionError.class, () -> {
            new Game(0, 5, true, false, 3.7, 2) {};
        });

        assertThrows(AssertionError.class, () -> {
            new Game(10, 0, true, false, 3.7, 2) {};
        });

        assertThrows(AssertionError.class, () -> {
            new Game(-1, 5, true, false, 3.7, 2) {};
        });

        assertThrows(AssertionError.class, () -> {
            new Game(10, -1, true, false, 3.7, 2) {};
        });
    }

    /**
     * Vérifie que les échelles invalides sont refusées.
     *
     * Contraintes :
     * - cmPerCell > 0
     * - pixelPerCm > 0
     */
    @Test
    void constructorRejectsInvalidScaleWhenAssertionsEnabled() {
        assertThrows(AssertionError.class, () -> {
            new Game(10, 5, true, false, 0.0, 2) {};
        });

        assertThrows(AssertionError.class, () -> {
            new Game(10, 5, true, false, -1.0, 2) {};
        });

        assertThrows(AssertionError.class, () -> {
            new Game(10, 5, true, false, 3.7, 0) {};
        });

        assertThrows(AssertionError.class, () -> {
            new Game(10, 5, true, false, 3.7, -1) {};
        });
    }

    /**
     * Vérifie que les dimensions en cm invalides sont refusées.
     *
     * Les dimensions doivent être strictement positives :
     * - width_cm > 0
     * - height_cm > 0
     */
    @Test
    void constructorRejectsInvalidCmDimensionsWhenAssertionsEnabled() {
        assertThrows(AssertionError.class, () -> {
            new Game(0.0, 18.5, true, false, 3.7, 2) {};
        });

        assertThrows(AssertionError.class, () -> {
            new Game(37.0, 0.0, true, false, 3.7, 2) {};
        });

        assertThrows(AssertionError.class, () -> {
            new Game(-37.0, 18.5, true, false, 3.7, 2) {};
        });

        assertThrows(AssertionError.class, () -> {
            new Game(37.0, -18.5, true, false, 3.7, 2) {};
        });
    }

    /**
     * Vérifie que les dimensions en cm sont compatibles avec cmPerCell.
     *
     * Exemple :
     * width_cm = 37.0
     * cmPerCell = 3.7
     *
     * 37.0 / 3.7 = 10
     * donc c'est valide.
     *
     * Mais :
     * width_cm = 38.0
     * cmPerCell = 3.7
     *
     * 38.0 / 3.7 n'est pas un entier.
     * Donc on ne peut pas construire un nombre entier de cellules.
     */
    @Test
    void constructorRejectsCmDimensionsNotMultipleOfCmPerCellWhenAssertionsEnabled() {
        assertThrows(AssertionError.class, () -> {
            new Game(38.0, 18.5, true, false, 3.7, 2) {};
        });

        assertThrows(AssertionError.class, () -> {
            new Game(37.0, 19.0, true, false, 3.7, 2) {};
        });
    }
    
    @Test
    void showWritesGameInformation() {
        Game game = new TestGame();

        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        java.io.PrintStream ps = new java.io.PrintStream(out);

        game.show(ps);

        String result = out.toString();

        assertTrue(result.contains("GAME"));
        assertTrue(result.contains("width_ncell"));
        assertTrue(result.contains("height_ncell"));
        assertTrue(result.contains("width_cm"));
        assertTrue(result.contains("height_cm"));
        assertTrue(result.contains("cmPerCell"));
        assertTrue(result.contains("pixelPerCm"));
    }
    
    static class NonTorusTestGame extends Game {
        NonTorusTestGame() {
            super(
                4,
                6,
                false,
                true,
                2.5,
                3
            );
        }
    }
    
    @Test
    void gameInitializesAnotherNCellConfiguration() {
        Game game = new NonTorusTestGame();

        assertEquals(4, game.width_ncell());
        assertEquals(6, game.height_ncell());

        assertEquals(10.0, game.width_cm(), 0.000001);
        assertEquals(15.0, game.height_cm(), 0.000001);

        assertEquals(2.5, game.cmPerCell(), 0.000001);
        assertEquals(3, game.pixelPerCm());

        assertFalse(game.torusOnXaxis());
        assertTrue(game.torusOnYaxis());

        assertNotNull(game.grid());
        assertNotNull(game.isu());
    }
    
    static class NonTorusTestGameFromCm extends Game {
        NonTorusTestGameFromCm() {
            super(
                10.0,
                15.0,
                false,
                true,
                2.5,
                3
            );
        }
    }
    
    @Test
    void gameInitializesAnotherCmConfiguration() {
        Game game = new NonTorusTestGameFromCm();

        assertEquals(4, game.width_ncell());
        assertEquals(6, game.height_ncell());

        assertEquals(10.0, game.width_cm(), 0.000001);
        assertEquals(15.0, game.height_cm(), 0.000001);

        assertEquals(2.5, game.cmPerCell(), 0.000001);
        assertEquals(3, game.pixelPerCm());

        assertFalse(game.torusOnXaxis());
        assertTrue(game.torusOnYaxis());

        assertNotNull(game.grid());
        assertNotNull(game.isu());
    }
    
    
}