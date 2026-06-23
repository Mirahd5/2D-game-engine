package junit.suites;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import engine.model.BoundingTest;
import engine.model.CircleTest;
import engine.model.RectTest;
import engine.model.ShapeTest;

@Suite
@SelectClasses({ ShapeTest.class, CircleTest.class, RectTest.class, BoundingTest.class })
public class EngineShapeTestSuite {
}