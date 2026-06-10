package test_engine;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import engine.model.Game;
import engine.model.Grid;
import engine.model.ISU;

/**
 * Tests de la classe ISU.
 *
 * ISU représente le repère réel du monde, exprimé en centimètres.
 *
 * Dans ces tests, on vérifie :
 * - que ISU est bien lié à Grid ;
 * - que cmPerCell est correct ;
 * - que les dimensions en cm fonctionnent ;
 * - que les coordonnées ISU se normalisent correctement ;
 * - que les conversions ISU -> Grid fonctionnent ;
 * - que les vecteurs ISU fonctionnent ;
 * - que les distances et rotations sont correctes.
 */
public class ISUTest {

    /**
     * Jeu torique utilisé pour tester les comportements avec boucle.
     *
     * Configuration :
     * - largeur = 5 cellules
     * - hauteur = 3 cellules
     * - axe X torique
     * - axe Y torique
     * - 1 cellule = 10 cm
     *
     * Donc :
     * - largeur ISU = 5 * 10 = 50 cm
     * - hauteur ISU = 3 * 10 = 30 cm
     */
    static class TorusGame extends Game {
        TorusGame() {
            super(5, 3, true, true, 10.0, 2);
        }
    }

    /**
     * Jeu non torique utilisé pour tester les coordonnées sans normalisation.
     *
     * Ici, si on met une coordonnée négative ou hors bornes,
     * elle n'est pas ramenée de l'autre côté.
     */
    static class NonTorusGame extends Game {
        NonTorusGame() {
            super(5, 3, false, false, 10.0, 2);
        }
    }

    /**
     * Vérifie que ISU connaît :
     * - sa Grid ;
     * - son échelle cmPerCell ;
     * - ses axes X et Y.
     */
    @Test
    void isuHasGridAndScale() {
        Game game = new TorusGame();
        ISU isu = game.isu();

        assertSame(game.grid(), isu.grid());
        assertEquals(10.0, isu.cmPerCell());
        assertNotNull(isu.xAxis());
        assertNotNull(isu.yAxis());
    }

    /**
     * Vérifie qu'une ISU.Dimension stocke bien ses valeurs positives.
     *
     * Une dimension représente une taille réelle en cm.
     *
     * Exemple :
     * Dimension(20,30)
     * signifie largeur 20 cm, hauteur 30 cm.
     */
    @Test
    void dimensionStoresPositiveValues() {
        Game game = new TorusGame();
        ISU.Dimension d = game.isu().new Dimension(20.0, 30.0);

        assertEquals(20.0, d.x());
        assertEquals(30.0, d.y());
        assertSame(game.isu(), d.isu());
    }

    /**
     * Vérifie qu'une dimension refuse les valeurs nulles ou négatives.
     *
     * Une dimension est une taille, donc elle doit être strictement positive.
     *
     * Ces tests nécessitent les assertions activées avec -ea.
     */
    @Test
    void dimensionRejectsZeroOrNegativeValuesWhenAssertionsEnabled() {
        Game game = new TorusGame();
        ISU isu = game.isu();

        assertThrows(AssertionError.class, () -> isu.new Dimension(0.0, 10.0));
        assertThrows(AssertionError.class, () -> isu.new Dimension(10.0, 0.0));
        assertThrows(AssertionError.class, () -> isu.new Dimension(-1.0, 10.0));
        assertThrows(AssertionError.class, () -> isu.new Dimension(10.0, -1.0));
    }

    /**
     * Vérifie equals(...) et equiv(...) pour ISU.Dimension.
     *
     * d1 = (10,20)
     * d2 = (10,20)
     * d3 = (20,10)
     *
     * d1 doit être égal à d2, mais pas à d3.
     */
    @Test
    void dimensionEqualsEquivalentDimension() {
        Game game = new TorusGame();
        ISU isu = game.isu();

        ISU.Dimension d1 = isu.new Dimension(10.0, 20.0);
        ISU.Dimension d2 = isu.new Dimension(10.0, 20.0);
        ISU.Dimension d3 = isu.new Dimension(20.0, 10.0);

        assertEquals(d1, d2);
        assertTrue(d1.equiv(d2));
        assertNotEquals(d1, d3);
        assertNotEquals(d1, "not a dimension");
    }

