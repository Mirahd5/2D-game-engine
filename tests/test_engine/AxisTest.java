package test_engine;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import engine.model.Axis;

/**
 * Classe de tests pour Axis.
 *
 * Axis représente un axe du monde :
 * - soit normal ;
 * - soit torique.
 *
 * Un axe torique fonctionne comme Pacman :
 * si on sort d'un côté, on revient de l'autre.
 *
 * Exemple avec un axe de taille 5 :
 * - 0 reste 0
 * - 4 reste 4
 * - 5 revient à 0
 * - 6 revient à 1
 * - -1 revient à 4
 */
public class AxisTest {

    /**
     * Vérifie que les valeurs déjà dans l'intervalle restent inchangées.
     *
     * Axe torique de périmètre 5 :
     * les positions valides sont :
     *
     * [0, 1, 2, 3, 4]
     *
     * Donc normalize ne doit pas les modifier.
     */
    @Test
    void integerNormalizeKeepsValuesInsideTorusInterval() {
        Axis axis = new Axis(true, 5);

        assertEquals(0, axis.normalize(0));
        assertEquals(1, axis.normalize(1));
        assertEquals(2, axis.normalize(2));
        assertEquals(3, axis.normalize(3));
        assertEquals(4, axis.normalize(4));
    }

    /**
     * Vérifie que les valeurs positives hors bornes reviennent au début.
     *
     * Axe torique de périmètre 5 :
     *
     * 5 correspond à 0
     * 6 correspond à 1
     * 7 correspond à 2
     * 8 correspond à 3
     * 9 correspond à 4
     *
     * C'est exactement le comportement d'un modulo positif.
     */
    @Test
    void integerNormalizeWrapsPositiveValues() {
        Axis axis = new Axis(true, 5);

        assertEquals(0, axis.normalize(5));
        assertEquals(1, axis.normalize(6));
        assertEquals(2, axis.normalize(7));
        assertEquals(3, axis.normalize(8));
        assertEquals(4, axis.normalize(9));
    }

    /**
     * Vérifie que les valeurs négatives reviennent par la fin.
     *
     * Axe torique de périmètre 5 :
     *
     * -1 correspond à 4
     * -2 correspond à 3
     * -3 correspond à 2
     * -4 correspond à 1
     * -5 correspond à 0
     *
     * C'est important parce qu'en Java :
     *
     * -1 % 5 == -1
     *
     * Donc notre méthode modp doit corriger ce comportement
     * pour obtenir une valeur positive.
     */
    @Test
    void integerNormalizeWrapsNegativeValues() {
        Axis axis = new Axis(true, 5);

        assertEquals(4, axis.normalize(-1));
        assertEquals(3, axis.normalize(-2));
        assertEquals(2, axis.normalize(-3));
        assertEquals(1, axis.normalize(-4));
        assertEquals(0, axis.normalize(-5));
    }

    /**
     * Vérifie la propriété mathématique fondamentale du tore :
     *
     * i est équivalent à i + k * perimeter
     *
     * Exemple avec perimeter = 5 :
     *
     * 2 est équivalent à :
     * - 2 + 1*5 = 7
     * - 2 + 2*5 = 12
     * - 2 - 1*5 = -3
     * - 2 - 2*5 = -8
     *
     * Toutes ces valeurs doivent revenir à 2 après normalize.
     */
    @Test
    void integerNormalizeVerifiesTorusEquivalenceProperty() {
        Axis axis = new Axis(true, 5);

        int perimeter = 5;

        for (int i = 0; i < perimeter; i++) {
            for (int k = -10; k <= 10; k++) {
                assertEquals(i, axis.normalize(i + k * perimeter));
            }
        }
    }

    /**
     * Vérifie le comportement d'un axe NON torique.
     *
     * Si l'axe n'est pas torique, normalize ne modifie pas la valeur.
     *
     * Donc :
     * - 5 reste 5
     * - 7 reste 7
     * - -1 reste -1
     *
     * Cela permet ensuite à Grid.cellAt(...) de détecter les positions
     * hors bornes avec des assertions.
     */
    @Test
    void integerNormalizeWithoutTorusDoesNotWrap() {
        Axis axis = new Axis(false, 5);

        assertEquals(5, axis.normalize(5));
        assertEquals(7, axis.normalize(7));
        assertEquals(-1, axis.normalize(-1));
    }

    /**
     * Vérifie normalize(double) sur un axe torique.
     *
     * Différence avec normalize(int) :
     *
     * normalize(int) donne une valeur dans :
     * [0 ; perimeter[
     *
     * normalize(double) donne une valeur dans :
     * [-perimeter/2 ; perimeter/2[
     *
     * Ici perimeter = 10.
     *
     * L'intervalle attendu est donc :
     * [-5 ; 5[
     *
     * Exemples :
     *
     * 0  -> 0
     * 4  -> 4
     * 6  -> -4
     * 9  -> -1
     * -9 -> 1
     *
     * Pourquoi 6 devient -4 ?
     * Parce que sur un tore de longueur 10, avancer de 6 revient
     * à reculer de 4, ce qui est plus court.
     */
    @Test
    void realNormalizeWithTorusReturnsValueInHalfOpenInterval() {
        Axis axis = new Axis(true, 10.0);

        assertEquals(0.0, axis.normalizeDelta(0.0));
        assertEquals(4.0, axis.normalizeDelta(4.0));
        assertEquals(-4.0, axis.normalizeDelta(6.0));
        assertEquals(-1.0, axis.normalizeDelta(9.0));
        assertEquals(1.0, axis.normalizeDelta(-9.0));
    }

