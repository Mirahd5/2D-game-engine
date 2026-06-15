package junit.suites;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import engine.view.ViewPaintTest;
import engine.view.ViewTest;

@Suite
@SelectClasses({ ViewTest.class, ViewPaintTest.class })
public class EngineViewTestSuite {
}