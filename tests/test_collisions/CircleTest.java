package test_collisions;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import engine.intersection.Box;
import engine.intersection.Circle;
import engine.model.Game;
import engine.model.ISU;

public class CircleTest {

    static class TestGame extends Game {
        TestGame() {
            super(10, 10, true, true, 10.0, 2);
        }
    }

    @Test
    void circleBoxSurroundsCircle() {
        Game game = new TestGame();
        ISU isu = game.isu();

        Circle c = new Circle(isu.new Coord(20.0, 30.0), 5.0);

        Box box = c.box();

        assertEquals(15.0, box.minX(), 0.000001);
        assertEquals(25.0, box.minY(), 0.000001);
        assertEquals(25.0, box.maxX(), 0.000001);
        assertEquals(35.0, box.maxY(), 0.000001);
    }

    @Test
    void circleCircleIntersectionWorks() {
        Game game = new TestGame();
        ISU isu = game.isu();

        Circle c1 = new Circle(isu.new Coord(0.0, 0.0), 5.0);
        Circle c2 = new Circle(isu.new Coord(8.0, 0.0), 5.0);

        assertTrue(c1.intersects(c2));
        assertTrue(c2.intersects(c1));
    }

    @Test
    void circleCircleSeparatedDoesNotIntersect() {
        Game game = new TestGame();
        ISU isu = game.isu();

        Circle c1 = new Circle(isu.new Coord(0.0, 0.0), 5.0);
        Circle c2 = new Circle(isu.new Coord(20.0, 0.0), 5.0);

        assertFalse(c1.intersects(c2));
    }
}