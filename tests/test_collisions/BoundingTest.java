package test_collisions;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import engine.intersection.Bounding;
import engine.intersection.Box;
import engine.intersection.Circle;
import engine.model.Game;
import engine.model.ISU;

public class BoundingTest {

    static class TestGame extends Game {
        TestGame() {
            super(10, 10, true, true, 10.0, 2);
        }
    }

    @Test
    void emptyBoundingIsEmpty() {
        Bounding b = new Bounding();

        assertTrue(b.isEmpty());
        assertEquals(0, b.size());
    }

    @Test
    void boundingCanAddShape() {
        Game game = new TestGame();
        ISU isu = game.isu();

        Bounding b = new Bounding();
        b.add(new Circle(isu.new Coord(0.0, 0.0), 5.0));

        assertFalse(b.isEmpty());
        assertEquals(1, b.size());
    }

    @Test
    void boundingIntersectsShape() {
        Game game = new TestGame();
        ISU isu = game.isu();

        Bounding b = new Bounding();
        b.add(new Circle(isu.new Coord(0.0, 0.0), 5.0));

        Circle other = new Circle(isu.new Coord(8.0, 0.0), 5.0);

        assertTrue(b.intersects(other));
    }

    @Test
    void boundingDoesNotIntersectSeparatedShape() {
        Game game = new TestGame();
        ISU isu = game.isu();

        Bounding b = new Bounding();
        b.add(new Circle(isu.new Coord(0.0, 0.0), 5.0));

        Circle other = new Circle(isu.new Coord(30.0, 0.0), 5.0);

        assertFalse(b.intersects(other));
    }

    @Test
    void twoBoundingsIntersect() {
        Game game = new TestGame();
        ISU isu = game.isu();

        Bounding b1 = new Bounding();
        b1.add(new Circle(isu.new Coord(0.0, 0.0), 5.0));

        Bounding b2 = new Bounding();
        b2.add(new Circle(isu.new Coord(8.0, 0.0), 5.0));

        assertTrue(b1.intersects(b2));
        assertTrue(b2.intersects(b1));
    }

    @Test
    void twoBoundingsDoNotIntersectWhenSeparated() {
        Game game = new TestGame();
        ISU isu = game.isu();

        Bounding b1 = new Bounding();
        b1.add(new Circle(isu.new Coord(0.0, 0.0), 5.0));

        Bounding b2 = new Bounding();
        b2.add(new Circle(isu.new Coord(30.0, 0.0), 5.0));

        assertFalse(b1.intersects(b2));
    }

    @Test
    void boundingBoxContainsAllShapes() {
        Game game = new TestGame();
        ISU isu = game.isu();

        Bounding b = new Bounding();

        b.add(new Circle(isu.new Coord(0.0, 0.0), 5.0));
        b.add(new Circle(isu.new Coord(20.0, 10.0), 2.0));

        Box box = b.box();

        assertEquals(-5.0, box.minX(), 0.000001);
        assertEquals(-5.0, box.minY(), 0.000001);
        assertEquals(22.0, box.maxX(), 0.000001);
        assertEquals(12.0, box.maxY(), 0.000001);
    }
}