    /**
     * Vérifie que normalize(double) respecte aussi la propriété du tore.
     *
     * Une valeur x est équivalente à :
     *
     * x + k * perimeter
     *
     * Exemple avec x = 2.5 et perimeter = 10 :
     *
     * 2.5
     * 12.5
     * 22.5
     * -7.5
     * -17.5
     *
     * doivent toutes donner la même valeur normalisée.
     */
    @Test
    void realNormalizeVerifiesTorusEquivalenceProperty() {
        Axis axis = new Axis(true, 10.0);

        double x = 2.5;
        double perimeter = 10.0;

        for (int k = -10; k <= 10; k++) {
            assertEquals(axis.normalize(x), axis.normalize(x + k * perimeter));
        }
    }

    /**
     * Vérifie la distance sur un axe NON torique.
     *
     * Si l'axe n'est pas torique, la distance est simplement :
     *
     * abs(position2 - position1)
     *
     * Exemples :
     * distance(1, 9) = 8
     * distance(9, 1) = 8
     * distance(4, 4) = 0
     */
    @Test
    void distanceWithoutTorusIsAbsoluteDifference() {
        Axis axis = new Axis(false, 10.0);

        assertEquals(8.0, axis.distance(1.0, 9.0));
        assertEquals(8.0, axis.distance(9.0, 1.0));
        assertEquals(0.0, axis.distance(4.0, 4.0));
    }

    /**
     * Vérifie la distance sur un axe torique.
     *
     * Sur un tore, on prend toujours le chemin le plus court.
     *
     * Axe de périmètre 10 :
     *
     * distance(1, 9)
     * - chemin direct : 8
     * - chemin par le bord : 2
     * donc résultat = 2
     *
     * distance(9, 1)
     * même idée, résultat = 2
     *
     * distance(1, 5)
     * chemin direct = 4
     * chemin par bord = 6
     * donc résultat = 4
     *
     * distance(1, 11)
     * 11 est équivalent à 1 sur un tore de longueur 10
     * donc résultat = 0
     */
    @Test
    void distanceWithTorusUsesShortestPath() {
        Axis axis = new Axis(true, 10.0);

        assertEquals(2.0, axis.distance(1.0, 9.0));
        assertEquals(2.0, axis.distance(9.0, 1.0));
        assertEquals(4.0, axis.distance(1.0, 5.0));
        assertEquals(0.0, axis.distance(1.0, 11.0));
    }
    
    @Test
    void halfPerimeterReturnsHalfOfPerimeter() {
        Axis axis = new Axis(true, 10.0);

        assertEquals(5.0, axis.halfPerimeter(), 0.000001);
    }
    
    @Test
    void constructorRejectsInvalidPerimeterWhenAssertionsEnabled() {
        assertThrows(AssertionError.class, () -> new Axis(true, 0.0));
        assertThrows(AssertionError.class, () -> new Axis(false, -1.0));
    }
    
    @Test
    void integerModpReturnsPositiveModulo() {
        Axis axis = new Axis(true, 5);

        assertEquals(0, axis.modp(0, 5));
        assertEquals(1, axis.modp(1, 5));
        assertEquals(4, axis.modp(4, 5));
        assertEquals(0, axis.modp(5, 5));
        assertEquals(1, axis.modp(6, 5));
        assertEquals(4, axis.modp(-1, 5));
        assertEquals(3, axis.modp(-2, 5));
    }
    
    @Test
    void realModpRejectsInvalidPerimeterWhenAssertionsEnabled() {
        Axis axis = new Axis(true, 10.0);

        assertThrows(AssertionError.class, () -> axis.modp(1.0, 0.0));
        assertThrows(AssertionError.class, () -> axis.modp(1.0, -10.0));
    }
    
    @Test
    void realNormalizeWithoutTorusDoesNotWrap() {
        Axis axis = new Axis(false, 10.0);

        assertEquals(12.5, axis.normalize(12.5), 0.000001);
        assertEquals(-3.5, axis.normalize(-3.5), 0.000001);
    }
    
    
    @Test
    void realNormalizeJustBeforeHalfPerimeterStaysPositive() {
        Axis axis = new Axis(true, 10.0);

        assertEquals(4.999, axis.normalize(4.999), 0.000001);
    }
    
    @Test
    void realNormalizeAtHalfPerimeterGoesNegative() {
        Axis axis = new Axis(true, 10.0);

        assertEquals(-5.0, axis.normalizeDelta(5.0), 0.000001);
    }
    
    
}