    /**
     * Vérifie que ISU.Dimension peut créer des vecteurs.
     *
     * Dimension(10,20).mkVector()
     * donne Vector(10,20)
     *
     * Dimension(10,20).mkScaledVector(2)
     * donne Vector(20,40)
     *
     * Dimension(10,20).mkScaledVector(2,3)
     * donne Vector(20,60)
     */
    @Test
    void dimensionCreatesVectors() {
        Game game = new TorusGame();
        ISU.Dimension d = game.isu().new Dimension(10.0, 20.0);

        ISU.Vector v1 = d.mkVector();
        assertEquals(10.0, v1.x());
        assertEquals(20.0, v1.y());

        ISU.Vector v2 = d.mkScaledVector(2.0);
        assertEquals(20.0, v2.x());
        assertEquals(40.0, v2.y());

        ISU.Vector v3 = d.mkScaledVector(2.0, 3.0);
        assertEquals(20.0, v3.x());
        assertEquals(60.0, v3.y());
    }

    /**
     * Vérifie que dans un monde non torique, les coordonnées restent inchangées.
     *
     * Coord(12,8) doit rester Coord(12,8).
     */
    @Test
    void coordStoresValuesInNonTorusWorld() {
        Game game = new NonTorusGame();
        ISU.Coord c = game.isu().new Coord(12.0, 8.0);

        assertEquals(12.0, c.x());
        assertEquals(8.0, c.y());
    }

    /**
     * Vérifie que dans un monde torique, les coordonnées ISU sont normalisées.
     *
     * Configuration :
     * - largeur = 5 cellules * 10 cm = 50 cm
     * - hauteur = 3 cellules * 10 cm = 30 cm
     *
     * Coord(60,40) devient :
     * - x = 60 modulo 50 = 10
     * - y = 40 modulo 30 = 10
     */
    @Test
    void coordNormalizesOnTorus() {
        Game game = new TorusGame();
        ISU isu = game.isu();

        ISU.Coord c = isu.new Coord(60.0, 40.0);

        assertEquals(10.0, c.x());
        assertEquals(10.0, c.y());
    }

    /**
     * Vérifie la translation d'une coordonnée.
     *
     * Départ :
     * Coord(0,0)
     *
     * Déplacement :
     * Vector(10,20)
     *
     * Sur l'axe X :
     * 0 + 10 = 10
     *
     * Sur l'axe Y :
     * 0 + 20 = 20
     *
     * Mais l'axe Y est torique de hauteur 30 cm.
     * Avec normalize(double), l'intervalle est [-15 ; 15[.
     * Donc 20 devient -10.
     */
    @Test
    void coordTranslateWorks() {
        Game game = new TorusGame();
        ISU isu = game.isu();

        ISU.Coord c = isu.new Coord(0.0, 0.0);
        c.translate(isu.new Vector(10.0, 20.0));

        assertEquals(10.0, c.x());
        assertEquals(20.0, c.y());
    }

    /**
     * Vérifie que mkCopy() crée une copie :
     * - équivalente ;
     * - mais différente comme objet Java.
     */
    @Test
    void coordCopyCreatesEquivalentButDifferentObject() {
        Game game = new TorusGame();
        ISU.Coord c = game.isu().new Coord(10.0, 0.0);

        ISU.Coord copy = c.mkCopy();

        assertNotSame(c, copy);
        assertEquals(c, copy);
    }

    /**
     * Vérifie la conversion ISU.Coord -> Grid.Position.
     *
     * cmPerCell = 10.
     *
     * Coord(20,10) devient :
     * - x = 20 / 10 = 2
     * - y = 10 / 10 = 1
     *
     * Donc Position(2,1).
     */
    @Test
    void coordToGridPositionWorks() {
        Game game = new TorusGame();
        ISU isu = game.isu();

        ISU.Coord c = isu.new Coord(20.0, 10.0);
        Grid.Position p = c.toGridPosition();

        assertEquals(2, p.x());
        assertEquals(1, p.y());
    }

