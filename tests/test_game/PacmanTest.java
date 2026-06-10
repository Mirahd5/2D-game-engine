package test_game;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import game.model.Pacman;

/**
 * Tests de la classe Pacman.
 *
 * Pacman est un jeu concret qui hérite de Game.
 *
 * Game est la classe générique du moteur.
 * Pacman donne simplement une configuration particulière :
 * - taille du monde ;
 * - géométrie torique ou non ;
 * - échelle cm/cellule ;
 * - échelle pixel/cm.
 *
 * Ce test vérifie donc que le constructeur de Pacman appelle bien
 * le constructeur de Game avec les bons paramètres.
 */
public class PacmanTest {

    /**
     * Vérifie la configuration du jeu Pacman.
     *
     * Configuration attendue :
     *
     * - largeur  = 28 cellules
     * - hauteur  = 31 cellules
     * - axe X torique
     * - axe Y non torique
     * - 1 cellule = 3.7 cm
     * - 1 cm = 2 pixels
     *
     * Donc :
     *
     * width_cm  = 28 * 3.7
     * height_cm = 31 * 3.7
     *
     * On vérifie aussi que le jeu crée bien :
     * - une Grid ;
     * - un ISU.
     */
    @Test
    void pacmanConfigurationIsCorrect() {
        Pacman pacman = new Pacman();

        /*
         * Dimensions en nombre de cellules.
         */
        assertEquals(31, pacman.width_ncell());
        assertEquals(31, pacman.height_ncell());

        /*
         * Géométrie du monde.
         *
         * Ici :
         * - X est torique : on peut sortir à droite et revenir à gauche.
         * - Y n'est pas torique : on ne boucle pas verticalement.
         */
        assertTrue(pacman.torusOnXaxis());
        assertFalse(pacman.torusOnYaxis());

        /*
         * Échelles.
         */
        assertEquals(3.7, pacman.cmPerCell());
        assertEquals(7, pacman.pixelPerCm());

        /*
         * Dimensions réelles en cm.
         */
        assertEquals(31 * 3.7, pacman.width_cm());
        assertEquals(31 * 3.7, pacman.height_cm());

        /*
         * Objets du moteur créés par Game.
         */
        assertNotNull(pacman.grid());
        assertNotNull(pacman.isu());
    }
}