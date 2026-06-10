package Tests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import test_engine.AxisTest;
import test_engine.EntityTest;
import test_engine.GameTest;
import test_engine.GridTest;
import test_engine.ISUTest;
import test_game.PacmanTest;

@Suite
@SelectClasses({
    AxisTest.class,
    GameTest.class,
    GridTest.class,
    EntityTest.class,
    ISUTest.class,
    PacmanTest.class
})
public class TestSuite {
}