    /**
     * Vérifie que mkTranslated(...) ne modifie pas la coordonnée de départ.
     *
     * Différence :
     * - translate(...) modifie l'objet courant ;
     * - mkTranslated(...) crée une nouvelle coordonnée.
     */
    @Test
    void coordMkTranslatedDoesNotModifyOriginal() {
        Game game = new TorusGame();
        ISU isu = game.isu();

        ISU.Coord c = isu.new Coord(0.0, 0.0);
        ISU.Coord moved = c.mkTranslated(isu.new Vector(10.0, 0.0));

        assertEquals(0.0, c.x());
        assertEquals(0.0, c.y());

        assertEquals(10.0, moved.x());
        assertEquals(0.0, moved.y());
    }

    /**
     * Vérifie la distance sur un tore.
     *
     * Largeur du monde = 50 cm.
     *
     * Distance entre x = 0 et x = 40 :
     * - chemin direct = 40
     * - chemin par le bord = 10
     *
     * Donc la distance doit être 10.
     */
    @Test
    void coordDistanceUsesTorusShortestPath() {
        Game game = new TorusGame();
        ISU isu = game.isu();

        ISU.Coord c1 = isu.new Coord(0.0, 0.0);
        ISU.Coord c2 = isu.new Coord(40.0, 0.0);

        assertEquals(10.0, c1.distanceTo(c2));
    }

    /**
     * Vérifie plusieurs opérations sur ISU.Vector :
     *
     * 1. norm()
     *    Vector(3,4) a une norme de 5.
     *
     * 2. unity()
     *    transforme le vecteur en vecteur de norme 1.
     *
     * 3. scale(10)
     *    multiplie la norme par 10.
     *
     * 4. add(Vector(1,2))
     *    additionne un autre vecteur.
     *
     * Détail :
     * Vector(3,4) après unity devient (0.6,0.8).
     * Après scale(10), il devient (6,8).
     * Après add(1,2), il devient (7,10).
     */
    @Test
    void vectorAddScaleNormAndUnityWork() {
        Game game = new TorusGame();
        ISU isu = game.isu();

        ISU.Vector v = isu.new Vector(3.0, 4.0);

        assertEquals(5.0, v.norm());

        v.unity();

        assertEquals(1.0, v.norm(), 0.000001);

        v.scale(10.0);

        assertEquals(10.0, v.norm(), 0.000001);

        v.add(isu.new Vector(1.0, 2.0));

        assertEquals(7.0, v.x(), 0.000001);
        assertEquals(10.0, v.y(), 0.000001);
    }

    /**
     * Vérifie la rotation d'un vecteur de 90 degrés.
     *
     * Départ :
     * Vector(1,0)
     *
     * Rotation de 90° :
     * Vector(0,1)
     *
     * Comme l'axe Y est orienté vers le bas,
     * 90° correspond au sud.
     */
    @Test
    void vectorTurn90Degrees() {
        Game game = new TorusGame();
        ISU.Vector v = game.isu().new Vector(1.0, 0.0);

        v.turn(90);

        assertEquals(0.0, v.x(), 0.000001);
        assertEquals(1.0, v.y(), 0.000001);
    }

    /**
     * Vérifie mkVectorToward(...) sans cas de tore compliqué.
     *
     * Source :
     * Coord(0,0)
     *
     * Target :
     * Coord(20,10)
     *
     * Vecteur attendu :
     * Vector(20,10)
     */
    @Test
    void coordMkVectorTowardWorks() {
        Game game = new TorusGame();
        ISU isu = game.isu();

        ISU.Coord source = isu.new Coord(0.0, 0.0);
        ISU.Coord target = isu.new Coord(20.0, 10.0);

        ISU.Vector v = source.mkVectorToward(target);

        assertEquals(20.0, v.x(), 0.000001);
        assertEquals(10.0, v.y(), 0.000001);
    }

