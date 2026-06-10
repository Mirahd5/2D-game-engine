package test_collisions;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import engine.intersection.Box;
import engine.model.Game;
import engine.model.ISU;


public class BoxTest {

    static class TestGame extends Game {
        TestGame() {
            super(10, 10, true, true, 10.0, 2);
        }
    }

    @Test
    void boxesIntersectWhenTheyOverlap() {
        Game game = new TestGame();
        ISU isu = game.isu();

        Box b1 = new Box(isu, 0.0, 0.0, 10.0, 10.0);
        Box b2 = new Box(isu, 5.0, 5.0, 15.0, 15.0);

        assertTrue(b1.intersects(b2));
        assertTrue(b2.intersects(b1));
    }

    @Test
    void boxesDoNotIntersectWhenSeparated() {
        Game game = new TestGame();
        ISU isu = game.isu();

        Box b1 = new Box(isu, 0.0, 0.0, 10.0, 10.0);
        Box b2 = new Box(isu, 20.0, 20.0, 30.0, 30.0);

        assertFalse(b1.intersects(b2));
        assertFalse(b2.intersects(b1));
    }

    @Test
    void boxesIntersectWhenTouchingBorder() {
        Game game = new TestGame();
        ISU isu = game.isu();

        Box b1 = new Box(isu, 0.0, 0.0, 10.0, 10.0);
        Box b2 = new Box(isu, 10.0, 0.0, 20.0, 10.0);

        assertTrue(b1.intersects(b2));
    }

    @Test
    void unionContainsBothBoxes() {
        Game game = new TestGame();
        ISU isu = game.isu();

        Box b1 = new Box(isu, 0.0, 0.0, 10.0, 10.0);
        Box b2 = new Box(isu, -5.0, 5.0, 20.0, 30.0);

        Box union = b1.union(b2);

        assertEquals(-5.0, union.minX(), 0.000001);
        assertEquals(0.0, union.minY(), 0.000001);
        assertEquals(20.0, union.maxX(), 0.000001);
        assertEquals(30.0, union.maxY(), 0.000001);
    }
}