    /**
     * Vérifie que mkVectorToward(...) utilise le plus court chemin sur un tore.
     *
     * Largeur = 50 cm.
     *
     * Source :
     * x = 0
     *
     * Target :
     * x = 40
     *
     * Chemin direct :
     * +40
     *
     * Chemin par le bord :
     * -10
     *
     * Donc le vecteur attendu est Vector(-10,0).
     */
    @Test
    void coordMkVectorTowardUsesShortestDirectionOnTorus() {
        Game game = new TorusGame();
        ISU isu = game.isu();

        ISU.Coord source = isu.new Coord(0.0, 0.0);
        ISU.Coord target = isu.new Coord(40.0, 0.0);

        ISU.Vector v = source.mkVectorToward(target);

        assertEquals(-10.0, v.x(), 0.000001);
        assertEquals(0.0, v.y(), 0.000001);
    }

    /**
     * Vérifie la rotation d'une coordonnée autour de l'origine.
     *
     * On utilise NonTorusGame pour éviter que les coordonnées négatives
     * soient normalisées par le tore.
     *
     * Départ :
     * Coord(10,0)
     *
     * Rotation de 90° autour de (0,0) :
     * Coord(0,10)
     */
    @Test
    void coordRotationAroundOrigin90Degrees() {
        Game game = new NonTorusGame();
        ISU isu = game.isu();

        ISU.Coord c = isu.new Coord(10.0, 0.0);

        c.rotation(90);

        assertEquals(0.0, c.x(), 0.000001);
        assertEquals(10.0, c.y(), 0.000001);
    }

    /**
     * Vérifie une rotation de 180 degrés autour de l'origine.
     *
     * Départ :
     * Coord(10,0)
     *
     * Rotation 180° :
     * Coord(-10,0)
     */
    @Test
    void coordRotationAroundOrigin180Degrees() {
        Game game = new NonTorusGame();
        ISU isu = game.isu();

        ISU.Coord c = isu.new Coord(10.0, 0.0);

        c.rotation(180);

        assertEquals(-10.0, c.x(), 0.000001);
        assertEquals(0.0, c.y(), 0.000001);
    }

    /**
     * Vérifie la rotation d'un point autour d'un autre point.
     *
     * Centre :
     * Coord(10,10)
     *
     * Point :
     * Coord(20,10)
     *
     * Le point est à droite du centre.
     *
     * Après rotation de 90° :
     * il se retrouve sous le centre :
     * Coord(10,20)
     */
    @Test
    void coordRotateAroundAnotherCoord90Degrees() {
        Game game = new NonTorusGame();
        ISU isu = game.isu();

        ISU.Coord center = isu.new Coord(10.0, 10.0);
        ISU.Coord point = isu.new Coord(20.0, 10.0);

        point.rotateAround(center, 90);

        assertEquals(10.0, point.x(), 0.000001);
        assertEquals(20.0, point.y(), 0.000001);
    }

    /**
     * Vérifie la rotation de 180° autour d'un autre point.
     *
     * Centre :
     * Coord(10,10)
     *
     * Point :
     * Coord(20,10)
     *
     * Après rotation 180° :
     * le point passe de l'autre côté du centre :
     * Coord(0,10)
     */
    @Test
    void coordRotateAroundAnotherCoord180Degrees() {
        Game game = new NonTorusGame();
        ISU isu = game.isu();

        ISU.Coord center = isu.new Coord(10.0, 10.0);
        ISU.Coord point = isu.new Coord(20.0, 10.0);

        point.rotateAround(center, 180);

        assertEquals(0.0, point.x(), 0.000001);
        assertEquals(10.0, point.y(), 0.000001);
    }

    /**
     * Vérifie que show(...) écrit quelque chose.
     * On utilise ByteArrayOutputStream pour capturer le texte écrit
     * dans le PrintStream.
     */
    @Test
    void coordShowWritesSomething() {
        Game game = new TorusGame();
        ISU.Coord c = game.isu().new Coord(10.0, 20.0);

        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        java.io.PrintStream ps = new java.io.PrintStream(out);

        c.show(ps);

        String result = out.toString();

        assertTrue(result.contains("ISU.Coord"));
        assertTrue(result.contains("10.0"));
    }
}